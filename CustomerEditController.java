/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentscheduler;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import static appointmentscheduler.CustomerController.getHighlightedCustomerID;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Customer Edit Controller class
 *
 * @author Seth
 */
public class CustomerEditController implements Initializable {

    private int customerToEdit = 0;    // Retrieve the ID of the customer to edit
    private int countryID = 0;
    private int cityID = 0;
    private int addressID = 0;
    private int customerID = 0;
    
    @FXML private TextField field_name;
    @FXML private TextField field_address;
    @FXML private TextField field_city;
    @FXML private TextField field_postal;
    @FXML private TextField field_country;
    @FXML private TextField field_phone;
    
    @FXML private Label lbl_dataError;
    
    // Arraylist of the above textfields <used for quick data verification>
    private ArrayList<TextField> field_arraylist = new ArrayList<>();
    
    
    // Update edited rows with user inputted data
    public void onEditCustomerButtonClick(){
        if(dataVerification()) {
            updateCountry();
            updateCity();
            updateAddress();
            updateCustomer(); 
            lbl_dataError.setStyle("-fx-text-fill: green");
            lbl_dataError.setText(field_name.getText() + " was successfully updated.");
        }
    }
    
    // Update the country record
    public void updateCountry(){
//        String tableName = "country";
//        String values = "country = '" + field_country.getText() + "'"; 
//        String condition = "countryID = " + countryID;
//                            
//        Database db = new Database();
//        db.updateData(tableName, values, condition);
        
        Database db = new Database();
        com.mysql.jdbc.Connection connection = null;
        try {
            connection = db.jdbc_connection.connect();
            String sql = "UPDATE country SET country=? WHERE countryID = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, field_country.getText());
            pstmt.setInt(2, countryID);
           
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
    
    // Update the city record
    public void updateCity(){
//        String tableName = "city";
//        String values = "city = '" + field_city.getText() + "'"; 
//        String condition = "cityID = " + cityID;
//                            
//        Database db = new Database();
//        db.updateData(tableName, values, condition); 
        
        Database db = new Database();
        com.mysql.jdbc.Connection connection = null;
        try {
            connection = db.jdbc_connection.connect();
            String sql = "UPDATE city SET city=? WHERE cityID = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, field_city.getText());
            pstmt.setInt(2, cityID);
           
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
    
    // Update the address record
    public void updateAddress(){
//        String tableName = "address";
//        String values = "address = '" + field_address.getText() + "', postalCode = '" + field_postal.getText() + "', phone = '" + field_phone.getText() + "'"; 
//        String condition = "addressID = " + addressID;
//                            
//        Database db = new Database();
//        db.updateData(tableName, values, condition);      
        
        Database db = new Database();
        com.mysql.jdbc.Connection connection = null;
        try {
            connection = db.jdbc_connection.connect();
            String sql = "UPDATE address SET address=?,postalCode=?,phone=? WHERE addressID = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, field_address.getText());
            pstmt.setString(2, field_postal.getText());
            pstmt.setString(3, field_phone.getText());
            pstmt.setInt(4, addressID);
           
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
    
    // Update the customer record
    public void updateCustomer(){
//        String tableName = "customer";
//        String values = "customerName = '" + field_name.getText() + "'"; 
//        String condition = "customerID = " + customerID;
//                            
//        Database db = new Database();
//        db.updateData(tableName, values, condition);    
       
        Database db = new Database();
        com.mysql.jdbc.Connection connection = null;
        try {
            connection = db.jdbc_connection.connect();
            String sql = "UPDATE customer SET customerName=? WHERE customerID = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, field_name.getText());
            pstmt.setInt(2, customerID);
           
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
    
    // Populates the text fields with their original data from the database
    public void initialPopulation(){
        String name = "";
        String address = "";
        String city = "";
        String postalCode = "";
        String country = "";
        String phone = "";
        
        // Vars to hold the ids
        int id_customer = 0;
        int id_address = 0;
        int id_city = 0;
        int id_country = 0;
        
        // Connect to database and perform select query on highlighted customer record
        Database db = new Database();
        Connection connection = null;
        try{
            connection = db.jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            
            // We are inner joining all 4 tables to retrieve all values of the customer 
            String sql = "SELECT customer.customerName, address.address, address.phone, address.postalCode, city.city, country.country, customer.customerID, customer.addressID, city.cityID, country.countryID FROM customer " +
                         "INNER JOIN address ON customer.addressID=address.addressID " +
                         "INNER JOIN city ON address.cityID=city.cityID " +
                         "INNER JOIN country ON city.countryID=country.countryID " +
                         "WHERE customerID = " + customerToEdit;
                         
//            String sql = "SELECT * FROM " +
//                         "(SELECT c.customerName, a.address, a.phone, a.postalCode, a.cityID FROM customer c, address a " +
//                         "WHERE c.addressID=a.addressID) q1 " +
//                         "INNER JOIN (SELECT ci.city, co.country, ci.cityID FROM city ci, country co " +
//                         "WHERE ci.countryID=co.countryID) q2 " +
//                         "ON q1.cityID=q2.cityID";
            ResultSet results = statement.executeQuery(sql); 
            
            // Grab the customer records
            results.next();
            name = results.getString("customerName");
            address = results.getString("address");
            city = results.getString("city");
            postalCode = results.getString("postalCode");
            country = results.getString("country");
            phone = results.getString("phone");   
            
            // Grab the ids of each table
            id_customer = results.getInt("customerID");
            id_address = results.getInt("addressID");
            id_city = results.getInt("cityID");
            id_country = results.getInt("countryID");
            
            // Populate fields with attributing valeus
            updateTextFields(name, address, city, postalCode, country, phone);
            
            // Save the ids for later use
            customerID = id_customer;
            addressID = id_address;
            cityID = id_city;
            countryID = id_country;
  
        }catch(Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            System.out.println("Error");        
        }finally {
            try {
                connection.close();
            } catch (Exception e2) {    // Closing databse error handling
            System.err.println(e2.getMessage());
            }
        } 
    }
    
    // Updates the text fields
    public void updateTextFields(String name, String address, String city, String postalCode, String country, String phone){
        field_name.setText(name);
        field_address.setText(address);
        field_city.setText(city);
        field_postal.setText(postalCode);
        field_country.setText(country);
        field_phone.setText(phone);        
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
    // Cancels the edit customer function and returns to main
    public void cancelEditCustomer(javafx.event.ActionEvent event) throws IOException {
        Parent next = FXMLLoader.load(getClass().getResource("Customer.fxml"));
        Scene scene = new Scene(next);
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Retrieve the current stage by using any node 
        appStage.setScene(scene);
        appStage.show();
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        customerToEdit = getHighlightedCustomerID();
        initialPopulation();
        
        // Populate the arraylist of textfields
        field_arraylist.add(field_name);
        field_arraylist.add(field_address);
        field_arraylist.add(field_city);
        field_arraylist.add(field_postal);
        field_arraylist.add(field_country);
        field_arraylist.add(field_phone);
    }    
    
}
