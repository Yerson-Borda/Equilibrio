
// --- Master Currency List ---
export const CURRENCIES = [
    { code: 'USD', label: 'USD - US Dollar', symbol: '$', name: 'US Dollar' },
    { code: 'EUR', label: 'EUR - Euro', symbol: '€', name: 'Euro' },
    { code: 'GBP', label: 'GBP - British Pound', symbol: '£', name: 'British Pound' },
    { code: 'JPY', label: 'JPY - Japanese Yen', symbol: '¥', name: 'Japanese Yen' },
    { code: 'RUB', label: 'RUB - Russian Ruble', symbol: '₽', name: 'Russian Ruble' },
    { code: 'INR', label: 'INR - Indian Rupee', symbol: '₹', name: 'Indian Rupee' },
    { code: 'CNY', label: 'CNY - Chinese Yuan', symbol: '¥', name: 'Chinese Yuan' },
    { code: 'KRW', label: 'KRW - South Korean Won', symbol: '₩', name: 'South Korean Won' },
    { code: 'CHF', label: 'CHF - Swiss Franc', symbol: 'CHF', name: 'Swiss Franc' },
    { code: 'CAD', label: 'CAD - Canadian Dollar', symbol: 'CA$', name: 'Canadian Dollar' },
    { code: 'AUD', label: 'AUD - Australian Dollar', symbol: 'A$', name: 'Australian Dollar' },
    { code: 'NZD', label: 'NZD - New Zealand Dollar', symbol: 'NZ$', name: 'New Zealand Dollar' },
    { code: 'SGD', label: 'SGD - Singapore Dollar', symbol: 'S$', name: 'Singapore Dollar' },
    { code: 'HKD', label: 'HKD - Hong Kong Dollar', symbol: 'HK$', name: 'Hong Kong Dollar' },
    { code: 'AED', label: 'AED - UAE Dirham', symbol: 'AED', name: 'UAE Dirham' },
    { code: 'SAR', label: 'SAR - Saudi Riyal', symbol: 'SAR', name: 'Saudi Riyal' },
    { code: 'TRY', label: 'TRY - Turkish Lira', symbol: '₺', name: 'Turkish Lira' },
    { code: 'ZAR', label: 'ZAR - South African Rand', symbol: 'R', name: 'South African Rand' },
    { code: 'BRL', label: 'BRL - Brazilian Real', symbol: 'R$', name: 'Brazilian Real' },
    { code: 'MXN', label: 'MXN - Mexican Peso', symbol: 'MX$', name: 'Mexican Peso' },
    // add more here anytime…
];

export const DEFAULT_CURRENCY_CODE = 'USD';

// --- Helpers ---
export function getCurrencyByCode(code) {
    return CURRENCIES.find((c) => c.code === code) || null;
}

export function getCurrencySymbol(code) {
    const c = getCurrencyByCode(code);
    return c ? c.symbol : '$';
}

export function getCurrencyName(code) {
    const c = getCurrencyByCode(code);
    return c ? c.name : code;
}

// Smart formatter for ALL components including Header
export function formatCurrency(amount, code = DEFAULT_CURRENCY_CODE) {
    const symbol = getCurrencySymbol(code);
    const num = Number(amount) || 0;

    const formatted = new Intl.NumberFormat("en-US", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    }).format(num);

    return `${symbol}${formatted}`;
}
/**
 * NEW: Currency formatter with overflow masking (**)
 *
 * Example (maxChars = 13):
 * "$123,456,789.12" -> "$123,456,7**"
 */
export function formatCurrencyMasked(amount, currencySymbol = '$', maxChars = 13) {
    const num = Number(amount ?? 0);

    const full = `${currencySymbol}${num.toLocaleString(undefined, {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    })}`;

    if (full.length <= maxChars) return full;

    const keep = Math.max(0, maxChars - 2);
    return `${full.slice(0, keep)}**`;
}