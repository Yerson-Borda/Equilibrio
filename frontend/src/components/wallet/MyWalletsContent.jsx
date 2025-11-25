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
    const [selectedWallet, setSelectedWallet] = useState(null);
    const [walletToEdit, setWalletToEdit] = useState(null);
    const [walletToDelete, setWalletToDelete] = useState(null);
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

    const [showSearch, setShowSearch] = useState(false);


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



    const currentCurrencySymbol = getCurrencySymbol();

    return (
        <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-8">

            {/* LEFT COLUMN */}
            <div className="space-y-6" style={{ width: "450px" }}>

                {/* Wallet Cards (Stacked UI) */}
                <div
                    className="relative mb-20"
                    style={{
                        width: "380px",
                        height: Math.max(260, 200 + (wallets.length - 1) * 70)
                    }}
                >


                    {wallets.length === 0 && (
                        <div className="p-6 bg-white rounded-xl shadow-sm border text-center">
                            <p className="font-medium text-text">No wallets yet</p>
                        </div>
                    )}

                    {wallets.map((wallet, i) => {
                        const isSelected = selectedWallet?.id === wallet.id;

                        return (
                            <div
                                key={wallet.id}
                                onClick={() => setSelectedWallet(wallet)}
                                className="absolute left-0 w-[450px] h-[250px] rounded-xl cursor-pointer transition-all duration-300"
                                style={{
                                    top: i * 70,
                                    zIndex: isSelected ? 40 : 20 - i,

                                    // SELECTED WALLET = BLACK
                                    backgroundColor: isSelected ? "#6FBAFC" : "transparent",

                                    // UNSELECTED = blur + slight border
                                    backdropFilter: isSelected ? "none" : "blur(8px)",
                                    border: isSelected ? "none" : "1px solid hsla(0, 0%, 100%, 0.30)",

                                    // Scaling
                                    transform: isSelected ? "scale(1)" : "scale(0.97)",

                                    // Shadows
                                    boxShadow: isSelected
                                        ? "0 12px 30px rgba(0,0,0,0.35)"
                                        : "0 4px 10px rgba(0,0,0,0.08)",
                                }}
                            >
                                <div className="p-6 flex flex-col justify-between h-full">

                                    {/* Wallet Name */}
                                    <h3 className={`text-base font-semibold ${isSelected ? "text-white" : "text-text"}`}>
                                        {wallet.name}
                                    </h3>

                                    {/* Chip + Balance + NFC */}
                                    <div className="flex justify-between items-start mb-6">
                                        <div className="flex items-start space-x-3">
                                            <img
                                                src={chipIcon}
                                                alt="Chip"
                                                className="w-10 h-8 object-contain mt-4"
                                            />
                                            <div>
                                                <p className="text-xs opacity-75 font-bold leading-none ml-6 mt-3">Total Balance</p>
                                                <p className="text-xl font-bold leading-tight ml-6">
                                                    {currentCurrencySymbol}{'0.00'}
                                                </p>
                                            </div>
                                        </div>
                                        <img
                                            src={nfcIcon}
                                            alt="NFC"
                                            className="w-8 h-7 object-contain mt-3"
                                        />
                                    </div>

                                    {/* Card Number */}
                                    <p className={`mt-4 text-lg font-mono tracking-wider ${isSelected ? "text-white" : "text-text"}`}>
                                        {formatCardNumber(wallet.card_number)}
                                    </p>

                                    {/* Footer */}
                                    <div className="flex justify-between items-center mt-4">
                                        <span className={`${isSelected ? "text-white/70" : "text-metallic-gray"} text-sm`}>
                                            {wallet.expiry || "09/30"}
                                        </span>

                                        <img
                                            src={visaIcon}
                                            alt="VISA"
                                            className="h-8 opacity-90"
                                        />
                                    </div>

                                </div>
                            </div>
                        );
                    })}
                </div>


                {/* SELECTED WALLET DETAILS */}
                {selectedWallet && (
                    <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes space-y-4">

                        <div>
                            <p className="text-sm text-metallic-gray">Balance</p>
                            <p className="text-2xl font-bold text-text">
                                {getCurrencySymbol(selectedWallet.currency)}
                                {selectedWallet.balance || "0.00"}
                            </p>
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <p className="text-sm text-metallic-gray">Currency</p>
                                <p className="font-medium">{selectedWallet.currency}</p>
                            </div>

                            <div>
                                <p className="text-sm text-metallic-gray">Type</p>
                                <p className="font-medium capitalize">
                                    {selectedWallet.wallet_type?.replace("_", " ") || "debit"}
                                </p>
                            </div>
                        </div>

                        {/* BUTTONS */}
                        <div className="space-y-3 pt-2">

                            {/* Row 1 – Add Wallet (Full width) */}
                            <button
                                onClick={() => setIsCreateModalOpen(true)}
                                style={{ color: "green" }}
                                className="w-full bg-white border-2 border-green-500 !text-green-500 hover:bg-green-50 py-3 rounded-lg font-semibold"
                            >
                                + Add Wallet
                            </button>


                            {/* Row 2 – Add Transaction (70%) + Delete (30%) */}
                            <div className="flex space-x-3">
                                <button
                                    onClick={() => handleOpenTransactionModal(selectedWallet)}
                                    style={{ color: "green" }}
                                    className="flex-[0.7] bg-white border-2 border-blue-500 !text-green-500 hover:bg-blue-50 py-3 rounded-lg font-semibold"
                                >
                                    Add Transaction
                                </button>

                                <button
                                    onClick={() => handleDeleteWallet(selectedWallet.id)}
                                    disabled={isLoading}
                                    className="flex-[0.3] bg-red-500 text-white hover:bg-red-600 py-3 rounded-lg font-semibold"
                                >
                                    {isLoading ? "..." : "Delete"}
                                </button>
                            </div>

                        </div>

                    </div>
                )}
            </div>

            {/* RIGHT COLUMN */}
            <div className="space-y-8" style={{ width: "800px" }}>

                {/* TRANSACTIONS */}
                <div className="bg-[#F5F7FA] rounded-xl p-6">

                    <div className="flex justify-between items-center mb-6">
                        <h3 className="text-lg font-semibold text-text">Transactions</h3>

                        {/* Search icon */}
                        <button onClick={() => setShowSearch(!showSearch)}>
                            🔍
                        </button>
                    </div>

                    {/* Filters */}
                    <div className="flex space-x-4 mb-6 items-center">
                        <button className="text-blue font-medium border-b-2 border-blue pb-1">
                            All Transactions
                        </button>
                        <button className="text-metallic-gray pb-1">
                            Regular Transactions
                        </button>
                    </div>

                    {/* Search Bar */}
                    {showSearch && (
                        <input
                            type="text"
                            placeholder="Search transactions..."
                            className="w-full mb-4 p-2 border rounded"
                        />
                    )}

                    {/* Transaction List */}
                    <div className="space-y-3">
                        {transactions.length > 0 ? (
                            transactions.map((t, index) => (
                                <div
                                    key={index}
                                    className="flex justify-between items-center p-3 bg-gray-50 rounded-lg"
                                >
                                    <div className="flex items-center space-x-3">
                                        {/* Icon depending on transaction type */}
                                        <div className="text-2xl">
                                            {getTransactionIcon(t.source)}
                                        </div>

                                        <div>
                                            <p className="font-bold text-text">{t.source}</p>
                                            <p className="text-sm text-metallic-gray">{t.date}</p>
                                        </div>
                                    </div>

                                    {/* Amount */}
                                    <p className="font-semibold">
                                        {getCurrencySymbol(t.currency)}
                                        {t.amount}
                                    </p>
                                </div>
                            ))
                        ) : (
                            <p className="text-center text-metallic-gray py-4">
                                No transactions yet
                            </p>
                        )}
                    </div>
                </div>

                {/* UPCOMING PAYMENTS */}
                <div className="bg-[#F5F7FA] p-6 shadow-none border-none">

                    <h3 className="text-lg font-semibold text-text mb-6">Upcoming Payments</h3>

                    {upcomingPayments.length > 0 ? (
                        upcomingPayments.map((p, index) => (
                            <div
                                key={index}
                                className="flex justify-between items-center p-3 bg-gray-50 rounded-lg"
                            >
                                <div className="flex items-center space-x-3">
                                    <div className="text-2xl">📅</div>

                                    <div>
                                        <p className="font-bold text-text">{p.name}</p>
                                        <p className="text-sm text-metallic-gray">{p.date}</p>
                                    </div>
                                </div>

                                <p className="font-semibold text-red-600">
                                    -{getCurrencySymbol(p.currency)}
                                    {p.amount}
                                </p>
                            </div>
                        ))
                    ) : (
                        <p className="text-center text-metallic-gray py-4">
                            No upcoming payments
                        </p>
                    )}
                </div>
            </div>

            {/* Modals */}
            <CreateWalletModal isOpen={isCreateModalOpen} onClose={() => setIsCreateModalOpen(false)} onSubmit={handleCreateWallet} isLoading={isLoading} />
            <AddTransactionModal isOpen={isTransactionModalOpen} onClose={() => setIsTransactionModalOpen(false)} onSubmit={handleAddTransaction} isLoading={isLoading} wallets={wallets} selectedWallet={selectedWallet} />
        </div>
    );

};

export default MyWalletsContent;