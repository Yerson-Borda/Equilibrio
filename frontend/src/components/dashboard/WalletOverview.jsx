import React from 'react';
import Card from '../ui/Card';
import Button from '../ui/Button';
import { formatCurrency } from '../../config/currencies';

const WalletOverview = ({
                            wallets = [],
                            totalBalance = 0,
                            totalSaved = 0,
                            defaultCurrency = 'USD',
                            onCreateWallet,
                        }) => {
    const formatCardNumber = (number) => {
        if (!number) return '•••• •••• •••• ••••';
        const digits = number.replace(/\D/g, '').slice(0, 16);
        return digits.replace(/(\d{4})/g, '$1 ').trim();
    };

    const topWallet = wallets[0];

    return (
        <div className="grid grid-cols-1 xl:grid-cols-[2fr,1.3fr] gap-6">
            {/* Left: Primary wallet card + quick stats */}
            <Card
                title="Wallet Overview"
                headerRight={
                    <Button
                        variant="outline"
                        className="text-xs"
                        onClick={onCreateWallet}
                    >
                        + New wallet
                    </Button>
                }
            >
                {wallets.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-10">
                        <p className="text-sm text-metallic-gray mb-4 text-center">
                            You don&apos;t have any wallets yet. Create one to get an
                            overview of your finances.
                        </p>
                        <Button variant="primary" onClick={onCreateWallet}>
                            Create your first wallet
                        </Button>
                    </div>
                ) : (
                    <div className="space-y-6">
                        {/* Card preview */}
                        <div
                            className="rounded-2xl text-white shadow-lg relative overflow-hidden"
                            style={{
                                background: `linear-gradient(to right, ${
                                    topWallet.color || '#4361ee'
                                }, ${topWallet.color || '#4361ee'})`,
                            }}
                        >
                            <div className="p-5 h-full flex flex-col justify-between">
                                <div className="mb-4">
                                    <p className="text-xs uppercase opacity-80 mb-1">
                                        {topWallet.wallet_type?.replace('_', ' ') ||
                                            'Wallet'}
                                    </p>
                                    <p className="text-xl font-semibold">
                                        {topWallet.name || 'Wallet Name'}
                                    </p>
                                </div>

                                <div className="mb-4">
                                    <p className="text-xs uppercase opacity-80 mb-1">
                                        Card Number
                                    </p>
                                    <p className="text-lg tracking-[0.2em] font-mono">
                                        {formatCardNumber(topWallet.card_number)}
                                    </p>
                                </div>

                                <div className="flex justify-between items-center">
                                    <div>
                                        <p className="text-xs uppercase opacity-80 mb-1">
                                            Balance
                                        </p>
                                        <p className="text-lg font-bold">
                                            {Number(topWallet.balance || 0).toFixed(2)}{' '}
                                            {topWallet.currency}
                                        </p>
                                    </div>
                                    <div className="text-right text-xs opacity-80">
                                        <p>
                                            Total wallets:{' '}
                                            <span className="font-semibold">
                                                {wallets.length}
                                            </span>
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Wallet list mini-summary */}
                        <div className="space-y-2">
                            <p className="text-xs font-medium text-metallic-gray mb-1">
                                Other wallets
                            </p>
                            <div className="space-y-1 max-h-32 overflow-y-auto">
                                {wallets.slice(1).map((w) => (
                                    <div
                                        key={w.id}
                                        className="flex items-center justify-between text-xs text-metallic-gray"
                                    >
                                        <span className="truncate mr-2">
                                            {w.name} ({w.currency})
                                        </span>
                                        <span className="font-medium text-text">
                                            {Number(w.balance || 0).toFixed(2)}
                                        </span>
                                    </div>
                                ))}
                                {wallets.length === 1 && (
                                    <p className="text-xs text-metallic-gray">
                                        No more wallets yet.
                                    </p>
                                )}
                            </div>
                        </div>
                    </div>
                )}
            </Card>

            {/* Right: totals */}
            <div className="space-y-4">
                <Card title="Totals">
                    <div className="space-y-3 text-sm">
                        <div className="flex items-center justify-between">
                            <span className="text-metallic-gray">
                                Total balance (all wallets)
                            </span>
                            <span className="font-semibold text-text">
                                {formatCurrency(totalBalance, defaultCurrency)}
                            </span>
                        </div>

                        <div className="flex items-center justify-between">
                            <span className="text-metallic-gray">Total saved</span>
                            <span className="font-semibold text-text">
                                {formatCurrency(totalSaved, defaultCurrency)}
                            </span>
                        </div>

                        <p className="text-xs text-metallic-gray mt-2">
                            These totals are converted to your default currency when
                            possible. For detailed analytics, check the main dashboard.
                        </p>
                    </div>
                </Card>
            </div>
        </div>
    );
};

export default WalletOverview;
