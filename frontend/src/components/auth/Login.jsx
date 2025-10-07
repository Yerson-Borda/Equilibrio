import React, { useState } from 'react';
import Button from '../ui/Button';
import logo from '../../assets/images/logo.png';
import clockimage from '../../assets/images/clock-image.png';

const Login = () => {
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        rememberMe: false
    });
    const [showPassword, setShowPassword] = useState(false);

    const handleChange = (e) => {
        const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
        setFormData({
            ...formData,
            [e.target.name]: value
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log('Login data:', formData);
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
                                    placeholder="Enter your email"
                                    className="w-full p-4 border border-strokes rounded-lg bg-white placeholder-placeholder focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                    required
                                />
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
                                        placeholder="*********"
                                        className="w-full p-4 pr-12 border border-strokes rounded-lg bg-white placeholder-placeholder focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
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
                            <Button type="submit" variant="primary">
                                Sign in
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

export default Login;