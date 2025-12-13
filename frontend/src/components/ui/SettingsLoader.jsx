import React from "react";

const SettingsLoader = ({ className = "" }) => {
    return (
        <div className={`w-full h-[60vh] flex items-center justify-center ${className}`}>
            <div className="relative w-44 h-44">
                {/* base ring */}
                <div className="absolute inset-0 rounded-full border-[14px] border-[#E9EEF6]" />

                {/* spinning arc */}
                <div className="absolute inset-0 rounded-full border-[14px] border-blue border-t-transparent border-r-transparent animate-spin" />

                {/* inner cutout */}
                <div className="absolute inset-[22px] rounded-full bg-background" />
            </div>
        </div>
    );
};

export default SettingsLoader;
