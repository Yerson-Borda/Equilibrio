import React, { useState } from 'react';
import Button from '../ui/Button';

import visaIcon from '../../assets/icons/visa-icon.png';
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';

import { CURRENCIES, getCurrencySymbol } from '../../config/currencies';

const walletTypes = [
    { value: 'debit_card', label: 'Debit Card' },
    { value: 'cash', label: 'Cash' },
    { value: 'credit_card', label: 'Credit Card' },
    { value: 'saving_account', label: 'Saving Account' },
    { value: 'investment', label: 'Investment' },
    { value: 'loan', label: 'Loan' },
];

const colorOptions = [
    '#6FBAFC', // light blue (default)
    '#4ADE80', // green
    '#FBBF24', // yellow
    '#F87171', // red
    '#A78BFA', // purple
    '#F472B6', // pink
    '#34D399', // teal
    '#60A5FA', // blue
];

const initialFormState = {
    name: '',
    currency: 'USD',
    wallet_type: 'debit_card',
    initial_balance: '',
    card_number: '',
    color: '#6FBAFC',
};

const CreateWalletModal = ({ isOpen, onClose, onSubmit, isLoading }) => {
    const [formData, setFormData] = useState(initialFormState);
    const [errors, setErrors] = useState({});

    if (!isOpen) return null;

    const resetForm = () => {
        setFormData(initialFormState);
        setErrors({});
    };

    const currentCurrencySymbol = getCurrencySymbol(formData.currency);

    const formatCardNumber = (value) => {
        const cleaned = (value || '').replace(/\D/g, '').slice(0, 16);
        const chunks = cleaned.match(/.{1,4}/g);
        return chunks ? chunks.join(' ') : '';
    };

    const handleChange = (e) => {
        const { name, value } = e.target;

        if (name === 'card_number') {
            const cleaned = value.replace(/\D/g, '').slice(0, 16);
            setFormData((prev) => ({
                ...prev,
                [name]: cleaned,
            }));
        } else {
            setFormData((prev) => ({
                ...prev,
                [name]: value,
            }));
        }

        if (errors[name]) {
            setErrors((prev) => ({ ...prev, [name]: '' }));
        }
    };

    const validate = () => {
        const newErrors = {};

        if (!formData.name.trim()) {
            newErrors.name = 'Wallet name is required';
        }
        if (
            formData.initial_balance === '' ||
            Number.isNaN(parseFloat(formData.initial_balance)) ||
            parseFloat(formData.initial_balance) < 0
        ) {
            newErrors.initial_balance = 'Valid initial balance is required';
        }
        if (formData.card_number) {
            const digits = formData.card_number.replace(/\s/g, '');
            if (digits.length !== 16) {
                newErrors.card_number = 'Card number must be 16 digits';
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!validate()) return;

        const submitData = {
            ...formData,
            initial_balance: parseFloat(formData.initial_balance) || 0,
            card_number: formData.card_number.replace(/\s/g, ''),
        };

        onSubmit(submitData);
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="flex flex-col items-center space-y-4 mt-4">
                {/* Card Preview */}
                <div
                    className="p-6 text-[#0B0C10] rounded-xl shadow-lg relative transition-colors duration-300"
                    style={{
                        width: '345px',
                        height: '200px',
                        backgroundColor: formData.color,
                    }}
                >
                    <div className="text-left mb-2">
                        <h3 className="text-base font-semibold">
                            {formData.name || 'T-Банк'}
                        </h3>
                    </div>

                    <div className="flex justify-between items-start mb-6">
                        <div className="flex items-start space-x-3">
                            <img
                                src={chipIcon}
                                alt="Chip"
                                className="w-10 h-8 object-contain mt-4"
                            />
                            <div>
                                <p className="text-xs opacity-75 font-bold leading-none ml-6 mt-3">
                                    Total Balance
                                </p>
                                <p className="text-xl font-bold leading-tight ml-6">
                                    {currentCurrencySymbol}
                                    {formData.initial_balance || '0.00'}
                                </p>
                            </div>
                        </div>
                        <img
                            src={nfcIcon}
                            alt="NFC"
                            className="w-8 h-7 object-contain mt-3"
                        />
                    </div>

                    <div className="mb-0">
                        <span className="tracking-wider font-mono text-lg font-semibold">
                            {formatCardNumber(formData.card_number) ||
                                '5495 7381 3759 2321'}
                        </span>
                    </div>

                    <div className="flex justify-between items-center mb-10">
                        <span className="text-sm opacity-80">09/30</span>
                        <img
                            src={visaIcon}
                            alt="VISA"
                            className="h-8 object-contain"
                        />
                    </div>
                </div>

                {/* Form box */}
                <div
                    className="bg-white rounded-xl shadow-lg p-6"
                    style={{ width: '345px', height: '660px' }}
                >
                    <form
                        onSubmit={handleSubmit}
                        className="h-full flex flex-col justify-between"
                    >
                        <div className="space-y-4">
                            {/* Initial Balance */}
                            <div>
                                <label className="block text-sm font-medium text-[#929EAE] mb-2">
                                    Initial value
                                </label>
                                <div className="relative">
                                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">
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
                                        className="w-full p-3 pl-10 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                    />
                                </div>
                                {errors.initial_balance && (
                                    <p className="mt-1 text-xs text-red-500">
                                        {errors.initial_balance}
                                    </p>
                                )}
                            </div>

                            {/* Currency */}
                            <div>
                                <label className="block text-sm font-medium text-[#929EAE] mb-2">
                                    Currency
                                </label>
                                <select
                                    name="currency"
                                    value={formData.currency}
                                    onChange={handleChange}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent cursor-pointer"
                                >
                                    {CURRENCIES.map((currency) => (
                                        <option key={currency.code} value={currency.code}>
                                            {currency.label} ({currency.symbol})
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {/* Card Number */}
                            <div>
                                <label className="block text-sm font-medium text-[#929EAE] mb-2">
                                    Card number
                                </label>
                                <input
                                    type="text"
                                    name="card_number"
                                    value={formatCardNumber(formData.card_number)}
                                    onChange={handleChange}
                                    placeholder="5495 7381 3759 2321"
                                    maxLength={19}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                />
                                {errors.card_number && (
                                    <p className="mt-1 text-xs text-red-500">
                                        {errors.card_number}
                                    </p>
                                )}
                            </div>

                            {/* Wallet Name */}
                            <div>
                                <label className="block text-sm font-medium text-[#929EAE] mb-2">
                                    Wallet name
                                </label>
                                <input
                                    type="text"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    placeholder="Enter wallet name"
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                />
                                {errors.name && (
                                    <p className="mt-1 text-xs text-red-500">
                                        {errors.name}
                                    </p>
                                )}
                            </div>

                            {/* Type */}
                            <div>
                                <label className="block text-sm font-medium text-[#929EAE] mb-2">
                                    Type
                                </label>
                                <select
                                    name="wallet_type"
                                    value={formData.wallet_type}
                                    onChange={handleChange}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent cursor-pointer"
                                >
                                    {walletTypes.map((type) => (
                                        <option key={type.value} value={type.value}>
                                            {type.label}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {/* Color */}
                            <div>
                                <label className="block text-sm font-medium text-[#929EAE] mb-2">
                                    Color
                                </label>
                                <select
                                    name="color"
                                    value={formData.color}
                                    onChange={handleChange}
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent cursor-pointer"
                                    style={{
                                        backgroundColor: formData.color,
                                        color: 'transparent',
                                        textShadow: '0 0 0 transparent',
                                    }}
                                >
                                    {colorOptions.map((color) => (
                                        <option
                                            key={color}
                                            value={color}
                                            style={{
                                                backgroundColor: color,
                                                borderRadius: '20px',
                                                margin: '4px 0',
                                                height: '24px',
                                                border: '1px solid rgba(0,0,0,0.1)',
                                            }}
                                        >
                                            &nbsp;
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        {/* Buttons */}
                        <div className="flex justify-end space-x-4 mt-8">
                            <Button
                                type="submit"
                                variant="primary"
                                disabled={isLoading}
                                style={{ backgroundColor: '#4361ee' }}
                                className="text-white font-medium rounded-md px-8 py-3 min-w-[170px] hover:opacity-90 transition"
                            >
                                {isLoading ? 'Creating...' : 'Add Wallet'}
                            </Button>

                            <Button
                                type="button"
                                variant="secondary"
                                onClick={handleClose}
                                disabled={isLoading}
                                style={{ backgroundColor: '#D06978' }}
                                className="text-white font-medium rounded-md px-6 py-3 min-w-[110px] hover:opacity-90 transition"
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

export default CreateWalletModal;
