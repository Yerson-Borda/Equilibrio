import React, { useState } from 'react';
import Button from '../ui/Button';
import logo from '../../assets/images/logo.png';
import clockimage from '../../assets/images/clock-image.png';

const SignUp = () => {
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: ''
    });

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log('Sign up data:', formData);
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
                                    className="w-full p-4 border border-strokes rounded-lg bg-white placeholder-placeholder focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                    required
                                />
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
                                    placeholder="••••••••"
                                    className="w-full p-4 border border-strokes rounded-lg bg-white placeholder-placeholder focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                                    required
                                />
                            </div>

                            {/* Divider */}
                            <div className="relative my-6">
                                <div className="absolute inset-0 flex items-center">
                                    <div className="w-full border-t border-strokes"></div>
                                </div>
                            </div>

                            {/* Submit Button */}
                            <Button type="submit" variant="primary">
                                Create Account
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