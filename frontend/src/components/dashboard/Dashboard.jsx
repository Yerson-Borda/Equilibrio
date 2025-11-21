import React, { useEffect, useState } from 'react';
import EmptyState from './EmptyState';
import DashboardContent from './DashboardContent';
import CreateWalletModal from '../modals/CreateWalletModal';
import { apiService } from '../../services/api';
import { webSocketService } from '../../services/websocketService';

const Dashboard = ({
                       wallets: initialWallets = [],
                       onWalletCreated,
                       userStats: initialUserStats
                   }) => {
    const [wallets, setWallets] = useState(initialWallets || []);
    const [userStats, setUserStats] = useState(
        initialUserStats || {
            totalBalance: 0,
            totalSpending: 0,
            totalSaved: 0,
            defaultCurrency: 'USD'
        }
    );
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        setWallets(initialWallets || []);
    }, [initialWallets]);

    useEffect(() => {
        if (initialUserStats) {
            setUserStats(initialUserStats);
        }
    }, [initialUserStats]);

    useEffect(() => {
        setupWebSocketListeners();
        return () => {
            cleanupWebSocketListeners();
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const setupWebSocketListeners = () => {
        webSocketService.addEventListener('wallet_created', (data) => {
            console.log('🔄 Real-time: Wallet created', data);
            if (data?.wallet) {
                handleWalletCreated(data.wallet);
            }
        });

        webSocketService.addEventListener('wallet_updated', (data) => {
            console.log('🔄 Real-time: Wallet updated', data);
            handleWalletUpdated(data);
        });

        webSocketService.addEventListener('wallet_deleted', (data) => {
            console.log('🔄 Real-time: Wallet deleted', data);
            if (data?.wallet_id) {
                handleWalletDeleted(data.wallet_id);
            }
        });

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
        setWallets((prevWallets = []) => {
            const exists = prevWallets.some((w) => w.id === newWallet.id);
            if (exists) {
                return prevWallets.map((w) =>
                    w.id === newWallet.id ? newWallet : w
                );
            }
            return [...prevWallets, newWallet];
        });

        refreshUserStats();

        if (onWalletCreated) {
            onWalletCreated(newWallet);
        }
    };

    const handleWalletUpdated = (data) => {
        console.log('📝 Updating wallet in state:', data);
        setWallets((prevWallets = []) =>
            prevWallets.map((wallet) =>
                wallet.id === data.wallet_id
                    ? {
                        ...wallet,
                        balance: data.wallet_balance ?? wallet.balance,
                        ...(data.wallet || {})
                    }
                    : wallet
            )
        );
        refreshUserStats();
    };

    const handleWalletDeleted = (walletId) => {
        console.log('🗑️ Removing wallet from state:', walletId);
        setWallets((prevWallets = []) =>
            prevWallets.filter((wallet) => wallet.id !== walletId)
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

            const updatedStats = {
                totalBalance: balanceData.total_balance || 0,
                totalSpending,
                totalSaved,
                defaultCurrency: userData.default_currency || 'USD'
            };

            console.log('📊 Refreshed user stats:', updatedStats);
            setUserStats(updatedStats);
        } catch (error) {
            console.error('Error refreshing user stats:', error);
        }
    };

    const calculateTotalSpending = async () => {
        try {
            const transactions = await apiService.getTransactions();
            const expenses = (transactions || []).filter(
                (t) => t.type === 'expense'
            );
            return expenses.reduce(
                (sum, transaction) => sum + parseFloat(transaction.amount),
                0
            );
        } catch (error) {
            console.error('Error calculating spending:', error);
            return 0;
        }
    };

    const calculateTotalSaved = async () => {
        try {
            const transactions = await apiService.getTransactions();
            const income = (transactions || []).filter(
                (t) => t.type === 'income'
            );
            const expenses = (transactions || []).filter(
                (t) => t.type === 'expense'
            );

            const totalIncome = income.reduce(
                (sum, transaction) => sum + parseFloat(transaction.amount),
                0
            );
            const totalExpenses = expenses.reduce(
                (sum, transaction) => sum + parseFloat(transaction.amount),
                0
            );

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

            handleWalletCreated(newWallet);
            setIsCreateModalOpen(false);

            alert('Wallet created successfully!');
        } catch (error) {
            console.error('❌ Error creating wallet:', error);
            alert(
                `Failed to create wallet: ${error.message || 'Please try again.'}`
            );
            await refreshWallets();
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <>
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

            {isCreateModalOpen && (
                <CreateWalletModal
                    isOpen={isCreateModalOpen}
                    onClose={() => setIsCreateModalOpen(false)}
                    onSubmit={handleCreateWallet}
                    isLoading={isLoading}
                />
            )}
        </>
    );
};

export default Dashboard;
