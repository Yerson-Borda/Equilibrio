import React, { useState } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Select from '../ui/Select';

const TransferModal = ({ isOpen, onClose, onSubmit, isLoading, wallets = [] }) => {
    const [formData, setFormData] = useState({
        source_wallet_id: '',
        destination_wallet_id: '',
        amount: '',
        note: '',
    });

    const [errors, setErrors] = useState({});

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

    const validate = () => {
        const newErrors = {};

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
        if (!formData.amount || Number(formData.amount) <= 0) {
            newErrors.amount = 'Amount must be greater than 0';
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

    return (
        <Modal
            isOpen={isOpen}
            onClose={handleClose}
            title="Transfer Funds"
            size="md"
        >
            <form onSubmit={handleSubmit} className="space-y-4">
                <Select
                    label="From Wallet"
                    name="source_wallet_id"
                    value={formData.source_wallet_id}
                    onChange={handleChange}
                    error={errors.source_wallet_id}
                >
                    <option value="">Select source wallet</option>
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
                    <option value="">Select destination wallet</option>
                    {wallets.map((wallet) => (
                        <option key={wallet.id} value={wallet.id}>
                            {wallet.name} ({wallet.currency})
                        </option>
                    ))}
                </Select>

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

                <Input
                    label="Note (optional)"
                    name="note"
                    value={formData.note}
                    onChange={handleChange}
                    placeholder="Add a note..."
                />

                <div className="flex space-x-3 pt-2">
                    <Button
                        type="submit"
                        variant="primary"
                        className="flex-1 py-3"
                        disabled={isLoading}
                    >
                        {isLoading ? 'Transferring...' : 'Transfer Funds'}
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

export default TransferModal;
