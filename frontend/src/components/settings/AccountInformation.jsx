import React, { useState, useEffect } from 'react';
import Button from '../ui/Button';
import { apiService } from '../../services/api';

// Import your avatar placeholder image
import avatarPlaceholder from '../../assets/images/avatar-placeholder.png';

const AccountInformation = () => {
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        date_of_birth: '',
        email: '',
        phone_number: '',
        default_currency: 'USD',
        password: '',
        confirmPassword: ''
    });
    const [isLoading, setIsLoading] = useState(false);
    const [errors, setErrors] = useState({});
    const [user, setUser] = useState(null);
    const [avatarFile, setAvatarFile] = useState(null);
    const [avatarPreview, setAvatarPreview] = useState(null);

    useEffect(() => {
        fetchUserData();
    }, []);

    const fetchUserData = async () => {
        try {
            const userData = await apiService.getCurrentUser();
            setUser(userData);

            // Split full_name into first and last name
            const fullName = userData.full_name || '';
            const nameParts = fullName.split(' ');
            const firstName = nameParts[0] || '';
            const lastName = nameParts.slice(1).join(' ') || '';

            setFormData(prev => ({
                ...prev,
                firstName: firstName,
                lastName: lastName,
                date_of_birth: userData.date_of_birth || '',
                email: userData.email || '',
                phone_number: userData.phone_number || '',
                default_currency: userData.default_currency || 'USD'
            }));

            if (userData.avatar_url) {
                // Make sure the avatar URL includes the full path
                const fullAvatarUrl = userData.avatar_url.startsWith('http')
                    ? userData.avatar_url
                    : `${window.location.origin}${userData.avatar_url}`;
                setAvatarPreview(fullAvatarUrl);
            }
        } catch (error) {
            console.error('Error fetching user data:', error);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));

        // Clear error when user types
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }

        // Check password match
        if (name === 'confirmPassword' && value !== formData.password) {
            setErrors(prev => ({
                ...prev,
                confirmPassword: 'Passwords do not match'
            }));
        } else if (name === 'confirmPassword' && value === formData.password) {
            setErrors(prev => ({
                ...prev,
                confirmPassword: ''
            }));
        }
    };

    const handleAvatarChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setAvatarFile(file);
            const reader = new FileReader();
            reader.onloadend = () => {
                setAvatarPreview(reader.result);
            };
            reader.readAsDataURL(file);

            // Auto-upload when file is selected
            uploadAvatar(file);
        }
    };

    const uploadAvatar = async (file = avatarFile) => {
        if (!file) return;

        try {
            const updatedUser = await apiService.uploadAvatar(file);
            setUser(updatedUser);

            // Update avatar preview with the new URL
            if (updatedUser.avatar_url) {
                const fullAvatarUrl = updatedUser.avatar_url.startsWith('http')
                    ? updatedUser.avatar_url
                    : `${window.location.origin}${updatedUser.avatar_url}`;
                setAvatarPreview(fullAvatarUrl);
            }

            alert('Avatar updated successfully!');
            return true;
        } catch (error) {
            console.error('Error uploading avatar:', error);
            alert('Failed to upload avatar. Please try again.');
            return false;
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setErrors({});

        // Validate passwords match
        if (formData.password && formData.password !== formData.confirmPassword) {
            setErrors({ confirmPassword: 'Passwords do not match' });
            setIsLoading(false);
            return;
        }

        try {
            // Combine first name and last name into full_name for backend
            const fullName = `${formData.firstName} ${formData.lastName}`.trim();

            const updateData = {
                full_name: fullName,
                date_of_birth: formData.date_of_birth,
                email: formData.email,
                phone_number: formData.phone_number,
                default_currency: formData.default_currency
            };

            // Only include password if provided
            if (formData.password) {
                updateData.password = formData.password;
            }

            await apiService.updateUser(updateData);
            alert('Profile updated successfully!');

            // Clear password fields
            setFormData(prev => ({
                ...prev,
                password: '',
                confirmPassword: ''
            }));

            // Refresh user data
            await fetchUserData();
        } catch (error) {
            console.error('Error updating profile:', error);
            alert('Failed to update profile. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    const currencies = [
        'USD', 'EUR', 'GBP', 'JPY', 'CAD', 'AUD', 'CHF', 'CNY', 'INR'
    ];

    return (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Left side - Form (No Box, Bigger Fields) */}
            <div className="lg:col-span-2">
                <h2 className="text-2xl font-bold text-text mb-8">Personal Information</h2>

                <form onSubmit={handleSubmit} className="space-y-8">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                        {/* First Name */}
                        <div className="space-y-3">
                            <label className="block text-lg font-medium text-text">
                                First Name
                            </label>
                            <input
                                type="text"
                                name="firstName"
                                value={formData.firstName}
                                onChange={handleChange}
                                className="w-full p-5 text-lg bg-transparent border-b-2 border-strokes focus:outline-none focus:border-blue transition-colors"
                                placeholder="Enter first name"
                            />
                        </div>

                        {/* Last Name */}
                        <div className="space-y-3">
                            <label className="block text-lg font-medium text-text">
                                Last Name
                            </label>
                            <input
                                type="text"
                                name="lastName"
                                value={formData.lastName}
                                onChange={handleChange}
                                placeholder="Enter last name"
                                className="w-full p-5 text-lg bg-transparent border-b-2 border-strokes focus:outline-none focus:border-blue transition-colors"
                            />
                        </div>

                        {/* Date of Birth */}
                        <div className="space-y-3">
                            <label className="block text-lg font-medium text-text">
                                Date of Birth
                            </label>
                            <input
                                type="date"
                                name="date_of_birth"
                                value={formData.date_of_birth}
                                onChange={handleChange}
                                className="w-full p-5 text-lg bg-transparent border-b-2 border-strokes focus:outline-none focus:border-blue transition-colors"
                            />
                        </div>

                        {/* Mobile Number */}
                        <div className="space-y-3">
                            <label className="block text-lg font-medium text-text">
                                Mobile Number
                            </label>
                            <input
                                type="tel"
                                name="phone_number"
                                value={formData.phone_number}
                                onChange={handleChange}
                                placeholder="+123 456 7890"
                                className="w-full p-5 text-lg bg-transparent border-b-2 border-strokes focus:outline-none focus:border-blue transition-colors"
                            />
                        </div>

                        {/* Email */}
                        <div className="space-y-3">
                            <label className="block text-lg font-medium text-text">
                                Email
                            </label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-5 flex items-center pointer-events-none">
                                    <svg className="h-6 w-6 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                                        <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                                        <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
                                    </svg>
                                </div>
                                <input
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    className="w-full pl-14 p-5 text-lg bg-transparent border-b-2 border-strokes focus:outline-none focus:border-blue transition-colors"
                                    placeholder="Enter your email"
                                />
                            </div>
                        </div>

                        {/* Display Currency */}
                        <div className="space-y-3">
                            <label className="block text-lg font-medium text-text">
                                Display currency
                            </label>
                            <div className="relative">
                                <select
                                    name="default_currency"
                                    value={formData.default_currency}
                                    onChange={handleChange}
                                    className="w-full p-5 text-lg bg-transparent border-b-2 border-strokes focus:outline-none focus:border-blue transition-colors appearance-none"
                                >
                                    {currencies.map(currency => (
                                        <option key={currency} value={currency}>
                                            {currency}
                                        </option>
                                    ))}
                                </select>
                                <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700">
                                    <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                                    </svg>
                                </div>
                            </div>
                        </div>

                        {/* New Password */}
                        <div className="space-y-3">
                            <label className="block text-lg font-medium text-text">
                                New Password
                            </label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-5 flex items-center pointer-events-none">
                                    <svg className="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                                    </svg>
                                </div>
                                <input
                                    type="password"
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    placeholder="••••••••"
                                    className="w-full pl-14 pr-14 p-5 text-lg bg-transparent border-b-2 border-strokes focus:outline-none focus:border-blue transition-colors"
                                />
                                <button
                                    type="button"
                                    className="absolute inset-y-0 right-0 pr-5 flex items-center"
                                    onClick={(e) => {
                                        const input = e.target.closest('.relative').querySelector('input');
                                        input.type = input.type === 'password' ? 'text' : 'password';
                                    }}
                                >
                                    <svg className="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                    </svg>
                                </button>
                            </div>
                        </div>

                        {/* Confirm Password */}
                        <div className="space-y-3">
                            <label className="block text-lg font-medium text-text">
                                Confirm Password
                            </label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-5 flex items-center pointer-events-none">
                                    <svg className="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                                    </svg>
                                </div>
                                <input
                                    type="password"
                                    name="confirmPassword"
                                    value={formData.confirmPassword}
                                    onChange={handleChange}
                                    placeholder="••••••••"
                                    className="w-full pl-14 pr-14 p-5 text-lg bg-transparent border-b-2 border-strokes focus:outline-none focus:border-blue transition-colors"
                                />
                                <button
                                    type="button"
                                    className="absolute inset-y-0 right-0 pr-5 flex items-center"
                                    onClick={(e) => {
                                        const input = e.target.closest('.relative').querySelector('input');
                                        input.type = input.type === 'password' ? 'text' : 'password';
                                    }}
                                >
                                    <svg className="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                    </svg>
                                </button>
                            </div>
                            {errors.confirmPassword && (
                                <p className="text-red-500 text-lg mt-2">{errors.confirmPassword}</p>
                            )}
                        </div>
                    </div>

                    <div className="pt-8">
                        <Button
                            type="submit"
                            variant="primary"
                            className="px-12 py-4 text-lg font-semibold"
                            disabled={isLoading}
                        >
                            {isLoading ? 'Updating...' : 'Update'}
                        </Button>
                    </div>
                </form>
            </div>

            {/* Right side - Only the image as upload button */}
            <div className="lg:col-span-1 flex items-center justify-center">
                {/* Clickable Picture for Avatar Upload */}
                <label className="cursor-pointer group relative">
                    <div className="w-48 h-48 rounded-full overflow-hidden border-4 border-gray-200 group-hover:border-blue transition-colors duration-300 shadow-lg">
                        {avatarPreview ? (
                            <img
                                src={avatarPreview}
                                alt="Profile"
                                className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-110"
                            />
                        ) : (
                            <img
                                src={avatarPlaceholder}
                                alt="Upload Avatar"
                                className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-110"
                            />
                        )}
                    </div>

                    {/* Upload Icon Overlay */}
                    <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-0 group-hover:bg-opacity-40 rounded-full transition-all duration-300">
                        <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                            <svg className="w-16 h-16 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                            </svg>
                        </div>
                    </div>

                    {/* Hidden File Input */}
                    <input
                        type="file"
                        className="hidden"
                        onChange={handleAvatarChange}
                        accept="image/*"
                    />
                </label>
            </div>
        </div>
    );
};

export default AccountInformation;