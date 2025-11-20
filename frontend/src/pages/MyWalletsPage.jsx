import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import AppLayout from '../components/layout/AppLayout';
import MyWalletsContent from '../components/wallet/MyWalletsContent';
import AddTransactionModal from '../components/modals/AddTransactionModal';
import { apiService } from '../services/api';
import { webSocketService } from '../services/websocketService';

const MyWalletsPage = () => {
    const [wallets, setWallets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isTransactionModalOpen, setIsTransactionModalOpen] = useState(false);
    const [selectedWallet, setSelectedWallet] = useState(null);
    const [isCreatingTransaction, setIsCreatingTransaction] = useState(false);
    const [currentUser, setCurrentUser] = useState(null);

    const location = useLocation();

    useEffect(() => {
        fetchWallets();
        loadCurrentUser();

        // Check if we need to open transaction modal from dashboard
        if (location.state?.openTransactionModal) {
            setIsTransactionModalOpen(true);
            setSelectedWallet(location.state.selectedWallet);
            // Clear the state to prevent reopening on refresh
            window.history.replaceState({}, document.title);
        }

        // Set up WebSocket listeners
        setupWebSocketListeners();

        return () => {
            cleanupWebSocketListeners();
        };
    }, [location.state]);

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
            console.log('🔄 Real-time: Wallet created in MyWallets', data);
            handleWalletCreated(data.wallet);
        });

        // Wallet updated event
        webSocketService.addEventListener('wallet_updated', (data) => {
            console.log('🔄 Real-time: Wallet updated in MyWallets', data);
            handleWalletUpdated(data);
        });

        // Wallet deleted event
        webSocketService.addEventListener('wallet_deleted', (data) => {
            console.log('🔄 Real-time: Wallet deleted in MyWallets', data);
            handleWalletDeleted(data.wallet_id);
        });
    };

    const cleanupWebSocketListeners = () => {
        webSocketService.removeEventListener('wallet_created');
        webSocketService.removeEventListener('wallet_updated');
        webSocketService.removeEventListener('wallet_deleted');
    };

    const fetchWallets = async () => {
        try {
            setLoading(true);
            console.log('🔄 Fetching wallets for MyWallets page.');
            const walletsData = await apiService.getWallets();
            console.log('📋 Wallets loaded in MyWallets:', walletsData);
            setWallets(walletsData || []);
        } catch (error) {
            console.error('❌ Error fetching wallets:', error);
            if (error.status === 401) {
                window.location.href = '/login';
            } else {
                setError('Failed to load wallets');
            }
        } finally {
            setLoading(false);
        }
    };

    const handleWalletCreated = (newWallet) => {
        setWallets((prev) => {
            const exists = prev.find((w) => w.id === newWallet.id);
            if (exists) {
                return prev.map((w) => (w.id === newWallet.id ? newWallet : w));
            }
            return [...prev, newWallet];
        });
    };

    const handleWalletUpdated = (data) => {
        setWallets((prev) =>
            prev.map((wallet) =>
                wallet.id === data.wallet_id
                    ? { ...wallet, ...data.wallet }
                    : wallet
            )
        );
    };

    const handleWalletDeleted = (walletId) => {
        setWallets((prev) => prev.filter((wallet) => wallet.id !== walletId));
    };

    const handleWalletCreatedCallback = (newWallet) => {
        handleWalletCreated(newWallet);
    };

    const handleAddTransaction = async (transactionData) => {
        try {
            setIsCreatingTransaction(true);

            if (transactionData.type === 'transfer') {
                const payload = {
                    source_wallet_id: parseInt(transactionData.source_wallet_id, 10),
                    destination_wallet_id: parseInt(
                        transactionData.destination_wallet_id,
                        10
                    ),
                    amount: parseFloat(transactionData.amount),
                    note: transactionData.note || '',
                };
                console.log('Creating transfer from MyWalletsPage:', payload);
                await apiService.createTransfer(payload);
            } else {
                const payload = {
                    amount: parseFloat(transactionData.amount),
                    description: transactionData.note || '',
                    type: transactionData.type,
                    transaction_date: new Date().toISOString().split('T')[0],
                    wallet_id: parseInt(transactionData.wallet_id, 10),
                    category_id: transactionData.category_id || null,
                };
                console.log('Creating transaction from MyWalletsPage:', payload);
                await apiService.createTransaction(payload);
            }

            // Notify other components that a transaction was updated
            window.dispatchEvent(new Event('transaction_updated'));
            setIsTransactionModalOpen(false);
            setSelectedWallet(null);
        } catch (error) {
            console.error('Error creating transaction from MyWalletsPage:', error);
            alert(
                error.message || 'Failed to create transaction. Please try again.'
            );
        } finally {
            setIsCreatingTransaction(false);
        }
    };

    const handleOpenTransactionModal = (wallet = null) => {
        setSelectedWallet(wallet);
        setIsTransactionModalOpen(true);
    };

    const handleCloseTransactionModal = () => {
        setIsTransactionModalOpen(false);
        setSelectedWallet(null);
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-background flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue mx-auto"></div>
                    <p className="mt-4 text-text">Loading wallets.</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <AppLayout activeItem="my-wallets">
                <div className="flex items-center justify-center h-full">
                    <p className="text-red-500">{error}</p>
                </div>
            </AppLayout>
        );
    }

    return (
        <AppLayout activeItem="my-wallets">
            <>
                <MyWalletsContent
                    wallets={wallets}
                    onWalletCreated={handleWalletCreatedCallback}
                    onAddTransaction={handleOpenTransactionModal}
                />

                {/* Add Transaction Modal */}
                <AddTransactionModal
                    isOpen={isTransactionModalOpen}
                    onClose={handleCloseTransactionModal}
                    onSubmit={handleAddTransaction}
                    isLoading={isCreatingTransaction}
                    wallets={wallets}
                    selectedWallet={selectedWallet}
                />
            </>
        </AppLayout>
    );
};

export default MyWalletsPage;
