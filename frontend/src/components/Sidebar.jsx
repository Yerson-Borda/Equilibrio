import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import logo from '../assets/images/logo.png';
// Import your custom icons
import dashboardIcon from '../assets/icons/dashboard-icon.png';
import transactionsIcon from '../assets/icons/transactions-icon.png';
import walletsIcon from '../assets/icons/wallets-icon.png';
import goalsIcon from '../assets/icons/goals-icon.png';
import settingsIcon from '../assets/icons/settings-icon.png';
import helpIcon from '../assets/icons/help-icon.png';
import logoutIcon from '../assets/icons/logout-icon.png';

const Sidebar = ({ activeItem }) => {
    const location = useLocation();

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
            path: '/logout',
            icon: logoutIcon
        }
    ];

    const isActive = (itemPath) => {
        return location.pathname === itemPath;
    };

    return (
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
                                    className={`w-5 h-5 mr-3 ${isActive(item.path) ? 'filter brightness-0 invert' : ''}`}
                                />
                                {item.label}
                            </Link>
                        </li>
                    ))}
                </ul>
            </nav>

            {/* Bottom Navigation - Help & Logout */}
            <div className="p-4 border-t border-strokes">
                <ul className="space-y-2">
                    {bottomMenuItems.map((item) => (
                        <li key={item.id}>
                            <Link
                                to={item.path}
                                className="flex items-center px-4 py-3 rounded-lg text-text hover:bg-gray-50 transition-colors"
                            >
                                <img
                                    src={item.icon}
                                    alt={item.label}
                                    className="w-5 h-5 mr-3"
                                />
                                {item.label}
                            </Link>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default Sidebar;