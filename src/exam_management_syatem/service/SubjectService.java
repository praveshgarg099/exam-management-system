package exam_management_syatem.service;

import exam_management_syatem.dao.ExamScheduleDAO;
import exam_management_syatem.dao.SubjectDAO;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;

import java.sql.SQLException;
import java.util.List;

public class SubjectService {
    private final SubjectDAO subjectDAO;

    public SubjectService() {
        this.subjectDAO = new SubjectDAO();
    }

    public SubjectService(SubjectDAO subjectDAO) {
        this.subjectDAO = subjectDAO;
    }

    private static volatile List<Subject> cachedActive = null;
    private static volatile long lastActiveCacheTime = 0;
    private static volatile List<Subject> cachedAll = null;
    private static volatile long lastAllCacheTime = 0;
    private static final long CACHE_TTL_MS = 60_000;

    public static synchronized void invalidateCache() {
        cachedActive = null;
        lastActiveCacheTime = 0;
        cachedAll = null;
        lastAllCacheTime = 0;
    }

    public Subject addSubject(UserSession session, String name) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name cannot be empty.");
        }
        String cleanName = name.trim();
        Subject existing = subjectDAO.findByName(cleanName);
        if (existing != null) {
            throw new IllegalArgumentException("Subject '" + cleanName + "' already exists.");
        }

        Subject subject = new Subject();
        subject.setName(cleanName);
        subject.setActive(true);
        int id = subjectDAO.insert(subject);
        subject.setId(id);
        invalidateCache();
        return subject;
    }

    public Subject getSubjectByName(UserSession session, String name) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAuthenticated();
        return subjectDAO.findByName(name);
    }

    public Subject getSubjectById(UserSession session, int id) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAuthenticated();
        return subjectDAO.findById(id);
    }

    public List<Subject> getActiveSubjects(UserSession session) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAuthenticated();
        
        long now = System.currentTimeMillis();
        if (cachedActive != null && (now - lastActiveCacheTime) < CACHE_TTL_MS) {
            return cachedActive;
        }

        List<Subject> list = subjectDAO.listActive();
        cachedActive = list;
        lastActiveCacheTime = now;
        return list;
    }

    public List<Subject> getAllSubjects(UserSession session) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        long now = System.currentTimeMillis();
        if (cachedAll != null && (now - lastAllCacheTime) < CACHE_TTL_MS) {
            return cachedAll;
        }

        List<Subject> list = subjectDAO.listAll();
        cachedAll = list;
        lastAllCacheTime = now;
        return list;
    }

    public void updateSubject(UserSession session, int id, String newName) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name cannot be empty.");
        }
        String cleanName = newName.trim();
        Subject existing = subjectDAO.findByName(cleanName);
        if (existing != null && existing.getId() != id) {
            throw new IllegalArgumentException("Another subject with name '" + cleanName + "' already exists.");
        }
        Subject subject = subjectDAO.findById(id);
        if (subject == null) {
            throw new IllegalArgumentException("Subject not found.");
        }
        subject.setName(cleanName);
        subjectDAO.update(subject);
        invalidateCache();
    }

    public void setSubjectActive(UserSession session, int id, boolean active) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        if (!active) {
            ExamScheduleDAO scheduleDAO = new ExamScheduleDAO();
            if (scheduleDAO.hasActiveScheduledExams(id)) {
                throw new IllegalStateException("Cannot deactivate subject because there are active scheduled exams for it.");
            }
        }
        subjectDAO.setActive(id, active);
        invalidateCache();
    }
}
