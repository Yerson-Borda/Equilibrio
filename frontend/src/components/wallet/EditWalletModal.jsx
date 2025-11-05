import React, { useState, useEffect } from 'react';
import Button from '../ui/Button';

// Import your custom icons
import visaIcon from '../../assets/icons/visa-icon.png';
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';

const EditWalletModal = ({ isOpen, onClose, onSubmit, isLoading, wallet }) => {
    const [formData, setFormData] = useState({
        name: '',
        currency: 'USD',
        wallet_type: 'debit_card',
        initial_balance: '',
        card_number: '',
        color: '#6FBAFC'
    });

    const [errors, setErrors] = useState({});

    const currencies = [
        { value: 'USD', label: 'USD - US Dollar', symbol: '$' },
        { value: 'EUR', label: 'EUR - Euro', symbol: '€' },
        { value: 'GBP', label: 'GBP - British Pound', symbol: '£' },
        { value: 'JPY', label: 'JPY - Japanese Yen', symbol: '¥' },
        { value: 'CAD', label: 'CAD - Canadian Dollar', symbol: 'CA$' },
        { value: 'AUD', label: 'AUD - Australian Dollar', symbol: 'A$' }
    ];

    const walletTypes = [
        { value: 'debit_card', label: 'Debit Card' },
        { value: 'cash', label: 'Cash' },
        { value: 'credit_card', label: 'Credit Card' },
        { value: 'saving_account', label: 'Saving Account' },
        { value: 'investment', label: 'Investment' },
        { value: 'loan', label: 'Loan' }
    ];

    useEffect(() => {
        if (wallet) {
            setFormData({
                name: wallet.name || '',
                currency: wallet.currency || 'USD',
                wallet_type: wallet.wallet_type || 'debit_card',
                initial_balance: wallet.balance || '',
                card_number: wallet.card_number || '',
                color: wallet.color || '#6FBAFC'
            });
        }
    }, [wallet]);

    const getCurrentCurrencySymbol = () => {
        const currency = currencies.find(c => c.value === formData.currency);
        return currency ? currency.symbol : '$';
    };

    const formatCardNumber = (number) => {
        const cleaned = number.replace(/\s/g, '');
        const chunks = cleaned.match(/.{1,4}/g);
        return chunks ? chunks.join(' ') : '';
    };

    const handleChange = (e) => {
        const { name, value } = e.target;

        if (name === 'card_number') {
            const numbersOnly = value.replace(/\D/g, '').slice(0, 16);
            setFormData(prev => ({
                ...prev,
                [name]: numbersOnly
            }));
            return;
        }

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

        if (!formData.name.trim()) {
            newErrors.name = 'Wallet name is required';
        }

        if (!formData.initial_balance || parseFloat(formData.initial_balance) < 0) {
            newErrors.initial_balance = 'Valid initial balance is required';
        }

        if (formData.card_number && formData.card_number.length !== 16) {
            newErrors.card_number = 'Card number must be 16 digits';
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
            initial_balance: parseFloat(formData.initial_balance) || 0,
            card_number: formData.card_number.replace(/\s/g, '')
        };

        onSubmit(submitData, wallet.id);
    };

    const handleClose = () => {
        setErrors({});
        onClose();
    };

    if (!isOpen || !wallet) return null;

    const currentCurrencySymbol = getCurrentCurrencySymbol();

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="flex flex-col items-center space-y-4 mt-16">
                {/* Card Preview */}
                <div
                    className="bg-gradient-to-r from-[#6FBAFC] to-[#6FBAFC] p-6 text-white rounded-xl shadow-lg"
                    style={{
                        width: '354px',
                        height: '220px',
                        background: `linear-gradient(to right, ${formData.color}, ${formData.color})`
                    }}
                >
                    <div className="flex justify-between items-start mb-4">
                        <div className="flex items-center space-x-3">
                            <div className="w-12 h-8 flex items-center justify-center">
                                <img
                                    src={chipIcon}
                                    alt="Chip"
                                    className="w-full h-full object-contain"
                                />
                            </div>
                            <div>
                                <h3 className="text-lg font-semibold">
                                    {formData.name || 'T-Bank'}
                                </h3>
                                <p className="text-sm opacity-75">Total Balance</p>
                                <p className="text-xl font-bold">
                                    {currentCurrencySymbol}{formData.initial_balance || '0.00'}
                                </p>
                            </div>
                        </div>

                        <div className="w-10 h-10 flex items-center justify-center">
                            <img
                                src={nfcIcon}
                                alt="NFC"
                                className="w-full h-full object-contain"
                            />
                        </div>
                    </div>

                    <div className="mb-4">
                        <span className="tracking-wider font-mono text-2xl font-bold">
                            {formatCardNumber(formData.card_number) || '5495 7381 3759 2321'}
                        </span>
                    </div>

                    <div className="flex justify-between items-center">
                        <span className="text-lg">09/30</span>
                        <div className="text-right">
                            <div className="flex items-center justify-end">
                                <img
                                    src={visaIcon}
                                    alt="VISA"
                                    className="h-6 object-contain"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                {/* Form Section */}
                <div
                    className="bg-white rounded-xl shadow-lg p-6"
                    style={{
                        width: '354px',
                        height: '650px'
                    }}
                >
                    <form onSubmit={handleSubmit} className="h-full flex flex-col justify-between">
                        <div className="space-y-4">
                            {/* Initial Balance */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Initial Balance
                                </label>
                                <div className="relative">
                                    <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">
                                        {currentCurrencySymbol}
                                    </span>
                                    <input
                                        type="number"
                                        name="initial_balance"
                                        value={formData.initial_balance}
                                        onChange={handleChange}
                                        placeholder="0.00"
                                        step="0.01"
                                        min="0"
                                        className={`w-full pl-10 p-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent ${
                                            errors.initial_balance ? 'border-red-500' : 'border-strokes'
                                        }`}
                                    />
                                </div>
                                {errors.initial_balance && (
                                    <p className="text-red-500 text-xs mt-1">{errors.initial_balance}</p>
                                )}
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
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                >
                                    {currencies.map(currency => (
                                        <option key={currency.value} value={currency.value}>
                                            {currency.label} ({currency.symbol})
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {/* Card Number */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Card Number
                                </label>
                                <input
                                    type="text"
                                    name="card_number"
                                    value={formatCardNumber(formData.card_number)}
                                    onChange={handleChange}
                                    placeholder="5495 7381 3759 2321"
                                    maxLength="19"
                                    className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent ${
                                        errors.card_number ? 'border-red-500' : 'border-strokes'
                                    }`}
                                />
                                {errors.card_number && (
                                    <p className="text-red-500 text-xs mt-1">{errors.card_number}</p>
                                )}
                            </div>

                            {/* Wallet Name */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Wallet Name
                                </label>
                                <input
                                    type="text"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    placeholder="Enter wallet name"
                                    className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent ${
                                        errors.name ? 'border-red-500' : 'border-strokes'
                                    }`}
                                />
                                {errors.name && (
                                    <p className="text-red-500 text-xs mt-1">{errors.name}</p>
                                )}
                            </div>

                            {/* Type */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Type
                                </label>
                                <select
                                    name="wallet_type"
                                    value={formData.wallet_type}
                                    onChange={handleChange}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                >
                                    {walletTypes.map(type => (
                                        <option key={type.value} value={type.value}>
                                            {type.label}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {/* Color */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Color
                                </label>
                                <div className="flex space-x-2">
                                    {['#6FBAFC', '#4E5C75', '#9C9C9C', '#0B0C10', '#4361ee', '#D06978'].map(color => (
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
                        <div className="flex space-x-3 mt-8">
                            <Button
                                type="submit"
                                variant="primary"
                                className="flex-1 py-3"
                                disabled={isLoading}
                                style={{ backgroundColor: '#4361ee' }}
                            >
                                {isLoading ? 'Updating...' : 'Update Wallet'}
                            </Button>
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
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default EditWalletModal;