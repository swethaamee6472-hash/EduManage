package com.sset.erp.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Subject-wise grades and credit-weighted CGPA derived from recorded assessments.
 */
public final class AcademicPerformanceSummary {
    private final List<SubjectResult> subjectResults;
    private final double cgpa;
    private final int totalCredits;
    private final double overallPercentage;

    private AcademicPerformanceSummary(List<SubjectResult> subjectResults, double cgpa,
                                       int totalCredits, double overallPercentage) {
        this.subjectResults = List.copyOf(subjectResults);
        this.cgpa = cgpa;
        this.totalCredits = totalCredits;
        this.overallPercentage = overallPercentage;
    }

    public static AcademicPerformanceSummary from(List<AssessmentRecord> assessments) {
        if (assessments == null) {
            throw new IllegalArgumentException("Assessment records are required.");
        }
        Map<String, SubjectTotals> totalsBySubject = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        double totalScore = 0;
        double totalMaximum = 0;
        for (AssessmentRecord record : assessments) {
            if (record == null) {
                continue;
            }
            SubjectTotals totals = totalsBySubject.computeIfAbsent(record.getSubject(),
                    ignored -> new SubjectTotals(record.getSubject()));
            totals.score += record.getScore();
            totals.maximum += record.getMaximumScore();
            if (totals.latestRecord == null
                    || record.getRecordedAt().isAfter(totals.latestRecord.getRecordedAt())) {
                totals.latestRecord = record;
            }
            totalScore += record.getScore();
            totalMaximum += record.getMaximumScore();
        }

        List<SubjectResult> subjects = new ArrayList<>();
        int totalCredits = 0;
        int weightedGradePoints = 0;
        for (SubjectTotals totals : totalsBySubject.values()) {
            double percentage = totals.score / totals.maximum * 100.0;
            int credits = totals.latestRecord.getSubjectCredits();
            int gradePoint = gradePointForPercentage(percentage);
            subjects.add(new SubjectResult(totals.subject, percentage,
                    gradeForPercentage(percentage), gradePoint, credits));
            totalCredits += credits;
            weightedGradePoints += gradePoint * credits;
        }
        subjects.sort(Comparator.comparing(SubjectResult::getSubject, String.CASE_INSENSITIVE_ORDER));
        return new AcademicPerformanceSummary(subjects,
                totalCredits == 0 ? 0 : (double) weightedGradePoints / totalCredits,
                totalCredits, totalMaximum == 0 ? 0 : totalScore / totalMaximum * 100.0);
    }

    public static String gradeForPercentage(double percentage) {
        if (!Double.isFinite(percentage) || percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100.");
        }
        if (percentage >= 90) return "S";
        if (percentage >= 85) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 40) return "P";
        return "F";
    }

    public static int gradePointForPercentage(double percentage) {
        switch (gradeForPercentage(percentage)) {
            case "S": return 10;
            case "A+": return 9;
            case "A": return 8;
            case "B+": return 7;
            case "B": return 6;
            case "C": return 5;
            case "P": return 4;
            default: return 0;
        }
    }

    public List<SubjectResult> getSubjectResults() {
        return subjectResults;
    }

    public double getCgpa() {
        return cgpa;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    public double getOverallPercentage() {
        return overallPercentage;
    }

    public static final class SubjectResult {
        private final String subject;
        private final double percentage;
        private final String grade;
        private final int gradePoint;
        private final int credits;

        private SubjectResult(String subject, double percentage, String grade,
                              int gradePoint, int credits) {
            this.subject = subject;
            this.percentage = percentage;
            this.grade = grade;
            this.gradePoint = gradePoint;
            this.credits = credits;
        }

        public String getSubject() {
            return subject;
        }

        public double getPercentage() {
            return percentage;
        }

        public String getGrade() {
            return grade;
        }

        public int getGradePoint() {
            return gradePoint;
        }

        public int getCredits() {
            return credits;
        }
    }

    private static final class SubjectTotals {
        private final String subject;
        private double score;
        private double maximum;
        private AssessmentRecord latestRecord;

        private SubjectTotals(String subject) {
            this.subject = subject;
        }
    }
}
