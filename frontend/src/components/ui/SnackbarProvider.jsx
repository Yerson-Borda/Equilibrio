import React, {
    createContext,
    useCallback,
    useContext,
    useMemo,
    useRef,
    useState,
} from "react";

const SnackbarContext = createContext(null);

const icons = {
    success: (
        <svg viewBox="0 0 24 24" className="w-5 h-5" fill="none">
            <path
                d="M20 6 9 17l-5-5"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
            />
        </svg>
    ),
    error: (
        <svg viewBox="0 0 24 24" className="w-5 h-5" fill="none">
            <path d="M12 9v4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            <path d="M12 17h.01" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
            <path
                d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinejoin="round"
            />
        </svg>
    ),
    info: (
        <svg viewBox="0 0 24 24" className="w-5 h-5" fill="none">
            <path
                d="M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Z"
                stroke="currentColor"
                strokeWidth="2"
            />
            <path d="M12 16v-5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            <path d="M12 8h.01" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
        </svg>
    ),
};

const variantStyles = {
    success: {
        wrapper: "bg-[#E9F7EF] border border-[#A7E7C2] text-[#0F5132]",
        icon: "text-[#16A34A]",
    },
    error: {
        wrapper: "bg-[#FDECEC] border border-[#F8B4B4] text-[#7F1D1D]",
        icon: "text-[#EF4444]",
    },
    info: {
        wrapper: "bg-[#EEF2FF] border border-[#C7D2FE] text-[#1E3A8A]",
        icon: "text-[#4F46E5]",
    },
};

function SnackbarItem({ id, message, variant = "success", onClose }) {
    const styles = variantStyles[variant] || variantStyles.success;
    const icon = icons[variant] || icons.success;

    return (
        <div
            className={`w-[360px] max-w-[92vw] rounded-lg shadow-lg px-3.5 py-2.5 flex items-center gap-2 ${styles.wrapper}`}
            role="status"
            aria-live="polite"
        >
            <div className={`${styles.icon} flex-shrink-0`}>{icon}</div>
            <div className="text-sm font-medium flex-1">{message}</div>
            <button
                type="button"
                className="ml-2 text-current/70 hover:text-current transition"
                onClick={() => onClose(id)}
                aria-label="Close"
            >
                ✕
            </button>
        </div>
    );
}

export function SnackbarProvider({ children }) {
    const [snacks, setSnacks] = useState([]);
    const timersRef = useRef(new Map());

    const remove = useCallback((id) => {
        setSnacks((prev) => prev.filter((s) => s.id !== id));
        const t = timersRef.current.get(id);
        if (t) {
            clearTimeout(t);
            timersRef.current.delete(id);
        }
    }, []);

    const showSnackbar = useCallback(
        (message, opts = {}) => {
            const { variant = "success", duration = 2800 } = opts;
            const id = `${Date.now()}-${Math.random().toString(16).slice(2)}`;

            setSnacks((prev) => {
                const next = [...prev, { id, message, variant }];
                return next.slice(-3); // max 3 visible
            });

            const timeout = setTimeout(() => remove(id), duration);
            timersRef.current.set(id, timeout);
            return id;
        },
        [remove]
    );

    const value = useMemo(() => ({ showSnackbar, remove }), [showSnackbar, remove]);

    return (
        <SnackbarContext.Provider value={value}>
            {children}

            {/* top-center like your screenshot */}
            <div className="fixed top-4 left-1/2 -translate-x-1/2 z-[9999] space-y-2">
                {snacks.map((s) => (
                    <SnackbarItem key={s.id} {...s} onClose={remove} />
                ))}
            </div>
        </SnackbarContext.Provider>
    );
}

export function useSnackbar() {
    const ctx = useContext(SnackbarContext);
    if (!ctx) throw new Error("useSnackbar must be used within a SnackbarProvider");
    return ctx;
}
