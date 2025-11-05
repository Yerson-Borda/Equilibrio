import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import MyWalletsContent from '../components/wallet/MyWalletsContent';
import AddTransactionModal from '../components/wallet/AddTransactionModal';
import TransferModal from '../components/wallet/TransferModal';
import { apiService } from '../services/api';

const MyWalletsPage = () => {
    const [wallets, setWallets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isTransactionModalOpen, setIsTransactionModalOpen] = useState(false);
    const [isTransferModalOpen, setIsTransferModalOpen] = useState(false);
    const [selectedWallet, setSelectedWallet] = useState(null);
    const [isCreatingTransaction, setIsCreatingTransaction] = useState(false);
    const [isCreatingTransfer, setIsCreatingTransfer] = useState(false);

    const location = useLocation();

    useEffect(() => {
        fetchWallets();

        if (location.state?.openTransactionModal) {
            setIsTransactionModalOpen(true);
            setSelectedWallet(location.state.selectedWallet);
            window.history.replaceState({}, document.title);
        }
    }, [location.state]);

    const fetchWallets = async () => {
        try {
            setLoading(true);
            const walletsData = await apiService.getWallets();
            setWallets(walletsData || []);
        } catch (error) {
            console.error('Error fetching wallets:', error);
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
        setWallets(prev => [...prev, newWallet]);
    };

    const handleWalletUpdated = (updatedWallet) => {
        setWallets(prev => prev.map(wallet =>
            wallet.id === updatedWallet.id ? updatedWallet : wallet
        ));
    };

    const handleWalletDeleted = (walletId) => {
        setWallets(prev => prev.filter(wallet => wallet.id !== walletId));
    };

    const handleAddTransaction = async (transactionData) => {
        try {
            setIsCreatingTransaction(true);

            // Format the transaction data for the API
            const formattedData = {
                amount: parseFloat(transactionData.amount),
                description: transactionData.note || '',
                type: transactionData.type,
                transaction_date: new Date().toISOString().split('T')[0],
                wallet_id: parseInt(transactionData.wallet_id),
                category_id: await getCategoryId(transactionData.category)
            };

            console.log('Creating transaction:', formattedData);

            const newTransaction = await apiService.createTransaction(formattedData);

            await fetchWallets();

            setIsTransactionModalOpen(false);
            setSelectedWallet(null);

            alert('Transaction added successfully!');

        } catch (error) {
            console.error('Error creating transaction:', error);
            alert(`Failed to create transaction: ${error.message || 'Please try again.'}`);
        } finally {
            setIsCreatingTransaction(false);
        }
    };

    const handleTransfer = async (transferData) => {
        try {
            setIsCreatingTransfer(true);

            console.log('Creating transfer:', transferData);

            const result = await apiService.transferFunds(transferData);

            await fetchWallets();

            setIsTransferModalOpen(false);

            alert('Transfer completed successfully!');

        } catch (error) {
            console.error('Error creating transfer:', error);
            alert(`Failed to create transfer: ${error.message || 'Please try again.'}`);
        } finally {
            setIsCreatingTransfer(false);
        }
    };

    const getCategoryId = async (categoryName) => {
        try {
            const categories = await apiService.getCategories();
            const category = categories.find(cat => cat.name === categoryName);
            return category ? category.id : 1;
        } catch (error) {
            console.error('Error fetching categories:', error);
            return 1;
        }
    };

    const handleOpenTransactionModal = (wallet = null) => {
        setSelectedWallet(wallet);
        setIsTransactionModalOpen(true);
    };

    const handleOpenTransferModal = () => {
        setIsTransferModalOpen(true);
    };

    const handleCloseTransactionModal = () => {
        setIsTransactionModalOpen(false);
        setSelectedWallet(null);
    };

    const handleCloseTransferModal = () => {
        setIsTransferModalOpen(false);
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
        <div className="min-h-screen bg-background flex">
            <Sidebar activeItem="my-wallets" />
            <div className="flex-1 ml-64 flex flex-col bg-background">
                <Header />
                <div className="flex-1 p-8 overflow-auto bg-background">
                    {/* Add Transfer Button */}
                    <div className="flex justify-end mb-4">
                        <button
                            onClick={handleOpenTransferModal}
                            className="bg-white border-2 border-blue-500 text-blue-500 hover:bg-blue-50 px-6 py-3 rounded-lg font-semibold transition-colors duration-200"
                        >
                            💸 Transfer Funds
                        </button>
                    </div>

                    <MyWalletsContent
                        wallets={wallets}
                        onWalletCreated={handleWalletCreated}
                        onWalletUpdated={handleWalletUpdated}
                        onWalletDeleted={handleWalletDeleted}
                        onAddTransaction={handleOpenTransactionModal}
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

            {/* Transfer Modal */}
            <TransferModal
                isOpen={isTransferModalOpen}
                onClose={handleCloseTransferModal}
                onSubmit={handleTransfer}
                isLoading={isCreatingTransfer}
                wallets={wallets}
            />
        </div>
    );
};

export default MyWalletsPage;