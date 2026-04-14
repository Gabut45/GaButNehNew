package com.ujicoba.gabut.models;

public class TasksModel {

    private int id;
    private String task_logo;
    private String task_name;
    private String task_desc;
    private String task_coin;
    private String task_cost; // 🔹 biaya buka task
    private String task_time;
    private String task_link;

    // 🔹 Constructor lengkap
    public TasksModel(int id, String task_logo, String task_name, String task_desc,
                      String task_coin, String task_cost, String task_time, String task_link) {
        this.id = id;
        this.task_logo = task_logo;
        this.task_name = task_name;
        this.task_desc = task_desc;
        this.task_coin = task_coin;
        this.task_cost = task_cost;
        this.task_time = task_time;
        this.task_link = task_link;
    }

    // 🔹 Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTask_logo() {
        return task_logo;
    }

    public void setTask_logo(String task_logo) {
        this.task_logo = task_logo;
    }

    public String getTask_name() {
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    public String getTask_desc() {
        return task_desc;
    }

    public void setTask_desc(String task_desc) {
        this.task_desc = task_desc;
    }

    public String getTask_coin() {
        return task_coin;
    }

    public void setTask_coin(String task_coin) {
        this.task_coin = task_coin;
    }

    public String getTask_cost() {
        return task_cost;
    }

    public void setTask_cost(String task_cost) {
        this.task_cost = task_cost;
    }

    public String getTask_time() {
        return task_time;
    }

    public void setTask_time(String task_time) {
        this.task_time = task_time;
    }

    public String getTask_link() {
        return task_link;
    }

    public void setTask_link(String task_link) {
        this.task_link = task_link;
    }
}
