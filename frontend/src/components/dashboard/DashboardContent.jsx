import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

// Stat icons
import balanceIcon from '../../assets/icons/balance-icon.png';
import spendingIcon from '../../assets/icons/spending-icon.png';
import savedIcon from '../../assets/icons/saved-icon.png';

// Card assets – copied from MyWalletsContent design
import mastercardIcon from '../../assets/icons/mastercard-icon.png';
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';

import DualLineChart from '../charts/DualLineChart.jsx';
import BudgetGauge from '../budget/BudgetGauge.jsx';

import { apiService } from '../../services/api';
import { syncService } from '../../services/syncService';
import { getCurrencySymbol, formatCurrencyMasked } from '../../config/currencies';

// Helper to show "USD - US Dollar" style text
const getCurrencyDisplay = (code) => {
    if (!code) return 'USD - US Dollar';
    const upper = code.toUpperCase();
    const map = {
        USD: 'USD - US Dollar',
        EUR: 'EUR - Euro',
        GBP: 'GBP - British Pound',
        RUB: 'RUB - Russian Ruble',
        IDR: 'IDR - Indonesian Rupiah',
        MYR: 'MYR - Malaysian Ringgit',
        SGD: 'SGD - Singapore Dollar',
        INR: 'INR - Indian Rupee',
        JPY: 'JPY - Japanese Yen',
    };
    return map[upper] || `${upper} - ${upper}`;
};

