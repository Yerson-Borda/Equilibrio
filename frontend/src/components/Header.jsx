import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
// Import your custom header icons
import searchIcon from '../assets/icons/search-icon.png';
import notificationIcon from '../assets/icons/notification-icon.png';
import dropdownIcon from '../assets/icons/dropdown-icon.png';
import dropupIcon from '../assets/icons/dropup-icon.png'; // You'll need to add this icon
import { apiService } from '../services/api';

const Header = () => {
    const [isProfileOpen, setIsProfileOpen] = useState(false);
    const [user, setUser] = useState(null);
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        fetchUserData();
    }, [location]);

    const fetchUserData = async () => {
        try {
            const userData = await apiService.getCurrentUser();
            setUser(userData);
        } catch (error) {
            console.error('Error fetching user data:', error);
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

    return (
        <header className="bg-transparent">
            <div className="flex items-center justify-between px-8 py-4">
                {/* Left side - Empty for balance */}
                <div className="flex-1"></div>

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
        </header>
    );
};

export default Header;