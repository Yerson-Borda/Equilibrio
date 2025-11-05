import React, { useState, useEffect } from 'react';
import Sidebar from './Sidebar';
import Header from './Header';
import EmptyState from './dashboard/EmptyState';
import DashboardContent from './dashboard/DashboardContent';
import CreateWalletModal from './wallet/CreateWalletModal';
import { apiService } from '../services/api';
import { syncService } from '../services/syncService';
import { localDB } from '../services/localDB';

const Dashboard = () => {
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [wallets, setWallets] = useState([]);
    const [userStats, setUserStats] = useState({
        totalBalance: 0,
        totalSpending: 0,
        totalSaved: 0
    });

    useEffect(() => {
        loadData();
        setupAutomaticSync();
    }, []);

    const setupAutomaticSync = () => {
        // Auto-sync every 5 minutes
        const syncInterval = setInterval(() => {
            if (navigator.onLine) {
                syncService.sync().catch(error => {
                    console.error('Background sync failed:', error);
                });
            }
        }, 5 * 60 * 1000); // 5 minutes

        // Sync when coming online
        const handleOnline = () => {
            console.log('App came online, triggering sync...');
            syncService.sync().catch(error => {
                console.error('Online sync failed:', error);
            });
        };

        window.addEventListener('online', handleOnline);

        return () => {
            clearInterval(syncInterval);
            window.removeEventListener('online', handleOnline);
        };
    };

    const loadData = async () => {
        try {
            setIsLoading(true);

            // Load wallets with cache fallback
            try {
                const walletsData = await syncService.getDataWithFallback(
                    () => apiService.getWallets(),
                    'wallets'
                );
                setWallets(walletsData || []);
            } catch (error) {
                console.error('Error loading wallets:', error);
                setWallets([]);
            }

            // Load user stats with error handling
            try {
                const statsData = await apiService.getDetailedUserInfo();
                console.log('User stats response:', statsData);

                // Transform the API response to match the expected format
                setUserStats({
                    totalBalance: statsData.stats?.total_balance ||
                        statsData.total_balance ||
                        statsData.balance || 0,
                    totalSpending: statsData.stats?.total_spending ||
                        statsData.total_spending ||
                        statsData.spending || 0,
                    totalSaved: statsData.stats?.total_saved ||
                        statsData.total_saved ||
                        statsData.saved || 0
                });
            } catch (error) {
                console.error('Error loading user stats:', error);
                // Keep default values if API fails
            }

        } catch (error) {
            console.error('Error loading dashboard data:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleCreateWallet = async (walletData) => {
        try {
            setIsLoading(true);

            const formattedData = {
                name: walletData.name,
                currency: walletData.currency,
                wallet_type: walletData.wallet_type,
                initial_balance: parseFloat(walletData.initial_balance) || 0,
                card_number: walletData.card_number || '',
                color: walletData.color || '#6FBAFC'
            };

            console.log('Creating wallet:', formattedData);

            // Use sync service to queue the operation
            const newWallet = await syncService.queueOperation({
                type: 'CREATE_WALLET',
                entityType: 'wallets',
                data: formattedData
            });

            // Update local state optimistically
            setWallets(prev => [...prev, { ...newWallet, id: Date.now() }]); // Temporary ID

            setIsCreateModalOpen(false);

            // Refresh data to get the actual server response
            setTimeout(() => loadData(), 1000);

        } catch (error) {
            console.error('Error creating wallet:', error);
            alert(`Failed to create wallet: ${error.message || 'Please try again.'}`);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-background flex">
            <Sidebar activeItem="dashboard" />

            <div className="flex-1 ml-64 flex flex-col bg-background">
                <Header />

                {/* Removed sync status indicator */}

                <div className="flex-1 p-8 overflow-auto bg-background">
                    {isLoading && wallets.length === 0 ? (
                        <div className="flex items-center justify-center h-64">
                            <div className="text-lg text-metallic-gray">Loading...</div>
                        </div>
                    ) : wallets.length === 0 ? (
                        <EmptyState
                            onCreateWallet={() => setIsCreateModalOpen(true)}
                            userStats={userStats}
                        />
                    ) : (
                        <DashboardContent
                            wallets={wallets}
                            onCreateWallet={() => setIsCreateModalOpen(true)}
                            userStats={userStats}
                        />
                    )}
                </div>
            </div>

            <CreateWalletModal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                onSubmit={handleCreateWallet}
                isLoading={isLoading}
            />
        </div>
    );
};

export default Dashboard;