const DashboardContent = ({ wallets = [], userStats }) => {
    const navigate = useNavigate();

    const [chartData, setChartData] = useState({ spendingTrends: [] });
    const [budgetData, setBudgetData] = useState({
        budget: 0,
        spent: 0,
        month: new Date().toISOString().slice(0, 7),
    });
    const [recentTransactions, setRecentTransactions] = useState([]);
    const [selectedRange, setSelectedRange] = useState('7');

    const [dashboardWallets, setDashboardWallets] = useState(wallets || []);
    const [selectedWalletIndex, setSelectedWalletIndex] = useState(0);
    const [hoverWalletIndex, setHoverWalletIndex] = useState(0);
    const walletStackRef = useRef(null);
    const [selectedWallet, setSelectedWallet] = useState(null);
    const [isWalletDialogOpen, setIsWalletDialogOpen] = useState(false);

    const currencySymbol = getCurrencySymbol(userStats?.defaultCurrency || 'USD');

    const parseNumber = (value) => {
        if (value === null || value === undefined) return 0;
        const n = typeof value === 'string' ? parseFloat(value) : Number(value);
        return Number.isNaN(n) ? 0 : n;
    };

    // keep dashboardWallets synced with prop & backend
    useEffect(() => {
        if (wallets && wallets.length) {
            setDashboardWallets(wallets);
        } else {
            (async () => {
                try {
                    const w = await apiService.getWallets();
                    setDashboardWallets(w || []);
                } catch (e) {
                    console.error('Error loading wallets for dashboard:', e);
                }
            })();
        }
    }, [wallets]);

    // Ensure selectedWallet is always one of the dashboardWallets
    useEffect(() => {
        if (dashboardWallets.length === 0) {
            setSelectedWallet(null);
            setSelectedWalletIndex(0);
            return;
        }

        if (!selectedWallet) {
            setSelectedWallet(dashboardWallets[0]);
            setSelectedWalletIndex(0);
            return;
        }

        const idx = dashboardWallets.findIndex((w) => w.id === selectedWallet.id);
        if (idx === -1) {
            setSelectedWallet(dashboardWallets[0]);
            setSelectedWalletIndex(0);
        } else {
            setSelectedWalletIndex(idx);
        }
    }, [dashboardWallets, selectedWallet]);

    // Keep hover stack in sync with selected wallet (so mouse leave returns to selection)
    useEffect(() => {
        setHoverWalletIndex(selectedWalletIndex);
    }, [selectedWalletIndex]);


    // ----- chart helpers -----
    const buildIncomeExpenseSeries = (transactions, days) => {
        const today = new Date();
        const start = new Date();
        start.setHours(0, 0, 0, 0);
        today.setHours(0, 0, 0, 0);
        start.setDate(start.getDate() - (days - 1));

        const dateMap = new Map();

        for (let i = 0; i < days; i++) {
            const d = new Date(start);
            d.setDate(start.getDate() + i);
            const key = d.toISOString().split('T')[0];
            dateMap.set(key, {
                date: key,
                label: d.toLocaleDateString('en-US', {
                    month: 'short',
                    day: 'numeric',
                }),
                income: 0,
                expense: 0,
            });
        }

        (transactions || []).forEach((t) => {
            if (!t.transaction_date) return;
            const d = new Date(t.transaction_date);
            d.setHours(0, 0, 0, 0);
            if (d < start || d > today) return;

            const key = d.toISOString().split('T')[0];
            const entry = dateMap.get(key);
            if (!entry) return;

            const amount = parseFloat(t.amount) || 0;
            if (t.type === 'income') entry.income += amount;
            else if (t.type === 'expense') entry.expense += amount;
        });

        return Array.from(dateMap.values());
    };

    const loadChartData = async (range) => {
        try {
            let days = 7;
            if (range === '30') days = 30;
            if (range === '90') days = 90;

            const transactions = await apiService.getTransactions();
            const spendingTrends = buildIncomeExpenseSeries(transactions, days);

            setChartData({ spendingTrends });
        } catch (error) {
            console.error('Error loading chart data:', error);
        }
    };

    const loadBudgetData = async () => {
        try {
            const budget = await apiService.getCurrentBudget();
            if (budget) {
                const monthStr = `${budget.year}-${String(budget.month).padStart(2, '0')}`;
                setBudgetData({
                    budget: parseNumber(budget.monthly_limit),
                    spent: parseNumber(budget.monthly_spent),
                    month: monthStr,
                });
            } else {
                const today = new Date();
                const monthStr = today.toISOString().slice(0, 7);
                setBudgetData({ budget: 0, spent: 0, month: monthStr });
            }
        } catch (e) {
            console.error('Error loading budget data:', e);
        }
    };

    const loadRecentTransactions = async () => {
        try {
            const transactions = await apiService.getTransactions();
            const sorted = (transactions || [])
                .slice()
                .sort((a, b) => new Date(b.transaction_date) - new Date(a.transaction_date));
            setRecentTransactions(sorted.slice(0, 4));
        } catch (error) {
            console.error('Error loading recent transactions:', error);
        }
    };

    const handleSyncUpdate = () => {
        loadChartData(selectedRange);
        loadBudgetData();
        loadRecentTransactions();
        apiService
            .getWallets()
            .then((w) => setDashboardWallets(w || []))
            .catch((e) => console.error('Error refreshing wallets on sync:', e));
    };

    useEffect(() => {
        loadChartData(selectedRange);
        loadBudgetData();
        loadRecentTransactions();

        syncService.addListener('transaction_synced', handleSyncUpdate);
        syncService.addListener('wallet_synced', handleSyncUpdate);

        return () => {
            syncService.removeListener('transaction_synced', handleSyncUpdate);
            syncService.removeListener('wallet_synced', handleSyncUpdate);
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedRange]);

    // ----- navigation / helpers -----
    const handleViewAllTransactions = () => navigate('/transactions');
    const handleViewAllWallets = () => navigate('/my-wallets');

    const formatCardNumber = (number) => {
        if (!number) return '•••• •••• •••• ••••';
        const digits = String(number).replace(/\D/g, '');
        const lastFour = digits.slice(-4) || '••••';
        return `•••• •••• •••• ${lastFour}`;
    };

    // Single-click opens Card Overview dialog for that wallet
    const openWalletDialog = (wallet) => {
        if (!wallet) return;
        const idx = dashboardWallets.findIndex((w) => w.id === wallet.id);
        if (idx >= 0) setSelectedWalletIndex(idx);
        setSelectedWallet(wallet);
        setIsWalletDialogOpen(true);
    };

    const handleAddTransactionFromDialog = () => {
        if (!selectedWallet) return;

        setIsWalletDialogOpen(false);

        // ✅ go to MyWallets and tell it to open the modal
        navigate('/wallets', {
            state: {
                openAddTransaction: true,
                defaultWalletId: selectedWallet.id,
            },
        });
    };

    // ------- Wallet detail dialog (Card Shortcut 3.2) -------
    const WalletDetailDialog = () => {
        if (!isWalletDialogOpen || !selectedWallet) return null;

        const walletSymbol = getCurrencySymbol(selectedWallet.currency);
        const balance = parseNumber(selectedWallet.balance || 0);
        const positiveChange = 23.65; // placeholder %
        const negativeChange = 10.4;

        return (
            <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
                <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full overflow-hidden">
                    {/* TOP CARD – SOLID WALLET COLOR */}
                    <div
                        className="text-white p-6"
                        style={{ backgroundColor: selectedWallet.color || '#111827' }}
                    >
                        <div className="flex justify-between items-start mb-4">
                            <p className="text-sm font-semibold">
                                {selectedWallet.name || 'Wallet'}
                            </p>
                            <button
                                onClick={() => setIsWalletDialogOpen(false)}
                                className="text-gray-200 hover:text-white text-lg leading-none"
                            >
                                ✕
                            </button>
                        </div>

                        <div className="flex justify-between items-center mb-6">
                            <div className="flex items-start space-x-3">
                                <img
                                    src={chipIcon}
                                    alt="Chip"
                                    className="h-8 w-10 object-contain mt-4"
                                />
                                <div>
                                    <p className="text-xs opacity-80 mb-1 ml-6 mt-3">
                                        Total Balance
                                    </p>
                                    <p className="text-3xl font-bold ml-6">
                                        {formatCurrencyMasked(balance, walletSymbol, 16)}
                                    </p>
                                </div>
                            </div>
                            <div className="flex flex-col items-end space-y-2">
                                <img
                                    src={nfcIcon}
                                    alt="NFC"
                                    className="h-5 opacity-80 mt-2"
                                />
                                <img
                                    src={mastercardIcon}
                                    alt="Mastercard"
                                    className="h-7 opacity-90"
                                />
                            </div>
                        </div>

                        <div className="flex flex-col items-start text-xs opacity-80 mt-2">
                            <span className="tracking-widest font-mono text-sm mb-1">
                                {formatCardNumber(selectedWallet.card_number)}
                            </span>
                            <span>{selectedWallet.expiry || '09/30'}</span>
                        </div>
                    </div>

                    {/* WHITE SECTION */}
                    <div className="bg-white p-6">
                        <div className="mb-4">
                            <p className="text-sm text-metallic-gray mb-1">Your Balance</p>
                            <div className="flex items-center justify-between">
                                <p className="text-2xl font-semibold text-text">
                                    {formatCurrencyMasked(balance, walletSymbol, 16)}
                                </p>
                                <div className="flex items-center space-x-3 text-xs">
                                    <span className="flex items-center space-x-1 text-green-600">
                                        <span>▲</span>
                                        <span>{positiveChange}%</span>
                                    </span>
                                    <span className="flex items-center space-x-1 text-red-500">
                                        <span>▲</span>
                                        <span>{negativeChange}%</span>
                                    </span>
                                </div>
                            </div>
                        </div>

                        <hr className="border-strokes mb-4" />

                        <div className="grid grid-cols-2 gap-4 mb-4 text-sm">
                            <div>
                                <p className="text-metallic-gray mb-1">Currency</p>
                                <p className="font-medium text-text">
                                    {getCurrencyDisplay(selectedWallet.currency)}
                                </p>
                            </div>
                            <div>
                                <p className="text-metallic-gray mb-1">Status</p>
                                <p className="font-medium text-text">Active</p>
                            </div>
                        </div>

                        <hr className="border-strokes mb-2" />

                        <button
                            type="button"
                            onClick={handleAddTransactionFromDialog}
                            className="w-full text-center text-sm font-semibold text-[#16A34A] hover:text-[#15803D] py-2"
                        >
                            Add Transaction
                        </button>
                    </div>
                </div>
            </div>
        );
    };

    const budgetMonthLabel = budgetData.month
        ? new Date(`${budgetData.month}-01`).toLocaleDateString('en-US', { month: 'long' })
        : undefined;

    return (
        <div className="space-y-6">
            {/* ✅ ONE GRID ONLY: left column flows independently from right column (no empty space) */}
            <div className="grid grid-cols-1 xl:grid-cols-[2.1fr,1.2fr] gap-6 items-start">
                {/* ================= LEFT COLUMN ================= */}
                <div className="space-y-6">
                    {/* Stat Cards (stretch so combined width aligns with Working Capital) */}
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-[15px]">
                        {/* Total Balance - Dark */}
                        <div className="bg-[#363A3F] flex flex-row items-center w-full h-[110px] rounded-[10px] shadow-sm border border-strokes pt-6 pr-5 pb-6 pl-5 gap-[15px]">
                            <img src={balanceIcon} alt="Balance" className="w-10 h-10" />
                            <div className="flex-1 text-center">
                                <h3 className="text-sm font-medium text-[#9c9c9c] mb-1">
                                    Total balance
                                </h3>
                                <p className="text-2xl font-bold text-white">
                                    {formatCurrencyMasked(
                                        userStats?.totalBalance || 0,
                                        currencySymbol,
                                        13
                                    )}
                                </p>
                            </div>
                        </div>

                        {/* Total Spending */}
                        <div className="bg-white flex flex-row items-center w-full h-[110px] rounded-[10px] shadow-sm border border-strokes pt-6 pr-5 pb-6 pl-5 gap-[15px]">
                            <img src={spendingIcon} alt="Spending" className="w-10 h-10" />
                            <div className="flex-1 text-center">
                                <h3 className="text-sm font-medium text-metallic-gray mb-1">
                                    Total spending
                                </h3>
                                <p className="text-2xl font-bold text-text">
                                    {formatCurrencyMasked(userStats?.totalSpending || 0, currencySymbol, 13)}
                                </p>
                            </div>
                        </div>

                        {/* Total Saved */}
                        <div className="bg-white flex flex-row items-center w-full h-[110px] rounded-[10px] shadow-sm border border-strokes pt-6 pr-5 pb-6 pl-5 gap-[15px]">
                            <img src={savedIcon} alt="Saved" className="w-10 h-10" />
                            <div className="flex-1 text-center">
                                <h3 className="text-sm font-medium text-metallic-gray mb-1">
                                    Total saved
                                </h3>
                                <p className="text-2xl font-bold text-text">
                                    {formatCurrencyMasked(userStats?.totalSaved || 0, currencySymbol, 13)}
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Working Capital (moved up automatically; no big gap) */}
                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-lg font-semibold text-text">Working Capital</h2>

                            <div className="flex items-center space-x-6">
                                <div className="flex items-center space-x-3 text-xs">
                                    <div className="flex items-center space-x-1">
                                        <span className="inline-block w-3 h-3 rounded-full bg-[#6F5BFF]" />
                                        <span className="text-metallic-gray">Income</span>
                                    </div>
                                    <div className="flex items-center space-x-1">
                                        <span className="inline-block w-3 h-3 rounded-full bg-[#FFC75A]" />
                                        <span className="text-metallic-gray">Expenses</span>
                                    </div>
                                </div>

                                <div className="relative">
                                    <select
                                        value={selectedRange}
                                        onChange={(e) => setSelectedRange(e.target.value)}
                                        className="border border-strokes rounded-lg pl-3 pr-7 py-1.5 text-xs text-text bg-white focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent appearance-none"
                                    >
                                        <option value="7">Last 7 days</option>
                                        <option value="30">Last 30 days</option>
                                        <option value="90">Last 90 days</option>
                                    </select>
                                    <span className="pointer-events-none absolute inset-y-0 right-2 flex items-center text-[10px] text-metallic-gray">
                                        ▼
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="h-72">
                            <DualLineChart
                                data={chartData.spendingTrends}
                                currencySymbol={currencySymbol}
                            />
                        </div>
                    </div>

                    {/* Recent Transaction (also moved up) */}
                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-lg font-semibold text-text">Recent Transaction</h2>
                            <button
                                onClick={handleViewAllTransactions}
                                className="flex items-center text-xs text-blue hover:text-blue-700"
                            >
                                <span className="mr-1">View all</span>
                                <span className="text-sm">›</span>
                            </button>
                        </div>

                        <div className="overflow-x-auto">
                            <table className="min-w-full text-left text-sm">
                                <thead>
                                <tr className="text-xs text-metallic-gray border-b border-strokes">
                                    <th className="py-2 pr-4">NAME/BANK-CARD</th>
                                    <th className="py-2 pr-4">TYPE</th>
                                    <th className="py-2 pr-4">AMOUNT</th>
                                    <th className="py-2 pr-4">DATE</th>
                                </tr>
                                </thead>
                                <tbody>
                                {recentTransactions.length === 0 ? (
                                    <tr>
                                        <td
                                            colSpan="4"
                                            className="py-4 text-center text-sm text-metallic-gray"
                                        >
                                            No recent transactions.
                                        </td>
                                    </tr>
                                ) : (
                                    recentTransactions.map((t) => (
                                        <tr
                                            key={t.id}
                                            className="border-b last:border-b-0 border-strokes/60"
                                        >
                                            <td className="py-3 pr-4">
                                                <div>
                                                    <p className="text-sm font-medium text-text">
                                                        {t.description || t.note || 'Transaction'}
                                                    </p>
                                                    <p className="text-xs text-metallic-gray">
                                                        {t.wallet_name || 'Wallet'}
                                                    </p>
                                                </div>
                                            </td>
                                            <td className="py-3 pr-4 text-sm text-metallic-gray capitalize">
                                                {t.type}
                                            </td>
                                            <td className="py-3 pr-4 text-sm">
                                                    <span
                                                        className={
                                                            t.type === 'income'
                                                                ? 'text-green-600'
                                                                : 'text-red-600'
                                                        }
                                                    >
                                                        {t.type === 'income' ? '+' : '-'}
                                                        {currencySymbol}
                                                        {parseNumber(t.amount || 0).toFixed(2)}
                                                    </span>
                                            </td>
                                            <td className="py-3 pr-4 text-sm text-metallic-gray">
                                                {t.transaction_date
                                                    ? new Date(t.transaction_date).toLocaleDateString(
                                                        'en-US',
                                                        {
                                                            day: '2-digit',
                                                            month: 'short',
                                                            year: 'numeric',
                                                        }
                                                    )
                                                    : '--'}
                                            </td>
                                        </tr>
                                    ))
                                )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                {/* ================= RIGHT COLUMN ================= */}
                <div className="space-y-6">
                    {/* Wallet (next to Total Saved, stays at the top) */}
                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-lg font-semibold text-text">Wallet</h2>
                            <button
                                onClick={handleViewAllWallets}
                                className="text-xs text-metallic-gray hover:text-text"
                            >
                                •••
                            </button>
                        </div>

                        {dashboardWallets.length === 0 ? (
                            <div className="text-sm text-metallic-gray py-4">
                                No wallets yet. Create a wallet to see it here.
                            </div>
                        ) : (
                            <div
                                ref={walletStackRef}
                                className="relative mt-2 mx-auto mb-0 overflow-hidden select-none"
                                style={{
                                    width: '100%',
                                    maxWidth: '380px',
                                    height: 260, // fixed so the page never grows with N wallets
                                }}
                                onMouseMove={(e) => {
                                    if (!walletStackRef.current) return;
                                    const rect = walletStackRef.current.getBoundingClientRect();
                                    const y = e.clientY - rect.top;

                                    const peek = 32; // how much of each next card is visible
                                    const idx = Math.max(
                                        0,
                                        Math.min(dashboardWallets.length - 1, Math.floor(y / peek))
                                    );

                                    setHoverWalletIndex(idx);
                                }}
                                onMouseLeave={() => setHoverWalletIndex(selectedWalletIndex)}
                            >
                                {dashboardWallets.map((wallet, index) => {
                                    const isActive = index === hoverWalletIndex;
                                    const symbol = getCurrencySymbol(wallet.currency);

                                    const peek = 32;
                                    const maxAbove = peek * 2; // show at most ~2 cards above
                                    const top = Math.max(-maxAbove, (index - hoverWalletIndex) * peek);

                                    const distance = Math.abs(index - hoverWalletIndex);
                                    const scale = 1 - Math.min(0.03 * distance, 0.12);

                                    return (
                                        <div
                                            key={wallet.id}
                                            onClick={() => {
                                                setSelectedWallet(wallet);
                                                setSelectedWalletIndex(index);
                                                openWalletDialog(wallet);
                                            }}
                                            className="absolute left-0 w-full h-[250px] rounded-xl cursor-pointer transition-all duration-300"
                                            style={{
                                                top,
                                                zIndex: 50 - distance,
                                                backgroundColor: isActive
                                                    ? wallet.color || '#6FBAFC'
                                                    : 'rgba(255,255,255,0.72)',
                                                backdropFilter: isActive ? 'none' : 'blur(8px)',
                                                border: isActive
                                                    ? 'none'
                                                    : '1px solid hsla(0, 0%, 100%, 0.30)',
                                                transform: `scale(${scale})`,
                                                boxShadow: isActive
                                                    ? '0 12px 30px rgba(0,0,0,0.35)'
                                                    : '0 4px 10px rgba(0,0,0,0.08)',
                                            }}
                                        >
                                            <div className="p-6 flex flex-col justify-between h-full">
                                                <h3
                                                    className={`text-base font-semibold ${
                                                        isActive ? 'text-white' : 'text-text'
                                                    }`}
                                                >
                                                    {wallet.name}
                                                </h3>

                                                <div className="flex justify-between items-start mb-6">
                                                    <div className="flex items-start space-x-3">
                                                        <img
                                                            src={chipIcon}
                                                            alt="Chip"
                                                            className="w-10 h-8 object-contain mt-4"
                                                        />
                                                        <div>
                                                            <p
                                                                className={`text-xs font-bold leading-none ml-6 mt-3 ${
                                                                    isActive
                                                                        ? 'text-white/80'
                                                                        : 'text-metallic-gray'
                                                                }`}
                                                            >
                                                                Total Balance
                                                            </p>
                                                            <p
                                                                className={`text-xl font-bold leading-tight ml-6 ${
                                                                    isActive
                                                                        ? 'text-white'
                                                                        : 'text-text'
                                                                }`}
                                                            >
                                                                {formatCurrencyMasked(wallet.balance || 0, symbol, 15)}
                                                            </p>
                                                        </div>
                                                    </div>
                                                    <img
                                                        src={nfcIcon}
                                                        alt="NFC"
                                                        className="w-8 h-7 object-contain mt-3"
                                                    />
                                                </div>

                                                <p
                                                    className={`mt-4 text-lg font-mono tracking-wider ${
                                                        isActive ? 'text-white' : 'text-text'
                                                    }`}
                                                >
                                                    {formatCardNumber(wallet.card_number)}
                                                </p>

                                                <div className="flex justify-between items-center mt-4">
                                                    <span
                                                        className={`${
                                                            isActive
                                                                ? 'text-white/70'
                                                                : 'text-metallic-gray'
                                                        } text-sm`}
                                                    >
                                                        {wallet.expiry || '09/30'}
                                                    </span>

                                                    <img
                                                        src={mastercardIcon}
                                                        alt="MASTERCARD"
                                                        className={`h-8 ${
                                                            isActive ? 'opacity-90' : 'opacity-70'
                                                        }`}
                                                    />
                                                </div>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </div>

                    {/* Budget vs Expense (moves up under wallet automatically) */}
                    <BudgetGauge
                        budget={budgetData.budget}
                        spent={budgetData.spent}
                        monthLabel={budgetMonthLabel}
                        currencySymbol={currencySymbol}
                    />
                </div>
            </div>

            <WalletDetailDialog />
        </div>
    );
};

export default DashboardContent;
