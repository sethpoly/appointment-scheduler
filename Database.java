/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentscheduler;

import com.mysql.jdbc.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author Seth
 */
public class Database {
    public JDBCConnection jdbc_connection = new JDBCConnection();
    Connection connection = null;
    
    
//     Inserts new data into a specified table
//     @param tableName the name of the table, insertData the data to be inserted
    public void insertData(String tableName, String insertColumns, String insertData){
         try {
            connection = jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            String sql = "INSERT INTO " + tableName + " " + insertColumns +  " VALUES " + insertData;
                        
            statement.executeUpdate(sql);
            System.out.println("Inserted " + insertData + " into table: " + tableName + ".");
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

    
    // Counts number of rows in a table - is used when retrieving foreign keys of the last updated table
    // @param tableName the name of the table
    public int countRows(String tableName) {
        int rowCount = 0;   // Number of rows to return
        
        try {
            connection = jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            String sql = "SELECT COUNT(*) FROM " + tableName;
            
            ResultSet results = statement.executeQuery(sql);      
            results.next();
            rowCount = results.getInt(1);  
        } catch (Exception e) { // Statement error handling
            System.err.println(e.getMessage());
        }finally {
            try {
                connection.close();
            } catch (Exception e2) {    // Closing databse error handling
            System.err.println(e2.getMessage());
            }
        }  
        return rowCount;
    }
    
    // Simple select example
    public void selectData(String tableName, String columns) {
          try {
            connection = jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            String sql = "SELECT " + columns + " FROM " + tableName;
            
            ResultSet results = statement.executeQuery(sql);
            
            // Iterate through each row and grab the data
            while(results.next()) {
                int id = results.getInt("userId");
                String userName = results.getString("userName");
                String password = results.getString("password");
                int active = results.getInt("active");
                
                System.out.format("%s, %s, %s, %s\n", id, userName, password, active);
            }
            
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
    
    // Drops a record from a database
    // @param rowID, tableName
    public void dropRow(int id, String tableName, String column) {
        try {
            connection = jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            String sql = "DELETE FROM " + tableName + " WHERE " + column + " = " + id + ";";
            
            statement.executeUpdate(sql);
            System.out.println("Dropped record: " + id + " from " + tableName + ".");
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
    
    
    // Drops a table from our database
    // @param tableName the name of the table to drop
    public void dropTable(String tableName){
         try {
            connection = jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            String sql = "SET foreign_key_checks = 0; DROP TABLE " + tableName + "; SET foreign_key_checks = 1;";
            
            statement.executeUpdate(sql);
            System.out.println("Dropped table: " + tableName + " from database.");
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
    
    // Alters a table from the database
    // @param tablename, altervalue
    public void alterTable(){
         try {
            connection = jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            String sql = "";
            
            
            statement.executeUpdate(sql);
            //rs.next();
            //System.out.println(rs.getString(2));
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
    
        // Drops a table from our database
    // @param tableName the name of the table to drop
    public void createTable(String tableName, String tableSql){
         try {
            connection = jdbc_connection.connect();
            Statement statement = connection.createStatement(); // Set up statement
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + tableSql;
            
            statement.executeUpdate(sql);
            System.out.println("Created table: " + tableName + ".");
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
}
