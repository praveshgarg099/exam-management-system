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
        return subjectDAO.listActive();
    }

    public List<Subject> getAllSubjects(UserSession session) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        return subjectDAO.listAll();
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
    }
}
