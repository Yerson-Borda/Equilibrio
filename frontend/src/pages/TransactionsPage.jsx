import React, { useEffect, useMemo, useState } from 'react';
import AppLayout from '../components/layout/AppLayout';
import DualLineChart from '../components/charts/DualLineChart';
import MonthlyBarChart from '../components/charts/MonthlyBarChart';
import { apiService } from '../services/api';

// ✅ embedded widgets shown INSIDE the carousel box
import Top3CategoriesWidget from '../components/charts/Top3CategoriesWidget';
import AverageSpendingWidget from '../components/charts/AverageSpendingWidget';

const formatDateLabel = (dateStr) => {
    const d = new Date(dateStr);
    if (Number.isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
};

const todayISO = () => new Date().toISOString().split('T')[0];
const sevenDaysAgoISO = () => {
    const d = new Date();
    d.setDate(d.getDate() - 6);
    return d.toISOString().split('T')[0];
};

const TransactionsPage = () => {
    const [transactions, setTransactions] = useState([]);
    const [categories, setCategories] = useState([]);

    const [isLoading, setIsLoading] = useState(true);
    const [filterType, setFilterType] = useState('all'); // all | income | expense
    const [range, setRange] = useState('365d'); // 7d | 30d | 90d | 365d (Year)
    const [error, setError] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');

    // ✅ carousel slides (now includes average)
    // 0 monthly bar, 1 daily dual line, 2 expense breakdown (top3), 3 average spending
    const [chartIndex, setChartIndex] = useState(0);
    const chartViews = ['monthly', 'daily', 'expense_breakdown', 'average_spending'];

    // monthly bar series
    const [barSeriesType, setBarSeriesType] = useState('expense'); // income | expense

    // daily date range
    const [fromDate, setFromDate] = useState(sevenDaysAgoISO());
    const [toDate, setToDate] = useState(todayISO());

    // ✅ API data for embedded slides
    const [topCategories, setTopCategories] = useState([]); // top 3
    const [avgPeriod, setAvgPeriod] = useState('year'); // day | month | year
    const [avgSpending, setAvgSpending] = useState([]);

    const currentChartType = chartViews[chartIndex];

    const handlePrevChart = () => {
        setChartIndex((prev) => (prev - 1 + chartViews.length) % chartViews.length);
    };
    const handleNextChart = () => {
        setChartIndex((prev) => (prev + 1) % chartViews.length);
    };

    const categoryMap = useMemo(() => {
        const m = {};
        (categories || []).forEach((c) => {
            if (c?.id != null) m[c.id] = c.name;
        });
        return m;
    }, [categories]);

    const parseISO = (value) => {
        if (!value) return null;
        const d = new Date(value);
        return Number.isNaN(d.getTime()) ? null : d;
    };

    // Filtered + searched transactions
    const filteredTransactions = useMemo(() => {
        let base = transactions;
        if (filterType !== 'all') {
            base = base.filter((t) => t.type === filterType);
        }
        if (!searchQuery) return base;

        const q = searchQuery.toLowerCase();
        return base.filter((t) => {
            const desc = (t.description || '').toLowerCase();
            const catName =
                categoryMap[t.category_id] ||
                (t.category?.name || t.category_name || t.category || '');
            return (
                desc.includes(q) ||
                (catName && catName.toLowerCase().includes(q))
            );
        });
    }, [transactions, filterType, searchQuery, categoryMap]);

    const getDaysForRange = (r) => {
        switch (r) {
            case '7d':
                return 7;
            case '30d':
                return 30;
            case '90d':
                return 90;
            case '365d':
            default:
                return 365;
        }
    };

    // DAILY dual-line chart data
    const dailyChartData = useMemo(() => {
        let start = parseISO(fromDate);
        let end = parseISO(toDate);

        if (!start || !end || start > end) {
            const allDates = filteredTransactions
                .map((t) => parseISO(t.transaction_date || t.created_at))
                .filter(Boolean);
            if (!allDates.length) return [];
            start = new Date(Math.min(...allDates.map((d) => d.getTime())));
            end = new Date(Math.max(...allDates.map((d) => d.getTime())));
        }

        const map = new Map();

        const addToMap = (dateStr, type, amount) => {
            const d = new Date(dateStr);
            if (Number.isNaN(d.getTime())) return;
            if (d < start || d > end) return;

            const key = d.toISOString().split('T')[0];
            if (!map.has(key)) {
                map.set(key, { date: key, income: 0, expense: 0 });
            }
            const entry = map.get(key);
            if (type === 'income') entry.income += Number(amount || 0);
            if (type === 'expense') entry.expense += Number(amount || 0);
        };

        filteredTransactions.forEach((t) =>
            addToMap(t.transaction_date || t.created_at, t.type, t.amount)
        );

        const result = [];
        const cursor = new Date(start);
        while (cursor <= end) {
            const key = cursor.toISOString().split('T')[0];
            const entry = map.get(key) || { date: key, income: 0, expense: 0 };
            result.push({
                label: formatDateLabel(key),
                income: entry.income,
                expense: entry.expense,
            });
            cursor.setDate(cursor.getDate() + 1);
        }

        return result;
    }, [filteredTransactions, fromDate, toDate]);

    // MONTHLY bar data
    const monthlyChartData = useMemo(() => {
        const days = getDaysForRange(range);
        const now = new Date();
        const start = new Date(now);
        start.setDate(now.getDate() - (days - 1));

        const months = Array.from({ length: 12 }).map((_, idx) => ({
            monthIndex: idx,
            label: new Date(2024, idx, 1).toLocaleDateString(undefined, {
                month: 'short',
            }),
            income: 0,
            expense: 0,
        }));

        filteredTransactions.forEach((t) => {
            const dateStr = t.transaction_date || t.created_at;
            const d = new Date(dateStr);
            if (Number.isNaN(d.getTime())) return;
            if (d < start || d > now) return;

            const m = d.getMonth();
            if (m < 0 || m > 11) return;

            const amt = Number(t.amount || 0);
            if (t.type === 'income') months[m].income += amt;
            if (t.type === 'expense') months[m].expense += amt;
        });

        return months;
    }, [filteredTransactions, range]);

    // ✅ Load base page + top3 once
    useEffect(() => {
        const load = async () => {
            try {
                setIsLoading(true);
                setError(null);

                const [txData, cats, top3] = await Promise.all([
                    apiService.getTransactions(),
                    apiService.getCategories(),
                    apiService.getTopCategoriesCurrentMonth(), // ✅ TOP 3 for breakdown slide
                ]);

                const txList = txData || [];
                setTransactions(txList);
                setCategories(cats || []);
                setTopCategories(Array.isArray(top3) ? top3 : []);

                // good default date range for daily chart
                if (txList.length > 0) {
                    let minDate = null;
                    let maxDate = null;

                    txList.forEach((t) => {
                        const raw = t.transaction_date || t.created_at;
                        const d = new Date(raw);
                        if (Number.isNaN(d.getTime())) return;
                        if (!minDate || d < minDate) minDate = d;
                        if (!maxDate || d > maxDate) maxDate = d;
                    });

                    if (minDate && maxDate) {
                        setFromDate(minDate.toISOString().split('T')[0]);
                        setToDate(maxDate.toISOString().split('T')[0]);
                    }
                }
            } catch (err) {
                console.error('Error loading transactions page:', err);
                setError(err?.message || 'Failed to load transactions. Please try again.');
            } finally {
                setIsLoading(false);
            }
        };
        load();
    }, []);

    // ✅ Load avg spending (when period changes OR when user reaches avg slide)
    useEffect(() => {
        const loadAvg = async () => {
            try {
                const data = await apiService.getAverageSpending(avgPeriod);
                setAvgSpending(Array.isArray(data) ? data : []);
            } catch (e) {
                console.error('Error loading avg spending:', e);
                setAvgSpending([]);
            }
        };

        // load always on period change; also ensures data ready when slide opens
        loadAvg();
    }, [avgPeriod]);

    const chartTitle =
        currentChartType === 'monthly'
            ? 'Transactions Overview'
            : currentChartType === 'daily'
                ? 'Income vs Expense'
                : currentChartType === 'expense_breakdown'
                    ? 'Expense Distribution'
                    : 'Average spending per category';

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
                    {/* HEADER + SEARCH + FILTER TABS */}
                    <div className="space-y-4">
                        <div className="flex items-center justify-between">
                            <h1 className="text-2xl font-bold text-text">Transactions</h1>
                            <div className="flex items-center space-x-4 text-metallic-gray">
                                <button className="p-2 rounded-full hover:bg-gray-100">🔍</button>
                                <button className="p-2 rounded-full hover:bg-gray-100">🔔</button>
                            </div>
                        </div>

                        {/* Search bar */}
                        <div className="w-full">
                            <div className="relative">
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-metallic-gray">
                  🔍
                </span>
                                <input
                                    type="text"
                                    placeholder="Search anything on Transactions"
                                    className="w-full pl-9 pr-3 py-2.5 rounded-xl border border-strokes text-sm focus:outline-none focus:ring-2 focus:ring-blue/40"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                />
                            </div>
                        </div>

                        {/* All / Income / Expenses tabs */}
                        <div className="flex items-center space-x-6 text-sm">
                            {['all', 'income', 'expense'].map((t) => (
                                <button
                                    key={t}
                                    onClick={() => setFilterType(t)}
                                    className={`pb-2 border-b-2 ${
                                        filterType === t
                                            ? 'border-blue text-blue font-semibold'
                                            : 'border-transparent text-metallic-gray'
                                    }`}
                                >
                                    {t === 'all' ? 'All' : t === 'income' ? 'Income' : 'Expenses'}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* ✅ MAIN CHART CARD (carousel) */}
                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        {/* Header row with title + filters */}
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="text-lg font-semibold text-text">{chartTitle}</h2>

                            {/* Right side controls by chart type */}
                            {currentChartType === 'monthly' && (
                                <div className="flex items-center space-x-3 text-xs">
                                    <div className="bg-gray-100 rounded-full p-1 flex">
                                        <button
                                            onClick={() => setBarSeriesType('income')}
                                            className={`px-3 py-1 rounded-full ${
                                                barSeriesType === 'income'
                                                    ? 'bg-black text-white'
                                                    : 'text-metallic-gray'
                                            }`}
                                        >
                                            Income
                                        </button>
                                        <button
                                            onClick={() => setBarSeriesType('expense')}
                                            className={`px-3 py-1 rounded-full ${
                                                barSeriesType === 'expense'
                                                    ? 'bg-black text-white'
                                                    : 'text-metallic-gray'
                                            }`}
                                        >
                                            Expenses
                                        </button>
                                    </div>

                                    <select
                                        className="border border-strokes rounded-md px-2 py-1 text-xs"
                                        value={range}
                                        onChange={(e) => setRange(e.target.value)}
                                    >
                                        <option value="7d">Last 7 days</option>
                                        <option value="30d">Last 30 days</option>
                                        <option value="90d">Last 90 days</option>
                                        <option value="365d">Year</option>
                                    </select>
                                </div>
                            )}

                            {currentChartType === 'daily' && (
                                <div className="flex items-center space-x-4 text-xs">
                                    <div className="flex items-center space-x-3">
                                        <div className="flex items-center space-x-1">
                                            <span className="w-2 h-2 rounded-full bg-[#6366F1]" />
                                            <span className="text-metallic-gray">Income</span>
                                        </div>
                                        <div className="flex items-center space-x-1">
                                            <span className="w-2 h-2 rounded-full bg-[#F97316]" />
                                            <span className="text-metallic-gray">Expense</span>
                                        </div>
                                    </div>

                                    <div className="flex items-center space-x-1">
                                        <input
                                            type="date"
                                            value={fromDate}
                                            onChange={(e) => setFromDate(e.target.value)}
                                            className="border border-strokes rounded-md px-2 py-1 text-xs"
                                        />
                                        <span className="text-metallic-gray">to</span>
                                        <input
                                            type="date"
                                            value={toDate}
                                            onChange={(e) => setToDate(e.target.value)}
                                            className="border border-strokes rounded-md px-2 py-1 text-xs"
                                        />
                                    </div>
                                </div>
                            )}

                            {/* ✅ Expense breakdown slide: keep range selector like old distribution */}
                            {currentChartType === 'expense_breakdown' && (
                                <div className="flex items-center space-x-3 text-xs">
                                    <span className="text-metallic-gray">Current month</span>
                                </div>
                            )}

                            {/* ✅ Average spending slide: period selector */}
                            {currentChartType === 'average_spending' && (
                                <div className="flex items-center space-x-3 text-xs">
                                    <select
                                        className="border border-strokes rounded-md px-2 py-1 text-xs"
                                        value={avgPeriod}
                                        onChange={(e) => setAvgPeriod(e.target.value)}
                                    >
                                        <option value="day">Day</option>
                                        <option value="month">Month</option>
                                        <option value="year">Year</option>
                                    </select>
                                </div>
                            )}
                        </div>

                        {/* ✅ Chart area with arrows */}
                        <div className="relative">
                            {/* Left arrow */}
                            <button
                                onClick={handlePrevChart}
                                className="hidden sm:flex absolute left-0 top-1/2 -translate-y-1/2 w-8 h-8 items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 shadow"
                            >
                                ‹
                            </button>

                            <div className="px-0 sm:px-8">
                                {/* Slide 0 */}
                                {currentChartType === 'monthly' && (
                                    <MonthlyBarChart data={monthlyChartData} activeSeries={barSeriesType} />
                                )}

                                {/* Slide 1 */}
                                {currentChartType === 'daily' && (
                                    <DualLineChart data={dailyChartData} />
                                )}

                                {/* Slide 2 ✅ Replace ExpenseDistributionChart with Top3 breakdown inside this box */}
                                {currentChartType === 'expense_breakdown' && (
                                    <Top3CategoriesWidget items={topCategories} />
                                )}

                                {/* Slide 3 ✅ Average spending shown when clicking next arrow */}
                                {currentChartType === 'average_spending' && (
                                    <AverageSpendingWidget items={avgSpending} periodValue={avgPeriod} />
                                )}
                            </div>

                            {/* Right arrow */}
                            <button
                                onClick={handleNextChart}
                                className="hidden sm:flex absolute right-0 top-1/2 -translate-y-1/2 w-8 h-8 items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 shadow"
                            >
                                ›
                            </button>
                        </div>
                    </div>

                    {/* TABLE */}
                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        <h2 className="text-lg font-semibold text-text mb-4">Recent Transactions</h2>

                        {filteredTransactions.length === 0 ? (
                            <p className="text-metallic-gray text-sm">
                                No transactions found for the selected filters.
                            </p>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="min-w-full text-sm">
                                    <thead>
                                    <tr className="text-left text-metallic-gray border-b border-strokes">
                                        <th className="py-2 pr-4">Name / Bank Card</th>
                                        <th className="py-2 pr-4">Category</th>
                                        <th className="py-2 pr-4 text-right">Amount</th>
                                        <th className="py-2 pr-4">Date</th>
                                        <th className="py-2 pr-4 text-right">Action</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {filteredTransactions.map((t) => {
                                        const categoryName =
                                            categoryMap[t.category_id] ||
                                            t.category?.name ||
                                            t.category_name ||
                                            t.category ||
                                            '-';
                                        const dateLabel = formatDateLabel(t.transaction_date || t.created_at);

                                        return (
                                            <tr
                                                key={t.id}
                                                className="border-b border-strokes last:border-b-0"
                                            >
                                                <td className="py-2 pr-4">
                                                    {t.description || 'Transaction'}
                                                    {t.wallet_name && (
                                                        <div className="text-[11px] text-metallic-gray">
                                                            {t.wallet_name}
                                                        </div>
                                                    )}
                                                </td>
                                                <td className="py-2 pr-4">{categoryName}</td>
                                                <td className="py-2 pr-4 text-right">
                                                    {Number(t.amount || 0).toFixed(2)}
                                                </td>
                                                <td className="py-2 pr-4">{dateLabel}</td>
                                                <td className="py-2 pr-4 text-right">
                                                    <button className="px-4 py-1.5 rounded-full bg-blue text-white text-xs font-semibold hover:bg-blue-600">
                                                        View
                                                    </button>
                                                </td>
                                            </tr>
                                        );
                                    })}
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
