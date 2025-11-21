import React, { useEffect, useMemo, useState } from 'react';
import AppLayout from '../components/layout/AppLayout';
import DualLineChart from '../components/charts/DualLineChart';
import { apiService } from '../services/api';

const formatDateLabel = (dateStr) => {
    const d = new Date(dateStr);
    if (Number.isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
};

const TransactionsPage = () => {
    const [transactions, setTransactions] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [filterType, setFilterType] = useState('all');
    const [range, setRange] = useState('7d');
    const [error, setError] = useState(null);

    useEffect(() => {
        const load = async () => {
            try {
                setIsLoading(true);
                setError(null);
                const data = await apiService.getTransactions();
                setTransactions(data || []);
            } catch (err) {
                console.error('Error loading transactions page:', err);
                setError(
                    err?.message || 'Failed to load transactions. Please try again.'
                );
            } finally {
                setIsLoading(false);
            }
        };
        load();
    }, []);

    const filteredTransactions = useMemo(() => {
        if (filterType === 'all') return transactions;
        return transactions.filter((t) => t.type === filterType);
    }, [transactions, filterType]);

    const chartData = useMemo(() => {
        const now = new Date();
        let days = 7;
        if (range === '30d') days = 30;
        if (range === '90d') days = 90;

        const start = new Date(now);
        start.setDate(now.getDate() - (days - 1));

        const map = new Map();

        const addToMap = (dateStr, type, amount) => {
            const d = new Date(dateStr);
            if (Number.isNaN(d.getTime())) return;
            if (d < start || d > now) return;

            const key = d.toISOString().split('T')[0];
            if (!map.has(key)) {
                map.set(key, {
                    date: key,
                    income: 0,
                    expense: 0,
                });
            }
            const entry = map.get(key);
            if (type === 'income') {
                entry.income += Number(amount || 0);
            } else if (type === 'expense') {
                entry.expense += Number(amount || 0);
            }
        };

        filteredTransactions.forEach((t) =>
            addToMap(t.transaction_date || t.created_at, t.type, t.amount)
        );

        const result = [];
        const cursor = new Date(start);
        while (cursor <= now) {
            const key = cursor.toISOString().split('T')[0];
            const entry = map.get(key) || {
                date: key,
                income: 0,
                expense: 0,
            };
            result.push({
                label: formatDateLabel(key),
                income: entry.income,
                expense: entry.expense,
            });
            cursor.setDate(cursor.getDate() + 1);
        }

        return result;
    }, [filteredTransactions, range]);

    return (
        <AppLayout activeItem="transactions">
            {isLoading ? (
                <div className="flex items-center justify-center h-full">
                    <div className="text-center">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue mx-auto"></div>
                        <p className="mt-4 text-text">Loading transactions...</p>
                    </div>
                </div>
            ) : error ? (
                <div className="flex items-center justify-center h-full">
                    <p className="text-red-500">{error}</p>
                </div>
            ) : (
                <div className="max-w-7xl mx-auto space-y-8">
                    <div className="flex items-center justify-between">
                        <h1 className="text-2xl font-bold text-text">Transactions</h1>

                        <div className="flex items-center space-x-4">
                            <select
                                className="border border-strokes rounded-md px-3 py-2 text-sm"
                                value={filterType}
                                onChange={(e) => setFilterType(e.target.value)}
                            >
                                <option value="all">All</option>
                                <option value="income">Income</option>
                                <option value="expense">Expense</option>
                            </select>

                            <select
                                className="border border-strokes rounded-md px-3 py-2 text-sm"
                                value={range}
                                onChange={(e) => setRange(e.target.value)}
                            >
                                <option value="7d">Last 7 days</option>
                                <option value="30d">Last 30 days</option>
                                <option value="90d">Last 90 days</option>
                            </select>
                        </div>
                    </div>

                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        <h2 className="text-lg font-semibold text-text mb-4">
                            Income vs Expense
                        </h2>
                        <DualLineChart data={chartData} />
                    </div>

                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        <h2 className="text-lg font-semibold text-text mb-4">
                            Recent Transactions
                        </h2>

                        {filteredTransactions.length === 0 ? (
                            <p className="text-metallic-gray text-sm">
                                No transactions found for the selected filters.
                            </p>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="min-w-full text-sm">
                                    <thead>
                                    <tr className="text-left text-metallic-gray border-b border-strokes">
                                        <th className="py-2 pr-4">Date</th>
                                        <th className="py-2 pr-4">Description</th>
                                        <th className="py-2 pr-4">Type</th>
                                        <th className="py-2 pr-4 text-right">Amount</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {filteredTransactions.map((t) => (
                                        <tr
                                            key={t.id}
                                            className="border-b border-strokes last:border-b-0"
                                        >
                                            <td className="py-2 pr-4">
                                                {formatDateLabel(
                                                    t.transaction_date || t.created_at
                                                )}
                                            </td>
                                            <td className="py-2 pr-4">
                                                {t.description || '-'}
                                            </td>
                                            <td className="py-2 pr-4 capitalize">
                                                {t.type}
                                            </td>
                                            <td className="py-2 pr-4 text-right">
                                                {Number(t.amount || 0).toFixed(2)}
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </AppLayout>
    );
};

export default TransactionsPage;
