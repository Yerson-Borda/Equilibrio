import React, { useState } from 'react';
// Import your custom header icons
import searchIcon from '../assets/icons/search-icon.png';
import notificationIcon from '../assets/icons/notification-icon.png';
import dropdownIcon from '../assets/icons/dropdown-icon.png';

const Header = () => {
    const [isProfileOpen, setIsProfileOpen] = useState(false);

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
                            {/* Profile Picture - Will be dynamic later */}
                            <div className="w-8 h-8 bg-[#2260FF] rounded-full flex items-center justify-center">
                                <span className="text-white text-sm font-medium">U</span>
                            </div>

                            {/* User Name with Custom Dropdown Icon */}
                            <div className="flex items-center space-x-1">
                                <span className="text-sm font-medium text-text">User_Name</span>
                                <img
                                    src={dropdownIcon}
                                    alt="Dropdown"
                                    className="w-4 h-4"
                                />
                            </div>
                        </button>

                        {/* Profile Dropdown Menu */}
                        {isProfileOpen && (
                            <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-strokes py-1 z-50">
                                <a href="#" className="block px-4 py-2 text-sm text-text hover:bg-gray-50">Profile</a>
                                <a href="#" className="block px-4 py-2 text-sm text-text hover:bg-gray-50">Settings</a>
                                <a href="#" className="block px-4 py-2 text-sm text-text hover:bg-gray-50">Help</a>
                                <div className="border-t border-strokes my-1"></div>
                                <a href="#" className="block px-4 py-2 text-sm text-red-600 hover:bg-gray-50">Sign out</a>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </header>
    );
};

export default Header;