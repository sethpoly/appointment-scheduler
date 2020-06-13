/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentscheduler;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
/**
 *
 * @author Seth
 */
public class JDBCConnection {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://3.227.166.251/U06Zlc?allowMultiQueries=true";
    static final String USER = "U06Zlc";
    static final String PASSWORD = "53688911951";
    
    Connection connection = null;
    
    // Connect to the sql database
    public Connection connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database...");
            connection = (Connection) DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("Connected to database successfully");
        }
        catch(Exception e)
        {
            System.err.println("Error! Unable to connect to the database.");
            System.err.println(e.getMessage());
        }
        return connection;
    }
}
