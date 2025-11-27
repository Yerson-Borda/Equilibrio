import React, { useState, useEffect } from 'react';
import Sidebar from '../components/layout/Sidebar';
import Header from '../components/layout/Header';
import BudgetProgress from '../components/budget/BudgetProgress';
import BudgetSetup from '../components/budget/BudgetSetup';
import ComparisonChart from '../components/charts/ComparisonChart';
import CustomPieChart from '../components/charts/PieChart';
import { apiService } from '../services/api';
import { getCurrencySymbol } from '../config/currencies';

const GoalsPage = () => {
    const [budgetState, setBudgetState] = useState({
        budget: 0,
        spent: 0,
        month: new Date().toISOString().slice(0, 7),
    });

    const [defaultCurrency, setDefaultCurrency] = useState('USD');
    const [comparisonData, setComparisonData] = useState([]);
    const [categoryData, setCategoryData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const currencySymbol = getCurrencySymbol(defaultCurrency);

    const handleBudgetUpdate = ({ budget, spent, month }) => {
        setBudgetState({ budget, spent, month });
    };

    const getMonthBoundaries = () => {
        const now = new Date();
        const year = now.getFullYear();
        const month = now.getMonth(); // 0-based

        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);

        const toISODate = (d) => d.toISOString().split('T')[0];

        return {
            monthStr: `${year}-${String(month + 1).padStart(2, '0')}`,
            startDate: toISODate(firstDay),
            endDate: toISODate(lastDay),
        };
    };

    useEffect(() => {
        const loadGoalsData = async () => {
            try {
                setLoading(true);
                setError(null);

                const { monthStr, startDate, endDate } = getMonthBoundaries();

                const [user, monthlyComparison, categorySummary] = await Promise.all([
                    apiService.getCurrentUser(),
                    apiService.getMonthlyComparison(monthStr),
                    apiService.getCategorySummary(startDate, endDate),
                ]);

                setDefaultCurrency(user?.default_currency || 'USD');
                setComparisonData(monthlyComparison || []);
                setCategoryData(categorySummary || []);
            } catch (err) {
                console.error('Error loading goals analytics:', err);
                setError('Failed to load analytics data');
            } finally {
                setLoading(false);
            }
        };

        loadGoalsData();
    }, []);

    return (
        <div className="min-h-screen bg-background flex">
            <Sidebar activeItem="goals" />
            <div className="flex-1 ml-64 flex flex-col bg-background">
                <Header />
                <main className="flex-1 p-8 overflow-auto bg-background">
                    <div className="flex items-center justify-between mb-6">
                        <div>
                            <h1 className="text-2xl font-semibold text-text">Goals & Budget</h1>
                            <p className="text-sm text-metallic-gray">
                                Track how your spending aligns with your planned budget and goals.
                            </p>
                        </div>
                    </div>

                    {error && (
                        <div className="mb-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
                            {error}
                        </div>
                    )}

                    {/* TOP: budget setup + progress */}
                    <div className="grid grid-cols-1 lg:grid-cols-[1.3fr,1.2fr] gap-6 mb-8">
                        <BudgetSetup onBudgetUpdate={handleBudgetUpdate} />
                        <BudgetProgress
                            budget={budgetState.budget}
                            spent={budgetState.spent}
                            month={budgetState.month}
                            currencySymbol={currencySymbol}
                        />
                    </div>

                    {/* BOTTOM: analytics charts */}
                    <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                        <div className="h-full">
                            <ComparisonChart
                                data={comparisonData}
                                title="Category Comparison (This Month)"
                                currencySymbol={currencySymbol}
                            />
                        </div>

                        <div className="h-full">
                            <CustomPieChart
                                data={categoryData}
                                title="Spending by Category"
                                currencySymbol={currencySymbol}
                            />
                        </div>
                    </div>

                    {/* Future goals section */}
                    <div className="mt-10">
                        <h2 className="text-lg font-semibold text-text mb-2">
                            Future Goals (coming soon)
                        </h2>
                        <p className="text-metallic-gray text-sm">
                            Here you can later show cards for individual goals (e.g. “Trip to
                            Iceland”, “New Laptop”) once backend endpoints for goals are in place.
                        </p>
                    </div>
                </main>
            </div>
        </div>
    );
};

export default GoalsPage;
