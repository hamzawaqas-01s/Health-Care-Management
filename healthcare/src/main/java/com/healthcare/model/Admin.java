package com.healthcare.model;

public class Admin extends User {

    private String adminCode;

    public Admin(int id, String name, String email, String phone, String adminCode) {
        super(id, name, email, phone);
        this.adminCode = adminCode;
    }

    public String getAdminCode() { return adminCode; }

    @Override
    public String getRole() { return "Admin"; }
}
