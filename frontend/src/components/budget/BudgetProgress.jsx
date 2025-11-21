import React from 'react';

const BudgetProgress = ({ budget = 0, spent = 0, month }) => {
    const safeBudget = Number(budget) || 0;
    const safeSpent = Number(spent) || 0;

    const percentage = safeBudget > 0 ? (safeSpent / safeBudget) * 100 : 0;
    const remaining = safeBudget - safeSpent;
    const isOverBudget = safeSpent > safeBudget;

    const getProgressColor = () => {
        if (percentage >= 100) return '#ef4444'; // red
        if (percentage >= 80) return '#f59e0b';  // amber
        return '#10b981';                        // green
    };

    const getStatusText = () => {
        if (safeBudget === 0) {
            return 'No budget set for this month yet.';
        }

        if (isOverBudget) {
            return 'You have exceeded your budget.';
        }

        if (percentage >= 80) {
            return 'You are close to your budget limit.';
        }

        return 'You are within your budget.';
    };

    const getRemainingText = () => {
        if (safeBudget === 0) return '';
        if (isOverBudget) {
            return `Over budget by $${Math.abs(remaining).toFixed(2)}`;
        }
        return `$${remaining.toFixed(2)} remaining in budget`;
    };

    const formattedMonth = month
        ? new Date(`${month}-01`).toLocaleDateString('en-US', {
            month: 'long',
            year: 'numeric',
        })
        : 'This month';

    return (
        <div className="space-y-4">
            <div className="flex items-center justify-between">
                <div>
                    <p className="text-sm text-metallic-gray">{formattedMonth}</p>
                    <p className="text-xl font-semibold text-text">
                        {safeBudget > 0
                            ? `$${safeBudget.toFixed(2)} budget`
                            : 'No budget set'}
                    </p>
                </div>
                {safeBudget > 0 && (
                    <div className="text-right">
                        <p className="text-sm text-metallic-gray">Spent</p>
                        <p className="text-lg font-semibold text-text">
                            ${safeSpent.toFixed(2)}
                        </p>
                    </div>
                )}
            </div>

            {/* Progress bar */}
            {safeBudget > 0 && (
                <div className="space-y-2">
                    <div className="w-full h-3 rounded-full bg-soft-gray overflow-hidden">
                        <div
                            className="h-full rounded-full transition-all"
                            style={{
                                width: `${Math.min(percentage, 120)}%`,
                                backgroundColor: getProgressColor(),
                            }}
                        />
                    </div>

                    <div className="flex items-center justify-between text-xs text-metallic-gray">
                        <span>{percentage.toFixed(1)}% of budget used</span>
                        <span>{getRemainingText()}</span>
                    </div>
                </div>
            )}

            <div className="mt-2">
                <p className="text-sm font-medium text-text">{getStatusText()}</p>
                {safeBudget > 0 && (
                    <p className="text-xs text-metallic-gray mt-1">
                        Daily average: $
                        {(
                            (safeBudget - safeSpent) /
                            Math.max(1, new Date().getDate())
                        ).toFixed(2)}{' '}
                        remaining / day
                    </p>
                )}
            </div>
        </div>
    );
};

export default BudgetProgress;
