package com.technicaltest.taskman.data.model;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public static class Data {

        @SerializedName("id")
        private int id;

        @SerializedName("email")
        private String email;

        @SerializedName("employee")
        private Employee employee;

        @SerializedName("last_login")
        private String lastLogin;

        public int getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public Employee getEmployee() {
            return employee;
        }

        public String getLastLogin() {
            return lastLogin;
        }
    }

    public static class Employee {

        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("nik")
        private String nik;

        @SerializedName("phone")
        private String phone;

        @SerializedName("role")
        private String role;

        @SerializedName("company")
        private String company;

        @SerializedName("division")
        private String division;

        @SerializedName("work_entry_date")
        private String workEntryDate;

        @SerializedName("avatar")
        private String avatar;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getNik() {
            return nik;
        }

        public String getPhone() {
            return phone;
        }

        public String getRole() {
            return role;
        }

        public String getCompany() {
            return company;
        }

        public String getDivision() {
            return division;
        }

        public String getWorkEntryDate() {
            return workEntryDate;
        }

        public String getAvatar() {
            return avatar;
        }
    }
}
