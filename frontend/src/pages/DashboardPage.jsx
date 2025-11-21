import React, { useState, useEffect } from 'react';
import AppLayout from '../components/layout/AppLayout';
import Dashboard from '../components/dashboard/Dashboard';
import { apiService } from '../services/api';

const DashboardPage = () => {
    const [wallets, setWallets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [userStats, setUserStats] = useState({
        totalBalance: 0,
        totalSpending: 0,
        totalSaved: 0,
        defaultCurrency: 'USD'
    });

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            setLoading(true);
            setError(null);
            console.log('🔄 Fetching dashboard data.');

            const walletsData = await apiService.getWallets();
            console.log('📋 Wallets loaded:', walletsData);
            setWallets(walletsData || []);

            const balanceData = await apiService.getUserTotalBalance();
            console.log('💰 Balance data:', balanceData);

            const totalSpending = await calculateTotalSpending();
            const totalSaved = await calculateTotalSaved();
            const userData = await apiService.getCurrentUser();

            const stats = {
                totalBalance: balanceData.total_balance || 0,
                totalSpending,
                totalSaved,
                defaultCurrency: userData.default_currency || 'USD'
            };

            console.log('📊 Final user stats:', stats);
            setUserStats(stats);
        } catch (error) {
            console.error('❌ Error fetching dashboard data:', error);
            if (error.status === 401) {
                window.location.href = '/login';
            } else {
                setError('Failed to load dashboard data');
            }
        } finally {
            setLoading(false);
        }
    };

    const calculateTotalSpending = async () => {
        try {
            const transactions = await apiService.getTransactions();
            const expenses = (transactions || []).filter(
                (t) => t.type === 'expense'
            );
            const total = expenses.reduce(
                (sum, transaction) => sum + parseFloat(transaction.amount),
                0
            );
            console.log('💸 Total spending calculated:', total);
            return total;
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
            const saved = Math.max(0, totalIncome - totalExpenses);
            console.log('🏦 Total saved calculated:', saved);
            return saved;
        } catch (error) {
            console.error('Error calculating savings:', error);
            return 0;
        }
    };

    const handleWalletCreated = (newWallet) => {
        console.log('🆕 Wallet created callback:', newWallet);
        // Dashboard handles real-time updates, keep this for future side-effects if needed
    };

    return (
        <AppLayout activeItem="dashboard">
            {loading ? (
                <div className="flex items-center justify-center h-full">
                    <div className="text-center">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue mx-auto"></div>
                        <p className="mt-4 text-text">Loading dashboard…</p>
                    </div>
                </div>
            ) : error ? (
                <div className="flex items-center justify-center h-full">
                    <div className="text-center">
                        <p className="text-red-500 text-lg">{error}</p>
                        <button
                            onClick={fetchDashboardData}
                            className="mt-4 bg-blue text-white px-4 py-2 rounded-lg"
                        >
                            Retry
                        </button>
                    </div>
                </div>
            ) : (
                <Dashboard
                    wallets={wallets}
                    onWalletCreated={handleWalletCreated}
                    userStats={userStats}
                />
            )}
        </AppLayout>
    );
};

export default DashboardPage;
