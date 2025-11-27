import React, { useState, useEffect } from 'react';
import Card from '../ui/Card';
import Button from '../ui/Button';
import CreateWalletModal from './../modals/CreateWalletModal';
import { apiService } from '../../services/api';

import visaIcon from '../../assets/icons/visa-icon.png';
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';

import { getCurrencySymbol } from '../../config/currencies';

const MyWalletsContent = ({
                              wallets = [],
                              onWalletCreated,
                              onAddTransaction,
                              onWalletDeleted,
                          }) => {
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isCreatingWallet, setIsCreatingWallet] = useState(false);
    const [selectedWallet, setSelectedWallet] = useState(null);

    const [transactions, setTransactions] = useState([]);
    const [upcomingPayments, setUpcomingPayments] = useState([]);
    const [showSearch, setShowSearch] = useState(false);

    // Load transactions + upcoming payments once
    useEffect(() => {
        fetchTransactions();
        fetchUpcomingPayments();
    }, []);

    const fetchTransactions = async () => {
        try {
            const data = await apiService.getTransactions();
            setTransactions(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('Error fetching transactions:', error);
        }
    };

    const fetchUpcomingPayments = async () => {
        // Still mock for now – you can plug backend later
        const mockUpcomingPayments = [
            { name: 'Facebook Ads', amount: 400.0, date: 'Next month' },
            { name: 'LinkedIn Ads', amount: 200.5, date: 'Next month' },
        ];
        setUpcomingPayments(mockUpcomingPayments);
    };

    const handleOpenCreateWallet = () => {
        setIsCreateModalOpen(true);
    };

    const handleCloseCreateWallet = () => {
        setIsCreateModalOpen(false);
    };

    const handleCreateWallet = async (walletData) => {
        try {
            setIsCreatingWallet(true);

            const formattedData = {
                name: walletData.name,
                currency: walletData.currency,
                wallet_type: walletData.wallet_type,
                initial_balance: parseFloat(walletData.initial_balance) || 0,
                card_number: walletData.card_number || '',
                color: walletData.color || '#6FBAFC',
            };

            const newWallet = await apiService.createWallet(formattedData);
            if (onWalletCreated) onWalletCreated(newWallet);

            setIsCreateModalOpen(false);
            setSelectedWallet(newWallet);
            alert('Wallet created successfully!');
        } catch (error) {
            console.error('Error creating wallet:', error);
            alert(
                `Failed to create wallet: ${
                    error.message || 'Please try again.'
                }`
            );
        } finally {
            setIsCreatingWallet(false);
        }
    };

    const handleDeleteWallet = async (walletId) => {
        if (
            !window.confirm(
                'Are you sure you want to delete this wallet? This action cannot be undone.'
            )
        ) {
            return;
        }

        try {
            setIsCreatingWallet(true);
            await apiService.deleteWallet(walletId);
            if (onWalletDeleted) onWalletDeleted(walletId);
            if (selectedWallet?.id === walletId) setSelectedWallet(null);
            alert('Wallet deleted successfully!');
        } catch (error) {
            console.error('Error deleting wallet:', error);
            alert('Failed to delete wallet');
        } finally {
            setIsCreatingWallet(false);
        }
    };

    const handleAddTransactionClick = (wallet) => {
        if (onAddTransaction) {
            onAddTransaction(wallet);
        }
    };

    const formatCardNumber = (number) => {
        if (!number) return '•••• •••• •••• ••••';
        const digits = String(number).replace(/\D/g, '');
        const lastFour = digits.slice(-4) || '••••';
        return `•••• •••• •••• ${lastFour}`;
    };

    const getTransactionIcon = (type) => {
        switch (type) {
            case 'income':
                return '💰';
            case 'expense':
                return '💸';
            case 'transfer':
                return '🔄';
            default:
                return '🧾';
        }
    };

    const getTxDate = (tx) => {
        const raw =
            tx.transaction_date ||
            tx.date ||
            tx.created_at ||
            tx.timestamp ||
            '';
        if (!raw) return '';
        return raw.split('T')[0];
    };

    const getDisplayCurrencySymbol = (currencyCode) =>
        getCurrencySymbol(currencyCode || selectedWallet?.currency || 'USD');

    return (
        <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* LEFT COLUMN */}
            <div className="space-y-6" style={{ width: '450px' }}>
                {/* Stacked wallet cards */}
                <div
                    className="relative mb-20"
                    style={{
                        width: '380px',
                        height: Math.max(260, 200 + (wallets.length - 1) * 70),
                    }}
                >
                    {wallets.length === 0 && (
                        <Card className="p-6 text-center">
                            <p className="font-medium text-text">
                                No wallets yet
                            </p>
                            <p className="text-sm text-metallic-gray mb-3">
                                Create a wallet to get started.
                            </p>
                            <Button
                                variant="primary"
                                onClick={handleOpenCreateWallet}
                            >
                                + Add Wallet
                            </Button>
                        </Card>
                    )}

                    {wallets.map((wallet, index) => {
                        const isSelected = selectedWallet?.id === wallet.id;
                        const symbol = getCurrencySymbol(wallet.currency);

                        return (
                            <div
                                key={wallet.id}
                                onClick={() => setSelectedWallet(wallet)}
                                className="absolute left-0 w-[450px] h-[250px] rounded-xl cursor-pointer transition-all duration-300"
                                style={{
                                    top: index * 70,
                                    zIndex: isSelected ? 40 : 20 - index,
                                    backgroundColor: isSelected
                                        ? wallet.color || '#6FBAFC'
                                        : 'transparent',
                                    backdropFilter: isSelected
                                        ? 'none'
                                        : 'blur(8px)',
                                    border: isSelected
                                        ? 'none'
                                        : '1px solid hsla(0, 0%, 100%, 0.30)',
                                    transform: isSelected
                                        ? 'scale(1)'
                                        : 'scale(0.97)',
                                    boxShadow: isSelected
                                        ? '0 12px 30px rgba(0,0,0,0.35)'
                                        : '0 4px 10px rgba(0,0,0,0.08)',
                                }}
                            >
                                <div className="p-6 flex flex-col justify-between h-full">
                                    {/* Wallet Name */}
                                    <h3
                                        className={`text-base font-semibold ${
                                            isSelected
                                                ? 'text-white'
                                                : 'text-text'
                                        }`}
                                    >
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
                                                <p className="text-xs opacity-75 font-bold leading-none ml-6 mt-3">
                                                    Total Balance
                                                </p>
                                                <p className="text-xl font-bold leading-tight ml-6">
                                                    {symbol}
                                                    {Number(
                                                        wallet.balance || 0
                                                    ).toFixed(2)}
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
                                    <p
                                        className={`mt-4 text-lg font-mono tracking-wider ${
                                            isSelected
                                                ? 'text-white'
                                                : 'text-text'
                                        }`}
                                    >
                                        {formatCardNumber(wallet.card_number)}
                                    </p>

                                    {/* Footer */}
                                    <div className="flex justify-between items-center mt-4">
                                        <span
                                            className={`${
                                                isSelected
                                                    ? 'text-white/70'
                                                    : 'text-metallic-gray'
                                            } text-sm`}
                                        >
                                            {wallet.expiry || '09/30'}
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

                {/* Selected wallet details */}
                {selectedWallet && (
                    <Card>
                        <div className="space-y-4">
                            <div>
                                <p className="text-sm text-metallic-gray">
                                    Balance
                                </p>
                                <p className="text-2xl font-bold text-text">
                                    {getCurrencySymbol(selectedWallet.currency)}
                                    {Number(
                                        selectedWallet.balance || 0
                                    ).toFixed(2)}
                                </p>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <p className="text-sm text-metallic-gray">
                                        Currency
                                    </p>
                                    <p className="font-medium">
                                        {selectedWallet.currency}
                                    </p>
                                </div>

                                <div>
                                    <p className="text-sm text-metallic-gray">
                                        Type
                                    </p>
                                    <p className="font-medium capitalize">
                                        {selectedWallet.wallet_type
                                            ?.replace('_', ' ') || 'debit'}
                                    </p>
                                </div>
                            </div>

                            {/* Buttons */}
                            <div className="space-y-3 pt-2">
                                {/* Row 1 – Add wallet */}
                                <button
                                    onClick={handleOpenCreateWallet}
                                    className="w-full bg-white border-2 border-green-500 text-green-500 hover:bg-green-50 py-3 rounded-lg font-semibold"
                                >
                                    + Add Wallet
                                </button>

                                {/* Row 2 – Add transaction + Delete */}
                                <div className="flex space-x-3">
                                    <button
                                        onClick={() =>
                                            handleAddTransactionClick(
                                                selectedWallet
                                            )
                                        }
                                        className="flex-[0.7] bg-white border-2 border-blue-500 text-blue-500 hover:bg-blue-50 py-3 rounded-lg font-semibold"
                                    >
                                        Add Transaction
                                    </button>

                                    <button
                                        onClick={() =>
                                            handleDeleteWallet(
                                                selectedWallet.id
                                            )
                                        }
                                        disabled={isCreatingWallet}
                                        className="flex-[0.3] bg-red-500 text-white hover:bg-red-600 py-3 rounded-lg font-semibold"
                                    >
                                        {isCreatingWallet ? '...' : 'Delete'}
                                    </button>
                                </div>
                            </div>
                        </div>
                    </Card>
                )}
            </div>

            {/* RIGHT COLUMN */}
            <div className="space-y-8" style={{ width: '800px' }}>
                {/* Transactions */}
                <Card title="Transactions">
                    <div className="flex justify-between items-center mb-4">
                        <div className="flex space-x-4 items-center">
                            <button className="text-blue font-medium border-b-2 border-blue pb-1 text-sm">
                                All Transactions
                            </button>
                            <button className="text-metallic-gray pb-1 text-sm">
                                Regular Transactions
                            </button>
                        </div>

                        <button onClick={() => setShowSearch(!showSearch)}>
                            🔍
                        </button>
                    </div>

                    {showSearch && (
                        <input
                            type="text"
                            placeholder="Search transactions..."
                            className="w-full mb-4 p-2 border rounded"
                        />
                    )}

                    <div className="space-y-3 max-h-80 overflow-y-auto">
                        {transactions.length > 0 ? (
                            transactions.map((t, index) => {
                                const symbol = getDisplayCurrencySymbol(
                                    t.currency
                                );
                                return (
                                    <div
                                        key={index}
                                        className="flex justify-between items-center p-3 bg-gray-50 rounded-lg"
                                    >
                                        <div className="flex items-center space-x-3">
                                            <div className="text-2xl">
                                                {getTransactionIcon(t.type)}
                                            </div>
                                            <div>
                                                <p className="font-bold text-text">
                                                    {t.description ||
                                                        t.category ||
                                                        'Transaction'}
                                                </p>
                                                <p className="text-sm text-metallic-gray">
                                                    {getTxDate(t)}
                                                </p>
                                            </div>
                                        </div>

                                        <p className="font-semibold">
                                            {symbol}
                                            {Number(t.amount || 0).toFixed(2)}
                                        </p>
                                    </div>
                                );
                            })
                        ) : (
                            <p className="text-center text-metallic-gray py-4">
                                No transactions yet
                            </p>
                        )}
                    </div>
                </Card>

                {/* Upcoming payments */}
                <Card title="Upcoming Payments" className="bg-[#F5F7FA] border-none">
                    {upcomingPayments.length > 0 ? (
                        upcomingPayments.map((p, index) => (
                            <div
                                key={index}
                                className="flex justify-between items-center p-3 bg-gray-50 rounded-lg mb-3"
                            >
                                <div className="flex items-center space-x-3">
                                    <div className="text-2xl">📅</div>
                                    <div>
                                        <p className="font-bold text-text">
                                            {p.name}
                                        </p>
                                        <p className="text-sm text-metallic-gray">
                                            {p.date}
                                        </p>
                                    </div>
                                </div>

                                <p className="font-semibold text-red-600">
                                    -
                                    {getDisplayCurrencySymbol(
                                        p.currency || selectedWallet?.currency
                                    )}
                                    {Number(p.amount || 0).toFixed(2)}
                                </p>
                            </div>
                        ))
                    ) : (
                        <p className="text-center text-metallic-gray py-4">
                            No upcoming payments
                        </p>
                    )}
                </Card>
            </div>

            {/* Create Wallet Modal */}
            <CreateWalletModal
                isOpen={isCreateModalOpen}
                onClose={handleCloseCreateWallet}
                onSubmit={handleCreateWallet}
                isLoading={isCreatingWallet}
            />
        </div>
    );
};

export default MyWalletsContent;
