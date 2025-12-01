import React from 'react';

const BudgetProgress = ({ budget = 0, spent = 0, month, currencySymbol = '$' }) => {
    const totalBudget = Number(budget) || 0;
    const totalSpent = Number(spent) || 0;

    const percentage =
        totalBudget > 0 ? Math.min(100, (totalSpent / totalBudget) * 100) : 0;
    const remaining = Math.max(0, totalBudget - totalSpent);

    const getBarColor = () => {
        if (percentage >= 90) return '#ef4444'; // red
        if (percentage >= 80) return '#f59e0b'; // amber
        if (percentage >= 50) return '#22c55e'; // green
        return '#22c55e';
    };

    const monthLabel = month
        ? new Date(`${month}-01`).toLocaleDateString('en-US', {
            month: 'long',
            year: 'numeric',
        })
        : 'This month';

    return (
        <div className="bg-white rounded-xl shadow-sm border border-strokes p-6 h-full">
            <div className="flex justify-between items-start mb-2">
                <div>
                    <h2 className="text-lg font-semibold text-text">Budget Overview</h2>
                    <p className="text-sm text-metallic-gray">{monthLabel}</p>
                </div>
                <span className="text-xs font-medium text-metallic-gray bg-gray-100 px-3 py-1 rounded-full">
                    {percentage.toFixed(0)}% used
                </span>
            </div>

            <div className="mt-4">
                <div className="flex justify-between text-sm mb-1">
                    <span className="text-metallic-gray">Spent</span>
                    <span className="font-medium text-text">
                        {currencySymbol}
                        {totalSpent.toFixed(2)}
                    </span>
                </div>

                <div className="w-full h-3 bg-gray-100 rounded-full overflow-hidden">
                    <div
                        className="h-3 rounded-full transition-all duration-300"
                        style={{
                            width: `${percentage}%`,
                            backgroundColor: getBarColor(),
                        }}
                    />
                </div>

                <div className="mt-3 grid grid-cols-2 gap-4 text-sm">
                    <div>
                        <p className="text-metallic-gray">Limit</p>
                        <p className="font-semibold text-text">
                            {currencySymbol}
                            {totalBudget.toFixed(2)}
                        </p>
                    </div>
                    <div className="text-right">
                        <p className="text-metallic-gray">Remaining</p>
                        <p className="font-semibold text-text">
                            {currencySymbol}
                            {remaining.toFixed(2)}
                        </p>
                    </div>
                </div>
            </div>

            <div className="mt-4 text-xs text-metallic-gray">
                <p>
                    Tip: Staying below{' '}
                    <span className="font-semibold">80%</span> of your monthly budget
                    gives you extra safety for unexpected expenses.
                </p>
            </div>
        </div>
    );
};

export default BudgetProgress;
