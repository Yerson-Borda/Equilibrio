import React, { useState, useEffect } from 'react';
import Button from '../ui/Button';
import CreateWalletModal from '../modals/CreateWalletModal.jsx';
import AddTransactionModal from '../modals/AddTransactionModal.jsx';
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

    // Listen for external transaction updates (e.g. from MyWalletsPage modal)
    useEffect(() => {
        const handleExternalUpdate = () => {
            fetchTransactions();
        };
        window.addEventListener('transaction_updated', handleExternalUpdate);
        return () => window.removeEventListener('transaction_updated', handleExternalUpdate);
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
            { name: 'Facebook Ads', amount: 400.0, date: 'Next month' },
            { name: 'LinkedIn Ads', amount: 200.5, date: 'Next month' }
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

            if (transactionData.type === 'transfer') {
                // Transfer between wallets
                const transferPayload = {
                    source_wallet_id: parseInt(transactionData.source_wallet_id),
                    destination_wallet_id: parseInt(transactionData.destination_wallet_id),
                    amount: parseFloat(transactionData.amount),
                    note: transactionData.note || ''
                };

                console.log('Creating transfer:', transferPayload);
                await apiService.createTransfer(transferPayload);
            } else {
                // Regular income/expense transaction
                const formattedData = {
                    amount: parseFloat(transactionData.amount),
                    description: transactionData.note || '',
                    type: transactionData.type,
                    transaction_date: new Date().toISOString().split('T')[0],
                    wallet_id: parseInt(transactionData.wallet_id),
                    category_id: await getCategoryId(transactionData.category)
                };

                console.log('Creating transaction:', formattedData);
                await apiService.createTransaction(formattedData);
            }

            // Refresh transactions
            await fetchTransactions();

            setIsTransactionModalOpen(false);
            setSelectedWallet(null);

            // Show success message
            alert(
                transactionData.type === 'transfer'
                    ? 'Transfer completed successfully!'
                    : 'Transaction added successfully!'
            );
        } catch (error) {
            console.error('Error creating transaction/transfer:', error);
            alert(
                `Failed to save transaction: ${
                    error.message || 'Please try again.'
                }`
            );
        } finally {
            setIsLoading(false);
        }
    };

    // Helper function to get category ID from category name
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

    const handleCloseTransactionModal = () => {
        setIsTransactionModalOpen(false);
        setSelectedWallet(null);
    };

    return (
        <div className="max-w-7xl mx-auto pt-8">
            {/* Header */}
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-text">My Wallets</h1>
                    <p className="text-metallic-gray mt-1">
                        Manage all your financial accounts and track their performance.
                    </p>
                </div>
                <div className="flex gap-3">
                    <Button
                        variant="secondary"
                        onClick={() => setIsCreateModalOpen(true)}
                    >
                        Create New Wallet
                    </Button>
                </div>
            </div>

            {/* Wallets + Right side */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Wallets list */}
                <div className="lg:col-span-2 space-y-4">
                    {wallets.length === 0 && (
                        <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes text-center">
                            <p className="text-metallic-gray">
                                You don’t have any wallets yet.
                            </p>
                        </div>
                    )}

                    {wallets.map(wallet => (
                        <div
                            key={wallet.id}
                            className="bg-white rounded-xl shadow-sm p-6 border border-strokes flex items-center justify-between"
                        >
                            <div>
                                <h3 className="text-lg font-semibold text-text">
                                    {wallet.name}
                                </h3>
                                <p className="text-metallic-gray text-sm">
                                    {wallet.wallet_type} · {wallet.currency}
                                </p>
                                <p className="mt-2 font-bold text-text">
                                    {wallet.balance} {wallet.currency}
                                </p>
                            </div>
                            <div className="flex flex-col gap-2">
                                <Button
                                    variant="primary"
                                    onClick={() => handleOpenTransactionModal(wallet)}
                                >
                                    Add Transaction
                                </Button>
                            </div>
                        </div>
                    ))}
                </div>

                {/* Right column: Transactions + Upcoming payments */}
                <div className="space-y-6">
                    {/* Transactions */}
                    <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
                        <h3 className="text-lg font-semibold text-text mb-6">
                            Transactions
                        </h3>

                        <div className="flex space-x-4 mb-6">
                            <button className="text-blue font-medium border-b-2 border-blue pb-1">
                                All Transactions
                            </button>
                            <button className="text-metallic-gray pb-1">
                                Regular Transactions
                            </button>
                        </div>

                        <div className="space-y-4">
                            <h4 className="font-medium text-text">Today</h4>
                            {transactions.slice(0, 4).map((transaction, index) => (
                                <div
                                    key={index}
                                    className="flex justify-between items-center p-3 bg-gray-50 rounded-lg"
                                >
                                    <div>
                                        <p className="font-medium text-text">
                                            {transaction.description || 'Transaction'}
                                        </p>
                                        <p className="text-sm text-metallic-gray capitalize">
                                            {transaction.type}
                                        </p>
                                    </div>
                                    <p
                                        className={`font-semibold ${
                                            transaction.type === 'income'
                                                ? 'text-green-600'
                                                : transaction.type === 'expense'
                                                    ? 'text-red-600'
                                                    : 'text-blue-600'
                                        }`}
                                    >
                                        {transaction.type === 'income'
                                            ? '+'
                                            : transaction.type === 'expense'
                                                ? '-'
                                                : '↔'}
                                        ${transaction.amount}
                                    </p>
                                </div>
                            ))}

                            {transactions.length === 0 && (
                                <div className="text-center py-4">
                                    <p className="text-metallic-gray">
                                        No transactions yet
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Upcoming Payments */}
                    <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
                        <h3 className="text-lg font-semibold text-text mb-6">
                            Upcoming Payments
                        </h3>

                        <div className="space-y-4">
                            <h4 className="font-medium text-text">Next month</h4>
                            {upcomingPayments.map((payment, index) => (
                                <div
                                    key={index}
                                    className="flex justify-between items-center p-3 bg-gray-50 rounded-lg"
                                >
                                    <div>
                                        <p className="font-medium text-text">
                                            {payment.name}
                                        </p>
                                        <p className="text-sm text-metallic-gray">
                                            {payment.date}
                                        </p>
                                    </div>
                                    <p className="font-semibold text-red-600">
                                        -${payment.amount}
                                    </p>
                                </div>
                            ))}

                            {upcomingPayments.length === 0 && (
                                <div className="text-center py-4">
                                    <p className="text-metallic-gray">
                                        No upcoming payments
                                    </p>
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
                onClose={handleCloseTransactionModal}
                onSubmit={handleAddTransaction}
                isLoading={isLoading}
                wallets={wallets}
                selectedWallet={selectedWallet}
            />
        </div>
    );
};

export default MyWalletsContent;
