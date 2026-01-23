import React, { useState, useEffect } from "react";
import EmptyState from "../dashboard/EmptyState";
import DashboardContent from "../dashboard/DashboardContent";
import CreateWalletModal from "../modals/CreateWalletModal";
import { apiService } from "../../services/api";
import { webSocketService } from "../../services/websocketService";
import { useSnackbar } from "../ui/SnackbarProvider";

/**
 * Dashboard is now "CONTENT ONLY".
 * AppLayout is responsible for Sidebar + Header + main paddings.
 */
const Dashboard = ({ wallets: initialWallets, onWalletCreated, userStats: initialUserStats }) => {
    const { showSnackbar } = useSnackbar();

    const [wallets, setWallets] = useState(initialWallets || []);
    const [userStats, setUserStats] = useState(
        initialUserStats || {
            totalBalance: 0,
            totalSpending: 0,
            totalSaved: 0,
            defaultCurrency: "USD",
        }
    );

    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [currentUser, setCurrentUser] = useState(null);

    const parseNumber = (value) => {
        if (value === null || value === undefined) return 0;
        const n = typeof value === "string" ? parseFloat(value) : Number(value);
        return Number.isNaN(n) ? 0 : n;
    };

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

    const refreshUserStats = async () => {
        try {
            const [balanceData, summary, user] = await Promise.all([
                apiService.getUserTotalBalance(),
                apiService.getCurrentSummary(),
                apiService.getCurrentUser(),
            ]);

            const totalBalance = extractTotalBalance(balanceData);
            const totalSpent = parseNumber(summary?.total_spent);
            const totalSaved = parseNumber(summary?.total_saved);
            const defaultCurrency = user?.default_currency || "USD";

            setUserStats({
                totalBalance,
                totalSpending: totalSpent,
                totalSaved,
                defaultCurrency,
            });
        } catch (error) {
            console.error("Error refreshing user stats:", error);
        }
    };

    const refreshWallets = async () => {
        try {
            const walletsData = await apiService.getWallets();
            setWallets(walletsData || []);
        } catch (error) {
            console.error("Error refreshing wallets:", error);
        }
    };

    const loadCurrentUser = async () => {
        try {
            const userData = await apiService.getCurrentUser();
            setCurrentUser(userData);

            // Connect websocket once we know user id
            if (userData?.id) {
                webSocketService.connect(userData.id);
            }
        } catch (error) {
            console.error("Error loading current user:", error);
        }
    };

    const handleWalletCreated = (newWallet) => {
        setWallets((prev) => {
            const exists = prev.find((w) => w.id === newWallet.id);
            if (exists) return prev.map((w) => (w.id === newWallet.id ? newWallet : w));
            return [...prev, newWallet];
        });

        refreshUserStats();

        if (typeof onWalletCreated === "function") {
            onWalletCreated(newWallet);
        }
    };

    const handleWalletUpdated = (data) => {
        setWallets((prev) =>
            prev.map((wallet) =>
                wallet.id === data.wallet_id ? { ...wallet, balance: data.wallet_balance } : wallet
            )
        );
        refreshUserStats();
    };

    const handleWalletDeleted = (walletId) => {
        setWallets((prev) => prev.filter((wallet) => wallet.id !== walletId));
        refreshUserStats();
    };

    const handleTransactionUpdate = () => {
        refreshWallets();
        refreshUserStats();
    };

    const setupWebSocketListeners = () => {
        webSocketService.addEventListener("wallet_created", (data) => {
            handleWalletCreated(data.wallet || data);
        });

        webSocketService.addEventListener("wallet_updated", (data) => {
            handleWalletUpdated(data);
        });

        webSocketService.addEventListener("wallet_deleted", (data) => {
            handleWalletDeleted(data.wallet_id);
        });

        webSocketService.addEventListener("transaction_created", handleTransactionUpdate);
        webSocketService.addEventListener("transaction_updated", handleTransactionUpdate);
        webSocketService.addEventListener("transaction_deleted", handleTransactionUpdate);
    };

    const cleanupWebSocketListeners = () => {
        webSocketService.removeEventListener("wallet_created");
        webSocketService.removeEventListener("wallet_updated");
        webSocketService.removeEventListener("wallet_deleted");
        webSocketService.removeEventListener("transaction_created");
        webSocketService.removeEventListener("transaction_updated");
        webSocketService.removeEventListener("transaction_deleted");
    };

    const handleCreateWallet = async (walletData) => {
        try {
            setIsLoading(true);
            const newWallet = await apiService.createWallet(walletData);

            handleWalletCreated(newWallet);

            // ✅ Snackbar instead of dialog/alert
            showSnackbar("Wallet created successfully!", { variant: "success" });

            setIsCreateModalOpen(false);
        } catch (error) {
            console.error("Error creating wallet:", error);

            // ✅ Snackbar error
            showSnackbar(error?.message ? `Failed to create wallet: ${error.message}` : "Failed to create wallet", {
                variant: "error",
            });
        } finally {
            setIsLoading(false);
        }
    };

    // Keep in sync if parent passes data
    useEffect(() => {
        if (initialWallets) setWallets(initialWallets);
        if (initialUserStats) setUserStats(initialUserStats);
    }, [initialWallets, initialUserStats]);

    useEffect(() => {
        loadCurrentUser();
        refreshUserStats();
        refreshWallets();
        setupWebSocketListeners();

        return () => {
            cleanupWebSocketListeners();
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <>
            {/* CONTENT ONLY */}
            <div className="w-full">
                {wallets.length === 0 ? (
                    <EmptyState onCreateWallet={() => setIsCreateModalOpen(true)} userStats={userStats} />
                ) : (
                    <DashboardContent wallets={wallets} onCreateWallet={() => setIsCreateModalOpen(true)} userStats={userStats} />
                )}
            </div>

            <CreateWalletModal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                onSubmit={handleCreateWallet}
                isLoading={isLoading}
            />
        </>
    );
};

export default Dashboard;
