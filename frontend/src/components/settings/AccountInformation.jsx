import React, { useEffect, useRef, useState } from "react";
import Button from "../ui/Button";
import { apiService } from "../../services/api";
import SettingsLoader from "../ui/SettingsLoader";

const AccountInformation = () => {
    const fileInputRef = useRef(null);

    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        date_of_birth: "",
        email: "",
        phone_number: "",
        default_currency: "USD",
        password: "",
        confirmPassword: "",
    });

    // Submit loading (Update button)
    const [isLoading, setIsLoading] = useState(false);

    // Page loading (ring loader like design)
    const [isPageLoading, setIsPageLoading] = useState(true);

    const [errors, setErrors] = useState({});
    const [user, setUser] = useState(null);

    const [avatarFile, setAvatarFile] = useState(null);
    const [avatarPreview, setAvatarPreview] = useState(null);

    useEffect(() => {
        fetchUserData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const fetchUserData = async () => {
        try {
            setIsPageLoading(true);

            const userData = await apiService.getCurrentUser();
            setUser(userData);

            const fullName = userData.full_name || "";
            const parts = fullName.split(" ").filter(Boolean);
            const firstName = parts[0] || "";
            const lastName = parts.slice(1).join(" ") || "";

            setFormData((prev) => ({
                ...prev,
                firstName,
                lastName,
                date_of_birth: userData.date_of_birth || "",
                email: userData.email || "",
                phone_number: userData.phone_number || "",
                default_currency: userData.default_currency || "USD",
            }));

            if (userData.avatar_url) {
                const fullAvatarUrl = userData.avatar_url.startsWith("http")
                    ? userData.avatar_url
                    : `${window.location.origin}${userData.avatar_url}`;
                setAvatarPreview(fullAvatarUrl);
            } else {
                setAvatarPreview(null);
            }
        } catch (error) {
            console.error("Error fetching user data:", error);
        } finally {
            setIsPageLoading(false);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;

        setFormData((prev) => ({ ...prev, [name]: value }));

        if (errors[name]) {
            setErrors((prev) => ({ ...prev, [name]: "" }));
        }

        // live confirm password check
        if (name === "confirmPassword") {
            if (value && value !== formData.password) {
                setErrors((prev) => ({ ...prev, confirmPassword: "Passwords do not match" }));
            } else {
                setErrors((prev) => ({ ...prev, confirmPassword: "" }));
            }
        }
    };

    const uploadAvatar = async (file = avatarFile) => {
        if (!file) return false;

        try {
            const updatedUser = await apiService.uploadAvatar(file);
            setUser(updatedUser);

            if (updatedUser.avatar_url) {
                const fullAvatarUrl = updatedUser.avatar_url.startsWith("http")
                    ? updatedUser.avatar_url
                    : `${window.location.origin}${updatedUser.avatar_url}`;
                setAvatarPreview(fullAvatarUrl);
            }

            alert("Avatar updated successfully!");
            return true;
        } catch (error) {
            console.error("Error uploading avatar:", error);
            alert("Failed to upload avatar. Please try again.");
            return false;
        }
    };

    const handleAvatarChange = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        setAvatarFile(file);

        // instant preview
        const reader = new FileReader();
        reader.onloadend = () => setAvatarPreview(reader.result);
        reader.readAsDataURL(file);

        // upload to backend
        await uploadAvatar(file);

        // allow selecting the same file again later
        e.target.value = "";
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setErrors({});

        if (formData.password && formData.password !== formData.confirmPassword) {
            setErrors({ confirmPassword: "Passwords do not match" });
            setIsLoading(false);
            return;
        }

        try {
            const full_name = `${formData.firstName} ${formData.lastName}`.trim();

            const updateData = {
                full_name,
                date_of_birth: formData.date_of_birth,
                email: formData.email,
                phone_number: formData.phone_number,
                default_currency: formData.default_currency,
            };

            if (formData.password) updateData.password = formData.password;

            await apiService.updateUser(updateData);

            alert("Profile updated successfully!");

            setFormData((prev) => ({
                ...prev,
                password: "",
                confirmPassword: "",
            }));

            await fetchUserData();
        } catch (error) {
            console.error("Error updating profile:", error);
            alert("Failed to update profile. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    const openFilePicker = () => fileInputRef.current?.click();

    const togglePasswordVisibility = (containerEl) => {
        const input = containerEl?.querySelector("input");
        if (!input) return;
        input.type = input.type === "password" ? "text" : "password";
    };

    const currencies = ["USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "INR"];

    // design loader
    if (isPageLoading) return <SettingsLoader />;

    return (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-10">
            {/* Left side - Form */}
            <div className="lg:col-span-2">
                <h2 className="text-2xl font-bold text-text mb-8">Personal Information</h2>

                <form onSubmit={handleSubmit} className="space-y-8">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                        {/* First Name */}
                        <div className="space-y-3">
                            <label className="block text-lg font-medium text-text">First Name</label>
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
                            <label className="block text-lg font-medium text-text">Last Name</label>
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
                            <label className="block text-lg font-medium text-text">Date of Birth</label>
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
                            <label className="block text-lg font-medium text-text">Mobile Number</label>
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
                            <label className="block text-lg font-medium text-text">Email</label>
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
                            <label className="block text-lg font-medium text-text">Display currency</label>
                            <div className="relative">
                                <select
                                    name="default_currency"
                                    value={formData.default_currency}
                                    onChange={handleChange}
                                    className="w-full p-5 text-lg bg-transparent border-b-2 border-strokes focus:outline-none focus:border-blue transition-colors appearance-none"
                                >
                                    {currencies.map((currency) => (
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
                            <label className="block text-lg font-medium text-text">New Password</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-5 flex items-center pointer-events-none">
                                    <svg className="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                                        />
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
                                    onClick={(e) => togglePasswordVisibility(e.currentTarget.closest(".relative"))}
                                >
                                    <svg className="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                                        />
                                    </svg>
                                </button>
                            </div>
                        </div>

                        {/* Confirm Password */}
                        <div className="space-y-3">
                            <label className="block text-lg font-medium text-text">Confirm Password</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-5 flex items-center pointer-events-none">
                                    <svg className="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                                        />
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
                                    onClick={(e) => togglePasswordVisibility(e.currentTarget.closest(".relative"))}
                                >
                                    <svg className="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                                        />
                                    </svg>
                                </button>
                            </div>

                            {errors.confirmPassword && (
                                <p className="text-red-500 text-lg mt-2">{errors.confirmPassword}</p>
                            )}
                        </div>
                    </div>

                    {/* Bigger update button */}
                    <div className="pt-8">
                        <Button
                            type="submit"
                            variant="primary"
                            className="px-16 py-5 text-xl font-semibold rounded-xl"
                            disabled={isLoading}
                        >
                            {isLoading ? "Updating..." : "Update"}
                        </Button>
                    </div>
                </form>
            </div>

            {/* Right side - avatar EXTREME RIGHT */}
            <div className="lg:col-span-1 flex justify-end items-start pt-10 pr-0">
                <button
                    type="button"
                    onClick={openFilePicker}
                    className="w-64 h-64 rounded-full border-2 border-[#D9DDE7] bg-white flex flex-col items-center justify-center text-[#B8BCC9] hover:opacity-90 transition"
                    aria-label="Upload your photo"
                >
                    {avatarPreview ? (
                        <img
                            src={avatarPreview}
                            alt="Profile"
                            className="w-full h-full object-cover rounded-full"
                        />
                    ) : (
                        <>
                            {/* icon */}
                            <svg
                                className="w-20 h-20 mb-3"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                            >
                                <path
                                    strokeWidth="1.6"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    d="M3 16.5V6.75A2.25 2.25 0 0 1 5.25 4.5h13.5A2.25 2.25 0 0 1 21 6.75v10.5A2.25 2.25 0 0 1 18.75 19.5H7.5L3 16.5Z"
                                />
                                <path
                                    strokeWidth="1.6"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    d="M9 10.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3Z"
                                />
                                <path
                                    strokeWidth="1.6"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    d="M21 15l-5.2-5.2a1.2 1.2 0 0 0-1.7 0L7 16.9"
                                />
                                {/* plus */}
                                <path
                                    strokeWidth="1.6"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    d="M17.5 7.5v-2m-1 1h2"
                                />
                            </svg>

                            {/* text inside circle like design */}
                            <div className="text-center font-semibold text-2xl leading-tight">
                                Upload your
                                <br />
                                photo
                            </div>
                        </>
                    )}
                </button>

                <input
                    ref={fileInputRef}
                    type="file"
                    className="hidden"
                    onChange={handleAvatarChange}
                    accept="image/*"
                />
            </div>
        </div>
    );
};

export default AccountInformation;
