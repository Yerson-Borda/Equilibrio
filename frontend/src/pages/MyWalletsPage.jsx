import React, { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout";
import MyWalletsContent from "../components/wallet/MyWalletsContent";
import { apiService } from "../services/api";
import { webSocketService } from "../services/websocketService";
import SettingsLoader from "../components/ui/SettingsLoader";

const MyWalletsPage = () => {
    const [wallets, setWallets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [, setCurrentUser] = useState(null);
    const location = useLocation();

    const fetchWallets = async () => {
        try {
            setLoading(true);
            setError(null);
            const walletsData = await apiService.getWallets();
            setWallets(walletsData || []);
        } catch (err) {
            console.error("❌ Error fetching wallets:", err);
            const status = err?.status || err?.response?.status;
            if (status === 401) window.location.href = "/login";
            else setError("Failed to load wallets");
        } finally {
            setLoading(false);
        }
    };

    const loadCurrentUser = async () => {
        try {
            const userData = await apiService.getCurrentUser();
            setCurrentUser(userData);

            if (userData?.id) {
                webSocketService.connect(userData.id);
            }
        } catch (err) {
            console.error("Error loading current user:", err);
        }
    };

    useEffect(() => {
        fetchWallets();
        loadCurrentUser();

        const onWalletUpdated = () => {
            fetchWallets();
        };

        window.addEventListener("wallet_updated", onWalletUpdated);

        return () => {
            window.removeEventListener("wallet_updated", onWalletUpdated);
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [location.state]);

    return (
        <AppLayout activeItem="my-wallets">
            {loading ? (
                <SettingsLoader />
            ) : error ? (
                <div className="flex items-center justify-center h-full">
                    <p className="text-red-500">{error}</p>
                </div>
            ) : (
                <MyWalletsContent
                    wallets={wallets}
                    openTransactionOnLoad={!!location.state?.openAddTransaction}
                    defaultWalletId={location.state?.defaultWalletId || null}
                    onWalletCreated={() => fetchWallets()}
                    onWalletDeleted={() => fetchWallets()}
                />
            )}
        </AppLayout>
    );
};

export default MyWalletsPage;
