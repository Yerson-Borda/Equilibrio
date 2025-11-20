import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
// Import your custom stat icons
import balanceIcon from '../../assets/icons/balance-icon.png';
import spendingIcon from '../../assets/icons/spending-icon.png';
import savedIcon from '../../assets/icons/saved-icon.png';
import mastercardIcon from '../../assets/icons/mastercard-icon.png';
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';

// Import chart & budget components
import DualLineChart from '../charts/DualLineChart.jsx';
import BudgetSetup from '../budget/BudgetSetup.jsx';
import BudgetProgress from '../budget/BudgetProgress.jsx';
import { apiService } from '../../services/api';
import { syncService } from '../../services/syncService';

const DashboardContent = ({ wallets, onCreateWallet, userStats }) => {
    const [selectedWallet, setSelectedWallet] = useState(null);
    const [isWalletDialogOpen, setIsWalletDialogOpen] = useState(false);
    const [chartData, setChartData] = useState({
        spendingTrends: []
    });
    const [budgetData, setBudgetData] = useState({
        budget: 3000,
        spent: 1730,
        month: new Date().toISOString().slice(0, 7)
    });
    const [isLoading, setIsLoading] = useState(false);
    const [recentTransactions, setRecentTransactions] = useState([]);
    const [selectedRange, setSelectedRange] = useState('7'); // "last 7 days" by default
    const navigate = useNavigate();

    useEffect(() => {
        loadChartData(selectedRange);
        loadBudgetData();
        loadRecentTransactions();

        // Set up sync event listeners
        const handleSyncUpdate = () => {
            loadChartData(selectedRange);
            loadBudgetData();
            loadRecentTransactions();
        };

        window.addEventListener('sync_transaction_updated', handleSyncUpdate);
        window.addEventListener('sync_wallet_updated', handleSyncUpdate);

        return () => {
            window.removeEventListener('sync_transaction_updated', handleSyncUpdate);
            window.removeEventListener('sync_wallet_updated', handleSyncUpdate);
        };
    }, [selectedRange]);

    const buildIncomeExpenseSeries = (transactions, days) => {
        const today = new Date();
        const start = new Date();
        start.setHours(0, 0, 0, 0);
        today.setHours(0, 0, 0, 0);
        start.setDate(start.getDate() - (days - 1));

        const dateMap = new Map();

        // Initialize all days to 0
        for (let i = 0; i < days; i++) {
            const d = new Date(start);
            d.setDate(start.getDate() + i);
            const key = d.toISOString().split('T')[0];
            dateMap.set(key, {
                date: key,
                label: d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
                income: 0,
                expense: 0
            });
        }

        // Aggregate income and expenses
        transactions.forEach((t) => {
            if (!t.transaction_date) return;
            const d = new Date(t.transaction_date);
            d.setHours(0, 0, 0, 0);
            if (d < start || d > today) return;

            const key = d.toISOString().split('T')[0];
            const item = dateMap.get(key);
            if (!item) return;

            const amount = parseFloat(t.amount) || 0;
            if (t.type === 'income') {
                item.income += amount;
            } else if (t.type === 'expense') {
                item.expense += amount;
            }
        });

        return Array.from(dateMap.values());
    };

    const loadChartData = async (range = '7') => {
        setIsLoading(true);
        try {
            const days = parseInt(range, 10) || 7;
            const transactions = await apiService.getTransactions();
            const series = buildIncomeExpenseSeries(transactions || [], days);

            setChartData({
                spendingTrends: series
            });
        } catch (error) {
            console.error('Error loading chart data:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const loadBudgetData = async () => {
        try {
            const today = new Date();
            const currentMonth = today.toISOString().slice(0, 7);
            const savedBudget = localStorage.getItem(`budget_${currentMonth}`);

            if (savedBudget) {
                const budget = JSON.parse(savedBudget);
                const startDate = new Date(today.getFullYear(), today.getMonth(), 1)
                    .toISOString()
                    .split('T')[0];
                const endDate = new Date(today.getFullYear(), today.getMonth() + 1, 0)
                    .toISOString()
                    .split('T')[0];

                const analytics = await apiService.getCategorySummary(startDate, endDate);
                const totalSpent = analytics?.total_expenses || 0;

                setBudgetData({
                    budget: budget.amount,
                    spent: totalSpent,
                    month: currentMonth
                });
            }
        } catch (error) {
            console.error('Error loading budget data:', error);
        }
    };

    const loadRecentTransactions = async () => {
        try {
            const transactions = await apiService.getTransactions();
            // Get last 3 transactions (assuming API returns newest first)
            const recent = (transactions || []).slice(0, 3).map((transaction) => ({
                name: transaction.description || 'Transaction',
                type: transaction.type,
                amount: parseFloat(transaction.amount),
                date: new Date(transaction.transaction_date).toLocaleDateString('en-US', {
                    day: 'numeric',
                    month: 'short',
                    year: 'numeric'
                })
            }));
            setRecentTransactions(recent);
        } catch (error) {
            console.error('Error loading recent transactions:', error);
        }
    };

    const handleBudgetUpdate = (newBudgetData) => {
        setBudgetData(newBudgetData);
        loadChartData(selectedRange);
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
        USD: '$',
        EUR: '€',
        GBP: '£',
        JPY: '¥',
        CAD: 'CA$',
        AUD: 'A$',
        CHF: 'CHF',
        CNY: '¥',
        INR: '₹',
        BRL: 'R$',
        RUB: '₽',
        KRW: '₩',
        SGD: 'S$',
        NZD: 'NZ$',
        MXN: 'MX$',
        HKD: 'HK$',
        TRY: '₺',
        SEK: 'kr',
        NOK: 'kr',
        DKK: 'kr',
        ZAR: 'R',
        PLN: 'zł',
        THB: '฿',
        MYR: 'RM',
        IDR: 'Rp',
        HUF: 'Ft',
        CZK: 'Kč',
        ILS: '₪',
        CLP: 'CLP$',
        PHP: '₱',
        AED: 'د.إ',
        COP: 'COL$',
        SAR: '﷼',
        QAR: '﷼',
        KWD: 'د.ك',
        EGP: 'E£',
        ARS: '$',
        NGN: '₦',
        BDT: '৳',
        PKR: '₨',
        UAH: '₴',
        VND: '₫',
        RON: 'lei',
        PEN: 'S/',
        BGN: 'лв',
        HRK: 'kn',
        ISK: 'kr'
    };

    const getCurrencySymbol = (currencyCode) => {
        return currencySymbols[currencyCode] || '$';
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
                                background: selectedWallet.color
                                    ? `linear-gradient(to right, ${selectedWallet.color}, ${selectedWallet.color})`
                                    : undefined,
                                minHeight: '200px'
                            }}
                        >
                            <div className="flex justify-between items-start mb-4">
                                {/* Bank Name and Chip */}
                                <div className="flex items-center space-x-3">
                                    <img
                                        src={mastercardIcon}
                                        alt="Bank"
                                        className="h-6 object-contain"
                                    />
                                    <span className="text-lg font-semibold">
                                        {selectedWallet.name || 'Wallet'}
                                    </span>
                                </div>
                                <img src={chipIcon} alt="Chip" className="h-6" />
                            </div>

                            {/* Balance and Card Number */}
                            <div className="mt-4">
                                <p className="text-xs text-gray-200 mb-1">Total Balance</p>
                                <p className="text-2xl font-bold">
                                    {getCurrencySymbol(selectedWallet.currency)}
                                    {selectedWallet.balance || '0.00'}
                                </p>
                                <p className="text-xs tracking-widest mt-4">
                                    {formatCardNumber(selectedWallet.card_number)}
                                </p>
                            </div>

                            {/* Footer Row */}
                            <div className="mt-4 flex justify-between items-center text-xs text-gray-100">
                                <span>{selectedWallet.wallet_type?.replace('_', ' ')}</span>
                                <span className="flex items-center space-x-2">
                                    <img src={nfcIcon} alt="NFC" className="h-4" />
                                    <span>09/30</span>
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* Wallet details body */}
                    <div className="pt-40 pb-6 px-6">
                        <div className="flex justify-between items-center mb-4">
                            <h3 className="text-lg font-semibold text-text">
                                {selectedWallet.name}
                            </h3>
                            <button
                                onClick={() => setIsWalletDialogOpen(false)}
                                className="text-sm text-metallic-gray hover:text-text"
                            >
                                Close
                            </button>
                        </div>

                        <div className="space-y-3 text-sm text-metallic-gray">
                            <div className="flex justify-between">
                                <span>Currency</span>
                                <span>{selectedWallet.currency}</span>
                            </div>
                            <div className="flex justify-between">
                                <span>Type</span>
                                <span>
                                    {selectedWallet.wallet_type?.replace('_', ' ') || 'debit card'}
                                </span>
                            </div>
                            <div className="flex justify-between">
                                <span>Balance</span>
                                <span>
                                    {getCurrencySymbol(selectedWallet.currency)}
                                    {selectedWallet.balance || '0.00'}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    };

    const handleWalletClick = (wallet) => {
        setSelectedWallet(wallet);
        setIsWalletDialogOpen(true);
    };

    const handleViewAllTransactions = () => {
        navigate('/transactions');
    };

    const renderRangeButton = (value, label) => {
        const isActive = selectedRange === value;
        return (
            <button
                key={value}
                type="button"
                onClick={() => setSelectedRange(value)}
                className={`px-3 py-1 text-xs sm:text-sm ${
                    isActive
                        ? 'bg-blue text-white'
                        : 'bg-white text-metallic-gray hover:bg-gray-100'
                }`}
                style={isActive ? { backgroundColor: '#4361ee' } : {}}
            >
                {label}
            </button>
        );
    };

    return (
        <div className="max-w-7xl mx-auto pt-8">
            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                {/* Total Balance - Dark Background */}
                <div className="bg-[#363A3F] rounded-lg shadow-sm p-6 border border-strokes">
                    <div className="flex items-center">
                        <img src={balanceIcon} alt="Balance" className="w-10 h-10 mr-4" />
                        <div className="flex-1 text-center">
                            <h3 className="text-sm font-medium text-white mb-1">
                                Total balance
                            </h3>
                            <p className="text-2xl font-bold text-white">
                                {getCurrencySymbol(userStats.defaultCurrency)}
                                {userStats.totalBalance.toFixed(2)}
                            </p>
                        </div>
                    </div>
                </div>

                {/* Total Spending */}
                <div className="bg-white rounded-lg shadow-sm p-6 border border-strokes">
                    <div className="flex items-center">
                        <img src={spendingIcon} alt="Spending" className="w-10 h-10 mr-4" />
                        <div className="flex-1 text-center">
                            <h3 className="text-sm font-medium text-metallic-gray mb-1">
                                Total spending
                            </h3>
                            <p className="text-2xl font-bold text-text">
                                {getCurrencySymbol(userStats.defaultCurrency)}
                                {userStats.totalSpending.toFixed(2)}
                            </p>
                        </div>
                    </div>
                </div>

                {/* Total Saved */}
                <div className="bg-white rounded-lg shadow-sm p-6 border border-strokes">
                    <div className="flex items-center">
                        <img src={savedIcon} alt="Saved" className="w-10 h-10 mr-4" />
                        <div className="flex-1 text-center">
                            <h3 className="text-sm font-medium text-metallic-gray mb-1">
                                Total saved
                            </h3>
                            <p className="text-2xl font-bold text-text">
                                {getCurrencySymbol(userStats.defaultCurrency)}
                                {userStats.totalSaved.toFixed(2)}
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Main Content with Chart / Transactions / Wallets */}
            <div className="flex flex-col lg:flex-row gap-8">
                {/* Left Content */}
                <div className="flex-1 space-y-8">
                    {/* Single Income vs Expense Chart with filter */}
                    <DualLineChart
                        data={chartData.spendingTrends}
                        title="Income vs Expense"
                        xKey="label"
                        line1Key="income"
                        line1Name="Income"
                        line2Key="expense"
                        line2Name="Expense"
                        currencySymbol={getCurrencySymbol(userStats.defaultCurrency)}
                        headerRight={
                            <div className="inline-flex rounded-lg border border-strokes overflow-hidden bg-white">
                                {renderRangeButton('7', 'Last 7 days')}
                                {renderRangeButton('30', 'Last 30 days')}
                                {renderRangeButton('90', 'Last 90 days')}
                            </div>
                        }
                    />

                    {/* Recent Transactions */}
                    <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
                        <div className="flex items-center justify-between mb-6">
                            <h3 className="text-lg font-semibold text-text">
                                Recent Transactions
                            </h3>
                            <button
                                onClick={handleViewAllTransactions}
                                className="text-sm text-blue-600 hover:underline"
                            >
                                View all
                            </button>
                        </div>
                        <div className="overflow-x-auto">
                            <table className="w-full">
                                <thead>
                                <tr className="border-b border-strokes">
                                    <th className="text-left py-3 text-sm font-medium text-metallic-gray">
                                        NAME/BANK/CARD
                                    </th>
                                    <th className="text-left py-3 text-sm font-medium text-metallic-gray">
                                        TYPE
                                    </th>
                                    <th className="text-left py-3 text-sm font-medium text-metallic-gray">
                                        AMOUNT
                                    </th>
                                    <th className="text-left py-3 text-sm font-medium text-metallic-gray">
                                        DATE
                                    </th>
                                </tr>
                                </thead>
                                <tbody>
                                {recentTransactions.length === 0 && (
                                    <tr>
                                        <td
                                            colSpan="4"
                                            className="py-4 text-center text-metallic-gray"
                                        >
                                            No recent transactions
                                        </td>
                                    </tr>
                                )}
                                {recentTransactions.map((tx, index) => (
                                    <tr
                                        key={index}
                                        className="border-b border-strokes last:border-b-0"
                                    >
                                        <td className="py-3 text-sm text-text">
                                            {tx.name}
                                        </td>
                                        <td className="py-3 text-sm capitalize text-text">
                                            {tx.type}
                                        </td>
                                        <td
                                            className={`py-3 text-sm font-semibold ${
                                                tx.type === 'income'
                                                    ? 'text-green-600'
                                                    : 'text-red-600'
                                            }`}
                                        >
                                            {getCurrencySymbol(
                                                userStats.defaultCurrency
                                            )}
                                            {tx.amount.toFixed(2)}
                                        </td>
                                        <td className="py-3 text-sm text-metallic-gray">
                                            {tx.date}
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                {/* Right Sidebar - Wallets and Budget */}
                <div className="lg:w-96 space-y-8">
                    {/* Wallets Section */}
                    <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
                        <div className="flex justify-between items-center mb-6">
                            <h3 className="text-lg font-semibold text-text">Wallet</h3>
                            <span className="text-sm text-metallic-gray">
                                {wallets.length} wallets
                            </span>
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
                                            background: `linear-gradient(135deg, ${getWalletColor(
                                                index
                                            )}99, ${getWalletColor(index)}CC)`
                                        }}
                                        onClick={() => handleWalletClick(wallet)}
                                    >
                                        <div className="flex justify-between items-start mb-3">
                                            <div className="flex-1">
                                                <div className="flex items-center gap-2 mb-2">
                                                    <div
                                                        className="w-3 h-3 rounded-full border-2 border-white"
                                                        style={{
                                                            backgroundColor:
                                                                wallet.color || '#6FBAFC'
                                                        }}
                                                    ></div>
                                                    <h4 className="font-semibold text-white text-sm">
                                                        {wallet.name}
                                                    </h4>
                                                </div>
                                                <p className="text-xs text-white opacity-80 mb-1">
                                                    Total Balance
                                                </p>
                                                <p className="text-lg font-bold text-white">
                                                    {getCurrencySymbol(wallet.currency)}
                                                    {wallet.balance || '0.00'}
                                                </p>
                                            </div>
                                            <span className="text-xs px-2 py-1 bg-white bg-opacity-20 rounded text-white capitalize backdrop-blur-sm">
                                                {wallet.wallet_type?.replace('_', ' ') ||
                                                    'debit card'}
                                            </span>
                                        </div>
                                        <div className="text-xs text-white opacity-90 font-mono mb-2">
                                            {formatCardNumber(wallet.card_number)}
                                        </div>
                                        <div className="flex justify-between items-center text-xs text-white opacity-80">
                                            <span>09/30</span>
                                            <img
                                                src={mastercardIcon}
                                                alt="mastercard"
                                                className="h-5 object-contain"
                                            />
                                        </div>
                                    </div>
                                ))}
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
                    </div>

                    {/* Budget Setup */}
                    <BudgetSetup onBudgetUpdate={handleBudgetUpdate} />

                    {/* Budget Progress */}
                    <BudgetProgress
                        budget={budgetData.budget}
                        spent={budgetData.spent}
                        month={budgetData.month}
                        currencySymbol={getCurrencySymbol(userStats.defaultCurrency)}
                    />

                    {/* Quick Stats */}
                    <div
                        className="rounded-xl shadow-sm p-6 border border-strokes transition-all duration-300 hover:shadow-md"
                        style={{ backgroundColor: '#0B0C10' }}
                    >
                        <h3 className="text-lg font-semibold text-white mb-4">
                            Quick Stats
                        </h3>
                        <div className="space-y-4">
                            <div className="flex justify-between items-center">
                                <span className="text-white opacity-80">Monthly Budget</span>
                                <span className="font-semibold text-white">
                                    {getCurrencySymbol(userStats.defaultCurrency)}
                                    {budgetData.budget.toLocaleString()}
                                </span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-white opacity-80">Remaining</span>
                                <span className="font-semibold text-green-400">
                                    {getCurrencySymbol(userStats.defaultCurrency)}
                                    {(budgetData.budget - budgetData.spent).toLocaleString()}
                                </span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-white opacity-80">Daily Average</span>
                                <span className="font-semibold text-white">
                                    {getCurrencySymbol(userStats.defaultCurrency)}
                                    {(budgetData.spent / 30).toFixed(2)}
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
