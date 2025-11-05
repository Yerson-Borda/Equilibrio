import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
// Import your custom header icons
import searchIcon from '../assets/icons/search-icon.png';
import notificationIcon from '../assets/icons/notification-icon.png';
import dropdownIcon from '../assets/icons/dropdown-icon.png';
import dropupIcon from '../assets/icons/dropup-icon.png';
import { apiService } from '../services/api';

const Header = () => {
    const [isProfileOpen, setIsProfileOpen] = useState(false);
    const [user, setUser] = useState(null);
    const [totalBalance, setTotalBalance] = useState(0);
    const [isLoadingBalance, setIsLoadingBalance] = useState(false);
    const [balanceBreakdown, setBalanceBreakdown] = useState([]);
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        fetchUserData();
        fetchTotalBalance();
    }, [location]);

    const fetchUserData = async () => {
        try {
            const userData = await apiService.getCurrentUser();
            setUser(userData);
        } catch (error) {
            console.error('Error fetching user data:', error);
        }
    };

    const fetchTotalBalance = async () => {
        try {
            setIsLoadingBalance(true);
            const balanceData = await apiService.getUserTotalBalance();
            setTotalBalance(balanceData.total_balance || 0);
            setBalanceBreakdown(balanceData.breakdown || []);
        } catch (error) {
            console.error('Error fetching total balance:', error);
        } finally {
            setIsLoadingBalance(false);
        }
    };

    const handleUpdateProfile = () => {
        navigate('/settings');
        setIsProfileOpen(false);
    };

    const handleExportTransactions = () => {
        // Placeholder for export functionality
        console.log('Export transactions');
        setIsProfileOpen(false);
    };

    const handleDeleteData = () => {
        // Placeholder for delete data functionality
        console.log('Delete data');
        setIsProfileOpen(false);
    };

    const getAvatarUrl = (avatarUrl) => {
        if (!avatarUrl) return null;
        return avatarUrl.startsWith('http') ? avatarUrl : `${window.location.origin}${avatarUrl}`;
    };

    // Enhanced currency formatting with symbols
    const formatCurrency = (amount, currency = 'USD') => {
        const currencySymbols = {
            'USD': '$', 'EUR': '€', 'GBP': '£', 'JPY': '¥',
            'CAD': 'CA$', 'AUD': 'A$', 'CHF': 'CHF', 'CNY': '¥',
            'INR': '₹', 'BRL': 'R$', 'RUB': '₽', 'KRW': '₩',
            'SGD': 'S$', 'NZD': 'NZ$', 'MXN': 'MX$', 'HKD': 'HK$',
            'TRY': '₺', 'SEK': 'kr', 'NOK': 'kr', 'DKK': 'kr',
            'ZAR': 'R', 'PLN': 'zł', 'THB': '฿', 'MYR': 'RM',
            'IDR': 'Rp', 'HUF': 'Ft', 'CZK': 'Kč', 'ILS': '₪',
            'CLP': 'CLP$', 'PHP': '₱', 'AED': 'د.إ', 'COP': 'COL$',
            'SAR': '﷼', 'QAR': '﷼', 'KWD': 'د.ك', 'EGP': '£',
            'ARS': 'ARS$', 'NGN': '₦', 'BDT': '৳', 'PKR': '₨',
            'UAH': '₴', 'VND': '₫', 'RON': 'lei', 'PEN': 'S/',
            'BGN': 'лв', 'HRK': 'kn', 'ISK': 'kr'
        };

        const symbol = currencySymbols[currency] || currency;

        // Format number with proper thousands separators and 2 decimal places
        const formattedAmount = new Intl.NumberFormat('en-US', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(amount);

        return `${symbol}${formattedAmount}`;
    };

    // Get currency name from code
    const getCurrencyName = (currencyCode) => {
        const currencyNames = {
            'USD': 'US Dollar', 'EUR': 'Euro', 'GBP': 'British Pound',
            'JPY': 'Japanese Yen', 'CAD': 'Canadian Dollar', 'AUD': 'Australian Dollar',
            'CHF': 'Swiss Franc', 'CNY': 'Chinese Yuan', 'INR': 'Indian Rupee',
            'BRL': 'Brazilian Real', 'RUB': 'Russian Ruble', 'KRW': 'South Korean Won',
            'SGD': 'Singapore Dollar', 'NZD': 'New Zealand Dollar', 'MXN': 'Mexican Peso',
            'HKD': 'Hong Kong Dollar', 'TRY': 'Turkish Lira', 'SEK': 'Swedish Krona',
            'NOK': 'Norwegian Krone', 'DKK': 'Danish Krone', 'ZAR': 'South African Rand',
            'PLN': 'Polish Zloty', 'THB': 'Thai Baht', 'MYR': 'Malaysian Ringgit',
            'IDR': 'Indonesian Rupiah', 'HUF': 'Hungarian Forint', 'CZK': 'Czech Koruna',
            'ILS': 'Israeli New Shekel', 'CLP': 'Chilean Peso', 'PHP': 'Philippine Peso',
            'AED': 'UAE Dirham', 'COP': 'Colombian Peso', 'SAR': 'Saudi Riyal',
            'QAR': 'Qatari Rial', 'KWD': 'Kuwaiti Dinar', 'EGP': 'Egyptian Pound',
            'ARS': 'Argentine Peso', 'NGN': 'Nigerian Naira', 'BDT': 'Bangladeshi Taka',
            'PKR': 'Pakistani Rupee', 'UAH': 'Ukrainian Hryvnia', 'VND': 'Vietnamese Dong',
            'RON': 'Romanian Leu', 'PEN': 'Peruvian Sol', 'BGN': 'Bulgarian Lev',
            'HRK': 'Croatian Kuna', 'ISK': 'Icelandic Króna'
        };

        return currencyNames[currencyCode] || currencyCode;
    };

    // Calculate total wallets for display
    const totalWallets = balanceBreakdown.length;

    return (
        <header className="bg-transparent">
            <div className="flex items-center justify-between px-8 py-4">
                {/* Left side - Total Balance with Enhanced Display */}
                <div className="flex-1">
                    <div className="flex items-center space-x-4">
                        {/* Main Balance Card */}
                        <div className="bg-white rounded-lg shadow-sm px-6 py-3 border border-strokes">
                            <div className="flex items-center space-x-3">
                                <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                                <div>
                                    <p className="text-sm text-metallic-gray">Total Balance</p>
                                    <p className="text-xl font-bold text-text">
                                        {isLoadingBalance ? (
                                            <span className="text-metallic-gray">Loading...</span>
                                        ) : (
                                            formatCurrency(totalBalance, user?.default_currency || 'USD')
                                        )}
                                    </p>
                                    {user?.default_currency && (
                                        <p className="text-xs text-metallic-gray">
                                            in {getCurrencyName(user.default_currency)}
                                        </p>
                                    )}
                                </div>
                            </div>
                        </div>

                        {/* Additional Balance Info */}
                        {!isLoadingBalance && balanceBreakdown.length > 0 && (
                            <div className="flex items-center space-x-4">
                                <div className="text-center">
                                    <p className="text-sm text-metallic-gray">Wallets</p>
                                    <p className="text-lg font-semibold text-text">{totalWallets}</p>
                                </div>

                                {/* Show multi-currency indicator if multiple currencies exist */}
                                {new Set(balanceBreakdown.map(w => w.original_currency)).size > 1 && (
                                    <div className="flex items-center space-x-2 bg-blue-50 px-3 py-1 rounded-full">
                                        <span className="text-blue-600 text-sm font-medium">
                                            {new Set(balanceBreakdown.map(w => w.original_currency)).size} currencies
                                        </span>
                                    </div>
                                )}

                                {/* Quick breakdown tooltip */}
                                <div className="relative group">
                                    <button className="text-metallic-gray hover:text-text transition-colors">
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                        </svg>
                                    </button>

                                    {/* Breakdown Tooltip */}
                                    <div className="absolute left-0 mt-2 w-64 bg-white rounded-lg shadow-lg border border-strokes p-4 z-50 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
                                        <h4 className="font-semibold text-text mb-2">Balance Breakdown</h4>
                                        <div className="space-y-2 max-h-48 overflow-y-auto">
                                            {balanceBreakdown.map((wallet, index) => (
                                                <div key={index} className="flex justify-between items-center text-sm">
                                                    <span className="text-metallic-gray truncate flex-1 mr-2">
                                                        {wallet.wallet_name}
                                                    </span>
                                                    <div className="text-right">
                                                        <div className="font-medium text-text">
                                                            {formatCurrency(wallet.original_balance, wallet.original_currency)}
                                                        </div>
                                                        {wallet.original_currency !== (user?.default_currency || 'USD') && (
                                                            <div className="text-xs text-metallic-gray">
                                                                ≈ {formatCurrency(wallet.converted_balance, wallet.converted_currency)}
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                        {balanceBreakdown.some(w => w.original_currency !== (user?.default_currency || 'USD')) && (
                                            <div className="mt-2 pt-2 border-t border-strokes">
                                                <p className="text-xs text-metallic-gray">
                                                    * Converted to {user?.default_currency || 'USD'}
                                                </p>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* Right side - Icons and Profile */}
                <div className="flex items-center space-x-6">
                    {/* Search Icon Button */}
                    <button className="p-2 hover:bg-gray-50 rounded-lg transition-colors">
                        <img src={searchIcon} alt="Search" className="w-5 h-5" />
                    </button>

                    {/* Notification Icon Button */}
                    <button className="p-2 hover:bg-gray-50 rounded-lg transition-colors">
                        <img src={notificationIcon} alt="Notifications" className="w-5 h-5" />
                    </button>

                    {/* Profile Section */}
                    <div className="relative">
                        <button
                            className="flex items-center space-x-3 p-2 rounded-lg hover:bg-gray-50 transition-colors"
                            onClick={() => setIsProfileOpen(!isProfileOpen)}
                        >
                            {/* Profile Picture - Dynamic from backend */}
                            <div className="w-10 h-10 bg-[#2260FF] rounded-full flex items-center justify-center overflow-hidden">
                                {user?.avatar_url ? (
                                    <img
                                        src={getAvatarUrl(user.avatar_url)}
                                        alt="Profile"
                                        className="w-full h-full object-cover"
                                        onError={(e) => {
                                            e.target.style.display = 'none';
                                            e.target.nextSibling.style.display = 'flex';
                                        }}
                                    />
                                ) : null}
                                <div className={`w-full h-full flex items-center justify-center ${user?.avatar_url ? 'hidden' : 'flex'}`}>
                                    <span className="text-white text-sm font-medium">
                                        {user?.full_name ? user.full_name.charAt(0).toUpperCase() : 'U'}
                                    </span>
                                </div>
                            </div>

                            {/* User Name with Dynamic Dropdown Icon */}
                            <div className="flex items-center space-x-1">
                                <span className="text-sm font-medium text-text">
                                    {user?.full_name || 'User_Name'}
                                </span>
                                <img
                                    src={isProfileOpen ? dropupIcon : dropdownIcon}
                                    alt="Dropdown"
                                    className="w-4 h-4 transition-transform"
                                />
                            </div>
                        </button>

                        {/* Profile Dropdown Menu - Only three sections with dividers */}
                        {isProfileOpen && (
                            <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-strokes py-1 z-50">
                                {/* Update Profile */}
                                <button
                                    onClick={handleUpdateProfile}
                                    className="block w-full text-left px-4 py-3 text-sm text-text hover:bg-gray-50 transition-colors"
                                >
                                    Update Profile
                                </button>

                                {/* First Divider */}
                                <div className="border-t border-strokes my-1"></div>

                                {/* Export Transactions */}
                                <button
                                    onClick={handleExportTransactions}
                                    className="block w-full text-left px-4 py-3 text-sm text-text hover:bg-gray-50 transition-colors"
                                >
                                    Export Transactions
                                </button>

                                {/* Second Divider */}
                                <div className="border-t border-strokes my-1"></div>

                                {/* Delete Data */}
                                <button
                                    onClick={handleDeleteData}
                                    className="block w-full text-left px-4 py-3 text-sm text-text hover:bg-gray-50 transition-colors"
                                >
                                    Delete Data
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Currency Summary Bar - Shows when multiple currencies exist */}
            {!isLoadingBalance && balanceBreakdown.length > 1 && (
                <div className="px-8 pb-2">
                    <div className="flex items-center space-x-4 text-xs text-metallic-gray">
                        <span>Balances in:</span>
                        {Array.from(new Set(balanceBreakdown.map(w => w.original_currency))).map(currency => (
                            <span key={currency} className="flex items-center space-x-1">
                                <span className="w-2 h-2 bg-blue-500 rounded-full"></span>
                                <span>{currency}</span>
                            </span>
                        ))}
                    </div>
                </div>
            )}
        </header>
    );
};

export default Header;