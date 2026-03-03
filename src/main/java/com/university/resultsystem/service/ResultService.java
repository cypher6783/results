package com.university.resultsystem.service;

import com.university.resultsystem.dto.CourseResultDto;
import com.university.resultsystem.dto.DetailedResultDto;
import com.university.resultsystem.dto.ResultDto;
import com.university.resultsystem.model.*;
import com.university.resultsystem.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ResultService {

    private final ResultRepository resultRepository;
    private final ScoreRepository scoreRepository;
    private final CourseRegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final AcademicSessionRepository sessionRepository;
    private final CourseResultRepository courseResultRepository;
    private final GradeCalculator gradeCalculator;
    private final TransactionTemplate transactionTemplate;

    public ResultService(ResultRepository resultRepository, ScoreRepository scoreRepository,
            CourseRegistrationRepository registrationRepository, StudentRepository studentRepository,
            AcademicSessionRepository sessionRepository, CourseResultRepository courseResultRepository,
            GradeCalculator gradeCalculator, PlatformTransactionManager transactionManager) {
        this.resultRepository = resultRepository;
        this.scoreRepository = scoreRepository;
        this.registrationRepository = registrationRepository;
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
        this.courseResultRepository = courseResultRepository;
        this.gradeCalculator = gradeCalculator;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional
    public Result processResult(UUID studentId, UUID sessionId, Integer semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        AcademicSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Get all registrations for this student
        List<CourseRegistration> allRegs = registrationRepository.findByStudentId(studentId);

        // Filter for current semester
        List<CourseRegistration> currentRegs = allRegs.stream()
                .filter(reg -> reg.getSession().getId().equals(sessionId) && reg.getSemester().equals(semester))
                .collect(Collectors.toList());

        // Identify if a result already exists for this semester
        Result existingResult = resultRepository.findByStudentIdAndSessionIdAndSemester(studentId, sessionId, semester)
                .orElse(null);

        // Initialize current semester metrics to literal zero
        int tcc = 0;
        int tce = 0;
        double tpe = 0.0;

        List<CourseResult> courseResults = new ArrayList<>();

        for (CourseRegistration reg : currentRegs) {
            Score score = scoreRepository.findByRegistrationId(reg.getId()).orElse(null);

            if (score != null) {
                int units = reg.getCourse().getUnits();
                tcc += units;

                double totalScore = score.getCaScore() + score.getExamScore();
                String grade = gradeCalculator.calculateGrade(totalScore);
                double gradePoint = gradeCalculator.getGradePoint(grade);
                double pointEarned = gradePoint * units;
                String remark = gradeCalculator.getRemark(grade);

                tpe += pointEarned;
                if (!grade.equals("F")) {
                    tce += units;
                }

                // Create CourseResult entry (will be saved later)
                CourseResult courseResult = new CourseResult();
                courseResult.setCourse(reg.getCourse());
                courseResult.setUnit(units);
                courseResult.setScore(totalScore);
                courseResult.setPointEarned(pointEarned);
                courseResult.setGrade(grade);
                courseResult.setRemark(remark);
                courseResults.add(courseResult);
            }
        }

        double gpa = (tcc > 0) ? tpe / tcc : 0.0;

        // Initialize cumulative metrics to literal zero (Atomic Consolidation)
        int ccc = 0;
        int cce = 0;
        double cpe = 0.0;

        // Map to hold unique unit/point data for each logical semester
        // Key: Super-Normalized "SESSION|SEMESTER" (ignores slashes, spaces, case)
        java.util.Map<String, RecordSums> logicalSemesterSums = new java.util.HashMap<>();

        // 1. Add current (in-memory) calculation as the DEFINITIVE truth for this
        // semester
        final String currentSessionNormalized = session.getName().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        final String currentKey = currentSessionNormalized + "|" + semester;
        logicalSemesterSums.put(currentKey, new RecordSums(tcc, tce, tpe));

        // 2. Fetch all historical results and merge them
        List<Result> allStudentResults = resultRepository.findByStudentId(studentId);
        for (Result r : allStudentResults) {
            String rSessionNormalized = r.getSession().getName().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
            String key = rSessionNormalized + "|" + r.getSemester();

            // Skip the semester we're currently calculating (already in map as truth)
            if (key.equals(currentKey)) {
                continue;
            }

            // Consolidate other semesters (ignores ghosts/duplicates by overwriting)
            logicalSemesterSums.put(key, new RecordSums(
                    (r.getTcc() != null) ? r.getTcc() : 0,
                    (r.getTce() != null) ? r.getTce() : 0,
                    (r.getTpe() != null) ? r.getTpe() : 0.0));
        }

        // 3. Perform final accumulation starting from literal zero
        for (RecordSums sums : logicalSemesterSums.values()) {
            ccc += sums.tcc;
            cce += sums.tce;
            cpe += sums.tpe;
        }

        double cgpa = (ccc > 0) ? cpe / ccc : 0.0;

        // Session-Agnostic Previous Metrics Calculation
        // Previous = Cumulative (Total History) - Current (This Semester)
        // This guarantees continuity across sessions/years without complex DB lookups
        int previousTccVal = ccc - tcc;
        int previousTceVal = cce - tce;
        double previousTpeVal = cpe - tpe;
        double previousCgpaVal = (previousTccVal > 0) ? previousTpeVal / previousTccVal : 0.0;

        // Create or update Result
        Result result = (existingResult != null) ? existingResult : new Result();

        if (result.getId() == null) {
            result.setStudent(student);
            result.setSession(session);
            result.setSemester(semester);
        }

        // Set current semester metrics
        result.setTcc(tcc);
        result.setTce(tce);
        result.setTpe(Math.round(tpe * 100.0) / 100.0);
        result.setGpa(Math.round(gpa * 100.0) / 100.0);

        // Set previous semester metrics (Mathematically Derived)
        if (previousTccVal > 0) {
            result.setPreviousTcc(previousTccVal);
            result.setPreviousTce(previousTceVal);
            result.setPreviousTpe(Math.round(previousTpeVal * 100.0) / 100.0);
            result.setPreviousGpa(Math.round(previousCgpaVal * 100.0) / 100.0);
        } else {
            result.setPreviousTcc(null);
            result.setPreviousTce(null);
            result.setPreviousTpe(null);
            result.setPreviousGpa(null);
        }

        // Set cumulative metrics
        result.setCcc(ccc);
        result.setCce(cce);
        result.setCpe(Math.round(cpe * 100.0) / 100.0);
        result.setCgpa(Math.round(cgpa * 100.0) / 100.0);

        // Legacy fields
        result.setTotalUnits(ccc);
        result.setTotalPoints(cpe);

        // Determine status
        if (gpa < 1.0) {
            result.setStatus("PROBATION");
        } else {
            result.setStatus("PASS");
        }

        result = resultRepository.save(result);

        // Clear existing CourseResult entries to avoid duplicates
        courseResultRepository.deleteByResultId(result.getId());

        // Save CourseResult entries
        for (CourseResult courseResult : courseResults) {
            courseResult.setResult(result);
            courseResultRepository.save(courseResult);
        }

        return result;
    }

    @Transactional
    public void submitResults(UUID sessionId, Integer semester) {
        List<Result> results = resultRepository.findBySessionIdAndSemester(sessionId, semester);
        for (Result result : results) {
            result.setApprovalStatus(ResultStatus.SUBMITTED);
        }
        resultRepository.saveAll(results);
    }

    @Transactional
    public void vetResults(UUID sessionId, Integer semester) {
        List<Result> results = resultRepository.findBySessionIdAndSemester(sessionId, semester);
        for (Result result : results) {
            result.setApprovalStatus(ResultStatus.VETTED);
        }
        resultRepository.saveAll(results);
    }

    @Transactional
    public void publishResults(UUID sessionId, Integer semester) {
        List<Result> results = resultRepository.findBySessionIdAndSemester(sessionId, semester);
        for (Result result : results) {
            result.setApprovalStatus(ResultStatus.PUBLISHED);
            result.setPublished(true);
        }
        resultRepository.saveAll(results);
    }

    @Transactional
    public void updateResultStatus(UUID resultId, ResultStatus status) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Result not found"));
        result.setApprovalStatus(status);
        if (status == ResultStatus.PUBLISHED) {
            result.setPublished(true);
        }
        resultRepository.save(result);
    }

    public ResultStatus getAggregateStatus(UUID sessionId, Integer semester) {
        List<Result> results = resultRepository.findBySessionIdAndSemester(sessionId, semester);
        if (results.isEmpty()) {
            throw new RuntimeException("No results found for the specified session and semester.");
        }
        return results.get(0).getApprovalStatus();
    }

    public DetailedResultDto getDetailedResult(UUID studentId, UUID sessionId, Integer semester) {
        return getDetailedResult(studentId, sessionId, semester, false);
    }

    public DetailedResultDto getDetailedResult(UUID studentId, UUID sessionId, Integer semester,
            boolean checkPublished) {
        Result result = resultRepository.findByStudentIdAndSessionIdAndSemester(studentId, sessionId, semester)
                .orElseThrow(() -> new RuntimeException("Result not found"));

        if (checkPublished && result.getApprovalStatus() != ResultStatus.PUBLISHED) {
            throw new RuntimeException("Results for this semester have not been published yet.");
        }

        Student student = result.getStudent();

        DetailedResultDto dto = new DetailedResultDto();
        // ... rest of the method (unchanged)

        // Student info
        dto.setMatricNo(student.getMatricNo());
        dto.setFullName(student.getUser().getFullName());
        dto.setCourse(student.getDepartment());
        dto.setLevel(String.valueOf(student.getLevel()));

        // Session info
        dto.setSessionName(result.getSession().getName());
        dto.setSemester(result.getSemester());

        // Get course results
        List<CourseResult> courseResults = courseResultRepository.findByResultId(result.getId());
        List<CourseResultDto> courseDtos = courseResults.stream().map(cr -> {
            CourseResultDto courseDto = new CourseResultDto();
            courseDto.setCode(cr.getCourse().getCode());
            courseDto.setTitle(cr.getCourse().getTitle());
            courseDto.setUnit(cr.getUnit());
            courseDto.setScore(cr.getScore());
            courseDto.setPointEarned(cr.getPointEarned());
            courseDto.setGrade(cr.getGrade());
            courseDto.setRemark(cr.getRemark());
            return courseDto;
        }).collect(Collectors.toList());

        dto.setCourses(courseDtos);

        // Current semester metrics
        dto.setTcc(result.getTcc());
        dto.setTce(result.getTce());
        dto.setTpe(result.getTpe());
        dto.setGpa(result.getGpa());

        // Previous semester metrics
        dto.setPreviousTcc(result.getPreviousTcc());
        dto.setPreviousTce(result.getPreviousTce());
        dto.setPreviousTpe(result.getPreviousTpe());
        dto.setPreviousGpa(result.getPreviousGpa());

        // Cumulative metrics
        dto.setCcc(result.getCcc());
        dto.setCce(result.getCce());
        dto.setCpe(result.getCpe());
        dto.setCgpa(result.getCgpa());

        dto.setStatus(result.getStatus());
        dto.setApprovalStatus(result.getApprovalStatus().name());

        return dto;
    }

    public ResultDto getResultDto(UUID studentId, UUID sessionId, Integer semester) {
        return getResultDto(studentId, sessionId, semester, false);
    }

    public ResultDto getResultDto(UUID studentId, UUID sessionId, Integer semester, boolean checkPublished) {
        Result result = resultRepository.findByStudentIdAndSessionIdAndSemester(studentId, sessionId, semester)
                .orElseThrow(() -> new RuntimeException("Result not found"));

        if (checkPublished && !result.isPublished()) {
            throw new RuntimeException("Results for this semester have not been published yet.");
        }

        ResultDto dto = new ResultDto();
        dto.setStudentId(result.getStudent().getId());
        dto.setMatricNo(result.getStudent().getMatricNo());
        dto.setStudentName(result.getStudent().getUser().getFullName());
        dto.setSessionId(result.getSession().getId());
        dto.setSessionName(result.getSession().getName());
        dto.setSemester(result.getSemester());
        dto.setGpa(result.getGpa());
        dto.setCgpa(result.getCgpa());
        dto.setStatus(result.getCgpa() >= 1.0 ? "PASS" : "FAIL");
        dto.setRemarks(dto.getStatus());

        return dto;
    }

    // @Transactional - Removed to prevent rollback of entire batch if one fails
    public java.util.Map<String, Object> processBulkResults(UUID sessionId, Integer semester) {
        AcademicSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<Student> allStudents = studentRepository.findAll();

        int successCount = 0;
        int failureCount = 0;
        java.util.List<String> errors = new java.util.ArrayList<>();

        for (Student student : allStudents) {
            try {
                final Student currentStudent = student;
                transactionTemplate.execute(status -> {
                    processResult(currentStudent.getId(), sessionId, semester);
                    return null;
                });
                successCount++;
            } catch (Exception e) {
                failureCount++;
                String errorMsg = "Failed to process result for student " + student.getMatricNo();
                errors.add(errorMsg + ": " + e.getMessage());
                log.error("{}: {}", errorMsg, e.getMessage(), e);
            }
        }

        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("totalStudents", allStudents.size());
        summary.put("successCount", successCount);
        summary.put("failureCount", failureCount);
        summary.put("sessionId", sessionId);
        summary.put("sessionName", session.getName());
        summary.put("semester", semester);
        summary.put("errors", errors);

        return summary;
    }

    @Transactional
    public void deleteAllResults() {
        log.warn("Wiping all result data from the database...");
        courseResultRepository.deleteAll();
        resultRepository.deleteAll();
    }

    @Transactional
    public void deleteResultsBySession(UUID sessionId, Integer semester) {
        log.warn("Deleting results for session {} and semester {}...", sessionId, semester);
        courseResultRepository.deleteByResult_SessionIdAndResult_Semester(sessionId, semester);
        resultRepository.deleteBySessionIdAndSemester(sessionId, semester);
    }

    private static class RecordSums {
        int tcc;
        int tce;
        double tpe;

        RecordSums(int tcc, int tce, double tpe) {
            this.tcc = tcc;
            this.tce = tce;
            this.tpe = tpe;
        }
    }
}
