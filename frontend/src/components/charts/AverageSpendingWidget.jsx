import React, { useMemo } from "react";
import {
    ResponsiveContainer,
    RadarChart,
    PolarGrid,
    PolarAngleAxis,
    PolarRadiusAxis,
    Radar,
    Tooltip,
} from "recharts";
import { getCategoryIcon } from "../../utils/categoryIcons";

const safeNum = (v) => {
    const n = Number(v);
    return Number.isFinite(n) ? Math.abs(n) : 0;
};

export default function AverageSpendingWidget({ items = [], periodValue = "year" }) {
    const rows = useMemo(() => {
        return (Array.isArray(items) ? items : []).map((it) => {
            const name = it.category_name ?? it.name ?? it.category ?? "Other";
            const value = safeNum(it.average_amount ?? it.avg_amount ?? it.amount ?? it.value ?? it.total_amount);
            const iconKey = it.category_icon ?? it.icon ?? name;
            return { name, value, iconKey };
        });
    }, [items]);

    const radarData = useMemo(() => {
        const max = rows.reduce((m, r) => Math.max(m, r.value), 0) || 1;
        return rows.map((r) => ({
            category: r.name,
            percent: (r.value / max) * 100,
            amount: r.value,
        }));
    }, [rows]);

    const periodLabel =
        periodValue === "day" ? "Day" : periodValue === "month" ? "Month" : "Year";

    return (
        <div className="w-full">
            <div className="flex items-center justify-between mb-4">
                <h3 className="text-sm font-semibold text-text">Average spending per category</h3>
                <span className="text-xs text-metallic-gray">{periodLabel}</span>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
                {/* Left list */}
                <div className="lg:col-span-5 space-y-3">
                    {rows.map((r) => (
                        <div key={r.name} className="flex items-center justify-between">
                            <div className="flex items-center gap-3 min-w-0">
                                <div className="w-9 h-9 rounded-full bg-[#F5F7FA] flex items-center justify-center shrink-0">
                                    <img
                                        src={getCategoryIcon(r.iconKey)}
                                        alt={r.name}
                                        className="w-5 h-5"
                                        draggable={false}
                                    />
                                </div>
                                <div className="text-sm font-medium text-text truncate">{r.name}</div>
                            </div>
                            <div className="text-sm font-semibold text-text">${r.value.toFixed(0)}</div>
                        </div>
                    ))}

                    {rows.length === 0 && (
                        <div className="text-xs text-metallic-gray">No data for selected period.</div>
                    )}
                </div>

                {/* Right radar */}
                <div className="lg:col-span-7 h-[280px]">
                    <ResponsiveContainer width="100%" height="100%">
                        <RadarChart data={radarData}>
                            <PolarGrid />
                            <PolarAngleAxis dataKey="category" tick={{ fontSize: 11 }} />
                            <PolarRadiusAxis tick={{ fontSize: 10 }} />
                            <Radar dataKey="percent" stroke="#22C55E" fill="#22C55E" fillOpacity={0.25} />
                            <Tooltip formatter={(_v, _n, p) => `${p?.payload?.amount?.toFixed(2) ?? 0}`} />
                        </RadarChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </div>
    );
}
