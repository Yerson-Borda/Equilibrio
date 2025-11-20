import React from 'react';
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer
} from 'recharts';

const DualLineChart = ({
                           data,
                           title,
                           xKey = 'label',
                           line1Key = 'income',
                           line1Name = 'Income',
                           line2Key = 'expense',
                           line2Name = 'Expense',
                           currencySymbol = '$',
                           headerRight = null
                       }) => {
    const CustomTooltip = ({ active, payload, label }) => {
        if (active && payload && payload.length) {
            return (
                <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
                    <p className="font-semibold text-gray-800">{label}</p>
                    {payload.map((entry, index) => (
                        <p
                            key={index}
                            className="text-sm"
                            style={{ color: entry.color }}
                        >
                            {entry.name}: {currencySymbol}
                            {entry.value.toFixed(2)}
                        </p>
                    ))}
                </div>
            );
        }
        return null;
    };

    return (
        <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-4">
                <h3 className="text-lg font-semibold text-text mb-2 sm:mb-0">
                    {title}
                </h3>
                {headerRight}
            </div>
            <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                    <LineChart
                        data={data}
                        margin={{
                            top: 5,
                            right: 30,
                            left: 20,
                            bottom: 5
                        }}
                    >
                        <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                        <XAxis
                            dataKey={xKey}
                            stroke="#374151"
                            fontSize={12}
                        />
                        <YAxis
                            stroke="#374151"
                            fontSize={12}
                            tickFormatter={(value) =>
                                `${currencySymbol}${value}`
                            }
                        />
                        <Tooltip content={<CustomTooltip />} />
                        <Legend />
                        <Line
                            type="monotone"
                            dataKey={line1Key}
                            stroke="#8884d8"
                            strokeWidth={2}
                            name={line1Name}
                            dot={{ r: 4 }}
                            activeDot={{ r: 6 }}
                            connectNulls
                        />
                        <Line
                            type="monotone"
                            dataKey={line2Key}
                            stroke="#82ca9d"
                            strokeWidth={2}
                            name={line2Name}
                            strokeDasharray="3 3"
                            dot={{ r: 4 }}
                            activeDot={{ r: 6 }}
                            connectNulls
                        />
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
};

export default DualLineChart;
