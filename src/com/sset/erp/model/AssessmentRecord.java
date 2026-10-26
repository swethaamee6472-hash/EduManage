package com.sset.erp.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * An internal assessment result recorded for a student.
 */
public class AssessmentRecord {
    private final String id;
    private final String rollNumber;
    private final String subject;
    private final String assessmentName;
    private final double score;
    private final double maximumScore;
    private final int subjectCredits;
    private final String facultyName;
    private final LocalDateTime recordedAt;

    public AssessmentRecord(String id, String rollNumber, String subject, String assessmentName,
                            double score, double maximumScore, String facultyName,
                            LocalDateTime recordedAt) {
        this(id, rollNumber, subject, assessmentName, score, maximumScore, 1, facultyName, recordedAt);
    }

    public AssessmentRecord(String id, String rollNumber, String subject, String assessmentName,
                            double score, double maximumScore, int subjectCredits, String facultyName,
                            LocalDateTime recordedAt) {
        this.id = requireText(id, "Record ID");
        this.rollNumber = requireText(rollNumber, "Student roll number");
        this.subject = requireText(subject, "Subject");
        this.assessmentName = requireText(assessmentName, "Assessment name");
        this.facultyName = requireText(facultyName, "Faculty name");
        if (!Double.isFinite(score) || !Double.isFinite(maximumScore)
                || maximumScore <= 0 || score < 0 || score > maximumScore) {
            throw new IllegalArgumentException("Score must be between 0 and a positive maximum score.");
        }
        if (subjectCredits <= 0) {
            throw new IllegalArgumentException("Subject credits must be a positive whole number.");
        }
        if (recordedAt == null) {
            throw new IllegalArgumentException("Recorded date is required.");
        }
        this.score = score;
        this.maximumScore = maximumScore;
        this.subjectCredits = subjectCredits;
        this.recordedAt = recordedAt;
    }

    public static AssessmentRecord create(String rollNumber, String subject, String assessmentName,
                                          double score, double maximumScore, String facultyName) {
        return new AssessmentRecord(UUID.randomUUID().toString(), rollNumber, subject, assessmentName,
                score, maximumScore, facultyName, LocalDateTime.now());
    }

    public static AssessmentRecord create(String rollNumber, String subject, String assessmentName,
                                          double score, double maximumScore, int subjectCredits,
                                          String facultyName) {
        return new AssessmentRecord(UUID.randomUUID().toString(), rollNumber, subject, assessmentName,
                score, maximumScore, subjectCredits, facultyName, LocalDateTime.now());
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    public String getId() {
        return id;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getSubject() {
        return subject;
    }

    public String getAssessmentName() {
        return assessmentName;
    }

    public double getScore() {
        return score;
    }

    public double getMaximumScore() {
        return maximumScore;
    }

    public int getSubjectCredits() {
        return subjectCredits;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public double getPercentage() {
        return score / maximumScore * 100.0;
    }

    public String getGrade() {
        return AcademicPerformanceSummary.gradeForPercentage(getPercentage());
    }

    public int getGradePoint() {
        return AcademicPerformanceSummary.gradePointForPercentage(getPercentage());
    }
}
