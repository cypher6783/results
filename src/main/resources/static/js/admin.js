import { logout } from "./auth.js";

export function renderAdminDashboard(user) {
  const app = document.getElementById("app");
  app.innerHTML = `
        <nav class="navbar">
            <div class="nav-brand">Admin Portal</div>
            <div class="nav-links">
                <span>${user.fullName}</span>
                <button class="btn" id="logoutBtn">Logout</button>
            </div>
        </nav>
        <div class="container">
            <h1>Admin Dashboard</h1>
            <div class="dashboard-grid">
                <div class="card stat-card">
                    <h3>Manage Students</h3>
                    <p>Add and view students</p>
                    <button class="btn btn-primary" style="margin-top: 1rem" id="addStudentBtn">Add Student</button>
                    <button class="btn" style="margin-top: 0.5rem" id="bulkUploadStudentsBtn">Bulk Upload CSV</button>
                    <button class="btn" style="margin-top: 0.5rem" id="viewStudentsBtn">View Students</button>
                </div>
                <div class="card stat-card">
                    <h3>Manage Lecturers</h3>
                    <p>Add and view lecturers</p>
                    <button class="btn btn-primary" style="margin-top: 1rem" id="addLecturerBtn">Add Lecturer</button>
                    <button class="btn" style="margin-top: 0.5rem" id="viewLecturersBtn">View Lecturers</button>
                </div>
                <div class="card stat-card">
                    <h3>Manage Courses</h3>
                    <p>Add and view courses</p>
                    <button class="btn btn-primary" style="margin-top: 1rem" id="addCourseBtn">Add Course</button>
                    <button class="btn" style="margin-top: 0.5rem" id="bulkUploadCoursesBtn">Bulk Upload CSV</button>
                    <button class="btn" style="margin-top: 0.5rem" id="viewCoursesBtn">View Courses</button>
                </div>
                <div class="card stat-card">
                    <h3>Admin Tools</h3>
                    <p>Password reset and bulk operations</p>
                    <button class="btn btn-primary" style="margin-top: 1rem" id="changePasswordBtn">Change User Password</button>
                    <button class="btn" style="margin-top: 0.5rem" id="bulkResultsBtn">Generate Bulk Results</button>
                    <button class="btn" style="margin-top: 0.5rem" id="changeOwnPasswordBtn">Change My Password</button>
                </div>
            </div>

            <!-- Add Student Form -->
            <div id="addStudentForm" class="card" style="margin-top: 2rem; display: none;">
                <h3>Add New Student</h3>
                <form id="studentForm">
                    <div class="input-group">
                        <label>Full Name</label>
                        <input type="text" name="fullName" required>
                    </div>
                    <div class="input-group">
                        <label>Matric Number</label>
                        <input type="text" name="matricNo" placeholder="e.g., 2024/001" required>
                    </div>
                    <div class="input-group">
                        <label>Level</label>
                        <input type="text" name="level" placeholder="e.g., 100, 200" required>
                    </div>
                    <div class="input-group">
                        <label>Department</label>
                        <input type="text" name="department" required>
                    </div>
                    <p style="color: #666; font-size: 0.9rem; margin: 1rem 0;">
                        Default password will be the matric number. Student must change password on first login.
                    </p>
                    <button type="submit" class="btn btn-primary">Add Student</button>
                    <button type="button" class="btn" id="cancelStudentBtn">Cancel</button>
                </form>
            </div>

            <!-- Add Lecturer Form -->
            <div id="addLecturerForm" class="card" style="margin-top: 2rem; display: none;">
                <h3>Add New Lecturer</h3>
                <form id="lecturerForm">
                    <div class="input-group">
                        <label>Full Name</label>
                        <input type="text" name="fullName" required>
                    </div>
                    <div class="input-group">
                        <label>Staff ID</label>
                        <input type="text" name="staffId" placeholder="e.g., STAFF001" required>
                    </div>
                    <p style="color: #666; font-size: 0.9rem; margin: 1rem 0;">
                        Default password will be the staff ID. Lecturer must change password on first login.
                    </p>
                    <button type="submit" class="btn btn-primary">Add Lecturer</button>
                    <button type="button" class="btn" id="cancelLecturerBtn">Cancel</button>
                </form>
            </div>

            <!-- Add Course Form -->
            <div id="addCourseForm" class="card" style="margin-top: 2rem; display: none;">
                <h3>Create New Course</h3>
                <form id="courseForm">
                    <div class="input-group">
                        <label>Course Code</label>
                        <input type="text" name="code" placeholder="CSC101" required>
                    </div>
                    <div class="input-group">
                        <label>Title</label>
                        <input type="text" name="title" placeholder="Introduction to CS" required>
                    </div>
                    <div class="input-group">
                        <label>Units</label>
                        <input type="number" name="units" required>
                    </div>
                    <div class="input-group">
                        <label>Semester</label>
                        <select name="semester">
                            <option value="1">First</option>
                            <option value="2">Second</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label>Level</label>
                        <input type="number" name="level" placeholder="100" required>
                    </div>
                    <div class="input-group">
                        <label>Department</label>
                        <input type="text" name="department" required>
                    </div>
                    <div class="input-group">
                        <label>Lecturer</label>
                        <select name="lecturerId" id="lecturerSelect" required>
                            <option value="">Loading lecturers...</option>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary">Create Course</button>
                    <button type="button" class="btn" id="cancelCourseBtn">Cancel</button>
                </form>
            </div>

            <!-- Bulk Upload Students Form -->
            <div id="bulkUploadStudentsForm" class="card" style="margin-top: 2rem; display: none;">
                <h3>Bulk Upload Students (CSV)</h3>
                <p style="color: #666; margin-bottom: 1rem;">
                    CSV Format: <code>FullName,MatricNo,Level,Department</code><br>
                    Example: <code>John Doe,2024/001,100,Computer Science</code>
                </p>
                <form id="bulkStudentsForm">
                    <div class="input-group">
                        <label>Select CSV File</label>
                        <input type="file" id="studentsFile" accept=".csv" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Upload Students</button>
                    <button type="button" class="btn" id="cancelBulkStudentsBtn">Cancel</button>
                </form>
            </div>

            <!-- Bulk Upload Courses Form -->
            <div id="bulkUploadCoursesForm" class="card" style="margin-top: 2rem; display: none;">
                <h3>Bulk Upload Courses (CSV)</h3>
                <p style="color: #666; margin-bottom: 1rem;">
                    CSV Format: <code>Code,Title,Units,Semester,Level,Department</code><br>
                    Example: <code>CSC101,Introduction to Computing,3,1,100,Computer Science</code>
                </p>
                <form id="bulkCoursesForm">
                    <div class="input-group">
                        <label>Select CSV File</label>
                        <input type="file" id="coursesFile" accept=".csv" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Upload Courses</button>
                    <button type="button" class="btn" id="cancelBulkCoursesBtn">Cancel</button>
                </form>
            </div>

            <!-- View Lists -->
            <div id="viewStudentsList" class="card" style="margin-top: 2rem; display: none;">
                <h3>All Students</h3>
                <div id="studentsTable"></div>
            </div>

            <div id="viewLecturersList" class="card" style="margin-top: 2rem; display: none;">
                <h3>All Lecturers</h3>
                <div id="lecturersTable"></div>
            </div>

            <div id="viewCoursesList" class="card" style="margin-top: 2rem; display: none;">
                <h3>All Courses</h3>
                <div id="coursesTable"></div>
            </div>

            <!-- Admin Password Change Form -->
            <div id="changePasswordForm" class="card" style="margin-top: 2rem; display: none;">
                <h3>Change User Password</h3>
                <form id="adminPasswordForm">
                    <div class="input-group">
                        <label>Username (Matric No or Staff ID)</label>
                        <input type="text" name="username" placeholder="e.g., 2024/001 or STAFF001" required>
                    </div>
                    <div class="input-group">
                        <label>New Password</label>
                        <input type="password" name="newPassword" required>
                    </div>
                    <p style="color: #666; font-size: 0.9rem; margin: 1rem 0;">
                        User will be forced to change password on next login.
                    </p>
                    <button type="submit" class="btn btn-primary">Change Password</button>
                    <button type="button" class="btn" id="cancelPasswordBtn">Cancel</button>
                </form>
            </div>

            <!-- Bulk Results Generation Form -->
            <div id="bulkResultsForm" class="card" style="margin-top: 2rem; display: none;">
                <h3>Generate Bulk Results</h3>
                <p style="color: #666; margin-bottom: 1rem;">
                    This will process results for all students in the selected session and semester.
                </p>
                <form id="generateResultsForm">
                    <div class="input-group">
                        <label>Academic Session</label>
                        <select name="sessionId" id="sessionSelect" required>
                            <option value="">Loading sessions...</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label>Semester</label>
                        <select name="semester" required>
                            <option value="1">First Semester</option>
                            <option value="2">Second Semester</option>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary">Generate Results</button>
                    <button type="submit" class="btn btn-primary">Generate Results</button>
                    <button type="button" class="btn" id="downloadBatchPdfBtn" style="display: none; margin-left: 0.5rem;">Download Batch PDF</button>
                    <button type="button" class="btn" id="cancelResultsBtn">Cancel</button>
                </form>
                <div id="resultsOutput" style="margin-top: 1rem; display: none;"></div>
            </div>

            <!-- Admin Self Password Change Form -->
            <div id="changeOwnPasswordForm" class="card" style="margin-top: 2rem; display: none;">
                <h3>Change My Password</h3>
                <form id="adminSelfPasswordForm">
                    <div class="input-group">
                        <label>Current Password</label>
                        <input type="password" name="oldPassword" required>
                    </div>
                    <div class="input-group">
                        <label>New Password</label>
                        <input type="password" name="newPassword" required>
                    </div>
                    <div class="input-group">
                        <label>Confirm New Password</label>
                        <input type="password" name="confirmNewPassword" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Change Password</button>
                    <button type="button" class="btn" id="cancelOwnPasswordBtn">Cancel</button>
                </form>
            </div>
        </div>
    `;

  // Event Listeners
  document.getElementById("logoutBtn").addEventListener("click", async () => {
    await logout();
    window.location.hash = "/";
    window.location.reload();
  });

  // Show/Hide Forms
  document.getElementById("addStudentBtn").addEventListener("click", () => {
    hideAllSections();
    const form = document.getElementById("addStudentForm");
    form.style.display = "block";
    smoothScrollTo(form);
  });

  document.getElementById("addLecturerBtn").addEventListener("click", () => {
    hideAllSections();
    const form = document.getElementById("addLecturerForm");
    form.style.display = "block";
    smoothScrollTo(form);
  });

  document.getElementById("addCourseBtn").addEventListener("click", async () => {
    hideAllSections();
    await populateLecturerDropdown();
    const form = document.getElementById("addCourseForm");
    form.style.display = "block";
    smoothScrollTo(form);
  });

  document.getElementById("viewStudentsBtn").addEventListener("click", async () => {
    hideAllSections();
    await loadStudents();
  });

  document.getElementById("bulkUploadStudentsBtn").addEventListener("click", () => {
    hideAllSections();
    const form = document.getElementById("bulkUploadStudentsForm");
    form.style.display = "block";
    smoothScrollTo(form);
  });

  document.getElementById("bulkUploadCoursesBtn").addEventListener("click", () => {
    hideAllSections();
    const form = document.getElementById("bulkUploadCoursesForm");
    form.style.display = "block";
    smoothScrollTo(form);
  });

  document.getElementById("viewLecturersBtn").addEventListener("click", async () => {
    hideAllSections();
    await loadLecturers();
  });

  document.getElementById("viewCoursesBtn").addEventListener("click", async () => {
    hideAllSections();
    await loadCourses();
  });

  document.getElementById("changePasswordBtn").addEventListener("click", () => {
    hideAllSections();
    const form = document.getElementById("changePasswordForm");
    form.style.display = "block";
    smoothScrollTo(form);
  });

  document.getElementById("bulkResultsBtn").addEventListener("click", async () => {
    hideAllSections();
    await loadSessions();
    const form = document.getElementById("bulkResultsForm");
    form.style.display = "block";
    smoothScrollTo(form);
  });

  document.getElementById("changeOwnPasswordBtn").addEventListener("click", () => {
    hideAllSections();
    const form = document.getElementById("changeOwnPasswordForm");
    form.style.display = "block";
    smoothScrollTo(form);
  });

  // Cancel Buttons
  document.getElementById("cancelStudentBtn").addEventListener("click", () => {
    document.getElementById("addStudentForm").style.display = "none";
  });

  document.getElementById("cancelLecturerBtn").addEventListener("click", () => {
    document.getElementById("addLecturerForm").style.display = "none";
  });

  document.getElementById("cancelCourseBtn").addEventListener("click", () => {
    document.getElementById("addCourseForm").style.display = "none";
  });

  document.getElementById("cancelBulkStudentsBtn").addEventListener("click", () => {
    document.getElementById("bulkUploadStudentsForm").style.display = "none";
  });

  document.getElementById("cancelBulkCoursesBtn").addEventListener("click", () => {
    document.getElementById("bulkUploadCoursesForm").style.display = "none";
  });

  document.getElementById("cancelPasswordBtn").addEventListener("click", () => {
    document.getElementById("changePasswordForm").style.display = "none";
  });

  document.getElementById("cancelResultsBtn").addEventListener("click", () => {
    document.getElementById("bulkResultsForm").style.display = "none";
  });

  document.getElementById("cancelOwnPasswordBtn").addEventListener("click", () => {
    document.getElementById("changeOwnPasswordForm").style.display = "none";
  });

  // Form Submissions
  document
    .getElementById("studentForm")
    .addEventListener("submit", async (e) => {
      e.preventDefault();
      const formData = new FormData(e.target);
      const data = Object.fromEntries(formData.entries());

      try {
        const response = await fetch("/api/admin/students", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(data),
        });

        if (response.ok) {
          alert("Student added successfully");
          e.target.reset();
          document.getElementById("addStudentForm").style.display = "none";
        } else {
          const error = await response.text();
          alert("Failed to add student: " + error);
        }
      } catch (err) {
        console.error(err);
        alert("Error adding student");
      }
    });

  document
    .getElementById("lecturerForm")
    .addEventListener("submit", async (e) => {
      e.preventDefault();
      const formData = new FormData(e.target);
      const data = Object.fromEntries(formData.entries());

      try {
        const response = await fetch("/api/admin/lecturers", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(data),
        });

        if (response.ok) {
          alert("Lecturer added successfully");
          e.target.reset();
          document.getElementById("addLecturerForm").style.display = "none";
        } else {
          const error = await response.text();
          alert("Failed to add lecturer: " + error);
        }
      } catch (err) {
        console.error(err);
        alert("Error adding lecturer");
      }
    });

  document
    .getElementById("courseForm")
    .addEventListener("submit", async (e) => {
      e.preventDefault();
      const formData = new FormData(e.target);
      const data = Object.fromEntries(formData.entries());
      data.units = parseInt(data.units);
      data.semester = parseInt(data.semester);
      data.level = parseInt(data.level);
      
      // Only include lecturerId if one is selected
      if (data.lecturerId && data.lecturerId !== "") {
        // lecturerId is already a string UUID from the select value
      } else {
        delete data.lecturerId;
      }

      try {
        const response = await fetch("/api/admin/courses", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(data),
        });

        if (response.ok) {
          alert("Course created successfully");
          e.target.reset();
          document.getElementById("addCourseForm").style.display = "none";
        } else {
          const errorText = await response.text();
          alert("Failed to create course: " + errorText);
        }
      } catch (err) {
        console.error(err);
        alert("Error creating course: " + err.message);
      }
    });

  // Bulk Upload Forms
  document.getElementById("bulkStudentsForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fileInput = document.getElementById("studentsFile");
    const file = fileInput.files[0];

    if (!file) {
      alert("Please select a CSV file");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch("/api/admin/students/bulk", {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        const message = await response.text();
        alert(message);
        e.target.reset();
        document.getElementById("bulkUploadStudentsForm").style.display = "none";
      } else {
        const error = await response.text();
        alert("Failed to upload students: " + error);
      }
    } catch (err) {
      console.error(err);
      alert("Error uploading students");
    }
  });

  document.getElementById("bulkCoursesForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fileInput = document.getElementById("coursesFile");
    const file = fileInput.files[0];

    if (!file) {
      alert("Please select a CSV file");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch("/api/admin/courses/bulk", {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        const message = await response.text();
        alert(message);
        e.target.reset();
        document.getElementById("bulkUploadCoursesForm").style.display = "none";
      } else {
        const error = await response.text();
        alert("Failed to upload courses: " + error);
      }
    } catch (err) {
      console.error(err);
      alert("Error uploading courses");
    }
  });

  // Admin Password Change Form
  document.getElementById("adminPasswordForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData.entries());

    try {
      const response = await fetch("/api/admin/change-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      if (response.ok) {
        const message = await response.text();
        alert(message);
        e.target.reset();
        document.getElementById("changePasswordForm").style.display = "none";
      } else {
        const error = await response.text();
        alert("Failed to change password: " + error);
      }
    } catch (err) {
      console.error(err);
      alert("Error changing password");
    }
  });

  // Bulk Results Generation Form
  document.getElementById("generateResultsForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const sessionId = formData.get("sessionId");
    const semester = formData.get("semester");

    try {
      const response = await fetch(`/api/admin/bulk-results?sessionId=${sessionId}&semester=${semester}`, {
        method: "POST",
      });

      if (response.ok) {
        const result = await response.json();
        displayResultsSummary(result);
        
        // Show download button
        const downloadBtn = document.getElementById('downloadBatchPdfBtn');
        downloadBtn.style.display = 'inline-block';
        downloadBtn.onclick = async () => {
             try {
                 const filename = `batch_results_${result.sessionName.replace(/\//g, '-')}_sem${result.semester}.pdf`;
                 const pdfResponse = await fetch(`/api/admin/bulk-results/pdf?sessionId=${sessionId}&semester=${semester}`);
                 
                 if (pdfResponse.ok) {
                     const blob = await pdfResponse.blob();
                     const url = window.URL.createObjectURL(blob);
                     const a = document.createElement('a');
                     a.href = url;
                     a.download = filename;
                     document.body.appendChild(a);
                     a.click();
                     window.URL.revokeObjectURL(url);
                     a.remove();
                 } else {
                     alert('Failed to download Batch PDF');
                 }
             } catch (e) {
                 console.error('Error downloading Batch PDF:', e);
                 alert('Error downloading Batch PDF');
             }
        };
      } else {
        const error = await response.text();
        alert("Failed to generate results: " + error);
      }
    } catch (err) {
      console.error(err);
      alert("Error generating results");
    }
  });

  // Admin Self Password Change Form
  document.getElementById("adminSelfPasswordForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData.entries());

    // Validate password confirmation
    if (data.newPassword !== data.confirmNewPassword) {
      alert("New password and confirmation do not match");
      return;
    }

    try {
      const response = await fetch("/api/user/change-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      if (response.ok) {
        const message = await response.text();
        alert(message);
        e.target.reset();
        document.getElementById("changeOwnPasswordForm").style.display = "none";
      } else {
        const error = await response.text();
        alert("Failed to change password: " + error);
      }
    } catch (err) {
      console.error(err);
      alert("Error changing password");
    }
  });
}

