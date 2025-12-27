import React, { useEffect } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Select from '../ui/Select';

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

const EditWalletModal = ({ isOpen, onClose, onSubmit, isLoading, wallet }) => {
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

    // Populate form when wallet changes
    useEffect(() => {
        if (wallet) {
            setFormData({
                name: wallet.name || '',
                currency: wallet.currency || 'USD',
                wallet_type: wallet.wallet_type || 'debit_card',
                initial_balance:
                    wallet.balance !== undefined && wallet.balance !== null
                        ? String(wallet.balance)
                        : '',
                card_number: wallet.card_number || '',
                color: wallet.color || '#6FBAFC',
            });
        }
    }, [wallet, setFormData]);

    if (!isOpen || !wallet) return null;

    const currentCurrencySymbol = () => getCurrencySymbol(formData.currency);

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!validate()) return;

        const submitData = {
            ...formData,
            initial_balance: parseFloat(formData.initial_balance) || 0,
            card_number: formData.card_number.replace(/\s/g, ''),
        };

        onSubmit(submitData, wallet.id);
    };

    const handleClose = () => {
        resetErrors();
        onClose();
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={handleClose}
            title="Edit Wallet"
            size="xl"
        >
            <div className="grid md:grid-cols-2 gap-8">
                {/* Card Preview */}
                <div className="flex flex-col items-center space-y-4 mt-4">
                    <div
                        className="bg-gradient-to-r p-6 text-white rounded-xl shadow-lg"
                        style={{
                            width: '354px',
                            height: '220px',
                            background: `linear-gradient(to right, ${formData.color}, ${formData.color})`,
                        }}
                    >
                        <div className="flex justify-between items-center mb-4">
                            <img src={chipIcon} alt="Chip" className="h-8" />
                            <img src={nfcIcon} alt="NFC" className="h-6 opacity-80" />
                        </div>

                        <div className="mb-4">
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

                {/* Form */}
                <div>
                    <form onSubmit={handleSubmit} className="space-y-4">
                        {/* Wallet Name */}
                        <Input
                            label="Wallet Name"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            placeholder="Main Card, Cash, Savings..."
                            error={errors.name}
                        />

                        {/* Initial Balance */}
                        <div>
                            <label className="block text-sm font-medium text-text mb-1">
                                Initial value
                            </label>
                            <div className="relative">
                                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">
                                    {currentCurrencySymbol()}
                                </span>
                                <Input
                                    name="initial_balance"
                                    type="number"
                                    value={formData.initial_balance}
                                    onChange={handleChange}
                                    placeholder="0.00"
                                    step="0.01"
                                    min="0"
                                    className="pl-10"
                                    error={errors.initial_balance}
                                />
                            </div>
                        </div>

                        {/* Currency */}
                        <Select
                            label="Currency"
                            name="currency"
                            value={formData.currency}
                            onChange={handleChange}
                        >
                            {CURRENCIES.map((c) => (
                                <option key={c.code} value={c.code}>
                                    {c.label}
                                </option>
                            ))}
                        </Select>

                        {/* Wallet Type */}
                        <Select
                            label="Type"
                            name="wallet_type"
                            value={formData.wallet_type}
                            onChange={handleChange}
                        >
                            {walletTypes.map((t) => (
                                <option key={t.value} value={t.value}>
                                    {t.label}
                                </option>
                            ))}
                        </Select>

                        {/* Card Number */}
                        <Input
                            label="Card number (optional)"
                            name="card_number"
                            value={formData.card_number}
                            onChange={handleChange}
                            placeholder="1234 5678 9012 3456"
                            error={errors.card_number}
                        />

                        {/* Color Picker */}
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
                                ].map((color) => (
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
                                            setFormData((prev) => ({
                                                ...prev,
                                                color,
                                            }))
                                        }
                                    />
                                ))}
                            </div>
                        </div>

                        {/* Actions */}
                        <div className="flex space-x-3 mt-6">
                            <Button
                                type="submit"
                                variant="primary"
                                className="flex-1 py-3"
                                disabled={isLoading}
                            >
                                {isLoading ? 'Updating...' : 'Update Wallet'}
                            </Button>
                            <Button
                                type="button"
                                variant="secondary"
                                className="flex-1 py-3"
                                onClick={handleClose}
                                disabled={isLoading}
                            >
                                Cancel
                            </Button>
                        </div>
                    </form>
                </div>
            </div>
        </Modal>
    );
};

export default EditWalletModal;
