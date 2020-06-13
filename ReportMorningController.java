/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentscheduler;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Seth
 */
public class ReportMorningController implements Initializable {

    @FXML private TextArea text_report;

    // Builds report data of only morning appointments (before 12PM)
    public void buildReportData(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  // Date formatter used when getting SQL records
        Database db = new Database();
        Connection connection = null;
        String sql = "";
        String report = "";
        try{
            connection = db.jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            
           sql = "SELECT customer.customerName, appointment.title, " +
             "appointment.type, appointment.start FROM appointment " +
             "INNER JOIN customer ON appointment.customerID=customer.customerID " +
             "GROUP BY appointment.start"; 
            ResultSet results = statement.executeQuery(sql); 
            
            while(results.next()) {  
                String customer = results.getString("customerName");
                String title = results.getString("title");
                String start = results.getString("start");
                
                
                // Convert the start and end DATETIMES to the user's current timezone                
                java.time.LocalDateTime start_converted = convertToCurrentZone(java.time.LocalDateTime.parse(start.replace(".0", ""), formatter));
                
                if(start_converted.getHour() >= 12) continue;
                
                String month = start_converted.getMonth().toString();
                int day = start_converted.getDayOfMonth();
                int hour = start_converted.getHour();
                String minute = String.valueOf(start_converted.getMinute());
                
                if(minute.length() == 1) minute += "0";   // Add following zeroes
                System.out.println(month + " " + day + " at " + hour + ":" + minute + " - " + title + " with " + customer);
                                
                report += month + " " + day + " at " + hour + ":" + minute + " - " + title + " with " + customer + "\n";
            }           
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }finally {
            try {
                connection.close();
            } catch (Exception e2) {    // Closing databse error handling
            System.err.println(e2.getMessage());
            }
        }         
        text_report.setText(report);
    } 
    
    // Retrieve current timezone object
    public TimeZone getTimeZone(){
        return TimeZone.getDefault();
    }
    
    // Converts returned SQL localdatetime to the user's current timezone
    public java.time.LocalDateTime convertToCurrentZone(java.time.LocalDateTime time_input){
        java.time.LocalDateTime newDateTime = time_input.atZone(ZoneId.of("GMT")).withZoneSameInstant(getTimeZone().toZoneId()).toLocalDateTime();
        return newDateTime;
    }
    
    // btn_exit : event
    // returns to main appointment view
    public void exitReport(javafx.event.ActionEvent event) throws IOException {
        Parent next = FXMLLoader.load(getClass().getResource("Appointment.fxml"));
        Scene scene = new Scene(next);
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Retrieve the current stage by using any node 
        appStage.setScene(scene);
        appStage.show();
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buildReportData();
    }    
    
}
