package appointmentscheduler;

import com.mysql.jdbc.Connection;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Seth
 */
public class CustomerAddController implements Initializable {

    @FXML private TextField field_name;
    @FXML private TextField field_address;
    @FXML private TextField field_city;
    @FXML private TextField field_postal;
    @FXML private TextField field_country;
    @FXML private TextField field_phone;
    
    @FXML private Label lbl_dataError;
    
    
    @FXML private Button btn_add_customer;
    @FXML private Button btn_cancel;
    
    // Arraylist of the above textfields <used for quick data verification>
    private ArrayList<TextField> field_arraylist = new ArrayList<>();

    
    
    // btn_add_customer : event
    // Calls 'dataVerification(field_arraylist)' to verify the user input and handle any exceptions
    // Uses the input fields to populate new rows of data for each table
    public void onAddCustomerButtonClick() { 
        // Verify user input data 
        if(dataVerification()) {
            // Insert new rows in each table with the proper foreign keys
            insertCountry();
            insertCity();
            insertAddress();
            insertCustomer();
            clearFields();  // Clear text fields at end and display status message
        }
    }
    
    // Inserts new row into country table
    public void insertCountry() {
        Database db = new Database();
        Connection connection = null;
        try {
            connection = db.jdbc_connection.connect();
            String sql = "INSERT INTO country (country) VALUES (?)";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, field_country.getText());
           
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
    }
    // Inserts new row into city table
    public void insertCity() {  
        Database db = new Database();
        Connection connection = null;
        int countryFK = db.countRows("country");   // Retrieve the matching foreign key
        try {
            connection = db.jdbc_connection.connect();
            String sql = "INSERT INTO city (city, countryID) VALUES (?,?)";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, field_city.getText());
            pstmt.setInt(2, countryFK);
           
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
    }
    // Inserts new row into address table
    public void insertAddress() {    
        Database db = new Database();
        Connection connection = null;
        int cityFK = db.countRows("city");   // Retrieve the matching foreign key
        try {
            connection = db.jdbc_connection.connect();
            String sql = "INSERT INTO address (address, cityID, postalCode, phone) VALUES (?,?,?,?)";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, field_address.getText());
            pstmt.setInt(2, cityFK);
            pstmt.setString(3, field_postal.getText());
            pstmt.setString(4, field_phone.getText());
           
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
    }
    // Inserts new row into customer table
    public void insertCustomer() { 
        Database db = new Database();
        Connection connection = null;
        int addressFK = db.countRows("address");   // Retrieve the matching foreign key
        try {
            connection = db.jdbc_connection.connect();
            String sql = "INSERT INTO customer (customerName, addressID, active) VALUES (?,?,?)";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, field_name.getText());
            pstmt.setInt(2, addressFK);
            pstmt.setInt(3, 0);
           
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
    }

        
    // Clears text fields and displays successful message
    public void clearFields() {
        for(TextField field : field_arraylist) {
            field.setText("");
        }
        
        // Display success to UI
        lbl_dataError.setStyle("-fx-text-fill: green");
        lbl_dataError.setText("Customer successfully added.");
    }
    
    // Checks data-type verification and empty values for customer TextFields
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
    
    // btn_cancel : event
    // Cancels the add customer function and returns to main
    public void cancelAddCustomer(javafx.event.ActionEvent event) throws IOException {
        Parent next = FXMLLoader.load(getClass().getResource("Customer.fxml"));
        Scene scene = new Scene(next);
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Retrieve the current stage by using any node 
        appStage.setScene(scene);
        appStage.show();
    }

    
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // Populate the arraylist of textfields
        field_arraylist.add(field_name);
        field_arraylist.add(field_address);
        field_arraylist.add(field_city);
        field_arraylist.add(field_postal);
        field_arraylist.add(field_country);
        field_arraylist.add(field_phone);  
    }    
    
}


// Exception class that handles data verification
class InvalidDataException extends Exception{
    InvalidDataException(String s){
        super(s);
    }
}
