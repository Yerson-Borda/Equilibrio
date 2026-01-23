import React from 'react';
import {
    PieChart,
    Pie,
    Cell,
    ResponsiveContainer,
    Tooltip,
    Legend,
} from 'recharts';

const COLORS = [
    '#0EA5E9', // Blue
    '#22C55E', // Green
    '#F97316', // Orange
    '#6366F1', // Purple
    '#EC4899', // Pink
    '#14B8A6', // Teal
    '#A855F7', // Violet
    '#FACC15', // Yellow
    '#EF4444', // Red
    '#8B5CF6', // Indigo
];

const ExpenseDistributionChart = ({ data = [] }) => {
    const chartData = Array.isArray(data) ? data.slice(0, 8).map((item, index) => ({
        name: item.category_name || item.name || item.category || `Category ${index + 1}`,
        value: Number(item.total_amount ?? item.amount ?? item.value ?? 0),
        color: COLORS[index % COLORS.length],
    })) : [];

    const total = chartData.reduce((sum, item) => sum + item.value, 0);

    const CustomTooltip = ({ active, payload }) => {
        if (!active || !payload || payload.length === 0) return null;

        const data = payload[0].payload;
        const percent = total ? (data.value / total) * 100 : 0;

        return (
            <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
                <p className="font-semibold text-gray-800 mb-1">{data.name}</p>
                <p className="text-sm text-gray-700">
                    Amount: ${data.value.toFixed(2)}
                </p>
                <p className="text-sm text-gray-500">
                    {percent.toFixed(1)}% of total
                </p>
            </div>
        );
    };

    const renderCustomizedLabel = ({
                                       cx,
                                       cy,
                                       midAngle,
                                       innerRadius,
                                       outerRadius,
                                       percent,
                                       index
                                   }) => {
        const RADIAN = Math.PI / 180;
        const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
        const x = cx + radius * Math.cos(-midAngle * RADIAN);
        const y = cy + radius * Math.sin(-midAngle * RADIAN);

        if (percent < 0.05) return null;

        return (
            <text
                x={x}
                y={y}
                fill="white"
                textAnchor={x > cx ? 'start' : 'end'}
                dominantBaseline="central"
                fontSize={11}
                fontWeight="bold"
            >
                {`${(percent * 100).toFixed(0)}%`}
            </text>
        );
    };

    if (chartData.length === 0) {
        return (
            <div className="h-72 flex flex-col items-center justify-center">
                <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mb-4">
                    <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                </div>
                <p className="text-sm text-gray-500">No expense data available</p>
            </div>
        );
    }

    return (
        <div className="h-full">
            <div className="flex items-center justify-between mb-4">
                <h3 className="text-sm font-semibold text-gray-900">Expense Distribution</h3>
                <span className="text-xs text-gray-500">
                    Total: ${total.toFixed(2)}
                </span>
            </div>

            <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                        <Pie
                            data={chartData}
                            dataKey="value"
                            nameKey="name"
                            cx="50%"
                            cy="50%"
                            innerRadius={60}
                            outerRadius={90}
                            paddingAngle={2}
                            label={renderCustomizedLabel}
                            labelLine={false}
                        >
                            {chartData.map((entry, index) => (
                                <Cell
                                    key={`cell-${index}`}
                                    fill={entry.color}
                                    stroke="#fff"
                                    strokeWidth={2}
                                />
                            ))}
                        </Pie>
                        <Tooltip content={<CustomTooltip />} />
                        <Legend
                            layout="vertical"
                            verticalAlign="middle"
                            align="right"
                            wrapperStyle={{
                                paddingLeft: '20px',
                                fontSize: '11px'
                            }}
                            formatter={(value, entry) => (
                                <span className="text-xs text-gray-700">{value}</span>
                            )}
                        />
                    </PieChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
};

export default ExpenseDistributionChart;