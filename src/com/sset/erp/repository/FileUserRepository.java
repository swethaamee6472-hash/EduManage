package com.sset.erp.repository;

import com.sset.erp.model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-based implementation of UserRepository.
 * Persists users to a formatted JSON file without needing third-party libraries.
 * Pre-seeds demo accounts for immediate testing and viva evaluation.
 */
public class FileUserRepository implements UserRepository {
    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = DATA_DIR + File.separator + "users.json";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final Map<String, User> userCache = new ConcurrentHashMap<>();

    public FileUserRepository() {
        initStorage();
    }

    private synchronized void initStorage() {
        try {
            File dir = new File(DATA_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(DATA_FILE);
            if (!file.exists() || file.length() == 0) {
                seedInitialUsers();
                saveToFile();
            } else {
                loadFromFile();
                if (userCache.isEmpty()) {
                    seedInitialUsers();
                    saveToFile();
                }
            }
        } catch (Exception e) {
            System.err.println("[UserRepository] Error initializing storage: " + e.getMessage());
            seedInitialUsers();
        }
    }

    private void seedInitialUsers() {
        userCache.clear();

        // 1. Admin: Dr. Litty Koshy
        AdminUser admin = new AdminUser(
            "USR-ADM-001", "admin", "admin123",
            "Dr. Litty Koshy", "littykoshy@edumanage.edu",
            "System Administrator"
        );

        // 2. Faculty: Prof. Rajesh Kumar
        FacultyUser faculty = new FacultyUser(
            "USR-FAC-001", "faculty_cs", "faculty123",
            "Prof. Rajesh Kumar", "rajeshk@edumanage.edu",
            "EMP-CS-104", "Computer Science & Engineering", "Associate Professor"
        );

        // 3. Student: Swetha Sathyan
        StudentUser student = new StudentUser(
            "USR-STU-001", "student_cs", "student123",
            "Swetha Sathyan", "swethasathyan@edumanage.edu",
            "SSET24CS042", "Computer Science & Engineering", 3, "2024-2028 (CS4)"
        );

        // 4. Parent: Sathyan K (Parent of Swetha)
        ParentUser parent = new ParentUser(
            "USR-PAR-001", "parent_cs", "parent123",
            "Sathyan K", "sathyan.k@gmail.com",
            "SSET24CS042", "+91 98470 12345"
        );

        userCache.put(admin.getUsername().toLowerCase(), admin);
        userCache.put(faculty.getUsername().toLowerCase(), faculty);
        userCache.put(student.getUsername().toLowerCase(), student);
        userCache.put(parent.getUsername().toLowerCase(), parent);
    }

    private synchronized void saveToFile() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[\n");
            List<User> list = new ArrayList<>(userCache.values());
            for (int i = 0; i < list.size(); i++) {
                User u = list.get(i);
                sb.append("  {\n");
                sb.append("    \"id\": \"").append(escape(u.getId())).append("\",\n");
                sb.append("    \"username\": \"").append(escape(u.getUsername())).append("\",\n");
                sb.append("    \"passwordHash\": \"").append(escape(u.getPasswordHash())).append("\",\n");
                sb.append("    \"salt\": \"").append(escape(u.getSalt())).append("\",\n");
                sb.append("    \"fullName\": \"").append(escape(u.getFullName())).append("\",\n");
                sb.append("    \"email\": \"").append(escape(u.getEmail())).append("\",\n");
                sb.append("    \"role\": \"").append(u.getRole().name()).append("\",\n");
                sb.append("    \"active\": ").append(u.isActive()).append(",\n");
                sb.append("    \"createdAt\": \"").append(u.getCreatedAt() != null ? DATE_FORMAT.format(u.getCreatedAt()) : "").append("\"");

                if (u instanceof AdminUser) {
                    AdminUser a = (AdminUser) u;
                    sb.append(",\n    \"adminLevel\": \"").append(escape(a.getAdminLevel())).append("\"\n");
                } else if (u instanceof FacultyUser) {
                    FacultyUser f = (FacultyUser) u;
                    sb.append(",\n    \"employeeId\": \"").append(escape(f.getEmployeeId())).append("\",\n");
                    sb.append("    \"department\": \"").append(escape(f.getDepartment())).append("\",\n");
                    sb.append("    \"designation\": \"").append(escape(f.getDesignation())).append("\"\n");
                } else if (u instanceof StudentUser) {
                    StudentUser s = (StudentUser) u;
                    sb.append(",\n    \"rollNumber\": \"").append(escape(s.getRollNumber())).append("\",\n");
                    sb.append("    \"department\": \"").append(escape(s.getDepartment())).append("\",\n");
                    sb.append("    \"semester\": ").append(s.getSemester()).append(",\n");
                    sb.append("    \"batch\": \"").append(escape(s.getBatch())).append("\"\n");
                } else if (u instanceof ParentUser) {
                    ParentUser p = (ParentUser) u;
                    sb.append(",\n    \"studentRollNumber\": \"").append(escape(p.getStudentRollNumber())).append("\",\n");
                    sb.append("    \"emergencyContact\": \"").append(escape(p.getEmergencyContact())).append("\"\n");
                } else {
                    sb.append("\n");
                }

                sb.append("  }").append(i < list.size() - 1 ? ",\n" : "\n");
            }
            sb.append("]\n");

            Path path = Paths.get(DATA_FILE);
            Files.write(path, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("[UserRepository] Failed to persist data: " + e.getMessage());
        }
    }

    private synchronized void loadFromFile() {
        try {
            File file = new File(DATA_FILE);
            if (!file.exists()) return;

            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            List<Map<String, String>> records = parseJsonArray(content);

            userCache.clear();
            for (Map<String, String> map : records) {
                String id = map.get("id");
                String username = map.get("username");
                String passwordHash = map.get("passwordHash");
                String salt = map.get("salt");
                String fullName = map.get("fullName");
                String email = map.get("email");
                String roleStr = map.get("role");
                boolean active = Boolean.parseBoolean(map.getOrDefault("active", "true"));
                Date createdAt = parseDate(map.get("createdAt"));

                if (roleStr == null || username == null) continue;
                Role role = Role.valueOf(roleStr);

                User user;
                switch (role) {
                    case ADMIN:
                        user = new AdminUser(id, username, passwordHash, salt, fullName, email, active, createdAt,
                                map.getOrDefault("adminLevel", "Administrator"));
                        break;
                    case FACULTY:
                        user = new FacultyUser(id, username, passwordHash, salt, fullName, email, active, createdAt,
                                map.getOrDefault("employeeId", "EMP-000"),
                                map.getOrDefault("department", "CSE"),
                                map.getOrDefault("designation", "Faculty"));
                        break;
                    case STUDENT:
                        int sem = 1;
                        try { sem = Integer.parseInt(map.getOrDefault("semester", "1")); } catch (NumberFormatException ignored) {}
                        user = new StudentUser(id, username, passwordHash, salt, fullName, email, active, createdAt,
                                map.getOrDefault("rollNumber", "STU-000"),
                                map.getOrDefault("department", "CSE"),
                                sem,
                                map.getOrDefault("batch", "2024-2028"));
                        break;
                    case PARENT:
                        user = new ParentUser(id, username, passwordHash, salt, fullName, email, active, createdAt,
                                map.getOrDefault("studentRollNumber", "STU-000"),
                                map.getOrDefault("emergencyContact", "N/A"));
                        break;
                    default:
                        continue;
                }
                userCache.put(username.toLowerCase(), user);
            }
        } catch (Exception e) {
            System.err.println("[UserRepository] Error parsing JSON: " + e.getMessage());
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private Date parseDate(String s) {
        if (s == null || s.isEmpty()) return new Date();
        try {
            return DATE_FORMAT.parse(s);
        } catch (Exception e) {
            return new Date();
        }
    }

    /**
     * Minimal JSON array parser for dictionary objects.
     */
    private List<Map<String, String>> parseJsonArray(String json) {
        List<Map<String, String>> list = new ArrayList<>();
        int start = json.indexOf('{');
        while (start != -1) {
            int depth = 0;
            int end = -1;
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        end = i;
                        break;
                    }
                }
            }
            if (end != -1) {
                String objStr = json.substring(start + 1, end);
                list.add(parseJsonObject(objStr));
                start = json.indexOf('{', end + 1);
            } else {
                break;
            }
        }
        return list;
    }

