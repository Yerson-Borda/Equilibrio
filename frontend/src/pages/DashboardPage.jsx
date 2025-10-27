import React, { useState, useEffect } from 'react';
import Dashboard from '../components/Dashboard';
import { apiService } from '../services/api';

const DashboardPage = () => {
    const [wallets, setWallets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [userStats, setUserStats] = useState({
        totalBalance: 0,
        totalSpending: 0,
        totalSaved: 0
    });

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            setLoading(true);

            // Fetch wallets
            const walletsData = await apiService.getWallets();
            setWallets(walletsData || []);

            // Fetch user total balance
            const balanceData = await apiService.getUserTotalBalance();

            // Calculate spending and savings (you might need to adjust this based on your backend)
            const totalSpending = await calculateTotalSpending();
            const totalSaved = await calculateTotalSaved();

            setUserStats({
                totalBalance: balanceData.total_balance || 0,
                totalSpending: totalSpending,
                totalSaved: totalSaved
            });

        } catch (error) {
            console.error('Error fetching dashboard data:', error);
            if (error.status === 401) {
                window.location.href = '/login';
            } else {
                setError('Failed to load dashboard data');
            }
        } finally {
            setLoading(false);
        }
    };

    // Helper function to calculate total spending
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

    // Helper function to calculate total saved
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

    const handleWalletCreated = (newWallet) => {
        setWallets(prev => [...prev, newWallet]);
        // Refresh stats after wallet creation
        fetchDashboardData();
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-background flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue mx-auto"></div>
                    <p className="mt-4 text-text">Loading...</p>
                </div>
            </div>
        );
    }

    return (
        <Dashboard
            wallets={wallets}
            onWalletCreated={handleWalletCreated}
            userStats={userStats}
        />
    );
};

export default DashboardPage;