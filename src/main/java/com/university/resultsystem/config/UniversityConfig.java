package com.university.resultsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "university")
@Data
public class UniversityConfig {
    private String name = "JOSEPH SARWUAN TARKA UNIVERSITY";
    private String pmb = "P.M.B 2373, MAKURDI";
    private String college = "COLLEGE OF PHYSICAL SCIENCES";
    private String department = "DEPARTMENT OF COMPUTER SCIENCE";
    private String course = "B.SC. COMPUTER SCIENCE";
}
