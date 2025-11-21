import React from 'react';
import { useNavigate } from 'react-router-dom';
import errorImage from '../assets/images/something-went-wrong.png'; // rename your PNG into this path

const UnauthorizedPage = () => {
    const navigate = useNavigate();

    const handleTryAgain = () => {
        navigate('/login');
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-background">
            <div className="max-w-3xl w-full px-4 text-center">
                <img
                    src={errorImage}
                    alt="Something went wrong"
                    className="mx-auto mb-10 max-w-xl w-full h-auto"
                />
                <h1 className="text-3xl md:text-4xl font-bold text-text mb-4">
                    Oops! Something Went Wrong
                </h1>
                <p className="text-metallic-gray mb-8">
                    You’re not logged in, so we can’t show this page. Please sign in and
                    try again.
                </p>
                <button
                    onClick={handleTryAgain}
                    className="px-8 py-3 bg-blue text-white font-semibold rounded-lg hover:bg-blue-600 transition-colors"
                >
                    Try Again
                </button>
            </div>
        </div>
    );
};

export default UnauthorizedPage;
