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
