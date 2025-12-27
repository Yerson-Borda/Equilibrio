import React from 'react';
import {
    PieChart,
    Pie,
    Cell,
    ResponsiveContainer,
    Tooltip,
} from 'recharts';

const COLORS = [
    '#0EA5E9',
    '#22C55E',
    '#F97316',
    '#6366F1',
    '#EC4899',
    '#14B8A6',
    '#A855F7',
    '#FACC15',
];

const ExpenseDistributionChart = ({ data }) => {
    const total = data.reduce((sum, d) => sum + d.value, 0);

    return (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Donut chart */}
            <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                        <Pie
                            data={data}
                            dataKey="value"
                            nameKey="name"
                            innerRadius={70}
                            outerRadius={110}
                            paddingAngle={3}
                        >
                            {data.map((entry, index) => (
                                <Cell
                                    key={`cell-${index}`}
                                    fill={COLORS[index % COLORS.length]}
                                />
                            ))}
                        </Pie>
                        <Tooltip
                            formatter={(value) => value.toFixed(2)}
                            labelFormatter={(label) => `${label}`}
                        />
                    </PieChart>
                </ResponsiveContainer>
                <p className="mt-4 text-sm text-metallic-gray text-center">
                    Expense Distribution (by category)
                </p>
            </div>

            {/* Breakdown card (like 4.3 right side) */}
            <div className="bg-[#F5F7FA] rounded-xl p-4 flex flex-col justify-between">
                <div className="flex items-center justify-between mb-4">
                    <h3 className="text-sm font-semibold text-text">
                        Expenses Breakdown
                    </h3>
                    <span className="text-xs text-metallic-gray">
            {total ? 'Total ' + total.toFixed(2) : 'No data'}
          </span>
                </div>

                <div className="space-y-2 overflow-y-auto max-h-56">
                    {data.map((item, index) => {
                        const percent = total ? (item.value / total) * 100 : 0;
                        return (
                            <div
                                key={item.name + index}
                                className="flex items-center justify-between text-sm"
                            >
                                <div className="flex items-center space-x-2">
                  <span
                      className="w-3 h-3 rounded-full"
                      style={{
                          backgroundColor: COLORS[index % COLORS.length],
                      }}
                  />
                                    <span className="text-text">{item.name}</span>
                                </div>
                                <div className="text-right">
                                    <p className="text-text font-medium">
                                        {item.value.toFixed(2)}
                                    </p>
                                    <p className="text-[11px] text-metallic-gray">
                                        {percent.toFixed(1)}%
                                    </p>
                                </div>
                            </div>
                        );
                    })}
                    {data.length === 0 && (
                        <p className="text-xs text-metallic-gray">
                            No expense data available for the selected period.
                        </p>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ExpenseDistributionChart;