function hideAllSections() {
  document.getElementById("addStudentForm").style.display = "none";
  document.getElementById("addLecturerForm").style.display = "none";
  document.getElementById("addCourseForm").style.display = "none";
  document.getElementById("bulkUploadStudentsForm").style.display = "none";
  document.getElementById("bulkUploadCoursesForm").style.display = "none";
  document.getElementById("viewStudentsList").style.display = "none";
  document.getElementById("viewLecturersList").style.display = "none";
  document.getElementById("viewCoursesList").style.display = "none";
  document.getElementById("changePasswordForm").style.display = "none";
  document.getElementById("bulkResultsForm").style.display = "none";
  document.getElementById("changeOwnPasswordForm").style.display = "none";
}

async function loadStudents() {
  try {
    const response = await fetch("/api/admin/students");
    const students = await response.json();

    const table = `
            <table style="width: 100%; border-collapse: collapse;">
                <thead>
                    <tr style="background: #f5f5f5;">
                        <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd;">Full Name</th>
                        <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd;">Username</th>
                        <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd;">Status</th>
                    </tr>
                </thead>
                <tbody>
                    ${students
                      .map(
                        (student) => `
                        <tr>
                            <td style="padding: 0.75rem; border-bottom: 1px solid #eee;">${
                              student.fullName
                            }</td>
                            <td style="padding: 0.75rem; border-bottom: 1px solid #eee;">${
                              student.username
                            }</td>
                            <td style="padding: 0.75rem; border-bottom: 1px solid #eee;">${
                              student.active ? "Active" : "Inactive"
                            }</td>
                        </tr>
                    `
                      )
                      .join("")}
                </tbody>
            </table>
        `;

    document.getElementById("studentsTable").innerHTML = table;
    const listElement = document.getElementById("viewStudentsList");
    listElement.style.display = "block";
    smoothScrollTo(listElement);
  } catch (err) {
    console.error(err);
    alert("Error loading students");
  }
}

