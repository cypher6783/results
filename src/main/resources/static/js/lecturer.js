import { logout } from './auth.js';

export function renderLecturerDashboard(user) {
    const app = document.getElementById('app');
    app.innerHTML = `
        <nav class="navbar">
            <div class="nav-brand">Lecturer Portal</div>
            <div class="nav-links">
                <span>${user.fullName}</span>
                <button class="btn" id="changePasswordBtn">Change Password</button>
                <button class="btn" id="logoutBtn">Logout</button>
            </div>
        </nav>
        <div class="container">
            <h1>Score Entry</h1>
            <div class="card" style="margin-top: 2rem">
                <form id="scoreForm">
                    <div class="input-group">
                        <label>Student Matric No</label>
                        <input type="text" id="matricNo" placeholder="Enter Matric No" required>
                    </div>
                    <div class="input-group">
                        <label>Course Code</label>
                        <select id="courseSelect">
                            <option>Loading...</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label>Session</label>
                        <select id="sessionSelect">
                            <option>Loading...</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label>CA Score</label>
                        <input type="number" id="caScore" step="0.1" max="30" required>
                    </div>
                    <div class="input-group">
                        <label>Exam Score</label>
                        <input type="number" id="examScore" step="0.1" max="70" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Submit Score</button>
                </form>
            </div>

            <div class="card" style="margin-top: 2rem">
                <h3>Bulk Upload (CSV)</h3>
                <p>Format: MatricNo, CA, Exam</p>
                <form id="bulkForm">
                    <div class="input-group">
                        <input type="file" id="csvFile" accept=".csv" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Upload CSV</button>
                </form>
            </div>

            <!-- Password Change Modal -->
            <div id="passwordModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000;">
                <div class="card" style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); max-width: 400px; width: 90%;">
                    <h3>Change Password</h3>
                    <form id="passwordForm">
                        <div class="input-group">
                            <label>Old Password</label>
                            <input type="password" id="oldPassword" required>
                        </div>
                        <div class="input-group">
                            <label>New Password</label>
                            <input type="password" id="newPassword" required>
                        </div>
                        <div class="input-group">
                            <label>Confirm New Password</label>
                            <input type="password" id="confirmNewPassword" required>
                        </div>
                        <button type="submit" class="btn btn-primary">Change Password</button>
                        <button type="button" class="btn" id="cancelPasswordBtn">Cancel</button>
                    </form>
                </div>
            </div>
        </div>
    `;
    
    document.getElementById('logoutBtn').addEventListener('click', async () => {
        await logout();
        window.location.hash = '/';
        window.location.reload();
    });

    document.getElementById('scoreForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const matricNo = document.getElementById('matricNo').value;
        const courseId = document.getElementById('courseSelect').value;
        const sessionId = document.getElementById('sessionSelect').value;
        const caScore = parseFloat(document.getElementById('caScore').value);
        const examScore = parseFloat(document.getElementById('examScore').value);

        if (!courseId || !sessionId) {
            alert("Please select Course and Session");
            return;
        }

        try {
            const response = await fetch('/api/lecturer/scores', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    matricNo: matricNo,
                    courseId: courseId,
                    sessionId: sessionId,
                    caScore: caScore,
                    examScore: examScore
                })
            });

            if (response.ok) {
                const message = await response.text();
                alert(message);
                e.target.reset();
            } else {
                const error = await response.text();
                alert('Failed to submit score: ' + error);
            }
        } catch (err) {
            console.error(err);
            alert('Error submitting score: ' + err.message);
        }
    });

    document.getElementById('bulkForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const file = document.getElementById('csvFile').files[0];
        const courseId = document.getElementById('courseSelect').value;
        const sessionId = document.getElementById('sessionSelect').value;
        
        if (!courseId || !sessionId) {
            alert("Please select Course and Session");
            return;
        }

        const formData = new FormData();
        formData.append('file', file);
        formData.append('courseId', courseId);
        formData.append('sessionId', sessionId);

        try {
            const response = await fetch('/api/lecturer/scores/bulk', {
                method: 'POST',
                body: formData
            });
            
            if (response.ok) {
                const text = await response.text();
                alert(text);
            } else {
                alert("Upload failed");
            }
        } catch (e) {
            console.error(e);
            alert("Error uploading file");
        }
    });
    
    document.getElementById('changePasswordBtn').addEventListener('click', () => {
        document.getElementById('passwordModal').style.display = 'block';
    });

    document.getElementById('cancelPasswordBtn').addEventListener('click', () => {
        document.getElementById('passwordModal').style.display = 'none';
        document.getElementById('passwordForm').reset();
    });

    document.getElementById('passwordForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const oldPassword = document.getElementById('oldPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmNewPassword = document.getElementById('confirmNewPassword').value;

        // Client-side validation
        if (newPassword !== confirmNewPassword) {
            alert('New password and confirmation do not match');
            return;
        }

        try {
            const response = await fetch('/api/user/change-password', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ oldPassword, newPassword, confirmNewPassword })
            });

            if (response.ok) {
                alert('Password changed successfully');
                document.getElementById('passwordModal').style.display = 'none';
                document.getElementById('passwordForm').reset();
            } else {
                const error = await response.text();
                alert('Failed to change password: ' + error);
            }
        } catch (err) {
            console.error(err);
            alert('Error changing password');
        }
    });
    
    loadData();
}

async function loadData() {
    try {
        const [coursesRes, sessionsRes] = await Promise.all([
            fetch('/api/lecturer/courses'),
            fetch('/api/sessions')
        ]);

        if (coursesRes.ok) {
            const courses = await coursesRes.json();
            document.getElementById('courseSelect').innerHTML = courses.map(c => `<option value="${c.id}">${c.code}</option>`).join('');
        }
        
        if (sessionsRes.ok) {
            const sessions = await sessionsRes.json();
            document.getElementById('sessionSelect').innerHTML = sessions.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
        }
    } catch (e) {
        console.error("Failed to load data", e);
    }
}
