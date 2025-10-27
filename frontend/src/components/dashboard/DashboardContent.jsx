import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
// Import your custom stat icons - same as in EmptyState
import balanceIcon from '../../assets/icons/balance-icon.png';
import spendingIcon from '../../assets/icons/spending-icon.png';
import savedIcon from '../../assets/icons/saved-icon.png';
import mastercardIcon from '../../assets/icons/mastercard-icon.png';
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';

const DashboardContent = ({ wallets, onCreateWallet, userStats }) => {
    const [selectedWallet, setSelectedWallet] = useState(null);
    const [isWalletDialogOpen, setIsWalletDialogOpen] = useState(false);
    const navigate = useNavigate();

    const recentTransactions = [
        { name: 'Iphone 13 Pro MAX', type: 'Shopping', amount: 420.84, date: '11 Apr 2022' },
        { name: 'Netflix Subscription', type: 'Entertainment', amount: 100.00, date: '05 Apr 2022' },
        { name: 'Figma Subscription', type: 'Others', amount: 244.20, date: '02 Apr 2022' }
    ];

    const budgetData = {
        spent: 1730,
        total: 3000
    };

    const formatCardNumber = (number) => {
        if (!number) return '5495 7381 3759 2321';
        return number.replace(/(\d{4})/g, '$1 ').trim();
    };

    // Wallet colors from your palette
    const walletColors = ['#4E5C75', '#9C9C9C', '#0B0C10', '#4361ee', '#6FBAFC', '#D06978'];

    const getWalletColor = (index) => {
        return walletColors[index % walletColors.length];
    };

    // Currency symbols mapping
    const currencySymbols = {
        'USD': '$',
        'EUR': '€',
        'GBP': '£',
        'JPY': '¥',
        'CAD': 'CA$',
        'AUD': 'A$',
        'CHF': 'CHF',
        'CNY': '¥',
        'INR': '₹',
        'BRL': 'R$',
        'RUB': '₽',
        'KRW': '₩',
        'SGD': 'S$',
        'NZD': 'NZ$',
        'MXN': 'MX$',
        'HKD': 'HK$',
        'TRY': '₺',
        'SEK': 'kr',
        'NOK': 'kr',
        'DKK': 'kr',
        'ZAR': 'R',
        'PLN': 'zł',
        'THB': '฿',
        'MYR': 'RM',
        'IDR': 'Rp',
        'HUF': 'Ft',
        'CZK': 'Kč',
        'ILS': '₪',
        'CLP': 'CLP$',
        'PHP': '₱',
        'AED': 'د.إ',
        'COP': 'COL$',
        'SAR': '﷼',
        'QAR': '﷼',
        'KWD': 'د.ك',
        'EGP': '£',
        'ARS': 'ARS$',
        'NGN': '₦',
        'BDT': '৳',
        'PKR': '₨',
        'UAH': '₴',
        'VND': '₫',
        'RON': 'lei',
        'PEN': 'S/',
        'BGN': 'лв',
        'HRK': 'kn',
        'ISK': 'kr'
    };

    const getCurrencySymbol = (currencyCode) => {
        return currencySymbols[currencyCode] || '$';
    };

    const handleWalletClick = (wallet) => {
        setSelectedWallet(wallet);
        setIsWalletDialogOpen(true);
    };

    const handleAddTransaction = () => {
        setIsWalletDialogOpen(false);
        // Navigate to MyWallets page and open transaction modal
        navigate('/wallets', {
            state: {
                openTransactionModal: true,
                selectedWallet: selectedWallet
            }
        });
    };

    const WalletDetailDialog = () => {
        if (!selectedWallet) return null;

        return (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
                <div className="bg-white rounded-xl shadow-lg max-w-md w-full mx-4 relative">
                    {/* Card Preview - Full width matching the form */}
                    <div className="absolute -top-6 left-0 right-0 mx-6 z-10">
                        <div
                            className="bg-gradient-to-r from-[#6FBAFC] to-[#6FBAFC] p-6 text-white rounded-xl shadow-2xl"
                            style={{
                                background: selectedWallet.color ? `linear-gradient(to right, ${selectedWallet.color}, ${selectedWallet.color})` : undefined,
                                minHeight: '200px'
                            }}
                        >
                            <div className="flex justify-between items-start mb-4">
                                {/* Bank Name and Chip */}
                                <div className="flex items-center space-x-3">
                                    <div className="w-12 h-8 flex items-center justify-center">
                                        <img
                                            src={chipIcon}
                                            alt="Chip"
                                            className="w-full h-full object-contain"
                                        />
                                    </div>
                                    <div>
                                        <h3 className="text-lg font-semibold">T-Банк</h3>
                                        <p className="text-sm opacity-75">Total Balance</p>
                                        <p className="text-xl font-bold">
                                            {getCurrencySymbol(selectedWallet.currency)}{selectedWallet.balance || '0.00'}
                                        </p>
                                    </div>
                                </div>

                                {/* NFC Icon */}
                                <div className="w-10 h-10 flex items-center justify-center">
                                    <img
                                        src={nfcIcon}
                                        alt="NFC"
                                        className="w-full h-full object-contain"
                                    />
                                </div>
                            </div>

                            {/* Card Number */}
                            <div className="mb-4">
                                <span className="tracking-wider font-mono text-2xl font-bold">
                                    {formatCardNumber(selectedWallet.card_number)}
                                </span>
                            </div>

                            {/* Expiry and Mastercard */}
                            <div className="flex justify-between items-center">
                                <span className="text-lg">09/30</span>
                                <div className="text-right">
                                    <div className="flex items-center justify-end">
                                        <img
                                            src={mastercardIcon}
                                            alt="mastercard"
                                            className="h-6 object-contain"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Wallet Details Section - Pushed down to accommodate overlapping card */}
                    <div className="pt-44 pb-6 px-6">
                        {/* Balance and Exchange Rate Row - Exactly like screenshot */}
                        <div className="flex justify-between items-center mb-8">
                            <div>
                                <p className="text-sm text-metallic-gray mb-1">Total Balance</p>
                                <p className="text-2xl font-bold text-text">
                                    {getCurrencySymbol(selectedWallet.currency)}{selectedWallet.balance || '0.00'}
                                </p>
                            </div>
                            <div className="text-right">
                                <p className="text-sm text-metallic-gray mb-1">Exchange Rate</p>
                                <p className="text-lg font-semibold text-text">
                                    1 {selectedWallet.currency} = 1.0 USD
                                </p>
                            </div>
                        </div>

                        {/* Currency and Status Row */}
                        <div className="grid grid-cols-2 gap-4 mb-8">
                            <div>
                                <p className="text-sm text-metallic-gray mb-1">Currency</p>
                                <p className="text-lg font-semibold text-text">
                                    {selectedWallet.currency} - {getCurrencyName(selectedWallet.currency)}
                                </p>
                            </div>
                            <div>
                                <p className="text-sm text-metallic-gray mb-1">Status</p>
                                <div className="flex items-center">
                                    <span className="w-2 h-2 bg-green-500 rounded-full mr-2"></span>
                                    <p className="text-lg font-semibold text-text">Active</p>
                                </div>
                            </div>
                        </div>

                        {/* Add Transaction Text Button */}
                        <div className="text-center border-t border-strokes pt-6">
                            <button
                                onClick={handleAddTransaction}
                                className="text-blue text-lg font-semibold hover:text-blue-600 transition-colors duration-200 underline"
                            >
                                Add Transaction
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    };

    const getCurrencyName = (currencyCode) => {
        const currencyNames = {
            'USD': 'US Dollar',
            'EUR': 'Euro',
            'GBP': 'British Pound',
            'JPY': 'Japanese Yen',
            'CAD': 'Canadian Dollar',
            'AUD': 'Australian Dollar',
            'CHF': 'Swiss Franc',
            'CNY': 'Chinese Yuan',
            'INR': 'Indian Rupee',
            'BRL': 'Brazilian Real',
            'RUB': 'Russian Ruble',
            'KRW': 'South Korean Won',
            'SGD': 'Singapore Dollar',
            'NZD': 'New Zealand Dollar',
            'MXN': 'Mexican Peso',
            'HKD': 'Hong Kong Dollar',
            'TRY': 'Turkish Lira',
            'SEK': 'Swedish Krona',
            'NOK': 'Norwegian Krone',
            'DKK': 'Danish Krone',
            'ZAR': 'South African Rand',
            'PLN': 'Polish Zloty',
            'THB': 'Thai Baht',
            'MYR': 'Malaysian Ringgit',
            'IDR': 'Indonesian Rupiah',
            'HUF': 'Hungarian Forint',
            'CZK': 'Czech Koruna',
            'ILS': 'Israeli New Shekel',
            'CLP': 'Chilean Peso',
            'PHP': 'Philippine Peso',
            'AED': 'UAE Dirham',
            'COP': 'Colombian Peso',
            'SAR': 'Saudi Riyal',
            'QAR': 'Qatari Rial',
            'KWD': 'Kuwaiti Dinar',
            'EGP': 'Egyptian Pound',
            'ARS': 'Argentine Peso',
            'NGN': 'Nigerian Naira',
            'BDT': 'Bangladeshi Taka',
            'PKR': 'Pakistani Rupee',
            'UAH': 'Ukrainian Hryvnia',
            'VND': 'Vietnamese Dong',
            'RON': 'Romanian Leu',
            'PEN': 'Peruvian Sol',
            'BGN': 'Bulgarian Lev',
            'HRK': 'Croatian Kuna',
            'ISK': 'Icelandic Króna'
        };
        return currencyNames[currencyCode] || 'Currency';
    };

    return (
        <div className="max-w-7xl mx-auto">
            {/* Stats Cards with Left-Centered Icons - EXACTLY like EmptyState */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                {/* Total Balance - Dark Background */}
                <div className="bg-[#363A3F] rounded-lg shadow-sm p-6 border border-strokes">
                    <div className="flex items-center">
                        <img src={balanceIcon} alt="Balance" className="w-10 h-10 mr-4" />
                        <div className="flex-1 text-center">
                            <h3 className="text-sm font-medium text-white mb-1">Total balance</h3>
                            <p className="text-2xl font-bold text-white">${userStats.totalBalance.toFixed(2)}</p>
                        </div>
                    </div>
                </div>

                {/* Total Spending */}
                <div className="bg-white rounded-lg shadow-sm p-6 border border-strokes">
                    <div className="flex items-center">
                        <img src={spendingIcon} alt="Spending" className="w-10 h-10 mr-4" />
                        <div className="flex-1 text-center">
                            <h3 className="text-sm font-medium text-metallic-gray mb-1">Total spending</h3>
                            <p className="text-2xl font-bold text-text">${userStats.totalSpending.toFixed(2)}</p>
                        </div>
                    </div>
                </div>

                {/* Total Saved */}
                <div className="bg-white rounded-lg shadow-sm p-6 border border-strokes">
                    <div className="flex items-center">
                        <img src={savedIcon} alt="Saved" className="w-10 h-10 mr-4" />
                        <div className="flex-1 text-center">
                            <h3 className="text-sm font-medium text-metallic-gray mb-1">Total saved</h3>
                            <p className="text-2xl font-bold text-text">${userStats.totalSaved.toFixed(2)}</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Main Content with Wallets */}
            <div className="flex flex-col lg:flex-row gap-8">
                {/* Left Content - Chart and Transactions */}
                <div className="flex-1">
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                        {/* Left Column - Chart and Transactions */}
                        <div className="lg:col-span-2 space-y-8">
                            {/* Working Capital Chart */}
                            <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
                                <h3 className="text-lg font-semibold text-text mb-6">Working Capital</h3>
                                <div className="h-64 bg-gray-100 rounded-lg flex items-center justify-center">
                                    <p className="text-metallic-gray">Chart Placeholder - Working Capital</p>
                                </div>
                            </div>

                            {/* Recent Transactions */}
                            <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
                                <h3 className="text-lg font-semibold text-text mb-6">Recent Transaction</h3>
                                <div className="overflow-x-auto">
                                    <table className="w-full">
                                        <thead>
                                        <tr className="border-b border-strokes">
                                            <th className="text-left py-3 text-sm font-medium text-metallic-gray">NAME/BANK/CARD</th>
                                            <th className="text-left py-3 text-sm font-medium text-metallic-gray">TYPE</th>
                                            <th className="text-left py-3 text-sm font-medium text-metallic-gray">AMOUNT</th>
                                            <th className="text-left py-3 text-sm font-medium text-metallic-gray">DATE</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {recentTransactions.map((transaction, index) => (
                                            <tr key={index} className="border-b border-strokes hover:bg-gray-50 transition-colors">
                                                <td className="py-4">
                                                    <div className="font-medium text-text">{transaction.name}</div>
                                                </td>
                                                <td className="py-4 text-metallic-gray">{transaction.type}</td>
                                                <td className="py-4 font-semibold text-text">${transaction.amount}</td>
                                                <td className="py-4 text-metallic-gray">{transaction.date}</td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Right Sidebar - Wallets and Budget */}
                <div className="lg:w-96 space-y-8">
                    {/* Wallets Section */}
                    <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
                        <div className="flex justify-between items-center mb-6">
                            <h3 className="text-lg font-semibold text-text">Wallet</h3>
                            <span className="text-sm text-metallic-gray">{wallets.length} wallets</span>
                        </div>

                        {/* 3D Scrollable Wallets Container */}
                        <div className="relative">
                            <div className="max-h-80 overflow-y-auto space-y-4 mb-6 pr-2 scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-gray-100">
                                {wallets.map((wallet, index) => (
                                    <div
                                        key={wallet.id}
                                        className="border-0 rounded-xl p-4 transition-all duration-500 transform hover:scale-105 hover:rotate-1 shadow-lg cursor-pointer"
                                        style={{
                                            backgroundColor: getWalletColor(index),
                                            background: `linear-gradient(135deg, ${getWalletColor(index)}99, ${getWalletColor(index)}CC)`
                                        }}
                                        onClick={() => handleWalletClick(wallet)}
                                    >
                                        <div className="flex justify-between items-start mb-3">
                                            <div className="flex-1">
                                                <div className="flex items-center gap-2 mb-2">
                                                    <div
                                                        className="w-3 h-3 rounded-full border-2 border-white"
                                                        style={{ backgroundColor: wallet.color || '#6FBAFC' }}
                                                    ></div>
                                                    <h4 className="font-semibold text-white text-sm">{wallet.name}</h4>
                                                </div>
                                                <p className="text-xs text-white opacity-80 mb-1">Total Balance</p>
                                                <p className="text-lg font-bold text-white">
                                                    {getCurrencySymbol(wallet.currency)}{wallet.balance || '0.00'}
                                                </p>
                                            </div>
                                            <span className="text-xs px-2 py-1 bg-white bg-opacity-20 rounded text-white capitalize backdrop-blur-sm">
                                                {wallet.wallet_type?.replace('_', ' ') || 'debit card'}
                                            </span>
                                        </div>
                                        <div className="text-xs text-white opacity-90 font-mono mb-2">
                                            {formatCardNumber(wallet.card_number)}
                                        </div>
                                        <div className="flex justify-between items-center text-xs text-white opacity-80">
                                            <span>{wallet.currency}</span>
                                            <span className="flex items-center">
                                                <span className="w-2 h-2 bg-green-400 rounded-full mr-1"></span>
                                                Active
                                            </span>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            {/* 3D Scroll Indicator */}
                            {wallets.length > 3 && (
                                <div className="absolute bottom-0 left-0 right-0 text-center">
                                    <div className="inline-flex animate-bounce">
                                        <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                                        </svg>
                                    </div>
                                </div>
                            )}
                        </div>

                        {/* Add Wallet Button */}
                        <button
                            onClick={onCreateWallet}
                            className="w-full py-4 text-lg font-semibold text-white rounded-xl hover:opacity-90 transition-all duration-300 transform hover:scale-105 flex items-center justify-center shadow-lg"
                            style={{ backgroundColor: '#4361ee' }}
                        >
                            + Add Wallet
                        </button>
                    </div>

                    {/* Budget vs Expense */}
                    <div
                        className="rounded-xl shadow-sm p-6 border border-strokes transition-all duration-300 hover:shadow-md"
                        style={{ backgroundColor: '#4E5C75' }}
                    >
                        <h3 className="text-lg font-semibold text-white mb-4">Budget Vs Expense</h3>
                        <p className="text-sm text-white opacity-80 mb-2">From 01 - 30 April</p>
                        <div className="mb-4">
                            <p className="text-2xl font-bold text-white">
                                ${budgetData.spent.toLocaleString()} of ${budgetData.total.toLocaleString()}
                            </p>
                        </div>
                        <div className="w-full bg-gray-600 rounded-full h-3 mb-2">
                            <div
                                className="bg-white rounded-full h-3 transition-all duration-500"
                                style={{ width: `${(budgetData.spent / budgetData.total) * 100}%` }}
                            ></div>
                        </div>
                        <p className="text-sm text-white opacity-80 text-right">
                            {Math.round((budgetData.spent / budgetData.total) * 100)}% spent
                        </p>
                    </div>

                    {/* Quick Stats */}
                    <div
                        className="rounded-xl shadow-sm p-6 border border-strokes transition-all duration-300 hover:shadow-md"
                        style={{ backgroundColor: '#0B0C10' }}
                    >
                        <h3 className="text-lg font-semibold text-white mb-4">Quick Stats</h3>
                        <div className="space-y-4">
                            <div className="flex justify-between items-center">
                                <span className="text-white opacity-80">Monthly Budget</span>
                                <span className="font-semibold text-white">${budgetData.total.toLocaleString()}</span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-white opacity-80">Remaining</span>
                                <span className="font-semibold text-green-400">
                                    ${(budgetData.total - budgetData.spent).toLocaleString()}
                                </span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-white opacity-80">Daily Average</span>
                                <span className="font-semibold text-white">
                                    ${(budgetData.spent / 30).toFixed(2)}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Wallet Detail Dialog */}
            {isWalletDialogOpen && <WalletDetailDialog />}
        </div>
    );
};

export default DashboardContent;