import React from 'react';
import ExpenseDistributionChart from './ExpenseDistributionChart';
import ExpenseBreakdownChart from './ExpenseBreakdownChart';

const CombinedExpenseView = ({ data = [] }) => {
    // Filter expense data only
    const expenseData = Array.isArray(data)
        ? data.filter(item => item.type === 'expense' || !item.type)
        : [];

    return (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Left: Expense Distribution Chart */}
            <div className="bg-white rounded-xl border border-gray-200 p-6">
                <ExpenseDistributionChart data={expenseData} />
            </div>

            {/* Right: Expense Breakdown with Top 3 Categories */}
            <div className="bg-white rounded-xl border border-gray-200 p-6">
                <ExpenseBreakdownChart items={expenseData} />
            </div>
        </div>
    );
};

export default CombinedExpenseView;