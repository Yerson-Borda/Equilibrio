import React, { useMemo } from "react";
import { ResponsiveContainer, PieChart, Pie, Cell, Tooltip } from "recharts";
import { getCategoryIcon } from "../../utils/categoryIcons";

const DEFAULT_COLORS = ["#4C6FFF", "#22C55E", "#F97316"];

const safeNum = (v) => {
    const n = Number(v);
    return Number.isFinite(n) ? Math.abs(n) : 0;
};

export default function Top3CategoriesWidget({ items = [] }) {
    const rows = useMemo(() => {
        return (Array.isArray(items) ? items : []).slice(0, 3).map((it, idx) => {
            const name = it.category_name ?? it.name ?? "Other";
            const value = safeNum(it.total_amount ?? it.amount ?? it.value);
            const color = it.category_color ?? it.color ?? DEFAULT_COLORS[idx % DEFAULT_COLORS.length];
            const iconKey = it.category_icon ?? it.icon ?? name;

            // optional: trend if backend provides
            const trend = it.percentage_change ?? it.change_percent ?? it.trend_percent ?? null;

            return { name, value, color, iconKey, trend };
        });
    }, [items]);

    const total = useMemo(() => rows.reduce((s, r) => s + r.value, 0), [rows]);

    return (
        <div className="w-full">
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
                {/* LEFT: legend list */}
                <div className="lg:col-span-5">
                    <h3 className="text-sm font-semibold text-text mb-4">Expense Distribution</h3>

                    <div className="space-y-3">
                        {rows.map((r, idx) => {
                            const pct = total ? (r.value / total) * 100 : 0;
                            const trendUp = typeof r.trend === "number" ? r.trend >= 0 : null;

                            return (
                                <div key={r.name + idx} className="flex items-center justify-between">
                                    <div className="flex items-center gap-3 min-w-0">
                                        <div className="w-3 h-3 rounded-full" style={{ backgroundColor: r.color }} />

                                        <div className="flex items-center gap-2 min-w-0">
                                            <div
                                                className="w-9 h-9 rounded-full flex items-center justify-center shrink-0"
                                                style={{ backgroundColor: r.color + "22" }}
                                            >
                                                <img
                                                    src={getCategoryIcon(r.iconKey)}
                                                    alt={r.name}
                                                    className="w-5 h-5"
                                                    draggable={false}
                                                />
                                            </div>

                                            <div className="min-w-0">
                                                <div className="text-sm font-medium text-text truncate">{r.name}</div>
                                                <div className="text-[11px] text-metallic-gray">{pct.toFixed(1)}%</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="flex items-center gap-3">
                                        <div className="text-sm font-semibold text-text">${r.value.toFixed(0)}</div>
                                        {typeof r.trend === "number" && (
                                            <div className={`text-xs font-semibold ${trendUp ? "text-green-600" : "text-red-500"}`}>
                                                {trendUp ? "↑" : "↓"} {Math.abs(r.trend).toFixed(1)}%
                                            </div>
                                        )}
                                    </div>
                                </div>
                            );
                        })}

                        {rows.length === 0 && (
                            <div className="text-xs text-metallic-gray">No expense data for this month.</div>
                        )}
                    </div>
                </div>

                {/* RIGHT: donut */}
                <div className="lg:col-span-7">
                    <div className="h-[280px] relative">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={rows}
                                    dataKey="value"
                                    nameKey="name"
                                    innerRadius={78}
                                    outerRadius={115}
                                    paddingAngle={3}
                                >
                                    {rows.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={entry.color || DEFAULT_COLORS[index % DEFAULT_COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip formatter={(v) => safeNum(v).toFixed(2)} />
                            </PieChart>
                        </ResponsiveContainer>

                        <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                            <div className="text-xs text-metallic-gray">Total</div>
                            <div className="text-2xl font-bold text-text">${total.toFixed(0)}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
