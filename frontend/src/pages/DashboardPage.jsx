import React, { useState, useEffect } from 'react';
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
        defaultCurrency: 'USD',
    });

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

    const fetchDashboardData = async () => {
        try {
            setLoading(true);
            setError(null);

            const [walletsData, userData, balanceData, summary] = await Promise.all([
                apiService.getWallets(),
                apiService.getCurrentUser(),
                apiService.getUserTotalBalance(),
                apiService.getCurrentSummary(),
            ]);

            setWallets(walletsData || []);

            const defaultCurrency = userData?.default_currency || 'USD';
            const totalBalance = extractTotalBalance(balanceData);

            const totalSpent = parseNumber(summary?.total_spent);
            const totalSaved = parseNumber(summary?.total_saved);

            setUserStats({
                totalBalance,
                totalSpending: totalSpent,
                totalSaved,
                defaultCurrency,
            });

            console.log('📊 Dashboard user stats from backend:', {
                totalBalance,
                totalSpending: totalSpent,
                totalSaved,
                defaultCurrency,
            });
        } catch (err) {
            console.error('❌ Error fetching dashboard data:', err);
            const status = err?.status || err?.response?.status;

            if (status === 401) {
                window.location.href = '/login';
            } else {
                setError('Failed to load dashboard data');
            }
        } finally {
            setLoading(false);
        }
    };

    // eslint-disable-next-line react-hooks/exhaustive-deps
    useEffect(() => {
        fetchDashboardData();// eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleWalletCreated = () => {
        // After a new wallet is created in <Dashboard />, refresh high-level stats
        fetchDashboardData();
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-background flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue mx-auto" />
                    <p className="mt-4 text-text">Loading dashboard...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen bg-background flex items-center justify-center">
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
