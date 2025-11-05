import React, { useState } from 'react';
import Button from '../ui/Button';

const TransferModal = ({ isOpen, onClose, onSubmit, isLoading, wallets }) => {
    const [formData, setFormData] = useState({
        source_wallet_id: '',
        destination_wallet_id: '',
        amount: '',
        note: ''
    });

    const [errors, setErrors] = useState({});

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));

        // Clear error when user starts typing
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!formData.source_wallet_id) {
            newErrors.source_wallet_id = 'Source wallet is required';
        }

        if (!formData.destination_wallet_id) {
            newErrors.destination_wallet_id = 'Destination wallet is required';
        }

        if (formData.source_wallet_id === formData.destination_wallet_id) {
            newErrors.destination_wallet_id = 'Source and destination wallets must be different';
        }

        if (!formData.amount || parseFloat(formData.amount) <= 0) {
            newErrors.amount = 'Valid amount is required';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        const submitData = {
            ...formData,
            amount: parseFloat(formData.amount)
        };

        onSubmit(submitData);
    };

    const handleClose = () => {
        setFormData({
            source_wallet_id: '',
            destination_wallet_id: '',
            amount: '',
            note: ''
        });
        setErrors({});
        onClose();
    };

    const getWalletCurrency = (walletId) => {
        const wallet = wallets.find(w => w.id === parseInt(walletId));
        return wallet ? wallet.currency : '';
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
                <div className="p-6">
                    <h2 className="text-xl font-bold text-text mb-6 text-center">Transfer Funds</h2>

                    <form onSubmit={handleSubmit}>
                        {/* Source Wallet */}
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-text mb-2">
                                From Wallet
                            </label>
                            <select
                                name="source_wallet_id"
                                value={formData.source_wallet_id}
                                onChange={handleChange}
                                className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent ${
                                    errors.source_wallet_id ? 'border-red-500' : 'border-strokes'
                                }`}
                            >
                                <option value="">Select Source Wallet</option>
                                {wallets.map(wallet => (
                                    <option key={wallet.id} value={wallet.id}>
                                        {wallet.name} ({wallet.currency})
                                    </option>
                                ))}
                            </select>
                            {errors.source_wallet_id && (
                                <p className="text-red-500 text-xs mt-1">{errors.source_wallet_id}</p>
                            )}
                        </div>

                        {/* Destination Wallet */}
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-text mb-2">
                                To Wallet
                            </label>
                            <select
                                name="destination_wallet_id"
                                value={formData.destination_wallet_id}
                                onChange={handleChange}
                                className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent ${
                                    errors.destination_wallet_id ? 'border-red-500' : 'border-strokes'
                                }`}
                            >
                                <option value="">Select Destination Wallet</option>
                                {wallets.map(wallet => (
                                    <option key={wallet.id} value={wallet.id}>
                                        {wallet.name} ({wallet.currency})
                                    </option>
                                ))}
                            </select>
                            {errors.destination_wallet_id && (
                                <p className="text-red-500 text-xs mt-1">{errors.destination_wallet_id}</p>
                            )}
                        </div>

                        {/* Amount */}
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-text mb-2">
                                Amount
                            </label>
                            <div className="relative">
                                <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">
                                    {getWalletCurrency(formData.source_wallet_id) || '$'}
                                </span>
                                <input
                                    type="number"
                                    name="amount"
                                    value={formData.amount}
                                    onChange={handleChange}
                                    placeholder="0.00"
                                    step="0.01"
                                    min="0"
                                    className={`w-full pl-12 p-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent ${
                                        errors.amount ? 'border-red-500' : 'border-strokes'
                                    }`}
                                />
                            </div>
                            {errors.amount && (
                                <p className="text-red-500 text-xs mt-1">{errors.amount}</p>
                            )}
                        </div>

                        {/* Note */}
                        <div className="mb-6">
                            <label className="block text-sm font-medium text-text mb-2">
                                Note (Optional)
                            </label>
                            <textarea
                                name="note"
                                value={formData.note}
                                onChange={handleChange}
                                placeholder="Add a note about this transfer..."
                                rows="3"
                                className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent resize-none"
                            />
                        </div>

                        {/* Exchange Rate Info */}
                        {formData.source_wallet_id && formData.destination_wallet_id &&
                            getWalletCurrency(formData.source_wallet_id) !== getWalletCurrency(formData.destination_wallet_id) && (
                                <div className="mb-4 p-3 bg-blue-50 rounded-lg">
                                    <p className="text-sm text-blue-700">
                                        <strong>Note:</strong> This transfer involves currency conversion from{' '}
                                        {getWalletCurrency(formData.source_wallet_id)} to{' '}
                                        {getWalletCurrency(formData.destination_wallet_id)}.
                                        The exchange rate will be applied automatically.
                                    </p>
                                </div>
                            )}

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
                                {isLoading ? 'Transferring...' : 'Transfer Funds'}
                            </Button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default TransferModal;