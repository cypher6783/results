# Student Grading and Result Processing System

## Overview
A comprehensive web-based application for managing student results, course registration, and grading. This system streamlines the academic process by providing distinct portals for Administrators, Lecturers, and Students.

## Features

### 🎓 Student Portal
- **View Results**: Check detailed semester results including GPA and CGPA.
- **Download PDF**: Generate and download official result slips.
- **Profile Management**: Secure password management.

### 👨‍🏫 Lecturer Portal
- **Score Entry**: Upload CA and Exam scores for assigned courses.
- **Bulk Upload**: Support for CSV upload of student scores.
- **Course Management**: View assigned courses and sessions.

### 🛡️ Admin Portal
- **User Management**: Create and manage Students and Lecturers.
- **Course & Session Management**: Set up academic sessions, semesters, and courses.
- **Result Processing**: Generate bulk results for entire sessions.
- **Batch PDF**: Download all results for a session in a single PDF file.
- **Security**: Reset user passwords and manage system access.

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.3 (Web, Data JPA, Security)
- **Database**: H2 (File-based, persistent)
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **PDF Generation**: iText 5
- **Build Tool**: Maven

## Getting Started

### Prerequisites
- JDK 17 or later
- Maven 3.6+

### Installation

1.  **Clone the repository**
    ```bash
    git clone https://github.com/yourusername/result-system.git
    cd result-system
    ```

2.  **Build the project**
    ```bash
    mvn clean install
    ```

3.  **Run the application**
    ```bash
    mvn spring-boot:run
    ```

4.  **Access the application**
    Open your browser and navigate to `http://localhost:8080`

### Configuration
The application uses an embedded H2 database by default. Configuration can be found in `src/main/resources/application.properties`.

## Usage

- **Default Admin Credentials**:
    - Username: `admin`
    - Password: `password` (or as configured in `DataInitializer`)

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
