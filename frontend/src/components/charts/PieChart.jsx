import React from 'react';
import {
    PieChart,
    Pie,
    Cell,
    ResponsiveContainer,
    Legend,
    Tooltip,
} from 'recharts';

const COLORS = [
    '#4F46E5',
    '#10B981',
    '#F59E0B',
    '#EF4444',
    '#8B5CF6',
    '#06B6D4',
    '#EC4899',
    '#22C55E',
];

const CustomPieChart = ({ data, title = 'Spending by Category', currencySymbol = '$' }) => {
    // ---- make data safe ----
    let raw = [];

    if (Array.isArray(data)) {
        raw = data;
    } else if (data && typeof data === 'object') {
        // Try common shapes from APIs
        if (Array.isArray(data.categories)) raw = data.categories;
        else if (Array.isArray(data.items)) raw = data.items;
        else if (Array.isArray(data.results)) raw = data.results;
        else if (Array.isArray(data.data)) raw = data.data;
        else raw = Object.values(data); // fallback: best effort
    } else {
        raw = [];
    }

    if (!Array.isArray(raw)) {
        raw = [];
    }

    const chartData = raw.map((item) => ({
        name: item.category_name || item.category || item.name || 'Category',
        value: Number(item.total_amount ?? item.amount ?? 0),
        count: item.transaction_count ?? item.count ?? 0,
    }));

    const renderCustomizedLabel = ({
                                       cx,
                                       cy,
                                       midAngle,
                                       innerRadius,
                                       outerRadius,
                                       percent,
                                   }) => {
        const RADIAN = Math.PI / 180;
        const radius = innerRadius + (outerRadius - innerRadius) * 0.7;
        const x = cx + radius * Math.cos(-midAngle * RADIAN);
        const y = cy + radius * Math.sin(-midAngle * RADIAN);

        if (percent < 0.03) return null;

        return (
            <text
                x={x}
                y={y}
                fill="#111827"
                textAnchor={x > cx ? 'start' : 'end'}
                dominantBaseline="central"
                fontSize={11}
            >
                {(percent * 100).toFixed(0)}%
            </text>
        );
    };

    const CustomTooltip = ({ active, payload }) => {
        if (!active || !payload || payload.length === 0) return null;

        const { name, value, payload: original } = payload[0];

        return (
            <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
                <p className="font-semibold text-gray-800 mb-1">{name}</p>
                <p className="text-sm text-gray-700">
                    Total: {currencySymbol}
                    {value.toFixed(2)}
                </p>
                {original && original.count !== undefined && (
                    <p className="text-xs text-metallic-gray">
                        Transactions: {original.count}
                    </p>
                )}
            </div>
        );
    };

    if (!chartData.length) {
        return (
            <div className="bg-white rounded-xl shadow-sm border border-strokes p-6 h-full flex flex-col justify-center items-center">
                <h3 className="text-lg font-semibold text-text mb-2">{title}</h3>
                <p className="text-sm text-metallic-gray">
                    Not enough data to display category distribution yet.
                </p>
            </div>
        );
    }

    return (
        <div className="bg-white rounded-xl shadow-sm border border-strokes p-6 h-full">
            <h3 className="text-lg font-semibold text-text mb-4">{title}</h3>
            <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                        <Pie
                            data={chartData}
                            dataKey="value"
                            nameKey="name"
                            cx="50%"
                            cy="50%"
                            outerRadius={90}
                            innerRadius={50}
                            labelLine={false}
                            label={renderCustomizedLabel}
                        >
                            {chartData.map((entry, index) => (
                                <Cell
                                    key={`cell-${index}`}
                                    fill={COLORS[index % COLORS.length]}
                                />
                            ))}
                        </Pie>
                        <Tooltip content={<CustomTooltip />} />
                        <Legend
                            layout="vertical"
                            align="right"
                            verticalAlign="middle"
                            formatter={(value) => (
                                <span className="text-xs text-gray-700">{value}</span>
                            )}
                        />
                    </PieChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
};

export default CustomPieChart;
