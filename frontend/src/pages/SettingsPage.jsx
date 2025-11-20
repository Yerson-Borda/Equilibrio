import React, { useState } from 'react';
import AppLayout from '../components/layout/AppLayout';
import AccountInformation from '../components/settings/AccountInformation';
import Notifications from '../components/settings/Notifications';

const SettingsPage = () => {
    const [activeTab, setActiveTab] = useState('account');

    return (
        <AppLayout activeItem="settings">
            <div className="max-w-4xl mx-auto">
                <h1 className="text-2xl font-bold text-text mb-8">Settings</h1>

                {/* Tab Navigation */}
                <div className="flex space-x-8 border-b border-strokes mb-8">
                    <button
                        className={`pb-4 px-2 font-medium ${
                            activeTab === 'account'
                                ? 'text-blue border-b-2 border-blue'
                                : 'text-metallic-gray'
                        }`}
                        onClick={() => setActiveTab('account')}
                    >
                        Account Information
                    </button>
                    <button
                        className={`pb-4 px-2 font-medium ${
                            activeTab === 'notifications'
                                ? 'text-blue border-b-2 border-blue'
                                : 'text-metallic-gray'
                        }`}
                        onClick={() => setActiveTab('notifications')}
                    >
                        Notifications
                    </button>
                </div>

                {/* Tab Content */}
                {activeTab === 'account' && <AccountInformation />}
                {activeTab === 'notifications' && <Notifications />}
            </div>
        </AppLayout>
    );
};

export default SettingsPage;
