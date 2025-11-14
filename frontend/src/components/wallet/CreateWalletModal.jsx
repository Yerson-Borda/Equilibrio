import React, { useState, useEffect } from 'react';
import Button from '../ui/Button';

// Import your custom icons - replace these with your actual icon files
import visaIcon from '../../assets/icons/visa-icon.png';
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';

const CreateWalletModal = ({ isOpen, onClose, onSubmit, isLoading }) => {
    const [formData, setFormData] = useState({
        name: '',
        currency: 'USD',
        wallet_type: 'debit_card',
        initial_balance: '',
        card_number: '',
        color: '#6FBAFC'
    });

    // Extensive list of currencies with symbols
    const currencies = [
        { value: 'USD', label: 'USD - US Dollar', symbol: '$' },
        { value: 'EUR', label: 'EUR - Euro', symbol: '€' },
        { value: 'GBP', label: 'GBP - British Pound', symbol: '£' },
        { value: 'JPY', label: 'JPY - Japanese Yen', symbol: '¥' },
        { value: 'CAD', label: 'CAD - Canadian Dollar', symbol: 'CA$' },
        { value: 'AUD', label: 'AUD - Australian Dollar', symbol: 'A$' },
        { value: 'CHF', label: 'CHF - Swiss Franc', symbol: 'CHF' },
        { value: 'CNY', label: 'CNY - Chinese Yuan', symbol: '¥' },
        { value: 'INR', label: 'INR - Indian Rupee', symbol: '₹' },
        { value: 'BRL', label: 'BRL - Brazilian Real', symbol: 'R$' },
        { value: 'RUB', label: 'RUB - Russian Ruble', symbol: '₽' },
        { value: 'KRW', label: 'KRW - South Korean Won', symbol: '₩' },
        { value: 'SGD', label: 'SGD - Singapore Dollar', symbol: 'S$' },
        { value: 'NZD', label: 'NZD - New Zealand Dollar', symbol: 'NZ$' },
        { value: 'MXN', label: 'MXN - Mexican Peso', symbol: 'MX$' },
        { value: 'HKD', label: 'HKD - Hong Kong Dollar', symbol: 'HK$' },
        { value: 'TRY', label: 'TRY - Turkish Lira', symbol: '₺' },
        { value: 'SEK', label: 'SEK - Swedish Krona', symbol: 'kr' },
        { value: 'NOK', label: 'NOK - Norwegian Krone', symbol: 'kr' },
        { value: 'DKK', label: 'DKK - Danish Krone', symbol: 'kr' },
        { value: 'ZAR', label: 'ZAR - South African Rand', symbol: 'R' },
        { value: 'PLN', label: 'PLN - Polish Zloty', symbol: 'zł' },
        { value: 'THB', label: 'THB - Thai Baht', symbol: '฿' },
        { value: 'MYR', label: 'MYR - Malaysian Ringgit', symbol: 'RM' },
        { value: 'IDR', label: 'IDR - Indonesian Rupiah', symbol: 'Rp' },
        { value: 'HUF', label: 'HUF - Hungarian Forint', symbol: 'Ft' },
        { value: 'CZK', label: 'CZK - Czech Koruna', symbol: 'Kč' },
        { value: 'ILS', label: 'ILS - Israeli New Shekel', symbol: '₪' },
        { value: 'CLP', label: 'CLP - Chilean Peso', symbol: 'CLP$' },
        { value: 'PHP', label: 'PHP - Philippine Peso', symbol: '₱' },
        { value: 'AED', label: 'AED - UAE Dirham', symbol: 'د.إ' },
        { value: 'COP', label: 'COP - Colombian Peso', symbol: 'COL$' },
        { value: 'SAR', label: 'SAR - Saudi Riyal', symbol: '﷼' },
        { value: 'QAR', label: 'QAR - Qatari Rial', symbol: '﷼' },
        { value: 'KWD', label: 'KWD - Kuwaiti Dinar', symbol: 'د.ك' },
        { value: 'EGP', label: 'EGP - Egyptian Pound', symbol: '£' },
        { value: 'ARS', label: 'ARS - Argentine Peso', symbol: 'ARS$' },
        { value: 'NGN', label: 'NGN - Nigerian Naira', symbol: '₦' },
        { value: 'BDT', label: 'BDT - Bangladeshi Taka', symbol: '৳' },
        { value: 'PKR', label: 'PKR - Pakistani Rupee', symbol: '₨' },
        { value: 'UAH', label: 'UAH - Ukrainian Hryvnia', symbol: '₴' },
        { value: 'VND', label: 'VND - Vietnamese Dong', symbol: '₫' },
        { value: 'RON', label: 'RON - Romanian Leu', symbol: 'lei' },
        { value: 'PEN', label: 'PEN - Peruvian Sol', symbol: 'S/' },
        { value: 'BGN', label: 'BGN - Bulgarian Lev', symbol: 'лв' },
        { value: 'HRK', label: 'HRK - Croatian Kuna', symbol: 'kn' },
        { value: 'ISK', label: 'ISK - Icelandic Króna', symbol: 'kr' }
    ];

    const walletTypes = [
        { value: 'debit_card', label: 'Debit Card' },
        { value: 'cash', label: 'Cash' },
        { value: 'credit_card', label: 'Credit Card' },
        { value: 'saving_account', label: 'Saving Account' },
        { value: 'investment', label: 'Investment' },
        { value: 'loan', label: 'Loan' }
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


    // Get current currency symbol
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
            // Only allow numbers and limit to 16 digits
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
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        const submitData = {
            ...formData,
            initial_balance: parseFloat(formData.initial_balance) || 0,
            card_number: formData.card_number.replace(/\s/g, '')
        };

        onSubmit(submitData);
    };

    const resetForm = () => {
        setFormData({
            name: '',
            currency: 'USD',
            wallet_type: 'debit_card',
            initial_balance: '',
            card_number: '',
            color: '#6FBAFC'
        });
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    if (!isOpen) return null;

    const currentCurrencySymbol = getCurrentCurrencySymbol();

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50 ">
            <div className="flex flex-col items-center space-y-4 mt-16 ">
                {/* Card Preview - Separate Box with Custom Icons */}
                <div
                    className="p-6 text-[#0B0C10] rounded-xl shadow-lg relative transition-colors duration-300"
                    style={{
                        width: '345px',
                        height: '200px',
                        backgroundColor: formData.color,
                    }}
                >

                    {/* Bank name */}
                    <div className="text-left mb-2">
                        <h3 className="text-base font-semibold">{formData.name || 'Т-Банк'}</h3>
                    </div>

                    {/* Chip + Balance + NFC */}
                    <div className="flex justify-between items-start mb-6">
                        <div className="flex items-start space-x-3">
                            <img
                                src={chipIcon}
                                alt="Chip"
                                className="w-10 h-8 object-contain mt-4"
                            />
                            <div>
                                <p className="text-xs opacity-75 font-bold leading-none ml-6 mt-3">Total Balance</p>
                                <p className="text-xl font-bold leading-tight ml-6">
                                    {currentCurrencySymbol}{formData.initial_balance || '0.00'}
                                </p>
                            </div>
                        </div>
                        <img
                            src={nfcIcon}
                            alt="NFC"
                            className="w-8 h-7 object-contain mt-3"
                        />
                    </div>

                    {/* Card Number */}
                    <div className="mb-0">
                        <span className="tracking-wider font-mono text-lg font-semibold">
                            {formatCardNumber(formData.card_number) || '5495 7381 3759 2321'}
                        </span>
                    </div>

                    {/* Expiry and VISA */}
                    <div className="flex justify-between items-center mb-10">
                        <span className="text-sm opacity-80">09/30</span>
                        <img
                            src={visaIcon}
                            alt="VISA"
                            className="h-8 object-contain"
                        />
                    </div>
                </div>


                {/* Form Section - Larger Box to fit all content including buttons */}
                <div
                    className="bg-white rounded-xl shadow-lg p-6  "
                    style={{
                        width: '345px',
                        height: '660px',

                    }}
                >
                    <form onSubmit={handleSubmit} className="h-full flex flex-col justify-between ">
                        {/* Form Fields */}
                        <div className="space-y-4">
                            {/* Initial Balance */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2 text-[#929EAE]">
                                    Initial value
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
                                        className="w-full pl-10 p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                        required
                                    />
                                </div>
                            </div>

                            {/* Currency - Scrollable Dropdown */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2 text-[#929EAE]">
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
                                <label className="block text-sm font-medium text-text mb-2 text-[#929EAE]">
                                    Card number
                                </label>
                                <input
                                    type="text"
                                    name="card_number"
                                    value={formatCardNumber(formData.card_number)}
                                    onChange={handleChange}
                                    placeholder="5495 7381 3759 2321"
                                    maxLength="19"
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                />
                            </div>

                            {/* Wallet Name */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2 text-[#929EAE]">
                                    Wallet name
                                </label>
                                <input
                                    type="text"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    placeholder="Enter wallet name"
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                    required
                                />
                            </div>

                            {/* Type */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2 text-[#929EAE]">
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

                            {/* Color Section */}
                            <div>
                                <label className="block text-sm font-medium text-[#929EAE] ">
                                    Color
                                </label>
                                <select
                                    name="color"
                                    value={formData.color}
                                    onChange={(e) =>
                                        setFormData((prev) => ({ ...prev, color: e.target.value }))
                                    }
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent cursor-pointer"
                                    style={{
                                        backgroundColor: formData.color,
                                        color: 'transparent', // hides any text color
                                        textShadow: '0 0 0 transparent', // removes visible text shadow
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
                                            &nbsp; {/* invisible space, so color only shows */}
                                        </option>
                                    ))}
                                </select>
                            </div>



                        </div>

                        {/* Buttons - Now properly inside the form box */}
                        <div className="flex justify-end space-x-4 mt-8">
                            {/* Add Wallet button */}
                            <Button
                                type="submit"
                                variant="primary"
                                disabled={isLoading}
                                style={{ backgroundColor: '#4361ee' }}
                                className="text-white font-medium rounded-md px-8 py-3 min-w-[170px] hover:opacity-90 transition"
                            >
                                {isLoading ? 'Creating...' : 'Add Wallet'}
                            </Button>

                            {/* Cancel button */}
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