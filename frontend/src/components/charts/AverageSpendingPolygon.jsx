// components/charts/AverageSpendingPolygon.jsx
import React, { useMemo } from "react";
import {
    RadarChart,
    PolarGrid,
    PolarAngleAxis,
    PolarRadiusAxis,
    Radar,
    ResponsiveContainer,
    Tooltip,
    Legend
} from "recharts";

const AverageSpendingPolygon = ({ items = [], period = "month" }) => {
    // Process data
    const chartData = useMemo(() => {
        if (!Array.isArray(items) || items.length === 0) {
            // Fallback data
            return [
                { subject: 'Food & Drinks', A: 500, fullMark: 2000 },
                { subject: 'Shopping', A: 1500, fullMark: 2000 },
                { subject: 'Housing', A: 1000, fullMark: 2000 },
                { subject: 'Transport', A: 100, fullMark: 2000 },
                { subject: 'Vehicle', A: 400, fullMark: 2000 },
                { subject: 'Entertainment', A: 500, fullMark: 2000 },
                { subject: 'Communication', A: 50, fullMark: 2000 },
                { subject: 'Investments', A: 2000, fullMark: 2000 },
                { subject: 'Others', A: 500, fullMark: 2000 }
            ];
        }

        // Find max value for scaling
        const maxValue = Math.max(...items.map(item =>
            Math.abs(Number(item.average_amount) || 0)
        ));

        return items.map(item => ({
            subject: item.category_name || 'Category',
            A: Math.abs(Number(item.average_amount) || 0),
            fullMark: maxValue * 1.2,
            percentage: ((Math.abs(Number(item.average_amount) || 0) / maxValue) * 100).toFixed(1)
        }));
    }, [items]);

    // Left panel list data
    const listData = useMemo(() => {
        return chartData.map(item => ({
            category: item.subject,
            amount: item.A,
            percentage: item.percentage
        })).sort((a, b) => b.amount - a.amount);
    }, [chartData]);

    const CustomTooltip = ({ active, payload, label }) => {
        if (!active || !payload || payload.length === 0) return null;

        return (
            <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
                <p className="font-semibold text-gray-800 mb-2">{label}</p>
                {payload.map((entry, index) => (
                    <div key={index} className="flex justify-between items-center mb-1">
                        <span className="text-sm text-gray-600">{entry.name}:</span>
                        <span className="text-sm font-semibold text-gray-900 ml-2">
                            ${entry.value.toFixed(2)}
                        </span>
                    </div>
                ))}
            </div>
        );
    };

    return (
        <div className="w-full">
            <div className="flex items-center justify-between mb-6">
                <h3 className="text-lg font-semibold text-gray-900">Average spending per category</h3>
                <span className="text-sm text-gray-600">
                    Per {period === 'day' ? 'Day' : period === 'month' ? 'Month' : 'Year'}
                </span>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                {/* Left column: Category list */}
                <div className="lg:col-span-5">
                    <div className="bg-white border border-gray-200 rounded-lg p-4">
                        <div className="space-y-4">
                            {listData.map((item, index) => (
                                <div
                                    key={index}
                                    className="flex items-center justify-between p-3 hover:bg-gray-50 rounded-lg transition-colors"
                                >
                                    <div className="flex items-center gap-3">
                                        <div className="relative">
                                            <div className="w-10 h-10 rounded-full bg-blue-50 flex items-center justify-center">
                                                <span className="text-blue-600 font-semibold">
                                                    {item.percentage}%
                                                </span>
                                            </div>
                                        </div>
                                        <div>
                                            <div className="font-medium text-gray-900">
                                                {item.category}
                                            </div>
                                            <div className="text-xs text-gray-500">
                                                Average spending
                                            </div>
                                        </div>
                                    </div>
                                    <div className="text-right">
                                        <div className="text-lg font-bold text-gray-900">
                                            ${item.amount.toFixed(2)}
                                        </div>
                                        <div className="text-xs text-gray-500">
                                            per {period}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Right column: Polygon/Radar chart */}
                <div className="lg:col-span-7">
                    <div className="bg-white border border-gray-200 rounded-lg p-6 h-full">
                        <div className="h-[320px]">
                            <ResponsiveContainer width="100%" height="100%">
                                <RadarChart cx="50%" cy="50%" outerRadius="80%" data={chartData}>
                                    <PolarGrid
                                        gridType="polygon"
                                        stroke="#E5E7EB"
                                        strokeWidth={1}
                                    />
                                    <PolarAngleAxis
                                        dataKey="subject"
                                        tick={{
                                            fontSize: 11,
                                            fill: '#4B5563',
                                            fontWeight: 500
                                        }}
                                    />
                                    <PolarRadiusAxis
                                        angle={30}
                                        tick={{ fontSize: 10, fill: '#9CA3AF' }}
                                        axisLine={{ stroke: '#E5E7EB' }}
                                    />
                                    <Radar
                                        name="Average Spending"
                                        dataKey="A"
                                        stroke="#3B82F6"
                                        fill="#3B82F6"
                                        fillOpacity={0.2}
                                        strokeWidth={2}
                                        dot={{
                                            r: 4,
                                            fill: "#3B82F6",
                                            stroke: "#fff",
                                            strokeWidth: 2
                                        }}
                                    />
                                    <Tooltip content={<CustomTooltip />} />
                                    <Legend
                                        verticalAlign="bottom"
                                        height={36}
                                        wrapperStyle={{ fontSize: '12px' }}
                                    />
                                </RadarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AverageSpendingPolygon;