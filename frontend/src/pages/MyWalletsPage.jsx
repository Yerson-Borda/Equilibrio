import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import Sidebar from '../components/layout/Sidebar';
import Header from '../components/layout/Header';
import MyWalletsContent from '../components/wallet/MyWalletsContent';
import { apiService } from '../services/api';
import { webSocketService } from '../services/websocketService';

const MyWalletsPage = () => {
    const [wallets, setWallets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // We only need the setter – currentUser is not read anywhere else
    const [, setCurrentUser] = useState(null);

    const location = useLocation();

    const fetchWallets = async () => {
        try {
            setLoading(true);
            const walletsData = await apiService.getWallets();
            setWallets(walletsData || []);
        } catch (err) {
            console.error('❌ Error fetching wallets:', err);
            const status = err?.status || err?.response?.status;
            if (status === 401) window.location.href = '/login';
            else setError('Failed to load wallets');
        } finally {
            setLoading(false);
        }
    };

    const loadCurrentUser = async () => {
        try {
            const userData = await apiService.getCurrentUser();
            setCurrentUser(userData);

            if (userData?.id) {
                webSocketService.connect(userData.id);
            }
        } catch (err) {
            console.error('Error loading current user:', err);
        }
    };

    useEffect(() => {
        fetchWallets();
        loadCurrentUser();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [location.state]);

    if (loading) {
        return (
            <div className="min-h-screen bg-background flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue mx-auto" />
                    <p className="mt-4 text-text">Loading wallets...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen bg-background flex items-center justify-center">
                <p className="text-red-500">{error}</p>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-background flex">
            <Sidebar />
            <div className="flex-1">
                <Header />
                <MyWalletsContent
                    wallets={wallets}
                    // Open inline add transaction if dashboard navigates with state.openTransactionModal
                    openTransactionOnLoad={!!location.state?.openTransactionModal}
                    defaultWalletId={location.state?.selectedWallet?.id || null}
                    onWalletCreated={() => fetchWallets()}
                    onWalletDeleted={() => fetchWallets()}
                />
            </div>
        </div>
    );
};

export default MyWalletsPage;
