import React from 'react';

const Select = ({
                    label,
                    id,
                    error,
                    helperText,
                    className = '',
                    wrapperClassName = '',
                    children,
                    ...props
                }) => {
    const selectId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

    return (
        <div className={wrapperClassName}>
            {label && (
                <label
                    htmlFor={selectId}
                    className="block text-sm font-medium text-text mb-1"
                >
                    {label}
                </label>
            )}

            <select
                id={selectId}
                className={`w-full px-3 py-2 border rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent ${
                    error ? 'border-red-500' : 'border-strokes'
                } ${className}`}
                {...props}
            >
                {children}
            </select>

            {error ? (
                <p className="mt-1 text-xs text-red-500">{error}</p>
            ) : helperText ? (
                <p className="mt-1 text-xs text-metallic-gray">{helperText}</p>
            ) : null}
        </div>
    );
};

export default Select;
