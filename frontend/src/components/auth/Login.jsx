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
                                <input
                                    type="password"
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    placeholder="*********"
                                    className="w-full p-4 border border-strokes rounded-lg bg-white placeholder-placeholder focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                    required
                                />
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