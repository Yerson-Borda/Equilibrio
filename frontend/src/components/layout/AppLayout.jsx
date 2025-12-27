import React from 'react';
import Sidebar from './Sidebar';
import Header from './Header';

const AppLayout = ({ activeItem, children }) => {
    return (
        <div className="min-h-screen bg-background flex">
            {/* Sidebar on the left */}
            <Sidebar activeItem={activeItem} />

            {/* Main content area */}
            <div className="flex-1 ml-64 flex flex-col bg-background">
                <Header />
                <main className="flex-1 p-8 overflow-auto bg-background">
                    {children}
                </main>
            </div>
        </div>
    );
};

export default AppLayout;
