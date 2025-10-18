import React, { useState } from 'react';
import Button from '../ui/Button';

const CreateWalletModal = ({ isOpen, onClose, onSubmit, isLoading }) => {
    const [formData, setFormData] = useState({
        initial_value: '',
        currency: 'USD',
        card_number: '',
        name: '',
        type: 'debit',
        color: '#3B82F6'
    });

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
            initial_value: '',
            currency: 'USD',
            card_number: '',
            name: '',
            type: 'debit',
            color: '#3B82F6'
        });
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
                <div className="p-6">
                    <h2 className="text-xl font-bold text-text mb-4">Create New Wallet</h2>

                    <form onSubmit={handleSubmit}>
                        <div className="space-y-4">
                            {/* Initial Value */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Initial value
                                </label>
                                <input
                                    type="number"
                                    name="initial_value"
                                    value={formData.initial_value}
                                    onChange={handleChange}
                                    placeholder="0.00"
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue"
                                    required
                                />
                            </div>

                            {/* Currency */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Currency
                                </label>
                                <select
                                    name="currency"
                                    value={formData.currency}
                                    onChange={handleChange}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue"
                                >
                                    <option value="USD">USD - US Dollar</option>
                                    <option value="EUR">EUR - Euro</option>
                                    <option value="GBP">GBP - British Pound</option>
                                </select>
                            </div>

                            {/* Card Number */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Card number
                                </label>
                                <input
                                    type="text"
                                    name="card_number"
                                    value={formData.card_number}
                                    onChange={handleChange}
                                    placeholder="Enter card number"
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue"
                                />
                            </div>

                            {/* Wallet Name */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Wallet name
                                </label>
                                <input
                                    type="text"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    placeholder="Enter wallet name"
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue"
                                    required
                                />
                            </div>

                            {/* Type */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Type
                                </label>
                                <select
                                    name="type"
                                    value={formData.type}
                                    onChange={handleChange}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue"
                                >
                                    <option value="debit">Debit Card</option>
                                    <option value="credit">Credit Card</option>
                                    <option value="savings">Savings Account</option>
                                </select>
                            </div>

                            {/* Color */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Color
                                </label>
                                <div className="flex space-x-2">
                                    {['#3B82F6', '#10B981', '#EF4444', '#F59E0B', '#8B5CF6'].map((color) => (
                                        <button
                                            key={color}
                                            type="button"
                                            className={`w-8 h-8 rounded-full border-2 ${
                                                formData.color === color ? 'border-gray-800' : 'border-gray-300'
                                            }`}
                                            style={{ backgroundColor: color }}
                                            onClick={() => setFormData(prev => ({ ...prev, color }))}
                                        />
                                    ))}
                                </div>
                            </div>
                        </div>

                        {/* Buttons */}
                        <div className="flex space-x-3 mt-6">
                            <Button
                                type="button"
                                variant="secondary"
                                onClick={handleClose}
                                className="flex-1"
                                disabled={isLoading}
                            >
                                Cancel
                            </Button>
                            <Button
                                type="submit"
                                variant="primary"
                                className="flex-1"
                                disabled={isLoading}
                            >
                                {isLoading ? 'Creating...' : 'Add Wallet'}
                            </Button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default CreateWalletModal;