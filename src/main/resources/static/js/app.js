import { login, logout, getCurrentUser, verifyMfa, forgotPassword, resetPassword } from './auth.js';
import { renderAdminDashboard } from './admin.js';
import { renderLecturerDashboard } from './lecturer.js';
import { renderStudentDashboard } from './student.js';

const app = document.getElementById('app');

// Simple Router
const routes = {
    '/': renderLogin,
    '/dashboard': renderDashboard,
    '/admin': renderAdminDashboard,
    '/lecturer': renderLecturerDashboard,
    '/student': renderStudentDashboard,
    '/forgot-password': renderForgotPassword,
    '/reset-password': renderResetPassword
};

async function router() {
    let path = window.location.hash.slice(1) || '/';
    // Handle query params in routing (like reset token)
    if (path.includes('?')) {
        path = path.split('?')[0];
    }
    
    const renderer = routes[path] || renderLogin;
    
    // Auth Check (Except For Public Routes)
    if (path === '/forgot-password' || path === '/reset-password') {
        renderer();
        return;
    }

    const user = await getCurrentUser();
    if (!user && path !== '/') {
        window.location.hash = '/';
        return;
    }
    if (user && path === '/') {
        // Redirect based on role
        if (user.role === 'ADMIN') window.location.hash = '/admin';
        else if (user.role === 'LECTURER') window.location.hash = '/lecturer';
        else window.location.hash = '/student';
        return;
    }

    if (user) {
        renderer(user);
    } else {
        renderLogin();
    }
}

window.addEventListener('hashchange', router);
window.addEventListener('load', router);

// Service Worker Registration
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/service-worker.js')
            .then(reg => console.log('Service Worker registered', reg))
            .catch(err => console.log('Service Worker NOT registered', err));
    });
}

function renderLogin() {
    app.innerHTML = `
        <div class="login-container">
            <div class="card login-card" id="loginCard">
                <div class="login-header">
                    <h1>Result System</h1>
                    <p>Secure Academic Portal</p>
                </div>
                <form id="loginForm">
                    <div class="input-group">
                        <label>Username</label>
                        <input type="text" id="username" required>
                    </div>
                    <div class="input-group password-group">
                        <label>Password</label>
                        <div style="position: relative;">
                            <input type="password" id="password" required style="width: 100%; padding-right: 40px;">
                            <button type="button" class="btn-toggle-password" onclick="togglePasswordVisibility('password')" style="position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: 1.2rem;">👁</button>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary" style="width: 100%">Sign In</button>
                    <div style="text-align: center; margin-top: 1rem;">
                        <a href="#/forgot-password" style="color: var(--accent-color); text-decoration: none; font-size: 0.9rem;">Forgot Password?</a>
                    </div>
                    <p id="errorMsg" style="color: var(--error-color); margin-top: 1rem; text-align: center; display: none;"></p>
                </form>
            </div>

            <!-- MFA Setup Card (Hidden by default) -->
            <div class="card login-card" id="mfaSetupCard" style="display: none;">
                <div class="login-header">
                    <h2>Setup MFA</h2>
                    <p>Enhance account security</p>
                </div>
                <div style="text-align: center; margin-bottom: 1rem;">
                    <p style="font-size: 0.9rem; margin-bottom: 1rem;">Scan this QR code with Google Authenticator or Microsoft Authenticator, then enter the 6-digit code below.</p>
                    <img id="mfaQrCode" src="" alt="MFA QR Code" style="max-width: 200px; border-radius: 8px; border: 4px solid white;">
                </div>
                <form id="mfaSetupForm">
                    <div class="input-group">
                        <label>Initial 6-Digit Code</label>
                        <input type="text" id="mfaSetupCode" required placeholder="123456" pattern="[0-9]{6}">
                    </div>
                    <button type="submit" class="btn btn-primary" style="width: 100%">Verify and Complete</button>
                    <p id="mfaSetupErrorMsg" style="color: var(--error-color); margin-top: 1rem; text-align: center; display: none;"></p>
                </form>
            </div>

            <!-- MFA Login Card (Hidden by default) -->
            <div class="card login-card" id="mfaLoginCard" style="display: none;">
                <div class="login-header">
                    <h2>Two-Factor Authentication</h2>
                    <p>Enter authenticator code</p>
                </div>
                <form id="mfaLoginForm">
                    <div class="input-group">
                        <label>6-Digit Code</label>
                        <input type="text" id="mfaLoginCode" required placeholder="123456" pattern="[0-9]{6}">
                    </div>
                    <button type="submit" class="btn btn-primary" style="width: 100%">Verify</button>
                    <p id="mfaLoginErrorMsg" style="color: var(--error-color); margin-top: 1rem; text-align: center; display: none;"></p>
                </form>
                <div style="text-align: center; margin-top: 1rem;">
                    <button class="btn" onclick="window.location.reload()" style="background: transparent;">Cancel</button>
                </div>
            </div>
        </div>
    `;

    document.getElementById('loginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = e.target.username.value;
        const password = e.target.password.value;
        const errorMsg = document.getElementById('errorMsg');
        
        try {
            const loginResult = await login(username, password);
            
            if (loginResult && loginResult.mfaSetupRequired) {
                document.getElementById('loginCard').style.display = 'none';
                document.getElementById('mfaSetupCard').style.display = 'block';
                document.getElementById('mfaQrCode').src = loginResult.qrCode;
            } else if (loginResult && loginResult.mfaRequired) {
                document.getElementById('loginCard').style.display = 'none';
                document.getElementById('mfaLoginCard').style.display = 'block';
            } else {
                // Direct login success
                window.location.reload(); 
            }
        } catch (err) {
            errorMsg.textContent = "Invalid credentials";
            errorMsg.style.display = 'block';
        }
    });

    document.getElementById('mfaSetupForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const code = e.target.mfaSetupCode.value;
        const errorMsg = document.getElementById('mfaSetupErrorMsg');
        try {
            await verifyMfa(code);
            window.location.reload();
        } catch (err) {
            errorMsg.textContent = err.message;
            errorMsg.style.display = 'block';
        }
    });

    document.getElementById('mfaLoginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const code = e.target.mfaLoginCode.value;
        const errorMsg = document.getElementById('mfaLoginErrorMsg');
        try {
            await verifyMfa(code);
            window.location.reload();
        } catch (err) {
            errorMsg.textContent = err.message;
            errorMsg.style.display = 'block';
        }
    });
}

