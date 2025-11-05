import React from 'react';
import Button from '../ui/Button';
import welcomeImage from '../../assets/images/welcome-image.png';
import welcomeTextImage from '../../assets/images/welcome-text-image.png';
// Import your custom stat icons
import balanceIcon from '../../assets/icons/balance-icon.png';
import spendingIcon from '../../assets/icons/spending-icon.png';
import savedIcon from '../../assets/icons/saved-icon.png';

const EmptyState = ({ onCreateWallet, userStats }) => {
    // Provide default values if userStats is null
    const safeUserStats = userStats || {
        totalBalance: 0,
        totalSpending: 0,
        totalSaved: 0
    };

    const { totalBalance, totalSpending, totalSaved } = safeUserStats;

    return (
        <div className="max-w-7xl mx-auto pt-8">
            {/* Stats Cards with Left-Centered Icons - Same design as before but with real data */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-16">
                {/* Total Balance - Dark Background */}
                <div className="bg-[#363A3F] rounded-lg shadow-sm p-6 border border-strokes">
                    <div className="flex items-center">
                        <img src={balanceIcon} alt="Balance" className="w-10 h-10 mr-4" />
                        <div className="flex-1 text-center">
                            <h3 className="text-sm font-medium text-white mb-1">Total balance</h3>
                            <p className="text-2xl font-bold text-white">${totalBalance.toFixed(2)}</p>
                        </div>
                    </div>
                </div>

                {/* Total Spending */}
                <div className="bg-white rounded-lg shadow-sm p-6 border border-strokes">
                    <div className="flex items-center">
                        <img src={spendingIcon} alt="Spending" className="w-10 h-10 mr-4" />
                        <div className="flex-1 text-center">
                            <h3 className="text-sm font-medium text-metallic-gray mb-1">Total spending</h3>
                            <p className="text-2xl font-bold text-text">${totalSpending.toFixed(2)}</p>
                        </div>
                    </div>
                </div>

                {/* Total Saved */}
                <div className="bg-white rounded-lg shadow-sm p-6 border border-strokes">
                    <div className="flex items-center">
                        <img src={savedIcon} alt="Saved" className="w-10 h-10 mr-4" />
                        <div className="flex-1 text-center">
                            <h3 className="text-sm font-medium text-metallic-gray mb-1">Total saved</h3>
                            <p className="text-2xl font-bold text-text">${totalSaved.toFixed(2)}</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Welcome Section - Centered layout with text above button */}
            <div className="flex flex-col lg:flex-row items-center justify-between">
                {/* Welcome Image - Left Side */}
                <div className="flex-1 mb-8 lg:mb-0 lg:mr-12 flex justify-center">
                    <img
                        src={welcomeImage}
                        alt="Welcome to Equilibrio"
                        className="max-w-4xl w-full h-auto"
                    />
                </div>

                {/* Welcome Text and Button - Right Side */}
                <div className="flex-1 flex flex-col items-center justify-center">
                    {/* Welcome Text Image - Centered above button */}
                    <div className="w-full flex justify-center mb-8">
                        <img
                            src={welcomeTextImage}
                            alt="Welcome to Equilibrio - Create a Wallet and Start tracking your Money Today!"
                            className="w-full max-w-md"
                        />
                    </div>

                    {/* Create Wallet Button - LONG BAR */}
                    <div className="w-full max-w-2xl">
                        <button
                            onClick={onCreateWallet}
                            className="w-full py-6 text-2xl font-bold text-white bg-blue rounded-xl hover:bg-blue-600 transition-colors duration-200 shadow-lg flex items-center justify-center"
                            style={{ backgroundColor: '#4361ee' }}
                        >
                            Create Wallet
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default EmptyState;