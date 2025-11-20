import React, { useState, useEffect } from 'react';
import Sidebar from './Sidebar';
import Header from './Header';
import EmptyState from './dashboard/EmptyState';
import DashboardContent from './dashboard/DashboardContent';
import CreateWalletModal from './modals/CreateWalletModal.jsx';
import { apiService } from '../services/api';
import { webSocketService } from '../services/websocketService';

const Dashboard = ({ wallets: initialWallets, onWalletCreated, userStats: initialUserStats }) => {
    const [wallets, setWallets] = useState(initialWallets || []);
    const [userStats, setUserStats] = useState(initialUserStats || {
        totalBalance: 0,
        totalSpending: 0,
        totalSaved: 0,
        defaultCurrency: 'USD'
    });
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [currentUser, setCurrentUser] = useState(null);

    useEffect(() => {
        // Load current user for WebSocket connection
        loadCurrentUser();

        // Set up WebSocket listeners
        setupWebSocketListeners();

        return () => {
            // Clean up WebSocket listeners
            cleanupWebSocketListeners();
        };
    }, []);

    const loadCurrentUser = async () => {
        try {
            const userData = await apiService.getCurrentUser();
            setCurrentUser(userData);

            // Connect to WebSocket with user ID
            if (userData.id) {
                webSocketService.connect(userData.id);
            }
        } catch (error) {
            console.error('Error loading current user:', error);
        }
    };

    const setupWebSocketListeners = () => {
        // Wallet created event
        webSocketService.addEventListener('wallet_created', (data) => {
            console.log('🔄 Real-time: Wallet created', data);
            handleWalletCreated(data.wallet);
        });

        // Wallet updated event
        webSocketService.addEventListener('wallet_updated', (data) => {
            console.log('🔄 Real-time: Wallet updated', data);
            handleWalletUpdated(data);
        });

        // Wallet deleted event
        webSocketService.addEventListener('wallet_deleted', (data) => {
            console.log('🔄 Real-time: Wallet deleted', data);
            handleWalletDeleted(data.wallet_id);
        });

        // Transaction events that affect wallet balances
        webSocketService.addEventListener('transaction_created', (data) => {
            console.log('🔄 Real-time: Transaction created', data);
            handleTransactionUpdate();
        });

        webSocketService.addEventListener('transaction_updated', (data) => {
            console.log('🔄 Real-time: Transaction updated', data);
            handleTransactionUpdate();
        });

        webSocketService.addEventListener('transaction_deleted', (data) => {
            console.log('🔄 Real-time: Transaction deleted', data);
            handleTransactionUpdate();
        });

        // WebSocket connection status
        webSocketService.addEventListener('connected', () => {
            console.log('✅ WebSocket connected - real-time updates enabled');
        });

        webSocketService.addEventListener('disconnected', () => {
            console.log('❌ WebSocket disconnected - real-time updates disabled');
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

    const handleWalletCreated = (newWallet) => {
        console.log('🆕 Adding new wallet to state:', newWallet);
        setWallets(prevWallets => {
            // Check if wallet already exists to avoid duplicates
            const exists = prevWallets.find(w => w.id === newWallet.id);
            if (exists) {
                console.log('ℹ️ Wallet already exists, updating instead');
                return prevWallets.map(w => w.id === newWallet.id ? newWallet : w);
            }
            console.log('✅ Adding new wallet to list');
            return [...prevWallets, newWallet];
        });

        // Refresh user stats after wallet creation
        refreshUserStats();
    };

    const handleWalletUpdated = (data) => {
        console.log('📝 Updating wallet in state:', data);
        setWallets(prevWallets =>
            prevWallets.map(wallet =>
                wallet.id === data.wallet_id
                    ? { ...wallet, balance: data.wallet_balance }
                    : wallet
            )
        );
        refreshUserStats();
    };

    const handleWalletDeleted = (walletId) => {
        console.log('🗑️ Removing wallet from state:', walletId);
        setWallets(prevWallets =>
            prevWallets.filter(wallet => wallet.id !== walletId)
        );
        refreshUserStats();
    };

    const handleTransactionUpdate = () => {
        console.log('💰 Transaction update - refreshing wallets and stats');
        refreshWallets();
        refreshUserStats();
    };

    const refreshWallets = async () => {
        try {
            const walletsData = await apiService.getWallets();
            console.log('🔄 Refreshed wallets:', walletsData);
            setWallets(walletsData || []);
        } catch (error) {
            console.error('Error refreshing wallets:', error);
        }
    };

    const refreshUserStats = async () => {
        try {
            const balanceData = await apiService.getUserTotalBalance();
            const totalSpending = await calculateTotalSpending();
            const totalSaved = await calculateTotalSaved();
            const userData = await apiService.getCurrentUser();

            console.log('📊 Refreshed user stats:', {
                totalBalance: balanceData.total_balance || 0,
                totalSpending,
                totalSaved,
                defaultCurrency: userData.default_currency || 'USD'
            });

            setUserStats({
                totalBalance: balanceData.total_balance || 0,
                totalSpending,
                totalSaved,
                defaultCurrency: userData.default_currency || 'USD'
            });
        } catch (error) {
            console.error('Error refreshing user stats:', error);
        }
    };

    const calculateTotalSpending = async () => {
        try {
            const transactions = await apiService.getTransactions();
            const expenses = transactions.filter(t => t.type === 'expense');
            return expenses.reduce((sum, transaction) => sum + parseFloat(transaction.amount), 0);
        } catch (error) {
            console.error('Error calculating spending:', error);
            return 0;
        }
    };

    const calculateTotalSaved = async () => {
        try {
            const transactions = await apiService.getTransactions();
            const income = transactions.filter(t => t.type === 'income');
            const expenses = transactions.filter(t => t.type === 'expense');
            const totalIncome = income.reduce((sum, transaction) => sum + parseFloat(transaction.amount), 0);
            const totalExpenses = expenses.reduce((sum, transaction) => sum + parseFloat(transaction.amount), 0);
            return Math.max(0, totalIncome - totalExpenses);
        } catch (error) {
            console.error('Error calculating savings:', error);
            return 0;
        }
    };

    const handleCreateWallet = async (walletData) => {
        try {
            setIsLoading(true);
            console.log('🎯 Creating wallet with data:', walletData);

            const formattedData = {
                name: walletData.name,
                currency: walletData.currency,
                wallet_type: walletData.wallet_type,
                initial_balance: parseFloat(walletData.initial_balance) || 0,
                card_number: walletData.card_number || '',
                color: walletData.color || '#6FBAFC'
            };

            const newWallet = await apiService.createWallet(formattedData);
            console.log('✅ Wallet created successfully:', newWallet);

            // Update local state immediately (optimistic update)
            handleWalletCreated(newWallet);

            setIsCreateModalOpen(false);

            // Show success message
            alert('Wallet created successfully!');

        } catch (error) {
            console.error('❌ Error creating wallet:', error);
            alert(`Failed to create wallet: ${error.message || 'Please try again.'}`);

            // Revert optimistic update if needed
            refreshWallets();
        } finally {
            setIsLoading(false);
        }
    };

    // Initial data load
    useEffect(() => {
        if (initialWallets) {
            setWallets(initialWallets);
        }
        if (initialUserStats) {
            setUserStats(initialUserStats);
        }
    }, [initialWallets, initialUserStats]);

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