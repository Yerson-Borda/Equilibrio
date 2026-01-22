import React from "react";
import Sidebar from "./Sidebar";
import Header from "./Header";

const TITLES = {
    dashboard: "Dashboard",
    transactions: "Transactions",
    "my-wallets": "My Wallets",
    goals: "Goals",
    settings: "Settings",
};

const AppLayout = ({ activeItem, children, titleOverride = null }) => {
    const title = titleOverride ?? TITLES[activeItem] ?? "";

    return (
        <div className="min-h-screen bg-background flex">
            {/* Sidebar */}
            <Sidebar activeItem={activeItem} />

            {/* Main content area */}
            <div className="flex-1 ml-64 flex flex-col bg-background">
                {/* Top header area (Title + icons/profile) */}
                <Header title={title} />

                {/* Page content */}
                <main className="flex-1 p-8 overflow-auto bg-background">{children}</main>
            </div>
        </div>
    );
};

export default AppLayout;
