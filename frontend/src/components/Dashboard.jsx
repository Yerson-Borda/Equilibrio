import React, { useState } from 'react';
import Sidebar from './Sidebar';
import Header from './Header';
import EmptyState from './dashboard/EmptyState';
import DashboardContent from './dashboard/DashboardContent';
import CreateWalletModal from './wallet/CreateWalletModal';
import { apiService } from '../services/api';

const Dashboard = ({ wallets, onWalletCreated, userStats }) => {
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const handleCreateWallet = async (walletData) => {
        try {
            setIsLoading(true);

            const formattedData = {
                name: walletData.name,
                currency: walletData.currency,
                wallet_type: walletData.wallet_type,
                initial_balance: parseFloat(walletData.initial_balance) || 0,
                card_number: walletData.card_number || '',
                color: walletData.color || '#6FBAFC'
            };

            console.log('Sending wallet data:', formattedData);

            const newWallet = await apiService.createWallet(formattedData);
            onWalletCreated(newWallet);
            setIsCreateModalOpen(false);
        } catch (error) {
            console.error('Error creating wallet:', error);
            alert(`Failed to create wallet: ${error.message || 'Please try again.'}`);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-background flex">
            <Sidebar activeItem="dashboard" />

            <div className="flex-1 ml-64 flex flex-col bg-background">
                <Header />

                <div className="flex-1 p-8 overflow-auto bg-background">
                    {wallets.length === 0 ? (
                        <EmptyState
                            onCreateWallet={() => setIsCreateModalOpen(true)}
                            userStats={userStats}
                        />
                    ) : (
                        <DashboardContent
                            wallets={wallets}
                            onCreateWallet={() => setIsCreateModalOpen(true)}
                            userStats={userStats}
                        />
                    )}
                </div>
            </div>

            <CreateWalletModal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                onSubmit={handleCreateWallet}
                isLoading={isLoading}
            />
        </div>
    );
};

export default Dashboard;