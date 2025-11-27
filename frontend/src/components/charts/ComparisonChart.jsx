import React from 'react';
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer,
} from 'recharts';

const ComparisonChart = ({ data = [], title = 'Monthly Comparison', currencySymbol = '$' }) => {
    const preparedData = (data || []).map((item) => {
        const current = Number(item.current_month_amount ?? item.current ?? 0);
        const previous = Number(item.previous_month_amount ?? item.previous ?? 0);
        const difference = Number(
            item.difference !== undefined ? item.difference : current - previous
        );

        return {
            ...item,
            category_name: item.category_name || item.category || item.name || 'Category',
            current_month_amount: current,
            previous_month_amount: previous,
            difference,
        };
    });

    const CustomTooltip = ({ active, payload, label }) => {
        if (!active || !payload || payload.length === 0) return null;

        const currentEntry = payload.find(
            (p) => p.dataKey === 'current_month_amount'
        );
        const previousEntry = payload.find(
            (p) => p.dataKey === 'previous_month_amount'
        );

        const current = currentEntry ? currentEntry.value : 0;
        const previous = previousEntry ? previousEntry.value : 0;
        const diff = current - previous;

        return (
            <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
                <p className="font-semibold text-gray-800 mb-2">{label}</p>
                <p className="text-sm text-blue-600">
                    This Month: {currencySymbol}
                    {current.toFixed(2)}
                </p>
                <p className="text-sm text-emerald-600">
                    Previous Month: {currencySymbol}
                    {previous.toFixed(2)}
                </p>
                <p
                    className={`text-sm mt-1 ${
                        diff >= 0 ? 'text-green-600' : 'text-red-600'
                    }`}
                >
                    Difference:{' '}
                    {diff >= 0 ? '+' : '-'}
                    {currencySymbol}
                    {Math.abs(diff).toFixed(2)}
                </p>
            </div>
        );
    };

    return (
        <div className="bg-white rounded-xl shadow-sm border border-strokes p-6 h-full">
            <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-semibold text-text">{title}</h3>
                <span className="text-xs text-metallic-gray">
                    Comparison of this month vs previous
                </span>
            </div>

            <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                        data={preparedData}
                        margin={{
                            top: 20,
                            right: 30,
                            left: 0,
                            bottom: 5,
                        }}
                    >
                        <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
                        <XAxis
                            dataKey="category_name"
                            stroke="#374151"
                            fontSize={12}
                            tickMargin={10}
                        />
                        <YAxis
                            stroke="#374151"
                            fontSize={12}
                            tickFormatter={(value) =>
                                `${currencySymbol}${value.toFixed(0)}`
                            }
                        />
                        <Tooltip content={<CustomTooltip />} />
                        <Legend
                            formatter={(value) => (
                                <span className="text-sm text-gray-700">{value}</span>
                            )}
                        />
                        <Bar
                            dataKey="current_month_amount"
                            fill="#4F46E5"
                            name="This Month"
                            radius={[4, 4, 0, 0]}
                            maxBarSize={40}
                        />
                        <Bar
                            dataKey="previous_month_amount"
                            fill="#10B981"
                            name="Previous Month"
                            radius={[4, 4, 0, 0]}
                            maxBarSize={40}
                        />
                    </BarChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
};

export default ComparisonChart;
