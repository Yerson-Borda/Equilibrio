import React, { useEffect, useMemo, useState } from "react";
import Sidebar from "../components/layout/Sidebar";
import Header from "../components/layout/Header";

import ComparisonChart from "../components/charts/ComparisonChart";
import { apiService } from "../services/api";
import { getCurrencySymbol } from "../config/currencies";
import { getCategoryIcon } from "../utils/categoryIcons";

const GoalsPage = () => {
    const [loading, setLoading] = useState(true);

    // API data
    const [budget, setBudget] = useState(null);
    const [summary, setSummary] = useState(null);
    const [monthlyComparison, setMonthlyComparison] = useState([]);
    const [prevMonthlyComparison, setPrevMonthlyComparison] = useState([]);
    const [currency, setCurrency] = useState("USD");
    const [totalBalance, setTotalBalance] = useState(0);
    const [categoryLimits, setCategoryLimits] = useState([]);

    // Monthly expenses by category (transaction history)
    const [categoryExpenses, setCategoryExpenses] = useState([]);

    // Pagination
    const [limitsPage, setLimitsPage] = useState(0);

    // Editing states
    const [editingMonthly, setEditingMonthly] = useState(false);
    const [editingDaily, setEditingDaily] = useState(false);
    const [monthlyDraft, setMonthlyDraft] = useState("");
    const [dailyDraft, setDailyDraft] = useState("");
    const [savingBudget, setSavingBudget] = useState(false);

    // Adjust goal modal UI (frontend)
    const [showAdjustGoal, setShowAdjustGoal] = useState(false);

    // Notification toggle (local)
    const [budgetNotificationsEnabled, setBudgetNotificationsEnabled] = useState(() => {
        const v = localStorage.getItem("budget_notifications_enabled");
        return v === null ? true : v === "true";
    });

    useEffect(() => {
        localStorage.setItem("budget_notifications_enabled", String(budgetNotificationsEnabled));
    }, [budgetNotificationsEnabled]);

    // ---- helpers ----
    const parseNumber = (value) => {
        if (value === null || value === undefined) return 0;
        const n = typeof value === "string" ? parseFloat(value) : Number(value);
        return Number.isNaN(n) ? 0 : n;
    };

    const clampPercent = (p) => {
        const n = Number(p);
        if (!Number.isFinite(n)) return 0;
        return Math.max(0, Math.min(100, n));
    };

    // EXACT SAME extraction strategy as Dashboard
    const extractTotalBalance = (balanceData) => {
        if (balanceData == null) return 0;

        if (typeof balanceData === "number") return balanceData;
        if (typeof balanceData === "string") return parseNumber(balanceData);

        if (typeof balanceData === "object") {
            if ("total_balance" in balanceData) return parseNumber(balanceData.total_balance);
            if ("balance" in balanceData) return parseNumber(balanceData.balance);
        }

        return 0;
    };

    const symbol = useMemo(() => getCurrencySymbol(currency), [currency]);

    const formatMoney = (v, decimals = 2) => {
        const num = parseNumber(v);
        return `${symbol}${num.toLocaleString(undefined, {
            minimumFractionDigits: decimals,
            maximumFractionDigits: decimals,
        })}`;
    };

    const getMonthString = (d) => {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, "0");
        return `${y}-${m}`;
    };

    const getPrevMonthString = (yyyyMm) => {
        const [yStr, mStr] = yyyyMm.split("-");
        const y = Number(yStr);
        const m = Number(mStr);
        if (!Number.isFinite(y) || !Number.isFinite(m)) return yyyyMm;
        const date = new Date(y, m - 1, 1);
        date.setMonth(date.getMonth() - 1);
        return getMonthString(date);
    };

    // month string YYYY-MM
    const today = new Date();
    const thisMonth = getMonthString(today);
    const prevMonth = getPrevMonthString(thisMonth);

    const firstDay = `${thisMonth}-01`;
    const lastDay = `${thisMonth}-31`;

    const monthLabel = useMemo(() => {
        const y = budget?.year;
        const m = budget?.month;
        if (!y || !m) return "This month";
        const d = new Date(y, m - 1, 1);
        return d.toLocaleDateString("en-US", { month: "long", year: "numeric" });
    }, [budget]);

    const monthShort = useMemo(() => {
        const y = budget?.year;
        const m = budget?.month;
        if (!y || !m) return "This month";
        const d = new Date(y, m - 1, 1);
        return d.toLocaleDateString("en-US", { month: "long" });
    }, [budget]);

    const sumSavedFromComparison = (arr) => {
        // saved = income - expense (or d.saved if backend provides)
        return (arr || []).reduce((acc, d) => {
            const income = parseNumber(d?.income);
            const expense = parseNumber(d?.expense);
            const saved = parseNumber(d?.saved ?? (income - expense));
            return acc + saved;
        }, 0);
    };

    // ---- fetch all ----
    const loadAll = async () => {
        setLoading(true);

        const results = await Promise.allSettled([
            apiService.getCurrentUser(),
            apiService.getCurrentBudget(),
            apiService.getCurrentSummary(),
            apiService.getMonthlyComparison(thisMonth),
            apiService.getMonthlyComparison(prevMonth),          // ✅ for ↗ vs last month
            apiService.getUserTotalBalance(),
            apiService.getCategoryLimitsOverview(),
            apiService.getCategorySummary(firstDay, lastDay),    // ✅ spending from transaction history
        ]);

        const getVal = (idx, fallback) =>
            results[idx].status === "fulfilled" ? results[idx].value : fallback;

        const user = getVal(0, null);
        const budg = getVal(1, null);
        const summ = getVal(2, null);
        const comp = getVal(3, []);
        const prevComp = getVal(4, []);
        const balanceData = getVal(5, null);
        const limits = getVal(6, []);
        const catSummary = getVal(7, null);

        // currency
        setCurrency(user?.default_currency || summ?.currency || "USD");

        // budget + drafts
        setBudget(budg || null);
        setMonthlyDraft(budg?.monthly_limit ?? "");
        setDailyDraft(budg?.daily_limit ?? "");

        // summary + charts
        setSummary(summ || null);
        setMonthlyComparison(Array.isArray(comp) ? comp : []);
        setPrevMonthlyComparison(Array.isArray(prevComp) ? prevComp : []);

        // total balance (available balance)
        setTotalBalance(extractTotalBalance(balanceData));

        // limits
        setCategoryLimits(Array.isArray(limits) ? limits : []);
        setLimitsPage(0);

        // monthly expenses by category
        const expenses = catSummary && Array.isArray(catSummary.expenses) ? catSummary.expenses : [];
        setCategoryExpenses(expenses);

        setLoading(false);
    };

    useEffect(() => {
        loadAll();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // ---- derived ----
    const totalSaved = parseNumber(summary?.total_saved);
    const totalIncome = parseNumber(summary?.total_income);
    const totalSpent = parseNumber(summary?.total_spent);

    // available balance must match dashboard total balance
    const availableBalance = totalBalance;

    const monthlyLimit = parseNumber(budget?.monthly_limit);
    const monthlySpent = parseNumber(budget?.monthly_spent);
    const dailyLimit = parseNumber(budget?.daily_limit);
    const dailySpent = parseNumber(budget?.daily_spent);

    const monthlyPercent = clampPercent(monthlyLimit > 0 ? (monthlySpent / monthlyLimit) * 100 : 0);
    const dailyPercent = clampPercent(dailyLimit > 0 ? (dailySpent / dailyLimit) * 100 : 0);

    // prevent “blank” bar when spent > 0
    const progressWidth = (percent, spentValue) => {
        const p = clampPercent(percent);
        if (spentValue > 0 && p <= 0) return "6px";
        if (p <= 0) return "0%";
        return `max(${p}%, 6px)`;
    };

    // Goal gauge values (keep your existing logic)
    const goalTargetRaw = totalIncome > 0 ? totalIncome - totalSpent : totalSaved;
    const goalTarget = goalTargetRaw > 0 ? goalTargetRaw : 1000;
    const goalProgress = goalTarget > 0 ? Math.min(1, totalSaved / goalTarget) : 0;

    // ✅ Saving Summary must show “how much I saved”
    // Make the chart plot ONLY the saved series:
    const savedOnlyComparison = useMemo(() => {
        return (monthlyComparison || []).map((d) => {
            const income = parseNumber(d?.income);
            const expense = parseNumber(d?.expense);
            const saved = parseNumber(d?.saved ?? (income - expense));
            // Put saved into "income" series; set expense to null so it doesn’t render a second series
            return { ...d, income: saved, expense: null };
        });
    }, [monthlyComparison]);

    // ✅ ↗ % vs last month computed from SAVED totals
    const savedThisMonth = useMemo(() => sumSavedFromComparison(monthlyComparison), [monthlyComparison]);
    const savedLastMonth = useMemo(() => sumSavedFromComparison(prevMonthlyComparison), [prevMonthlyComparison]);

    const savedDeltaPct = useMemo(() => {
        if (savedLastMonth === 0) {
            if (savedThisMonth === 0) return 0;
            return 100; // from 0 to something
        }
        return ((savedThisMonth - savedLastMonth) / Math.abs(savedLastMonth)) * 100;
    }, [savedThisMonth, savedLastMonth]);

    const savedDeltaIsUp = savedDeltaPct >= 0;

    // ---- pagination ----
    const pageSize = 5;
    const totalPages = Math.max(1, Math.ceil(categoryLimits.length / pageSize));
    const currentLimits = categoryLimits.slice(limitsPage * pageSize, limitsPage * pageSize + pageSize);
    const goPrevLimits = () => setLimitsPage((p) => (p <= 0 ? totalPages - 1 : p - 1));
    const goNextLimits = () => setLimitsPage((p) => (p >= totalPages - 1 ? 0 : p + 1));

    // spent map by category_id from transaction history (expenses)
    const spentByCategoryId = useMemo(() => {
        const m = new Map();
        for (const e of categoryExpenses || []) {
            const id = Number(e?.category_id);
            if (!Number.isFinite(id)) continue;
            m.set(id, parseNumber(e?.total_amount ?? 0));
        }
        return m;
    }, [categoryExpenses]);

    // ✅ Limits rows: Budget + Remaining, where Remaining uses transaction history spent
    const currentLimitRows = useMemo(() => {
        return (currentLimits || []).map((item) => {
            const id = Number(item.category_id);
            const limit = parseNumber(item.monthly_limit);
            const spent = spentByCategoryId.get(id) ?? 0;
            return {
                ...item,
                __remaining: limit - spent,
            };
        });
    }, [currentLimits, spentByCategoryId]);

    // ---- budget updates ----
    const commitBudgetUpdate = async (payload) => {
        try {
            setSavingBudget(true);

            await apiService.updateCurrentBudget(payload);

            // refresh budget + month category summary (so bars + remaining update)
            const [freshBudget, catSummary] = await Promise.all([
                apiService.getCurrentBudget(),
                apiService.getCategorySummary(firstDay, lastDay),
            ]);

            setBudget(freshBudget || null);
            setMonthlyDraft(freshBudget?.monthly_limit ?? "");
            setDailyDraft(freshBudget?.daily_limit ?? "");

            const expenses = catSummary && Array.isArray(catSummary.expenses) ? catSummary.expenses : [];
            setCategoryExpenses(expenses);

            window.dispatchEvent(new Event("budget_updated"));
        } catch (err) {
            console.error("Failed to update budget:", err);
            alert(err?.message || "Failed to update budget");
            setMonthlyDraft(budget?.monthly_limit ?? "");
            setDailyDraft(budget?.daily_limit ?? "");
        } finally {
            setSavingBudget(false);
        }
    };

    const submitMonthlyDraft = async () => {
        const v = Number(monthlyDraft);
        if (!Number.isFinite(v) || v <= 0) return alert("Please enter a valid monthly budget.");
        setEditingMonthly(false);
        await commitBudgetUpdate({ monthly_limit: v });
    };

    const submitDailyDraft = async () => {
        const v = Number(dailyDraft);
        if (!Number.isFinite(v) || v <= 0) return alert("Please enter a valid daily limit.");
        setEditingDaily(false);
        await commitBudgetUpdate({ daily_limit: v });
    };

    // ---- tiny icons ----
    const PencilIcon = ({ className = "w-4 h-4" }) => (
        <svg className={className} viewBox="0 0 24 24" fill="none">
            <path d="M12 20h9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            <path
                d="M16.5 3.5a2.12 2.12 0 0 1 3 3L8 18l-4 1 1-4 11.5-11.5Z"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinejoin="round"
            />
        </svg>
    );

    const ArrowIcon = ({ dir = "right" }) => (
        <svg className={`w-4 h-4 ${dir === "left" ? "rotate-180" : ""}`} viewBox="0 0 24 24" fill="none">
            <path d="M9 18l6-6-6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
    );

    const InfoIcon = () => (
        <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none">
            <path d="M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Z" stroke="currentColor" strokeWidth="2" />
            <path d="M12 16v-5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            <path d="M12 8h.01" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
        </svg>
    );

    const StripedProgress = ({ percent, spentValue }) => (
        <div className="w-full h-[14px] rounded-full bg-white/10 overflow-hidden">
            <div
                className="h-full rounded-full"
                style={{
                    width: progressWidth(percent, spentValue),
                    backgroundImage:
                        "repeating-linear-gradient(135deg, rgba(255,255,255,0.85) 0 8px, rgba(255,255,255,0.35) 8px 16px)",
                }}
            />
        </div>
    );

    if (loading) {
        return (
            <div className="min-h-screen flex bg-background">
                <Sidebar activeItem="goals" />
                <div className="flex-1 ml-64 flex flex-col">
                    <Header />
                    <main className="p-8 text-text">Loading goals...</main>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen flex bg-background">
            <Sidebar activeItem="goals" />

            <div className="flex-1 ml-64 flex flex-col">
                <Header />

                <main className="p-8">
                    <h1 className="text-2xl font-semibold text-text mb-6">Goals</h1>

                    {/* TOP: goal overview */}
                    <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-8">
                        {/* Saving Goal */}
                        <div className="bg-white border border-strokes rounded-xl p-6 shadow-sm">
                            <div className="flex items-start justify-between">
                                <h2 className="text-base font-semibold text-text">Saving Goal</h2>
                                <select className="text-sm bg-gray-50 border border-strokes rounded-lg px-3 py-2 text-metallic-gray">
                                    <option>01 April - 30 April</option>
                                    <option>This Month</option>
                                </select>
                            </div>

                            <div className="mt-6 flex items-center justify-between gap-6">
                                <div className="flex-1">
                                    <div className="flex items-start gap-3">
                                        <div className="text-metallic-gray mt-1">🏆</div>
                                        <div>
                                            <p className="text-xs text-metallic-gray">Target Achieved</p>
                                            <p className="text-xl font-bold text-text">{formatMoney(totalSaved, 0)}</p>
                                        </div>
                                    </div>

                                    <div className="flex items-start gap-3 mt-4">
                                        <div className="text-metallic-gray mt-1">🎯</div>
                                        <div>
                                            <p className="text-xs text-metallic-gray">This month Target</p>
                                            <p className="text-base font-semibold text-text">{formatMoney(goalTarget, 0)}</p>
                                        </div>
                                    </div>
                                </div>

                                {/* gauge */}
                                <div className="w-[160px] flex flex-col items-center">
                                    <svg width="160" height="110" viewBox="0 0 160 110">
                                        <path
                                            d="M20 90 A60 60 0 0 1 140 90"
                                            fill="none"
                                            stroke="#E5E7EB"
                                            strokeWidth="12"
                                            strokeLinecap="round"
                                        />
                                        <defs>
                                            <linearGradient id="goalGrad" x1="0" y1="0" x2="1" y2="0">
                                                <stop offset="0%" stopColor="#93C5FD" />
                                                <stop offset="100%" stopColor="#4F46E5" />
                                            </linearGradient>
                                        </defs>

                                        {(() => {
                                            const start = Math.PI;
                                            const end = Math.PI + Math.PI * goalProgress;
                                            const cx = 80,
                                                cy = 90,
                                                r = 60;
                                            const x1 = cx + r * Math.cos(start);
                                            const y1 = cy + r * Math.sin(start);
                                            const x2 = cx + r * Math.cos(end);
                                            const y2 = cy + r * Math.sin(end);
                                            const largeArc = goalProgress > 0.5 ? 1 : 0;

                                            return (
                                                <path
                                                    d={`M ${x1} ${y1} A ${r} ${r} 0 ${largeArc} 1 ${x2} ${y2}`}
                                                    fill="none"
                                                    stroke="url(#goalGrad)"
                                                    strokeWidth="12"
                                                    strokeLinecap="round"
                                                />
                                            );
                                        })()}

                                        <text x="80" y="88" textAnchor="middle" fontSize="18" fontWeight="700" fill="#111827">
                                            {`${Math.round(totalSaved / 1000) || 0}K`}
                                        </text>
                                    </svg>

                                    <p className="text-xs text-metallic-gray -mt-1">Target vs Achievement</p>
                                </div>
                            </div>

                            <button
                                type="button"
                                onClick={() => setShowAdjustGoal(true)}
                                className="mt-6 h-[44px] px-5 rounded-xl border border-strokes text-sm font-semibold text-text hover:bg-gray-50 transition inline-flex items-center gap-2"
                            >
                                Adjust Goal <PencilIcon className="w-4 h-4" />
                            </button>
                        </div>

                        {/* Saving Summary */}
                        <div className="bg-white border border-strokes rounded-xl p-6 shadow-sm">
                            <div className="flex justify-between items-start mb-3">
                                <h2 className="text-base font-semibold text-text">Saving Summary</h2>
                                <select className="text-sm bg-gray-50 border border-strokes rounded-lg px-3 py-2 text-metallic-gray">
                                    <option>{thisMonth}</option>
                                </select>
                            </div>

                            {/* ✅ graph = saved only */}
                            <ComparisonChart title="Summary" data={savedOnlyComparison} currencySymbol={symbol} />
                        </div>
                    </div>

                    {/* Budget and Limits */}
                    <h2 className="text-lg font-semibold text-text mb-4">Budget and Limits</h2>

                    <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-10">
                        {/* Available Balance */}
                        <div className="bg-[#2B2F33] text-white rounded-2xl p-7 shadow-lg">
                            <div className="flex items-center justify-between">
                                <p className="text-sm text-white/70">Available Balance</p>
                                <p className="text-xs text-white/50">{monthLabel}</p>
                            </div>

                            <div className="mt-2 flex items-baseline gap-3">
                                <p className="text-4xl font-semibold tracking-tight">{formatMoney(availableBalance, 2)}</p>

                                {/* ✅ real computed trend based on SAVED vs last month */}
                                <p className="text-xs text-white/60">
                  <span className={savedDeltaIsUp ? "text-emerald-300" : "text-[#FF5A72]"}>
                    {savedDeltaIsUp ? "↗" : "↘"}
                  </span>{" "}
                                    {`${Math.abs(savedDeltaPct).toFixed(1)}%`}{" "}
                                    <span className="text-white/50">vs last month</span>
                                </p>
                            </div>

                            <div className="mt-6 border-t border-white/10 pt-5">
                                {/* Monthly budget row */}
                                <div className="flex items-start justify-between">
                                    <div>
                                        <div className="flex items-center gap-2">
                                            <p className="text-xs text-white/60">Budget {monthShort}</p>
                                            <button
                                                type="button"
                                                disabled={savingBudget}
                                                className="text-white/70 hover:text-white transition"
                                                onClick={() => {
                                                    setEditingMonthly(true);
                                                    setMonthlyDraft(budget?.monthly_limit ?? "");
                                                }}
                                            >
                                                <PencilIcon />
                                            </button>
                                        </div>

                                        {!editingMonthly ? (
                                            <p className="text-base font-semibold mt-1">{formatMoney(monthlyLimit, 0)}</p>
                                        ) : (
                                            <div className="flex items-center gap-2 mt-1">
                                                <input
                                                    value={monthlyDraft}
                                                    onChange={(e) => setMonthlyDraft(e.target.value)}
                                                    className="w-[160px] h-[36px] rounded-lg px-3 text-sm text-white bg-white/10 border border-white/15 focus:outline-none focus:ring-2 focus:ring-white/20"
                                                    type="number"
                                                    min="0"
                                                    step="0.01"
                                                    disabled={savingBudget}
                                                    onKeyDown={(e) => {
                                                        if (e.key === "Enter") submitMonthlyDraft();
                                                        if (e.key === "Escape") {
                                                            setEditingMonthly(false);
                                                            setMonthlyDraft(budget?.monthly_limit ?? "");
                                                        }
                                                    }}
                                                />
                                                <button
                                                    type="button"
                                                    disabled={savingBudget}
                                                    className="h-[36px] px-3 rounded-lg bg-white/15 hover:bg-white/20 transition text-sm font-semibold"
                                                    onClick={submitMonthlyDraft}
                                                >
                                                    Save
                                                </button>
                                            </div>
                                        )}
                                    </div>

                                    <div className="text-right">
                                        <p className="text-xs text-white/60">Spent</p>
                                        <p className="text-base font-semibold mt-1">{formatMoney(monthlySpent, 0)}</p>
                                    </div>
                                </div>

                                <div className="mt-3">
                                    <StripedProgress percent={monthlyPercent} spentValue={monthlySpent} />
                                </div>

                                {/* Notifications toggle */}
                                <div className="mt-6 flex items-center justify-between">
                                    <div className="flex items-center gap-2 text-sm text-white/80">
                                        <span>Enable notifications?</span>
                                        <span className="text-white/50">
                      <InfoIcon />
                    </span>
                                    </div>

                                    <button
                                        type="button"
                                        className={`w-[46px] h-[26px] rounded-full relative transition ${
                                            budgetNotificationsEnabled ? "bg-white/30" : "bg-white/15"
                                        }`}
                                        onClick={() => setBudgetNotificationsEnabled((v) => !v)}
                                    >
                    <span
                        className={`absolute top-[3px] w-[20px] h-[20px] rounded-full bg-white transition ${
                            budgetNotificationsEnabled ? "left-[23px]" : "left-[3px]"
                        }`}
                    />
                                    </button>
                                </div>

                                {/* Daily limit */}
                                <div className="mt-6 border-t border-white/10 pt-5">
                                    <div className="flex items-start justify-between">
                                        <div>
                                            <div className="flex items-center gap-2">
                                                <p className="text-xs text-white/60">Daily Limit</p>
                                                <button
                                                    type="button"
                                                    disabled={savingBudget}
                                                    className="text-white/70 hover:text-white transition"
                                                    onClick={() => {
                                                        setEditingDaily(true);
                                                        setDailyDraft(budget?.daily_limit ?? "");
                                                    }}
                                                >
                                                    <PencilIcon />
                                                </button>
                                            </div>

                                            {!editingDaily ? (
                                                <p className="text-base font-semibold mt-1">{formatMoney(dailyLimit, 0)}</p>
                                            ) : (
                                                <div className="flex items-center gap-2 mt-1">
                                                    <input
                                                        value={dailyDraft}
                                                        onChange={(e) => setDailyDraft(e.target.value)}
                                                        className="w-[160px] h-[36px] rounded-lg px-3 text-sm text-white bg-white/10 border border-white/15 focus:outline-none focus:ring-2 focus:ring-white/20"
                                                        type="number"
                                                        min="0"
                                                        step="0.01"
                                                        disabled={savingBudget}
                                                        onKeyDown={(e) => {
                                                            if (e.key === "Enter") submitDailyDraft();
                                                            if (e.key === "Escape") {
                                                                setEditingDaily(false);
                                                                setDailyDraft(budget?.daily_limit ?? "");
                                                            }
                                                        }}
                                                    />
                                                    <button
                                                        type="button"
                                                        disabled={savingBudget}
                                                        className="h-[36px] px-3 rounded-lg bg-white/15 hover:bg-white/20 transition text-sm font-semibold"
                                                        onClick={submitDailyDraft}
                                                    >
                                                        Save
                                                    </button>
                                                </div>
                                            )}
                                        </div>

                                        <div className="text-right">
                                            <p className="text-xs text-white/60">Spent</p>
                                            <p className="text-base font-semibold mt-1">{formatMoney(dailySpent, 0)}</p>
                                        </div>
                                    </div>

                                    <div className="mt-3">
                                        <StripedProgress percent={dailyPercent} spentValue={dailySpent} />
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* ✅ Limits Per Category (design rows) */}
                        <div className="relative bg-[#2B2F33] text-white rounded-2xl p-7 shadow-lg">
                            <div className="flex items-center justify-between mb-5">
                                <h3 className="text-sm font-semibold text-white/90">Limits Per Category</h3>

                                {/* header labels like design (Budget/Remaining) */}
                                <div className="flex items-center gap-10 text-xs text-white/60">
                                    <span className="w-[80px] text-right">Budget</span>
                                    <span className="w-[90px] text-right">Remaining</span>
                                </div>
                            </div>

                            <div className="space-y-4">
                                {currentLimitRows.length === 0 ? (
                                    <p className="text-sm text-white/60">No category limits found.</p>
                                ) : (
                                    currentLimitRows.map((item) => {
                                        const limit = parseNumber(item.monthly_limit);
                                        const remaining = parseNumber(item.__remaining);

                                        const remainingText =
                                            remaining < 0 ? `-${formatMoney(Math.abs(remaining), 0)}` : formatMoney(remaining, 0);

                                        const remainingClass = remaining < 0 ? "text-[#FF5A72]" : "text-emerald-300";

                                        const iconSrc = getCategoryIcon(item.category_icon || item.category_name || "others");
                                        const bgColor = item.category_color || "rgba(255,255,255,0.12)";

                                        return (
                                            <div key={item.category_id} className="flex items-center justify-between">
                                                <div className="flex items-center gap-3 min-w-0">
                                                    <div
                                                        className="w-10 h-10 rounded-full flex items-center justify-center"
                                                        style={{ backgroundColor: bgColor }}
                                                    >
                                                        <img src={iconSrc} alt="" className="w-5 h-5" />
                                                    </div>

                                                    <p className="text-sm text-white/90 truncate">{item.category_name}</p>
                                                </div>

                                                {/* Budget + Remaining (same row like design) */}
                                                <div className="flex items-center gap-10 text-sm">
                                                    <div className="w-[80px] text-right text-white/70">{formatMoney(limit, 0)}</div>
                                                    <div className={`w-[90px] text-right font-semibold ${remainingClass}`}>{remainingText}</div>
                                                </div>
                                            </div>
                                        );
                                    })
                                )}
                            </div>

                            {categoryLimits.length > pageSize && (
                                <>
                                    <button
                                        type="button"
                                        onClick={goPrevLimits}
                                        className="absolute left-[-14px] top-1/2 -translate-y-1/2 w-9 h-9 rounded-full bg-black/40 hover:bg-black/55 transition flex items-center justify-center"
                                        title="Previous"
                                    >
                                        <ArrowIcon dir="left" />
                                    </button>

                                    <button
                                        type="button"
                                        onClick={goNextLimits}
                                        className="absolute right-[-14px] top-1/2 -translate-y-1/2 w-9 h-9 rounded-full bg-black/40 hover:bg-black/55 transition flex items-center justify-center"
                                        title="Next"
                                    >
                                        <ArrowIcon dir="right" />
                                    </button>
                                </>
                            )}

                            <div className="mt-6 pt-4 border-t border-white/10 text-xs text-white/60">
                                <p className="font-medium text-white/75 mb-2">Details</p>
                                <p>Set budgets and limits to keep track of your transactions and monitor your spending habits.</p>
                            </div>
                        </div>
                    </div>
                </main>

                {/* Adjust Goal Modal (unchanged) */}
                {showAdjustGoal && (
                    <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center">
                        <div className="bg-white rounded-2xl w-[560px] max-w-[92vw] p-8 shadow-2xl">
                            <h2 className="text-xl font-bold text-text">Adjust Saving Goal</h2>
                            <p className="text-sm text-metallic-gray mt-2">
                                Modal opens correctly. To save, we need the backend endpoint for saving goal update.
                            </p>

                            <div className="mt-8 flex justify-end">
                                <button
                                    className="h-11 px-6 rounded-xl bg-blue text-white font-semibold hover:opacity-90 transition"
                                    onClick={() => setShowAdjustGoal(false)}
                                >
                                    Close
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default GoalsPage;
