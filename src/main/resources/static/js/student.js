import { logout } from './auth.js';

export function renderStudentDashboard(user) {
    const app = document.getElementById('app');
    app.innerHTML = `
        <nav class="navbar">
            <div class="nav-brand">Student Portal</div>
            <div class="nav-links">
                <span>${user.fullName}</span>
                <button class="btn" id="changePasswordBtn">Change Password</button>
                <button class="btn" id="logoutBtn">Logout</button>
            </div>
        </nav>
        <div class="container">
            <h1>My Results</h1>
            <div class="card" style="margin-top: 2rem">
                <div class="input-group">
                    <label>Select Session</label>
                    <select id="sessionSelect">
                        <option value="">Loading...</option>
                    </select>
                </div>
                <div class="input-group">
                    <label>Semester</label>
                    <select id="semesterSelect">
                        <option value="1">First Semester</option>
                        <option value="2">Second Semester</option>
                    </select>
                </div>
                <button class="btn btn-primary" id="viewResultBtn">View Result</button>
            </div>
            <div id="resultDisplay" style="margin-top: 2rem"></div>

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

    document.getElementById('viewResultBtn').addEventListener('click', () => {
        const sessionId = document.getElementById('sessionSelect').value;
        const semester = document.getElementById('semesterSelect').value;
        
        if (!sessionId) {
            alert("Please select a session");
            return;
        }

        // Direct PDF Preview
        const pdfUrl = `/api/student/result/pdf?sessionId=${sessionId}&semester=${semester}`;
        window.open(pdfUrl, '_blank');
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
    
    loadSessions();
}

async function loadSessions() {
    try {
        const response = await fetch('/api/sessions');
        if (response.ok) {
            const sessions = await response.json();
            const select = document.getElementById('sessionSelect');
            select.innerHTML = sessions.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
        }
    } catch (e) {
        console.error("Failed to load sessions", e);
    }
}


function renderResult(result) {
    const display = document.getElementById('resultDisplay');
    
    // Helper to format numbers
    const formatScore = (score, grade) => `${score.toFixed(0)}${grade}`;
    const formatPoint = (point) => point.toFixed(2);
    
    // Generate Course Rows
    const courseRows = result.courses.map(c => `
        <tr>
            <td>${c.code}</td>
            <td>${c.title}</td>
            <td>${c.unit}</td>
            <td>${formatScore(c.score, c.grade)}</td>
            <td>${formatPoint(c.pointEarned)}</td>
            <td>${c.remark}</td>
        </tr>
    `).join('');

    display.innerHTML = `
        <div class="result-slip">
            <div class="result-header">
                <div class="uni-name">JOSEPH SARWUAN TARKA UNIVERSITY</div>
                <div class="sub-header">P.M.B 2373, MAKURDI</div>
                <div class="sub-header">COLLEGE OF PHYSICAL SCIENCES</div>
                <div class="sub-header">DEPARTMENT OF COMPUTER SCIENCE</div>
                
                <div class="statement-title">
                    STATEMENT OF EXAMINATION RESULT FOR ${result.semester === 1 ? 'FIRST' : 'SECOND'} SEMESTER ${result.sessionName}
                </div>
            </div>

            <div style="margin-bottom: 0.5rem; font-weight: bold;">COURSE: ${result.course}</div>
            <div style="margin-bottom: 1rem; font-weight: bold;">LEVEL: ${result.level}</div>

            <div class="student-info-grid">
                <div class="info-cell info-label">REGISTRATION NUMBER:</div>
                <div class="info-cell" style="font-weight: bold;">${result.matricNo}</div>
                <div class="info-cell info-label">FULL NAME:</div>
                <div class="info-cell" style="font-weight: bold;">${result.fullName.toUpperCase()}</div>
            </div>

            <table class="result-table">
                <thead>
                    <tr>
                        <th style="width: 15%">Code</th>
                        <th style="width: 45%">Title</th>
                        <th style="width: 10%">Unit</th>
                        <th style="width: 10%">Score</th>
                        <th style="width: 10%">Point Earn</th>
                        <th style="width: 10%">Remark</th>
                    </tr>
                </thead>
                <tbody>
                    ${courseRows}
                </tbody>
            </table>

            <table class="perf-table">
                <thead>
                    <tr>
                        <th colspan="8" class="perf-header">PERFORMANCE</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="perf-label">TCC</td>
                        <td class="perf-value">${result.tcc}</td>
                        <td class="perf-label">TCE</td>
                        <td class="perf-value">${result.tce}</td>
                        <td class="perf-label">TPE</td>
                        <td class="perf-value">${result.tpe.toFixed(0)}</td>
                        <td class="perf-label">GPA</td>
                        <td class="perf-value">${result.gpa.toFixed(2)}</td>
                    </tr>
                    <tr>
                        <td class="perf-label">Previous TCC</td>
                        <td class="perf-value">${result.previousTcc !== null ? result.previousTcc : '-'}</td>
                        <td class="perf-label">Previous TCE</td>
                        <td class="perf-value">${result.previousTce !== null ? result.previousTce : '-'}</td>
                        <td class="perf-label">Previous TPE</td>
                        <td class="perf-value">${result.previousTpe !== null ? result.previousTpe.toFixed(0) : '-'}</td>
                        <td class="perf-label">Previous CGPA</td>
                        <td class="perf-value">${result.previousGpa !== null ? result.previousGpa.toFixed(2) : '-'}</td>
                    </tr>
                    <tr>
                        <td class="perf-label">CCC</td>
                        <td class="perf-value">${result.ccc}</td>
                        <td class="perf-label">CCE</td>
                        <td class="perf-value">${result.cce}</td>
                        <td class="perf-label">CPE</td>
                        <td class="perf-value">${result.cpe.toFixed(0)}</td>
                        <td class="perf-label">CGPA</td>
                        <td class="perf-value">${result.cgpa.toFixed(2)}</td>
                    </tr>
                </tbody>
            </table>

            <table class="key-table">
                <thead>
                    <tr>
                        <th colspan="4" class="key-header">KEY</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>TCC = Total Credit Carried</td>
                        <td>TCE = Total Credit Earned</td>
                        <td>TPE = Total Points Earned</td>
                        <td>GPA = Grade Points Average</td>
                    </tr>
                    <tr>
                        <td>CCC = Cummulative Credit Carried</td>
                        <td>CCE = Cummulative Credit Earned</td>
                        <td>CPE = Cummulative Points Earned</td>
                        <td>CGPA = Cummulative Grade Points Averag</td>
                    </tr>
                </tbody>
            </table>

            <div class="footer-section">
                <div></div> <!-- Spacer -->
                <div class="signature-box">
                    <div style="margin-bottom: 0.5rem;">(Signature/Stamp)</div>
                </div>
            </div>
            
            <div style="text-align: center; margin-top: 2rem; font-style: italic; font-size: 0.9rem;">
                B. SC. COMPUTER SCIENCE
            </div>
            
             <div style="text-align: right; font-size: 0.8rem; margin-top: 0.5rem;">
                Page 1/1
            </div>
        </div>
        
        <div style="text-align: center; margin-top: 1rem; margin-bottom: 2rem;">
            <button class="btn btn-primary" onclick="window.print()">Print Result</button>
            <button class="btn" id="downloadPdfBtn" style="margin-left: 1rem;">Download PDF</button>
        </div>
    `;

    // Add event listener for PDF download
    // We need to re-attach this every time renderResult is called, or just attach it once if the button persists.
    // Since the button is part of the innerHTML set in renderResult, we must attach it here.
    document.getElementById('downloadPdfBtn').addEventListener('click', async () => {
        try {
             const filename = `result_${result.matricNo.replace(/\//g, '-')}_${result.sessionName.replace(/\//g, '-')}_sem${result.semester}.pdf`;
             
             const sessionId = document.getElementById('sessionSelect').value;
             const response = await fetch(`/api/student/result/pdf?sessionId=${sessionId}&semester=${result.semester}`);
             
             if (response.ok) {
                 const blob = await response.blob();
                 const url = window.URL.createObjectURL(blob);
                 const a = document.createElement('a');
                 a.href = url;
                 a.download = filename;
                 document.body.appendChild(a);
                 a.click();
                 window.URL.revokeObjectURL(url);
                 a.remove();
             } else {
                 alert('Failed to download PDF');
             }
        } catch (e) {
            console.error('Error downloading PDF:', e);
            alert('Error downloading PDF');
        }
    });
}
