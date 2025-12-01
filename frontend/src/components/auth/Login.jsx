import React, { useState } from 'react';
import Button from '../ui/Button';
import logo from '../../assets/images/logo.png';
import clockimage from '../../assets/images/clock-image.png';
import { apiService } from '../../services/api';

const Login = () => {
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        rememberMe: false,
    });
    const [showPassword, setShowPassword] = useState(false);
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);
    const [touched, setTouched] = useState({
        email: false,
        password: false,
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

        setFormData((prev) => ({
            ...prev,
            [name]: fieldValue,
        }));

        if (touched[name]) {
            let error = '';
            if (name === 'email') {
                error = validateEmail(value);
            } else if (name === 'password') {
                error = validatePassword(value);
            }

            setErrors((prev) => ({
                ...prev,
                [name]: error,
            }));
        }
    };

    const handleBlur = (e) => {
        const { name, value } = e.target;

        setTouched((prev) => ({
            ...prev,
            [name]: true,
        }));

        let error = '';
        if (name === 'email') {
            error = validateEmail(value);
        } else if (name === 'password') {
            error = validatePassword(value);
        }

        setErrors((prev) => ({
            ...prev,
            [name]: error,
        }));
    };

    const togglePasswordVisibility = () => {
        setShowPassword((prev) => !prev);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setErrors({});

        // mark all as touched
        setTouched({
            email: true,
            password: true,
        });

        const emailError = validateEmail(formData.email);
        const passwordError = validatePassword(formData.password);

        if (emailError || passwordError) {
            setErrors({
                email: emailError,
                password: passwordError,
            });
            setIsLoading(false);
            return;
        }

        try {
            const data = await apiService.login(formData.email, formData.password);
            console.log('Login successful:', data);

            // let App.jsx know the user logged in so it can initialize sync
            window.dispatchEvent(new Event('user_logged_in'));

            // redirect to dashboard
            window.location.href = '/dashboard';
        } catch (error) {
            console.error('Login error:', error);

            if (error.status === 401) {
                setErrors({ general: 'Invalid email or password' });
            } else {
                setErrors({
                    general: error.message || 'An error occurred during login',
                });
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="h-screen bg-background flex">
            {/* Left side - Form (same structure as SignUp) */}
            <div className="flex-1 flex items-center justify-center p-8 overflow-y-auto">
                <div className="max-w-md w-full">
                    {/* Logo */}
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

                        <form onSubmit={handleSubmit} className="space-y-6">
                            {/* Email */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Email
                                </label>
                                <input
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    onBlur={handleBlur}
                                    placeholder="Enter your email"
                                    className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                />
                                {errors.email && (
                                    <p className="text-red-500 text-sm mt-1 text-left">
                                        {errors.email}
                                    </p>
                                )}
                            </div>

                            {/* Password */}
                            <div>
                                <label className="block text-sm font-medium text-text mb-2">
                                    Password
                                </label>
                                <div className="relative">
                                    <input
                                        type={showPassword ? 'text' : 'password'}
                                        name="password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        onBlur={handleBlur}
                                        placeholder="••••••••"
                                        className="w-full p-3 pr-10 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                    />
                                    <button
                                        type="button"
                                        onClick={togglePasswordVisibility}
                                        className="absolute inset-y-0 right-0 pr-3 flex items-center text-sm leading-5"
                                    >
                                        {showPassword ? (
                                            <svg
                                                className="h-5 w-5 text-metallic-gray"
                                                fill="none"
                                                viewBox="0 0 24 24"
                                                stroke="currentColor"
                                            >
                                                <path
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                    strokeWidth={2}
                                                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                                                />
                                                <path
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                    strokeWidth={2}
                                                    d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                                                />
                                            </svg>
                                        ) : (
                                            <svg
                                                className="h-5 w-5 text-metallic-gray"
                                                fill="none"
                                                viewBox="0 0 24 24"
                                                stroke="currentColor"
                                            >
                                                <path
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                    strokeWidth={2}
                                                    d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"
                                                />
                                            </svg>
                                        )}
                                    </button>
                                </div>
                                {errors.password && (
                                    <p className="text-red-500 text-sm mt-1 text-left">
                                        {errors.password}
                                    </p>
                                )}
                            </div>

                            {/* Remember / forgot row */}
                            <div className="flex items-center justify-between text-sm">
                                <label className="inline-flex items-center">
                                    <input
                                        type="checkbox"
                                        name="rememberMe"
                                        checked={formData.rememberMe}
                                        onChange={handleChange}
                                        className="form-checkbox h-4 w-4 text-blue"
                                    />
                                    <span className="ml-2 text-text">Remember sign in</span>
                                </label>
                                <button
                                    type="button"
                                    className="text-blue hover:text-blue-600"
                                >
                                    Forgot password
                                </button>
                            </div>

                            {/* Divider */}
                            <div className="relative my-4">
                                <div className="absolute inset-0 flex items-center">
                                    <div className="w-full border-t border-strokes" />
                                </div>
                            </div>

                            {/* Submit */}
                            <Button
                                type="submit"
                                variant="primary"
                                disabled={isLoading}
                                className="w-full"
                            >
                                {isLoading ? 'Signing in...' : 'Sign in'}
                            </Button>
                        </form>

                        {/* General error */}
                        {errors.general && (
                            <div className="mt-6 p-4 bg-red-100 border-l-4 border-red-500 text-left">
                                <p className="text-red-700 text-sm">{errors.general}</p>
                            </div>
                        )}

                        {/* Footer */}
                        <div className="text-center mt-6">
                            <p className="text-metallic-gray">
                                Don&apos;t have an account?{' '}
                                <a
                                    href="/signup"
                                    className="text-blue font-medium hover:text-blue-600"
                                >
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
        </div>
    );
};

export default Login;
