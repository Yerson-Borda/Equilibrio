import React from 'react';
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
} from 'recharts';

const MonthlyBarChart = ({ data, activeSeries = 'expense' }) => {
    // activeSeries is 'income' or 'expense'
    const isIncome = activeSeries === 'income';

    return (
        <ResponsiveContainer width="100%" height={320}>
            <BarChart
                data={data}
                margin={{ top: 20, right: 20, left: 0, bottom: 0 }}
            >
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="label" />
                <YAxis />
                <Tooltip
                    formatter={(value) => value.toFixed(2)}
                    labelStyle={{ fontWeight: 600 }}
                />
                <Bar
                    dataKey={activeSeries}
                    radius={[10, 10, 0, 0]}
                    fill={isIncome ? '#6366F1' : '#F97316'} // purple for income, orange for expense
                    maxBarSize={32}
                />
            </BarChart>
        </ResponsiveContainer>
    );
};

export default MonthlyBarChart;
