import React, { useEffect, useState } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Select from '../ui/Select';

const transactionTypes = [
    { value: 'expense', label: 'Expense' },
    { value: 'income', label: 'Income' },
    { value: 'transfer', label: 'Transfer' },
];

const AddTransactionModal = ({
                                 isOpen,
                                 onClose,
                                 onSubmit,
                                 isLoading,
                                 wallets = [],
                                 selectedWallet,
                             }) => {
    const [formData, setFormData] = useState({
        type: 'expense',
        amount: '',
        wallet_id: selectedWallet?.id || '',
        source_wallet_id: selectedWallet?.id || '',
        destination_wallet_id: '',
        category_id: '',
        note: '',
        attachments: '',
    });

    const [errors, setErrors] = useState({});

    // Sync with selectedWallet when it changes
    useEffect(() => {
        setFormData((prev) => ({
            ...prev,
            wallet_id: selectedWallet?.id || prev.wallet_id || '',
            source_wallet_id: selectedWallet?.id || prev.source_wallet_id || '',
        }));
    }, [selectedWallet]);

    const handleChange = (e) => {
        const { name, value } = e.target;

        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));

        if (errors[name]) {
            setErrors((prev) => ({ ...prev, [name]: '' }));
        }
    };

    const handleTypeChange = (type) => {
        setFormData((prev) => ({
            ...prev,
            type,
        }));
        setErrors({});
    };

    const validate = () => {
        const newErrors = {};

        if (!formData.type) {
            newErrors.type = 'Transaction type is required';
        }

        if (!formData.amount || Number(formData.amount) <= 0) {
            newErrors.amount = 'Amount must be greater than 0';
        }

        if (formData.type === 'transfer') {
            if (!formData.source_wallet_id) {
                newErrors.source_wallet_id = 'Source wallet is required';
            }
            if (!formData.destination_wallet_id) {
                newErrors.destination_wallet_id = 'Destination wallet is required';
            }
            if (
                formData.source_wallet_id &&
                formData.destination_wallet_id &&
                formData.source_wallet_id === formData.destination_wallet_id
            ) {
                newErrors.destination_wallet_id =
                    'Source and destination wallets must be different';
            }
        } else {
            if (!formData.wallet_id) {
                newErrors.wallet_id = 'Wallet is required';
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!validate()) return;

        const payload = {
            ...formData,
            amount: parseFloat(formData.amount),
        };

        onSubmit(payload);
    };

    const handleClose = () => {
        setErrors({});
        onClose();
    };

    const isTransfer = formData.type === 'transfer';

    return (
        <Modal
            isOpen={isOpen}
            onClose={handleClose}
            title="Add Transaction"
            size="lg"
        >
            <form onSubmit={handleSubmit} className="space-y-6">
                {/* Type Switcher */}
                <div>
                    <p className="block text-sm font-medium text-text mb-2">
                        Transaction Type
                    </p>
                    <div className="flex space-x-2">
                        {transactionTypes.map((t) => (
                            <button
                                key={t.value}
                                type="button"
                                onClick={() => handleTypeChange(t.value)}
                                className={`flex-1 px-3 py-2 rounded-lg text-sm font-medium border ${
                                    formData.type === t.value
                                        ? 'bg-blue text-white border-blue'
                                        : 'bg-white text-text border-strokes hover:bg-soft-gray'
                                }`}
                            >
                                {t.label}
                            </button>
                        ))}
                    </div>
                    {errors.type && (
                        <p className="text-xs text-red-500 mt-1">{errors.type}</p>
                    )}
                </div>

                {/* Amount */}
                <Input
                    label="Amount"
                    name="amount"
                    type="number"
                    value={formData.amount}
                    onChange={handleChange}
                    placeholder="0.00"
                    step="0.01"
                    min="0"
                    error={errors.amount}
                />

                {/* Wallet selection */}
                {!isTransfer ? (
                    <Select
                        label="Wallet"
                        name="wallet_id"
                        value={formData.wallet_id}
                        onChange={handleChange}
                        error={errors.wallet_id}
                    >
                        <option value="">Select wallet</option>
                        {wallets.map((wallet) => (
                            <option key={wallet.id} value={wallet.id}>
                                {wallet.name} ({wallet.currency})
                            </option>
                        ))}
                    </Select>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <Select
                            label="From Wallet"
                            name="source_wallet_id"
                            value={formData.source_wallet_id}
                            onChange={handleChange}
                            error={errors.source_wallet_id}
                        >
                            <option value="">Select source</option>
                            {wallets.map((wallet) => (
                                <option key={wallet.id} value={wallet.id}>
                                    {wallet.name} ({wallet.currency})
                                </option>
                            ))}
                        </Select>

                        <Select
                            label="To Wallet"
                            name="destination_wallet_id"
                            value={formData.destination_wallet_id}
                            onChange={handleChange}
                            error={errors.destination_wallet_id}
                        >
                            <option value="">Select destination</option>
                            {wallets.map((wallet) => (
                                <option key={wallet.id} value={wallet.id}>
                                    {wallet.name} ({wallet.currency})
                                </option>
                            ))}
                        </Select>
                    </div>
                )}

                {/* Category (for income/expense only) */}
                {!isTransfer && (
                    <Input
                        label="Category (optional)"
                        name="category_id"
                        value={formData.category_id}
                        onChange={handleChange}
                        placeholder="e.g. Food, Salary, Rent..."
                        error={errors.category_id}
                    />
                )}

                {/* Note */}
                <Input
                    label="Note (optional)"
                    name="note"
                    value={formData.note}
                    onChange={handleChange}
                    placeholder="Add a note..."
                />

                {/* Attachments (placeholder text field) */}
                <Input
                    label="Attachments (optional)"
                    name="attachments"
                    value={formData.attachments}
                    onChange={handleChange}
                    placeholder="Link or description of attachments"
                />

                {/* Actions */}
                <div className="flex space-x-3 pt-2">
                    <Button
                        type="submit"
                        variant="primary"
                        className="flex-1 py-3"
                        disabled={isLoading}
                    >
                        {isLoading ? 'Adding...' : 'Confirm Transaction'}
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
        </Modal>
    );
};

export default AddTransactionModal;
