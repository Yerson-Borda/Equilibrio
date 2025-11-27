import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

// Stat icons
import balanceIcon from '../../assets/icons/balance-icon.png';
import spendingIcon from '../../assets/icons/spending-icon.png';
import savedIcon from '../../assets/icons/saved-icon.png';

// Card icons
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';
import mastercardIcon from '../../assets/icons/mastercard-icon.png';

import DualLineChart from '../charts/DualLineChart.jsx';
import BudgetGauge from '../budget/BudgetGauge.jsx';

import { apiService } from '../../services/api';
import { syncService } from '../../services/syncService';
import { getCurrencySymbol } from '../../config/currencies';

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
    const [selectedWallet, setSelectedWallet] = useState(null);
    const [isWalletDialogOpen, setIsWalletDialogOpen] = useState(false);

    const currencySymbol = getCurrencySymbol(
        userStats?.defaultCurrency || 'USD'
    );

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
            if (t.type === 'income') {
                entry.income += amount;
            } else if (t.type === 'expense') {
                entry.expense += amount;
            }
        });

        return Array.from(dateMap.values());
    };

    const loadChartData = async (range) => {
        try {
            let days = 7;
            if (range === '30') days = 30;
            if (range === '90') days = 90;

            const transactions = await apiService.getTransactions();
            const spendingTrends = buildIncomeExpenseSeries(
                transactions,
                days
            );

            setChartData({ spendingTrends });
        } catch (error) {
            console.error('Error loading chart data:', error);
        }
    };

    const loadBudgetData = async () => {
        try {
            const budget = await apiService.getCurrentBudget();
            if (budget) {
                const monthStr = `${budget.year}-${String(budget.month).padStart(
                    2,
                    '0'
                )}`;
                setBudgetData({
                    budget: parseNumber(budget.monthly_limit),
                    spent: parseNumber(budget.monthly_spent),
                    month: monthStr,
                });
            } else {
                const today = new Date();
                const monthStr = today.toISOString().slice(0, 7);
                setBudgetData({
                    budget: 0,
                    spent: 0,
                    month: monthStr,
                });
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
                .sort(
                    (a, b) =>
                        new Date(b.transaction_date) -
                        new Date(a.transaction_date)
                );
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
            .catch((e) =>
                console.error('Error refreshing wallets on sync:', e)
            );
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
    const handleViewAllTransactions = () => {
        navigate('/transactions');
    };

    const handleViewAllWallets = () => {
        navigate('/my-wallets');
    };

    const formatCardNumber = (number) => {
        if (!number) return '**** **** **** ****';
        const cleaned = String(number).replace(/\D/g, '').slice(0, 16);
        const chunks = cleaned.match(/.{1,4}/g);
        return chunks ? chunks.join(' ') : cleaned;
    };

    // stack animation like MyWallets
    const visibleWallets = dashboardWallets.slice(0, 3);
    const stackHeight = visibleWallets.length
        ? 260 + (visibleWallets.length - 1) * 40
        : 160;

    const getStackStyles = (index) => {
        const offset = index - selectedWalletIndex;
        const translateY = offset * 26;
        const scale = 1 - Math.abs(offset) * 0.04;
        const zIndex = visibleWallets.length - Math.abs(offset);

        return {
            transform: `translateY(${translateY}px) scale(${scale})`,
            zIndex,
        };
    };

    const handleWalletCardClick = (index, wallet) => {
        // only one wallet -> open directly
        if (visibleWallets.length === 1) {
            setSelectedWalletIndex(0);
            setSelectedWallet(wallet);
            setIsWalletDialogOpen(true);
            return;
        }

        // click different wallet: bring to front
        if (index !== selectedWalletIndex) {
            setSelectedWalletIndex(index);
            setSelectedWallet(wallet);
            setIsWalletDialogOpen(false);
            return;
        }

        // click same (front) wallet again: open dialog
        if (!isWalletDialogOpen) {
            setSelectedWallet(wallet);
            setIsWalletDialogOpen(true);
        }
    };

    const handleAddTransactionFromDialog = () => {
        if (!selectedWallet) return;
        navigate('/transactions', {
            state: { fromDashboard: true, selectedWallet },
        });
    };

    // ------- Wallet detail dialog (card + white details) -------
    const WalletDetailDialog = () => {
        if (!isWalletDialogOpen || !selectedWallet) return null;

        const walletSymbol = getCurrencySymbol(selectedWallet.currency);
        const balance = parseNumber(selectedWallet.balance || 0);
        const positiveChange = 23.65; // placeholder %
        const negativeChange = 10.4;

        return (
            <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
                <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full overflow-hidden">
                    {/* TOP CARD: same structure as MyWallets layout */}
                    <div className="bg-gradient-to-r from-[#1E1E2F] via-[#252B3F] to-[#1E1E2F] text-white p-6">
                        <div className="flex justify-between items-start mb-4">
                            <p className="text-sm font-semibold">
                                {selectedWallet.name || 'Wallet'}
                            </p>
                            <button
                                onClick={() => setIsWalletDialogOpen(false)}
                                className="text-gray-300 hover:text-white text-lg leading-none"
                            >
                                ✕
                            </button>
                        </div>

                        {/* chip + total balance + NFC + brand */}
                        <div className="flex justify-between items-center mb-6">
                            <div className="flex items-center space-x-3">
                                <img
                                    src={chipIcon}
                                    alt="Chip"
                                    className="h-6"
                                />
                                <div>
                                    <p className="text-xs opacity-80 mb-1">
                                        Total Balance
                                    </p>
                                    <p className="text-3xl font-bold">
                                        {walletSymbol}
                                        {balance.toLocaleString(undefined, {
                                            minimumFractionDigits: 2,
                                            maximumFractionDigits: 2,
                                        })}
                                    </p>
                                </div>
                            </div>
                            <div className="flex flex-col items-end space-y-2">
                                <img
                                    src={nfcIcon}
                                    alt="NFC"
                                    className="h-5 opacity-80"
                                />
                                <img
                                    src={mastercardIcon}
                                    alt="Mastercard"
                                    className="h-7"
                                />
                            </div>
                        </div>

                        {/* card number + expiry ON THE LEFT (stacked) */}
                        <div className="flex flex-col items-start text-xs opacity-80 mt-2">
                            <span className="tracking-widest font-mono text-sm mb-1">
                                {formatCardNumber(selectedWallet.card_number)}
                            </span>
                            <span>{selectedWallet.expiry_date || '09/30'}</span>
                        </div>
                    </div>

                    {/* WHITE SECTION (Your Balance, Currency, Status, Add Tx) */}
                    <div className="bg-white p-6">
                        {/* Your Balance row */}
                        <div className="mb-4">
                            <p className="text-sm text-metallic-gray mb-1">
                                Your Balance
                            </p>
                            <div className="flex items-center justify-between">
                                <p className="text-2xl font-semibold text-text">
                                    {walletSymbol}
                                    {balance.toLocaleString(undefined, {
                                        minimumFractionDigits: 2,
                                        maximumFractionDigits: 2,
                                    })}
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

                        {/* Currency + Status row */}
                        <div className="grid grid-cols-2 gap-4 mb-4 text-sm">
                            <div>
                                <p className="text-metallic-gray mb-1">
                                    Currency
                                </p>
                                <p className="font-medium text-text">
                                    {getCurrencyDisplay(selectedWallet.currency)}
                                </p>
                            </div>
                            <div>
                                <p className="text-metallic-gray mb-1">
                                    Status
                                </p>
                                <p className="font-medium text-text">Active</p>
                            </div>
                        </div>

                        <hr className="border-strokes mb-2" />

                        {/* Add transaction link (centered green text) */}
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

    // ----- render -----
    return (
        <div className="space-y-6">
            {/* top stat cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {/* Total balance */}
                <div className="bg-[#111827] rounded-xl shadow-sm p-5 flex items-center space-x-4">
                    <div className="w-10 h-10 rounded-full bg-[#1F2937] flex items-center justify-center">
                        <img
                            src={balanceIcon}
                            alt="Balance"
                            className="w-5 h-5 invert"
                        />
                    </div>
                    <div>
                        <p className="text-sm text-gray-300">Total balance</p>
                        <p className="text-2xl font-bold text-white">
                            {currencySymbol}
                            {Number(userStats?.totalBalance || 0).toFixed(2)}
                        </p>
                    </div>
                </div>

                {/* spending */}
                <div className="bg-white rounded-xl shadow-sm border border-strokes p-5 flex items-center space-x-4">
                    <div className="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center">
                        <img
                            src={spendingIcon}
                            alt="Spending"
                            className="w-5 h-5"
                        />
                    </div>
                    <div>
                        <p className="text-sm text-metallic-gray">
                            Total spending
                        </p>
                        <p className="text-2xl font-bold text-text">
                            {currencySymbol}
                            {Number(userStats?.totalSpending || 0).toFixed(2)}
                        </p>
                    </div>
                </div>

                {/* saved */}
                <div className="bg-white rounded-xl shadow-sm border border-strokes p-5 flex items-center space-x-4">
                    <div className="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center">
                        <img src={savedIcon} alt="Saved" className="w-5 h-5" />
                    </div>
                    <div>
                        <p className="text-sm text-metallic-gray">Total saved</p>
                        <p className="text-2xl font-bold text-text">
                            {currencySymbol}
                            {Number(userStats?.totalSaved || 0).toFixed(2)}
                        </p>
                    </div>
                </div>
            </div>

            {/* main grid */}
            <div className="grid grid-cols-1 xl:grid-cols-[2.1fr,1.2fr] gap-6">
                {/* left column: chart + recent tx */}
                <div className="space-y-6">
                    {/* Working Capital */}
                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        <div className="flex justify-between items-center mb-4">
                            <div>
                                <h2 className="text-lg font-semibold text-text">
                                    Working Capital
                                </h2>
                            </div>
                            <div className="flex items-center space-x-6">
                                <div className="flex items-center space-x-3 text-xs">
                                    <div className="flex items-center space-x-1">
                                        <span className="inline-block w-3 h-3 rounded-full bg-[#6F5BFF]" />
                                        <span className="text-metallic-gray">
                                            Income
                                        </span>
                                    </div>
                                    <div className="flex items-center space-x-1">
                                        <span className="inline-block w-3 h-3 rounded-full bg-[#FFC75A]" />
                                        <span className="text-metallic-gray">
                                            Expenses
                                        </span>
                                    </div>
                                </div>

                                <div className="relative">
                                    <select
                                        value={selectedRange}
                                        onChange={(e) =>
                                            setSelectedRange(e.target.value)
                                        }
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

                    {/* Recent transactions */}
                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-lg font-semibold text-text">
                                Recent Transaction
                            </h2>
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
                                    <th className="py-2 pr-4">
                                        NAME/BANK-CARD
                                    </th>
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
                                                        {t.description ||
                                                            t.note ||
                                                            'Transaction'}
                                                    </p>
                                                    <p className="text-xs text-metallic-gray">
                                                        {t.wallet_name ||
                                                            'Wallet'}
                                                    </p>
                                                </div>
                                            </td>
                                            <td className="py-3 pr-4 text-sm text-metallic-gray capitalize">
                                                {t.type}
                                            </td>
                                            <td className="py-3 pr-4 text-sm">
                                                    <span
                                                        className={
                                                            t.type ===
                                                            'income'
                                                                ? 'text-green-600'
                                                                : 'text-red-600'
                                                        }
                                                    >
                                                        {t.type === 'income'
                                                            ? '+'
                                                            : '-'}
                                                        {currencySymbol}
                                                        {parseNumber(
                                                            t.amount || 0
                                                        ).toFixed(2)}
                                                    </span>
                                            </td>
                                            <td className="py-3 pr-4 text-sm text-metallic-gray">
                                                {t.transaction_date
                                                    ? new Date(
                                                        t.transaction_date
                                                    ).toLocaleDateString(
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

                {/* right column: wallet + budget */}
                <div className="space-y-6">
                    {/* Wallet stack */}
                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-lg font-semibold text-text">
                                Wallet
                            </h2>
                            <button
                                onClick={handleViewAllWallets}
                                className="text-xs text-metallic-gray hover:text-text"
                            >
                                •••
                            </button>
                        </div>

                        {visibleWallets.length === 0 ? (
                            <div className="text-sm text-metallic-gray py-4">
                                No wallets yet. Create a wallet to see it here.
                            </div>
                        ) : (
                            <div
                                className="relative"
                                style={{ height: stackHeight }}
                            >
                                {visibleWallets.map((wallet, index) => {
                                    const styles = getStackStyles(index);
                                    const walletSymbol = getCurrencySymbol(
                                        wallet.currency
                                    );
                                    const isSelected =
                                        index === selectedWalletIndex;

                                    return (
                                        <button
                                            key={wallet.id}
                                            type="button"
                                            onClick={() =>
                                                handleWalletCardClick(
                                                    index,
                                                    wallet
                                                )
                                            }
                                            className={`absolute left-0 right-0 rounded-2xl text-left text-white transition-all duration-300 ${
                                                isSelected
                                                    ? 'shadow-2xl'
                                                    : 'shadow-md opacity-90'
                                            }`}
                                            style={styles}
                                        >
                                            {/* CARD LAYOUT matching MyWallets */}
                                            <div className="rounded-2xl p-6 h-56 bg-gradient-to-r from-[#1E1E2F] via-[#252B3F] to-[#1E1E2F] flex flex-col justify-between">
                                                {/* name */}
                                                <div className="flex justify-between items-start mb-4">
                                                    <p className="text-sm font-semibold">
                                                        {wallet.name || 'Wallet'}
                                                    </p>
                                                </div>

                                                {/* chip + total balance + NFC/brand */}
                                                <div className="flex justify-between items-center">
                                                    <div className="flex items-center space-x-3">
                                                        <img
                                                            src={chipIcon}
                                                            alt="Chip"
                                                            className="h-6"
                                                        />
                                                        <div>
                                                            <p className="text-xs opacity-80 mb-1">
                                                                Total Balance
                                                            </p>
                                                            <p className="text-2xl font-bold">
                                                                {walletSymbol}
                                                                {parseNumber(
                                                                    wallet.balance ||
                                                                    0
                                                                ).toLocaleString(
                                                                    undefined,
                                                                    {
                                                                        minimumFractionDigits: 2,
                                                                        maximumFractionDigits: 2,
                                                                    }
                                                                )}
                                                            </p>
                                                        </div>
                                                    </div>
                                                    <div className="flex flex-col items-end space-y-2">
                                                        <img
                                                            src={nfcIcon}
                                                            alt="NFC"
                                                            className="h-5 opacity-80"
                                                        />
                                                        <img
                                                            src={mastercardIcon}
                                                            alt="Mastercard"
                                                            className="h-7"
                                                        />
                                                    </div>
                                                </div>

                                                {/* card number + expiry ON THE LEFT (stacked) */}
                                                <div className="flex flex-col items-start text-xs opacity-80 mt-4">
                                                    <span className="tracking-widest font-mono text-sm mb-1">
                                                        {formatCardNumber(
                                                            wallet.card_number
                                                        )}
                                                    </span>
                                                    <span>
                                                        {wallet.expiry_date ||
                                                            '09/30'}
                                                    </span>
                                                </div>
                                            </div>
                                        </button>
                                    );
                                })}
                            </div>
                        )}
                    </div>

                    {/* Budget vs Expense */}
                    <BudgetGauge
                        budget={budgetData.budget}
                        spent={budgetData.spent}
                        monthLabel={
                            budgetData.month
                                ? new Date(
                                    `${budgetData.month}-01`
                                ).toLocaleDateString('en-US', {
                                    month: 'long',
                                })
                                : undefined
                        }
                        currencySymbol={currencySymbol}
                    />
                </div>
            </div>

            <WalletDetailDialog />
        </div>
    );
};

export default DashboardContent;
