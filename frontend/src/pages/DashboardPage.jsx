import React, { useState, useEffect } from 'react';
import Dashboard from '../components/Dashboard';
import { apiService } from '../services/api';

const DashboardPage = () => {
    const [wallets, setWallets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchWallets();
    }, []);

    const fetchWallets = async () => {
        try {
            setLoading(true);
            const walletsData = await apiService.getWallets();
            setWallets(walletsData || []);
        } catch (error) {
            console.error('Error fetching wallets:', error);
            setError('Failed to load wallets');
        } finally {
            setLoading(false);
        }
    };

    const handleWalletCreated = (newWallet) => {
        setWallets(prev => [...prev, newWallet]);
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
        />
    );
};

export default DashboardPage;