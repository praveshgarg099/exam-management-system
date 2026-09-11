package exam_management_syatem.model;

import java.sql.Timestamp;

public class Student {
    private int id;
    private String name;
    private String mobile;
    private String email;
    private String aadharNo;
    private String dateOfBirth;
    private boolean active = true;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Student() {}

    public Student(int id, String name, String mobile, String email, String aadharNo, String dateOfBirth) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.aadharNo = aadharNo;
        this.dateOfBirth = dateOfBirth;
        this.active = true;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAadharNo() { return aadharNo; }
    public void setAadharNo(String aadharNo) { this.aadharNo = aadharNo; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
