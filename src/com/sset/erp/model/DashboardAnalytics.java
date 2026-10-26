package com.sset.erp.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Aggregated, read-only metrics used by the administrator analytics dashboard.
 */
public class DashboardAnalytics {
    private final int totalUsers;
    private final int activeUsers;
    private final int studentCount;
    private final int attendanceRecordCount;
    private final double weightedAssessmentAverage;
    private final Map<String, Integer> usersByRole;
    private final Map<String, Integer> attendanceBySubject;
    private final Map<String, Double> assessmentAverageBySubject;

    private DashboardAnalytics(int totalUsers, int activeUsers, int studentCount,
                               int attendanceRecordCount, double weightedAssessmentAverage,
                               Map<String, Integer> usersByRole, Map<String, Integer> attendanceBySubject,
                               Map<String, Double> assessmentAverageBySubject) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.studentCount = studentCount;
        this.attendanceRecordCount = attendanceRecordCount;
        this.weightedAssessmentAverage = weightedAssessmentAverage;
        this.usersByRole = Collections.unmodifiableMap(new LinkedHashMap<>(usersByRole));
        this.attendanceBySubject = Collections.unmodifiableMap(new LinkedHashMap<>(attendanceBySubject));
        this.assessmentAverageBySubject = Collections.unmodifiableMap(
                new LinkedHashMap<>(assessmentAverageBySubject));
    }

    public static DashboardAnalytics from(List<User> users, List<AttendanceRecord> attendance,
                                          List<AssessmentRecord> assessments) {
        if (users == null || attendance == null || assessments == null) {
            throw new IllegalArgumentException("Analytics source data is required.");
        }

        Map<String, Integer> usersByRole = new LinkedHashMap<>();
        for (Role role : Role.values()) {
            usersByRole.put(role.getDisplayName(), 0);
        }
        int activeUsers = 0;
        int studentCount = 0;
        int totalUsers = 0;
        for (User user : users) {
            if (user == null || user.getRole() == null) {
                continue;
            }
            totalUsers++;
            String roleName = user.getRole().getDisplayName();
            usersByRole.put(roleName, usersByRole.getOrDefault(roleName, 0) + 1);
            if (user.isActive()) {
                activeUsers++;
            }
            if (user instanceof StudentUser) {
                studentCount++;
            }
        }

        Map<String, Integer> attendanceBySubject = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        int attendanceRecordCount = 0;
        for (AttendanceRecord record : attendance) {
            if (record != null) {
                attendanceRecordCount++;
                attendanceBySubject.put(record.getSubject(),
                        attendanceBySubject.getOrDefault(record.getSubject(), 0) + 1);
            }
        }

        Map<String, double[]> scoreTotalsBySubject = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        double totalScore = 0;
        double totalMaximum = 0;
        for (AssessmentRecord record : assessments) {
            if (record == null) {
                continue;
            }
            double[] totals = scoreTotalsBySubject.computeIfAbsent(record.getSubject(), key -> new double[2]);
            totals[0] += record.getScore();
            totals[1] += record.getMaximumScore();
            totalScore += record.getScore();
            totalMaximum += record.getMaximumScore();
        }
        Map<String, Double> assessmentAverageBySubject = new LinkedHashMap<>();
        for (Map.Entry<String, double[]> entry : scoreTotalsBySubject.entrySet()) {
            double[] totals = entry.getValue();
            assessmentAverageBySubject.put(entry.getKey(), totals[0] / totals[1] * 100.0);
        }

        return new DashboardAnalytics(totalUsers, activeUsers, studentCount, attendanceRecordCount,
                totalMaximum == 0 ? 0 : totalScore / totalMaximum * 100.0,
                usersByRole, attendanceBySubject, assessmentAverageBySubject);
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public int getAttendanceRecordCount() {
        return attendanceRecordCount;
    }

    public double getWeightedAssessmentAverage() {
        return weightedAssessmentAverage;
    }

    public Map<String, Integer> getUsersByRole() {
        return usersByRole;
    }

    public Map<String, Integer> getAttendanceBySubject() {
        return attendanceBySubject;
    }

    public Map<String, Double> getAssessmentAverageBySubject() {
        return assessmentAverageBySubject;
    }
}
