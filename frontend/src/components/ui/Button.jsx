import React from 'react';

const Button = ({
                    children,
                    variant = 'primary',
                    className = '',
                    style,
                    disabled = false,
                    ...props
                }) => {
    const baseClasses =
        'inline-flex items-center justify-center px-4 py-2 rounded-lg text-sm font-medium ' +
        'transition-colors focus:outline-none focus:ring-2 focus:ring-blue focus:ring-offset-2 ' +
        'disabled:opacity-60 disabled:cursor-not-allowed';

    const variants = {
        primary: 'bg-blue text-white hover:bg-blue-600',
        secondary: 'bg-gray-200 text-gray-700 hover:bg-gray-300',
        outline: 'border border-strokes bg-white text-text hover:bg-soft-gray',
        ghost: 'bg-transparent text-text hover:bg-gray-100',
        danger: 'bg-red-500 text-white hover:bg-red-600'
    };

    const variantClasses = variants[variant] || variants.primary;

    return (
        <button
            className={`${baseClasses} ${variantClasses} ${className}`}
            style={style}
            disabled={disabled}
            {...props}
        >
            {children}
        </button>
    );
};

export default Button;
