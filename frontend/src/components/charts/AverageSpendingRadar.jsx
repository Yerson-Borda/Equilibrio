import React, { useMemo } from "react";
import {
    Radar,
    RadarChart,
    PolarGrid,
    PolarAngleAxis,
    PolarRadiusAxis,
    ResponsiveContainer,
    Tooltip,
    Legend
} from "recharts";

const AverageSpendingRadar = ({ items = [], period = "month" }) => {
    // Process data for radar chart
    const radarData = useMemo(() => {
        if (!Array.isArray(items) || items.length === 0) {
            // Fallback sample data matching your screenshot
            return [
                { category: "Food & Drinks", amount: 500, percentage: 39.0 },
                { category: "Shopping", amount: 1500, percentage: 22.9 },
                { category: "Housing", amount: 1000, percentage: 15.27 },
                { category: "Transportation", amount: 100, percentage: 7.64 },
                { category: "Vehicle", amount: 400, percentage: 6.11 },
                { category: "Entertainment", amount: 500, percentage: 5.30 },
                { category: "Communication", amount: 50, percentage: 1.53 },
                { category: "Investments", amount: 2000, percentage: 0.76 },
                { category: "Others", amount: 500, percentage: 0.00 }
            ];
        }

        // Convert API data to radar format
        return items.map((item, index) => ({
            category: item.category_name || `Category ${index + 1}`,
            amount: Math.abs(Number(item.average_amount) || 0),
            percentage: parseFloat(item.percentage) || 0,
            fullMark: Math.max(...items.map(i => Math.abs(Number(i.average_amount) || 0))) * 1.2
        }));
    }, [items]);

    const periodLabel = period === "day" ? "Day" : period === "month" ? "Month" : "Year";

    const CustomTooltip = ({ active, payload }) => {
        if (!active || !payload || payload.length === 0) return null;

        const data = payload[0].payload;
        return (
            <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg min-w-[180px]">
                <div className="mb-2">
                    <div className="font-semibold text-gray-800 text-sm">{data.category}</div>
                </div>
                <div className="space-y-1">
                    <div className="flex justify-between text-sm">
                        <span className="text-gray-600">Amount:</span>
                        <span className="font-semibold text-gray-900">${data.amount.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                        <span className="text-gray-600">Percentage:</span>
                        <span className="font-semibold text-gray-900">{data.percentage.toFixed(1)}%</span>
                    </div>
                    <div className="flex justify-between text-sm">
                        <span className="text-gray-600">Period:</span>
                        <span className="font-semibold text-gray-900">{periodLabel}</span>
                    </div>
                </div>
            </div>
        );
    };

    // Calculate max value for better scaling
    const maxValue = useMemo(() => {
        const amounts = radarData.map(d => d.amount);
        return Math.max(...amounts) * 1.2;
    }, [radarData]);

    return (
        <div className="w-full">
            <div className="flex items-center justify-between mb-4">
                <h3 className="text-sm font-semibold text-text">Average spending per category</h3>
                <div className="flex items-center gap-3">
                    <span className="text-xs text-metallic-gray">{periodLabel}</span>
                    <div className="flex items-center gap-2 text-xs">
                        <div className="flex items-center gap-1">
                            <div className="w-2 h-2 rounded-full bg-blue-500"></div>
                            <span className="text-gray-600">Average</span>
                        </div>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
                {/* Left: Category list with amounts */}
                <div className="lg:col-span-5 space-y-4">
                    <div className="bg-gray-50 rounded-lg p-4">
                        <h4 className="text-xs font-semibold text-gray-700 mb-3 uppercase tracking-wider">
                            Average per {periodLabel.toLowerCase()}
                        </h4>
                        <div className="space-y-3">
                            {radarData.map((item, index) => (
                                <div key={index} className="flex items-center justify-between">
                                    <div className="flex items-center gap-3">
                                        <div className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center">
                                            <span className="text-xs font-semibold text-gray-700">
                                                {item.category.charAt(0)}
                                            </span>
                                        </div>
                                        <div>
                                            <div className="text-sm font-medium text-gray-900">
                                                {item.category}
                                            </div>
                                            <div className="text-xs text-gray-500">
                                                {item.percentage.toFixed(1)}%
                                            </div>
                                        </div>
                                    </div>
                                    <div className="text-right">
                                        <div className="text-sm font-bold text-gray-900">
                                            ${item.amount.toFixed(2)}
                                        </div>
                                        <div className="text-xs text-gray-500">
                                            per {periodLabel.toLowerCase()}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Right: Radar chart */}
                <div className="lg:col-span-7">
                    <div className="h-[320px]">
                        <ResponsiveContainer width="100%" height="100%">
                            <RadarChart outerRadius="80%" data={radarData}>
                                <PolarGrid
                                    stroke="#E5E7EB"
                                    strokeWidth={1}
                                    radialLines={true}
                                />
                                <PolarAngleAxis
                                    dataKey="category"
                                    tick={{
                                        fontSize: 11,
                                        fill: '#6B7280',
                                        fontWeight: 500
                                    }}
                                    axisLine={{ stroke: '#E5E7EB', strokeWidth: 1 }}
                                />
                                <PolarRadiusAxis
                                    angle={30}
                                    domain={[0, maxValue]}
                                    tick={{
                                        fontSize: 10,
                                        fill: '#9CA3AF'
                                    }}
                                    axisLine={{ stroke: '#E5E7EB', strokeWidth: 1 }}
                                />
                                <Radar
                                    name="Average Spending"
                                    dataKey="amount"
                                    stroke="#3B82F6"
                                    fill="#3B82F6"
                                    fillOpacity={0.3}
                                    strokeWidth={2}
                                    dot={{
                                        r: 4,
                                        fill: "#3B82F6",
                                        stroke: "#fff",
                                        strokeWidth: 2
                                    }}
                                    activeDot={{
                                        r: 6,
                                        fill: "#1D4ED8",
                                        stroke: "#fff",
                                        strokeWidth: 3
                                    }}
                                />
                                <Tooltip content={<CustomTooltip />} />
                                <Legend
                                    iconType="circle"
                                    iconSize={8}
                                    wrapperStyle={{ fontSize: '12px', paddingTop: '10px' }}
                                />
                            </RadarChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AverageSpendingRadar;