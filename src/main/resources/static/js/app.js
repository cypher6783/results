import { login, logout, getCurrentUser } from './auth.js';
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
    '/student': renderStudentDashboard
};

async function router() {
    const path = window.location.hash.slice(1) || '/';
    const renderer = routes[path] || renderLogin;
    
    // Auth Check
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

function renderLogin() {
    app.innerHTML = `
        <div class="login-container">
            <div class="card login-card">
                <div class="login-header">
                    <h1>Result System</h1>
                    <p>Secure Academic Portal</p>
                </div>
                <form id="loginForm">
                    <div class="input-group">
                        <label>Username</label>
                        <input type="text" id="username" required>
                    </div>
                    <div class="input-group">
                        <label>Password</label>
                        <input type="password" id="password" required>
                    </div>
                    <button type="submit" class="btn btn-primary" style="width: 100%">Sign In</button>
                    <p id="errorMsg" style="color: var(--error-color); margin-top: 1rem; text-align: center; display: none;"></p>
                </form>
            </div>
        </div>
    `;

    document.getElementById('loginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = e.target.username.value;
        const password = e.target.password.value;
        const errorMsg = document.getElementById('errorMsg');
        
        try {
            await login(username, password);
            window.location.reload(); 
        } catch (err) {
            errorMsg.textContent = "Invalid credentials";
            errorMsg.style.display = 'block';
        }
    });
}

function renderDashboard(user) {
    // Fallback
    app.innerHTML = `<h1>Welcome ${user.fullName}</h1>`;
}
