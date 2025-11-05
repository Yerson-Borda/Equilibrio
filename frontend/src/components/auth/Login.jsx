import React, { useState } from 'react';
import Button from '../ui/Button';
import logo from '../../assets/images/logo.png';
import clockimage from '../../assets/images/clock-image.png';
import { apiService } from '../../services/api';
import { syncService } from '../../services/syncService';

const Login = () => {
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        rememberMe: false
    });
    const [showPassword, setShowPassword] = useState(false);
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);
    const [touched, setTouched] = useState({
        email: false,
        password: false
    });

    const validateEmail = (email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!email) return 'Email is required';
        if (!emailRegex.test(email)) return 'Please enter a valid email address';
        return '';
    };

    const validatePassword = (password) => {
        if (!password) return 'Password is required';
        return '';
    };

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        const fieldValue = type === 'checkbox' ? checked : value;

        setFormData(prev => ({
            ...prev,
            [name]: fieldValue
        }));

        // Real-time validation for touched fields
        if (touched[name]) {
            let error = '';
            if (name === 'email') {
                error = validateEmail(value);
            } else if (name === 'password') {
                error = validatePassword(value);
            }

            setErrors(prev => ({
                ...prev,
                [name]: error
            }));
        }
    };

    const handleBlur = (e) => {
        const { name, value } = e.target;
        setTouched(prev => ({
            ...prev,
            [name]: true
        }));

        // Validate on blur
        let error = '';
        if (name === 'email') {
            error = validateEmail(value);
        } else if (name === 'password') {
            error = validatePassword(value);
        }

        setErrors(prev => ({
            ...prev,
            [name]: error
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setErrors({});

        // Mark all fields as touched
        setTouched({
            email: true,
            password: true
        });

        // Validate all fields
        const emailError = validateEmail(formData.email);
        const passwordError = validatePassword(formData.password);

        if (emailError || passwordError) {
            setErrors({
                email: emailError,
                password: passwordError
            });
            setIsLoading(false);
            return;
        }

        try {
            const data = await apiService.login(formData.email, formData.password);
            console.log('Login successful:', data);

            // Initialize sync service
            await syncService.init();

            // Trigger initial sync
            console.log('Starting initial sync...');
            await syncService.sync();

            // Start periodic sync
            syncService.startPeriodicSync();

            // Redirect to dashboard
            window.location.href = '/dashboard';

        } catch (error) {
            console.error('Login error:', error);

            if (error.status === 401) {
                setErrors({ general: 'The account credentials are incorrect or it doesn\'t exist.' });
            } else if (error.status === 422) {
                const validationErrors = {};
                if (Array.isArray(error.errors)) {
                    error.errors.forEach(err => {
                        if (err.loc && err.msg) {
                            const field = err.loc[err.loc.length - 1];
                            validationErrors[field] = err.msg;
                        }
                    });
                }
                setErrors(validationErrors);
            } else {
                setErrors({ general: error.message || 'An error occurred during login' });
            }
        } finally {
            setIsLoading(false);
        }
    };

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    const closeError = () => {
        setErrors(prev => ({ ...prev, general: '' }));
    };

    return (
        <div className="h-screen bg-background flex">
            {/* Left side - Form */}
            <div className="flex-1 flex items-center justify-center p-8 overflow-y-auto">
                <div className="max-w-md w-full">
                    {/* Logo and Title */}
                    <div className="flex items-center justify-start mb-16">
                        <img src={logo} alt="Equilibrio" className="h-12 mr-4" />
                        <h1 className="text-2xl font-bold text-text">Equilibrio.</h1>
                    </div>

                    <div className="mt-8">
                        <h1 className="text-3xl font-bold text-text text-left mb-2">
                            Welcome back!
                        </h1>
                        <p className="text-metallic-gray text-left mb-8">
                            Please enter your details to log in
                        </p>

                        <form onSubmit={handleSubmit}>
                            {/* Email */}
                            <div className="mb-6">
                                <label className="block text-text font-medium mb-3 text-left">
                                    Email
                                </label>
                                <input
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    onBlur={handleBlur}
                                    placeholder="Enter your email"
                                    className={`w-full p-4 border rounded-lg bg-white placeholder-placeholder focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent ${
                                        errors.email ? 'border-red-500' : 'border-strokes'
                                    }`}
                                    required
                                />
                                {errors.email && (
                                    <p className="text-red-500 text-sm mt-1 text-left">{errors.email}</p>
                                )}
                            </div>

                            {/* Password */}
                            <div className="mb-6">
                                <label className="block text-text font-medium mb-3 text-left">
                                    Password
                                </label>
                                <div className="relative">
                                    <input
                                        type={showPassword ? "text" : "password"}
                                        name="password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        onBlur={handleBlur}
                                        placeholder="*********"
                                        className={`w-full p-4 pr-12 border rounded-lg bg-white placeholder-placeholder focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent ${
                                            errors.password ? 'border-red-500' : 'border-strokes'
                                        }`}
                                        required
                                    />
                                    <button
                                        type="button"
                                        className="absolute inset-y-0 right-0 pr-3 flex items-center"
                                        onClick={togglePasswordVisibility}
                                    >
                                        {showPassword ? (
                                            <svg className="h-5 w-5 text-metallic-gray" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                            </svg>
                                        ) : (
                                            <svg className="h-5 w-5 text-metallic-gray" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                                            </svg>
                                        )}
                                    </button>
                                </div>
                                {errors.password && (
                                    <p className="text-red-500 text-sm mt-1 text-left">{errors.password}</p>
                                )}
                            </div>

                            {/* Remember Me & Forgot Password */}
                            <div className="flex items-center justify-between mb-6">
                                <label className="flex items-center">
                                    <input
                                        type="checkbox"
                                        name="rememberMe"
                                        checked={formData.rememberMe}
                                        onChange={handleChange}
                                        className="h-4 w-4 text-blue focus:ring-blue border-strokes rounded"
                                    />
                                    <span className="ml-2 text-text text-sm">Remember sign in</span>
                                </label>
                                <a href="#" className="text-blue text-sm hover:text-blue-600">
                                    Forgot password?
                                </a>
                            </div>

                            {/* Submit Button */}
                            <Button
                                type="submit"
                                variant="primary"
                                disabled={isLoading}
                                className="w-full"
                            >
                                {isLoading ? 'Signing in...' : 'Sign in'}
                            </Button>
                        </form>

                        {/* Footer */}
                        <div className="text-center mt-6">
                            <p className="text-metallic-gray">
                                Don't have an account?{' '}
                                <a href="/signup" className="text-blue font-medium hover:text-blue-600">
                                    Sign up
                                </a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Right side - Image */}
            <div className="flex-1 hidden lg:block relative">
                <img
                    src={clockimage}
                    alt="Finance Analytics"
                    className="absolute inset-0 w-full h-full object-cover"
                />
            </div>

            {/* Error Notification - Fixed at bottom left corner */}
            {errors.general && (
                <div className="fixed bottom-4 left-4 max-w-sm bg-red-500 bg-opacity-90 text-white p-4 rounded-lg shadow-lg">
                    <div className="flex items-start justify-between">
                        <div className="flex items-start">
                            <span className="text-lg mr-2">☹</span>
                            <p className="text-sm font-medium">{errors.general}</p>
                        </div>
                        <button
                            type="button"
                            className="ml-4 text-white hover:text-gray-200 focus:outline-none"
                            onClick={closeError}
                        >
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Login;