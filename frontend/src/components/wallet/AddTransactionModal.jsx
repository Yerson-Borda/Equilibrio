import React, { useState } from 'react';
import Button from '../ui/Button';

const AddTransactionModal = ({ isOpen, onClose, onSubmit, isLoading, wallets, selectedWallet }) => {
    const [formData, setFormData] = useState({
        type: 'expense',
        amount: '',
        wallet_id: selectedWallet?.id || '',
        category: '',
        note: '',
        attachments: ''
    });

    const categories = [
        'Shopping', 'Food & Dining', 'Transportation', 'Entertainment',
        'Bills & Utilities', 'Healthcare', 'Education', 'Others'
    ];

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(formData);
    };

    const resetForm = () => {
        setFormData({
            type: 'expense',
            amount: '',
            wallet_id: selectedWallet?.id || '',
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
                                onClick={() => setFormData(prev => ({ ...prev, type: 'income' }))}
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
                                onClick={() => setFormData(prev => ({ ...prev, type: 'expense' }))}
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
                                onClick={() => setFormData(prev => ({ ...prev, type: 'transfer' }))}
                            >
                                Transfer
                            </button>
                        </div>

                        {/* Amount */}
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-text mb-2">
                                Amount
                            </label>
                            <div className="relative">
                                <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">
                                    {formData.type === 'income' ? '+' : '-'}$
                                </span>
                                <input
                                    type="number"
                                    name="amount"
                                    value={formData.amount}
                                    onChange={handleChange}
                                    placeholder="0.00"
                                    step="0.01"
                                    min="0"
                                    className="w-full pl-12 p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                    required
                                />
                            </div>
                        </div>

                        {/* Wallet */}
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-text mb-2">
                                Wallet
                            </label>
                            <select
                                name="wallet_id"
                                value={formData.wallet_id}
                                onChange={handleChange}
                                className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                required
                            >
                                <option value="">Select Wallet</option>
                                {wallets.map(wallet => (
                                    <option key={wallet.id} value={wallet.id}>
                                        {wallet.name}
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
                                className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                required
                            >
                                <option value="">Select Category</option>
                                {categories.map(category => (
                                    <option key={category} value={category}>
                                        {category}
                                    </option>
                                ))}
                            </select>
                        </div>

                        {/* Note */}
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-text mb-2">
                                Note
                            </label>
                            <input
                                type="text"
                                name="note"
                                value={formData.note}
                                onChange={handleChange}
                                placeholder="Add note"
                                className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                            />
                        </div>

                        {/* Attachments */}
                        <div className="mb-6">
                            <label className="block text-sm font-medium text-text mb-2">
                                Add Attachments
                            </label>
                            <input
                                type="file"
                                name="attachments"
                                onChange={handleChange}
                                className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                            />
                        </div>

                        {/* Buttons */}
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