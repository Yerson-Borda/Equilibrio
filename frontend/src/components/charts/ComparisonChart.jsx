import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const ComparisonChart = ({ data, title }) => {
    const CustomTooltip = ({ active, payload, label }) => {
        if (active && payload && payload.length) {
            return (
                <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
                    <p className="font-semibold text-gray-800 mb-2">{label}</p>
                    {payload.map((entry, index) => (
                        <p key={index} className="text-sm" style={{ color: entry.color }}>
                            {entry.name}: ${entry.value.toFixed(2)}
                            {entry.dataKey === 'difference' && (
                                <span className={`ml-2 ${entry.value >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                                    ({entry.value >= 0 ? '+' : ''}{entry.value.toFixed(2)})
                                </span>
                            )}
                        </p>
                    ))}
                </div>
            );
        }
        return null;
    };

    return (
        <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
            <h3 className="text-lg font-semibold text-text mb-6">{title}</h3>
            <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                        data={data}
                        margin={{
                            top: 20,
                            right: 30,
                            left: 20,
                            bottom: 5,
                        }}
                    >
                        <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                        <XAxis
                            dataKey="category_name"
                            stroke="#374151"
                            fontSize={12}
                            angle={-45}
                            textAnchor="end"
                            height={80}
                        />
                        <YAxis
                            stroke="#374151"
                            fontSize={12}
                            tickFormatter={(value) => `$${value}`}
                        />
                        <Tooltip content={<CustomTooltip />} />
                        <Legend />
                        <Bar
                            dataKey="current_month_amount"
                            fill="#8884d8"
                            name="Current Month"
                            radius={[2, 2, 0, 0]}
                        />
                        <Bar
                            dataKey="previous_month_amount"
                            fill="#82ca9d"
                            name="Previous Month"
                            radius={[2, 2, 0, 0]}
                        />
                    </BarChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
};

export default ComparisonChart;