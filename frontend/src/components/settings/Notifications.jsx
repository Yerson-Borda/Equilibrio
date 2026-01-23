import React, { useState } from "react";
import Button from "../ui/Button";
import { useSnackbar } from "../ui/SnackbarProvider";

const Notifications = () => {
    const { showSnackbar } = useSnackbar();

    const [notifications, setNotifications] = useState({
        receiveNotifications: true,
        notifyWhenExceedLimit: true,
        reminderToLogExpenses: true,
        forecasts: true,
    });

    const handleToggle = (key) => {
        setNotifications((prev) => ({
            ...prev,
            [key]: !prev[key],
        }));
    };

    const Toggle = ({ value, onClick }) => (
        <button
            type="button"
            className={`w-12 h-6 flex items-center rounded-full p-1 transition-colors ${
                value ? "bg-blue" : "bg-gray-300"
            }`}
            onClick={onClick}
        >
            <div
                className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                    value ? "translate-x-6" : "translate-x-0"
                }`}
            />
        </button>
    );

    const handleUpdate = async () => {
        try {
            // Hook this to your API later
            // await apiService.updateNotifications(notifications);

            showSnackbar("Notifications updated successfully!", { variant: "success" });
        } catch (e) {
            showSnackbar("Failed to update notifications", { variant: "error" });
        }
    };

    return (
        <div className="w-full">
            <h2 className="text-xl font-semibold text-text mb-10">Update your notifications</h2>

            {/* Receive notifications row + divider */}
            <div className="flex items-center justify-between pb-5 border-b border-strokes">
                <span className="text-text font-medium">Receive notifications</span>
                <Toggle value={notifications.receiveNotifications} onClick={() => handleToggle("receiveNotifications")} />
            </div>

            {/* Two column area like the design */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-10 pt-8">
                <div className="space-y-10">
                    <div className="flex items-center justify-between">
                        <span className="text-text">Notify when exceed a limit</span>
                        <Toggle value={notifications.notifyWhenExceedLimit} onClick={() => handleToggle("notifyWhenExceedLimit")} />
                    </div>

                    <div className="flex items-center justify-between">
                        <span className="text-text">Reminder to log expenses</span>
                        <Toggle value={notifications.reminderToLogExpenses} onClick={() => handleToggle("reminderToLogExpenses")} />
                    </div>
                </div>

                <div className="flex items-start justify-between">
                    <span className="text-text">Forecasts</span>
                    <Toggle value={notifications.forecasts} onClick={() => handleToggle("forecasts")} />
                </div>
            </div>

            <div className="pt-16">
                <Button
                    type="button"
                    variant="primary"
                    className="px-16 py-5 text-xl font-semibold rounded-xl"
                    onClick={handleUpdate}
                >
                    Update
                </Button>
            </div>
        </div>
    );
};

export default Notifications;
