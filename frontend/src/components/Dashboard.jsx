import React, { useState } from 'react';
import Sidebar from './Sidebar';
import Header from './Header';
import EmptyState from './dashboard/EmptyState';
import WalletOverview from './dashboard/WalletOverview';
import CreateWalletModal from './wallet/CreateWalletModal';
import { apiService } from '../services/api';

const Dashboard = ({ wallets, onWalletCreated }) => {
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const handleCreateWallet = async (walletData) => {
        try {
            setIsLoading(true);
            const newWallet = await apiService.createWallet(walletData);
            onWalletCreated(newWallet);
            setIsCreateModalOpen(false);
        } catch (error) {
            console.error('Error creating wallet:', error);
            alert('Failed to create wallet. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    const totalBalance = wallets.reduce((sum, wallet) => sum + (parseFloat(wallet.balance) || 0), 0);
    const totalSaved = wallets.reduce((sum, wallet) => sum + (parseFloat(wallet.saved_amount) || 0), 0);

    return (
        <div className="min-h-screen bg-background flex">
            <Sidebar activeItem="dashboard" />

            {/* Main Content Area */}
            <div className="flex-1 ml-64 flex flex-col bg-background">
                <Header />

                <div className="flex-1 p-8 overflow-auto bg-background">
                    {wallets.length === 0 ? (
                        <EmptyState onCreateWallet={() => setIsCreateModalOpen(true)} />
                    ) : (
                        <WalletOverview
                            wallets={wallets}
                            totalBalance={totalBalance}
                            totalSaved={totalSaved}
                            onCreateWallet={() => setIsCreateModalOpen(true)}
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