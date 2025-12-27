import React from 'react';

const Card = ({ title, headerRight, children, className = '' }) => {
    return (
        <div className={`bg-white rounded-xl shadow-sm border border-strokes p-6 ${className}`}>
            {(title || headerRight) && (
                <div className="flex items-center justify-between mb-4">
                    {title && (
                        <h2 className="text-lg font-semibold text-text">
                            {title}
                        </h2>
                    )}
                    {headerRight && <div>{headerRight}</div>}
                </div>
            )}

            {children}
        </div>
    );
};

export default Card;