async function loadLecturers() {
  try {
    const response = await fetch("/api/admin/lecturers");
    const lecturers = await response.json();

    const table = `
            <table style="width: 100%; border-collapse: collapse;">
                <thead>
                    <tr style="background: #f5f5f5;">
                        <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd;">Full Name</th>
                        <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd;">Username</th>
                        <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd;">Status</th>
                    </tr>
                </thead>
                <tbody>
                    ${lecturers
                      .map(
                        (lecturer) => `
                        <tr>
                            <td style="padding: 0.75rem; border-bottom: 1px solid #eee;">${
                              lecturer.fullName
                            }</td>
                            <td style="padding: 0.75rem; border-bottom: 1px solid #eee;">${
                              lecturer.username
                            }</td>
                            <td style="padding: 0.75rem; border-bottom: 1px solid #eee;">${
                              lecturer.active ? "Active" : "Inactive"
                            }</td>
                        </tr>
                    `
                      )
                      .join("")}
                </tbody>
            </table>
        `;

    document.getElementById("lecturersTable").innerHTML = table;
    const listElement = document.getElementById("viewLecturersList");
    listElement.style.display = "block";
    smoothScrollTo(listElement);
  } catch (err) {
    console.error(err);
    alert("Error loading lecturers");
  }
}

async function loadCourses() {
  try {
    const response = await fetch("/api/admin/courses");
    const courses = await response.json();

    const table = `
            <table style="width: 100%; border-collapse: collapse;">
                <thead>
                    <tr style="background: #f5f5f5;">
                        <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd;">Code</th>
                        <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd;">Title</th>
                        <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd;">Units</th>
                        <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd;">Level</th>
                        <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd;">Department</th>
                    </tr>
                </thead>
                <tbody>
                    ${courses
                      .map(
                        (course) => `
                        <tr>
                            <td style="padding: 0.75rem; border-bottom: 1px solid #eee;">${course.code}</td>
                            <td style="padding: 0.75rem; border-bottom: 1px solid #eee;">${course.title}</td>
                            <td style="padding: 0.75rem; border-bottom: 1px solid #eee;">${course.units}</td>
                            <td style="padding: 0.75rem; border-bottom: 1px solid #eee;">${course.level}</td>
                            <td style="padding: 0.75rem; border-bottom: 1px solid #eee;">${course.department}</td>
                        </tr>
                    `
                      )
                      .join("")}
                </tbody>
            </table>
        `;

    document.getElementById("coursesTable").innerHTML = table;
    const listElement = document.getElementById("viewCoursesList");
    listElement.style.display = "block";
    smoothScrollTo(listElement);
  } catch (err) {
    console.error(err);
    alert("Error loading courses");
  }
}

