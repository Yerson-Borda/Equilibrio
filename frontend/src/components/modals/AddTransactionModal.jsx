import React, { useState, useEffect } from 'react';
import Button from '../ui/Button.jsx';
export { default } from '../modals/AddTransactionModal';

const AddTransactionModal = ({ isOpen, onClose, onSubmit, isLoading, wallets, selectedWallet }) => {
    const [formData, setFormData] = useState({
        type: 'expense',
        amount: '',
        wallet_id: selectedWallet?.id || '',
        source_wallet_id: selectedWallet?.id || '',
        destination_wallet_id: '',
        category: '',
        note: '',
        attachments: ''
    });

    const categories = [
        'Shopping', 'Food & Dining', 'Transportation', 'Entertainment',
        'Bills & Utilities', 'Healthcare', 'Education', 'Others'
    ];

    // Keep wallet selection in sync when modal opens with a different wallet
    useEffect(() => {
        if (isOpen) {
            setFormData(prev => ({
                ...prev,
                wallet_id: selectedWallet?.id || '',
                source_wallet_id: selectedWallet?.id || ''
            }));
        }
    }, [isOpen, selectedWallet]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        // Basic validation
        if (!formData.amount || parseFloat(formData.amount) <= 0) {
            alert('Please enter a valid amount');
            return;
        }

        if (formData.type === 'transfer') {
            if (!formData.source_wallet_id || !formData.destination_wallet_id) {
                alert('Please select both source and destination wallets');
                return;
            }
            if (formData.source_wallet_id === formData.destination_wallet_id) {
                alert('Source and destination wallets must be different');
                return;
            }
        } else {
            if (!formData.wallet_id) {
                alert('Please select a wallet');
                return;
            }
            if (!formData.category) {
                alert('Please select a category');
                return;
            }
        }

        onSubmit(formData);
    };

    const resetForm = () => {
        setFormData({
            type: 'expense',
            amount: '',
            wallet_id: selectedWallet?.id || '',
            source_wallet_id: selectedWallet?.id || '',
            destination_wallet_id: '',
            category: '',
            note: '',
            attachments: ''
        });
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    if (!isOpen) return null;

    const amountPrefix =
        formData.type === 'income'
            ? '+$'
            : formData.type === 'expense'
                ? '-$'
                : '↔';

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
                <div className="p-6">
                    <h2 className="text-xl font-bold text-text mb-6 text-center">Add Transaction</h2>

                    <form onSubmit={handleSubmit}>
                        {/* Transaction Type */}
                        <div className="grid grid-cols-3 gap-2 mb-6">
                            <button
                                type="button"
                                className={`p-3 rounded-lg border-2 text-center ${
                                    formData.type === 'income'
                                        ? 'border-green-500 bg-green-50 text-green-700'
                                        : 'border-strokes text-metallic-gray'
                                }`}
                                onClick={() =>
                                    setFormData(prev => ({
                                        ...prev,
                                        type: 'income'
                                    }))
                                }
                            >
                                Income
                            </button>
                            <button
                                type="button"
                                className={`p-3 rounded-lg border-2 text-center ${
                                    formData.type === 'expense'
                                        ? 'border-red-500 bg-red-50 text-red-700'
                                        : 'border-strokes text-metallic-gray'
                                }`}
                                onClick={() =>
                                    setFormData(prev => ({
                                        ...prev,
                                        type: 'expense'
                                    }))
                                }
                            >
                                Expense
                            </button>
                            <button
                                type="button"
                                className={`p-3 rounded-lg border-2 text-center ${
                                    formData.type === 'transfer'
                                        ? 'border-blue-500 bg-blue-50 text-blue-700'
                                        : 'border-strokes text-metallic-gray'
                                }`}
                                onClick={() =>
                                    setFormData(prev => ({
                                        ...prev,
                                        type: 'transfer'
                                    }))
                                }
                            >
                                Transfer
                            </button>
                        </div>

                        {/* Wallet selection ­– different for transfer vs income/expense */}
                        {formData.type === 'transfer' ? (
                            <>
                                {/* From Wallet */}
                                <div className="mb-4">
                                    <label className="block text-sm font-medium text-text mb-2">
                                        From Wallet
                                    </label>
                                    <select
                                        name="source_wallet_id"
                                        value={formData.source_wallet_id}
                                        onChange={handleChange}
                                        className="w-full border border-strokes rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue"
                                    >
                                        <option value="">Select wallet</option>
                                        {wallets.map(wallet => (
                                            <option key={wallet.id} value={wallet.id}>
                                                {wallet.name} ({wallet.currency})
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                {/* To Wallet */}
                                <div className="mb-4">
                                    <label className="block text-sm font-medium text-text mb-2">
                                        To Wallet
                                    </label>
                                    <select
                                        name="destination_wallet_id"
                                        value={formData.destination_wallet_id}
                                        onChange={handleChange}
                                        className="w-full border border-strokes rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue"
                                    >
                                        <option value="">Select wallet</option>
                                        {wallets.map(wallet => (
                                            <option key={wallet.id} value={wallet.id}>
                                                {wallet.name} ({wallet.currency})
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            </>
                        ) : (
                            <>
                                {/* Wallet (income/expense) */}
                                <div className="mb-4">
                                    <label className="block text-sm font-medium text-text mb-2">
                                        Wallet
                                    </label>
                                    <select
                                        name="wallet_id"
                                        value={formData.wallet_id}
                                        onChange={handleChange}
                                        className="w-full border border-strokes rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue"
                                    >
                                        <option value="">Select wallet</option>
                                        {wallets.map(wallet => (
                                            <option key={wallet.id} value={wallet.id}>
                                                {wallet.name} ({wallet.currency})
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                {/* Category */}
                                <div className="mb-4">
                                    <label className="block text-sm font-medium text-text mb-2">
                                        Category
                                    </label>
                                    <select
                                        name="category"
                                        value={formData.category}
                                        onChange={handleChange}
                                        className="w-full border border-strokes rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue"
                                    >
                                        <option value="">Select category</option>
                                        {categories.map((category, index) => (
                                            <option key={index} value={category}>
                                                {category}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            </>
                        )}

                        {/* Amount */}
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-text mb-2">
                                Amount
                            </label>
                            <div className="relative">
                                <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">
                                    {amountPrefix}
                                </span>
                                <input
                                    type="number"
                                    name="amount"
                                    value={formData.amount}
                                    onChange={handleChange}
                                    placeholder="0.00"
                                    step="0.01"
                                    className="w-full border border-strokes rounded-lg pl-10 pr-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue"
                                />
                            </div>
                        </div>

                        {/* Note */}
                        <div className="mb-6">
                            <label className="block text-sm font-medium text-text mb-2">
                                Note (optional)
                            </label>
                            <textarea
                                name="note"
                                value={formData.note}
                                onChange={handleChange}
                                rows="2"
                                placeholder="Add a short description"
                                className="w-full border border-strokes rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue resize-none"
                            />
                        </div>

                        <div className="flex space-x-3">
                            <Button
                                type="button"
                                variant="secondary"
                                onClick={handleClose}
                                className="flex-1 py-3"
                                disabled={isLoading}
                                style={{ backgroundColor: '#D06978' }}
                            >
                                Cancel
                            </Button>
                            <Button
                                type="submit"
                                variant="primary"
                                className="flex-1 py-3"
                                disabled={isLoading}
                                style={{ backgroundColor: '#4361ee' }}
                            >
                                {isLoading ? 'Adding...' : 'Confirm Transaction'}
                            </Button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default AddTransactionModal;
