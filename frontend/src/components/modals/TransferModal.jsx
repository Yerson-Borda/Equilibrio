import React, { useRef, useState, useEffect } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Select from '../ui/Select';
import { apiService } from '../../services/api';

const ACCEPTED_MIME =
    'image/*,application/pdf,.pdf,.png,.jpg,.jpeg,.webp,.heic,.heif,.doc,.docx,.xls,.xlsx,.csv,.txt';

const TransferModal = ({ isOpen, onClose, onSubmit, isLoading, wallets = [] }) => {
    const [formData, setFormData] = useState({
        source_wallet_id: '',
        destination_wallet_id: '',
        amount: '',
        note: '',
    });

    const [attachments, setAttachments] = useState([]); // File[]
    const fileInputRef = useRef(null);

    const [errors, setErrors] = useState({});

    // Exchange rate preview states
    const [transferPreview, setTransferPreview] = useState(null);
    const [isLoadingPreview, setIsLoadingPreview] = useState(false);

    const addFiles = (files) => {
        const incoming = Array.from(files || []);
        if (!incoming.length) return;

        setAttachments((prev) => {
            const existing = new Set(prev.map((f) => `${f.name}-${f.size}-${f.lastModified}`));
            const merged = [...prev];
            for (const f of incoming) {
                const key = `${f.name}-${f.size}-${f.lastModified}`;
                if (!existing.has(key)) merged.push(f);
            }
            return merged;
        });
    };

    const handlePick = (e) => {
        addFiles(e.target.files);
        e.target.value = '';
    };

    const removeAttachment = (idx) => {
        setAttachments((prev) => prev.filter((_, i) => i !== idx));
    };

    // Fetch transfer preview when details change
    useEffect(() => {
        const fetchPreview = async () => {
            const { source_wallet_id, destination_wallet_id, amount } = formData;

            if (!source_wallet_id || !destination_wallet_id || !amount || amount <= 0) {
                setTransferPreview(null);
                return;
            }

            try {
                setIsLoadingPreview(true);
                const preview = await apiService.previewTransfer(
                    source_wallet_id,
                    destination_wallet_id,
                    amount
                );
                setTransferPreview(preview);
            } catch (error) {
                console.error('Failed to fetch transfer preview:', error);
                setTransferPreview(null);
            } finally {
                setIsLoadingPreview(false);
            }
        };

        // Debounce the preview fetch
        const timer = setTimeout(fetchPreview, 500);
        return () => clearTimeout(timer);
    }, [formData.source_wallet_id, formData.destination_wallet_id, formData.amount]);

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
            // send to parent; parent can convert to FormData if backend expects it
            receipt_files: attachments,
        };

        onSubmit(payload);
    };

    const handleClose = () => {
        setErrors({});
        setAttachments([]);
        setTransferPreview(null);
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

                {/* Exchange Rate Preview */}
                {transferPreview && (
                    <div className="mt-4 p-3 bg-blue-50 rounded-lg border border-blue-200">
                        <div className="text-sm text-gray-700">
                            <div className="font-medium">Exchange Rate:</div>
                            <div className="text-xs text-gray-600 mt-1">
                                1 {transferPreview.source_currency} = {Number(transferPreview.exchange_rate).toFixed(4)} {transferPreview.destination_currency}
                            </div>
                            <div className="font-semibold text-green-700 mt-2">
                                Target amount: {transferPreview.destination_currency} {Number(transferPreview.converted_amount).toFixed(2)}
                            </div>
                        </div>
                    </div>
                )}

                {isLoadingPreview && (
                    <div className="mt-4 text-sm text-gray-500 italic">
                        Calculating exchange rate...
                    </div>
                )}

                <Input
                    label="Note (optional)"
                    name="note"
                    value={formData.note}
                    onChange={handleChange}
                    placeholder="Add a note..."
                />

                {/* Attachments */}
                <div>
                    <div className="text-sm font-medium text-text mb-2">
                        Attachments (receipt)
                    </div>
                    <button
                        type="button"
                        className="w-full border border-strokes rounded-lg p-3 text-sm bg-gray-50 hover:bg-gray-100 transition"
                        onClick={() => fileInputRef.current?.click()}
                    >
                        Click to upload or add files
                    </button>
                    <input
                        ref={fileInputRef}
                        type="file"
                        accept={ACCEPTED_MIME}
                        multiple
                        className="hidden"
                        onChange={handlePick}
                    />

                    {attachments.length > 0 && (
                        <div className="mt-3 space-y-2 max-h-36 overflow-auto pr-1">
                            {attachments.map((f, idx) => (
                                <div
                                    key={`${f.name}-${f.size}-${f.lastModified}`}
                                    className="flex items-center justify-between border border-strokes rounded-lg px-3 py-2"
                                >
                                    <div className="text-xs text-text truncate max-w-[240px]">
                                        {f.name}
                                    </div>
                                    <button
                                        type="button"
                                        className="text-xs font-semibold text-red-500"
                                        onClick={() => removeAttachment(idx)}
                                    >
                                        Remove
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

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