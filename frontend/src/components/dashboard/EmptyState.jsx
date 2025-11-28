import React from 'react';
import welcomeImage from '../../assets/images/welcome-image.png';
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
        <div className="max-w-9xl mx-auto ">
            {/* Stats Cards with Left-Centered Icons */}
            <div className="flex flex-row gap-[15px] mb-6">
                {/* Total Balance - Dark Background */}
                <div className="bg-[#363A3F] flex flex-row items-center w-[222px] h-[110px] rounded-[10px] shadow-sm border border-strokes pt-6 pr-5 pb-6 pl-5 gap-[15px]">
                    <img src={balanceIcon} alt="Balance" className="w-10 h-10" />
                    <div className="flex-1 text-center">
                        <h3 className="text-sm font-medium text-[#9c9c9c] mb-1">Total balance</h3>
                        <p className="text-2xl font-bold text-white">${totalBalance.toFixed(2)}</p>
                    </div>
                </div>

                {/* Total Spending */}
                <div className="bg-white flex flex-row items-center w-[222px] h-[110px] rounded-[10px] shadow-sm border border-strokes pt-6 pr-5 pb-6 pl-5 gap-[15px]">
                    <img src={spendingIcon} alt="Spending" className="w-10 h-10" />
                    <div className="flex-1 text-center">
                        <h3 className="text-sm font-medium text-metallic-gray mb-1">Total spending</h3>
                        <p className="text-2xl font-bold text-text">${totalSpending.toFixed(2)}</p>
                    </div>
                </div>

                {/* Total Saved */}
                <div className="bg-white flex flex-row items-center w-[222px] h-[110px] rounded-[10px] shadow-sm border border-strokes pt-6 pr-5 pb-6 pl-5 gap-[15px]">
                    <img src={savedIcon} alt="Saved" className="w-10 h-10" />
                    <div className="flex-1 text-center">
                        <h3 className="text-sm font-medium text-metallic-gray mb-1">Total saved</h3>
                        <p className="text-2xl font-bold text-text">${totalSaved.toFixed(2)}</p>
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
                        className="max-w-2xl w-full h-auto"
                    />
                </div>

                {/* Welcome Text and Button - Right Side */}
                <div className="flex-1 flex flex-col items-center justify-center">
                    {/* Welcome Text Image - Centered above button */}
                    <div className="text-center mb-8">
                        <h1 className="text-6xl text-[#598eff]">
                            Welcome to <span className="text-[#4361ee] font-extrabold">Equilibrio</span>
                        </h1>
                        <p className="text-[#4361ee] mt-2 text-3xl">
                            Create a Wallet and Start tracking your Money Today!
                        </p>
                    </div>

                    {/* Create Wallet Button - LONG BAR */}
                    <div className="w-full max-w-xl">
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