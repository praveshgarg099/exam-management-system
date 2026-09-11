package exam_management_syatem.service;

import exam_management_syatem.dao.ExamScheduleDAO;
import exam_management_syatem.dao.SubjectDAO;
import exam_management_syatem.model.Subject;

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

    public Subject addSubject(String name) throws Exception {
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

    public Subject getSubjectByName(String name) throws SQLException {
        return subjectDAO.findByName(name);
    }

    public Subject getSubjectById(int id) throws SQLException {
        return subjectDAO.findById(id);
    }

    public List<Subject> getActiveSubjects() throws SQLException {
        return subjectDAO.listActive();
    }

    public List<Subject> getAllSubjects() throws SQLException {
        return subjectDAO.listAll();
    }

    public void updateSubject(int id, String newName) throws Exception {
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

    public void setSubjectActive(int id, boolean active) throws Exception {
        if (!active) {
            ExamScheduleDAO scheduleDAO = new ExamScheduleDAO();
            if (scheduleDAO.hasActiveScheduledExams(id)) {
                throw new IllegalStateException("Cannot deactivate subject because there are active scheduled exams for it.");
            }
        }
        subjectDAO.setActive(id, active);
    }
}

