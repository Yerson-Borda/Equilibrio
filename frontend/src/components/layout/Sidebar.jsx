import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';

// logo & icons now from ../../assets/...
import logo from '../../assets/images/logo.png';
import dashboardIcon from '../../assets/icons/dashboard-icon.png';
import transactionsIcon from '../../assets/icons/transactions-icon.png';
import walletsIcon from '../../assets/icons/wallets-icon.png';
import goalsIcon from '../../assets/icons/goals-icon.png';
import settingsIcon from '../../assets/icons/settings-icon.png';
import helpIcon from '../../assets/icons/help-icon.png';
import logoutIcon from '../../assets/icons/logout-icon.png';

const Sidebar = ({ activeItem }) => {
    const location = useLocation();
    const [isLogoutConfirmOpen, setIsLogoutConfirmOpen] = useState(false);

    const mainMenuItems = [
        {
            id: 'dashboard',
            label: 'Dashboard',
            path: '/dashboard',
            icon: dashboardIcon
        },
        {
            id: 'transactions',
            label: 'Transactions',
            path: '/transactions',
            icon: transactionsIcon
        },
        {
            id: 'my-wallets',
            label: 'My Wallets',
            path: '/wallets',
            icon: walletsIcon
        },
        {
            id: 'goals',
            label: 'Goals',
            path: '/goals',
            icon: goalsIcon
        },
        {
            id: 'settings',
            label: 'Settings',
            path: '/settings',
            icon: settingsIcon
        }
    ];

    const bottomMenuItems = [
        {
            id: 'help',
            label: 'Help',
            path: '/help',
            icon: helpIcon
        },
        {
            id: 'logout',
            label: 'Logout',
            path: '#',
            icon: logoutIcon,
            action: () => setIsLogoutConfirmOpen(true)
        }
    ];

    const handleLogout = () => {
        localStorage.removeItem('token');
        window.location.href = '/login';
    };

    const isActive = (itemPath) => {
        // prefer URL match, fallback to activeItem prop if you want:
        if (location.pathname === itemPath) return true;
        if (activeItem && itemPath === `/${activeItem}`) return true;
        return false;
    };

    return (
        <>
            <div className="fixed left-0 top-0 h-full w-64 bg-white shadow-lg flex flex-col">
                {/* Logo */}
                <div className="flex items-center p-6 border-b border-strokes">
                    <img src={logo} alt="Equilibrio" className="h-8 mr-3" />
                    <h1 className="text-xl font-bold text-text">Equilibrio.</h1>
                </div>

                {/* Main Navigation */}
                <nav className="flex-1 p-4">
                    <ul className="space-y-2">
                        {mainMenuItems.map((item) => (
                            <li key={item.id}>
                                <Link
                                    to={item.path}
                                    className={`flex items-center px-4 py-3 rounded-lg transition-colors ${
                                        isActive(item.path)
                                            ? 'bg-blue text-white'
                                            : 'text-text hover:bg-gray-50'
                                    }`}
                                >
                                    <img
                                        src={item.icon}
                                        alt={item.label}
                                        className={`w-5 h-5 mr-3 ${
                                            isActive(item.path)
                                                ? 'filter brightness-0 invert'
                                                : ''
                                        }`}
                                    />
                                    <span className="text-sm font-medium">
                                        {item.label}
                                    </span>
                                </Link>
                            </li>
                        ))}
                    </ul>
                </nav>

                {/* Bottom Navigation */}
                <div className="p-4 border-t border-strokes">
                    <ul className="space-y-2">
                        {bottomMenuItems.map((item) => (
                            <li key={item.id}>
                                {item.id === 'logout' ? (
                                    <button
                                        type="button"
                                        onClick={item.action}
                                        className="w-full flex items-center px-4 py-3 rounded-lg transition-colors text-text hover:bg-gray-50"
                                    >
                                        <img
                                            src={item.icon}
                                            alt={item.label}
                                            className="w-5 h-5 mr-3"
                                        />
                                        <span className="text-sm font-medium">
                                            {item.label}
                                        </span>
                                    </button>
                                ) : (
                                    <Link
                                        to={item.path}
                                        className="flex items-center px-4 py-3 rounded-lg transition-colors text-text hover:bg-gray-50"
                                    >
                                        <img
                                            src={item.icon}
                                            alt={item.label}
                                            className="w-5 h-5 mr-3"
                                        />
                                        <span className="text-sm font-medium">
                                            {item.label}
                                        </span>
                                    </Link>
                                )}
                            </li>
                        ))}
                    </ul>
                </div>
            </div>

            {/* Logout Confirmation Modal */}
            {isLogoutConfirmOpen && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-lg shadow-xl p-6 max-w-sm w-full">
                        <h2 className="text-lg font-semibold text-text mb-4">
                            Logout
                        </h2>
                        <p className="text-sm text-metallic-gray mb-6">
                            Are you sure you want to log out?
                        </p>
                        <div className="flex justify-end space-x-3">
                            <button
                                className="px-4 py-2 text-sm rounded-lg border border-strokes text-text hover:bg-gray-50"
                                onClick={() => setIsLogoutConfirmOpen(false)}
                            >
                                Cancel
                            </button>
                            <button
                                className="px-4 py-2 text-sm rounded-lg bg-blue text-white hover:bg-blue/90"
                                onClick={handleLogout}
                            >
                                Logout
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
};

export default Sidebar;
