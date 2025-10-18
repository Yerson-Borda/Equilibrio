import React from 'react';
import Button from '../ui/Button';
import welcomeImage from '../../assets/images/welcome-image.png';
import welcomeTextImage from '../../assets/images/welcome-text-image.png';
// Import your custom stat icons
import balanceIcon from '../../assets/icons/balance-icon.png';
import spendingIcon from '../../assets/icons/spending-icon.png';
import savedIcon from '../../assets/icons/saved-icon.png';

const EmptyState = ({ onCreateWallet }) => {
    return (
        <div className="max-w-7xl mx-auto pt-8"> {/* Increased max-width */}
            {/* Stats Cards with Left-Centered Icons */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-16"> {/* Increased bottom margin */}
                {/* Total Balance - Dark Background */}
                <div className="bg-[#363A3F] rounded-lg shadow-sm p-6 border border-strokes">
                    <div className="flex items-center">
                        <img src={balanceIcon} alt="Balance" className="w-10 h-10 mr-4" /> {/* Increased icon size */}
                        <div className="flex-1 text-center">
                            <h3 className="text-sm font-medium text-white mb-1">Total balance</h3>
                            <p className="text-2xl font-bold text-white">$0.00</p>
                        </div>
                    </div>
                </div>

                {/* Total Spending */}
                <div className="bg-white rounded-lg shadow-sm p-6 border border-strokes">
                    <div className="flex items-center">
                        <img src={spendingIcon} alt="Spending" className="w-10 h-10 mr-4" /> {/* Increased icon size */}
                        <div className="flex-1 text-center">
                            <h3 className="text-sm font-medium text-metallic-gray mb-1">Total spending</h3>
                            <p className="text-2xl font-bold text-text">$0.00</p>
                        </div>
                    </div>
                </div>

                {/* Total Saved */}
                <div className="bg-white rounded-lg shadow-sm p-6 border border-strokes">
                    <div className="flex items-center">
                        <img src={savedIcon} alt="Saved" className="w-10 h-10 mr-4" /> {/* Increased icon size */}
                        <div className="flex-1 text-center">
                            <h3 className="text-sm font-medium text-metallic-gray mb-1">Total saved</h3>
                            <p className="text-2xl font-bold text-text">$0.00</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Welcome Section - Bigger Image and Text as Image */}
            <div className="flex flex-col lg:flex-row items-center justify-between">
                {/* Welcome Image - Left Side (Much Bigger) */}
                <div className="flex-1 mb-8 lg:mb-0 lg:mr-12 flex justify-center">
                    <img
                        src={welcomeImage}
                        alt="Welcome to Equilibrio"
                        className="max-w-4xl w-full h-auto" // Much bigger size
                    />
                </div>

                {/* Welcome Text as Image with Button - Right Side */}
                <div className="flex-1 flex flex-col items-center lg:items-start justify-center">
                    {/* Replace text with image */}
                    <img
                        src={welcomeTextImage}
                        alt="Welcome to Equilibrio - Create a Wallet and Start tracking your Money Today!"
                        className="mb-8 w-full max-w-md"
                    />

                    {/* Create Wallet Button */}
                    <Button
                        variant="primary"
                        onClick={onCreateWallet}
                        className="px-10 py-4 text-lg font-semibold" // Slightly bigger button
                    >
                        Create Wallet
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default EmptyState;