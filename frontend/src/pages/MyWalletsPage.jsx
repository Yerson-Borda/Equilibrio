import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import MyWalletsContent from '../components/wallet/MyWalletsContent';
import AddTransactionModal from '../components/wallet/AddTransactionModal';
import { apiService } from '../services/api';

const MyWalletsPage = () => {
    const [wallets, setWallets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isTransactionModalOpen, setIsTransactionModalOpen] = useState(false);
    const [selectedWallet, setSelectedWallet] = useState(null);
    const [isCreatingTransaction, setIsCreatingTransaction] = useState(false);

    const location = useLocation();

    useEffect(() => {
        fetchWallets();

        // Check if we need to open transaction modal from dashboard
        if (location.state?.openTransactionModal) {
            setIsTransactionModalOpen(true);
            setSelectedWallet(location.state.selectedWallet);
            // Clear the state to prevent reopening on refresh
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

    const handleAddTransaction = async (transactionData) => {
        try {
            setIsCreatingTransaction(true);

            // Format the transaction data for the API
            const formattedData = {
                amount: parseFloat(transactionData.amount),
                description: transactionData.note || '',
                type: transactionData.type,
                transaction_date: new Date().toISOString().split('T')[0], // Today's date
                wallet_id: parseInt(transactionData.wallet_id),
                category_id: await getCategoryId(transactionData.category)
            };

            console.log('Creating transaction:', formattedData);

            // Create the transaction via API
            const newTransaction = await apiService.createTransaction(formattedData);

            // Refresh wallets to update balances
            await fetchWallets();

            setIsTransactionModalOpen(false);
            setSelectedWallet(null);

            // Show success message
            alert('Transaction added successfully!');

        } catch (error) {
            console.error('Error creating transaction:', error);
            alert(`Failed to create transaction: ${error.message || 'Please try again.'}`);
        } finally {
            setIsCreatingTransaction(false);
        }
    };

    // Helper function to get category ID from category name
    const getCategoryId = async (categoryName) => {
        try {
            const categories = await apiService.getCategories();
            const category = categories.find(cat => cat.name === categoryName);
            return category ? category.id : 1; // Default to first category if not found
        } catch (error) {
            console.error('Error fetching categories:', error);
            return 1; // Default category ID
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
                    <MyWalletsContent
                        wallets={wallets}
                        onWalletCreated={handleWalletCreated}
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
        </div>
    );
};

export default MyWalletsPage;