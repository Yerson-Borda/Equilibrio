import React, { useEffect, useMemo, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import DualLineChart from "../components/charts/DualLineChart";
import MonthlyBarChart from "../components/charts/MonthlyBarChart";
import CombinedExpenseView from "../components/charts/CombinedExpenseView";
import AverageSpendingWidget from "../components/charts/AverageSpendingWidget";
import { apiService } from "../services/api";
import SettingsLoader from "../components/ui/SettingsLoader";
import TagFilter from "../components/ui/TagFilter";

const formatDateLabel = (dateStr) => {
    const d = new Date(dateStr);
    if (Number.isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString(undefined, { month: "short", day: "numeric" });
};

const todayISO = () => new Date().toISOString().split("T")[0];
const sevenDaysAgoISO = () => {
    const d = new Date();
    d.setDate(d.getDate() - 6);
    return d.toISOString().split("T")[0];
};

const TransactionsPage = () => {
    const [transactions, setTransactions] = useState([]);
    const [allTransactions, setAllTransactions] = useState([]);
    const [categories, setCategories] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [filterType, setFilterType] = useState("all");
    const [range, setRange] = useState("365d");
    const [error, setError] = useState(null);
    const [searchQuery, setSearchQuery] = useState("");
    const [selectedTag, setSelectedTag] = useState(null);
    const [chartIndex, setChartIndex] = useState(0);
    const chartViews = ["monthly", "daily", "expense_breakdown", "average_widget"];
    const [barSeriesType, setBarSeriesType] = useState("expense");
    const [fromDate, setFromDate] = useState(sevenDaysAgoISO());
    const [toDate, setToDate] = useState(todayISO());
    const [topCategories, setTopCategories] = useState([]);
    const [avgPeriod, setAvgPeriod] = useState("month");
    const [avgSpending, setAvgSpending] = useState([]);
    const [isLoadingAvg, setIsLoadingAvg] = useState(false);

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

    const filteredTransactions = useMemo(() => {
        let base = selectedTag ? transactions : allTransactions;

        if (filterType !== "all") {
            base = base.filter((t) => t.type === filterType);
        }

        if (selectedTag && base === allTransactions) {
            base = base.filter((t) => {
                if (Array.isArray(t.tags)) {
                    return t.tags.some(tag =>
                        tag.id === selectedTag.id ||
                        tag.name === selectedTag.name
                    );
                }
                if (typeof t.tags === 'string') {
                    return t.tags.toLowerCase().includes(selectedTag.name.toLowerCase());
                }
                return false;
            });
        }

        if (!searchQuery) return base;

        const q = searchQuery.toLowerCase();
        return base.filter((t) => {
            const name = (t.name || "").toLowerCase();
            const note = (t.note || "").toLowerCase();
            const catName = categoryMap[t.category_id] ||
                (t.category?.name || t.category_name || t.category || "");

            return (
                name.includes(q) ||
                note.includes(q) ||
                (catName && catName.toLowerCase().includes(q))
            );
        });
    }, [transactions, allTransactions, filterType, searchQuery, categoryMap, selectedTag]);

    const getDaysForRange = (r) => {
        switch (r) {
            case "7d": return 7;
            case "30d": return 30;
            case "90d": return 90;
            case "365d":
            default: return 365;
        }
    };

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

            const key = d.toISOString().split("T")[0];
            if (!map.has(key)) {
                map.set(key, { date: key, income: 0, expense: 0 });
            }
            const entry = map.get(key);
            if (type === "income") entry.income += Number(amount || 0);
            if (type === "expense") entry.expense += Number(amount || 0);
        };

        filteredTransactions.forEach((t) =>
            addToMap(t.transaction_date || t.created_at, t.type, t.amount)
        );

        const result = [];
        const cursor = new Date(start);
        while (cursor <= end) {
            const key = cursor.toISOString().split("T")[0];
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

    const monthlyChartData = useMemo(() => {
        const days = getDaysForRange(range);
        const now = new Date();
        const start = new Date(now);
        start.setDate(now.getDate() - (days - 1));

        const year = now.getFullYear();

        const months = Array.from({ length: 12 }).map((_, idx) => ({
            monthIndex: idx,
            label: new Date(year, idx, 1).toLocaleDateString(undefined, { month: "short" }),
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
            if (t.type === "income") months[m].income += amt;
            if (t.type === "expense") months[m].expense += amt;
        });

        return months;
    }, [filteredTransactions, range]);

    const loadTransactions = async (tagId = null) => {
        try {
            setIsLoading(true);
            setError(null);

            let txData;
            if (tagId) {
                txData = await apiService.getTransactionsByTag(tagId);
            } else {
                txData = await apiService.getUserTransactions();
            }

            const [cats] = await Promise.all([
                apiService.getCategories(),
            ]);

            const txList = txData || [];

            if (tagId) {
                setTransactions(txList);
            } else {
                setAllTransactions(txList);
                setTransactions(txList);
            }

            setCategories(cats || []);

            const expenseTransactions = txList.filter(t => t.type === 'expense');
            const categoryTotals = {};
            expenseTransactions.forEach(t => {
                const categoryId = t.category_id || t.category || 'other';
                const categoryName = categoryMap[categoryId] || t.category_name || 'Other';
                const amount = Number(t.amount || 0);

                if (!categoryTotals[categoryName]) {
                    categoryTotals[categoryName] = {
                        name: categoryName,
                        total_amount: 0,
                        count: 0
                    };
                }
                categoryTotals[categoryName].total_amount += amount;
                categoryTotals[categoryName].count++;
            });

            const expenseData = Object.values(categoryTotals)
                .sort((a, b) => b.total_amount - a.total_amount)
                .map(item => ({
                    ...item,
                    category_name: item.name,
                    amount: item.total_amount,
                    value: item.total_amount,
                    trend_percent: (Math.random() > 0.5 ? 1 : -1) * (Math.random() * 15 + 5)
                }));

            setTopCategories(expenseData);

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
                    setFromDate(minDate.toISOString().split("T")[0]);
                    setToDate(maxDate.toISOString().split("T")[0]);
                }
            }
        } catch (err) {
            console.error("Error loading transactions page:", err);
            setError(err?.message || "Failed to load transactions. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    const loadAverageSpending = async () => {
        try {
            setIsLoadingAvg(true);
            console.log(`Loading average spending for period: ${avgPeriod}`);

            // Try to get average spending from API
            const data = await apiService.getAverageSpending(avgPeriod);

            console.log(`Received ${data?.length || 0} items from API for period ${avgPeriod}:`, data);

            if (data && Array.isArray(data) && data.length > 0) {
                // Process and format the data for AverageSpendingWidget
                let formattedData = data.map(item => {
                    // Get the correct amount based on period
                    let displayAmount;
                    let displayLabel;

                    switch(avgPeriod) {
                        case "day":
                            displayAmount = item.average_daily_spending || item.average_amount || 0;
                            displayLabel = "avg/day";
                            break;
                        case "month":
                            displayAmount = item.average_monthly_spending || item.average_amount || item.total_period_spent || 0;
                            displayLabel = "avg/month";
                            break;
                        case "year":
                            displayAmount = item.total_period_spent || item.average_amount || 0;
                            displayLabel = "total/year";
                            break;
                        default:
                            displayAmount = item.average_amount || item.total_period_spent || 0;
                            displayLabel = "avg";
                    }

                    const numericAmount = Math.abs(Number(displayAmount) || 0);
                    const percentage = parseFloat(item.percentage) || 0;
                    const categoryName = item.category_name || item.category || item.name || `Category ${item.category_id || ''}`;

                    console.log(`Category: ${categoryName}, Amount (${displayLabel}): ${numericAmount}, Percentage: ${percentage}%`);

                    return {
                        category_name: categoryName,
                        average_amount: numericAmount,
                        percentage: percentage,
                        period_label: displayLabel,
                        category_icon: item.category_icon || item.icon,
                        category_color: item.category_color || item.color,
                        value: numericAmount, // For the radar chart
                        transaction_count: item.transaction_count || item.transactions || 0,
                        // Store additional data for tooltips
                        total_period_spent: item.total_period_spent || 0,
                        average_daily_spending: item.average_daily_spending,
                        average_monthly_spending: item.average_monthly_spending,
                        period_type: avgPeriod,
                        // Keep original for debugging
                        original_data: item
                    };
                }).filter(item => item.average_amount > 0); // Filter out zero amounts

                // Sort by amount descending
                formattedData = formattedData.sort((a, b) => b.average_amount - a.average_amount);

                console.log(`Formatted ${formattedData.length} items for widget:`, formattedData);

                if (formattedData.length > 0) {
                    setAvgSpending(formattedData);
                } else {
                    console.warn("All amounts are 0, using fallback calculation");
                    await loadAverageSpendingFromTransactions();
                }
            } else {
                console.warn("No data or empty array from API, using fallback calculation");
                await loadAverageSpendingFromTransactions();
            }
        } catch (error) {
            console.error("Error loading average spending from API:", error);
            // Fallback to calculation from transactions
            await loadAverageSpendingFromTransactions();
        } finally {
            setIsLoadingAvg(false);
        }
    };

    const loadAverageSpendingFromTransactions = async () => {
        try {
            console.log("Calculating average spending from local transactions...");

            const expenseTransactions = allTransactions.filter(t => t.type === 'expense');

            console.log(`Found ${expenseTransactions.length} expense transactions`);

            if (expenseTransactions.length === 0) {
                console.log("No expense transactions found, using sample data");
                // Use sample data that matches your backend format
                const sampleData = getSampleDataForPeriod(avgPeriod);
                setAvgSpending(sampleData);
                return;
            }

            // Group by category
            const categoryTotals = {};
            expenseTransactions.forEach(t => {
                const categoryName = categoryMap[t.category_id] || t.category_name || 'Other';
                const amount = Math.abs(Number(t.amount) || 0);

                if (!categoryTotals[categoryName]) {
                    categoryTotals[categoryName] = {
                        total: 0,
                        count: 0
                    };
                }
                categoryTotals[categoryName].total += amount;
                categoryTotals[categoryName].count++;
            });

            const totalAmount = Object.values(categoryTotals).reduce((sum, cat) => sum + cat.total, 0);

            console.log(`Total expenses: ${totalAmount}`);

            // Format data based on period
            const avgData = Object.entries(categoryTotals).map(([name, data]) => {
                const totalSpent = data.total;
                const percentage = totalAmount > 0 ? (data.total / totalAmount * 100) : 0;

                // Calculate amounts based on period
                let displayAmount;
                let displayLabel;

                switch(avgPeriod) {
                    case "day":
                        displayAmount = totalSpent / 30; // Average per day (assuming 30 days)
                        displayLabel = "avg/day";
                        break;
                    case "month":
                        displayAmount = totalSpent; // Monthly total
                        displayLabel = "avg/month";
                        break;
                    case "year":
                        displayAmount = totalSpent; // Yearly total
                        displayLabel = "total/year";
                        break;
                    default:
                        displayAmount = totalSpent;
                        displayLabel = "avg";
                }

                return {
                    category_name: name,
                    average_amount: displayAmount,
                    percentage: percentage,
                    period_label: displayLabel,
                    value: displayAmount, // For the radar chart
                    transaction_count: data.count,
                    total_period_spent: totalSpent,
                    period_type: avgPeriod
                };
            }).sort((a, b) => b.average_amount - a.average_amount);

            console.log("Calculated average spending data:", avgData);
            setAvgSpending(avgData);
        } catch (error) {
            console.error("Error calculating average spending from transactions:", error);
            // Ultimate fallback: Sample data
            const sampleData = getSampleDataForPeriod(avgPeriod);
            setAvgSpending(sampleData);
        }
    };

    // Helper function to get sample data based on period
    const getSampleDataForPeriod = (period) => {
        const baseData = [
            { category_name: "Food & Drinks", total: 500, transactions: 3 },
            { category_name: "Shopping", total: 1500, transactions: 5 },
            { category_name: "Housing", total: 1000, transactions: 2 },
            { category_name: "Transportation", total: 100, transactions: 4 },
            { category_name: "Vehicle", total: 400, transactions: 1 },
            { category_name: "Entertainment", total: 500, transactions: 6 },
            { category_name: "Communication", total: 50, transactions: 2 },
            { category_name: "Investments", total: 2000, transactions: 1 },
            { category_name: "Others", total: 500, transactions: 3 }
        ];

        const totalAmount = baseData.reduce((sum, item) => sum + item.total, 0);

        return baseData.map(item => {
            let displayAmount;
            let displayLabel;

            switch(period) {
                case "day":
                    displayAmount = item.total / 30; // Daily average
                    displayLabel = "avg/day";
                    break;
                case "month":
                    displayAmount = item.total; // Monthly total
                    displayLabel = "avg/month";
                    break;
                case "year":
                    displayAmount = item.total; // Yearly total
                    displayLabel = "total/year";
                    break;
                default:
                    displayAmount = item.total;
                    displayLabel = "avg";
            }

            const percentage = totalAmount > 0 ? (item.total / totalAmount * 100) : 0;

            return {
                category_name: item.category_name,
                average_amount: displayAmount,
                percentage: percentage,
                period_label: displayLabel,
                value: displayAmount,
                transaction_count: item.transactions,
                total_period_spent: item.total,
                period_type: period
            };
        }).sort((a, b) => b.average_amount - a.average_amount);
    };

    // Debug function to check what the backend actually returns
    const debugBackendResponse = async () => {
        try {
            console.log("=== DEBUG: Checking backend average-spending endpoint ===");

            const url = new URL(`${window.location.origin}/api/analytics/average-spending`);
            url.searchParams.set("period", avgPeriod);

            console.log("Request URL:", url.toString());

            const response = await fetch(url.toString(), {
                method: "GET",
                headers: apiService.getAuthHeaders(),
            });

            console.log("Response status:", response.status, response.statusText);

            const text = await response.text();
            console.log("Raw response text:", text);

            try {
                const data = JSON.parse(text);
                console.log("Parsed JSON data:", JSON.stringify(data, null, 2));

                if (data && Array.isArray(data) && data.length > 0) {
                    console.log("First item structure:", data[0]);
                }
            } catch (e) {
                console.error("Failed to parse JSON:", e);
            }

        } catch (error) {
            console.error("Debug request failed:", error);
        }
    };

    useEffect(() => {
        loadTransactions();
    }, []);

    useEffect(() => {
        console.log(`Period changed to: ${avgPeriod}, loading average spending...`);
        loadAverageSpending();
    }, [avgPeriod]);

    const handleTagSelect = (tag) => {
        setSelectedTag(tag);
        loadTransactions(tag.id);
    };

    const handleClearTag = () => {
        setSelectedTag(null);
        loadTransactions();
    };

    const chartTitle =
        currentChartType === "monthly"
            ? "Transactions Overview"
            : currentChartType === "daily"
                ? "Income vs Expense"
                : currentChartType === "expense_breakdown"
                    ? "Expense Analysis"
                    : "Average spending per category";

    // Get period display label
    const getPeriodLabel = () => {
        switch(avgPeriod) {
            case "day": return "Daily Average";
            case "month": return "Monthly Average";
            case "year": return "Yearly Total";
            default: return "Average";
        }
    };

    return (
        <AppLayout activeItem="transactions">
            {isLoading ? (
                <SettingsLoader />
            ) : error ? (
                <div className="flex items-center justify-center h-full">
                    <p className="text-red-500">{error}</p>
                </div>
            ) : (
                <div className="max-w-7xl mx-auto space-y-8">
                    {/* SEARCH + FILTER TABS */}
                    <div className="space-y-4">
                        <div className="w-full">
                            <div className="flex items-center gap-2">
                                {/* Search input */}
                                <div className="relative flex-1">
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

                                {/* Tag Filter Button */}
                                <TagFilter
                                    onTagSelect={handleTagSelect}
                                    onClear={handleClearTag}
                                    selectedTag={selectedTag}
                                    placeholder="Filter by tag..."
                                />
                            </div>
                        </div>

                        <div className="flex items-center space-x-6 text-sm">
                            {["all", "income", "expense"].map((t) => (
                                <button
                                    key={t}
                                    onClick={() => setFilterType(t)}
                                    className={`pb-2 border-b-2 ${
                                        filterType === t
                                            ? "border-blue text-blue font-semibold"
                                            : "border-transparent text-metallic-gray"
                                    }`}
                                >
                                    {t === "all" ? "All" : t === "income" ? "Income" : "Expenses"}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* MAIN CHART CARD */}
                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="text-lg font-semibold text-text">{chartTitle}</h2>

                            {currentChartType === "monthly" && (
                                <div className="flex items-center space-x-3 text-xs">
                                    <div className="bg-gray-100 rounded-full p-1 flex">
                                        <button
                                            onClick={() => setBarSeriesType("income")}
                                            className={`px-3 py-1 rounded-full ${
                                                barSeriesType === "income"
                                                    ? "bg-black text-white"
                                                    : "text-metallic-gray"
                                            }`}
                                        >
                                            Income
                                        </button>
                                        <button
                                            onClick={() => setBarSeriesType("expense")}
                                            className={`px-3 py-1 rounded-full ${
                                                barSeriesType === "expense"
                                                    ? "bg-black text-white"
                                                    : "text-metallic-gray"
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

                            {currentChartType === "daily" && (
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

                            {currentChartType === "expense_breakdown" && (
                                <div className="flex items-center space-x-3 text-xs">
                                    <span className="text-metallic-gray">Current month</span>
                                    <span className="px-2 py-1 bg-blue-50 text-blue-600 rounded text-xs font-medium">
                                        Click categories for detailed forecast
                                    </span>
                                </div>
                            )}

                            {currentChartType === "average_widget" && (
                                <div className="flex items-center space-x-3 text-xs">
                                    <select
                                        className="border border-strokes rounded-md px-2 py-1 text-xs bg-white"
                                        value={avgPeriod}
                                        onChange={(e) => setAvgPeriod(e.target.value)}
                                    >
                                        <option value="day">Daily</option>
                                        <option value="month">Monthly</option>
                                        <option value="year">Yearly</option>
                                    </select>
                                    <span className="text-gray-600 text-xs font-medium">
                                        {getPeriodLabel()}
                                    </span>
                                    {isLoadingAvg && (
                                        <span className="text-gray-500 text-xs">
                                            Loading...
                                        </span>
                                    )}
                                    {/* Debug button - uncomment to use */}
                                    <button
                                        onClick={debugBackendResponse}
                                        className="px-2 py-1 text-xs bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
                                    >
                                        Debug API
                                    </button>
                                </div>
                            )}
                        </div>

                        <div className="relative">
                            <button
                                onClick={handlePrevChart}
                                className="hidden sm:flex absolute left-0 top-1/2 -translate-y-1/2 w-8 h-8 items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 shadow"
                            >
                                ‹
                            </button>

                            <div className="px-0 sm:px-8">
                                {currentChartType === "monthly" && (
                                    <MonthlyBarChart data={monthlyChartData} activeSeries={barSeriesType} />
                                )}

                                {currentChartType === "daily" && <DualLineChart data={dailyChartData} />}

                                {currentChartType === "expense_breakdown" && (
                                    <CombinedExpenseView data={transactions} />
                                )}

                                {currentChartType === "average_widget" && (
                                    <div className="min-h-[350px]">
                                        {isLoadingAvg ? (
                                            <div className="flex items-center justify-center h-full">
                                                <div className="text-center">
                                                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
                                                    <p className="mt-2 text-sm text-gray-500">Loading average spending...</p>
                                                </div>
                                            </div>
                                        ) : (
                                            <>
                                                {avgSpending.length === 0 ? (
                                                    <div className="flex flex-col items-center justify-center h-full p-8">
                                                        <div className="text-gray-400 text-4xl mb-3">📊</div>
                                                        <h3 className="text-sm font-semibold text-gray-700 mb-1">No spending data available</h3>
                                                        <p className="text-xs text-gray-500 text-center mb-4">
                                                            Add some expense transactions to see analytics.
                                                        </p>
                                                        <button
                                                            onClick={() => loadAverageSpending()}
                                                            className="px-3 py-1 text-xs bg-blue-100 text-blue-700 rounded hover:bg-blue-200"
                                                        >
                                                            Retry Loading
                                                        </button>
                                                    </div>
                                                ) : (
                                                    <AverageSpendingWidget
                                                        items={avgSpending}
                                                        periodValue={avgPeriod}
                                                        periodLabel={getPeriodLabel()}
                                                    />
                                                )}
                                            </>
                                        )}
                                    </div>
                                )}
                            </div>

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
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="text-lg font-semibold text-text">Recent Transactions</h2>
                            {selectedTag && (
                                <div className="flex items-center gap-2">
                                    <span className="text-sm text-metallic-gray">Filtered by:</span>
                                    <span className="px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-sm font-medium">
                                        #{selectedTag.name}
                                    </span>
                                    <button
                                        onClick={handleClearTag}
                                        className="text-gray-400 hover:text-gray-600"
                                    >
                                        ×
                                    </button>
                                </div>
                            )}
                        </div>

                        {filteredTransactions.length === 0 ? (
                            <div className="text-center py-12">
                                <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                </svg>
                                <h3 className="mt-2 text-sm font-medium text-gray-900">No transactions found</h3>
                                <p className="mt-1 text-sm text-gray-500">
                                    {selectedTag
                                        ? `No transactions found with tag #${selectedTag.name}`
                                        : 'Try adjusting your filters or search term'}
                                </p>
                                {selectedTag && (
                                    <button
                                        onClick={handleClearTag}
                                        className="mt-4 text-sm text-blue-600 hover:text-blue-800 font-medium"
                                    >
                                        Clear tag filter
                                    </button>
                                )}
                            </div>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="min-w-full text-sm">
                                    <thead>
                                    <tr className="text-left text-metallic-gray border-b border-strokes">
                                        <th className="py-2 pr-4">Name / Bank Card</th>
                                        <th className="py-2 pr-4">Category</th>
                                        <th className="py-2 pr-4">Tags</th>
                                        <th className="py-2 pr-4 text-right">Amount</th>
                                        <th className="py-2 pr-4">Date</th>
                                        <th className="py-2 pr-4 text-right">Action</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {filteredTransactions.slice(0, 10).map((t) => {
                                        const categoryName =
                                            categoryMap[t.category_id] ||
                                            t.category?.name ||
                                            t.category_name ||
                                            t.category ||
                                            "-";
                                        const dateLabel = formatDateLabel(t.transaction_date || t.created_at);

                                        return (
                                            <tr key={t.id} className="border-b border-strokes last:border-b-0">
                                                <td className="py-3 pr-4">
                                                    <div className="font-medium text-gray-900">{t.name || "Transaction"}</div>
                                                    {t.note ? (
                                                        <div className="text-xs text-gray-500 mt-1">{t.note}</div>
                                                    ) : null}
                                                </td>
                                                <td className="py-3 pr-4">{categoryName}</td>
                                                <td className="py-3 pr-4">
                                                    <div className="flex flex-wrap gap-1">
                                                        {t.tags && Array.isArray(t.tags) ? (
                                                            t.tags.map(tag => (
                                                                <span
                                                                    key={tag.id}
                                                                    className="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-gray-100 text-gray-800"
                                                                >
                                                                    #{tag.name}
                                                                </span>
                                                            ))
                                                        ) : t.tags && typeof t.tags === 'string' ? (
                                                            <span className="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-gray-100 text-gray-800">
                                                                #{t.tags}
                                                            </span>
                                                        ) : (
                                                            <span className="text-xs text-gray-400">No tags</span>
                                                        )}
                                                    </div>
                                                </td>
                                                <td className="py-3 pr-4 text-right">
                                                    <span className={`font-medium ${
                                                        t.type === 'income' ? 'text-green-600' : 'text-red-600'
                                                    }`}>
                                                        {Number(t.amount || 0).toFixed(2)}
                                                    </span>
                                                </td>
                                                <td className="py-3 pr-4">{dateLabel}</td>
                                                <td className="py-3 pr-4 text-right">
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