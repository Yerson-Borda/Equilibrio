import React from 'react';

const Button = ({ children, variant = 'primary', className = '', ...props }) => {
    const baseClasses = 'w-full py-3 px-4 rounded-lg font-medium transition-colors duration-200';

    const variants = {
        primary: 'bg-blue text-white hover:bg-blue-600',
        secondary: 'bg-green text-white hover:bg-green-600',
        outline: 'border border-strokes bg-white text-text hover:bg-soft-gray'
    };

    return (
        <button
            className={`${baseClasses} ${variants[variant]} ${className}`}
            {...props}
        >
            {children}
        </button>
    );
};

export default Button;