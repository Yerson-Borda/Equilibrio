import React, { useState, useEffect } from 'react';
import Button from '../ui/Button';
import CreateWalletModal from './CreateWalletModal';
import AddTransactionModal from './AddTransactionModal';
import { apiService } from '../../services/api';

const MyWalletsContent = ({ wallets, onWalletCreated }) => {
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isTransactionModalOpen, setIsTransactionModalOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [selectedWallet, setSelectedWallet] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [upcomingPayments, setUpcomingPayments] = useState([]);

    useEffect(() => {
        fetchTransactions();
        fetchUpcomingPayments();
    }, []);

    const fetchTransactions = async () => {
        try {
            const transactionsData = await apiService.getTransactions();
            setTransactions(transactionsData || []);
        } catch (error) {
            console.error('Error fetching transactions:', error);
        }
    };

    const fetchUpcomingPayments = async () => {
        // Mock data for upcoming payments
        const mockUpcomingPayments = [
            { name: 'Facebook Ads', amount: 400.00, date: 'Next month' },
            { name: 'LinkedIn Ads', amount: 200.50, date: 'Next month' }
        ];
        setUpcomingPayments(mockUpcomingPayments);
    };

    const handleCreateWallet = async (walletData) => {
        try {
            setIsLoading(true);
            const formattedData = {
                name: walletData.name,
                currency: walletData.currency,
                wallet_type: walletData.wallet_type,
                initial_balance: parseFloat(walletData.initial_balance) || 0,
                card_number: walletData.card_number || '',
                color: walletData.color || '#6FBAFC'
            };

            const newWallet = await apiService.createWallet(formattedData);
            onWalletCreated(newWallet);
            setIsCreateModalOpen(false);
        } catch (error) {
            console.error('Error creating wallet:', error);
            alert(`Failed to create wallet: ${error.message || 'Please try again.'}`);
        } finally {
            setIsLoading(false);
        }
    };

    const handleAddTransaction = async (transactionData) => {
        try {
            setIsLoading(true);

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

            // Create the transaction via API
            await apiService.createTransaction(formattedData);

            // Refresh transactions
            await fetchTransactions();

            setIsTransactionModalOpen(false);
            setSelectedWallet(null);

            // Show success message
            alert('Transaction added successfully!');

        } catch (error) {
            console.error('Error creating transaction:', error);
            alert(`Failed to create transaction: ${error.message || 'Please try again.'}`);
        } finally {
            setIsLoading(false);
        }
    };

    const handleDeleteWallet = async (walletId) => {
        if (window.confirm('Are you sure you want to delete this wallet? This action cannot be undone.')) {
            try {
                // You'll need to implement deleteWallet in your apiService
                // await apiService.deleteWallet(walletId);
                console.log('Delete wallet:', walletId);
                alert('Wallet deletion would be implemented here');
            } catch (error) {
                console.error('Error deleting wallet:', error);
                alert('Failed to delete wallet');
            }
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

    const formatCardNumber = (number) => {
        if (!number) return '•••• •••• •••• ••••';
        const lastFour = number.slice(-4);
        return `•••• •••• •••• ${lastFour}`;
    };

    // Currency symbols mapping
    const currencySymbols = {
        'USD': '$', 'EUR': '€', 'GBP': '£', 'JPY': '¥', 'CAD': 'CA$', 'AUD': 'A$'
    };

    const getCurrencySymbol = (currencyCode) => {
        return currencySymbols[currencyCode] || '$';
    };

    return (
        <div className="max-w-7xl mx-auto">
            {/* Header with Add Wallet Button - White box with green text */}
            <div className="flex justify-between items-center mb-8">
                <h1 className="text-2xl font-bold text-text">My Wallets</h1>
                <button
                    onClick={() => setIsCreateModalOpen(true)}
                    className="bg-white border-2 border-green-500 text-green-500 hover:bg-green-50 px-6 py-3 rounded-lg font-semibold transition-colors duration-200"
                >
                    + Add Wallet
                </button>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Left Column - Wallets */}
                <div className="lg:col-span-1 space-y-6">
                    {wallets.map((wallet) => (
                        <div key={wallet.id} className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
                            {/* Wallet Header */}
                            <div className="flex justify-between items-start mb-4">
                                <div>
                                    <h3 className="text-lg font-semibold text-text mb-1">{wallet.name}</h3>
                                    <p className="text-sm text-metallic-gray">Total Balance</p>
                                    <p className="text-2xl font-bold text-text">
                                        {getCurrencySymbol(wallet.currency)}{wallet.balance || '0.00'}
                                    </p>
                                </div>
                                <span className="text-xs px-2 py-1 bg-gray-100 rounded text-metallic-gray capitalize">
                                    {wallet.wallet_type?.replace('_', ' ') || 'debit card'}
                                </span>
                            </div>

                            {/* Card Number */}
                            <div className="mb-4">
                                <p className="text-sm text-metallic-gray mb-1">Card Number</p>
                                <p className="text-text font-mono">{formatCardNumber(wallet.card_number)}</p>
                            </div>

                            {/* Wallet Details */}
                            <div className="grid grid-cols-2 gap-4 mb-6">
                                <div>
                                    <p className="text-sm text-metallic-gray mb-1">Currency</p>
                                    <p className="text-text font-medium">{wallet.currency}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-metallic-gray mb-1">Status</p>
                                    <div className="flex items-center">
                                        <span className="w-2 h-2 bg-green-500 rounded-full mr-2"></span>
                                        <p className="text-text font-medium">Active</p>
                                    </div>
                                </div>
                            </div>

                            {/* Action Buttons - Add Transaction (green text, white box) and Delete (red) */}
                            <div className="flex space-x-3">
                                <button
                                    onClick={() => handleOpenTransactionModal(wallet)}
                                    className="flex-1 bg-white border-2 border-green-500 text-green-500 hover:bg-green-50 py-2 rounded-lg font-semibold transition-colors duration-200"
                                >
                                    Add Transaction
                                </button>
                                <button
                                    onClick={() => handleDeleteWallet(wallet.id)}
                                    className="flex-1 bg-[#D06978] text-white hover:bg-red-700 py-2 rounded-lg font-semibold transition-colors duration-200"
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}

                    {/* Empty State for Wallets */}
                    {wallets.length === 0 && (
                        <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes text-center">
                            <h3 className="text-lg font-semibold text-text mb-2">No Wallets Yet</h3>
                            <p className="text-metallic-gray mb-4">Create your first wallet to start managing your finances</p>
                            <button
                                onClick={() => setIsCreateModalOpen(true)}
                                className="bg-white border-2 border-green-500 text-green-500 hover:bg-green-50 px-6 py-3 rounded-lg font-semibold transition-colors duration-200"
                            >
                                + Add Wallet
                            </button>
                        </div>
                    )}
                </div>

                {/* Middle Column - Transactions */}
                <div className="lg:col-span-1">
                    <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
                        <h3 className="text-lg font-semibold text-text mb-6">Transactions</h3>

                        <div className="flex space-x-4 mb-6">
                            <button className="text-blue font-medium border-b-2 border-blue pb-1">All Transactions</button>
                            <button className="text-metallic-gray pb-1">Regular Transactions</button>
                        </div>

                        <div className="space-y-4">
                            <h4 className="font-medium text-text">Today</h4>
                            {transactions.slice(0, 4).map((transaction, index) => (
                                <div key={index} className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                                    <div>
                                        <p className="font-medium text-text">{transaction.description || 'Transaction'}</p>
                                        <p className="text-sm text-metallic-gray">{transaction.type}</p>
                                    </div>
                                    <p className={`font-semibold ${
                                        transaction.type === 'income' ? 'text-green-600' : 'text-red-600'
                                    }`}>
                                        {transaction.type === 'income' ? '+' : '-'}${transaction.amount}
                                    </p>
                                </div>
                            ))}

                            {transactions.length === 0 && (
                                <div className="text-center py-4">
                                    <p className="text-metallic-gray">No transactions yet</p>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Right Column - Upcoming Payments */}
                <div className="lg:col-span-1">
                    <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
                        <h3 className="text-lg font-semibold text-text mb-6">Upcoming Payments</h3>

                        <div className="space-y-4">
                            <h4 className="font-medium text-text">Next month</h4>
                            {upcomingPayments.map((payment, index) => (
                                <div key={index} className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                                    <div>
                                        <p className="font-medium text-text">{payment.name}</p>
                                        <p className="text-sm text-metallic-gray">{payment.date}</p>
                                    </div>
                                    <p className="font-semibold text-red-600">-${payment.amount}</p>
                                </div>
                            ))}

                            {upcomingPayments.length === 0 && (
                                <div className="text-center py-4">
                                    <p className="text-metallic-gray">No upcoming payments</p>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Create Wallet Modal */}
            <CreateWalletModal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                onSubmit={handleCreateWallet}
                isLoading={isLoading}
            />

            {/* Add Transaction Modal */}
            <AddTransactionModal
                isOpen={isTransactionModalOpen}
                onClose={() => setIsTransactionModalOpen(false)}
                onSubmit={handleAddTransaction}
                isLoading={isLoading}
                wallets={wallets}
                selectedWallet={selectedWallet}
            />
        </div>
    );
};

export default MyWalletsContent;