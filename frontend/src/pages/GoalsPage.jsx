import React, { useEffect, useState } from "react";
import Sidebar from "../components/layout/Sidebar";
import Header from "../components/layout/Header";

import ComparisonChart from "../components/charts/ComparisonChart";
import CustomPieChart from "../components/charts/PieChart"; // if you use it later
import { apiService } from "../services/api";
import { getCurrencySymbol } from "../config/currencies";

const GoalsPage = () => {
    const [budget, setBudget] = useState(null);
    const [summary, setSummary] = useState(null);
    const [monthlyComparison, setMonthlyComparison] = useState([]);
    const [categorySummary, setCategorySummary] = useState([]);
    const [currency, setCurrency] = useState("USD");
    const [loading, setLoading] = useState(true);

    const today = new Date();
    const thisMonth = `${today.getFullYear()}-${String(
        today.getMonth() + 1
    ).padStart(2, "0")}`;

    const firstDay = `${today.getFullYear()}-${String(
        today.getMonth() + 1
    ).padStart(2, "0")}-01`;
    const lastDay = `${today.getFullYear()}-${String(
        today.getMonth() + 1
    ).padStart(2, "0")}-31`;

    useEffect(() => {
        const loadEverything = async () => {
            try {
                setLoading(true);

                const [user, budg, finSummary, comp, catSum] = await Promise.all([
                    apiService.getCurrentUser(),
                    apiService.getCurrentBudget(),
                    apiService.getFinancialSummary(),
                    apiService.getMonthlyComparison(thisMonth),
                    apiService.getCategorySummary(firstDay, lastDay),
                ]);

                setCurrency(user?.default_currency || "USD");
                setBudget(budg || null);
                setSummary(finSummary || null);
                setMonthlyComparison(comp || []);
                setCategorySummary((catSum && catSum.expenses) || []);
            } catch (err) {
                console.error("Error loading goals page:", err);
            } finally {
                setLoading(false);
            }
        };

        loadEverything();
    }, []);

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center text-text">
                Loading goals...
            </div>
        );
    }

    const symbol = getCurrencySymbol(currency);

    // ------- safely derived values (avoid null crashes) -------
    const totalSaved = Number(summary?.total_saved ?? 0);
    const totalIncome = Number(summary?.total_income ?? 0);
    const totalSpent = Number(summary?.total_spent ?? 0);

    const rawTarget = totalIncome > 0 ? totalIncome - totalSpent : totalSaved;
    const goalTarget = rawTarget > 0 ? rawTarget : 1000;

    const goalPercentage =
        goalTarget > 0 ? Math.min(100, Math.round((totalSaved / goalTarget) * 100)) : 0;

    const monthlyLimit = Number(budget?.monthly_limit ?? 0);
    const monthlySpent = Number(budget?.monthly_spent ?? 0);
    const dailyLimit = Number(budget?.daily_limit ?? 0);
    const dailySpent = Number(budget?.daily_spent ?? 0);

    const availableBalance = Math.max(monthlyLimit - monthlySpent, 0);

    const monthlyPercent =
        monthlyLimit > 0 ? Math.min(100, (monthlySpent / monthlyLimit) * 100) : 0;

    const dailyPercent =
        dailyLimit > 0 ? Math.min(100, (dailySpent / dailyLimit) * 100) : 0;

    return (
        <div className="min-h-screen flex bg-background">
            <Sidebar activeItem="goals" />

            <div className="flex-1 ml-64 flex flex-col">
                <Header />

                <main className="p-8">
                    <h1 className="text-2xl font-semibold text-text mb-6">Goals</h1>

                    {/* --------------------- TOP CARDS --------------------- */}
                    <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-8">
                        {/* SAVING GOAL */}
                        <div className="bg-white border border-strokes rounded-xl p-6">
                            <div className="flex justify-between items-start mb-4">
                                <h2 className="text-lg font-semibold text-text">Saving Goal</h2>
                                <select className="border rounded-lg p-2 text-sm">
                                    <option>This Month</option>
                                </select>
                            </div>

                            <div className="flex items-center space-x-4">
                                <div className="flex-1">
                                    <p className="text-metallic-gray text-sm">Target Achieved</p>
                                    <p className="text-xl font-bold text-text">
                                        {symbol}
                                        {totalSaved.toLocaleString()}
                                    </p>

                                    <p className="mt-3 text-sm text-metallic-gray">
                                        This Month Target
                                    </p>
                                    <p className="text-lg font-semibold text-text">
                                        {symbol}
                                        {goalTarget.toLocaleString()}
                                    </p>
                                </div>

                                {/* Simple semi-circular style progress indicator */}
                                <div
                                    className="w-32 h-32 rounded-full border-[10px] flex items-center justify-center text-xl font-bold"
                                    style={{
                                        borderColor: "#6F5BFF40",
                                        borderTopColor: "#6F5BFF",
                                    }}
                                >
                                    {goalPercentage}%
                                </div>
                            </div>

                            <button className="mt-6 px-4 py-2 bg-blue text-white font-medium rounded-lg hover:bg-blue/90">
                                Adjust Goal ✏️
                            </button>
                        </div>

                        {/* SAVING SUMMARY */}
                        <div className="bg-white border border-strokes rounded-xl p-6">
                            <div className="flex justify-between mb-4">
                                <h2 className="text-lg font-semibold text-text">
                                    Saving Summary
                                </h2>

                                <select className="border p-2 rounded-lg text-sm">
                                    <option>{thisMonth}</option>
                                </select>
                            </div>

                            <ComparisonChart
                                title="Summary"
                                data={monthlyComparison}
                                currencySymbol={symbol}
                            />
                        </div>
                    </div>

                    {/* --------------------- BUDGET + CATEGORY LIMITS --------------------- */}
                    <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-10">
                        {/* BUDGET BOX */}
                        <div className="bg-[#1d1e22] text-white p-6 rounded-xl">
                            <h3 className="text-lg font-semibold mb-2">Available Balance</h3>

                            <p className="text-4xl font-bold">
                                {symbol}
                                {availableBalance.toLocaleString()}
                            </p>

                            <div className="mt-6 flex justify-between">
                                <div>
                                    <p className="text-gray-400 text-sm">Budget</p>
                                    <p className="text-xl font-semibold">
                                        {symbol}
                                        {monthlyLimit.toLocaleString()}
                                    </p>
                                </div>

                                <div>
                                    <p className="text-gray-400 text-sm">Spent</p>
                                    <p className="text-xl font-semibold">
                                        {symbol}
                                        {monthlySpent.toLocaleString()}
                                    </p>
                                </div>
                            </div>

                            <div className="w-full h-3 mt-3 bg-gray-700 rounded-full">
                                <div
                                    className="h-3 bg-blue rounded-full"
                                    style={{ width: `${monthlyPercent}%` }}
                                />
                            </div>

                            {/* DAILY LIMIT */}
                            <div className="mt-8">
                                <div className="flex justify-between text-sm">
                                    <p className="text-gray-400">Daily Limit</p>
                                    <p>
                                        {symbol}
                                        {dailyLimit.toLocaleString()}
                                    </p>
                                </div>

                                <div className="w-full h-3 bg-gray-700 rounded-full mt-2">
                                    <div
                                        className="h-3 bg-green-400 rounded-full"
                                        style={{ width: `${dailyPercent}%` }}
                                    />
                                </div>
                            </div>
                        </div>

                        {/* CATEGORY LIMITS BOX */}
                        <div className="bg-[#1d1e22] text-white p-6 rounded-xl">
                            <h3 className="text-lg font-semibold mb-4">
                                Limits Per Category
                            </h3>

                            <div className="space-y-4">
                                {categorySummary.map((cat) => {
                                    const spent = Number(cat.total_amount || 0);
                                    const hardcodedLimit = 2000; // until you have real limit per category
                                    return (
                                        <div
                                            key={cat.category_id}
                                            className="flex justify-between items-center"
                                        >
                      <span className="text-gray-300">
                        {cat.category_name}
                      </span>

                                            <div>
                        <span className="text-red-400 font-semibold">
                          {symbol}
                            {spent.toLocaleString()}
                        </span>

                                                <span className="text-gray-400 ml-1">
                          of {symbol}
                                                    {hardcodedLimit.toLocaleString()}
                        </span>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>

                            <div className="border-t border-gray-700 mt-6 pt-4 text-gray-400 text-sm">
                                Set budgets and limits to track your spending.
                            </div>
                        </div>
                    </div>

                    {/* --------------------- SPENDING GOALS --------------------- */}
                    <h2 className="text-lg font-semibold text-text mb-3">
                        Spending Goals
                    </h2>

                    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6">
                        {[1, 2, 3].map((g) => (
                            <div
                                key={g}
                                className="bg-white border border-strokes rounded-xl"
                            >
                                <div
                                    className="h-36 bg-cover rounded-t-xl"
                                    style={{
                                        backgroundImage:
                                            "url(https://source.unsplash.com/random/800x400)",
                                    }}
                                />

                                <div className="p-4">
                                    <h3 className="font-semibold text-text">
                                        Trip to Iceland
                                    </h3>

                                    <div className="w-full bg-gray-200 h-2 rounded-full mt-3">
                                        <div className="h-2 bg-blue rounded-full w-1/2" />
                                    </div>

                                    <p className="text-sm mt-2 font-medium">
                                        $550 of $1000
                                    </p>
                                </div>
                            </div>
                        ))}

                        <button className="border-2 border-dashed border-strokes rounded-xl p-6 flex flex-col items-center justify-center hover:bg-gray-50">
                            <span className="text-4xl text-blue">+</span>
                            <p className="mt-2 text-sm font-medium text-text">New Goal</p>
                        </button>
                    </div>
                </main>
            </div>
        </div>
    );
};

export default GoalsPage;
