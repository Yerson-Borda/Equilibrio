import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

import balanceIcon from '../../assets/icons/balance-icon.png';
import spendingIcon from '../../assets/icons/spending-icon.png';
import savedIcon from '../../assets/icons/saved-icon.png';
import mastercardIcon from '../../assets/icons/mastercard-icon.png';
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';

import DualLineChart from '../charts/DualLineChart';
import BudgetSetup from '../budget/BudgetSetup';
import BudgetProgress from '../budget/BudgetProgress';
import Card from '../ui/Card';
import Button from '../ui/Button';
import { apiService } from '../../services/api';
import { formatCurrency } from '../../config/currencies';

const DashboardContent = ({ wallets = [], userStats, onCreateWallet }) => {
    const navigate = useNavigate();

    const [chartRange, setChartRange] = useState('7'); // days: 7, 30, 90
    const [chartLoading, setChartLoading] = useState(false);
    const [chartError, setChartError] = useState(null);
    const [chartData, setChartData] = useState([]);

    const [budgetState, setBudgetState] = useState({
        budget: 0,
        spent: 0,
        month: new Date().toISOString().slice(0, 7),
    });

    useEffect(() => {
        loadChartData(chartRange);
    }, [chartRange]);

    const handleViewAllWallets = () => {
        navigate('/wallets');
    };

    // Build daily income/expense series for last N days
    const buildIncomeExpenseSeries = (transactions, days) => {
        const now = new Date();
        const start = new Date(now);
        start.setDate(now.getDate() - (days - 1));

        const dateMap = new Map();

        // Initialize days
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

        (transactions || []).forEach((tx) => {
            const dateStr = (tx.transaction_date || tx.created_at || '').split('T')[0];
            if (!dateStr || !dateMap.has(dateStr)) return;

            const entry = dateMap.get(dateStr);
            const amount = Number(tx.amount) || 0;

            if (tx.type === 'income') {
                entry.income += amount;
            } else if (tx.type === 'expense') {
                entry.expense += amount;
            }
        });

        return Array.from(dateMap.values());
    };

    const loadChartData = async (range) => {
        try {
            setChartLoading(true);
            setChartError(null);

            const days = parseInt(range, 10) || 7;
            const transactions = await apiService.getTransactions();
            const series = buildIncomeExpenseSeries(transactions || [], days);

            setChartData(series);
        } catch (err) {
            console.error('Error loading dashboard chart data:', err);
            setChartError('Failed to load chart data.');
        } finally {
            setChartLoading(false);
        }
    };

    // Called by BudgetSetup when a budget is set/updated
    const handleBudgetUpdate = ({ amount, spent, month }) => {
        setBudgetState({
            budget: Number(amount) || 0,
            spent: Number(spent) || 0,
            month,
        });
    };

    const totalWallets = wallets.length;
    const defaultCurrency = userStats?.defaultCurrency || 'USD';

    const topWallet = wallets[0];

    return (
        <div className="space-y-8">
            {/* Top stats row */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <Card>
                    <div className="flex items-center">
                        <div className="w-10 h-10 rounded-full bg-blue-50 flex items-center justify-center mr-3">
                            <img src={balanceIcon} alt="Total Balance" className="w-5 h-5" />
                        </div>
                        <div>
                            <p className="text-xs text-metallic-gray">Total Balance</p>
                            <p className="text-lg font-semibold text-text">
                                {formatCurrency(
                                    userStats?.totalBalance || 0,
                                    defaultCurrency
                                )}
                            </p>
                        </div>
                    </div>
                </Card>

                <Card>
                    <div className="flex items-center">
                        <div className="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center mr-3">
                            <img src={spendingIcon} alt="Total Spending" className="w-5 h-5" />
                        </div>
                        <div>
                            <p className="text-xs text-metallic-gray">Total Spending</p>
                            <p className="text-lg font-semibold text-text">
                                {formatCurrency(
                                    userStats?.totalSpending || 0,
                                    defaultCurrency
                                )}
                            </p>
                        </div>
                    </div>
                </Card>

                <Card>
                    <div className="flex items-center">
                        <div className="w-10 h-10 rounded-full bg-green-50 flex items-center justify-center mr-3">
                            <img src={savedIcon} alt="Total Saved" className="w-5 h-5" />
                        </div>
                        <div>
                            <p className="text-xs text-metallic-gray">Total Saved</p>
                            <p className="text-lg font-semibold text-text">
                                {formatCurrency(
                                    userStats?.totalSaved || 0,
                                    defaultCurrency
                                )}
                            </p>
                        </div>
                    </div>
                </Card>
            </div>

            {/* Middle row: chart + featured wallet */}
            <div className="grid grid-cols-1 xl:grid-cols-[2fr,1.2fr] gap-6">
                {/* Chart card */}
                <Card
                    title="Income vs Expense"
                    headerRight={
                        <select
                            className="border border-strokes rounded-md px-3 py-1 text-xs"
                            value={chartRange}
                            onChange={(e) => setChartRange(e.target.value)}
                        >
                            <option value="7">Last 7 days</option>
                            <option value="30">Last 30 days</option>
                            <option value="90">Last 90 days</option>
                        </select>
                    }
                >
                    {chartLoading ? (
                        <div className="flex items-center justify-center h-64">
                            <div className="text-center">
                                <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue mx-auto" />
                                <p className="mt-3 text-sm text-text">Loading chart...</p>
                            </div>
                        </div>
                    ) : chartError ? (
                        <div className="flex items-center justify-center h-64">
                            <p className="text-sm text-red-500">{chartError}</p>
                        </div>
                    ) : chartData.length === 0 ? (
                        <div className="flex items-center justify-center h-64">
                            <p className="text-sm text-metallic-gray">
                                No transactions yet. Add some income or expenses to see the
                                chart.
                            </p>
                        </div>
                    ) : (
                        <DualLineChart
                            data={chartData}
                            title={null}
                            xKey="label"
                            leftKey="income"
                            rightKey="expense"
                        />
                    )}
                </Card>

                {/* Featured wallet / quick access */}
                <Card
                    title="Primary Wallet"
                    headerRight={
                        totalWallets > 0 && (
                            <Button
                                variant="outline"
                                className="text-xs"
                                onClick={handleViewAllWallets}
                            >
                                View all
                            </Button>
                        )
                    }
                >
                    {totalWallets === 0 ? (
                        <div className="flex flex-col items-center justify-center py-10">
                            <p className="text-sm text-metallic-gray mb-4 text-center">
                                You don&apos;t have any wallets yet. Create one to start
                                tracking your money.
                            </p>
                            <Button variant="primary" onClick={onCreateWallet}>
                                + Create Wallet
                            </Button>
                        </div>
                    ) : (
                        <div className="space-y-4">
                            {/* Card preview */}
                            <div
                                className="rounded-2xl text-white shadow-lg relative overflow-hidden"
                                style={{
                                    background: `linear-gradient(to right, ${
                                        topWallet.color || '#4361ee'
                                    }, ${topWallet.color || '#4361ee'})`,
                                }}
                            >
                                <div className="p-5 h-full flex flex-col justify-between">
                                    <div className="flex justify-between items-center mb-4">
                                        <img src={chipIcon} alt="Chip" className="h-8" />
                                        <img
                                            src={nfcIcon}
                                            alt="NFC"
                                            className="h-6 opacity-80"
                                        />
                                    </div>

                                    <div className="mb-4">
                                        <p className="text-xs uppercase opacity-80 mb-1">
                                            {topWallet.wallet_type?.replace('_', ' ') || 'Wallet'}
                                        </p>
                                        <p className="text-xl font-semibold">
                                            {topWallet.name || 'Wallet Name'}
                                        </p>
                                    </div>

                                    <div className="flex justify-between items-center">
                                        <div>
                                            <p className="text-xs uppercase opacity-80 mb-1">
                                                Balance
                                            </p>
                                            <p className="text-lg font-bold">
                                                {Number(topWallet.balance || 0).toFixed(2)}{' '}
                                                {topWallet.currency}
                                            </p>
                                        </div>
                                        <img
                                            src={mastercardIcon}
                                            alt="Brand"
                                            className="h-8"
                                        />
                                    </div>
                                </div>
                            </div>

                            <div className="flex justify-between items-center">
                                <div className="text-sm text-metallic-gray">
                                    <p>
                                        Total wallets:{' '}
                                        <span className="font-semibold text-text">
                                            {totalWallets}
                                        </span>
                                    </p>
                                </div>
                                <Button
                                    variant="outline"
                                    className="text-xs"
                                    onClick={onCreateWallet}
                                >
                                    + New wallet
                                </Button>
                            </div>
                        </div>
                    )}
                </Card>
            </div>

            {/* Bottom row: budget components */}
            <div className="grid grid-cols-1 xl:grid-cols-[1.3fr,1.5fr] gap-6">
                <Card title="Budget Progress">
                    <BudgetProgress
                        budget={budgetState.budget}
                        spent={budgetState.spent}
                        month={budgetState.month}
                    />
                </Card>

                <Card title="Budget Setup">
                    <BudgetSetup onBudgetUpdate={handleBudgetUpdate} />
                </Card>
            </div>
        </div>
    );
};

export default DashboardContent;
