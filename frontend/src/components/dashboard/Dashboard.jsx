import React, { useState, useEffect } from 'react';
import Sidebar from './../layout/Sidebar';
import Header from './../layout/Header';
import EmptyState from '../dashboard/EmptyState';
import DashboardContent from '../dashboard/DashboardContent';
import CreateWalletModal from './../modals/CreateWalletModal';
import { apiService } from '../../services/api';
import { webSocketService } from '../../services/websocketService';

const Dashboard = ({ wallets: initialWallets, onWalletCreated, userStats: initialUserStats }) => {
    const [wallets, setWallets] = useState(initialWallets || []);
    const [userStats, setUserStats] = useState(
        initialUserStats || {
            totalBalance: 0,
            totalSpending: 0,
            totalSaved: 0,
            defaultCurrency: 'USD',
        }
    );
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [currentUser, setCurrentUser] = useState(null);

    const parseNumber = (value) => {
        if (value === null || value === undefined) return 0;
        const n = typeof value === 'string' ? parseFloat(value) : Number(value);
        return Number.isNaN(n) ? 0 : n;
    };

    const extractTotalBalance = (balanceData) => {
        if (balanceData == null) return 0;

        if (typeof balanceData === 'number') return balanceData;
        if (typeof balanceData === 'string') return parseNumber(balanceData);

        if (typeof balanceData === 'object') {
            if ('total_balance' in balanceData) {
                return parseNumber(balanceData.total_balance);
            }
            if ('balance' in balanceData) {
                return parseNumber(balanceData.balance);
            }
        }

        return 0;
    };

    const refreshUserStats = async () => {
        try {
            const [balanceData, summary, user] = await Promise.all([
                apiService.getUserTotalBalance(),
                apiService.getCurrentSummary(),
                apiService.getCurrentUser(),
            ]);

            const totalBalance = extractTotalBalance(balanceData);
            const totalIncome = parseNumber(summary?.total_income);
            const totalSpent = parseNumber(summary?.total_spent);
            const totalSaved = parseNumber(summary?.total_saved);
            const defaultCurrency = user?.default_currency || 'USD';

            setUserStats({
                totalBalance,
                totalSpending: totalSpent,
                totalSaved,
                defaultCurrency,
            });
        } catch (error) {
            console.error('Error refreshing user stats:', error);
        }
    };

    const refreshWallets = async () => {
        try {
            const walletsData = await apiService.getWallets();
            setWallets(walletsData || []);
        } catch (error) {
            console.error('Error refreshing wallets:', error);
        }
    };

    const loadCurrentUser = async () => {
        try {
            const userData = await apiService.getCurrentUser();
            setCurrentUser(userData);

            if (userData.id) {
                webSocketService.connect(userData.id);
            }
        } catch (error) {
            console.error('Error loading current user:', error);
        }
    };

    const handleWalletCreated = (newWallet) => {
        console.log('🆕 Adding new wallet to state:', newWallet);
        setWallets((prevWallets) => {
            const exists = prevWallets.find((w) => w.id === newWallet.id);
            if (exists) {
                console.log('ℹ️ Wallet already exists, updating instead');
                return prevWallets.map((w) => (w.id === newWallet.id ? newWallet : w));
            }
            console.log('✅ Adding new wallet to list');
            return [...prevWallets, newWallet];
        });

        refreshUserStats();

        if (typeof onWalletCreated === 'function') {
            onWalletCreated(newWallet);
        }
    };

    const handleWalletUpdated = (data) => {
        console.log('📝 Updating wallet in state:', data);
        setWallets((prevWallets) =>
            prevWallets.map((wallet) =>
                wallet.id === data.wallet_id
                    ? { ...wallet, balance: data.wallet_balance }
                    : wallet
            )
        );
        refreshUserStats();
    };

    const handleWalletDeleted = (walletId) => {
        console.log('🗑️ Removing wallet from state:', walletId);
        setWallets((prevWallets) =>
            prevWallets.filter((wallet) => wallet.id !== walletId)
        );
        refreshUserStats();
    };

    const handleTransactionUpdate = () => {
        console.log('🔄 Transaction update event received, refreshing wallets & stats');
        refreshWallets();
        refreshUserStats();
    };

    const setupWebSocketListeners = () => {
        webSocketService.addEventListener('wallet_created', (data) => {
            console.log('🔄 Real-time: Wallet created', data);
            handleWalletCreated(data.wallet || data);
        });

        webSocketService.addEventListener('wallet_updated', (data) => {
            console.log('🔄 Real-time: Wallet updated', data);
            handleWalletUpdated(data);
        });

        webSocketService.addEventListener('wallet_deleted', (data) => {
            console.log('🔄 Real-time: Wallet deleted', data);
            handleWalletDeleted(data.wallet_id);
        });

        webSocketService.addEventListener('transaction_created', () => {
            handleTransactionUpdate();
        });
        webSocketService.addEventListener('transaction_updated', () => {
            handleTransactionUpdate();
        });
        webSocketService.addEventListener('transaction_deleted', () => {
            handleTransactionUpdate();
        });

        webSocketService.addEventListener('connected', () => {
            console.log('✅ WebSocket connected (dashboard)');
        });
        webSocketService.addEventListener('disconnected', () => {
            console.log('⚠️ WebSocket disconnected (dashboard)');
        });
    };

    const cleanupWebSocketListeners = () => {
        webSocketService.removeEventListener('wallet_created');
        webSocketService.removeEventListener('wallet_updated');
        webSocketService.removeEventListener('wallet_deleted');
        webSocketService.removeEventListener('transaction_created');
        webSocketService.removeEventListener('transaction_updated');
        webSocketService.removeEventListener('transaction_deleted');
        webSocketService.removeEventListener('connected');
        webSocketService.removeEventListener('disconnected');
    };

    const handleCreateWallet = async (walletData) => {
        try {
            setIsLoading(true);
            console.log('🎯 Creating wallet with data:', walletData);
            const newWallet = await apiService.createWallet(walletData);
            console.log('✅ Wallet created successfully:', newWallet);
            handleWalletCreated(newWallet);
            setIsCreateModalOpen(false);
        } catch (error) {
            console.error('❌ Error creating wallet:', error);
            alert(
                `Failed to create wallet: ${error.message || 'Please try again.'}`
            );
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (initialWallets) {
            setWallets(initialWallets);
        }
        if (initialUserStats) {
            setUserStats(initialUserStats);
        }
    }, [initialWallets, initialUserStats]);

    useEffect(() => {
        loadCurrentUser();
        refreshUserStats();
        refreshWallets();
        setupWebSocketListeners();

        return () => {
            cleanupWebSocketListeners();
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <div className="min-h-screen bg-background flex">
            <Sidebar activeItem="dashboard" />

            <div className="flex-1 ml-64 flex flex-col bg-background">
                <Header />

                <div className="flex-1 p-8 overflow-auto bg-background">
                    {wallets.length === 0 ? (
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
