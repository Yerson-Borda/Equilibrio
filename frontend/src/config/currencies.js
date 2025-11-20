
// --- Full Currency List (expandable) ---
export const CURRENCIES = [
    { code: 'USD', label: 'USD - US Dollar', symbol: '$' },
    { code: 'EUR', label: 'EUR - Euro', symbol: '€' },
    { code: 'GBP', label: 'GBP - British Pound', symbol: '£' },
    { code: 'JPY', label: 'JPY - Japanese Yen', symbol: '¥' },
    { code: 'RUB', label: 'RUB - Russian Ruble', symbol: '₽' },
    { code: 'INR', label: 'INR - Indian Rupee', symbol: '₹' },
    { code: 'CNY', label: 'CNY - Chinese Yuan', symbol: '¥' },
    { code: 'KRW', label: 'KRW - South Korean Won', symbol: '₩' },
    { code: 'CHF', label: 'CHF - Swiss Franc', symbol: 'CHF' },
    { code: 'CAD', label: 'CAD - Canadian Dollar', symbol: 'CA$' },
    { code: 'AUD', label: 'AUD - Australian Dollar', symbol: 'A$' },
    { code: 'NZD', label: 'NZD - New Zealand Dollar', symbol: 'NZ$' },
    { code: 'SGD', label: 'SGD - Singapore Dollar', symbol: 'S$' },
    { code: 'HKD', label: 'HKD - Hong Kong Dollar', symbol: 'HK$' },
    { code: 'AED', label: 'AED - UAE Dirham', symbol: 'AED' },
    { code: 'SAR', label: 'SAR - Saudi Riyal', symbol: 'SAR' },
    { code: 'TRY', label: 'TRY - Turkish Lira', symbol: '₺' },
    { code: 'ZAR', label: 'ZAR - South African Rand', symbol: 'R' },
    { code: 'BRL', label: 'BRL - Brazilian Real', symbol: 'R$' },
    { code: 'MXN', label: 'MXN - Mexican Peso', symbol: 'MX$' },
];

// --- Defaults ---
export const DEFAULT_CURRENCY_CODE = 'USD';

// --- Helper Functions ---
export function getCurrencyByCode(code) {
    return CURRENCIES.find(c => c.code === code) || null;
}

export function getCurrencySymbol(code) {
    const currency = getCurrencyByCode(code);
    return currency ? currency.symbol : '$';
}

export function getCurrencyLabel(code) {
    const currency = getCurrencyByCode(code);
    return currency ? currency.label : code;
}
