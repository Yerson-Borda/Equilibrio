import React from 'react';
import AppLayout from '../components/layout/AppLayout';
import BudgetProgress from '../components/budget/BudgetProgress';
import BudgetSetup from '../components/budget/BudgetSetup';

const GoalsPage = () => {
    // Later you can fetch real data here
    const dummyBudget = {
        budget: 3000,
        spent: 1730,
        month: new Date().toISOString().slice(0, 7),
    };

    const dummyLimits = [
        { category: 'Food', spent: 1800, limit: 2000 },
        { category: 'Entertainment', spent: 550, limit: 800 },
        { category: 'Transport', spent: 300, limit: 400 },
        { category: 'Shopping', spent: 900, limit: 1200 },
    ];

    return (
        <AppLayout activeItem="goals">
            <div className="max-w-7xl mx-auto pt-8 pb-10 px-6">
                <h1 className="text-2xl font-bold text-text mb-6">Goals</h1>

                {/* Top row: Saving goal & summary placeholder */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        <h2 className="text-lg font-semibold text-text mb-4">
                            Saving Goal
                        </h2>
                        <p className="text-metallic-gray text-sm">
                            You can connect this block to a real &quot;savings goal&quot;
                            API later. For now it’s just a visual placeholder.
                        </p>
                    </div>
                    <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
                        <h2 className="text-lg font-semibold text-text mb-4">
                            Saving Summary
                        </h2>
                        <p className="text-metallic-gray text-sm">
                            This can show a line chart similar to your design using the
                            analytics endpoints once they’re ready.
                        </p>
                    </div>
                </div>

                {/* Budget Progress */}
                <div className="mb-8">
                    <BudgetProgress budget={dummyBudget} limits={dummyLimits} />
                </div>

                {/* Budget Setup */}
                <div>
                    <BudgetSetup />
                </div>
            </div>
        </AppLayout>
    );
};

export default GoalsPage;
