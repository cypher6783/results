package com.university.resultsystem.service;

import org.springframework.stereotype.Service;

@Service
public class GradeCalculator {

    public String calculateGrade(double score) {
        if (score >= 70)
            return "A";
        if (score >= 60)
            return "B";
        if (score >= 50)
            return "C";
        if (score >= 45)
            return "D";
        if (score >= 40)
            return "E";
        return "F";
    }

    public double getGradePoint(String grade) {
        switch (grade) {
            case "A":
                return 5.0;
            case "B":
                return 4.0;
            case "C":
                return 3.0;
            case "D":
                return 2.0;
            case "E":
                return 1.0;
            case "F":
                return 0.0;
            default:
                return 0.0;
        }
    }

    public String getRemark(String grade) {
        return grade.equals("F") ? "Fail" : "Pass";
    }
}
