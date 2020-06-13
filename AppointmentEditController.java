/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentscheduler;

import static appointmentscheduler.AppointmentController.getHighlightedAppointmentID;
import static appointmentscheduler.LoginController.user_id;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Seth
 */
public class AppointmentEditController implements Initializable {

    private int appointmentID = 0;  // Holds the ID of appointment to update
    
    @FXML private TextField field_customer;
    @FXML private TextField field_title;
    @FXML private TextField field_description;
    @FXML private TextField field_type;
    @FXML private TextField field_location;
    @FXML private TextField field_contact;
    @FXML private TextField field_url;
    @FXML private DatePicker date_day;
    @FXML private ComboBox combo_time;
    
    @FXML private Label lbl_dataError;
    @FXML private Label lbl_timezone;   // Displays current timezone user is in
    @FXML private Label lbl_businessHoursError; // Displays business hours exception error to user
    @FXML private Label lbl_overlapError;   // Displays an overlapping appts error if occurs
    
    ObservableList<TimeWindow> timeWindow_combo_list = FXCollections.observableArrayList();    // List to hold available time windows

    // Arraylist of the above textfields <used for quick data verification>
    private ArrayList<TextField> field_arraylist = new ArrayList<>();   
    
    
    // Populates the text fields with their original data from the database
    public void initialPopulation(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  // Date formatter used when getting SQL records
         // Connect to database and perform select query on customer table
        Database db = new Database();
        Connection connection = null;
        try{
            connection = db.jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            
            String sql = "SELECT appointment.appointmentID, customer.customerName, appointment.title, appointment.description, appointment.location, appointment.contact, " +
                         "appointment.type, appointment.url, appointment.start, appointment.end FROM appointment " +
                         "INNER JOIN customer ON appointment.customerID=customer.customerID " +
                         "WHERE appointmentID = " + appointmentID; 
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
                java.time.LocalDateTime end_converted = convertToCurrentZone(java.time.LocalDateTime.parse(end.replace(".0", ""), formatter));

                start = start_converted.toString().replace("T", " ");
                end = end_converted.toString().replace("T", " ");     
                
                System.out.println("Start_converted: " + start_converted);
                
                // Get the corresponding time window from the database
                int window_index = findTimeWindow(start_converted);
                
                field_customer.setText(customer);
                field_title.setText(title);
                field_description.setText(description);
                field_location.setText(location);
                field_contact.setText(contact);
                field_type.setText(type);
                field_url.setText(url);
                date_day.setValue(start_converted.toLocalDate());
                combo_time.setValue(timeWindow_combo_list.get(window_index));      
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
    }
    
    // btn_add_appointment : event
    public void onEditAppointmentButtonClick(){
        // Verify data then proceed to save data
        if(dataVerification()){
            
            // Clear any error labels
            lbl_businessHoursError.setText("");
            lbl_overlapError.setText("");
            
            editAppointment();
        }   
    }    
    
    // Handle exceptions and update the appointment record in the database
    public void editAppointment(){
       //int userID = user_id; // Retrieve the current user who is logged into the application
        String title = field_title.getText();
        String description = field_description.getText();
        String type = field_type.getText();
        String location = field_location.getText();
        String contact = field_contact.getText();
        String url = field_url.getText();
        LocalDate day = date_day.getValue();
        TimeWindow timeWindow = (TimeWindow) combo_time.getSelectionModel().getSelectedItem();
        
        // Exception handling to verify correct business hours
        try {
            if(!verifyBusinessHours(day)){   // Verify correct business hours
                throw new InvalidBusinessHoursException(day.getDayOfWeek().name() + " is not within our business hours. Only Monday-Friday.");
            }
            // Create a LocalDateTime_from using 'day' and 'timeWindow.getTimeFrom()'
            int hour_from = Integer.parseInt(timeWindow.getTimeFrom().split(":")[0]);
            int min_from = Integer.parseInt(timeWindow.getTimeFrom().split(":")[1]);
            if(hour_from < 6) hour_from += 12;  // Convert to military
            java.time.LocalDateTime dt_from = day.atTime(hour_from, min_from);

            // Create a LocalDateTime_to using 'day' and 'timeWindow.getTimeTo()'
            int hour_to = Integer.parseInt(timeWindow.getTimeTo().split(":")[0]);
            int min_to = Integer.parseInt(timeWindow.getTimeTo().split(":")[1]);         
            if(hour_to < 6) hour_to += 12;  // Convert to military
            java.time.LocalDateTime dt_to = day.atTime(hour_to,  min_to);

            // Convert the dates to be usable in SQL
            java.time.LocalDateTime converted_from = convertDate(dt_from);
            java.time.LocalDateTime converted_to = convertDate(dt_to);
            
            // Check if scheduled day is not in the past
            if(!checkNotInPast(converted_from)){
                throw new InvalidBusinessHoursException("Unable to schedule in the past");
            }
            
            // Exception handling to avoid overlapping appointments
            if(!verifyOverlap(converted_from, converted_to)){
                throw new OverlappingAppointmentsException("You already have an appointment scheduled during this time.");
            }
    
            // Add the record to the database
            Database db = new Database();
            com.mysql.jdbc.Connection connection = null;
            try {
                connection = db.jdbc_connection.connect();
                String sql = "UPDATE appointment SET title=?, description=?, location=?, contact=?, type=?, url=?, start=?, end=? WHERE appointmentID=?";

                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, title);
                pstmt.setString(2, description);
                pstmt.setString(3, location);
                pstmt.setString(4, contact);
                pstmt.setString(5, type);
                pstmt.setString(6, url);
                pstmt.setString(7, converted_from.format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")));
                pstmt.setString(8, converted_to.format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")));
                pstmt.setInt(9, appointmentID);

                pstmt.executeUpdate();
            } catch (Exception e) { // Statement error handling
                System.err.println(e.getMessage());
            }finally {
                try {
                    connection.close();
                } catch (Exception e2) {    // Closing databse error handling
                System.err.println(e2.getMessage());
                }
            }
            
            // Display success to user
            lbl_dataError.setStyle("-fx-text-fill: green");
            lbl_dataError.setText("Appointment successfully updated.");
        } catch(InvalidBusinessHoursException e){
            // Catch and display error to user
            System.out.println(e.getMessage());
            lbl_businessHoursError.setText(e.getMessage());
        } catch(OverlappingAppointmentsException e2){
            System.out.println(e2.getMessage());  
            lbl_overlapError.setText(e2.getMessage()); 
        }        
    }
    
    // Finds the corresponding time window object using the database
    public int findTimeWindow(java.time.LocalDateTime start_time){
        int current = 0;
        int window_index = 0;
        for(TimeWindow window : timeWindow_combo_list){
            int comparedHour = Integer.parseInt(window.getTimeFrom().split(":")[0]);
            if(comparedHour == start_time.getHour()){
                window_index = current;
            }
            current++;
        }
        return window_index;
    }
   
    // Creates fixed appointment values to fill the timeWindow_combo_list
    public void buildTimeWindowData(){
        timeWindow_combo_list.add(new TimeWindow("09:00","09:30"));
        timeWindow_combo_list.add(new TimeWindow("09:30","10:00"));
        timeWindow_combo_list.add(new TimeWindow("10:00","10:30"));
        timeWindow_combo_list.add(new TimeWindow("10:30","11:00"));
        timeWindow_combo_list.add(new TimeWindow("11:00","11:30"));
        timeWindow_combo_list.add(new TimeWindow("11:30","12:00"));
        timeWindow_combo_list.add(new TimeWindow("12:00","12:30"));
        timeWindow_combo_list.add(new TimeWindow("12:30","01:00"));
        timeWindow_combo_list.add(new TimeWindow("01:00","01:30"));
        timeWindow_combo_list.add(new TimeWindow("01:30","02:00"));
        timeWindow_combo_list.add(new TimeWindow("02:00","02:30"));
        timeWindow_combo_list.add(new TimeWindow("02:30","03:00"));        
        timeWindow_combo_list.add(new TimeWindow("03:00","03:30"));
        timeWindow_combo_list.add(new TimeWindow("03:30","04:00"));
        timeWindow_combo_list.add(new TimeWindow("04:00","04:30"));
        timeWindow_combo_list.add(new TimeWindow("04:30","05:00"));

        combo_time.setItems(timeWindow_combo_list);
    }

    // Checks data-type verification and empty values for appointment TextFields
    // @return bool
    public Boolean dataVerification(){
        ArrayList<String> emptyDataArraylist = new ArrayList<>();   // ArrayList that will hold all values the user leaves blank
        
        try {
            // Iterate through the text fields to find the ones that were left blank
            for(TextField field : field_arraylist) {
                String fieldData = field.getText();    // Retrieve input data as string
                String fieldName = field.getId().substring(6);  // Get name of text field <substring of field_******>
                
                // Check for empty data & appends the name of field to arraylist if empty
                if(fieldData.equals("")) {
                    emptyDataArraylist.add(fieldName);
                }
            }
            // If any fields were left blank, throw an error and notify user which fields were left blank
            if(emptyDataArraylist.size() > 0) {
                String errorMessage = "Error: These text fields are blank: ";
            
                // Appends all empty text fields to the error message
                for(String fieldName : emptyDataArraylist) {
                    if(fieldName.equals(emptyDataArraylist.get(emptyDataArraylist.size() - 1))) {   // Checks if element is the last in the list to avoid punctuational comma error
                        errorMessage += (fieldName + ".");
                    }
                    else {
                        errorMessage += (fieldName + ", ");
                    }
                }
                throw new InvalidDataException(errorMessage);
            }
        } catch(InvalidDataException e){
            lbl_dataError.setStyle("-fx-text-fill: red");
            lbl_dataError.setText(e.getMessage());
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    // Verifies the business hours of the user inputted - day
    // <Not Saturday or Sunday>
    // @param LocalDate day
    public Boolean verifyBusinessHours(LocalDate day) {
        return day.getDayOfWeek().getValue() != SATURDAY.getValue() && day.getDayOfWeek().getValue() != SUNDAY.getValue();  
    }
    
    // Checks if the inputted appointment does not overlap with any of the user's (consultant's) other appointments
    // @param localdatetime time_from, localdatetime time_to
    public Boolean verifyOverlap(java.time.LocalDateTime input_time_from, java.time.LocalDateTime input_time_to){
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  // Date formatter used when getting SQL records
        
         // Connect to database and perform select query on consultant's appointments
        Database db = new Database();
        Connection connection = null;
        try{
            connection = db.jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            
            // Retrieve the datetimes from each appointment in the consultant's schedule - use this information to check for overlapping appts
            String sql = "SELECT start, end FROM appointment " +
                         "WHERE userID = " + user_id + " AND appointmentID <> " + appointmentID;
                         
            ResultSet results = statement.executeQuery(sql); 
            
            // Grab the customer records
            while(results.next()){
                java.time.LocalDateTime sql_from = java.time.LocalDateTime.parse(results.getString("start").replace(".0", ""), formatter);
                java.time.LocalDateTime sql_to = java.time.LocalDateTime.parse(results.getString("end").replace(".0", ""), formatter);
                
                // Convert SQL records to user's timezone & back to GMT <easier to compare>
                java.time.LocalDateTime from_converted = convertDate(convertToCurrentZone(sql_from));
                java.time.LocalDateTime to_converted = convertDate(convertToCurrentZone(sql_to));                
                
                // Check for overlap - throw exception if so  
                if(input_time_from.isEqual(sql_from) || input_time_to.isEqual(sql_to)){
                    System.out.println("Attempted schedule appt in GMT: From - " + input_time_from + "       TO - " + input_time_to);
                    System.out.println("Already scheduled appt in GMT: From - " + from_converted + "       TO - " + to_converted);
                    
                    return false;
                }
            }
            }catch(Exception e) {
                System.out.println(e.getMessage());     
        } finally {
            try {
                connection.close();
            } catch (Exception e2) {    // Closing databse error handling
            System.err.println(e2.getMessage());
            }
        } 
        
        return true;
    }

    // Check if date is before the current date
    public Boolean checkNotInPast(java.time.LocalDateTime input_time){
        if(input_time.isBefore(java.time.LocalDateTime.now(ZoneId.of("GMT")))){
            System.out.println("Can't schedule in the past!");
            return false;
        }    
        return true;
    }

    // Saves the datetime using the user's timezone
    // Then converts that datetime to GMT to keep consistency in the database
    public java.time.LocalDateTime convertDate(java.time.LocalDateTime time_input) {
        ZonedDateTime localzone = time_input.atZone(getTimeZone().toZoneId());     // Convert user input to their current timezone
        ZonedDateTime gmt_time = localzone.withZoneSameInstant(ZoneId.of("GMT"));  // Convert their new ZonedDateTime to GMT (for storing)
        java.time.LocalDateTime converted_time = gmt_time.toLocalDateTime();  // Convert the GMT ZonedDateTime to a LocalDateTime to store in database
                
        return converted_time;
    }
    
    // Converts returned SQL localdatetime to the user's current timezone
    public java.time.LocalDateTime convertToCurrentZone(java.time.LocalDateTime time_input){
        java.time.LocalDateTime newDateTime = time_input.atZone(ZoneId.of("GMT")).withZoneSameInstant(getTimeZone().toZoneId()).toLocalDateTime();
        return newDateTime;
    }
    
    // Populates the timewindow combo box
    public void buildTimeWindowCombo(){
        combo_time.getSelectionModel().selectFirst(); // Select the first element
        
        // Update each timewindow to show the string representation of the window
        Callback<ListView<TimeWindow>, ListCell<TimeWindow>> factory = lv -> new ListCell<TimeWindow>(){
            @Override
            protected void updateItem(TimeWindow timeWindow, boolean empty) {
                super.updateItem(timeWindow, empty);
                setText(empty ? "" : timeWindow.getTimeWindow());
            }
        };
        
        combo_time.setCellFactory(factory);
        combo_time.setButtonCell(factory.call(null));  
    }

    // btn_cancel : event
    // Cancels the add appointment function and returns to main appointment view
    public void cancelEditAppointment(javafx.event.ActionEvent event) throws IOException {
        Parent next = FXMLLoader.load(getClass().getResource("Appointment.fxml"));
        Scene scene = new Scene(next);
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Retrieve the current stage by using any node 
        appStage.setScene(scene);
        appStage.show();
    }
    
    // Retrieve current timezone object
    public TimeZone getTimeZone(){
        return TimeZone.getDefault();
    }
        
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        appointmentID = getHighlightedAppointmentID();
        
        lbl_timezone.setText("Timezone: " + getTimeZone().getDisplayName());        
        buildTimeWindowData();    // Retrieve static timewindows and inserts them into the combo box
        buildTimeWindowCombo(); // Builds and populates the timewindow combo box with data from local Observable list
         
        initialPopulation();

        // Populate the arraylist of textfields
        field_arraylist.add(field_title);
        field_arraylist.add(field_description);
        field_arraylist.add(field_location);
        field_arraylist.add(field_contact);
        field_arraylist.add(field_type);
        field_arraylist.add(field_url);         
    }    
}
