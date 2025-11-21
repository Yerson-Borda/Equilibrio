import React, { useState } from 'react';
import Card from '../ui/Card';
import Button from '../ui/Button';
import CreateWalletModal from '../modals/CreateWalletModal';
import { apiService } from '../../services/api';

const MyWalletsContent = ({ wallets = [], onWalletCreated, onAddTransaction }) => {
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isCreatingWallet, setIsCreatingWallet] = useState(false);

    const handleOpenCreateWallet = () => {
        setIsCreateModalOpen(true);
    };

    const handleCloseCreateWallet = () => {
        setIsCreateModalOpen(false);
    };

    const handleCreateWallet = async (walletData) => {
        try {
            setIsCreatingWallet(true);

            const formattedData = {
                name: walletData.name,
                currency: walletData.currency,
                wallet_type: walletData.wallet_type,
                initial_balance: parseFloat(walletData.initial_balance) || 0,
                card_number: walletData.card_number || '',
                color: walletData.color || '#6FBAFC',
            };

            const newWallet = await apiService.createWallet(formattedData);

            if (onWalletCreated) {
                onWalletCreated(newWallet);
            }

            setIsCreateModalOpen(false);
            alert('Wallet created successfully!');
        } catch (error) {
            console.error('Error creating wallet from MyWalletsContent:', error);
            alert(error.message || 'Failed to create wallet. Please try again.');
        } finally {
            setIsCreatingWallet(false);
        }
    };

    const handleAddTransactionClick = (wallet) => {
        if (onAddTransaction) {
            onAddTransaction(wallet);
        }
    };

    const totalWallets = wallets.length;
    const totalBalance = wallets.reduce(
        (sum, w) => sum + (parseFloat(w.balance) || 0),
        0
    );

    return (
        <div className="grid grid-cols-1 xl:grid-cols-[2fr,1.4fr] gap-8">
            {/* Left: Wallets list */}
            <div className="space-y-6">
                <Card
                    title="My Wallets"
                    headerRight={
                        <Button
                            variant="primary"
                            onClick={handleOpenCreateWallet}
                            className="text-sm"
                        >
                            + Create Wallet
                        </Button>
                    }
                >
                    {wallets.length === 0 ? (
                        <div className="text-center py-10">
                            <p className="text-metallic-gray mb-3">
                                You don’t have any wallets yet.
                            </p>
                            <Button
                                variant="primary"
                                onClick={handleOpenCreateWallet}
                            >
                                Create your first wallet
                            </Button>
                        </div>
                    ) : (
                        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
                            {wallets.map((wallet) => (
                                <div
                                    key={wallet.id}
                                    className="border border-strokes rounded-xl p-4 flex flex-col justify-between bg-white shadow-sm"
                                >
                                    <div>
                                        <h3 className="text-lg font-semibold text-text">
                                            {wallet.name}
                                        </h3>
                                        <p className="text-metallic-gray text-sm">
                                            {wallet.wallet_type} · {wallet.currency}
                                        </p>
                                        <p className="mt-2 font-bold text-text">
                                            {Number(wallet.balance || 0).toFixed(2)}{' '}
                                            {wallet.currency}
                                        </p>
                                    </div>
                                    <div className="mt-4 flex justify-end">
                                        <Button
                                            variant="outline"
                                            className="text-sm"
                                            onClick={() =>
                                                handleAddTransactionClick(wallet)
                                            }
                                        >
                                            Add Transaction
                                        </Button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </Card>
            </div>

            {/* Right: Overview / Summary */}
            <div className="space-y-6">
                <Card title="Wallets Overview">
                    {totalWallets === 0 ? (
                        <p className="text-sm text-metallic-gray">
                            Once you create wallets, you’ll see an overview of your total
                            balance and quick stats here.
                        </p>
                    ) : (
                        <div className="space-y-4">
                            <div className="flex items-center justify-between">
                                <span className="text-sm text-metallic-gray">
                                    Total wallets
                                </span>
                                <span className="text-base font-semibold text-text">
                                    {totalWallets}
                                </span>
                            </div>

                            <div className="flex items-center justify-between">
                                <span className="text-sm text-metallic-gray">
                                    Combined balance
                                </span>
                                <span className="text-base font-semibold text-text">
                                    {totalBalance.toFixed(2)}
                                </span>
                            </div>

                            <p className="text-xs text-metallic-gray mt-2">
                                * Balances are shown in each wallet’s own currency. For
                                unified stats and currency conversion, check the main
                                dashboard.
                            </p>
                        </div>
                    )}
                </Card>

                <Card title="Tips">
                    <ul className="list-disc list-inside text-sm text-metallic-gray space-y-2">
                        <li>
                            Use multiple wallets to separate your expenses (e.g., Cash,
                            Main Card, Savings).
                        </li>
                        <li>
                            Add transactions frequently to keep your analytics and goals
                            accurate.
                        </li>
                        <li>
                            You can always edit wallets and manage transactions from the
                            dashboard or the wallets page.
                        </li>
                    </ul>
                </Card>
            </div>

            {/* Create Wallet Modal */}
            <CreateWalletModal
                isOpen={isCreateModalOpen}
                onClose={handleCloseCreateWallet}
                onSubmit={handleCreateWallet}
                isLoading={isCreatingWallet}
            />
        </div>
    );
};

export default MyWalletsContent;
