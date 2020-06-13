package appointmentscheduler;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Seth
 */
public class CustomerController implements Initializable {
    
    // The user highlighted customer - used when deleting and updating records
    private static Customer highlightedCustomer;

    @FXML private Button btn_add_customer;
    @FXML private Button btn_edit_customer;
    @FXML private Button btn_delete_customer;
    @FXML private Button btn_exit;
    @FXML private RadioButton radio_appointments;
    @FXML private RadioButton radio_customers;
    
    @FXML private TableView<Customer> table_customers;
    @FXML private TableColumn<Customer, String> column_id;
    @FXML private TableColumn<Customer, String> column_name;
    @FXML private TableColumn<Customer, String> column_address;
    @FXML private TableColumn<Customer, String> column_phone;
    
    private ObservableList<Customer> customerList;
    
    
    // Segue: CustomerAdd.fxml
    public void segueCustomerAdd(javafx.event.ActionEvent event) throws IOException {
        Parent next = FXMLLoader.load(getClass().getResource("CustomerAdd.fxml"));
        Scene scene = new Scene(next);
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        appStage.setScene(scene);
        appStage.show();
    }
    
    // Segue: CustomerEdit.fxml
    // Save the highlighted customer to be used in the CustomerEdit.fxml scene
    public void segueCustomerEdit(javafx.event.ActionEvent event) throws IOException {
        if(table_customers.getSelectionModel().getSelectedItem() != null) {
            highlightedCustomer = table_customers.getSelectionModel().getSelectedItem();
            Parent next = FXMLLoader.load(getClass().getResource("CustomerEdit.fxml"));
            Scene scene = new Scene(next);
            Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            appStage.setScene(scene);
            appStage.show();
        }
    }
    
    // Segue: Appointment.fxml
    public void segueAppointmentMain() throws IOException {
        Parent next = FXMLLoader.load(getClass().getResource("Appointment.fxml"));
        Scene scene = new Scene(next);
        Stage appStage = (Stage) table_customers.getScene().getWindow();
        appStage.setScene(scene);
        appStage.show();
    }
    
    // Populates an observableList from the database to fill the tableview
    public void buildData(){
        customerList = FXCollections.observableArrayList(); // List used to populate tableview
        
        // Connect to database and perform select query on customer table
        Database db = new Database();
        Connection connection = null;
        try{
            connection = db.jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            
            String sql = "SELECT customer.customerID, customer.customerName, address.address, address.phone FROM customer " +
                         "INNER JOIN address ON customer.addressID=address.addressID"; 
            ResultSet results = statement.executeQuery(sql); 
            
            while(results.next()) {  
                int id = results.getInt("customerID");
                String name = results.getString("customerName");
                String address = results.getString("address");
                String phone = results.getString("phone");
               
                Customer customer = new Customer(id, name, address, phone);
                customerList.add(customer);
            }
            
            table_customers.setItems(customerList);
            
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
    
    // Deletes the highlighted customer from the database
    public void deleteCustomer() {
        highlightedCustomer = table_customers.getSelectionModel().getSelectedItem();    // Grab the highlighted record
        int customerID = highlightedCustomer.getId();   // Grab the primary key of customer record
        
        // Drop the selected row
        Database db = new Database();
        db.dropRow(customerID, "customer", "customerID");    
        
        // Reload the tableview
        buildData();
    }
    
    // Retrieve the selected customer for the user to edit
    public static int getHighlightedCustomerID(){
        return highlightedCustomer.getId();
    }
    
    // TODO
    public void checkForAppointmentInFifteen(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  // Date formatter used when getting SQL records
        ArrayList<Appointment> appointmentsAlertList = new ArrayList<Appointment>();    // Arraylist that holds all appointments within 15 minutes
        
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
                java.time.LocalDateTime start_converted = convertToCurrentZone(java.time.LocalDateTime.parse(start.replace(".0", ""), formatter));
                java.time.LocalDateTime end_converted = convertToCurrentZone(java.time.LocalDateTime.parse(end.replace(".0", ""), formatter));
                
                start = start_converted.toString().replace("T", " ");
                end = end_converted.toString().replace("T", " ");
                
                // Check if <start_converted> is after and within 15 minutes of current time & adds it to an arrayList if so
                // I saved the appointment objs just in case I want to display any specific details about the appointment
                if(start_converted.isAfter(java.time.LocalDateTime.now()) && java.time.LocalDateTime.now().plusMinutes(15).isAfter(start_converted)) {
                    Appointment appointment = new Appointment(id, title, customer, start, end, location, description, type, url, contact);
                    appointmentsAlertList.add(appointment);
                }
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
        
        int appointmentsCount = appointmentsAlertList.size();  // Number of appointments within 15 minutes
        if(appointmentsCount > 0) { // If any appointments are within 15 minutes ; display alert to user
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Appointments starting soon");
            // Set body text top display number of incoming appts
            if(appointmentsCount > 1) alert.setContentText("There are " + appointmentsCount + " appointments starting within 15 minutes.");
            else alert.setContentText("There is " + appointmentsCount + " appointment starting within 15 minutes.");
            
            alert.showAndWait();    // Show alert
        }
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
    
    // Exit Button: exits the application
    public void exit(){
       System.exit(0); 
    }
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up tableview column values
        column_id.setCellValueFactory(new PropertyValueFactory<Customer, String>("id"));
        column_name.setCellValueFactory(new PropertyValueFactory<Customer, String>("name"));
        column_address.setCellValueFactory(new PropertyValueFactory<Customer, String>("address"));
        column_phone.setCellValueFactory(new PropertyValueFactory<Customer, String>("phone"));
        
        // Grab customer data from database and populate tableview
        buildData();
        
        // Checks for any appointments within the next 15 minutes and displays an alert to the user
        checkForAppointmentInFifteen();
    }    
    
}
