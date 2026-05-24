package com.hours.model;

/**
 * 用户实体类
 * 对应数据库 user 表
 */
public class User {
    private Long id;
    private String studentId;
    private String name;
    private String password;
    private String role;       // "student" | "admin"
    private String className;
    private String major;

    public User() {}

    public User(String studentId, String name, String password, String role) {
        this.studentId = studentId;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    // ---- Getters & Setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    @Override
    public String toString() {
        return "User{id=" + id + ", studentId='" + studentId + "', name='" + name + "', role='" + role + "'}";
    }
}
