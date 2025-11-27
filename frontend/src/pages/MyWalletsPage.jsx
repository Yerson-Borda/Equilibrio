import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import Sidebar from '../components/layout/Sidebar';
import Header from '../components/layout/Header';
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
    // We only need the setter – currentUser is not read anywhere else
    const [, setCurrentUser] = useState(null);

    const location = useLocation();

    const fetchWallets = async () => {
        try {
            setLoading(true);
            console.log('🔄 Fetching wallets for MyWallets page...');
            const walletsData = await apiService.getWallets();
            console.log('📋 Wallets loaded in MyWallets:', walletsData);
            setWallets(walletsData || []);
        } catch (error) {
            console.error('❌ Error fetching wallets:', error);
            const status = error?.status || error?.response?.status;
            if (status === 401) {
                window.location.href = '/login';
            } else {
                setError('Failed to load wallets');
            }
        } finally {
            setLoading(false);
        }
    };

    const loadCurrentUser = async () => {
        try {
            const userData = await apiService.getCurrentUser();
            setCurrentUser(userData);

            // Connect to WebSocket with user ID
            if (userData?.id) {
                webSocketService.connect(userData.id);
            }
        } catch (error) {
            console.error('Error loading current user:', error);
        }
    };

    const handleWalletCreated = (newWallet) => {
        console.log('🆕 Adding new wallet to MyWallets state:', newWallet);
        setWallets((prevWallets) => {
            const exists = prevWallets.find((w) => w.id === newWallet.id);
            if (exists) {
                console.log('ℹ️ Wallet already exists in MyWallets, updating instead');
                return prevWallets.map((w) => (w.id === newWallet.id ? newWallet : w));
            }
            console.log('✅ Adding new wallet to MyWallets list');
            return [...prevWallets, newWallet];
        });
    };

    const handleWalletUpdated = (data) => {
        console.log('📝 Updating wallet in MyWallets state:', data);
        setWallets((prevWallets) =>
            prevWallets.map((wallet) =>
                wallet.id === data.wallet_id
                    ? { ...wallet, balance: data.wallet_balance }
                    : wallet
            )
        );
    };

    const handleWalletDeleted = (walletId) => {
        console.log('🗑️ Removing wallet from MyWallets state:', walletId);
        setWallets((prevWallets) =>
            prevWallets.filter((wallet) => wallet.id !== walletId)
        );
    };

    // When CreateWalletModal inside MyWalletsContent reports a new wallet,
    // we can rely on WebSocket to update the list, so we don't need to modify state here.
    const handleWalletCreatedCallback = (newWallet) => {
        console.log('🆕 Wallet created via callback in MyWallets:', newWallet);
        // No-op: WebSocket + fetchWallets handle updates
    };

    // Helper function to get category ID from category name
    const getCategoryId = async (categoryName) => {
        try {
            const categories = await apiService.getCategories();
            const category = categories.find((cat) => cat.name === categoryName);
            return category ? category.id : 1;
        } catch (error) {
            console.error('Error fetching categories:', error);
            return 1;
        }
    };

    const handleAddTransaction = async (transactionData) => {
        try {
            setIsCreatingTransaction(true);
            console.log('🎯 Creating transaction:', transactionData);

            if (transactionData.type === 'transfer') {
                const transferPayload = {
                    source_wallet_id: parseInt(transactionData.source_wallet_id, 10),
                    destination_wallet_id: parseInt(
                        transactionData.destination_wallet_id,
                        10
                    ),
                    amount: parseFloat(transactionData.amount),
                    note: transactionData.note || '',
                };

                console.log('📤 Sending transfer data:', transferPayload);
                const transferResult = await apiService.createTransfer(transferPayload);
                console.log('✅ Transfer created successfully:', transferResult);
            } else {
                // Format the transaction data for the API
                const formattedData = {
                    amount: parseFloat(transactionData.amount),
                    description: transactionData.note || '',
                    type: transactionData.type,
                    transaction_date: new Date().toISOString().split('T')[0],
                    wallet_id: parseInt(transactionData.wallet_id, 10),
                    category_id: await getCategoryId(transactionData.category),
                };

                console.log('📤 Sending transaction data:', formattedData);

                // Create the transaction via API
                const newTransaction = await apiService.createTransaction(formattedData);
                console.log('✅ Transaction created successfully:', newTransaction);
            }

            // Refresh wallets to update balances
            await fetchWallets();

            // Notify other components (like MyWalletsContent) to refresh transactions
            window.dispatchEvent(new Event('transaction_updated'));

            setIsTransactionModalOpen(false);
            setSelectedWallet(null);

            // Show success message
            alert(
                transactionData.type === 'transfer'
                    ? 'Transfer completed successfully!'
                    : 'Transaction added successfully!'
            );
        } catch (error) {
            console.error('❌ Error creating transaction:', error);
            alert(
                `Failed to create transaction: ${
                    error.message || 'Please try again.'
                }`
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

    // Main effect: load data + set up WebSocket listeners
    // eslint-disable-next-line react-hooks/exhaustive-deps
    useEffect(() => {
        fetchWallets();
        loadCurrentUser();

        // Check if we need to open transaction modal from dashboard navigation
        if (location.state?.openTransactionModal) {
            setIsTransactionModalOpen(true);
            setSelectedWallet(location.state.selectedWallet || null);
            // Clear the state to prevent reopening on refresh
            window.history.replaceState({}, document.title);
        }

        // Real-time WebSocket listeners
        const walletCreatedHandler = (data) => {
            console.log('🔄 Real-time: Wallet created in MyWallets', data);
            handleWalletCreated(data.wallet || data);
        };

        const walletUpdatedHandler = (data) => {
            console.log('🔄 Real-time: Wallet updated in MyWallets', data);
            handleWalletUpdated(data);
        };

        const walletDeletedHandler = (data) => {
            console.log('🔄 Real-time: Wallet deleted in MyWallets', data);
            handleWalletDeleted(data.wallet_id);
        };

        webSocketService.addEventListener('wallet_created', walletCreatedHandler);
        webSocketService.addEventListener('wallet_updated', walletUpdatedHandler);
        webSocketService.addEventListener('wallet_deleted', walletDeletedHandler);

        return () => {
            webSocketService.removeEventListener('wallet_created', walletCreatedHandler);
            webSocketService.removeEventListener('wallet_updated', walletUpdatedHandler);
            webSocketService.removeEventListener('wallet_deleted', walletDeletedHandler);
        };
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
                <div className="text-center">
                    <p className="text-red-500 text-lg">{error}</p>
                    <button
                        onClick={fetchWallets}
                        className="mt-4 bg-blue text-white px-4 py-2 rounded-lg"
                    >
                        Retry
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-background flex">
            <Sidebar activeItem="my-wallets" />
            <div className="flex-1 ml-64 flex flex-col bg-background">
                <Header />
                <div className="flex-1 p-8 overflow-auto bg-background">
                    <MyWalletsContent
                        wallets={wallets}
                        onWalletCreated={handleWalletCreatedCallback}
                        onAddTransaction={handleOpenTransactionModal}
                        onWalletDeleted={handleWalletDeleted}
                    />
                </div>
            </div>

            {/* Add Transaction Modal */}
            <AddTransactionModal
                isOpen={isTransactionModalOpen}
                onClose={handleCloseTransactionModal}
                onSubmit={handleAddTransaction}
                isLoading={isCreatingTransaction}
                wallets={wallets}
                selectedWallet={selectedWallet}
            />
        </div>
    );
};

export default MyWalletsPage;
