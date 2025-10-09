import React, { useState } from 'react';
import Button from '../ui/Button';
import logo from '../../assets/images/logo.png';
import clockimage from '../../assets/images/clock-image.png';
import { apiService } from '../../services/api';

const SignUp = () => {
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: ''
    });
    const [showPassword, setShowPassword] = useState(false);
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
        // Clear errors when user starts typing
        if (errors[e.target.name]) {
            setErrors({
                ...errors,
                [e.target.name]: ''
            });
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setErrors({});

        try {
            const data = await apiService.register(formData.fullName, formData.email, formData.password);
            console.log('Sign up successful:', data);

            // Auto-login after successful registration or redirect to login
            // You can automatically log them in:
            // const loginData = await apiService.login(formData.email, formData.password);

        } catch (error) {
            console.error('Sign up error:', error);

            if (error.status === 422) {
                // Handle validation errors from backend
                const validationErrors = {};
                if (Array.isArray(error.errors)) {
                    error.errors.forEach(err => {
                        if (err.loc && err.msg) {
                            const field = err.loc[err.loc.length - 1];
                            // Map backend field names to frontend field names
                            const frontendField = field === 'full_name' ? 'fullName' : field;
                            validationErrors[frontendField] = err.msg;
                        }
                    });
                }
                setErrors(validationErrors);
            } else if (error.status === 400) {
                setErrors({ general: 'User with this email already exists' });
            } else {
                setErrors({ general: error.message || 'An error occurred during registration' });
            }
        } finally {
            setIsLoading(false);
        }
    };

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    return (
        <div className="h-screen bg-background flex">
            {/* Left side - Form (without card style) */}
            <div className="flex-1 flex items-center justify-center p-8 overflow-y-auto">
                <div className="max-w-md w-full">
                    {/* Logo and Title - At the top */}
                    <div className="flex items-center justify-start mb-16">
                        <img
                            src={logo}
                            alt="Equilibrio"
                            className="h-12 mr-4"
                        />
                        <h1 className="text-2xl font-bold text-text">Equilibrio.</h1>
                    </div>

                    {/* Form without card styling */}
                    <div className="mt-8">
                        <h1 className="text-3xl font-bold text-text text-left mb-2">
                            Create an account
                        </h1>
                        <p className="text-metallic-gray text-left mb-8">
                            Please enter your details to register
                        </p>

                        {/* General error message */}
                        {errors.general && (
                            <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                                <p className="text-red-600 text-sm">{errors.general}</p>
                            </div>
                        )}

                        <form onSubmit={handleSubmit}>
                            {/* Full Name */}
                            <div className="mb-6">
                                <label className="block text-text font-medium mb-3 text-left">
                                    Full Name
                                </label>
                                <input
                                    type="text"
                                    name="fullName"
                                    value={formData.fullName}
                                    onChange={handleChange}
                                    placeholder="Full name"
                                    className={`w-full p-4 border rounded-lg bg-white placeholder-placeholder focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent ${
                                        errors.fullName ? 'border-red-500' : 'border-strokes'
                                    }`}
                                    required
                                />
                                {errors.fullName && (
                                    <p className="text-red-500 text-sm mt-1 text-left">{errors.fullName}</p>
                                )}
                            </div>

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
                                    placeholder="example@gmail.com"
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
                                        placeholder="••••••••"
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

                            {/* Divider */}
                            <div className="relative my-6">
                                <div className="absolute inset-0 flex items-center">
                                    <div className="w-full border-t border-strokes"></div>
                                </div>
                            </div>

                            {/* Submit Button */}
                            <Button
                                type="submit"
                                variant="primary"
                                disabled={isLoading}
                                className="w-full"
                            >
                                {isLoading ? 'Creating Account...' : 'Create Account'}
                            </Button>
                        </form>

                        {/* Footer */}
                        <div className="text-center mt-6">
                            <p className="text-metallic-gray">
                                Already have an account?{' '}
                                <a href="/login" className="text-blue font-medium hover:text-blue-600">
                                    Sign in
                                </a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Right side - Image without scroll */}
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

export default SignUp;