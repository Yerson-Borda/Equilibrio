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
    const [userStats, setUserStats] = useState(null);
    const [lastSync, setLastSync] = useState(null);

    useEffect(() => {
        loadData();
        setupSyncListener();
    }, []);

    const loadData = async () => {
        try {
            setIsLoading(true);

            // Load wallets with cache fallback
            const walletsData = await syncService.getDataWithFallback(
                () => apiService.getWallets(),
                'wallets'
            );
            setWallets(walletsData);

            // Load user stats
            const statsData = await apiService.getDetailedUserInfo();
            setUserStats(statsData);

            // Get last sync time
            const metadata = await localDB.getSyncMetadata();
            setLastSync(metadata.lastSyncAt);

        } catch (error) {
            console.error('Error loading dashboard data:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const setupSyncListener = () => {
        // Listen for sync completion
        const handleSyncComplete = () => {
            loadData(); // Refresh data after sync
        };

        // You can implement a custom event system or use a state management solution
        // For now, we'll use a simple interval to check for updates
        const interval = setInterval(async () => {
            const metadata = await localDB.getSyncMetadata();
            if (metadata.lastSyncAt !== lastSync) {
                setLastSync(metadata.lastSyncAt);
                loadData();
            }
        }, 30000); // Check every 30 seconds

        return () => clearInterval(interval);
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

    const handleRefresh = () => {
        loadData();
        syncService.sync();
    };

    return (
        <div className="min-h-screen bg-background flex">
            <Sidebar activeItem="dashboard" />

            <div className="flex-1 ml-64 flex flex-col bg-background">
                <Header />

                {/* Sync Status Indicator */}
                <div className="px-8 pt-4">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-4">
                            {lastSync && (
                                <div className="text-sm text-metallic-gray">
                                    Last synced: {new Date(lastSync).toLocaleTimeString()}
                                </div>
                            )}
                            {!navigator.onLine && (
                                <div className="text-sm text-orange-500 bg-orange-50 px-2 py-1 rounded">
                                    Offline Mode
                                </div>
                            )}
                        </div>
                        <button
                            onClick={handleRefresh}
                            disabled={isLoading}
                            className="text-sm text-blue hover:text-blue-600 flex items-center space-x-1"
                        >
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                            </svg>
                            <span>Refresh</span>
                        </button>
                    </div>
                </div>

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