    private Map<String, String> parseJsonObject(String obj) {
        Map<String, String> map = new HashMap<>();
        String[] lines = obj.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : lines) {
            int colon = pair.indexOf(':');
            if (colon != -1) {
                String key = pair.substring(0, colon).trim().replaceAll("^\"|\"$", "");
                String val = pair.substring(colon + 1).trim();
                if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                    val = val.substring(1, val.length() - 1);
                }
                map.put(key, val);
            }
        }
        return map;
    }

    // UserRepository API implementations
    @Override
    public Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        return userCache.values().stream().filter(u -> id.equals(u.getId())).findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(userCache.get(username.trim().toLowerCase()));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return userCache.values().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>(userCache.values());
        list.sort(Comparator.comparing(User::getFullName));
        return list;
    }

    @Override
    public List<User> findByRole(Role role) {
        List<User> list = new ArrayList<>();
        for (User u : userCache.values()) {
            if (u.getRole() == role) {
                list.add(u);
            }
        }
        list.sort(Comparator.comparing(User::getFullName));
        return list;
    }

    @Override
    public synchronized User save(User user) {
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("Cannot save null user or user with null username");
        }
        userCache.put(user.getUsername().toLowerCase(), user);
        saveToFile();
        return user;
    }

    @Override
    public synchronized boolean deleteById(String id) {
        Optional<User> u = findById(id);
        if (u.isPresent()) {
            userCache.remove(u.get().getUsername().toLowerCase());
            saveToFile();
            return true;
        }
        return false;
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null) return false;
        return userCache.containsKey(username.trim().toLowerCase());
    }

    @Override
    public long count() {
        return userCache.size();
    }

    @Override
    public long countByRole(Role role) {
        return userCache.values().stream().filter(u -> u.getRole() == role).count();
    }
}
