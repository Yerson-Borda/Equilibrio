// /mnt/data/MyWalletsContent.jsx
import React, { useState, useEffect } from 'react';
import Button from '../ui/Button';
import CreateWalletModal from './CreateWalletModal';
import EditWalletModal from './EditWalletModal';
import AddTransactionModal from './AddTransactionModal';
import { apiService } from '../../services/api';
import visaIcon from '../../assets/icons/visa-icon.png';
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';

const MyWalletsContent = ({ wallets, onWalletCreated, onWalletUpdated, onWalletDeleted }) => {
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isTransactionModalOpen, setIsTransactionModalOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [selectedWallet, setSelectedWallet] = useState(wallets && wallets.length ? wallets[0] : null);
    const [walletToEdit, setWalletToEdit] = useState(null);
    const [walletToDelete, setWalletToDelete] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [upcomingPayments, setUpcomingPayments] = useState([]);

    useEffect(() => {
        fetchTransactions();
        fetchUpcomingPayments();
    }, []);

    useEffect(() => {
        // if wallets prop changes, ensure a selected wallet exists
        if ((!selectedWallet || !wallets.find(w => w.id === selectedWallet.id)) && wallets.length) {
            setSelectedWallet(wallets[0]);
        }
    }, [wallets]);

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

    const handleUpdateWallet = async (walletData, walletId) => {
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

            const updatedWallet = await apiService.updateWallet(walletId, formattedData);
            onWalletUpdated(updatedWallet);
            setIsEditModalOpen(false);
            setWalletToEdit(null);
        } catch (error) {
            console.error('Error updating wallet:', error);
            alert(`Failed to update wallet: ${error.message || 'Please try again.'}`);
        } finally {
            setIsLoading(false);
        }
    };

    const handleDeleteWallet = async (walletId) => {
        if (window.confirm('Are you sure you want to delete this wallet? This action cannot be undone.')) {
            try {
                setIsLoading(true);
                await apiService.deleteWallet(walletId);
                onWalletDeleted(walletId);
                alert('Wallet deleted successfully!');
                // if the deleted wallet was selected, clear or select first
                if (selectedWallet && selectedWallet.id === walletId) {
                    const remaining = wallets.filter(w => w.id !== walletId);
                    setSelectedWallet(remaining.length ? remaining[0] : null);
                }
            } catch (error) {
                console.error('Error deleting wallet:', error);
                alert('Failed to delete wallet');
            } finally {
                setIsLoading(false);
            }
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

    const handleOpenEditModal = (wallet) => {
        setWalletToEdit(wallet);
        setIsEditModalOpen(true);
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

    // Render a large card preview (based on provided snippet)
    const CardPreview = ({ wallet }) => {
        if (!wallet) {
            return (
                <div className="p-6 rounded-xl shadow-lg bg-gray-50 w-full" style={{ height: 200 }}>
                    <div className="text-sm text-gray-400">No wallet selected</div>
                </div>
            );
        }

        return (
            <div
                className="p-6 text-white rounded-xl shadow-2xl relative transition-colors duration-300 overflow-hidden"
                style={{
                    width: '100%',
                    height: 200,
                    background: `linear-gradient(135deg, ${wallet.color || '#6FBAFC'} 0%, rgba(0,0,0,0.15) 100%)`,
                    boxShadow: '0 10px 30px rgba(16,24,40,0.12)'
                }}
            >
                {/* Bank name */}
                <div className="text-left mb-2">
                    <h3 className="text-base font-semibold">{wallet.name || 'Т-Банк'}</h3>
                </div>

                {/* Chip + Balance + NFC */}
                <div className="flex justify-between items-start mb-6">
                    <div className="flex items-start space-x-3">
                        <img
                            src={chipIcon}
                            alt="Chip"
                            className="w-10 h-8 object-contain mt-4"
                        />
                        <div>
                            <p className="text-xs opacity-80 font-bold leading-none ml-6 mt-3">Total Balance</p>
                            <p className="text-xl font-bold leading-tight ml-6">
                                {getCurrencySymbol(wallet.currency)}{wallet.balance || '0.00'}
                            </p>
                        </div>
                    </div>
                    <img
                        src={nfcIcon}
                        alt="NFC"
                        className="w-8 h-7 object-contain mt-3 opacity-80"
                    />
                </div>

                {/* Card Number */}
                <div className="mb-0">
                    <span className="tracking-wider font-mono text-lg font-semibold">
                        {formatCardNumber(wallet.card_number)}
                    </span>
                </div>

                {/* Expiry and VISA */}
                <div className="flex justify-between items-center mb-0 absolute bottom-6 left-6 right-6">
                    <span className="text-sm opacity-80">09/30</span>
                    <img
                        src={visaIcon}
                        alt="VISA"
                        className="h-8 object-contain"
                    />
                </div>
            </div>
        );
    };

    return (
        <div className="max-w-7xl mx-auto px-4">
            {/* Header with Add Wallet Button */}
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
                {/* Left Column - Wallets & Card Preview */}
                <div className="lg:col-span-1 space-y-6">
                    <div>
                        <CardPreview wallet={selectedWallet} />

                        {/* "Your Balance" and action buttons below the preview */}
                        <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes mt-4">
                            <div className="flex justify-between items-start mb-4">
                                <div>
                                    <p className="text-sm text-metallic-gray mb-1">Your Balance</p>
                                    <p className="text-2xl font-bold text-text">
                                        {selectedWallet ? `${getCurrencySymbol(selectedWallet.currency)}${selectedWallet.balance || '0.00'}` : '$0.00'}
                                    </p>
                                    <p className="text-xs text-metallic-gray mt-2">{selectedWallet?.currency || 'USD'} / {selectedWallet?.wallet_type?.replace('_', ' ') || 'Debit card'}</p>
                                </div>
                                <div className="flex items-center">
                                    <img src={visaIcon} alt="card preview" className="h-10" />
                                </div>
                            </div>

                            <div className="flex space-x-4 mt-4">
                                <button
                                    onClick={() => handleOpenTransactionModal(selectedWallet)}
                                    className="flex-1 bg-white border-2 border-gray-200 text-gray-800 hover:bg-gray-50 py-3 rounded-lg font-semibold transition-colors duration-200"
                                >
                                    Add Transaction
                                </button>
                                <button
                                    onClick={() => handleOpenEditModal(selectedWallet)}
                                    className="flex-1 bg-white border-2 border-gray-200 text-gray-800 hover:bg-gray-50 py-3 rounded-lg font-semibold transition-colors duration-200"
                                >
                                    Edit
                                </button>
                                <button
                                    onClick={() => selectedWallet && handleDeleteWallet(selectedWallet.id)}
                                    className="flex-1 bg-[#D06978] text-white hover:bg-red-700 py-3 rounded-lg font-semibold transition-colors duration-200"
                                    disabled={isLoading}
                                >
                                    {isLoading ? 'Deleting...' : 'Delete'}
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* Thumbnails / Other wallets */}
                    <div className="space-y-4">
                        {wallets
                            .filter(w => !selectedWallet || w.id !== selectedWallet.id)
                            .map((wallet) => (
                                <div
                                    key={wallet.id}
                                    onClick={() => setSelectedWallet(wallet)}
                                    className="p-3 rounded-lg cursor-pointer transition-transform transform hover:scale-101"
                                >
                                    <div
                                        className="rounded-lg p-3"
                                        style={{
                                            background: wallet.color || '#ffffff',
                                            boxShadow: selectedWallet && selectedWallet.id === wallet.id ? '0 8px 24px rgba(0,0,0,0.15)' : 'none',
                                            opacity: 0.85,
                                            filter: 'blur(0.3px)'
                                        }}
                                    >
                                        <div className="flex justify-between items-center">
                                            <div>
                                                <h4 className="text-sm font-semibold text-text">{wallet.name}</h4>
                                                <p className="text-xs text-metallic-gray">{getCurrencySymbol(wallet.currency)}{wallet.balance || '0.00'}</p>
                                            </div>
                                            <span className="text-xs px-2 py-1 bg-white/60 rounded text-metallic-gray capitalize">{wallet.wallet_type?.replace('_', ' ') || 'debit card'}</span>
                                        </div>
                                    </div>
                                </div>
                            ))
                        }

                        {wallets.length <= 1 && (
                            <div className="text-sm text-metallic-gray">No other wallets</div>
                        )}
                    </div>
                </div>

                {/* Middle Column - Transactions (placeholder, ready to be filled) */}
                <div className="lg:col-span-1">
                    <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes h-full">
                        <h3 className="text-lg font-semibold text-text mb-6">Transactions</h3>
                        <div className="flex space-x-4 mb-6">
                            <button className="text-blue font-medium border-b-2 border-blue pb-1">All Transactions</button>
                            <button className="text-metallic-gray pb-1">Regular Transactions</button>
                        </div>

                        {/* Placeholder area: keeps structure ready for transactions */}
                        <div className="h-48 flex items-center justify-center text-metallic-gray">
                            <p>No transactions yet</p>
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

            {/* Edit Wallet Modal */}
            <EditWalletModal
                isOpen={isEditModalOpen}
                onClose={() => {
                    setIsEditModalOpen(false);
                    setWalletToEdit(null);
                }}
                onSubmit={handleUpdateWallet}
                isLoading={isLoading}
                wallet={walletToEdit}
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
