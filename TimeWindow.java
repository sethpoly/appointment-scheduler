/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentscheduler;

import java.time.ZonedDateTime;

/**
 *
 * @author Seth
 */
public class TimeWindow {
    private String timeFrom;    // Time the appointment starts
    private String timeTo;      // Time the appointment ends
    
    //@Constructor
    public TimeWindow(String from, String to){
        timeFrom = from;
        timeTo = to;
    }

    // ###FIXME Should get time zone and convert the timewindow when displaying in timewindow
    // Retrieves a string representation of the time window
    // Used when displaying the times in the combo box
    public String getTimeWindow(){
        int timeToCompare = Integer.parseInt(timeTo.split(":")[0]);
        if(timeToCompare < 12 && timeToCompare > 5){
           return timeFrom + " - " + timeTo + " AM"; 
        }
         return timeFrom + " - " + timeTo + " PM"; 
    }
    
    // Retrieve the hour of the time
    // Convert to military first
    public int getHour(String time){
        int hour = Integer.parseInt(time.split(":")[0]);
        
        // Convert to military
        if(hour < 6) hour += 12;
        
        return hour;
    }
    
    // Retrieve the minute of the time
    public int getMinute(String time){
        int minute = Integer.parseInt(time.split(":")[1]);
        
        return minute;
    }

    
    
    
    
    public String getTimeFrom() {
        return timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }
}
