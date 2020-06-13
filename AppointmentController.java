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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.TimeZone;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Seth
 */
public class AppointmentController implements Initializable {
    
    @FXML private TableView<Appointment> table_appointments;
    @FXML private TableColumn<Appointment, String> column_id;
    @FXML private TableColumn<Appointment, String> column_title;
    @FXML private TableColumn<Appointment, String> column_customer;
    @FXML private TableColumn<Appointment, String> column_start;
    @FXML private TableColumn<Appointment, String> column_end;
    @FXML private TableColumn<Appointment, String> column_location;
    @FXML private TableColumn<Appointment, String> column_description;
    @FXML private TableColumn<Appointment, String> column_type;
    @FXML private TableColumn<Appointment, String> column_url;
    @FXML private TableColumn<Appointment, String> column_contact;
    
    private ObservableList<Appointment> appointmentList;
    private static Appointment highlightedAppointment;
    
    // Radio group for the calendar view
    @FXML private ToggleGroup radiogroup_calendar = new ToggleGroup();
    
    // Currently selected radio button value
    private String calendarViewValue = "All";
    
    @FXML private Label lbl_calendarType;   // Reference the calendarView label
    @FXML private Button btn_confirmCalendar;
    @FXML private DatePicker date_week; // Reference the datepicker for the calendar view
    @FXML private ComboBox combo_month; // Reference a combo box that holds a list of months
    
    // Report combo box
    @FXML private ComboBox combo_report;
    private ObservableList<String> report_list = FXCollections.observableArrayList();
    
    // ArrayList holds the Days of week - used when changing calendar view
    private ArrayList<DayOfWeek> daysOfWeek = new ArrayList<DayOfWeek>();
    
    // Observable list that holds the months in a combo box - used when filtering the calendar by month
    private ObservableList<Month> month_combo_list = FXCollections.observableArrayList();
    
    // radiogroup_calendar change listener
    public void changeCalendarView(){
        RadioButton selectedRadioButton = (RadioButton) radiogroup_calendar.getSelectedToggle();    // Get the chosen radio button
        calendarViewValue = selectedRadioButton.getText();   // Get the text of the radiobutton
        
        switch(calendarViewValue) { // Hides the datepicker, confirm btn, and associated label text
            case "All":
                lbl_calendarType.setVisible(false);
                date_week.setVisible(false);
                btn_confirmCalendar.setVisible(false);
                combo_month.setVisible(false);
            break;
            case "Weekly":  // Display the datepicker, confirm btn, and associated label text
                combo_month.setVisible(false);
                lbl_calendarType.setVisible(true);
                date_week.setVisible(true);
                btn_confirmCalendar.setVisible(true);
                lbl_calendarType.setText("Choose Week");
                
                // Sets the day of week to today as default value
                date_week.setValue(LocalDate.now()); 
            break;
            case "Monthly": // Hide datepicker, display combo box of months
                combo_month.setVisible(true);
                lbl_calendarType.setVisible(true);
                date_week.setVisible(false);
                btn_confirmCalendar.setVisible(true);
                lbl_calendarType.setText("Choose Month");
                
                // Sets the default month to current month
                combo_month.setValue(LocalDate.now().getMonth());
            break;
        }
 
        //TODO        
        // Calls buildAppointmentData - using calendarViewValue as @param
        buildAppointmentData(calendarViewValue);
    }
    
    // Changes calendar view based on user inpuuted WEEK or MONTH
    public void changeWeekOrMonth(){
        buildAppointmentData(calendarViewValue);
    }

    // Populates the observable month list with all months in the year
    public void buildMonthData(){
        month_combo_list.add(Month.JANUARY);
        month_combo_list.add(Month.FEBRUARY);
        month_combo_list.add(Month.MARCH);
        month_combo_list.add(Month.APRIL);
        month_combo_list.add(Month.MAY);
        month_combo_list.add(Month.JUNE);
        month_combo_list.add(Month.JULY);
        month_combo_list.add(Month.AUGUST);
        month_combo_list.add(Month.SEPTEMBER);
        month_combo_list.add(Month.OCTOBER);
        month_combo_list.add(Month.NOVEMBER);
        month_combo_list.add(Month.DECEMBER);  
        
        combo_month.setItems(month_combo_list);
    }
    
    // Populates the month combo box with the month observable list 
    public void buildMonthComboBox() {        
        Callback<ListView<Month>, ListCell<Month>> factory = lv -> new ListCell<Month>(){
            @Override
            protected void updateItem(Month month, boolean empty) {
                super.updateItem(month, empty);
                setText(empty ? "" : month.toString());
            }
        };
        
        combo_month.setCellFactory(factory);
        combo_month.setButtonCell(factory.call(null)); 
    }
    
