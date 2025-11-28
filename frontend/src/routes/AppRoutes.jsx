import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';

import LoginPage from '../pages/LoginPage';
import SignUpPage from '../pages/SignUpPage';
import DashboardPage from '../pages/DashboardPage';
import MyWalletsPage from '../pages/MyWalletsPage';
import SettingsPage from '../pages/SettingsPage';
import TransactionsPage from '../pages/TransactionsPage';
import GoalsPage from '../pages/GoalsPage';
import UnauthorizedPage from '../pages/UnauthorizedPage';

import RequireAuth from './RequireAuth';

const AppRoutes = () => {
    return (
        <Routes>
            {/* Public auth routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignUpPage />} />

            {/* Protected routes */}
            <Route
                path="/dashboard"
                element={
                    <RequireAuth>
                        <DashboardPage />
                    </RequireAuth>
                }
            />
            <Route
                path="/wallets"
                element={
                    <RequireAuth>
                        <MyWalletsPage />
                    </RequireAuth>
                }
            />
            <Route
                path="/transactions"
                element={
                    <RequireAuth>
                        <TransactionsPage />
                    </RequireAuth>
                }
            />
            <Route
                path="/goals"
                element={
                    <RequireAuth>
                        <GoalsPage />
                    </RequireAuth>
                }
            />
            <Route
                path="/settings"
                element={
                    <RequireAuth>
                        <SettingsPage />
                    </RequireAuth>
                }
            />

            {/* Default → login */}
            <Route path="/" element={<Navigate to="/login" replace />} />

            {/* Fallback */}
            <Route path="*" element={<UnauthorizedPage />} />
        </Routes>
    );
};

export default AppRoutes;
