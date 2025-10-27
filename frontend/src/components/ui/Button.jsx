import React from 'react';

const Button = ({ children, variant = 'primary', className = '', style, ...props }) => {
    const baseClasses = 'py-3 px-6 rounded-lg font-medium transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue focus:ring-offset-2';

    const variants = {
        primary: 'bg-blue text-white hover:bg-blue-600',
        secondary: 'bg-gray-200 text-gray-700 hover:bg-gray-300',
        outline: 'border border-strokes bg-white text-text hover:bg-soft-gray'
    };

    return (
        <button
            className={`${baseClasses} ${variants[variant]} ${className}`}
            style={style}
            {...props}
        >
            {children}
        </button>
    );
};

export default Button;