    // Populates the observable report list with the 3 required reports
    public void buildReportData(){
        report_list.add("Appointment types by month");
        report_list.add("Schedule for each consultant");
        report_list.add("Morning appointments");
        
        combo_report.setItems(report_list);
    }
    
    // Populates the report combo box with the report observable list
    public void buildReportComboBox(){
        Callback<ListView<String>, ListCell<String>> factory = lv -> new ListCell<String>(){
            @Override
            protected void updateItem(String report, boolean empty) {
                super.updateItem(report, empty);
                setText(empty ? "" : report);
            }
        };
        
        combo_report.setCellFactory(factory);
        combo_report.setButtonCell(factory.call(null));        
        combo_report.getSelectionModel().selectFirst();
    }
    
    // Populates an observableList from the database to fill appointment tableview
    // @param calendarView <0:ALL, 1:WEEKLY, 2:MONTHLY>
    public void buildAppointmentData(String calendarView){
        appointmentList = FXCollections.observableArrayList(); // List used to populate tableview
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  // Date formatter used when getting SQL records
        
        // Connect to database and perform select query on customer table
        Database db = new Database();
        Connection connection = null;
        String sql = "";
        try{
            connection = db.jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            
            // Select all appointments
            sql = "SELECT appointment.appointmentID, customer.customerName, appointment.title, appointment.description, appointment.location, appointment.contact, " +
                             "appointment.type, appointment.url, appointment.start, appointment.end FROM appointment " +
                             "INNER JOIN customer ON appointment.customerID=customer.customerID"; 
            
            ResultSet results = statement.executeQuery(sql); 
            
            while(results.next()) {  
                int id = results.getInt("appointmentID");
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
                LocalDateTime start_converted = convertToCurrentZone(LocalDateTime.parse(start.replace(".0", ""), formatter));
                LocalDateTime end_converted = convertToCurrentZone(LocalDateTime.parse(end.replace(".0", ""), formatter));
                
                // If calendar view is sorted by <WEEKLY> ~ only display those appointments within the same week
                if(calendarView.equals("Weekly")) {
                    int count = 0; // Counts how many days are equal
                    // Grab each appointment that matches a day within the chosen week
                    for(DayOfWeek day : daysOfWeek) {
                        // Using the inputted week: check each day in the week for a match in the database
                        if(date_week.getValue().with(day).equals(start_converted.toLocalDate())) {
                            count++;
                        }
                    }    
                    if(count == 0) continue;    // if there is no match - go to the next record
                }
                // If calendar view is sorted by <MONTHLY> ~ only display those appointments in the same month
                else if(calendarView.equals("Monthly")) {
                    if(!combo_month.getValue().toString().equals(start_converted.getMonth().toString())) {
                        continue;
                    } 
                }
                
                start = start_converted.toString().replace("T", " ");
                end = end_converted.toString().replace("T", " ");
                
                Appointment appointment = new Appointment(id, title, customer, start, end, location, description, type, url, contact);
                appointmentList.add(appointment);
            }
            
            table_appointments.setItems(appointmentList);
            
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
    
    // Delete the highlighted appointment from the database
    public void deleteAppointment(){
        highlightedAppointment = table_appointments.getSelectionModel().getSelectedItem();    // Grab the highlighted record
        int appointmentID = highlightedAppointment.getId();   // Grab the primary key of appt record
        
        // Drop the selected row
        Database db = new Database();
        db.dropRow(appointmentID, "appointment", "appointmentID");    
        
        //TODO Reload the tableview
        RadioButton selectedRadioButton = (RadioButton) radiogroup_calendar.getSelectedToggle();    // Get the chosen radio button
        String calendarViewValue = selectedRadioButton.getText();   // Get the text of the radiobutton
        buildAppointmentData(calendarViewValue);
    }
    
    
    // Retrieve current timezone object
    public TimeZone getTimeZone(){
        return TimeZone.getDefault();
    }
    
    // Converts returned SQL localdatetime to the user's current timezone
    public LocalDateTime convertToCurrentZone(LocalDateTime time_input){
        LocalDateTime newDateTime = time_input.atZone(ZoneId.of("GMT")).withZoneSameInstant(getTimeZone().toZoneId()).toLocalDateTime();
        return newDateTime;
    }
    
      
    // Segue: AppointmentAdd.fxml
    public void segueAppointmentAdd(javafx.event.ActionEvent event) throws IOException {
        Parent next = FXMLLoader.load(getClass().getResource("AppointmentAdd.fxml"));
        Scene scene = new Scene(next);
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        appStage.setScene(scene);
        appStage.show();
    }
    
    // Segue: AppointmentEdit.fxml
    // Save the highlighted appointment to be used in the AppointmentEdit.fxml scene
    public void segueAppointmentEdit(javafx.event.ActionEvent event) throws IOException {
        if(table_appointments.getSelectionModel().getSelectedItem() != null) {
            highlightedAppointment = table_appointments.getSelectionModel().getSelectedItem();
            Parent next = FXMLLoader.load(getClass().getResource("AppointmentEdit.fxml"));
            Scene scene = new Scene(next);
            Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            appStage.setScene(scene);
            appStage.show();
        }
    }
    
    // Segue: Customer.fxml
    public void segueCustomerMain() throws IOException {
        Parent next = FXMLLoader.load(getClass().getResource("Customer.fxml"));
        Scene scene = new Scene(next);
        Stage appStage = (Stage) table_appointments.getScene().getWindow();
        appStage.setScene(scene);
        appStage.show();
    }
    
    // Segue to specific report FXML & controller
    // Executes on <CONFIRM> button click
    public void getReport() throws IOException{
        String selectedReport = combo_report.getValue().toString(); // Selected report
        
        switch(selectedReport) {
            case "Appointment types by month":
                segueReportTypes();
                break;
            case "Schedule for each consultant":
                segueReportSchedule();
                break;
            case "Morning appointments":
                segueReportMorning();
                break;
        }
    }
    
    // Segue: ReportTypes.fxml
    public void segueReportTypes() throws IOException {
        Parent next = FXMLLoader.load(getClass().getResource("ReportTypes.fxml"));
        Scene scene = new Scene(next);
        Stage appStage = (Stage) table_appointments.getScene().getWindow();
        appStage.setScene(scene);
        appStage.show();
    }

    // Segue: ReportSchedule.fxml
    public void segueReportSchedule() throws IOException {
        Parent next = FXMLLoader.load(getClass().getResource("ReportSchedule.fxml"));
        Scene scene = new Scene(next);
        Stage appStage = (Stage) table_appointments.getScene().getWindow();
        appStage.setScene(scene);
        appStage.show();
    }
    
    // Segue: ReportPast.fxml
    public void segueReportMorning() throws IOException {
        Parent next = FXMLLoader.load(getClass().getResource("ReportMorning.fxml"));
        Scene scene = new Scene(next);
        Stage appStage = (Stage) table_appointments.getScene().getWindow();
        appStage.setScene(scene);
        appStage.show();
    }    
    
    // Retrieve the selected appointment for the user to edit
    public static int getHighlightedAppointmentID(){
        return highlightedAppointment.getId();
    }
    
    // Exit Button: exits the application
    public void exit(){
       System.exit(0); 
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Init the days of week arraylist
        daysOfWeek.add(DayOfWeek.MONDAY);
        daysOfWeek.add(DayOfWeek.TUESDAY);
        daysOfWeek.add(DayOfWeek.WEDNESDAY);
        daysOfWeek.add(DayOfWeek.THURSDAY);
        daysOfWeek.add(DayOfWeek.FRIDAY);
        
        buildMonthData();   // Populate the observable month list with Month objects
        buildMonthComboBox();  // Using the month data, build the combo box and select the first element
        
        buildReportData();  // Populate observable report list with report strings
        buildReportComboBox();  // Using report data list, build the report combo box
        
        // Set up tableview columns
        column_id.setCellValueFactory(new PropertyValueFactory<Appointment, String>("id"));
        column_title.setCellValueFactory(new PropertyValueFactory<Appointment, String>("title"));
        column_customer.setCellValueFactory(new PropertyValueFactory<Appointment, String>("customer"));
        column_start.setCellValueFactory(new PropertyValueFactory<Appointment, String>("start"));
        column_end.setCellValueFactory(new PropertyValueFactory<Appointment, String>("end"));
        column_location.setCellValueFactory(new PropertyValueFactory<Appointment, String>("location"));
        column_description.setCellValueFactory(new PropertyValueFactory<Appointment, String>("description"));
        column_type.setCellValueFactory(new PropertyValueFactory<Appointment, String>("type"));
        column_url.setCellValueFactory(new PropertyValueFactory<Appointment, String>("url"));
        column_contact.setCellValueFactory(new PropertyValueFactory<Appointment, String>("contact"));
        
        // Grab appointment data from database and populate tableview
        // @param 0: view ALL appts
        buildAppointmentData("All");
    }    
    
}
