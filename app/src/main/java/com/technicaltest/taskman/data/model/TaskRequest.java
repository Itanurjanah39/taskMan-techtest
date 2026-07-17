package com.technicaltest.taskman.data.model;

import com.google.gson.annotations.SerializedName;

public class TaskRequest {

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;

    @SerializedName("type")
    private String type;

    @SerializedName("deadline")
    private String deadline;

    public TaskRequest(String title, String description, String status, String type, String deadline) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = type;
        this.deadline = deadline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}
