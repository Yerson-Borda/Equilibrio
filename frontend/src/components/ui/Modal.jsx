import React from 'react';
import Button from './Button';

const Modal = ({
                   isOpen,
                   onClose,
                   title,
                   children,
                   footer,
                   size = 'md', // 'sm' | 'md' | 'lg' | 'xl'
               }) => {
    if (!isOpen) return null;

    const sizeClasses = {
        sm: 'max-w-md',
        md: 'max-w-lg',
        lg: 'max-w-2xl',
        xl: 'max-w-4xl',
    };

    const widthClass = sizeClasses[size] || sizeClasses.md;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
            <div className={`bg-white rounded-xl shadow-xl w-full ${widthClass}`}>
                {/* Header */}
                <div className="flex items-center justify-between px-6 py-4 border-b border-strokes">
                    <h2 className="text-lg md:text-xl font-semibold text-text">{title}</h2>
                    <button
                        type="button"
                        onClick={onClose}
                        className="text-metallic-gray hover:text-text text-xl leading-none px-2"
                    >
                        ×
                    </button>
                </div>

                {/* Body */}
                <div className="px-6 py-4">
                    {children}
                </div>

                {/* Footer */}
                {footer && (
                    <div className="px-6 py-4 border-t border-strokes bg-soft-gray flex justify-end space-x-3 rounded-b-xl">
                        {footer}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Modal;