async function loadSessions() {
  try {
    const response = await fetch("/api/admin/sessions");
    const sessions = await response.json();

    const select = document.getElementById("sessionSelect");
    select.innerHTML = `
      <option value="">Select a session</option>
      ${sessions.map(session => `<option value="${session.id}">${session.name}</option>`).join("")}
    `;
  } catch (err) {
    console.error(err);
    alert("Error loading sessions");
  }
}

function displayResultsSummary(result) {
  const output = document.getElementById("resultsOutput");
  
  let html = `
    <div style="background: #f0f9ff; border: 1px solid #0ea5e9; border-radius: 8px; padding: 1rem; margin-top: 1rem;">
      <h4 style="margin: 0 0 1rem 0; color: #0369a1;">Results Generation Summary</h4>
      <p><strong>Session:</strong> ${result.sessionName}</p>
      <p><strong>Semester:</strong> ${result.semester}</p>
      <p><strong>Total Students:</strong> ${result.totalStudents}</p>
      <p style="color: #16a34a;"><strong>Successful:</strong> ${result.successCount}</p>
      <p style="color: #dc2626;"><strong>Failed:</strong> ${result.failureCount}</p>
  `;

  if (result.errors && result.errors.length > 0) {
    html += `
      <div style="margin-top: 1rem; padding: 0.75rem; background: #fef2f2; border-radius: 4px;">
        <strong style="color: #dc2626;">Errors:</strong>
        <ul style="margin: 0.5rem 0 0 0; padding-left: 1.5rem;">
          ${result.errors.map(err => `<li style="color: #991b1b;">${err}</li>`).join("")}
        </ul>
      </div>
    `;
  }

  html += `</div>`;
  
  output.innerHTML = html;
  output.style.display = "block";
  smoothScrollTo(output);
}

async function populateLecturerDropdown() {
  try {
    const response = await fetch("/api/admin/lecturers/entities");
    const lecturers = await response.json();

    const select = document.getElementById("lecturerSelect");
    select.innerHTML = `
      <option value="">Select a lecturer</option>
      ${lecturers.map(lecturer => `<option value="${lecturer.id}">${lecturer.user.fullName} (${lecturer.staffId})</option>`).join("")}
    `;
  } catch (err) {
    console.error(err);
    alert("Error loading lecturers");
  }
}

// Smooth scroll helper function
function smoothScrollTo(element) {
  setTimeout(() => {
    element.scrollIntoView({ behavior: "smooth", block: "start" });
  }, 100);
}
