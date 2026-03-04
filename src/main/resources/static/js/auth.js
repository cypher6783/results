export async function login(username, password) {
    const formData = new FormData();
    formData.append('username', username);
    formData.append('password', password);

    const response = await fetch('/api/auth/login', {
        method: 'POST',
        body: formData
    });

    if (!response.ok) {
        throw new Error('Login failed');
    }

    // Attempt to parse JSON response. 
    // If it's empty (like for students), return nothing (success without MFA).
    const text = await response.text();
    if (text) {
        return JSON.parse(text);
    }
    return null;
}

export async function verifyMfa(code) {
    const formData = new FormData();
    formData.append('code', code);
    
    const response = await fetch('/api/auth/verify-mfa', {
        method: 'POST',
        body: formData
    });
    
    if (!response.ok) {
        const errorMsg = await response.text();
        throw new Error(errorMsg || 'MFA Verification failed');
    }
}

export async function forgotPassword(email) {
    const formData = new FormData();
    formData.append('email', email);
    
    const response = await fetch('/api/auth/forgot-password', {
        method: 'POST',
        body: formData
    });
    
    if (!response.ok) {
        throw new Error('Failed to request password reset');
    }
}

export async function resetPassword(token, newPassword) {
    const formData = new FormData();
    formData.append('token', token);
    formData.append('newPassword', newPassword);
    
    const response = await fetch('/api/auth/reset-password', {
        method: 'POST',
        body: formData
    });
    
    if (!response.ok) {
        const errorMsg = await response.text();
        throw new Error(errorMsg || 'Failed to reset password');
    }
}

export async function logout() {
    await fetch('/api/auth/logout', { method: 'POST' });
}

export async function getCurrentUser() {
    try {
        const response = await fetch('/api/auth/me');
        if (response.ok) {
            return await response.json();
        }
        return null;
    } catch (e) {
        return null;
    }
}
