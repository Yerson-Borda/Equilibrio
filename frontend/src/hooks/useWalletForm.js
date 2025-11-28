import { useState } from 'react';

// Helper: format card number as "1234 5678 9012 3456"
const formatCardNumber = value => {
    const cleaned = value.replace(/\D/g, '').slice(0, 16);
    const chunks = cleaned.match(/.{1,4}/g);
    return chunks ? chunks.join(' ') : '';
};

/**
 * Reusable wallet form logic for Create/Edit wallet modals.
 *
 * @param {object} initialState - initial form values
 */
export const useWalletForm = initialState => {
    const [formData, setFormData] = useState(initialState);
    const [errors, setErrors] = useState({});

    const handleChange = e => {
        const { name, value } = e.target;

        if (name === 'card_number') {
            const formatted = formatCardNumber(value);
            setFormData(prev => ({
                ...prev,
                [name]: formatted,
            }));
            return;
        }

        setFormData(prev => ({
            ...prev,
            [name]: value,
        }));

        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: '' }));
        }
    };

    const validate = () => {
        const newErrors = {};

        if (!formData.name?.trim()) {
            newErrors.name = 'Wallet name is required';
        }

        if (
            formData.initial_balance === '' ||
            formData.initial_balance === null ||
            isNaN(parseFloat(formData.initial_balance)) ||
            parseFloat(formData.initial_balance) < 0
        ) {
            newErrors.initial_balance = 'Valid initial balance is required';
        }

        if (
            formData.card_number &&
            formData.card_number.replace(/\s/g, '').length !== 16
        ) {
            newErrors.card_number = 'Card number must be 16 digits';
        }

        setErrors(newErrors);
        const isValid = Object.keys(newErrors).length === 0;
        return isValid;
    };

    const resetErrors = () => setErrors({});

    return {
        formData,
        setFormData,
        errors,
        setErrors,
        handleChange,
        validate,
        resetErrors,
    };
};
