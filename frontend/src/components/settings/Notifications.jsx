import React, { useState } from 'react';
import Button from '../ui/Button';

const Notifications = () => {
    const [notifications, setNotifications] = useState({
        receiveNotifications: true,
        notifyWhenExceedLimit: true,
        reminderToLogExpenses: false,
        forecasts: true
    });

    const handleToggle = (key) => {
        setNotifications(prev => ({
            ...prev,
            [key]: !prev[key]
        }));
    };

    return (
        <div className="bg-white rounded-xl shadow-sm p-8 border border-strokes">
            <h2 className="text-xl font-semibold text-text mb-6">Update your notifications</h2>

            <div className="space-y-6">
                {/* Notification Toggles */}
                <div className="space-y-4">
                    {Object.entries(notifications).map(([key, value]) => (
                        <div key={key} className="flex justify-between items-center">
                            <span className="text-text">
                                {key === 'receiveNotifications' && 'Receive notifications'}
                                {key === 'notifyWhenExceedLimit' && 'Notify when exceed a limit'}
                                {key === 'reminderToLogExpenses' && 'Reminder to log expenses'}
                                {key === 'forecasts' && 'Forecasts'}
                            </span>
                            <button
                                type="button"
                                className={`w-12 h-6 flex items-center rounded-full p-1 transition-colors ${
                                    value ? 'bg-blue' : 'bg-gray-300'
                                }`}
                                onClick={() => handleToggle(key)}
                            >
                                <div
                                    className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                                        value ? 'translate-x-6' : 'translate-x-0'
                                    }`}
                                />
                            </button>
                        </div>
                    ))}
                </div>

                {/* Bottom Links */}
                <div className="pt-6 border-t border-strokes space-y-3">
                    <button className="block w-full text-left text-blue hover:text-blue-600 py-2">
                        Login
                    </button>
                    <button className="block w-full text-left text-blue hover:text-blue-600 py-2">
                        Help
                    </button>
                    <button className="block w-full text-left text-red-600 hover:text-red-700 py-2">
                        Logout
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Notifications;