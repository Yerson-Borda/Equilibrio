import React, { useEffect } from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import AppRoutes from './routes/AppRoutes';
import { syncService } from './services/syncService';
import { apiService } from './services/api';

function App() {
    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            initializeApp();
        }

        const handleLogin = () => {
            initializeApp();
        };

        window.addEventListener('user_logged_in', handleLogin);

        return () => {
            window.removeEventListener('user_logged_in', handleLogin);
            syncService.stopSync();
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const initializeApp = async () => {
        try {
            const userData = await apiService.getCurrentUser();
            if (userData && userData.id) {
                console.log('Initializing sync for user:', userData.id);
                syncService.initialize(userData.id);
            }
        } catch (error) {
            console.error('Failed to initialize app:', error);
        }
    };

    return (
        <Router>
            <div className="App">
                <AppRoutes />
            </div>
        </Router>
    );
}

export default App;
