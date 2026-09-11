package exam_management_syatem.migration;

import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.*;
import exam_management_syatem.security.PasswordHasher;
import exam_management_syatem.dao.*;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DataMigrator {

    public static class MigrationAudit {
        public int studentsSourceCount = 0;
        public int studentsMigratedCount = 0;
        public int studentsSkippedCount = 0;

        public int usersSourceCount = 0;
        public int usersMigratedCount = 0;
        public int usersSkippedCount = 0;

        public int subjectsSourceCount = 0;
        public int subjectsMigratedCount = 0;
        public int subjectsSkippedCount = 0;

        public int questionsSourceCount = 0;
        public int questionsMigratedCount = 0;
        public int questionsSkippedCount = 0;

        public int schedulesSourceCount = 0;
        public int schedulesMigratedCount = 0;
        public int schedulesSkippedCount = 0;

        public int resultsSourceCount = 0;
        public int resultsMigratedCount = 0;
        public int resultsSkippedCount = 0;

        public List<String> errorLogs = new ArrayList<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n=================== DATA MIGRATION AUDIT REPORT ===================\n");
            sb.append(String.format("Students   : Source=%d, Migrated=%d, Skipped=%d\n", studentsSourceCount, studentsMigratedCount, studentsSkippedCount));
            sb.append(String.format("Users      : Source=%d, Migrated=%d, Skipped=%d\n", usersSourceCount, usersMigratedCount, usersSkippedCount));
            sb.append(String.format("Subjects   : Source=%d, Migrated=%d, Skipped=%d\n", subjectsSourceCount, subjectsMigratedCount, subjectsSkippedCount));
            sb.append(String.format("Questions  : Source=%d, Migrated=%d, Skipped=%d\n", questionsSourceCount, questionsMigratedCount, questionsSkippedCount));
            sb.append(String.format("Schedules  : Source=%d, Migrated=%d, Skipped=%d\n", schedulesSourceCount, schedulesMigratedCount, schedulesSkippedCount));
            sb.append(String.format("Results    : Source=%d, Migrated=%d, Skipped=%d\n", resultsSourceCount, resultsMigratedCount, resultsSkippedCount));
            if (!errorLogs.isEmpty()) {
                sb.append("\nDiscrepancies / Discarded Notes:\n");
                for (String err : errorLogs) {
                    sb.append(" - ").append(err).append("\n");
                }
            }
            sb.append("===================================================================\n");
            return sb.toString();
        }
    }

    public static MigrationAudit migrateAll(String baseDbDir) {
        MigrationAudit audit = new MigrationAudit();
        try {
            // Ensure SQLite database schema exists
            DatabaseManager.initializeDatabase();

            // Load UCanAccess driver
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            Map<String, Integer> studentUsernameToIdMap = new HashMap<>();
            Map<String, Integer> studentNameToIdMap = new HashMap<>();
            Map<String, Integer> subjectNameToIdMap = new HashMap<>();

            // 1. Migrate Students
            migrateStudents(baseDbDir, audit, studentUsernameToIdMap, studentNameToIdMap);

            // 2. Migrate Users / Logins
            migrateUsers(baseDbDir, audit, studentUsernameToIdMap);

            // 3. Migrate Subjects
            migrateSubjects(baseDbDir, audit, subjectNameToIdMap);

            // 4. Migrate Questions (Dynamic Tables)
            migrateQuestions(baseDbDir, audit, subjectNameToIdMap);

            // 5. Migrate Schedules
            migrateSchedules(baseDbDir, audit, studentUsernameToIdMap, subjectNameToIdMap);

            // 6. Migrate Results
            migrateResults(baseDbDir, audit, studentUsernameToIdMap, studentNameToIdMap, subjectNameToIdMap);

        } catch (Exception e) {
            audit.errorLogs.add("Fatal Migration Error: " + e.getMessage());
            e.printStackTrace();
        }
        return audit;
    }

    private static Connection getAccessConnection(String dbPath) throws SQLException {
        File file = new File(dbPath);
        if (!file.exists()) {
            throw new SQLException("Access DB file does not exist: " + dbPath);
        }
        return DriverManager.getConnection("jdbc:ucanaccess://" + file.getAbsolutePath());
    }

    private static void migrateStudents(String baseDbDir, MigrationAudit audit, Map<String, Integer> studentUsernameToIdMap, Map<String, Integer> studentNameToIdMap) {
        String dbPath = baseDbDir + File.separator + "studentdata.accdb";
        StudentDAO studentDAO = new StudentDAO();
        try (Connection accessConn = getAccessConnection(dbPath);
             Statement stmt = accessConn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM data")) {

            while (rs.next()) {
                audit.studentsSourceCount++;
                String name = rs.getString("Name");
                String mob = rs.getString("Mobile_number");
                String email = rs.getString("Email_Id");
                String aadharno = rs.getString("Aadhar_no");
                String dob = rs.getString("Date_Birth");

                if (name == null || name.trim().isEmpty() || aadharno == null || aadharno.trim().isEmpty()) {
                    audit.studentsSkippedCount++;
                    audit.errorLogs.add("Skipped student record missing name or aadhar: " + name);
                    continue;
                }

                // Check existing in SQLite
                Student existing = studentDAO.findByAadhar(aadharno.trim());
                int studentId;
                if (existing == null) {
                    Student s = new Student();
                    s.setName(name.trim());
                    s.setMobile(mob != null ? mob.trim() : "");
                    s.setEmail(email != null ? email.trim() : "");
                    s.setAadharNo(aadharno.trim());
                    s.setDateOfBirth(dob != null ? dob.trim() : "");

                    try (Connection sqliteConn = DatabaseManager.getConnection()) {
                        studentId = studentDAO.insert(s, sqliteConn);
                    }
                    audit.studentsMigratedCount++;
                } else {
                    studentId = existing.getId();
                    audit.studentsSkippedCount++;
                }

                // Derive username pattern matching Studentadder logic: name prefix + last 4 aadhar digits
                String cleanedName = name.trim().replaceAll("\\s+", "");
                if (cleanedName.length() < 4) {
                    cleanedName = (cleanedName + "aaaa").substring(0, 4);
                } else {
                    cleanedName = cleanedName.substring(0, 4);
                }
                String last4Aadhar = aadharno.trim().length() >= 4 ? aadharno.trim().substring(aadharno.trim().length() - 4) : "0000";
                String derivedUsername = (cleanedName + last4Aadhar).toLowerCase();

                studentUsernameToIdMap.put(derivedUsername, studentId);
                studentNameToIdMap.put(name.trim().toLowerCase(), studentId);
            }
        } catch (Exception e) {
            audit.errorLogs.add("Error migrating students: " + e.getMessage());
        }
    }

    private static void migrateUsers(String baseDbDir, MigrationAudit audit, Map<String, Integer> studentUsernameToIdMap) {
        String dbPath = baseDbDir + File.separator + "studentloginn.accdb";
        UserDAO userDAO = new UserDAO();
        try (Connection accessConn = getAccessConnection(dbPath);
             Statement stmt = accessConn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM star")) {

            while (rs.next()) {
                audit.usersSourceCount++;
                String username = rs.getString("username");
                String password = rs.getString("password");

                if (username == null || username.trim().isEmpty()) {
                    audit.usersSkippedCount++;
                    continue;
                }

                String cleanUsername = username.trim().toLowerCase();
                User existing = userDAO.findByUsername(cleanUsername);
                if (existing == null) {
                    Integer studentId = studentUsernameToIdMap.get(cleanUsername);
                    String passwordHash = (password != null && !password.isEmpty()) ? PasswordHasher.hashPassword(password) : PasswordHasher.hashPassword("123456");

                    User u = new User();
                    u.setUsername(cleanUsername);
                    u.setPasswordHash(passwordHash);
                    u.setRole("STUDENT");
                    u.setStudentId(studentId);
                    u.setActive(true);

                    try (Connection sqliteConn = DatabaseManager.getConnection()) {
                        userDAO.insert(u, sqliteConn);
                    }
                    audit.usersMigratedCount++;
                } else {
                    audit.usersSkippedCount++;
                }
            }
        } catch (Exception e) {
            audit.errorLogs.add("Error migrating users: " + e.getMessage());
        }
    }

    private static void migrateSubjects(String baseDbDir, MigrationAudit audit, Map<String, Integer> subjectNameToIdMap) {
        String dbPath = baseDbDir + File.separator + "tablename.accdb";
        SubjectDAO subjectDAO = new SubjectDAO();
        try (Connection accessConn = getAccessConnection(dbPath);
             Statement stmt = accessConn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Table1")) {

            while (rs.next()) {
                audit.subjectsSourceCount++;
                String name = rs.getString("ID");
                if (name == null || name.trim().isEmpty()) {
                    audit.subjectsSkippedCount++;
                    continue;
                }

                String cleanName = name.trim();
                Subject existing = subjectDAO.findByName(cleanName);
                int subjectId;
                if (existing == null) {
                    Subject s = new Subject();
                    s.setName(cleanName);
                    s.setActive(true);
                    subjectId = subjectDAO.insert(s);
                    audit.subjectsMigratedCount++;
                } else {
                    subjectId = existing.getId();
                    audit.subjectsSkippedCount++;
                }
                subjectNameToIdMap.put(cleanName.toLowerCase(), subjectId);
            }
        } catch (Exception e) {
            audit.errorLogs.add("Error migrating subjects: " + e.getMessage());
        }
    }

    private static void migrateQuestions(String baseDbDir, MigrationAudit audit, Map<String, Integer> subjectNameToIdMap) {
        String dbPath = baseDbDir + File.separator + "subject1.accdb";
        QuestionDAO questionDAO = new QuestionDAO();
        SubjectDAO subjectDAO = new SubjectDAO();

        try (Connection accessConn = getAccessConnection(dbPath)) {
            DatabaseMetaData md = accessConn.getMetaData();
            try (ResultSet tables = md.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    if (tableName.startsWith("MSys") || tableName.startsWith("msys")) {
                        continue;
                    }

                    // Find or create subject ID for table name
                    String cleanSubName = tableName.trim();
                    Integer subjectId = subjectNameToIdMap.get(cleanSubName.toLowerCase());
                    if (subjectId == null) {
                        Subject existing = subjectDAO.findByName(cleanSubName);
                        if (existing == null) {
                            Subject s = new Subject();
                            s.setName(cleanSubName);
                            s.setActive(true);
                            subjectId = subjectDAO.insert(s);
                        } else {
                            subjectId = existing.getId();
                        }
                        subjectNameToIdMap.put(cleanSubName.toLowerCase(), subjectId);
                    }

                    // Query table rows
                    try (Statement stmt = accessConn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT * FROM [" + tableName + "]")) {
                        while (rs.next()) {
                            audit.questionsSourceCount++;
                            String qText = rs.getString("Question");
                            String opt1 = rs.getString("Option1");
                            String opt2 = rs.getString("Option2");
                            String opt3 = rs.getString("Option3");
                            String opt4 = rs.getString("Option4");
                            String ans = rs.getString("Answer");

                            if (qText == null || qText.trim().isEmpty()) {
                                audit.questionsSkippedCount++;
                                continue;
                            }

                            Question q = new Question();
                            q.setSubjectId(subjectId);
                            q.setQuestionText(qText.trim());
                            q.setOption1(opt1 != null ? opt1.trim() : "");
                            q.setOption2(opt2 != null ? opt2.trim() : "");
                            q.setOption3(opt3 != null ? opt3.trim() : "");
                            q.setOption4(opt4 != null ? opt4.trim() : "");
                            q.setCorrectAnswer(ans != null ? ans.trim() : "");

                            questionDAO.insert(q);
                            audit.questionsMigratedCount++;
                        }
                    } catch (Exception te) {
                        audit.errorLogs.add("Error reading table " + tableName + ": " + te.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            audit.errorLogs.add("Error migrating questions: " + e.getMessage());
        }
    }

    private static void migrateSchedules(String baseDbDir, MigrationAudit audit, Map<String, Integer> studentUsernameToIdMap, Map<String, Integer> subjectNameToIdMap) {
        String dbPath = baseDbDir + File.separator + "tastmakerdata.accdb";
        ExamScheduleDAO scheduleDAO = new ExamScheduleDAO();
        try (Connection accessConn = getAccessConnection(dbPath);
             Statement stmt = accessConn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Table1")) {

            while (rs.next()) {
                audit.schedulesSourceCount++;
                String username = rs.getString("username");
                String subject = rs.getString("Subject");
                String timeStr = rs.getString("Time");
                String questionStr = rs.getString("noques");
                String passStr = rs.getString("pass");

                if (username == null || subject == null) {
                    audit.schedulesSkippedCount++;
                    continue;
                }

                Integer studentId = studentUsernameToIdMap.get(username.trim().toLowerCase());
                Integer subjectId = subjectNameToIdMap.get(subject.trim().toLowerCase());

                if (studentId == null || subjectId == null) {
                    audit.schedulesSkippedCount++;
                    audit.errorLogs.add(String.format("Skipped schedule for user '%s' / sub '%s': missing student or subject ID", username, subject));
                    continue;
                }

                int duration = 30;
                try { if (timeStr != null) duration = Integer.parseInt(timeStr.trim()); } catch (Exception ignored) {}

                int totalQ = 10;
                try { if (questionStr != null) totalQ = Integer.parseInt(questionStr.trim()); } catch (Exception ignored) {}

                double passP = 40.0;
                try { if (passStr != null) passP = Double.parseDouble(passStr.trim()); } catch (Exception ignored) {}

                ExamSchedule es = new ExamSchedule();
                es.setStudentId(studentId);
                es.setSubjectId(subjectId);
                es.setDurationMinutes(duration);
                es.setTotalQuestions(totalQ);
                es.setPassingPercentage(passP);
                es.setStatus("SCHEDULED");

                scheduleDAO.insert(es);
                audit.schedulesMigratedCount++;
            }
        } catch (Exception e) {
            audit.errorLogs.add("Error migrating schedules: " + e.getMessage());
        }
    }

    private static void migrateResults(String baseDbDir, MigrationAudit audit, Map<String, Integer> studentUsernameToIdMap, Map<String, Integer> studentNameToIdMap, Map<String, Integer> subjectNameToIdMap) {
        String dbPath = baseDbDir + File.separator + "result.accdb";
        ExamResultDAO resultDAO = new ExamResultDAO();
        try (Connection accessConn = getAccessConnection(dbPath);
             Statement stmt = accessConn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Table1")) {

            while (rs.next()) {
                audit.resultsSourceCount++;
                String username = rs.getString("username");
                String studentName = rs.getString("name");
                String sub = rs.getString("subject");
                String marksStr = rs.getString("marks");
                String pass1Str = rs.getString("pass1");

                if ((username == null && studentName == null) || sub == null) {
                    audit.resultsSkippedCount++;
                    continue;
                }

                Integer studentId = null;
                if (username != null) {
                    studentId = studentUsernameToIdMap.get(username.trim().toLowerCase());
                }
                if (studentId == null && studentName != null) {
                    studentId = studentNameToIdMap.get(studentName.trim().toLowerCase());
                }

                Integer subjectId = subjectNameToIdMap.get(sub.trim().toLowerCase());

                if (studentId == null || subjectId == null) {
                    audit.resultsSkippedCount++;
                    audit.errorLogs.add(String.format("Skipped result for student '%s' / sub '%s': student or subject ID not found", studentName != null ? studentName : username, sub));
                    continue;
                }

                int totalQ = 10;
                int correctQ = 0;
                try { if (marksStr != null) correctQ = Integer.parseInt(marksStr.trim()); } catch (Exception ignored) {}

                double perc = 0.0;
                try { if (pass1Str != null) perc = Double.parseDouble(pass1Str.trim()); } catch (Exception ignored) {}

                String finalResult = perc >= 40.0 ? "Pass" : "Fail";

                ExamResult er = new ExamResult();
                er.setStudentId(studentId);
                er.setSubjectId(subjectId);
                er.setTotalQuestions(totalQ);
                er.setCorrectAnswers(correctQ);
                er.setMarks(correctQ);
                er.setPercentage(perc);
                er.setResult(finalResult);

                resultDAO.insert(er);
                audit.resultsMigratedCount++;
            }
        } catch (Exception e) {
            audit.errorLogs.add("Error migrating results: " + e.getMessage());
        }
    }
}
