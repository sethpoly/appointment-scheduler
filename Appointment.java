/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentscheduler;

import java.time.LocalDateTime;

/**
 *
 * @author Seth
 */
public class Appointment {
    private int id;
    private String title;
    private String customer;
    private String start;
    private String end;
    private String location;
    private String description;
    private String type;
    private String url;
    private String contact;

    public Appointment(int id, String title, String customer, String start, String end, String location, String description, String type, String url, String contact) {
        this.id = id;
        this.title = title;
        this.customer = customer;
        this.start = start;
        this.end = end;
        this.location = location;
        this.description = description;
        this.type = type;
        this.url = url;
        this.contact = contact;
    }
    
    public int getId(){
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCustomer() {
        return customer;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    
}
