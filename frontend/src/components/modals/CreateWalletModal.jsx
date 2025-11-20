import React from 'react';
import Button from '../ui/Button';

import visaIcon from '../../assets/icons/visa-icon.png';
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';

import { CURRENCIES, getCurrencySymbol } from '../../config/currencies';
import { useWalletForm } from '../../hooks/useWalletForm';

const walletTypes = [
    { value: 'debit_card', label: 'Debit Card' },
    { value: 'cash', label: 'Cash' },
    { value: 'credit_card', label: 'Credit Card' },
    { value: 'saving_account', label: 'Saving Account' },
    { value: 'investment', label: 'Investment' },
    { value: 'loan', label: 'Loan' },
];

const CreateWalletModal = ({ isOpen, onClose, onSubmit, isLoading }) => {
    const initialState = {
        name: '',
        currency: 'USD',
        wallet_type: 'debit_card',
        initial_balance: '',
        card_number: '',
        color: '#6FBAFC',
    };

    const {
        formData,
        setFormData,
        errors,
        handleChange,
        validate,
        resetErrors,
    } = useWalletForm(initialState);

    const currentCurrencySymbol = () => getCurrencySymbol(formData.currency);

    const handleSubmit = e => {
        e.preventDefault();

        if (!validate()) return;

        const payload = {
            ...formData,
            initial_balance: parseFloat(formData.initial_balance) || 0,
            card_number: formData.card_number.replace(/\s/g, ''),
        };

        onSubmit(payload);
    };

    const handleClose = () => {
        // reset form back to initial state & errors
        setFormData(initialState);
        resetErrors();
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-xl shadow-xl max-w-2xl w-full">
                <div className="p-6 md:p-8">
                    <h2 className="text-2xl font-bold text-text mb-6 text-center">
                        Create New Wallet
                    </h2>

                    <div className="grid md:grid-cols-2 gap-8">
                        {/* Card Preview */}
                        <div className="flex flex-col items-center space-y-4">
                            <div
                                className="rounded-2xl text-white shadow-lg relative overflow-hidden"
                                style={{
                                    width: '320px',
                                    height: '200px',
                                    background: `linear-gradient(to right, ${formData.color}, ${formData.color})`,
                                }}
                            >
                                <div className="p-5 h-full flex flex-col justify-between">
                                    <div className="flex justify-between items-center">
                                        <img src={chipIcon} alt="Chip" className="h-8" />
                                        <img src={nfcIcon} alt="NFC" className="h-6 opacity-80" />
                                    </div>

                                    <div>
                                        <p className="text-xs uppercase opacity-80 mb-1">
                                            Card Number
                                        </p>
                                        <p className="text-lg tracking-[0.2em] font-mono">
                                            {formData.card_number || '•••• •••• •••• ••••'}
                                        </p>
                                    </div>

                                    <div className="flex justify-between items-center">
                                        <div>
                                            <p className="text-xs uppercase opacity-80 mb-1">
                                                Card Holder
                                            </p>
                                            <p className="font-semibold">
                                                {formData.name || 'Your Name'}
                                            </p>
                                        </div>
                                        <img src={visaIcon} alt="Card Brand" className="h-8" />
                                    </div>
                                </div>
                            </div>

                            <p className="text-sm text-metallic-gray text-center">
                                Customize your wallet card appearance and details.
                            </p>
                        </div>

                        {/* Form */}
                        <div>
                            <form onSubmit={handleSubmit} className="space-y-4">
                                {/* Name */}
                                <div>
                                    <label className="block text-sm font-medium text-text mb-1">
                                        Wallet Name
                                    </label>
                                    <input
                                        type="text"
                                        name="name"
                                        value={formData.name}
                                        onChange={handleChange}
                                        className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                        placeholder="Main Card, Cash, Savings..."
                                    />
                                    {errors.name && (
                                        <p className="text-sm text-red-500 mt-1">{errors.name}</p>
                                    )}
                                </div>

                                {/* Initial Balance */}
                                <div>
                                    <label className="block text-sm font-medium text-text mb-1">
                                        Initial value
                                    </label>
                                    <div className="relative">
                                        <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">
                                            {currentCurrencySymbol()}
                                        </span>
                                        <input
                                            type="number"
                                            name="initial_balance"
                                            value={formData.initial_balance}
                                            onChange={handleChange}
                                            placeholder="0.00"
                                            step="0.01"
                                            min="0"
                                            className="w-full pl-10 p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                        />
                                    </div>
                                    {errors.initial_balance && (
                                        <p className="text-sm text-red-500 mt-1">
                                            {errors.initial_balance}
                                        </p>
                                    )}
                                </div>

                                {/* Currency */}
                                <div>
                                    <label className="block text-sm font-medium text-text mb-1">
                                        Currency
                                    </label>
                                    <select
                                        name="currency"
                                        value={formData.currency}
                                        onChange={handleChange}
                                        className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent max-h-40 overflow-y-auto"
                                    >
                                        {CURRENCIES.map(c => (
                                            <option key={c.code} value={c.code}>
                                                {c.label}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                {/* Type */}
                                <div>
                                    <label className="block text-sm font-medium text-text mb-1">
                                        Type
                                    </label>
                                    <select
                                        name="wallet_type"
                                        value={formData.wallet_type}
                                        onChange={handleChange}
                                        className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                    >
                                        {walletTypes.map(t => (
                                            <option key={t.value} value={t.value}>
                                                {t.label}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                {/* Card number */}
                                <div>
                                    <label className="block text-sm font-medium text-text mb-1">
                                        Card number (optional)
                                    </label>
                                    <input
                                        type="text"
                                        name="card_number"
                                        value={formData.card_number}
                                        onChange={handleChange}
                                        placeholder="1234 5678 9012 3456"
                                        className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                    />
                                    {errors.card_number && (
                                        <p className="text-sm text-red-500 mt-1">
                                            {errors.card_number}
                                        </p>
                                    )}
                                </div>

                                {/* Color */}
                                <div>
                                    <label className="block text-sm font-medium text-text mb-1">
                                        Color
                                    </label>
                                    <div className="flex space-x-2">
                                        {[
                                            '#6FBAFC',
                                            '#4E5C75',
                                            '#9C9C9C',
                                            '#0B0C10',
                                            '#4361ee',
                                            '#D06978',
                                        ].map(color => (
                                            <button
                                                key={color}
                                                type="button"
                                                className={`w-8 h-8 rounded-full border-2 ${
                                                    formData.color === color
                                                        ? 'border-gray-800'
                                                        : 'border-gray-300'
                                                }`}
                                                style={{ backgroundColor: color }}
                                                onClick={() =>
                                                    setFormData(prev => ({
                                                        ...prev,
                                                        color,
                                                    }))
                                                }
                                            />
                                        ))}
                                    </div>
                                </div>

                                {/* Buttons */}
                                <div className="flex space-x-3 mt-6">
                                    <Button
                                        type="submit"
                                        variant="primary"
                                        className="flex-1 py-3"
                                        disabled={isLoading}
                                        style={{ backgroundColor: '#4361ee' }}
                                    >
                                        {isLoading ? 'Creating...' : 'Add Wallet'}
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
            </div>
        </div>
    );
};

export default CreateWalletModal;
