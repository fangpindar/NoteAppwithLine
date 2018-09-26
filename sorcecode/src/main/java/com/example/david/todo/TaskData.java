package com.example.david.todo;

import java.util.Calendar;
import java.util.Date;

public class TaskData {
    private String title;
    private Date taskDate;

    public TaskData(String title, Date taskDate) {
        this.title = title;
        this.taskDate = taskDate;
    }
    public String getTitle(){
        return title;
    }
    public Date getDate(){
        return taskDate;
    }
}
