package exam_management_syatem.db;

import exam_management_syatem.db.QuestionBankPart1.QuestionItem;
import exam_management_syatem.security.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transactional Database Reset and Fresh Realistic Data Seeder.
 * Resets non-admin data cleanly, preserves superadmin intact, and populates
 * 20 students, 15 subjects, 750+ MCQs, 35 schedules, 18 attempts, and 12 results.
 */
public class FreshDatabaseSeeder {

    private static class AdminSnapshot {
        int id;
        String username;
        String passwordHash;
        String role;
        Integer studentId;
        boolean active;
    }

    public static void main(String[] args) {
        if (args.length == 0 || !"RESET".equalsIgnoreCase(args[0])) {
            System.out.println("========================================================================");
            System.out.println("WARNING: THIS WILL DELETE ALL NON-ADMIN APPLICATION DATA.");
            System.out.println("To confirm and proceed, run this program with the explicit 'RESET' argument:");
            System.out.println("java -cp \"bin:Resource/*\" exam_management_syatem.db.FreshDatabaseSeeder RESET");
            System.out.println("========================================================================");
            System.exit(1);
            return;
        }

        System.out.println("========================================================================");
        System.out.println("           STARTING TRANSACTIONAL DATABASE RESET & SEEDING              ");
        System.out.println("========================================================================");

        try {
            executeResetAndSeed();
            System.out.println("========================================================================");
            System.out.println("      DATABASE RESET & SEEDING COMPLETED AND COMMITTED SUCCESSFULLY!    ");
            System.out.println("========================================================================");
        } catch (Exception e) {
            System.err.println("\n[FATAL ERROR] Seeding aborted due to exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    public static void executeResetAndSeed() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Take snapshot of all existing ADMIN accounts
                System.out.println("\n[Step 1/8] Capturing pre-reset snapshot of ADMIN accounts...");
                List<AdminSnapshot> adminSnapshots = new ArrayList<>();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT id, username, password_hash, role, student_id, active FROM users WHERE role = 'ADMIN'")) {
                    while (rs.next()) {
                        AdminSnapshot snap = new AdminSnapshot();
                        snap.id = rs.getInt("id");
                        snap.username = rs.getString("username");
                        snap.passwordHash = rs.getString("password_hash");
                        snap.role = rs.getString("role");
                        int sid = rs.getInt("student_id");
                        snap.studentId = rs.wasNull() ? null : sid;
                        snap.active = rs.getBoolean("active");
                        adminSnapshots.add(snap);
                        System.out.println("  -> Recorded Admin: " + snap.username + " (ID: " + snap.id + ", Active: " + snap.active + ")");
                    }
                }

                if (adminSnapshots.isEmpty()) {
                    throw new IllegalStateException("CRITICAL ERROR: No ADMIN account found in users table. Aborting reset!");
                }

                // 2. Perform clean deletion in strict foreign key order (ONLY non-admin data)
                System.out.println("\n[Step 2/8] Deleting non-admin application data in strict FK order...");
                executeDelete(conn, "DELETE FROM exam_attempt_questions;");
                executeDelete(conn, "DELETE FROM exam_results;");
                executeDelete(conn, "DELETE FROM exam_attempts;");
                executeDelete(conn, "DELETE FROM exam_schedules;");
                executeDelete(conn, "DELETE FROM questions;");
                executeDelete(conn, "DELETE FROM users WHERE role = 'STUDENT' OR student_id IS NOT NULL;");
                executeDelete(conn, "DELETE FROM students;");
                executeDelete(conn, "DELETE FROM subjects;");

                // 3. Verify clean table state
                System.out.println("\n[Step 3/8] Verifying tables are emptied and admin preserved...");
                assertCount(conn, "exam_attempt_questions", 0);
                assertCount(conn, "exam_results", 0);
                assertCount(conn, "exam_attempts", 0);
                assertCount(conn, "exam_schedules", 0);
                assertCount(conn, "questions", 0);
                assertCount(conn, "students", 0);
                assertCount(conn, "subjects", 0);
                assertCount(conn, "users", adminSnapshots.size());

                // Verify each admin is completely unchanged
                for (AdminSnapshot snap : adminSnapshots) {
                    try (PreparedStatement ps = conn.prepareStatement("SELECT id, username, password_hash, role, active FROM users WHERE id = ?")) {
                        ps.setInt(1, snap.id);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new IllegalStateException("Admin ID " + snap.id + " was unexpectedly deleted!");
                            }
                            if (!snap.username.equals(rs.getString("username")) ||
                                !snap.passwordHash.equals(rs.getString("password_hash")) ||
                                !snap.role.equals(rs.getString("role")) ||
                                snap.active != rs.getBoolean("active")) {
                                throw new IllegalStateException("Admin ID " + snap.id + " state was altered during reset!");
                            }
                        }
                    }
                }
                System.out.println("  -> Verified: All " + adminSnapshots.size() + " ADMIN accounts strictly preserved intact.");