function renderForgotPassword() {
    app.innerHTML = `
        <div class="login-container">
            <div class="card login-card">
                <div class="login-header">
                    <h2>Forgot Password</h2>
                    <p>Request a password reset link</p>
                </div>
                <form id="forgotPasswordForm">
                    <div class="input-group">
                        <label>Account Email</label>
                        <input type="email" id="resetEmail" required placeholder="user@university.edu">
                    </div>
                    <button type="submit" class="btn btn-primary" style="width: 100%">Send Reset Link</button>
                    <p id="forgotErrorMsg" style="color: var(--error-color); margin-top: 1rem; text-align: center; display: none;"></p>
                    <p id="forgotSuccessMsg" style="color: var(--success-color); margin-top: 1rem; text-align: center; display: none;"></p>
                </form>
                <div style="text-align: center; margin-top: 1rem;">
                    <a href="#/" style="color: var(--text-secondary); text-decoration: none; font-size: 0.9rem;">Back to Login</a>
                </div>
            </div>
        </div>
    `;

    document.getElementById('forgotPasswordForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = e.target.resetEmail.value;
        const errorMsg = document.getElementById('forgotErrorMsg');
        const successMsg = document.getElementById('forgotSuccessMsg');
        
        errorMsg.style.display = 'none';
        successMsg.style.display = 'none';
        
        try {
            await forgotPassword(email);
            successMsg.textContent = "If an account exists, a link has been sent to your email (check console output).";
            successMsg.style.display = 'block';
            e.target.reset();
        } catch(err) {
            errorMsg.textContent = "Failed to send reset request.";
            errorMsg.style.display = 'block';
        }
    });
}

function renderResetPassword() {
    // Extract token from URL hash, e.g. #/reset-password?token=1234
    const urlParams = new URLSearchParams(window.location.hash.split('?')[1]);
    const token = urlParams.get('token');

    if (!token) {
        app.innerHTML = `
            <div class="login-container">
                <div class="card login-card" style="text-align: center;">
                    <h2>Invalid Link</h2>
                    <p>The password reset link is invalid or missing the token.</p>
                    <a href="#/" class="btn btn-primary" style="margin-top: 1rem;">Back to Login</a>
                </div>
            </div>
        `;
        return;
    }

    app.innerHTML = `
        <div class="login-container">
            <div class="card login-card">
                <div class="login-header">
                    <h2>Create New Password</h2>
                    <p>Enter your new password below</p>
                </div>
                <form id="resetPasswordForm">
                    <div class="input-group password-group">
                        <label>New Password</label>
                        <div style="position: relative;">
                            <input type="password" id="newPassword" required style="width: 100%; padding-right: 40px;">
                            <button type="button" class="btn-toggle-password" onclick="togglePasswordVisibility('newPassword')" style="position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: 1.2rem;">👁</button>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary" style="width: 100%">Reset Password</button>
                    <p id="resetErrorMsg" style="color: var(--error-color); margin-top: 1rem; text-align: center; display: none;"></p>
                </form>
                <div style="text-align: center; margin-top: 1rem;">
                    <a href="#/" style="color: var(--text-secondary); text-decoration: none; font-size: 0.9rem;">Cancel</a>
                </div>
            </div>
        </div>
    `;

    document.getElementById('resetPasswordForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const newPassword = e.target.newPassword.value;
        const errorMsg = document.getElementById('resetErrorMsg');
        errorMsg.style.display = 'none';

        try {
            await resetPassword(token, newPassword);
            alert("Password successfully reset! You will now be redirected to login.");
            window.location.hash = '/';
        } catch (err) {
            errorMsg.textContent = err.message;
            errorMsg.style.display = 'block';
        }
    });
}

function renderDashboard(user) {
    // Fallback
    app.innerHTML = `<h1>Welcome ${user.fullName}</h1>`;
}

// Global utility for toggling password visibility
window.togglePasswordVisibility = function(inputId) {
    const input = document.getElementById(inputId);
    if (input) {
        if (input.type === 'password') {
            input.type = 'text';
        } else {
            input.type = 'password';
        }
    }
}
