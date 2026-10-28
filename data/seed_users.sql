-- ============================================================
--  EduManage - MySQL Seed Script (users.json → SQL)
--  Database : edumanage
--  Generated: 2026-10-27
-- ============================================================

CREATE DATABASE IF NOT EXISTS edumanage
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE edumanage;

-- ── Base users table ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id           VARCHAR(40)   NOT NULL PRIMARY KEY,
    username     VARCHAR(80)   NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    salt         VARCHAR(64)   NOT NULL,
    full_name    VARCHAR(120)  NOT NULL,
    email        VARCHAR(120)  NOT NULL,
    role         ENUM('STUDENT','FACULTY','ADMIN','PARENT') NOT NULL,
    active       TINYINT(1)    NOT NULL DEFAULT 1,
    created_at   DATETIME      NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Student-specific columns ─────────────────────────────────
CREATE TABLE IF NOT EXISTS students (
    user_id      VARCHAR(40)   NOT NULL PRIMARY KEY,
    roll_number  VARCHAR(30)   NOT NULL,
    department   VARCHAR(120)  NOT NULL,
    semester     TINYINT       NOT NULL,
    batch        VARCHAR(40)   NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Faculty-specific columns ──────────────────────────────────
CREATE TABLE IF NOT EXISTS faculty (
    user_id      VARCHAR(40)   NOT NULL PRIMARY KEY,
    employee_id  VARCHAR(30)   NOT NULL,
    department   VARCHAR(120)  NOT NULL,
    designation  VARCHAR(80)   NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Admin-specific columns ────────────────────────────────────
CREATE TABLE IF NOT EXISTS admins (
    user_id      VARCHAR(40)   NOT NULL PRIMARY KEY,
    admin_level  VARCHAR(60)   NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Parent-specific columns ───────────────────────────────────
CREATE TABLE IF NOT EXISTS parents (
    user_id              VARCHAR(40)  NOT NULL PRIMARY KEY,
    student_roll_number  VARCHAR(30)  NOT NULL,
    emergency_contact    VARCHAR(30),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  INSERT USERS  (INSERT IGNORE skips duplicates on re-run)
-- ============================================================

INSERT IGNORE INTO users (id, username, password_hash, salt, full_name, email, role, active, created_at) VALUES
('USR-STU-001',      'student_cs',       'yMPJUMsx2Mdvo/+8W00JXC+MEdJTRR0GfMV/TldSsyI=', 'vG0SoPeY2R3Cf0elN1GgVA==', 'Swetha Sathyan',     'swethasathyan@gmail.com',        'STUDENT', 1, '2026-09-12 14:00:07'),
('USR-STU-D2593ACA', 'test_user_50483',  'KcMw+mEP+6qT97ssi1iFwZxqkXFRaM0NH5/DDj/41vY=', '4uNJq2aXbdmv1P6J8RconQ==', 'Ragendu M',          'test_user_50483@gmail.com',      'STUDENT', 1, '2026-10-15 05:10:50'),
('USR-STU-6A2033C1', 'ashwin123',        'ORx/LPDPzQYh4XgTTNJodWYfU8mXoeI6A77OO60CC9g=', '9FDOcUBnozRYCBKB4DZuig==', 'Ashwin',             'ashwin@gmail.com',               'STUDENT', 1, '2026-09-27 20:41:35'),
('USR-STU-992759C3', 'test_user_50191',  'JygOPEIB0wPIa7ZbKvndUP2qt3wWdzFKExbX6Ksoc4o=', 'zLn/r5yzkkeJ4cTXv5eDIA==', 'Ragendu M',          'test_user_50191@gmail.com',      'STUDENT', 1, '2026-10-15 05:35:50'),
('USR-STU-BBF9F7BD', 'sourav123',        'X35l1DyZ+IX25L8re77FzO4UAnJ4Z4YMWlxaQ4qfXDI=', 'P8qyYevWDUrXLNfxZaxDbg==', 'Sourav',             'sourav@gmail.com',               'STUDENT', 1, '2026-09-25 10:14:56'),
('USR-STU-F576A052', 'test_user_85422',  'GBU5t/y9EV8mHiqMd/Q6Cx9xJNUQalGC+efuGi+5Sb8=', 'xe7VgvDyTWYOxA717NkbPg==', 'Ragendu M',          'test_user_85422@gmail.com',      'STUDENT', 1, '2026-09-27 20:39:45'),
('USR-STU-6FB3B6D2', 'Subin',            'lDSde3wcA9Arj0o5W+Tz+fSnzcSp3GtAVC3yjluhNK4=', 'F70T4WPrTuZno5Vpjn6B6Q==', 'Subin P Saji',       'subin@gmail.com',                'STUDENT', 1, '2026-10-25 17:49:19'),
('USR-STU-08560F3C', 'Vismaya',          'kuNKGyo4jq1q4VjSq5c3OlG7hRviLnVQUMlhtnGd5EA=', 'oiJRqtOMP/f7zvWtPIu+Gg==', 'Vismaya Sajith',     'vismaya@gmail.com',              'STUDENT', 1, '2026-10-25 17:48:20'),
('USR-STU-C66DB34D', 'test_user_61681',  'ltuQaNZvBnbl+L9l2+Y6CM1oQoX82zwqxcnVFy1tFuE=', 'Kk0CEKcY8R0Mx9yCqLCx+A==', 'Ragendu M',          'test_user_61681@gmail.com',      'STUDENT', 1, '2026-10-15 05:14:21'),
('USR-STU-586C7013', 'test_user_84064',  'kvGpeb7JEeJHPSfh6lFdRq+3tFPP+pO0F7Dh7dzDFrY=', 'rBbkol+Eaa4DLnoKUcPXpg==', 'Ragendu M',          'test_user_84064@gmail.com',      'STUDENT', 1, '2026-10-15 05:24:44'),
('USR-STU-2305E2DD', 'test_user_87240',  '0i0jJKZymkp5d9OOT3+27EFohyiVKyxTqyOxdUs8nD8=', 'aqPInu8ZHpkJy/N8jGe+Lg==', 'Ragendu M',          'test_user_87240@gmail.com',      'STUDENT', 1, '2026-10-15 05:19:47'),
('USR-STU-8C25B7ED', 'test_ragendu',     'clZL7neDybK3I4+5ByAWUUcdWV6zXc5s9yRU3PHVoGA=', 'lTMLvD4aZ0CJPvxzHtIMbA==', 'Ragendu M',          'ragendu@gmail.com',              'STUDENT', 1, '2026-09-12 14:00:07'),
('USR-STU-91FC004B', 'test_user_74526',  'uC/zxdTXWSymokYgzgtOrfkbuLyCAeMrXYfxVsd8qJM=', '+0xbWX5mKLP1ta2Y5O7fuA==', 'Ragendu M',          'test_user_74526@edumanage.edu',  'STUDENT', 1, '2026-09-28 11:21:14'),
('USR-STU-1C9091AD', 'test_user_18489',  'NXMz7ZwTiPbHCsAFkSjcr1Dr/HKwI2r0UwAxvQzb+7k=', '3bfwMQS+slrJTY8Pa+9C2Q==', 'Ragendu M',          'test_user_18489@edumanage.edu',  'STUDENT', 1, '2026-09-28 11:21:58'),
('USR-STU-29E8B8FE', 'test_user_13692',  'o2PwBASvJEWfT91n0siTYo4B3RemSEH7vjfOEKleJE0=', 'KUygBqGqc8fLJuJamGjVMw==', 'Ragendu M',          'test_user_13692@gmail.com',      'STUDENT', 1, '2026-10-15 05:28:33'),
('USR-FAC-001',      'faculty_cs',       'ZORvU5+CSc5KwynvpLNHN4OLFXC+Ubiv5V6wFm1qYok=', '/Vl3bqXzcM1MI4VOZy5sXw==', 'Prof. Rajesh Kumar', 'rajeshk@gmail.com',              'FACULTY', 1, '2026-09-12 14:00:07'),
('USR-ADM-001',      'admin',            'VSx3t/m0bWKk35rIFb2NhCj2jOLe/8AFETGJd91rPns=', 'dVWO2ClOf845w7LW1dZvxw==', 'admin',    'admin@gmail.com',           'ADMIN',   1, '2026-09-12 14:00:07'),
('USR-PAR-001',      'parent_cs',        'Y7vp6c98yLuoRdz/3Qxy0yNpmZ7M/LMyiOXP9/n+zCc=', 'AQ+AgfcwCe0hQvKXRd25/A==', 'Sathyan K',          'sathyan.k@gmail.com',            'PARENT',  1, '2026-09-12 14:00:07');

-- ── Students ──────────────────────────────────────────────────
INSERT IGNORE INTO students (user_id, roll_number, department, semester, batch) VALUES
('USR-STU-001',      'SSET24CS042',   'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-D2593ACA', 'SSET24CS015',   'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-6A2033C1', '23',            'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-992759C3', 'SSET24CS015',   'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-BBF9F7BD', 'sgvhgvhgvhgv',  'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-F576A052', 'SSET24CS015',   'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-6FB3B6D2', 'SSET24CS251',   'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-08560F3C', 'SSET25CS252',   'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-C66DB34D', 'SSET24CS015',   'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-586C7013', 'SSET24CS015',   'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-2305E2DD', 'SSET24CS015',   'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-8C25B7ED', 'SSET24CS015',   'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-91FC004B', 'SSET24CS015',   'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-1C9091AD', 'SSET24CS015',   'Computer Science & Engineering', 3, '2024-2028 (CS4)'),
('USR-STU-29E8B8FE', 'SSET24CS015',   'Computer Science & Engineering', 3, '2024-2028 (CS4)');

-- ── Faculty ───────────────────────────────────────────────────
INSERT IGNORE INTO faculty (user_id, employee_id, department, designation) VALUES
('USR-FAC-001', 'EMP-CS-104', 'Computer Science & Engineering', 'Associate Professor');

-- ── Admins ────────────────────────────────────────────────────
INSERT IGNORE INTO admins (user_id, admin_level) VALUES
('USR-ADM-001', 'System Administrator');

-- ── Parents ───────────────────────────────────────────────────
INSERT IGNORE INTO parents (user_id, student_roll_number, emergency_contact) VALUES
('USR-PAR-001', 'SSET24CS042', '+91 98470 12345');

-- ── Verification ─────────────────────────────────────────────
SELECT role, COUNT(*) AS total,
       GROUP_CONCAT(username ORDER BY username SEPARATOR ', ') AS usernames
FROM   users
GROUP  BY role
ORDER  BY role;
