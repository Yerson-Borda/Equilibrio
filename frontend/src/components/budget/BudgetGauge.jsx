import React from 'react';

const BudgetGauge = ({
                         budget = 0,
                         spent = 0,
                         currencySymbol = '$',
                         month,
                         monthLabel, // allow DashboardContent to pass monthLabel without breaking
                     }) => {
    const totalBudget = Number(budget) || 0;
    const totalSpent = Number(spent) || 0;

    const percentage =
        totalBudget > 0 ? Math.min(100, (totalSpent / totalBudget) * 100) : 0;

    // Arc angles for knob position: -180 (left) to 0 (right)
    const startAngle = -180;
    const endAngle = startAngle + (percentage / 100) * 180;

    const computedMonthLabel = monthLabel
        ? monthLabel
        : month
            ? new Date(`${month}-01`).toLocaleDateString('en-US', { month: 'long' })
            : 'April';

    const formatMoney = (v) => `${currencySymbol}${Number(v || 0).toFixed(2)}`;

    const getKnobPosition = (radius, angleDeg) => {
        const rad = (Math.PI / 180) * angleDeg;
        const cx = 120;
        const cy = 120;
        return {
            x: cx + radius * Math.cos(rad),
            y: cy + radius * Math.sin(rad),
        };
    };

    const knob = getKnobPosition(80, endAngle);

    return (
        <div className="bg-white rounded-xl shadow-sm border border-strokes p-6 h-full flex flex-col justify-between">
            <div className="flex justify-between items-start mb-4">
                <div>
                    <h3 className="text-lg font-semibold text-text">Budget Vs Expense</h3>
                    <p className="text-xs text-metallic-gray">
                        From 01 – 30 {computedMonthLabel}
                    </p>
                </div>
            </div>

            <div className="flex-1 flex flex-col items-center justify-center">
                <svg width="240" height="130" viewBox="0 0 240 130">
                    {/* Base arc (track) */}
                    <path
                        d="M40,120 A80,80 0 0,1 200,120"
                        fill="none"
                        stroke="#E5E7EB"
                        strokeWidth="14"
                        strokeLinecap="round"
                    />

                    {/* Progress arc (normalized to 0..100 using pathLength) */}
                    <path
                        d="M40,120 A80,80 0 0,1 200,120"
                        fill="none"
                        stroke="url(#gaugeGradient)"
                        strokeWidth="14"
                        strokeLinecap="round"
                        pathLength="100"
                        strokeDasharray="100"
                        strokeDashoffset={100 - percentage}
                    />

                    <defs>
                        <linearGradient id="gaugeGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                            <stop offset="0%" stopColor="#4F46E5" />
                            <stop offset="100%" stopColor="#60A5FA" />
                        </linearGradient>
                    </defs>

                    {/* Knob */}
                    {totalBudget > 0 && (
                        <circle
                            cx={knob.x}
                            cy={knob.y}
                            r="7"
                            fill="#FFFFFF"
                            stroke="#4F46E5"
                            strokeWidth="3"
                        />
                    )}
                </svg>

                <div className="-mt-4 text-center">
                    <p className="text-2xl font-bold text-text">{formatMoney(totalSpent)}</p>
                    <p className="text-xs text-metallic-gray mt-1">
                        of <span className="font-semibold">{formatMoney(totalBudget)}</span>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default BudgetGauge;
