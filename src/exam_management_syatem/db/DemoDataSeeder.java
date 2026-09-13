package exam_management_syatem.db;

import exam_management_syatem.security.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoDataSeeder {

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("       EXAM MANAGEMENT SYSTEM — REAL DEMO DATA SEEDER     ");
        System.out.println("=========================================================");

        try {
            DatabaseManager.initializeDatabase();
            seedDemoData();
            System.out.println("\n[SUCCESS] Demo data seeding completed successfully.");
        } catch (Throwable t) {
            System.err.println("\n[ERROR] Seeding failed:");
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static void seedDemoData() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                System.out.println("--> Seeding Subjects (at least 5 subjects, including inactive)...");
                Map<String, Integer> subjectIds = seedSubjects(conn);

                System.out.println("--> Seeding 50 Technical Questions (10 per active subject)...");
                Map<String, List<Integer>> questionIds = seedQuestions(conn, subjectIds);

                System.out.println("--> Seeding Students & User Accounts (11 students, active and inactive)...");
                Map<String, Integer> studentIds = seedStudentsAndUsers(conn);

                System.out.println("--> Seeding Exam Schedules (Multiple scenarios, multiple exams per student)...");
                Map<String, Integer> scheduleIds = seedSchedules(conn, studentIds, subjectIds);

                System.out.println("--> Seeding Exam Attempts & Attempt Questions...");
                Map<String, Integer> attemptIds = seedAttempts(conn, scheduleIds, studentIds, subjectIds, questionIds);

                System.out.println("--> Seeding Exam Results (PASS and FAIL scenarios)...");
                seedResults(conn, attemptIds, scheduleIds, studentIds, subjectIds);

                conn.commit();
                System.out.println("--> Transaction committed to PostgreSQL database.");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static Map<String, Integer> seedSubjects(Connection conn) throws Exception {
        Map<String, Integer> ids = new HashMap<>();

        String[][] subjects = {
                {"TEST Java Programming", "true"},
                {"TEST Database Systems", "true"},
                {"TEST Data Structures & Algorithms", "true"},
                {"TEST Computer Networks", "true"},
                {"TEST Operating Systems", "true"},
                {"TEST Software Engineering (Archived)", "false"}
        };

        for (String[] sub : subjects) {
            String name = sub[0];
            boolean active = Boolean.parseBoolean(sub[1]);

            int id = -1;
            try (PreparedStatement check = conn.prepareStatement("SELECT id FROM subjects WHERE name = ?")) {
                check.setString(1, name);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }
                }
            }

            if (id == -1) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO subjects (name, active) VALUES (?, ?) RETURNING id")) {
                    ins.setString(1, name);
                    ins.setBoolean(2, active);
                    try (ResultSet rs = ins.executeQuery()) {
                        if (rs.next()) id = rs.getInt(1);
                    }
                }
                System.out.println("    + Created subject: " + name + " (ID: " + id + ", Active: " + active + ")");
            } else {
                System.out.println("    . Existing subject: " + name + " (ID: " + id + ")");
            }
            ids.put(name, id);
        }

        return ids;
    }

    private static Map<String, List<Integer>> seedQuestions(Connection conn, Map<String, Integer> subjectIds) throws Exception {
        Map<String, List<Integer>> ids = new HashMap<>();

        // 10 questions for TEST Java Programming
        String[][] javaQ = {
                {"What is the size of an int data type in Java?", "16-bit", "32-bit", "64-bit", "8-bit", "32-bit", "EASY"},
                {"Which of these is NOT a Java keyword?", "static", "Boolean", "void", "private", "Boolean", "EASY"},
                {"Which collection class allows unique elements only?", "ArrayList", "LinkedList", "HashSet", "Vector", "HashSet", "MEDIUM"},
                {"Which exception is thrown when an array is accessed with an illegal index?", "NullPointerException", "ArrayIndexOutOfBoundsException", "ArithmeticException", "ClassCastException", "ArrayIndexOutOfBoundsException", "EASY"},
                {"What does the final keyword prevent on a method?", "Overloading", "Overriding", "Execution", "Compilation", "Overriding", "MEDIUM"},
                {"Which interface must be implemented for thread execution?", "Runnable", "Cloneable", "Serializable", "Comparable", "Runnable", "EASY"},
                {"What is the default value of a boolean variable in a class in Java?", "true", "false", "null", "0", "false", "EASY"},
                {"Which garbage collection algorithm uses mark-and-sweep?", "ZGC", "Serial GC", "CMS", "G1 GC", "CMS", "HARD"},
                {"Which method starts a thread in Java?", "run()", "start()", "execute()", "init()", "start()", "EASY"},
                {"What is the parent class of all classes in Java?", "java.lang.Class", "java.lang.Object", "java.lang.System", "java.lang.Runtime", "java.lang.Object", "EASY"}
        };

        // 10 questions for TEST Database Systems
        String[][] dbmsQ = {
                {"What does SQL stand for?", "Simple Query Language", "Structured Query Language", "Standard Query Logic", "System Query Language", "Structured Query Language", "EASY"},
                {"Which normal form eliminates partial dependency?", "1NF", "2NF", "3NF", "BCNF", "2NF", "MEDIUM"},
                {"Which SQL clause is used to filter group results?", "WHERE", "ORDER BY", "HAVING", "GROUP BY", "HAVING", "MEDIUM"},
                {"What type of join returns all rows from both tables?", "INNER JOIN", "FULL OUTER JOIN", "LEFT JOIN", "CROSS JOIN", "FULL OUTER JOIN", "EASY"},
                {"Which property of ACID ensures durability after commit?", "Atomicity", "Consistency", "Isolation", "Durability", "Durability", "EASY"},
                {"What index data structure is commonly used in relational databases?", "Binary Heap", "B-Tree / B+Tree", "Trie", "Hash Table Only", "B-Tree / B+Tree", "HARD"},
                {"Which command removes all rows from a table without logging individual row deletions?", "DELETE", "DROP", "TRUNCATE", "REMOVE", "TRUNCATE", "MEDIUM"},
                {"What is a candidate key with no null values chosen by the DBA called?", "Foreign Key", "Super Key", "Primary Key", "Composite Key", "Primary Key", "EASY"},
                {"Which transaction isolation level prevents dirty reads but allows non-repeatable reads?", "Read Uncommitted", "Read Committed", "Repeatable Read", "Serializable", "Read Committed", "HARD"},
                {"Which constraint ensures that all values in a column are distinct?", "NOT NULL", "CHECK", "UNIQUE", "DEFAULT", "UNIQUE", "EASY"}
        };

        // 10 questions for TEST Data Structures & Algorithms
        String[][] dsaQ = {
                {"What is the worst-case time complexity of QuickSort?", "O(n)", "O(n log n)", "O(n^2)", "O(log n)", "O(n^2)", "MEDIUM"},
                {"Which data structure operates on LIFO (Last In First Out)?", "Queue", "Stack", "Priority Queue", "Deque", "Stack", "EASY"},
                {"What is the time complexity of searching in a balanced Binary Search Tree (AVL)?", "O(1)", "O(log n)", "O(n)", "O(n log n)", "O(log n)", "EASY"},
                {"Which algorithm finds the shortest path in a graph with non-negative edge weights?", "Bellman-Ford", "Dijkstra", "Floyd-Warshall", "Prim", "Dijkstra", "MEDIUM"},
                {"What data structure is used to implement Breadth-First Search (BFS)?", "Stack", "Queue", "Tree", "Graph", "Queue", "EASY"},
                {"What is the space complexity of merge sort?", "O(1)", "O(log n)", "O(n)", "O(n^2)", "O(n)", "MEDIUM"},
                {"Which data structure is ideal for implementing LRU cache?", "Array", "Doubly Linked List with Hash Map", "Binary Search Tree", "Stack with Array", "Doubly Linked List with Hash Map", "HARD"},
                {"What is the worst-case time complexity of lookup in a Hash Table?", "O(1)", "O(log n)", "O(n)", "O(n log n)", "O(n)", "MEDIUM"},
                {"Which tree data structure guarantees self-balancing by red/black node color invariants?", "B-Tree", "Red-Black Tree", "Segment Tree", "Fenwick Tree", "Red-Black Tree", "MEDIUM"},
                {"What algorithmic technique does dynamic programming utilize to avoid redundant computations?", "Divide & Conquer", "Memoization", "Greedy Choice", "Backtracking", "Memoization", "EASY"}
        };

        // 10 questions for TEST Computer Networks
        String[][] netQ = {
                {"Which layer of the OSI model does IP (Internet Protocol) operate on?", "Data Link", "Network", "Transport", "Session", "Network", "EASY"},
                {"What is the default port number for HTTPS?", "80", "443", "8080", "22", "443", "EASY"},
                {"Which transport protocol is connection-oriented and reliable?", "UDP", "TCP", "ICMP", "IGMP", "TCP", "EASY"},
                {"What does DNS stand for?", "Data Network System", "Domain Name System", "Distributed Network Service", "Dynamic Name Standard", "Domain Name System", "EASY"},
                {"Which protocol maps an IP address to a physical MAC address?", "DHCP", "ARP", "RARP", "NAT", "ARP", "MEDIUM"},
                {"What is the maximum size of a standard Ethernet MTU in bytes?", "512", "1024", "1500", "4096", "1500", "MEDIUM"},
                {"Which protocol is used by ping utility?", "TCP", "UDP", "ICMP", "ARP", "ICMP", "EASY"},
                {"In subnetting, what is the broadcast address for 192.168.1.0/24?", "192.168.1.0", "192.168.1.1", "192.168.1.255", "192.168.1.254", "192.168.1.255", "MEDIUM"},
                {"Which routing protocol uses the Bellman-Ford distance-vector algorithm?", "OSPF", "BGP", "RIP", "IS-IS", "RIP", "HARD"},
                {"Which HTTP status code indicates 'Not Found'?", "400", "401", "403", "404", "404", "EASY"}
        };

        // 10 questions for TEST Operating Systems
        String[][] osQ = {
                {"What is a program in execution called?", "Thread", "Process", "Instruction", "Daemon", "Process", "EASY"},
                {"Which scheduling algorithm is non-preemptive and selects the process that arrived first?", "Round Robin", "FCFS (First-Come, First-Served)", "Shortest Job First", "Priority Scheduling", "FCFS (First-Come, First-Served)", "EASY"},
                {"Which of these is NOT one of the 4 Coffman conditions for deadlock?", "Mutual Exclusion", "Hold and Wait", "Preemption Allowed", "Circular Wait", "Preemption Allowed", "MEDIUM"},
                {"What hardware component translates virtual addresses to physical addresses?", "ALU", "MMU (Memory Management Unit)", "Cache Controller", "DMA Controller", "MMU (Memory Management Unit)", "MEDIUM"},
                {"What system call creates a new child process in UNIX/Linux?", "exec()", "fork()", "spawn()", "create()", "fork()", "EASY"},
                {"What technique brings pages into memory only when they are needed during execution?", "Demand Paging", "Swapping", "Segmentation", "Paging", "Demand Paging", "MEDIUM"},
                {"What is the phenomenon where excessive page faults slow down system execution called?", "Fragmentation", "Thrashing", "Deadlock", "Starvation", "Thrashing", "MEDIUM"},
                {"Which synchronization primitive uses an integer variable accessed through wait() and signal()?", "Spinlock", "Semaphore", "Mutex", "Barrier", "Semaphore", "EASY"},
                {"What is the first user-space process started by the Linux kernel (PID 1)?", "systemd / init", "bash", "login", "sshd", "systemd / init", "EASY"},
                {"Which disk scheduling algorithm services requests in a single sweeping direction then reverses?", "FCFS", "SSTF", "SCAN (Elevator)", "LOOK", "SCAN (Elevator)", "HARD"}
        };

        Map<String, String[][]> questionsBySubject = new HashMap<>();
        questionsBySubject.put("TEST Java Programming", javaQ);
        questionsBySubject.put("TEST Database Systems", dbmsQ);
        questionsBySubject.put("TEST Data Structures & Algorithms", dsaQ);
        questionsBySubject.put("TEST Computer Networks", netQ);
        questionsBySubject.put("TEST Operating Systems", osQ);

        for (Map.Entry<String, String[][]> entry : questionsBySubject.entrySet()) {
            String subName = entry.getKey();
            int subId = subjectIds.get(subName);
            List<Integer> qList = new ArrayList<>();

            for (String[] qData : entry.getValue()) {
                String text = qData[0];
                String o1 = qData[1];
                String o2 = qData[2];
                String o3 = qData[3];
                String o4 = qData[4];
                String ans = qData[5];
                String diff = qData[6];

                int qId = -1;
                try (PreparedStatement check = conn.prepareStatement(
                        "SELECT id FROM questions WHERE subject_id = ? AND question_text = ?")) {
                    check.setInt(1, subId);
                    check.setString(2, text);
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) qId = rs.getInt(1);
                    }
                }

                if (qId == -1) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO questions (subject_id, question_text, option1, option2, option3, option4, correct_answer, difficulty, active) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE) RETURNING id")) {
                        ins.setInt(1, subId);
                        ins.setString(2, text);
                        ins.setString(3, o1);
                        ins.setString(4, o2);
                        ins.setString(5, o3);
                        ins.setString(6, o4);
                        ins.setString(7, ans);
                        ins.setString(8, diff);
                        try (ResultSet rs = ins.executeQuery()) {
                            if (rs.next()) qId = rs.getInt(1);
                        }
                    }
                }
                qList.add(qId);
            }
            ids.put(subName, qList);
            System.out.println("    + Ready: " + subName + " (" + qList.size() + " questions)");
        }

        return ids;
    }

    private static Map<String, Integer> seedStudentsAndUsers(Connection conn) throws Exception {
        Map<String, Integer> studentIds = new HashMap<>();

        // 11 test students: 10 active, 1 inactive
        String[][] studentDefs = {
                {"TEST Student 01", "9000000001", "test.student01@example.com", "900000000001", "15012002", "teststudent01", "true"},
                {"TEST Student 02", "9000000002", "test.student02@example.com", "900000000002", "20022002", "teststudent02", "true"},
                {"TEST Student 03", "9000000003", "test.student03@example.com", "900000000003", "10032002", "teststudent03", "true"},
                {"TEST Student 04", "9000000004", "test.student04@example.com", "900000000004", "25042002", "teststudent04", "true"},
                {"TEST Student 05", "9000000005", "test.student05@example.com", "900000000005", "05052002", "teststudent05", "true"},
                {"TEST Student 06", "9000000006", "test.student06@example.com", "900000000006", "12062002", "teststudent06", "true"},
                {"TEST Student 07", "9000000007", "test.student07@example.com", "900000000007", "18072002", "teststudent07", "true"},
                {"TEST Student 08", "9000000008", "test.student08@example.com", "900000000008", "22082002", "teststudent08", "true"},
                {"TEST Student 09", "9000000009", "test.student09@example.com", "900000000009", "09092002", "teststudent09", "true"},
                {"TEST Student 10", "9000000010", "test.student10@example.com", "900000000010", "30102002", "teststudent10", "true"},
                {"TEST Student 11 (Inactive)", "9000000011", "test.student11@example.com", "900000000011", "01112002", "teststudent11", "false"}
        };

        String defaultTestPassword = "Password@123";
        String passwordHash = PasswordHasher.hashPassword(defaultTestPassword);

        for (String[] def : studentDefs) {
            String name = def[0];
            String mobile = def[1];
            String email = def[2];
            String aadhar = def[3];
            String dob = def[4];
            String username = def[5];
            boolean active = Boolean.parseBoolean(def[6]);

            int sId = -1;
            try (PreparedStatement check = conn.prepareStatement("SELECT id FROM students WHERE aadhar_no = ?")) {
                check.setString(1, aadhar);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) sId = rs.getInt(1);
                }
            }

            if (sId == -1) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO students (name, mobile, email, aadhar_no, date_of_birth, active) " +
                                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id")) {
                    ins.setString(1, name);
                    ins.setString(2, mobile);
                    ins.setString(3, email);
                    ins.setString(4, aadhar);
                    ins.setString(5, dob);
                    ins.setBoolean(6, active);
                    try (ResultSet rs = ins.executeQuery()) {
                        if (rs.next()) sId = rs.getInt(1);
                    }
                }
                System.out.println("    + Created student: " + name + " (ID: " + sId + ", Active: " + active + ")");
            } else {
                System.out.println("    . Existing student: " + name + " (ID: " + sId + ")");
            }

            // Ensure user login account exists in users table
            int uId = -1;
            try (PreparedStatement checkU = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
                checkU.setString(1, username);
                try (ResultSet rs = checkU.executeQuery()) {
                    if (rs.next()) uId = rs.getInt(1);
                }
            }

            if (uId == -1) {
                try (PreparedStatement insU = conn.prepareStatement(
                        "INSERT INTO users (username, password_hash, role, student_id, active) " +
                                "VALUES (?, ?, 'STUDENT', ?, ?) RETURNING id")) {
                    insU.setString(1, username);
                    insU.setString(2, passwordHash);
                    insU.setInt(3, sId);
                    insU.setBoolean(4, active);
                    try (ResultSet rs = insU.executeQuery()) {
                        if (rs.next()) uId = rs.getInt(1);
                    }
                }
                System.out.println("      -> Created user account: " + username + " (Role: STUDENT)");
            } else {
                // Keep password hash synchronized with known test password
                try (PreparedStatement upU = conn.prepareStatement("UPDATE users SET password_hash = ?, active = ? WHERE id = ?")) {
                    upU.setString(1, passwordHash);
                    upU.setBoolean(2, active);
                    upU.setInt(3, uId);
                    upU.executeUpdate();
                }
            }

            studentIds.put(username, sId);
        }

        return studentIds;
    }

    private static Map<String, Integer> seedSchedules(Connection conn, Map<String, Integer> studentIds, Map<String, Integer> subjectIds) throws Exception {
        Map<String, Integer> scheduleIds = new HashMap<>();

        // Schedule specifications:
        // Key, Username, SubjectName, Duration, TotalQ, Pass%, Status
        String[][] schedDefs = {
                {"SCHED_01", "teststudent01", "TEST Java Programming", "20", "5", "50.0", "COMPLETED"},
                {"SCHED_02", "teststudent01", "TEST Database Systems", "20", "5", "60.0", "SCHEDULED"},
                {"SCHED_03", "teststudent02", "TEST Data Structures & Algorithms", "25", "5", "50.0", "COMPLETED"},
                {"SCHED_04", "teststudent02", "TEST Computer Networks", "30", "5", "50.0", "IN_PROGRESS"},
                {"SCHED_05", "teststudent03", "TEST Database Systems", "20", "5", "50.0", "COMPLETED"},
                {"SCHED_06", "teststudent04", "TEST Java Programming", "20", "5", "50.0", "SCHEDULED"},
                {"SCHED_07", "teststudent05", "TEST Operating Systems", "25", "5", "50.0", "COMPLETED"},
                {"SCHED_08", "teststudent06", "TEST Operating Systems", "20", "5", "60.0", "COMPLETED"},
                {"SCHED_09", "teststudent07", "TEST Computer Networks", "30", "5", "50.0", "SCHEDULED"},
                {"SCHED_10", "teststudent08", "TEST Java Programming", "20", "5", "50.0", "CANCELLED"},
                {"SCHED_11", "teststudent09", "TEST Data Structures & Algorithms", "25", "5", "50.0", "SCHEDULED"},
                {"SCHED_12", "teststudent10", "TEST Database Systems", "20", "5", "50.0", "SCHEDULED"}
        };

        for (String[] def : schedDefs) {
            String key = def[0];
            String username = def[1];
            String subName = def[2];
            int duration = Integer.parseInt(def[3]);
            int totalQ = Integer.parseInt(def[4]);
            double passing = Double.parseDouble(def[5]);
            String status = def[6];

            int sId = studentIds.get(username);
            int subId = subjectIds.get(subName);

            int schedId = -1;
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM exam_schedules WHERE student_id = ? AND subject_id = ? AND duration_minutes = ?")) {
                check.setInt(1, sId);
                check.setInt(2, subId);
                check.setInt(3, duration);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) schedId = rs.getInt(1);
                }
            }

            if (schedId == -1) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO exam_schedules (student_id, subject_id, duration_minutes, total_questions, passing_percentage, status) " +
                                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id")) {
                    ins.setInt(1, sId);
                    ins.setInt(2, subId);
                    ins.setInt(3, duration);
                    ins.setInt(4, totalQ);
                    ins.setBigDecimal(5, new java.math.BigDecimal(passing));
                    ins.setString(6, status);
                    try (ResultSet rs = ins.executeQuery()) {
                        if (rs.next()) schedId = rs.getInt(1);
                    }
                }
                System.out.println("    + Created schedule " + key + ": #" + schedId + " (" + username + " - " + subName + " - " + status + ")");
            } else {
                System.out.println("    . Existing schedule " + key + ": #" + schedId);
            }
            scheduleIds.put(key, schedId);
        }

        return scheduleIds;
    }

    private static Map<String, Integer> seedAttempts(Connection conn, Map<String, Integer> scheduleIds,
                                                     Map<String, Integer> studentIds, Map<String, Integer> subjectIds,
                                                     Map<String, List<Integer>> questionIds) throws Exception {
        Map<String, Integer> attemptIds = new HashMap<>();

        // Attempts to seed:
        // Key, SchedKey, StudentUsername, SubjectName, Status, isPassScenario
        Object[][] attemptDefs = {
                {"ATTEMPT_01", "SCHED_01", "teststudent01", "TEST Java Programming", "COMPLETED", true},
                {"ATTEMPT_03", "SCHED_03", "teststudent02", "TEST Data Structures & Algorithms", "COMPLETED", false},
                {"ATTEMPT_04", "SCHED_04", "teststudent02", "TEST Computer Networks", "IN_PROGRESS", false},
                {"ATTEMPT_05", "SCHED_05", "teststudent03", "TEST Database Systems", "COMPLETED", true},
                {"ATTEMPT_07", "SCHED_07", "teststudent05", "TEST Operating Systems", "COMPLETED", true},
                {"ATTEMPT_08", "SCHED_08", "teststudent06", "TEST Operating Systems", "COMPLETED", false}
        };

        Instant now = Instant.now();

        for (Object[] def : attemptDefs) {
            String key = (String) def[0];
            String schedKey = (String) def[1];
            String username = (String) def[2];
            String subName = (String) def[3];
            String status = (String) def[4];
            boolean isPass = (Boolean) def[5];

            int schedId = scheduleIds.get(schedKey);
            int studentId = studentIds.get(username);
            int subId = subjectIds.get(subName);
            List<Integer> subQIds = questionIds.get(subName);

            int attId = -1;
            try (PreparedStatement check = conn.prepareStatement("SELECT id FROM exam_attempts WHERE exam_schedule_id = ?")) {
                check.setInt(1, schedId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) attId = rs.getInt(1);
                }
            }

            if (attId == -1) {
                Timestamp startedAt = Timestamp.from(now.minus(1, ChronoUnit.HOURS));
                Timestamp completedAt = "COMPLETED".equals(status) ? Timestamp.from(startedAt.toInstant().plus(15, ChronoUnit.MINUTES)) : null;
                int remainingSecs = "IN_PROGRESS".equals(status) ? 900 : 0;

                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO exam_attempts (exam_schedule_id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, started_at, completed_at, remaining_seconds) " +
                                "VALUES (?, ?, ?, 20, 5, 50.0, ?, ?, ?, ?) RETURNING id")) {
                    ins.setInt(1, schedId);
                    ins.setInt(2, studentId);
                    ins.setInt(3, subId);
                    ins.setString(4, status);
                    ins.setTimestamp(5, startedAt);
                    ins.setTimestamp(6, completedAt);
                    ins.setInt(7, remainingSecs);
                    try (ResultSet rs = ins.executeQuery()) {
                        if (rs.next()) attId = rs.getInt(1);
                    }
                }

                // Assign 5 questions into exam_attempt_questions
                for (int order = 1; order <= 5; order++) {
                    int qId = subQIds.get(order - 1);
                    String correctAns = getCorrectAnswer(conn, qId);

                    String selectedAns = null;
                    Boolean isCorrect = null;

                    if ("COMPLETED".equals(status)) {
                        if (isPass) {
                            // High score (5/5 or 4/5)
                            if (order <= 4) {
                                selectedAns = correctAns;
                                isCorrect = true;
                            } else {
                                selectedAns = "Wrong Answer";
                                isCorrect = false;
                            }
                        } else {
                            // Low score (1/5 or 2/5)
                            if (order == 1) {
                                selectedAns = correctAns;
                                isCorrect = true;
                            } else {
                                selectedAns = "Wrong Choice";
                                isCorrect = false;
                            }
                        }
                    } else if ("IN_PROGRESS".equals(status)) {
                        // Student answered question 1 & 2 so far
                        if (order <= 2) {
                            selectedAns = correctAns;
                        }
                    }

                    try (PreparedStatement insQ = conn.prepareStatement(
                            "INSERT INTO exam_attempt_questions (attempt_id, question_id, question_order, selected_option, is_correct) " +
                                    "VALUES (?, ?, ?, ?, ?)")) {
                        insQ.setInt(1, attId);
                        insQ.setInt(2, qId);
                        insQ.setInt(3, order);
                        insQ.setString(4, selectedAns);
                        if (isCorrect != null) {
                            insQ.setBoolean(5, isCorrect);
                        } else {
                            insQ.setNull(5, java.sql.Types.BOOLEAN);
                        }
                        insQ.executeUpdate();
                    }
                }
                System.out.println("    + Created attempt " + key + ": #" + attId + " (Status: " + status + ", 5 questions assigned)");
            } else {
                System.out.println("    . Existing attempt " + key + ": #" + attId);
            }
            attemptIds.put(key, attId);
        }

        return attemptIds;
    }

    private static void seedResults(Connection conn, Map<String, Integer> attemptIds, Map<String, Integer> scheduleIds,
                                    Map<String, Integer> studentIds, Map<String, Integer> subjectIds) throws Exception {

        // Results mapping:
        // ResultKey, AttemptKey, SchedKey, StudentUsername, SubjectName, TotalQ, Correct, ResultText
        Object[][] resultDefs = {
                {"RES_01", "ATTEMPT_01", "SCHED_01", "teststudent01", "TEST Java Programming", 5, 4, "Pass"},
                {"RES_03", "ATTEMPT_03", "SCHED_03", "teststudent02", "TEST Data Structures & Algorithms", 5, 1, "Fail"},
                {"RES_05", "ATTEMPT_05", "SCHED_05", "teststudent03", "TEST Database Systems", 5, 4, "Pass"},
                {"RES_07", "ATTEMPT_07", "SCHED_07", "teststudent05", "TEST Operating Systems", 5, 5, "Pass"},
                {"RES_08", "ATTEMPT_08", "SCHED_08", "teststudent06", "TEST Operating Systems", 5, 1, "Fail"}
        };

        for (Object[] def : resultDefs) {
            String resKey = (String) def[0];
            String attKey = (String) def[1];
            String schedKey = (String) def[2];
            String username = (String) def[3];
            String subName = (String) def[4];
            int totalQ = (Integer) def[5];
            int correct = (Integer) def[6];
            String resultText = (String) def[7];

            int attId = attemptIds.get(attKey);
            int schedId = scheduleIds.get(schedKey);
            int studentId = studentIds.get(username);
            int subId = subjectIds.get(subName);
            double percentage = (correct * 100.0) / totalQ;

            int resId = -1;
            try (PreparedStatement check = conn.prepareStatement("SELECT id FROM exam_results WHERE attempt_id = ?")) {
                check.setInt(1, attId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) resId = rs.getInt(1);
                }
            }

            if (resId == -1) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO exam_results (attempt_id, exam_schedule_id, student_id, subject_id, total_questions, correct_answers, marks, percentage, result, submitted_at) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING id")) {
                    ins.setInt(1, attId);
                    ins.setInt(2, schedId);
                    ins.setInt(3, studentId);
                    ins.setInt(4, subId);
                    ins.setInt(5, totalQ);
                    ins.setInt(6, correct);
                    ins.setBigDecimal(7, new java.math.BigDecimal(correct));
                    ins.setBigDecimal(8, new java.math.BigDecimal(percentage));
                    ins.setString(9, resultText);
                    try (ResultSet rs = ins.executeQuery()) {
                        if (rs.next()) resId = rs.getInt(1);
                    }
                }
                System.out.println("    + Created result " + resKey + ": #" + resId + " (" + username + " - " + subName + " - " + percentage + "% - " + resultText + ")");
            } else {
                System.out.println("    . Existing result " + resKey + ": #" + resId);
            }
        }
    }

    private static String getCorrectAnswer(Connection conn, int questionId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT correct_answer FROM questions WHERE id = ?")) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return "Option A";
    }
}
