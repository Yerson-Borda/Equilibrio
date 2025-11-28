import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

import searchIcon from '../../assets/icons/search-icon.png';
import notificationIcon from '../../assets/icons/notification-icon.png';
import dropdownIcon from '../../assets/icons/dropdown-icon.png';
import dropupIcon from '../../assets/icons/dropup-icon.png';

import { apiService } from '../../services/api';
import { formatCurrency} from '../../config/currencies';

const Header = () => {
    const [isProfileOpen, setIsProfileOpen] = useState(false);
    const [user, setUser] = useState(null);
    const [setTotalBalance] = useState(0);
    const [isLoadingBalance, setIsLoadingBalance] = useState(false);
    const [balanceBreakdown, setBalanceBreakdown] = useState([]);

    const navigate = useNavigate();
    const location = useLocation();


    useEffect(() => {
        fetchUserData();
        fetchTotalBalance();// eslint-disable-next-line react-hooks/exhaustive-deps
    }, [location]);

    const fetchUserData = async () => {
        try {
            const userData = await apiService.getCurrentUser();
            setUser(userData);
        } catch (error) {
            console.error("Error fetching user data:", error);
        }
    };

    const fetchTotalBalance = async () => {
        try {
            setIsLoadingBalance(true);
            const balanceData = await apiService.getUserTotalBalance();
            setTotalBalance(balanceData.total_balance || 0);
            setBalanceBreakdown(balanceData.breakdown || []);
        } catch (error) {
            console.error("Error fetching total balance:", error);
        } finally {
            setIsLoadingBalance(false);
        }
    };

    const handleUpdateProfile = () => {
        navigate("/settings");
        setIsProfileOpen(false);
    };

    const handleExportTransactions = () => {
        console.log("Export transactions triggered");
        setIsProfileOpen(false);
    };

    const handleDeleteData = () => {
        console.log("Delete data triggered");
        setIsProfileOpen(false);
    };

    const getAvatarUrl = (avatarUrl) => {
        if (!avatarUrl) return null;
        return avatarUrl.startsWith("http")
            ? avatarUrl
            : `${window.location.origin}${avatarUrl}`;
    };

    const totalWallets = balanceBreakdown.length;

    return (
        <header className="bg-transparent">
            <div className="flex items-center justify-between px-8 py-4">
                {/* Left side — Total Balance */}
                <div className="flex-1">
                    <div className="flex items-center space-x-4">
                        {/* Balance Card */}
                        <h1 className="text-3xl font-bold text-gray-900 mt-6 ml-1">Dashboard</h1>
                        {/* Wallet Count + Multi-currency indicators */}
                        {!isLoadingBalance && balanceBreakdown.length > 0 && (
                            <div className="flex items-center space-x-4">
                                <div className="text-center">
                                    <p className="text-sm text-metallic-gray">Wallets</p>
                                    <p className="text-lg font-semibold text-text">
                                        {totalWallets}
                                    </p>
                                </div>

                                {/* Multi-currency */}
                                {new Set(balanceBreakdown.map(w => w.original_currency)).size > 1 && (
                                    <div className="flex items-center space-x-2 bg-blue-50 px-3 py-1 rounded-full">
                                        <span className="text-blue-600 text-sm font-medium">
                                            {
                                                new Set(
                                                    balanceBreakdown.map(w => w.original_currency)
                                                ).size
                                            } currencies
                                        </span>
                                    </div>
                                )}

                                {/* Breakdown Tooltip */}
                                <div className="relative group">
                                    <button className="text-metallic-gray hover:text-text transition-colors">
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor">
                                            <path
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                                strokeWidth={2}
                                                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                                            />
                                        </svg>
                                    </button>

                                    <div className="absolute left-0 mt-2 w-64 bg-white rounded-lg shadow-lg border border-strokes p-4 z-50 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
                                        <h4 className="font-semibold text-text mb-2">
                                            Balance Breakdown
                                        </h4>

                                        <div className="space-y-2 max-h-48 overflow-y-auto">
                                            {balanceBreakdown.map((w, idx) => (
                                                <div key={idx} className="flex justify-between items-center text-sm">
                                                    <span className="text-metallic-gray truncate flex-1 mr-2">
                                                        {w.wallet_name}
                                                    </span>

                                                    <div className="text-right">
                                                        <div className="font-medium text-text">
                                                            {formatCurrency(
                                                                w.original_balance,
                                                                w.original_currency
                                                            )}
                                                        </div>

                                                        {w.original_currency !== (user?.default_currency || 'USD') && (
                                                            <div className="text-xs text-metallic-gray">
                                                                ≈ {formatCurrency(
                                                                w.converted_balance,
                                                                w.converted_currency
                                                            )}
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>
                                            ))}
                                        </div>

                                        {balanceBreakdown.some(
                                            w => w.original_currency !== (user?.default_currency || 'USD')
                                        ) && (
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

                {/* Right — Icons & Profile */}
                <div className="flex items-center space-x-6">

                    {/* Search */}
                    <button className="p-2 hover:bg-gray-50 rounded-lg transition">
                        <img src={searchIcon} alt="Search" className="w-5 h-5" />
                    </button>

                    {/* Notifications */}
                    <button className="p-2 hover:bg-gray-50 rounded-lg transition">
                        <img src={notificationIcon} alt="Notifications" className="w-5 h-5" />
                    </button>

                    {/* Profile */}
                    <div className="relative">
                        <button
                            className="flex items-center space-x-3 p-2 rounded-lg hover:bg-gray-50 transition"
                            onClick={() => setIsProfileOpen(!isProfileOpen)}
                        >
                            <div className="w-10 h-10 bg-blue rounded-full overflow-hidden flex items-center justify-center">
                                {user?.avatar_url ? (
                                    <img
                                        src={getAvatarUrl(user.avatar_url)}
                                        alt="Profile"
                                        className="w-full h-full object-cover"
                                        onError={(e) => {
                                            e.target.style.display = "none";
                                            e.target.nextSibling.style.display = "flex";
                                        }}
                                    />
                                ) : null}

                                <div className={`${user?.avatar_url ? 'hidden' : 'flex'} w-full h-full items-center justify-center`}>
                                    <span className="text-white text-sm font-medium">
                                        {user?.full_name ? user.full_name.charAt(0).toUpperCase() : 'U'}
                                    </span>
                                </div>
                            </div>

                            <div className="flex items-center space-x-1">
                                <span className="text-sm font-medium text-text">
                                    {user?.full_name || 'User'}
                                </span>
                                <img
                                    src={isProfileOpen ? dropupIcon : dropdownIcon}
                                    alt="Dropdown"
                                    className="w-4 h-4"
                                />
                            </div>
                        </button>

                        {/* Dropdown */}
                        {isProfileOpen && (
                            <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-strokes py-1 z-50">
                                <button
                                    onClick={handleUpdateProfile}
                                    className="block w-full text-left px-4 py-3 text-sm hover:bg-gray-50"
                                >
                                    Update Profile
                                </button>

                                <div className="border-t border-strokes my-1"></div>

                                <button
                                    onClick={handleExportTransactions}
                                    className="block w-full text-left px-4 py-3 text-sm hover:bg-gray-50"
                                >
                                    Export Transactions
                                </button>

                                <div className="border-t border-strokes my-1"></div>

                                <button
                                    onClick={handleDeleteData}
                                    className="block w-full text-left px-4 py-3 text-sm hover:bg-gray-50"
                                >
                                    Delete Data
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Small Currency Summary Bar */}
            {!isLoadingBalance && balanceBreakdown.length > 1 && (
                <div className="px-8 pb-2">
                    <div className="flex items-center space-x-4 text-xs text-metallic-gray">
                        <span>Balances in:</span>

                        {Array.from(new Set(balanceBreakdown.map(w => w.original_currency))).map((cur) => (
                            <span key={cur} className="flex items-center space-x-1">
                                <span className="w-2 h-2 bg-blue-500 rounded-full"></span>
                                <span>{cur}</span>
                            </span>
                        ))}
                    </div>
                </div>
            )}
        </header>
    );
};

export default Header;
