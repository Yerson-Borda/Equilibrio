import React from 'react';
import UnauthorizedPage from '../pages/UnauthorizedPage';

const RequireAuth = ({ children }) => {
    const token = localStorage.getItem('token');

    if (!token) {
        return <UnauthorizedPage />;
    }

    return children;
};

export default RequireAuth;
