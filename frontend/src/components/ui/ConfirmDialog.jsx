import React from "react";
import Button from "./Button";

const ConfirmDialog = ({
                           isOpen,
                           title = "Confirm",
                           message = "",
                           confirmText = "Confirm",
                           cancelText = "Cancel",
                           onConfirm,
                           onCancel,
                           loading = false,
                           danger = false,
                       }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[99999]">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/40"
                onClick={loading ? undefined : onCancel}
            />

            {/* Dialog */}
            <div className="relative h-full w-full flex items-center justify-center p-4">
                <div className="bg-white rounded-2xl shadow-2xl w-full max-w-[620px] px-10 py-8">
                    <h2 className="text-3xl font-extrabold text-text text-center">
                        {title}
                    </h2>

                    {message ? (
                        <p className="text-center text-metallic-gray mt-2">{message}</p>
                    ) : null}

                    <div className="mt-10 flex items-center justify-center gap-6">
                        <Button
                            type="button"
                            variant="secondary"
                            className="min-w-[220px] h-[56px] rounded-xl border border-strokes bg-white text-text font-semibold hover:bg-gray-50 transition"
                            onClick={onCancel}
                            disabled={loading}
                        >
                            {cancelText}
                        </Button>

                        <button
                            type="button"
                            onClick={onConfirm}
                            disabled={loading}
                            className={`min-w-[220px] h-[56px] rounded-xl text-white font-semibold transition disabled:opacity-60
                ${danger ? "bg-red-500 hover:bg-red-600" : "bg-blue hover:opacity-90"}`}
                        >
                            {loading ? "Please wait..." : confirmText}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ConfirmDialog;
