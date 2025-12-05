package com.university.resultsystem.service;

import com.university.resultsystem.dto.CourseResultDto;
import com.university.resultsystem.dto.DetailedResultDto;
import com.university.resultsystem.dto.ResultDto;
import com.university.resultsystem.model.*;
import com.university.resultsystem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResultService {

    private final ResultRepository resultRepository;
    private final ScoreRepository scoreRepository;
    private final CourseRegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final AcademicSessionRepository sessionRepository;
    private final CourseResultRepository courseResultRepository;
    private final GradeCalculator gradeCalculator;

    public ResultService(ResultRepository resultRepository, ScoreRepository scoreRepository,
            CourseRegistrationRepository registrationRepository, StudentRepository studentRepository,
            AcademicSessionRepository sessionRepository, CourseResultRepository courseResultRepository,
            GradeCalculator gradeCalculator) {
        this.resultRepository = resultRepository;
        this.scoreRepository = scoreRepository;
        this.registrationRepository = registrationRepository;
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
        this.courseResultRepository = courseResultRepository;
        this.gradeCalculator = gradeCalculator;
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

        // Calculate current semester metrics
        int tcc = 0;
        int tce = 0;
        double tpe = 0.0;

        List<CourseResult> courseResults = new ArrayList<>();

        for (CourseRegistration reg : currentRegs) {
            Score score = scoreRepository.findByRegistrationId(reg.getId()).orElse(null);

            int units = reg.getCourse().getUnits();
            tcc += units;

            if (score != null) {
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

        // Get previous semester result (from same session, previous semester)
        Result previousResult = null;
        Integer previousTcc = null;
        Integer previousTce = null;
        Double previousTpe = null;
        Double previousGpa = null;

        if (semester > 1) {
            previousResult = resultRepository
                    .findByStudentIdAndSessionIdAndSemester(studentId, sessionId, semester - 1)
                    .orElse(null);

            if (previousResult != null) {
                previousTcc = previousResult.getTcc();
                previousTce = previousResult.getTce();
                previousTpe = previousResult.getTpe();
                previousGpa = previousResult.getGpa();
            }
        }

        // Calculate cumulative metrics (all semesters from all sessions)
        int ccc = tcc;
        int cce = tce;
        double cpe = tpe;

        // Get all previous results for this student (excluding current one)
        List<Result> allPreviousResults = resultRepository.findByStudentId(studentId);
        for (Result prevRes : allPreviousResults) {
            // Skip if it's the current result we're processing
            if (prevRes.getSession().getId().equals(sessionId) && prevRes.getSemester().equals(semester)) {
                continue;
            }

            ccc += (prevRes.getTcc() != null) ? prevRes.getTcc() : 0;
            cce += (prevRes.getTce() != null) ? prevRes.getTce() : 0;
            cpe += (prevRes.getTpe() != null) ? prevRes.getTpe() : 0.0;
        }

        double cgpa = (ccc > 0) ? cpe / ccc : 0.0;

        // Create or update Result
        Result result = resultRepository.findByStudentIdAndSessionIdAndSemester(studentId, sessionId, semester)
                .orElse(new Result());

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

        // Set previous semester metrics
        result.setPreviousTcc(previousTcc);
        result.setPreviousTce(previousTce);
        result.setPreviousTpe(previousTpe != null ? Math.round(previousTpe * 100.0) / 100.0 : null);
        result.setPreviousGpa(previousGpa != null ? Math.round(previousGpa * 100.0) / 100.0 : null);

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

        // Save CourseResult entries
        for (CourseResult courseResult : courseResults) {
            courseResult.setResult(result);
            courseResultRepository.save(courseResult);
        }

        return result;
    }

    public DetailedResultDto getDetailedResult(UUID studentId, UUID sessionId, Integer semester) {
        Result result = resultRepository.findByStudentIdAndSessionIdAndSemester(studentId, sessionId, semester)
                .orElseThrow(() -> new RuntimeException("Result not found"));

        Student student = result.getStudent();

        DetailedResultDto dto = new DetailedResultDto();

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

        return dto;
    }

    public ResultDto getResultDto(UUID studentId, UUID sessionId, Integer semester) {
        Result result = resultRepository.findByStudentIdAndSessionIdAndSemester(studentId, sessionId, semester)
                .orElseThrow(() -> new RuntimeException("Result not found"));

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
                processResult(student.getId(), sessionId, semester);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                String errorMsg = "Failed to process result for student " + student.getMatricNo() +
                        ": " + e.getClass().getSimpleName() + " - " + e.getMessage();
                errors.add(errorMsg);
                System.err.println(errorMsg);
                e.printStackTrace();
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
}
