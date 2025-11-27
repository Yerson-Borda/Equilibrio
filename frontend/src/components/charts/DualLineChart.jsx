import React from 'react';
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
} from 'recharts';

const DualLineChart = ({ data = [], currencySymbol = '$' }) => {
    const safeData = Array.isArray(data) ? data : [];

    const CustomTooltip = ({ active, payload, label }) => {
        if (!active || !payload || payload.length === 0) return null;

        const income = payload.find((p) => p.dataKey === 'income');
        const expense = payload.find((p) => p.dataKey === 'expense');

        return (
            <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg text-xs">
                <p className="font-semibold text-gray-800 mb-1">{label}</p>
                {income && (
                    <p className="text-[11px] text-[#6F5BFF]">
                        Income: {currencySymbol}
                        {Number(income.value || 0).toFixed(2)}
                    </p>
                )}
                {expense && (
                    <p className="text-[11px] text-[#FFC75A] mt-1">
                        Expenses: {currencySymbol}
                        {Number(expense.value || 0).toFixed(2)}
                    </p>
                )}
            </div>
        );
    };

    return (
        <ResponsiveContainer width="100%" height="100%">
            <LineChart
                data={safeData}
                margin={{ top: 10, right: 20, left: 0, bottom: 0 }}
            >
                <CartesianGrid strokeDasharray="3 3" stroke="#F3F4F6" />
                <XAxis
                    dataKey="label"
                    tick={{ fontSize: 11, fill: '#9CA3AF' }}
                    axisLine={false}
                    tickLine={false}
                />
                <YAxis
                    tick={{ fontSize: 11, fill: '#9CA3AF' }}
                    axisLine={false}
                    tickLine={false}
                    tickFormatter={(value) =>
                        `${currencySymbol}${value.toFixed(0)}`
                    }
                />
                <Tooltip content={<CustomTooltip />} />
                <Line
                    type="monotone"
                    dataKey="income"
                    stroke="#6F5BFF" // purple
                    strokeWidth={3}
                    dot={false}
                    activeDot={{ r: 5 }}
                />
                <Line
                    type="monotone"
                    dataKey="expense"
                    stroke="#FFC75A" // yellow
                    strokeWidth={3}
                    dot={false}
                    activeDot={{ r: 5 }}
                />
            </LineChart>
        </ResponsiveContainer>
    );
};

export default DualLineChart;
