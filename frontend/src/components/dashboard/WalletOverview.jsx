import React from 'react';
import Button from '../ui/Button';

const WalletOverview = ({ wallets, totalBalance, totalSaved, onCreateWallet }) => {
    const formatCardNumber = (number) => {
        if (!number) return '5495 7381 3759 2321';
        return number.replace(/(\d{4})/g, '$1 ').trim();
    };

    const getCardType = (type) => {
        const typeMap = {
            'debit': 'VISA',
            'credit': 'VISA',
            'cash': 'CASH',
            'savings': 'SAVINGS',
            'investment': 'INVESTMENT',
            'loan': 'LOAN'
        };
        return typeMap[type] || 'VISA';
    };

    return (
        <div className="max-w-6xl mx-auto pt-8">
            {/* Total Balance Section */}
            <div className="mb-8">
                <h2 className="text-lg font-semibold text-metallic-gray mb-2">Total Balance</h2>
                <p className="text-3xl font-bold text-gray-900">${totalBalance.toFixed(2)}</p>
            </div>

            {/* Wallets List */}
            <div className="space-y-6 mb-8">
                {wallets.map((wallet) => (
                    <div key={wallet.id} className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
                        {/* Wallet Header */}
                        <div className="flex justify-between items-start mb-6">
                            <div>
                                <h3 className="text-lg font-semibold text-text mb-1">{wallet.name}</h3>
                                <p className="text-sm text-metallic-gray mb-2">Total Balance</p>
                                <p className="text-2xl font-bold text-text">${wallet.balance || '0.00'}</p>
                            </div>
                        </div>

                        {/* Card Preview */}
                        <div
                            className="bg-gradient-to-r from-[#2260FF] to-[#2260FF] rounded-2xl p-6 text-white shadow-lg"
                            style={{
                                background: wallet.color ? `linear-gradient(to right, ${wallet.color}, ${wallet.color})` : undefined
                            }}
                        >
                            <div className="flex justify-between items-center mb-6">
                                <span className="text-sm opacity-90">Saved</span>
                                <span className="text-lg font-bold">{wallet.saved_amount || '00'}</span>
                            </div>
                            <div className="flex justify-between items-center mb-2">
                                <span className="tracking-wider font-mono text-xl">
                                    {formatCardNumber(wallet.card_number)}
                                </span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-sm opacity-90">{wallet.expiry_date || '09/30'}</span>
                                <span className="text-sm font-semibold">{getCardType(wallet.type)}</span>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {/* Add Wallet Card */}
            <div className="bg-white rounded-xl shadow-sm p-8 border border-strokes border-dashed">
                <div className="text-center">
                    <h3 className="text-xl font-bold text-text mb-6">Add New Wallet</h3>

                    <div className="bg-gray-50 rounded-xl p-6 mb-6">
                        <div className="space-y-4 text-left">
                            {/* Initial Value */}
                            <div>
                                <label className="block text-sm font-medium text-metallic-gray mb-2">
                                    Initial value
                                </label>
                                <div className="text-text font-semibold">$0.00</div>
                            </div>

                            {/* Currency */}
                            <div>
                                <label className="block text-sm font-medium text-metallic-gray mb-2">
                                    Currency
                                </label>
                                <div className="text-text font-semibold">USD - US Dollar</div>
                            </div>

                            {/* Card Number */}
                            <div>
                                <label className="block text-sm font-medium text-metallic-gray mb-2">
                                    Card number
                                </label>
                                <div className="text-text">Enter card number</div>
                            </div>

                            {/* Wallet Name */}
                            <div>
                                <label className="block text-sm font-medium text-metallic-gray mb-2">
                                    Wallet name
                                </label>
                                <div className="text-text">Enter wallet name</div>
                            </div>

                            {/* Type */}
                            <div>
                                <label className="block text-sm font-medium text-metallic-gray mb-2">
                                    Type
                                </label>
                                <div className="text-text font-semibold">Debit Card</div>
                            </div>

                            {/* Color */}
                            <div>
                                <label className="block text-sm font-medium text-metallic-gray mb-2">
                                    Color
                                </label>
                                <div className="flex space-x-2">
                                    <div className="w-6 h-6 bg-blue rounded-full"></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <Button
                        variant="primary"
                        onClick={onCreateWallet}
                        className="w-full py-4 text-lg font-semibold rounded-xl"
                    >
                        Add Wallet
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default WalletOverview;