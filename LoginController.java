package appointmentscheduler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import static java.lang.System.out;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
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
 *
 * @author Seth
 * Create a log-in form that can determine the user’s location and 
 * translate log-in and error control messages (e.g., 
 * “The username and password did not match.”) into two languages.
 */
// Controller for the LOGIN modal
public class LoginController implements Initializable {
    
    public static int user_id;  // Global variable user ID is used to determine the current user logged into the system
    
    @FXML private TextField field_username;
    @FXML private TextField field_password;
    @FXML private Button btn_login;
    @FXML private Label lbl_login_message;
    @FXML private Label lbl_login_message_spanish;
    @FXML private Label lbl_user_location;
    
    // btn_login event: attempts to login to the application using username and password
    public void requestLogin(javafx.event.ActionEvent event) throws IOException, InvalidLoginException {
        String username_input = field_username.getText();
        String password_input = field_password.getText();
        authenticateUser(username_input, password_input);
    }
    
    // ### Prints all users
    public void printUser(){
        String columns = "*";
        String tableName = "user";
        Database db = new Database();
        db.selectData(tableName, columns);
    }
    
    // Authenticates user by searching through user table and matching the inputted username and password to a specific user
    public void authenticateUser(String input_username, String input_password) throws InvalidLoginException, IOException{
        Database db = new Database();
        Connection connection = null;
        
        String userName = "";
        String password = "";
        String loginMessage = "";
        String loginMessageSpanish = "";
        int id = 0;
        boolean loginResult = false;
        
        try {
            connection = db.jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            String sql = "SELECT * FROM user";
            
            ResultSet results = statement.executeQuery(sql);
            
            try {
                // Iterate through each user and check if user input matches any
                while(results.next()) {
                    // Store this record's username/password
                    userName = results.getString("userName");
                    password = results.getString("password");
                    id = results.getInt("userId");

                    // Check if inputted data == stored user data
                    if(userName.equals(input_username) && password.equals(input_password)) {
                        lbl_login_message.setStyle("-fx-text-fill: green");
                        lbl_login_message_spanish.setStyle("-fx-text-fill: green");
                        loginResult = true;
                        break;
                    }
                }
                // Handle unsuccessful login
                if(!loginResult){
                    lbl_login_message.setStyle("-fx-text-fill: red");
                    lbl_login_message_spanish.setStyle("-fx-text-fill: red");
                    throw new InvalidLoginException("Login failed");
                }
                else {
                    // Create new spanish locale to translate error message
                    Locale spanish = new Locale("es", "US");
                    ResourceBundle rb = ResourceBundle.getBundle("appointmentscheduler.Resources/Login", spanish);
                    loginMessageSpanish = rb.getString("loginSuccess");

                    loginMessage = "Login successful.";
                    System.out.println("Login Successful - Username: " + userName + " Password: " + password + " ID: " + id);
                }
            }catch(Exception login_exception) {
                // Create new spanish locale to translate error message
                Locale spanish = new Locale("es", "US");
                ResourceBundle rb = ResourceBundle.getBundle("appointmentscheduler.Resources/Login", spanish);
                loginMessageSpanish = rb.getString("loginFailure");                
                
                loginMessage = "Login failed. Username and password do not match.";
                loginMessageSpanish = "Error de inicio de sesion. Nombre de usuario y contraseña no coinciden.";
                System.out.println("Invalid Login");
            }
            
        } catch (Exception e) { // Statement error handling
            System.err.println(e.getMessage());
        } 
        finally {
            try {
                connection.close();
            } catch (Exception e2) {    // Closing database error handling
            System.err.println(e2.getMessage());
            }
        }
        
        // Display the error /or success message through UI
        lbl_login_message.setText(loginMessage);
        lbl_login_message_spanish.setText(loginMessageSpanish);
        
        
        // If successful login, save the user ID 
        if(loginResult){ 
            segueMain(); // segue to next modal
            user_id = id;   // Save the current user to a global var
            logTimestamp(user_id);
        }
    }
    
    // Logs the timestamp with the user ID a txt file
    // @param userID
    public void logTimestamp(int id){
        String filename = "src/UserTimestamps.txt";
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;

        try{
            fw = new FileWriter(filename, true);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);

            pw.println(getTimestamp.compute(id));
            pw.flush();

        }catch(IOException e){
            System.err.println(e.getMessage());
        }
    }
    
    // LAMBDA EXPRESSION: I created this lambda expression to create the timestamp value that would be appended to the timestamp.txt file. It is more easily readable
    // & allows me to easily change the result for the timestamp without editing the logTimeStamp() function
    StringLambda getTimestamp = (id) -> {
        String result = "User " + id + " logged in at: " + new Timestamp(System.currentTimeMillis());
        return result;
    };
        
    
      
    // Segue to the main screen of the application after successful login
    public void segueMain() throws IOException {
        Parent next = FXMLLoader.load(getClass().getResource("Customer.fxml"));
        Scene scene = new Scene(next);
        Stage appStage = (Stage) field_username.getScene().getWindow(); // Retrieve the current stage by using any node 
        appStage.setScene(scene);
        appStage.show();
    }
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get & display user location
        lbl_user_location.setText("You are signing in from this country: " + System.getProperty("user.country"));
    }    
    
}


interface StringLambda<T>{
    String compute(int x);
}



// Exception class that handles invalid login 
class InvalidLoginException extends Exception{
    InvalidLoginException(String s){
        super(s);
    }
}