                // 4. Safely realign sequences
                System.out.println("\n[Step 4/8] Safely aligning identity sequences...");
                // Align users sequence safely above highest preserved admin ID
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1));");
                    System.out.println("  -> users_id_seq safely set above existing admin IDs.");
                }

                // Restart sequences for emptied tables
                String[] emptyTables = {"students", "subjects", "questions", "exam_schedules", "exam_attempts", "exam_attempt_questions", "exam_results"};
                for (String tbl : emptyTables) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("ALTER TABLE " + tbl + " ALTER COLUMN id RESTART WITH 1;");
                    }
                }
                System.out.println("  -> Emptied tables identity sequences restarted at 1.");

                // 5. Insert 15 Academic Subjects
                System.out.println("\n[Step 5/8] Inserting 15 academic subjects...");
                String[] subjectNames = {
                        "Java Programming",
                        "Data Structures",
                        "Algorithms",
                        "Database Management Systems",
                        "Operating Systems",
                        "Computer Networks",
                        "Software Engineering",
                        "Object-Oriented Programming",
                        "Computer Architecture",
                        "Web Technologies",
                        "Artificial Intelligence",
                        "Machine Learning",
                        "Cloud Computing",
                        "Cyber Security",
                        "Distributed Systems"
                };

                int[] subjectIds = new int[subjectNames.length];
                for (int i = 0; i < subjectNames.length; i++) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO subjects (name, active) VALUES (?, true) RETURNING id")) {
                        ps.setString(1, subjectNames[i]);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                subjectIds[i] = rs.getInt(1);
                                System.out.println("  [" + (i + 1) + "/15] Subject #" + subjectIds[i] + ": " + subjectNames[i]);
                            }
                        }
                    }
                }

                // 6. Insert 750+ Questions across all 15 subjects (50 per subject)
                System.out.println("\n[Step 6/8] Inserting 750+ academic MCQs across 15 subjects (50 each)...");
                Map<Integer, List<Integer>> subjectQuestionMap = new HashMap<>();

                List<List<QuestionItem>> questionSets = new ArrayList<>();
                questionSets.add(QuestionBankPart1.getJavaQuestions());
                questionSets.add(QuestionBankPart1.getDataStructuresQuestions());
                questionSets.add(QuestionBankPart1.getAlgorithmsQuestions());
                questionSets.add(QuestionBankPart1.getDbmsQuestions());
                questionSets.add(QuestionBankPart1.getOperatingSystemsQuestions());

                questionSets.add(QuestionBankPart2.getComputerNetworksQuestions());
                questionSets.add(QuestionBankPart2.getSoftwareEngineeringQuestions());
                questionSets.add(QuestionBankPart2.getOopQuestions());
                questionSets.add(QuestionBankPart2.getComputerArchitectureQuestions());
                questionSets.add(QuestionBankPart2.getWebTechnologiesQuestions());

                questionSets.add(QuestionBankPart3.getAiQuestions());
                questionSets.add(QuestionBankPart3.getMlQuestions());
                questionSets.add(QuestionBankPart3.getCloudComputingQuestions());
                questionSets.add(QuestionBankPart3.getCyberSecurityQuestions());
                questionSets.add(QuestionBankPart3.getDistributedSystemsQuestions());

                int totalQuestionsSeeded = 0;
                for (int sIdx = 0; sIdx < subjectIds.length; sIdx++) {
                    int subId = subjectIds[sIdx];
                    List<QuestionItem> questions = questionSets.get(sIdx);
                    List<Integer> insertedQIds = new ArrayList<>();

                    for (QuestionItem q : questions) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO questions (subject_id, question_text, option1, option2, option3, option4, correct_answer, difficulty, active) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, true) RETURNING id")) {
                            ps.setInt(1, subId);
                            ps.setString(2, q.question);
                            ps.setString(3, q.optA);
                            ps.setString(4, q.optB);
                            ps.setString(5, q.optC);
                            ps.setString(6, q.optD);
                            ps.setString(7, q.correct);
                            ps.setString(8, q.difficulty);

                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    insertedQIds.add(rs.getInt(1));
                                    totalQuestionsSeeded++;
                                }
                            }
                        }
                    }
                    subjectQuestionMap.put(subId, insertedQIds);
                    System.out.println("  -> Subject #" + subId + " (" + subjectNames[sIdx] + "): Seeded " + insertedQIds.size() + " MCQs");
                }
                System.out.println("  -> Total Questions Seeded: " + totalQuestionsSeeded);

                // 7. Insert 20 Students (18 active, 2 inactive) and user accounts
                System.out.println("\n[Step 7/8] Inserting 20 students and user accounts (18 active, 2 inactive)...");
                String[][] studentData = {
                        {"Aarav Sharma", "9876543201", "aarav.sharma@example.com", "100020003001", "2001-05-15", "aaravs", "true"},
                        {"Ananya Verma", "9876543202", "ananya.verma@example.com", "100020003002", "2002-08-21", "ananyav", "true"},
                        {"Rohan Patel", "9876543203", "rohan.patel@example.com", "100020003003", "2000-11-10", "rohanp", "true"},
                        {"Priya Nair", "9876543204", "priya.nair@example.com", "100020003004", "2001-02-18", "priyan", "true"},
                        {"Vikram Singh", "9876543205", "vikram.singh@example.com", "100020003005", "1999-12-05", "vikrams", "true"},
                        {"Sneha Rao", "9876543206", "sneha.rao@example.com", "100020003006", "2002-04-30", "snehar", "true"},
                        {"Aditya Gupta", "9876543207", "aditya.gupta@example.com", "100020003007", "2001-09-12", "adityag", "true"},
                        {"Pooja Joshi", "9876543208", "pooja.joshi@example.com", "100020003008", "2000-07-25", "poojaj", "true"},
                        {"Rahul Mehta", "9876543209", "rahul.mehta@example.com", "100020003009", "2001-03-14", "rahulm", "true"},
                        {"Neha Kulkarni", "9876543210", "neha.kulkarni@example.com", "100020003010", "2002-01-09", "nehak", "true"},
                        {"Siddharth Malhotra", "9876543211", "siddharth.m@example.com", "100020003011", "2000-10-22", "siddharthm", "true"},
                        {"Tanvi Deshmukh", "9876543212", "tanvi.d@example.com", "100020003012", "2001-06-19", "tanvid", "true"},
                        {"Arjun Reddy", "9876543213", "arjun.reddy@example.com", "100020003013", "2002-12-01", "arjunr", "true"},
                        {"Kavya Iyer", "9876543214", "kavya.iyer@example.com", "100020003014", "2001-07-07", "kavyai", "true"},
                        {"Manish Tiwari", "9876543215", "manish.tiwari@example.com", "100020003015", "2000-04-16", "manisht", "true"},
                        {"Ritu Agarwal", "9876543216", "ritu.agarwal@example.com", "100020003016", "2002-03-28", "ritua", "true"},
                        {"Harsh Vardhan", "9876543217", "harsh.vardhan@example.com", "100020003017", "2001-11-03", "harshv", "true"},
                        {"Divya Pillai", "9876543218", "divya.pillai@example.com", "100020003018", "2000-09-17", "divyap", "true"},
                        {"Kunal Saxena", "9876543219", "kunal.saxena@example.com", "100020003019", "2001-08-11", "kunals", "false"}, // Inactive
                        {"Shreya Bhattacharya", "9876543220", "shreya.b@example.com", "100020003020", "2002-05-24", "shreyab", "false"} // Inactive
                };

                int[] studentIds = new int[studentData.length];
                String commonPasswordHash = PasswordHasher.hashPassword("Password@123");

                for (int i = 0; i < studentData.length; i++) {
                    String name = studentData[i][0];
                    String mobile = studentData[i][1];
                    String email = studentData[i][2];
                    String aadhar = studentData[i][3];
                    String dob = studentData[i][4];
                    String username = studentData[i][5];
                    boolean active = Boolean.parseBoolean(studentData[i][6]);

                    // Insert student
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO students (name, mobile, email, aadhar_no, date_of_birth, active) VALUES (?, ?, ?, ?, ?, ?) RETURNING id")) {
                        ps.setString(1, name);
                        ps.setString(2, mobile);
                        ps.setString(3, email);
                        ps.setString(4, aadhar);
                        ps.setString(5, dob);
                        ps.setBoolean(6, active);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                studentIds[i] = rs.getInt(1);
                            }
                        }
                    }

                    // Insert user account
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO users (username, password_hash, role, student_id, active) VALUES (?, ?, 'STUDENT', ?, ?)")) {
                        ps.setString(1, username);
                        ps.setString(2, commonPasswordHash);
                        ps.setInt(3, studentIds[i]);
                        ps.setBoolean(4, active);
                        ps.executeUpdate();
                    }

                    System.out.println("  [" + (i + 1) + "/20] Student #" + studentIds[i] + " (" + name + ") -> User: " + username + " (Active: " + active + ")");
                }

                // 8. Insert 35 Exam Schedules, 18 Attempts, and 12 Results
                System.out.println("\n[Step 8/8] Generating 35 exam schedules, 18 attempts, and 12 completed results...");

                // Definition of 35 Exam Schedules
                // Columns: studentIndex (0-19), subjectIndex (0-14), durationMins, totalQuestions, passingPercentage, status
                Object[][] scheduleConfigs = {
                        // Schedules 0 - 11 (Will be COMPLETED with 12 Results)
                        {0, 0, 30, 10, 50.0, "COMPLETED"}, // Student 1 (Aarav) on Java -> Pass (100%)
                        {0, 3, 45, 10, 50.0, "COMPLETED"}, // Student 1 (Aarav) on DBMS -> Pass (90%)
                        {1, 1, 30, 10, 50.0, "COMPLETED"}, // Student 2 (Ananya) on Data Structures -> Pass (80%)
                        {2, 2, 45, 10, 60.0, "COMPLETED"}, // Student 3 (Rohan) on Algorithms -> Pass (70%)
                        {3, 4, 30, 10, 50.0, "COMPLETED"}, // Student 4 (Priya) on Operating Systems -> Pass (60%)
                        {4, 5, 60, 10, 50.0, "COMPLETED"}, // Student 5 (Vikram) on Computer Networks -> Pass (50%)
                        {5, 6, 45, 10, 60.0, "COMPLETED"}, // Student 6 (Sneha) on Software Engineering -> Fail (40%)
                        {6, 7, 30, 10, 50.0, "COMPLETED"}, // Student 7 (Aditya) on OOP -> Fail (30%)
                        {7, 8, 45, 10, 50.0, "COMPLETED"}, // Student 8 (Pooja) on Computer Architecture -> Fail (20%)
                        {8, 9, 30, 10, 40.0, "COMPLETED"}, // Student 9 (Rahul) on Web Technologies -> Pass (80%)
                        {9, 10, 60, 10, 50.0, "COMPLETED"}, // Student 10 (Neha) on AI -> Pass (90%)
                        {10, 11, 45, 10, 50.0, "COMPLETED"}, // Student 11 (Siddharth) on Machine Learning -> Fail (30%)

                        // Schedules 12 - 17 (Will be IN_PROGRESS with 6 Attempts)
                        {1, 5, 45, 20, 50.0, "IN_PROGRESS"}, // Student 2 on Computer Networks
                        {2, 0, 30, 20, 50.0, "IN_PROGRESS"}, // Student 3 on Java
                        {3, 11, 60, 20, 60.0, "IN_PROGRESS"}, // Student 4 on ML
                        {4, 12, 45, 20, 50.0, "IN_PROGRESS"}, // Student 5 on Cloud Computing
                        {11, 13, 60, 30, 50.0, "IN_PROGRESS"}, // Student 12 (Tanvi) on Cyber Security
                        {12, 14, 90, 50, 60.0, "IN_PROGRESS"}, // Student 13 (Arjun) on Distributed Systems

                        // Schedules 18 - 32 (SCHEDULED future exams)
                        {0, 6, 45, 20, 50.0, "SCHEDULED"},  // Student 1 on Software Engineering
                        {1, 9, 30, 20, 50.0, "SCHEDULED"},  // Student 2 on Web Technologies
                        {5, 0, 30, 20, 50.0, "SCHEDULED"},  // Student 6 on Java
                        {6, 1, 45, 20, 50.0, "SCHEDULED"},  // Student 7 on Data Structures
                        {7, 2, 60, 30, 60.0, "SCHEDULED"},  // Student 8 on Algorithms
                        {8, 3, 45, 20, 50.0, "SCHEDULED"},  // Student 9 on DBMS
                        {9, 4, 30, 20, 50.0, "SCHEDULED"},  // Student 10 on Operating Systems
                        {10, 5, 45, 20, 50.0, "SCHEDULED"}, // Student 11 on Networks
                        {11, 7, 30, 20, 50.0, "SCHEDULED"}, // Student 12 on OOP
                        {12, 8, 45, 20, 50.0, "SCHEDULED"}, // Student 13 on Computer Architecture
                        {13, 9, 30, 20, 40.0, "SCHEDULED"}, // Student 14 (Kavya) on Web Technologies
                        {14, 10, 60, 30, 50.0, "SCHEDULED"}, // Student 15 (Manish) on AI
                        {15, 12, 45, 20, 50.0, "SCHEDULED"}, // Student 16 (Ritu) on Cloud Computing
                        {16, 13, 60, 30, 50.0, "SCHEDULED"}, // Student 17 (Harsh) on Cyber Security
                        {17, 14, 90, 50, 70.0, "SCHEDULED"}, // Student 18 (Divya) on Distributed Systems

                        // Schedules 33 - 34 (CANCELLED)
                        {13, 0, 30, 20, 50.0, "CANCELLED"}, // Student 14 on Java (Cancelled)
                        {14, 3, 45, 20, 50.0, "CANCELLED"}  // Student 15 on DBMS (Cancelled)
                };

                int[] scheduleIds = new int[scheduleConfigs.length];
                long now = System.currentTimeMillis();

                for (int i = 0; i < scheduleConfigs.length; i++) {
                    int stIdx = (Integer) scheduleConfigs[i][0];
                    int subIdx = (Integer) scheduleConfigs[i][1];
                    int duration = (Integer) scheduleConfigs[i][2];
                    int totalQ = (Integer) scheduleConfigs[i][3];
                    double passPct = (Double) scheduleConfigs[i][4];
                    String status = (String) scheduleConfigs[i][5];

                    int studentId = studentIds[stIdx];
                    int subjectId = subjectIds[subIdx];

                    Timestamp scheduleTime;
                    if ("COMPLETED".equals(status)) {
                        scheduleTime = new Timestamp(now - (86400000L * (i + 1))); // 1 to 12 days ago
                    } else if ("IN_PROGRESS".equals(status)) {
                        scheduleTime = new Timestamp(now - 1800000L); // 30 mins ago
                    } else {
                        scheduleTime = new Timestamp(now + (86400000L * (i - 17))); // future
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO exam_schedules (student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id")) {
                        ps.setInt(1, studentId);
                        ps.setInt(2, subjectId);
                        ps.setInt(3, duration);
                        ps.setInt(4, totalQ);
                        ps.setDouble(5, passPct);
                        ps.setString(6, status);
                        ps.setTimestamp(7, scheduleTime);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                scheduleIds[i] = rs.getInt(1);
                            }
                        }
                    }
                }
                System.out.println("  -> Created 35 Exam Schedules across 15 subjects and 20 students.");

                // 9. Generate 18 Exam Attempts & Attempt Questions
                // Target correct answers for the 12 completed attempts (out of 10 questions)
                int[] correctAnswersFor12 = {10, 9, 8, 7, 6, 5, 4, 3, 2, 8, 9, 3};
                // Scores: 100%, 90%, 80%, 70%, 60%, 50%, 40%, 30%, 20%, 80%, 90%, 30%
                // Results: Pass, Pass, Pass, Pass, Pass, Pass, Fail, Fail, Fail, Pass, Pass, Fail

                int[] attemptIds = new int[18];

                // A. 12 Completed Attempts
                for (int i = 0; i < 12; i++) {
                    int schId = scheduleIds[i];
                    int stIdx = (Integer) scheduleConfigs[i][0];
                    int subIdx = (Integer) scheduleConfigs[i][1];
                    int studentId = studentIds[stIdx];
                    int subjectId = subjectIds[subIdx];
                    int totalQ = (Integer) scheduleConfigs[i][3];
                    double passPct = (Double) scheduleConfigs[i][4];
                    int numCorrect = correctAnswersFor12[i];

                    Timestamp startTs = new Timestamp(now - (86400000L * (i + 1)));
                    Timestamp completeTs = new Timestamp(startTs.getTime() + (1800000L)); // 30 mins later

                    // Insert Attempt
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO exam_attempts (exam_schedule_id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, started_at, completed_at, remaining_seconds) " +
                            "VALUES (?, ?, ?, 30, ?, ?, 'COMPLETED', ?, ?, 0) RETURNING id")) {
                        ps.setInt(1, schId);
                        ps.setInt(2, studentId);
                        ps.setInt(3, subjectId);
                        ps.setInt(4, totalQ);
                        ps.setDouble(5, passPct);
                        ps.setTimestamp(6, startTs);
                        ps.setTimestamp(7, completeTs);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                attemptIds[i] = rs.getInt(1);
                            }
                        }
                    }

                    // Allocate Questions in exam_attempt_questions
                    List<Integer> qPool = subjectQuestionMap.get(subjectId);
                    for (int qIdx = 0; qIdx < totalQ; qIdx++) {
                        int questionId = qPool.get(qIdx);
                        boolean isCorrect = qIdx < numCorrect;

                        // Query correct answer
                        String corrAns = "";
                        String wrongAns = "";
                        try (PreparedStatement qps = conn.prepareStatement("SELECT option1, option2, option3, option4, correct_answer FROM questions WHERE id = ?")) {
                            qps.setInt(1, questionId);
                            try (ResultSet qrs = qps.executeQuery()) {
                                if (qrs.next()) {
                                    corrAns = qrs.getString("correct_answer");
                                    String opt1 = qrs.getString("option1");
                                    String opt2 = qrs.getString("option2");
                                    wrongAns = corrAns.equals(opt1) ? opt2 : opt1;
                                }
                            }
                        }

                        String selectedOption = isCorrect ? corrAns : wrongAns;

                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO exam_attempt_questions (attempt_id, question_id, question_order, selected_option, is_correct) " +
                                "VALUES (?, ?, ?, ?, ?)")) {
                            ps.setInt(1, attemptIds[i]);
                            ps.setInt(2, questionId);
                            ps.setInt(3, qIdx + 1);
                            ps.setString(4, selectedOption);
                            ps.setBoolean(5, isCorrect);
                            ps.executeUpdate();
                        }
                    }

                    // Insert corresponding Result
                    double marks = (double) numCorrect;
                    double percentage = (marks / totalQ) * 100.0;
                    String resultStatus = percentage >= passPct ? "Pass" : "Fail";

                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO exam_results (attempt_id, exam_schedule_id, student_id, subject_id, total_questions, correct_answers, marks, percentage, result, started_at, submitted_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, attemptIds[i]);
                        ps.setInt(2, schId);
                        ps.setInt(3, studentId);
                        ps.setInt(4, subjectId);
                        ps.setInt(5, totalQ);
                        ps.setInt(6, numCorrect);
                        ps.setDouble(7, marks);
                        ps.setDouble(8, percentage);
                        ps.setString(9, resultStatus);
                        ps.setTimestamp(10, startTs);
                        ps.setTimestamp(11, completeTs);
                        ps.executeUpdate();
                    }
                    System.out.println("  -> Result #" + (i + 1) + ": Student #" + studentId + " on Subject #" + subjectId + " -> " + numCorrect + "/" + totalQ + " (" + percentage + "%, " + resultStatus + ")");
                }

                // B. 6 In-Progress Attempts (Schedules 12 - 17)
                for (int i = 12; i < 18; i++) {
                    int schId = scheduleIds[i];
                    int stIdx = (Integer) scheduleConfigs[i][0];
                    int subIdx = (Integer) scheduleConfigs[i][1];
                    int studentId = studentIds[stIdx];
                    int subjectId = subjectIds[subIdx];
                    int totalQ = (Integer) scheduleConfigs[i][3];
                    int duration = (Integer) scheduleConfigs[i][2];
                    double passPct = (Double) scheduleConfigs[i][4];

                    Timestamp startTs = new Timestamp(now - 600000L); // 10 mins ago
                    int remainingSecs = (duration * 60) - 600;

                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO exam_attempts (exam_schedule_id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, started_at, remaining_seconds) " +
                            "VALUES (?, ?, ?, ?, ?, ?, 'IN_PROGRESS', ?, ?) RETURNING id")) {
                        ps.setInt(1, schId);
                        ps.setInt(2, studentId);
                        ps.setInt(3, subjectId);
                        ps.setInt(4, duration);
                        ps.setInt(5, totalQ);
                        ps.setDouble(6, passPct);
                        ps.setTimestamp(7, startTs);
                        ps.setInt(8, remainingSecs);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                attemptIds[i] = rs.getInt(1);
                            }
                        }
                    }

                    // Allocate questions with partial answers
                    List<Integer> qPool = subjectQuestionMap.get(subjectId);
                    for (int qIdx = 0; qIdx < totalQ; qIdx++) {
                        int questionId = qPool.get(qIdx);
                        boolean answered = qIdx < 5; // First 5 answered
                        String selectedOption = answered ? "Option A" : null;

                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO exam_attempt_questions (attempt_id, question_id, question_order, selected_option, is_correct) " +
                                "VALUES (?, ?, ?, ?, false)")) {
                            ps.setInt(1, attemptIds[i]);
                            ps.setInt(2, questionId);
                            ps.setInt(3, qIdx + 1);
                            ps.setString(4, selectedOption);
                            ps.executeUpdate();
                        }
                    }
                    System.out.println("  -> In-Progress Attempt #" + attemptIds[i] + ": Schedule #" + schId + ", Student #" + studentId + ", Subject #" + subjectId + " (" + totalQ + " Qs, " + remainingSecs + "s remaining)");
                }

                // 10. Post-Seeding Integrity Verification Assertions
                System.out.println("\n[Verification] Validating seeded counts and relational integrity...");
                assertCount(conn, "students", 20);
                assertCount(conn, "users", adminSnapshots.size() + 20);
                assertCount(conn, "subjects", 15);
                assertMinimumCount(conn, "questions", 750);
                assertCount(conn, "exam_schedules", 35);
                assertCount(conn, "exam_attempts", 18);
                assertCount(conn, "exam_results", 12);

                // Check for 0 orphan records
                assertZero(conn, "SELECT COUNT(*) FROM exam_schedules WHERE student_id NOT IN (SELECT id FROM students)", "Orphan exam_schedules (student)");
                assertZero(conn, "SELECT COUNT(*) FROM exam_schedules WHERE subject_id NOT IN (SELECT id FROM subjects)", "Orphan exam_schedules (subject)");
                assertZero(conn, "SELECT COUNT(*) FROM exam_attempts WHERE exam_schedule_id NOT IN (SELECT id FROM exam_schedules)", "Orphan exam_attempts (schedule)");
                assertZero(conn, "SELECT COUNT(*) FROM exam_attempt_questions WHERE attempt_id NOT IN (SELECT id FROM exam_attempts)", "Orphan exam_attempt_questions (attempt)");
                assertZero(conn, "SELECT COUNT(*) FROM exam_attempt_questions WHERE question_id NOT IN (SELECT id FROM questions)", "Orphan exam_attempt_questions (question)");
                assertZero(conn, "SELECT COUNT(*) FROM exam_results WHERE attempt_id IS NOT NULL AND attempt_id NOT IN (SELECT id FROM exam_attempts)", "Orphan exam_results (attempt)");
                assertZero(conn, "SELECT COUNT(*) FROM users WHERE student_id IS NOT NULL AND student_id NOT IN (SELECT id FROM students)", "Orphan users (student)");

                // Commit single atomic transaction
                conn.commit();
                System.out.println("\n[TRANSACTION COMMITTED] All assertions passed with 0 errors! Database is fresh and consistent.");

            } catch (Exception e) {
                conn.rollback();
                System.err.println("\n[TRANSACTION ROLLED BACK] An error occurred during reset/seed: " + e.getMessage());
                throw e;
            }
        }
    }

    private static void executeDelete(Connection conn, String sql) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate(sql);
            System.out.println("  -> Executed: " + sql + " (Affected rows: " + rows + ")");
        }
    }

    private static void assertCount(Connection conn, String table, int expected) throws Exception {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count != expected) {
                    throw new IllegalStateException("Assertion failure on " + table + ": expected " + expected + ", found " + count);
                }
                System.out.println("  [VERIFIED] " + table + ": " + count + " rows (Expected: " + expected + ")");
            }
        }
    }

    private static void assertMinimumCount(Connection conn, String table, int minimum) throws Exception {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count < minimum) {
                    throw new IllegalStateException("Assertion failure on " + table + ": expected >= " + minimum + ", found " + count);
                }
                System.out.println("  [VERIFIED] " + table + ": " + count + " rows (Expected >= " + minimum + ")");
            }
        }
    }

    private static void assertZero(Connection conn, String query, String description) throws Exception {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count != 0) {
                    throw new IllegalStateException("Assertion failure: " + description + " count is " + count + ", expected 0");
                }
                System.out.println("  [VERIFIED] " + description + ": 0 violations.");
            }
        }
    }
}
