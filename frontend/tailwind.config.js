/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/**/*.{js,jsx,ts,tsx}",
        "./public/index.html"
    ],
    theme: {
        extend: {
            colors: {
                'blue': '#3B82F6',
                'light-blue': '#93C5FD',
                'error': '#EF4444',
                'metallic-gray': '#6B7280',
                'pastel': '#E5E7EB',
                'black': '#000000',
                'background': '#F8FAFC',
                'boxes': '#FFFFFF',
                'green': '#10B981',
                'error2': '#DC2626',
                'text': '#1F2937',
                'placeholder': '#9CA3AF',
                'white': '#FFFFFF',
                'soft-gray': '#F3F4F6',
                'strokes': '#E5E7EB',
                'stats': '#8B5CF6',
                'expense': '#EF4444',
                'income': '#10B981',
                'information': '#3B82F6',
                'mob-boxes': '#F9FAFB'
            },
            fontFamily: {
                'sans': ['Noto Sans', 'sans-serif'],
            },
        },
    },
    plugins: [],
}