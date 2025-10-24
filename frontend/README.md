# Equilibrio - Personal Finance Tracker (Frontend)

## 🚀 Features Implemented
### ✅ Authentication Pages
- Login Page - Secure user authentication
- Sign Up Page - User registration with password confirmation
- Form Validation - Real-time validation for all form fields
- Password Confirmation - Ensures password matching during registration

### 🛠 Technology Stack
- Frontend Framework: React 18
- Styling: Tailwind CSS
- Routing: React Router DOM
- Icons: Custom design assets
- Font: Noto Sans (Google Fonts)
- Build Tool: Create React App

## 📁 Project Structure

```
frontend/
├── public/
│   ├── index.html
│   └── images/ (optional location for images)
├── src/
│   ├── assets/
│   │   └── images/
│   │       ├── logo.png
│   │       └── clock-image.png
│   ├── components/
│   │   ├── auth/
│   │   │   ├── Login.js
│   │   │   └── SignUp.js
│   │   └── ui/
│   │       └── Button.js
│   ├── pages/
│   │   ├── LoginPage.js
│   │   └── SignUpPage.js
│   ├── App.js
│   ├── index.js
│   └── index.css
├── tailwind.config.js
├── package.json
└── README.md
```

## 🚀 Getting Started
### Prerequisites
- Node.js (version 14 or higher)
- npm or yarn

### Installation & Setup
1. Clone the repository
   ```bash
   git clone https://github.com/Yerson-Borda/Equilibrio.git
   cd frontend
   ```
2. Install dependencies
   ```bash
   npm install
   ```
3. Start the development server
   ```bash
   npm start
   ```
4. Open your browser
   ```bash
   Navigate to http://localhost:3000
   ```


### Available Scripts
- npm start - Runs the app in development mode
- npm build - Builds the app for production
- npm test - Launches the test runner
- npm eject - Ejects from Create React App (one-way operation)
