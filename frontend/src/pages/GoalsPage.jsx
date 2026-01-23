import React, { useEffect, useMemo, useRef, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import ComparisonChart from "../components/charts/ComparisonChart";
import { apiService } from "../services/api";
import { getCurrencySymbol } from "../config/currencies";
import { getCategoryIcon } from "../utils/categoryIcons";
import SettingsLoader from "../components/ui/SettingsLoader";

/**
 * Minimal snackbar stack (no extra deps)
 * - fixed top-center, stacked like the design
 * - supports: info | success | warning | error
 */
const SnackbarStack = ({ items, onClose }) => {
    if (!items?.length) return null;

    const stylesByVariant = {
        info: {
            bg: "#EEF2FF",
            border: "#C7D2FE",
            text: "#1F2937",
            icon: "ℹ️",
        },
        success: {
            bg: "#ECFDF5",
            border: "#A7F3D0",
            text: "#064E3B",
            icon: "✅",
        },
        warning: {
            bg: "#FFF7ED",
            border: "#FED7AA",
            text: "#7C2D12",
            icon: "⚠️",
        },
        error: {
            bg: "#FEF2F2",
            border: "#FECACA",
            text: "#7F1D1D",
            icon: "⛔",
        },
    };

    return (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-[9999] w-[92vw] max-w-[520px] space-y-3">
            {items.map((snack) => {
                const s = stylesByVariant[snack.variant] || stylesByVariant.info;
                return (
                    <div
                        key={snack.id}
                        className="flex items-center gap-3 rounded-xl border px-4 py-3 shadow-lg"
                        style={{ backgroundColor: s.bg, borderColor: s.border, color: s.text }}
                        role="status"
                        aria-live="polite"
                    >
                        <span className="text-sm">{s.icon}</span>
                        <p className="text-sm font-medium flex-1 leading-snug">{snack.message}</p>
                        <button
                            type="button"
                            className="text-xs font-semibold opacity-70 hover:opacity-100 transition"
                            onClick={() => onClose(snack.id)}
                            aria-label="Close notification"
                            title="Close"
                        >
                            ✕
                        </button>
                    </div>
                );
            })}
        </div>
    );
};

const GoalsPage = () => {
    const [loading, setLoading] = useState(true);

    // Snackbars
    const [snacks, setSnacks] = useState([]);
    const shownWarningsRef = useRef(new Set());

    const addSnack = (message, variant = "info", opts = {}) => {
        const id = `${Date.now()}_${Math.random().toString(16).slice(2)}`;
        const duration = typeof opts.duration === "number" ? opts.duration : 4500;

        setSnacks((prev) => {
            const next = [...prev, { id, message, variant }];
            // keep stack small like the design
            return next.slice(-4);
        });

        if (duration > 0) {
            window.setTimeout(() => {
                setSnacks((prev) => prev.filter((s) => s.id !== id));
            }, duration);
        }

        return id;
    };

    const closeSnack = (id) => {
        setSnacks((prev) => prev.filter((s) => s.id !== id));
    };

    // API data
    const [budget, setBudget] = useState(null);
    const [summary, setSummary] = useState(null);
    const [monthlyComparison, setMonthlyComparison] = useState([]);
    const [prevMonthlyComparison, setPrevMonthlyComparison] = useState([]);
    const [currency, setCurrency] = useState("USD");
    const [totalBalance, setTotalBalance] = useState(0);
    const [categoryLimits, setCategoryLimits] = useState([]);
    const [goals, setGoals] = useState([]);
    const [wallets, setWallets] = useState([]);

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

    // NEW: Create goal modal state
    const [showCreateGoal, setShowCreateGoal] = useState(false);
    const [newGoal, setNewGoal] = useState({
        title: "",
        goal_amount: "",
        description: "",
        deadline: "",
        currency: "USD",
        image: null
    });
    const [selectedWalletId, setSelectedWalletId] = useState("");
    const [forecastData, setForecastData] = useState(null);
    const [depositAmount, setDepositAmount] = useState("");
    const [formLoading, setFormLoading] = useState(false);

    // Adjust goal modal UI (frontend)
    const [showAdjustGoal, setShowAdjustGoal] = useState(false);

    // Notification toggle (local)
    const [budgetNotificationsEnabled, setBudgetNotificationsEnabled] = useState(() => {
        const v = localStorage.getItem("budget_notifications_enabled");
        return v === null ? true : v === "true";
    });

    // NEW: Autodeposit toggle
    const [autodepositEnabled, setAutodepositEnabled] = useState(false);

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

    const formatMoneyCompact = (v, decimals = 0) => {
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

    // NEW: Goal helpers
    const calculateGoalProgress = (goal) => {
        const target = parseNumber(goal?.goal_amount);
        const saved = parseNumber(goal?.amount_saved);
        return target > 0 ? (saved / target) * 100 : 0;
    };

    const formatDate = (dateString) => {
        if (!dateString) return "";
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString("en-US", {
                month: "short",
                day: "numeric",
                year: "numeric"
            });
        } catch {
            return dateString;
        }
    };

    // NEW: Forecast calculation
    const calculateForecast = (goalAmount, savedAmount, deadline) => {
        if (!goalAmount || !deadline) return null;

        const target = parseNumber(goalAmount);
        const saved = parseNumber(savedAmount || 0);
        const remaining = target - saved;

        if (remaining <= 0) return {
            message: "Goal already achieved!",
            monthlyDeposit: 0,
            onTrack: true
        };

        const deadlineDate = new Date(deadline);
        const today = new Date();
        const monthsDiff = Math.max(1, Math.ceil((deadlineDate - today) / (1000 * 60 * 60 * 24 * 30.44)));

        const monthlyDeposit = remaining / monthsDiff;

        return {
            message: `To reach your goal by ${formatDate(deadline)}`,
            monthlyDeposit,
            monthsRemaining: monthsDiff,
            onTrack: monthlyDeposit <= (parseNumber(summary?.total_income) - parseNumber(summary?.total_spent)) / 2
        };
    };

    // NEW: Check if goal is within budget limits
    const isGoalWithinBudget = (goalAmount) => {
        const monthlyBudget = parseNumber(budget?.monthly_limit || 0);
        const monthlySpentAmount = parseNumber(budget?.monthly_spent || 0);
        const remainingBudget = monthlyBudget - monthlySpentAmount;

        const goalMonthlyDeposit = forecastData?.monthlyDeposit || 0;

        return goalMonthlyDeposit <= remainingBudget * 0.3;
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
        return (arr || []).reduce((acc, d) => {
            const income = parseNumber(d?.income);
            const expense = parseNumber(d?.expense);
            const saved = parseNumber(d?.saved ?? income - expense);
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
            apiService.getMonthlyComparison(prevMonth),
            apiService.getUserTotalBalance(),
            apiService.getCategoryLimitsOverview(),
            apiService.getCategorySummary(firstDay, lastDay),
            apiService.getGoals(),
            apiService.getWallets(), // NEW: Fetch wallets
        ]);

        const getVal = (idx, fallback) => (results[idx].status === "fulfilled" ? results[idx].value : fallback);

        const user = getVal(0, null);
        const budg = getVal(1, null);
        const summ = getVal(2, null);
        const comp = getVal(3, []);
        const prevComp = getVal(4, []);
        const balanceData = getVal(5, null);
        const limits = getVal(6, []);
        const catSummary = getVal(7, null);
        const goalsData = getVal(8, []);
        const walletsData = getVal(9, []);

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

        // NEW: Goals data
        setGoals(Array.isArray(goalsData) ? goalsData : []);

        // NEW: Wallets
        setWallets(Array.isArray(walletsData) ? walletsData : []);

        setLoading(false);
    };

    useEffect(() => {
        loadAll();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // NEW: Calculate forecast when goal details change
    useEffect(() => {
        if (newGoal.goal_amount && newGoal.deadline) {
            const forecast = calculateForecast(newGoal.goal_amount, 0, newGoal.deadline);
            setForecastData(forecast);
        } else {
            setForecastData(null);
        }
    }, [newGoal.goal_amount, newGoal.deadline]);

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

    // prevent "blank" bar when spent > 0
    const progressWidth = (percent, spentValue) => {
        const p = clampPercent(percent);
        if (spentValue > 0 && p <= 0) return "6px";
        if (p <= 0) return "0%";
        return `max(${p}%, 6px)`;
    };

    // Goal gauge values
    const goalTargetRaw = totalIncome > 0 ? totalIncome - totalSpent : totalSaved;
    const goalTarget = goalTargetRaw > 0 ? goalTargetRaw : 1000;
    const goalProgress = goalTarget > 0 ? Math.min(1, totalSaved / goalTarget) : 0;

    // Saving Summary must show "how much I saved"
    const savedOnlyComparison = useMemo(() => {
        return (monthlyComparison || []).map((d) => {
            const income = parseNumber(d?.income);
            const expense = parseNumber(d?.expense);
            const saved = parseNumber(d?.saved ?? income - expense);
            return { ...d, income: saved, expense: null };
        });
    }, [monthlyComparison]);

    // ↗ % vs last month computed from SAVED totals
    const savedThisMonth = useMemo(() => sumSavedFromComparison(monthlyComparison), [monthlyComparison]);
    const savedLastMonth = useMemo(() => sumSavedFromComparison(prevMonthlyComparison), [prevMonthlyComparison]);

    const savedDeltaPct = useMemo(() => {
        if (savedLastMonth === 0) {
            if (savedThisMonth === 0) return 0;
            return 100;
        }
        return ((savedThisMonth - savedLastMonth) / Math.abs(savedLastMonth)) * 100;
    }, [savedThisMonth, savedLastMonth]);

    const savedDeltaIsUp = savedDeltaPct >= 0;

    // ---- NEW: Goal handlers ----
    const handleCreateGoal = async () => {
        if (!newGoal.title || !newGoal.goal_amount || !selectedWalletId) {
            addSnack("Please fill in all required fields including wallet", "error");
            return;
        }

        setFormLoading(true);
        try {
            const formData = new FormData();
            formData.append("title", newGoal.title);
            formData.append("goal_amount", newGoal.goal_amount);
            if (newGoal.description) formData.append("description", newGoal.description);
            if (newGoal.deadline) formData.append("deadline", newGoal.deadline);
            if (newGoal.currency) formData.append("currency", newGoal.currency);
            if (newGoal.image) formData.append("image", newGoal.image);
            formData.append("wallet_id", selectedWalletId);

            const createdGoal = await apiService.createGoal(formData);
            setGoals(prev => [createdGoal, ...prev]);
            setShowCreateGoal(false);
            setNewGoal({
                title: "",
                goal_amount: "",
                description: "",
                deadline: "",
                currency: "USD",
                image: null
            });
            setSelectedWalletId("");
            setForecastData(null);
            setDepositAmount("");
            addSnack("Goal created successfully", "success");
        } catch (error) {
            console.error("Failed to create goal:", error);
            addSnack("Failed to create goal", "error");
        } finally {
            setFormLoading(false);
        }
    };

    // ---- snackbars: budget warnings ----
    useEffect(() => {
        if (loading) return;
        if (!budgetNotificationsEnabled) return;

        const warnings = [];

        // Monthly warnings
        if (monthlyLimit > 0) {
            const ratio = monthlySpent / monthlyLimit;

            if (monthlySpent > monthlyLimit) {
                warnings.push({
                    key: `month_exceeded_${thisMonth}`,
                    message: "You've exceeded your monthly budget limit",
                });
            } else if (ratio >= 0.9) {
                warnings.push({
                    key: `month_close_${thisMonth}`,
                    message: "You are close to exceed your budget limit",
                });
            }
        }

        // Daily warnings
        if (dailyLimit > 0) {
            const ratio = dailySpent / dailyLimit;

            if (dailySpent > dailyLimit) {
                warnings.push({
                    key: `day_exceeded_${thisMonth}_${today.toISOString().slice(0, 10)}`,
                    message: "You've exceeded your daily budget limit",
                });
            } else if (ratio >= 0.9) {
                warnings.push({
                    key: `day_close_${thisMonth}_${today.toISOString().slice(0, 10)}`,
                    message: "You are close to exceed your daily budget limit",
                });
            }
        }

        // Show each warning only once per page load
        for (const w of warnings) {
            if (shownWarningsRef.current.has(w.key)) continue;
            shownWarningsRef.current.add(w.key);
            addSnack(w.message, "warning", { duration: 6500 });
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [loading, budgetNotificationsEnabled, monthlySpent, monthlyLimit, dailySpent, dailyLimit, thisMonth]);

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

    // Limits rows: Budget + Remaining, where Remaining uses transaction history spent
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

            addSnack("Budget updated successfully", "success");
        } catch (err) {
            console.error("Failed to update budget:", err);

            addSnack(err?.message || "Failed to update budget", "error", { duration: 6500 });

            setMonthlyDraft(budget?.monthly_limit ?? "");
            setDailyDraft(budget?.daily_limit ?? "");
        } finally {
            setSavingBudget(false);
        }
    };

    const submitMonthlyDraft = async () => {
        const v = Number(monthlyDraft);
        if (!Number.isFinite(v) || v <= 0) {
            addSnack("Please enter a valid monthly budget.", "error");
            return;
        }
        setEditingMonthly(false);
        await commitBudgetUpdate({ monthly_limit: v });
    };

    const submitDailyDraft = async () => {
        const v = Number(dailyDraft);
        if (!Number.isFinite(v) || v <= 0) {
            addSnack("Please enter a valid daily limit.", "error");
            return;
        }
        setEditingDaily(false);
        await commitBudgetUpdate({ daily_limit: v });
    };

    // ---- icons ----
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
            <path
                d="M9 18l6-6-6-6"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
            />
        </svg>
    );

    const InfoIcon = () => (
        <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none">
            <path d="M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Z" stroke="currentColor" strokeWidth="2" />
            <path d="M12 16v-5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            <path d="M12 8h.01" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
        </svg>
    );

    const AddIcon = ({ className = "w-5 h-5" }) => (
        <svg className={className} viewBox="0 0 24 24" fill="none">
            <path
                d="M12 5v14M5 12h14"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
            />
        </svg>
    );

    const CloseIcon = ({ className = "w-5 h-5" }) => (
        <svg className={className} viewBox="0 0 24 24" fill="none">
            <path
                d="M18 6L6 18M6 6l12 12"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
            />
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
            <AppLayout activeItem="goals">
                <SettingsLoader />
            </AppLayout>
        );
    }

    return (
        <AppLayout activeItem="goals">
            {/* Snackbars */}
            <SnackbarStack items={snacks} onClose={closeSnack} />

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

                                <text
                                    x="80"
                                    y="88"
                                    textAnchor="middle"
                                    fontSize="18"
                                    fontWeight="700"
                                    fill="#111827"
                                >
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

                    {/* graph = saved only */}
                    <ComparisonChart title="Summary" data={savedOnlyComparison} currencySymbol={symbol} />
                </div>
            </div>

            {/* Budget and Limits */}
            <h2 className="text-lg font-semibold text-text mb-4">Budget and Limits</h2>

            <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-8">
                {/* Available Balance */}
                <div className="bg-[#2B2F33] text-white rounded-2xl p-7 shadow-lg">
                    <div className="flex items-center justify-between">
                        <p className="text-sm text-white/70">Available Balance</p>
                        <p className="text-xs text-white/50">{monthLabel}</p>
                    </div>

                    <div className="mt-2 flex items-baseline gap-3">
                        <p className="text-4xl font-semibold tracking-tight">{formatMoney(availableBalance, 2)}</p>

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

                        {/* Autodeposit toggle */}
                        <div className="mt-6 flex items-center justify-between">
                            <div className="flex items-center gap-2 text-sm text-white/80">
                                <span>Enable autodeposit?</span>
                                <span className="text-white/50">
                                    <InfoIcon />
                                </span>
                            </div>

                            <button
                                type="button"
                                onClick={() => setAutodepositEnabled(!autodepositEnabled)}
                                className={`w-[46px] h-[26px] rounded-full relative transition ${autodepositEnabled ? "bg-white/30" : "bg-white/15"}`}
                            >
                                <span
                                    className={`absolute top-[3px] w-[20px] h-[20px] rounded-full bg-white transition ${autodepositEnabled ? "left-[23px]" : "left-[3px]"}`}
                                />
                            </button>
                        </div>

                        {/* Notifications toggle */}
                        <div className="mt-4 flex items-center justify-between">
                            <div className="flex items-center gap-2 text-sm text-white/80">
                                <span>Enable notifications?</span>
                                <span className="text-white/50">
                                    <InfoIcon />
                                </span>
                            </div>

                            <button
                                type="button"
                                onClick={() => setBudgetNotificationsEnabled(!budgetNotificationsEnabled)}
                                className={`w-[46px] h-[26px] rounded-full relative transition ${budgetNotificationsEnabled ? "bg-white/30" : "bg-white/15"}`}
                            >
                                <span
                                    className={`absolute top-[3px] w-[20px] h-[20px] rounded-full bg-white transition ${budgetNotificationsEnabled ? "left-[23px]" : "left-[3px]"}`}
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

                {/* Limits Per Category */}
                <div className="relative bg-[#2B2F33] text-white rounded-2xl p-7 shadow-lg">
                    <div className="flex items-center justify-between mb-5">
                        <h3 className="text-sm font-semibold text-white/90">Limits Per Category</h3>

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

            {/* Spending Goals Section (MOVED BELOW Budget and Limits) */}
            <div className="bg-white rounded-xl border border-strokes p-6 shadow-sm mb-8">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-lg font-semibold text-text">Spending Goals</h2>
                    <button
                        onClick={() => setShowCreateGoal(true)}
                        className="bg-blue text-white px-4 py-2 rounded-lg font-semibold hover:bg-blue-dark transition flex items-center gap-2"
                    >
                        <AddIcon className="w-4 h-4" /> New Goal
                    </button>
                </div>

                {/* Show goals linked to budget */}
                <div className="mb-4 p-4 bg-gray-50 rounded-lg">
                    <div className="flex justify-between items-center">
                        <div>
                            <p className="text-sm text-metallic-gray">Monthly Budget Status</p>
                            <p className="font-semibold text-text">
                                {formatMoney(monthlyLimit)} limit • {formatMoney(monthlySpent)} spent • {formatMoney(monthlyLimit - monthlySpent)} remaining
                            </p>
                        </div>
                        <div className="text-right">
                            <p className="text-sm text-metallic-gray">Goals Impact</p>
                            <p className="font-semibold text-text">
                                {formatMoney(goals.reduce((sum, goal) => sum + parseNumber(goal.amount_saved), 0))} allocated
                            </p>
                        </div>
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    {goals.slice(0, 4).map((goal) => {
                        const progress = calculateGoalProgress(goal);
                        const goalWallet = wallets.find(w => w.id === goal.wallet_id);
                        const goalImpact = monthlyLimit > 0 ? (parseNumber(goal.amount_saved) / monthlyLimit * 100) : 0;

                        return (
                            <div
                                key={goal.id}
                                className="p-4 border border-strokes rounded-lg hover:bg-gray-50 transition"
                            >
                                <div className="flex justify-between items-start mb-2">
                                    <h3 className="font-semibold text-text">{goal.title}</h3>
                                    <span className="text-xs bg-blue-100 text-blue px-2 py-1 rounded">
                                        {goalWallet?.name || 'No wallet'}
                                    </span>
                                </div>
                                <p className="text-xs text-metallic-gray mb-3">
                                    {goal.deadline ? formatDate(goal.deadline) : "No deadline"}
                                </p>
                                <p className="font-semibold text-text mb-2">
                                    {formatMoneyCompact(goal.amount_saved)} out of {formatMoneyCompact(goal.goal_amount)}
                                </p>
                                <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden mb-2">
                                    <div
                                        className="h-2 rounded-full bg-green-500"
                                        style={{ width: `${progress}%` }}
                                    />
                                </div>
                                <div className="flex justify-between text-xs text-metallic-gray">
                                    <span>Progress: {progress.toFixed(1)}%</span>
                                    <span>Impact: {goalImpact.toFixed(1)}% of budget</span>
                                </div>
                            </div>
                        );
                    })}

                    {/* Add New Goal Card */}
                    <div
                        className="p-4 border-2 border-dashed border-strokes rounded-lg text-center hover:bg-gray-50 transition cursor-pointer flex flex-col items-center justify-center"
                        onClick={() => setShowCreateGoal(true)}
                    >
                        <AddIcon className="w-8 h-8 text-metallic-gray mb-2" />
                        <p className="text-sm text-metallic-gray">New Goal</p>
                    </div>
                </div>
            </div>

            {/* Create Goal Modal with wallet selection and forecast */}
            {showCreateGoal && (
                <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center">
                    <div className="bg-white rounded-2xl w-[560px] max-w-[92vw] max-h-[90vh] overflow-y-auto p-8 shadow-2xl">
                        <div className="flex justify-between items-center mb-6">
                            <h2 className="text-xl font-bold text-text">Create New Goal</h2>
                            <button
                                onClick={() => {
                                    setShowCreateGoal(false);
                                    setForecastData(null);
                                    setSelectedWalletId("");
                                }}
                                className="text-metallic-gray hover:text-text"
                            >
                                <CloseIcon />
                            </button>
                        </div>

                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Title *
                                </label>
                                <input
                                    type="text"
                                    value={newGoal.title}
                                    onChange={(e) => setNewGoal({...newGoal, title: e.target.value})}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue"
                                    placeholder="e.g., Trip to Iceland"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Target Amount *
                                </label>
                                <input
                                    type="number"
                                    value={newGoal.goal_amount}
                                    onChange={(e) => setNewGoal({...newGoal, goal_amount: e.target.value})}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue"
                                    placeholder="e.g., 1000"
                                    min="0"
                                    step="0.01"
                                />
                            </div>

                            {/* Wallet Selection */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Select Wallet *
                                </label>
                                <select
                                    value={selectedWalletId}
                                    onChange={(e) => setSelectedWalletId(e.target.value)}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue"
                                >
                                    <option value="">Select a wallet</option>
                                    {wallets.map((wallet) => (
                                        <option key={wallet.id} value={wallet.id}>
                                            {wallet.name} - {formatMoney(wallet.balance)}
                                        </option>
                                    ))}
                                </select>
                                <p className="text-xs text-metallic-gray mt-1">
                                    This wallet will be used for automatic deposits if enabled
                                </p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Description (Optional)
                                </label>
                                <textarea
                                    value={newGoal.description}
                                    onChange={(e) => setNewGoal({...newGoal, description: e.target.value})}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue"
                                    rows="3"
                                    placeholder="Describe your goal..."
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Deadline (Optional)
                                </label>
                                <input
                                    type="date"
                                    value={newGoal.deadline}
                                    onChange={(e) => setNewGoal({...newGoal, deadline: e.target.value})}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue"
                                    min={new Date().toISOString().split('T')[0]}
                                />
                            </div>

                            {/* Forecast Section */}
                            {forecastData && (
                                <div className={`p-4 rounded-lg border ${isGoalWithinBudget(newGoal.goal_amount) ? 'bg-green-50 border-green-200' : 'bg-yellow-50 border-yellow-200'}`}>
                                    <h3 className="font-semibold text-text mb-2">Forecast</h3>
                                    <p className="text-sm text-metallic-gray mb-2">{forecastData.message}</p>
                                    <div className="flex items-center justify-between">
                                        <div>
                                            <p className="text-xs text-metallic-gray">Monthly deposit needed</p>
                                            <p className="font-semibold text-text">{formatMoney(forecastData.monthlyDeposit)}</p>
                                        </div>
                                        <div>
                                            <p className="text-xs text-metallic-gray">Time remaining</p>
                                            <p className="font-semibold text-text">{forecastData.monthsRemaining} months</p>
                                        </div>
                                    </div>
                                    {!isGoalWithinBudget(newGoal.goal_amount) && (
                                        <p className="text-xs text-yellow-600 mt-2">
                                            ⚠️ This goal exceeds 30% of your remaining budget. Consider adjusting your budget or goal amount.
                                        </p>
                                    )}
                                </div>
                            )}

                            {/* Link to Budget and Limits */}
                            <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
                                <h3 className="font-semibold text-text mb-2">Budget Integration</h3>
                                <div className="space-y-2 text-sm">
                                    <div className="flex justify-between">
                                        <span className="text-metallic-gray">Monthly Budget:</span>
                                        <span className="font-semibold">{formatMoney(monthlyLimit)}</span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span className="text-metallic-gray">Monthly Spent:</span>
                                        <span className="font-semibold">{formatMoney(monthlySpent)}</span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span className="text-metallic-gray">Remaining Budget:</span>
                                        <span className="font-semibold">{formatMoney(monthlyLimit - monthlySpent)}</span>
                                    </div>
                                    {forecastData && (
                                        <div className="flex justify-between">
                                            <span className="text-metallic-gray">Goal Monthly Impact:</span>
                                            <span className={`font-semibold ${isGoalWithinBudget(newGoal.goal_amount) ? 'text-green-600' : 'text-yellow-600'}`}>
                                                {formatMoney(forecastData.monthlyDeposit)}
                                            </span>
                                        </div>
                                    )}
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Upload Image (Optional)
                                </label>
                                <input
                                    type="file"
                                    accept="image/*"
                                    onChange={(e) => setNewGoal({...newGoal, image: e.target.files[0]})}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue"
                                />
                            </div>

                            {/* Auto-deposit option */}
                            <div className="flex items-center justify-between">
                                <div className="flex items-center gap-2">
                                    <span className="text-sm text-text">Enable automatic monthly deposit?</span>
                                    <span className="text-metallic-gray cursor-help" title="Automatically transfer funds from your wallet each month">
                                        <InfoIcon />
                                    </span>
                                </div>
                                <button
                                    type="button"
                                    onClick={() => setAutodepositEnabled(!autodepositEnabled)}
                                    className={`w-12 h-6 rounded-full relative transition-colors ${autodepositEnabled ? 'bg-blue' : 'bg-gray-300'}`}
                                >
                                    <div
                                        className={`absolute top-1 w-4 h-4 rounded-full bg-white transition-transform ${autodepositEnabled ? 'left-7' : 'left-1'}`}
                                    />
                                </button>
                            </div>

                            {autodepositEnabled && selectedWalletId && (
                                <div>
                                    <label className="block text-sm font-medium text-text mb-2">
                                        Monthly Deposit Amount
                                    </label>
                                    <input
                                        type="number"
                                        value={depositAmount}
                                        onChange={(e) => setDepositAmount(e.target.value)}
                                        className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue"
                                        placeholder="Enter deposit amount"
                                        min="0"
                                        step="0.01"
                                    />
                                    <p className="text-xs text-metallic-gray mt-1">
                                        This amount will be automatically transferred from your selected wallet each month
                                    </p>
                                </div>
                            )}
                        </div>

                        <div className="mt-8 flex justify-end gap-3">
                            <button
                                onClick={() => {
                                    setShowCreateGoal(false);
                                    setForecastData(null);
                                    setSelectedWalletId("");
                                }}
                                className="h-11 px-6 rounded-xl border border-strokes text-text font-semibold hover:bg-gray-50 transition"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleCreateGoal}
                                disabled={formLoading || !selectedWalletId}
                                className="h-11 px-6 rounded-xl bg-blue text-white font-semibold hover:opacity-90 transition disabled:opacity-60 disabled:cursor-not-allowed"
                            >
                                {formLoading ? "Creating..." : "Create Goal"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Your existing Adjust Goal Modal */}
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
        </AppLayout>
    );
};

export default GoalsPage;