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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Seth
 */
public class ReportScheduleController implements Initializable {
    
    @FXML private ComboBox combo_user;  // Holds all consultants
    @FXML private TextArea text_schedule;   // Will display the consultant's schedule
    private ObservableList<User> user_list = FXCollections.observableArrayList();   // Used to populate combobox
    

    public void generateSchedule(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  // Date formatter used when getting SQL records
        Database db = new Database();
        Connection connection = null;
        String sql = "";
        String report = "";
        
        // Retrieve the inputted consultant's id
        User user = (User) combo_user.getValue();
        int userID = user.getId();
        try{
            connection = db.jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            
            sql = "SELECT appointment.appointmentID, customer.customerName, appointment.title, appointment.description, appointment.location, appointment.contact, " +
             "appointment.type, appointment.url, appointment.start, appointment.end FROM appointment " +
             "INNER JOIN customer ON appointment.customerID=customer.customerID " +
             "WHERE userID = " + userID + " GROUP BY appointment.start"; 
            ResultSet results = statement.executeQuery(sql); 
            
            while(results.next()) {  
                String customer = results.getString("customerName");
                String title = results.getString("title");
                String description = results.getString("description");
                String location = results.getString("location");
                String contact = results.getString("contact");
                String type = results.getString("type");
                String url = results.getString("url");
                String start = results.getString("start");
                String end = results.getString("end");
                
                
                // Convert the start and end DATETIMES to the user's current timezone                
                java.time.LocalDateTime start_converted = convertToCurrentZone(java.time.LocalDateTime.parse(start.replace(".0", ""), formatter));
                String month = start_converted.getMonth().toString();
                int day = start_converted.getDayOfMonth();
                int hour = start_converted.getHour();
                String minute = String.valueOf(start_converted.getMinute());
                
                if(minute.length() == 1) minute += "0";   // Add following zeroes
                                
                report += month + " " + day + " at " + hour + ":" + minute + " - appointment with " + customer + "\n";
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
        
        text_schedule.setText(report);
    }
    
    // Populates the observable arraylist with all users in the database
    public void buildConsultantData(){
        // Connect to database and perform select query on user table
        Database db = new Database();
        Connection connection = null;
        try{
            connection = db.jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            
            String sql = "SELECT userID, userName FROM user";
            ResultSet results = statement.executeQuery(sql); 
            
            while(results.next()) {  
                int id = results.getInt("userID");
                String name = results.getString("userName");
               
                User user = new User(id, name);
                user_list.add(user);
            }
            
            combo_user.setItems(user_list);
            
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }finally {
            try {
                connection.close();
            } catch (Exception e2) {    // Closing databse error handling
            System.err.println(e2.getMessage());
            }
        }                 
    }
    
    // Uses observable arraylist to populate the consultant combobox
    public void buildConsultantComboBox(){
        combo_user.getSelectionModel().selectFirst(); // Select the first element
        
        // Update each timewindow to show the string representation of the window
        Callback<ListView<User>, ListCell<User>> factory = lv -> new ListCell<User>(){
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty ? "" : user.getName());
            }
        };
        
        combo_user.setCellFactory(factory);
        combo_user.setButtonCell(factory.call(null));         
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
        buildConsultantData();
        buildConsultantComboBox();
    }    
    
}
