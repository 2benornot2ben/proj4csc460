/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Name: Benjamin Kanter & Zhaoyi Li
 * Course: CSC 460
 * Program: DBCreator
 * Instructor: Mr. McCann
 * Due: 2026-05-5
 * 
 * Description: Creates oracle tables for our DB!
 * Also cleans already existing one, handles sizes and formatting, whole nine yards.
 * Cool stuff! Was hard to make.
 * Might fail if there's any exceptionally long entries.
 * 
 * To run this, it  expects two args; a username and password.
 * But you can just let it prompt you if you want.
 * 
 * This was basically just written by Ben. Zhaoyi's busy with the queries &
 * double checking everything.
 * 
 * Known Bugs: None!
 * Unknown Bugs: Hopefully None!
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class DBCreator {
	public static void main(String args[]) {		
		String username = null,
	               password = null;
		
		final String oracleURL = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";
		
		Scanner scanUse = new Scanner(System.in);
		
		// Accept args OR input.
		
		if (args.length >= 1) {
			username = args[0];
		} else {
		    System.out.println("Enter username:");
		    username = scanUse.nextLine();
		}
		if (args.length >= 2) {
			password = args[1];
		} else {
		    System.out.println("Enter password:");
		    password = scanUse.nextLine();
		}
		
		// Frankly? I don't understand this the best.
		
		try {
            Class.forName("oracle.jdbc.OracleDriver");
	    } catch (ClassNotFoundException e) {
            System.err.println("*** ClassNotFoundException:  "
                + "Error loading Oracle JDBC driver.  \n"
                + "\tPerhaps the driver is not on the Classpath?");
            System.exit(-1);
	    }
		Connection dbconn = null;
        try {
            dbconn = DriverManager.getConnection
                           (oracleURL,username,password);
        } catch (SQLException e) {
                System.err.println("*** SQLException:  "
                    + "Could not open JDBC connection.");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
        }
        
        Statement stmt = null;
        ResultSet answer = null;
        
        // We're gonna be doing a lot of edits.
    
        try {
            dbconn.setAutoCommit(false);
		} catch (SQLException e) {
		
		        System.err.println("*** SQLException:  "
		            + "Failed to turn autocommit off.");
		        System.err.println("\tMessage:   " + e.getMessage());
		        System.err.println("\tSQLState:  " + e.getSQLState());
		        System.err.println("\tErrorCode: " + e.getErrorCode());
		        System.exit(-1);
		
		}
		
		createTable(dbconn, "instuser", 
				  "userid number(12) PRIMARY KEY , "
				+ "username varchar2(30) UNIQUE , "
				+ "displayname varchar2(30) , "
				+ "name varchar2(30) , "
				+ "email varchar2(40) UNIQUE, "
				+ "account_creation_date DATE , "
				+ "preferred_language varchar2(30) , "
				+ "messages_today number(8) , "
				+ "tier_name varchar2(30)");
		
		createTable(dbconn, "membershiptier", 
				  "tier_name varchar2(30) PRIMARY KEY , "
				+ "max_messages_per_day number(8) , "
				+ "pro_model_access number(1)");
		
		createTable(dbconn, "billingprofile", 
				  "billing_id number(15) PRIMARY KEY , "
				+ "userid number(12) , "
				+ "payment_method varchar2(30) , "
				+ "billing_address varchar2(40)");
		
		createTable(dbconn, "invoice", 
				  "invoice_id number(20) PRIMARY KEY , "
				+ "userid number(12) , "
				+ "amount number(12, 2) , " // This is decimal.	
				+ "invoice_date DATE , "
				+ "payment_status varchar2 (20)");
		
		createTable(dbconn, "supportagent", 
				  "agent_id number(12) PRIMARY KEY , "
				+ "agent_name varchar2(30) , "
				+ "email varchar2(40) UNIQUE");
		
		createTable(dbconn, "supportticket", 
				  "ticket_id number(20) PRIMARY KEY , "
				+ "userid number(12) , "
				+ "agent_id number(12) , "
				+ "topic varchar2(40) , "
				+ "status varchar2(25) , "
				+ "duration number(10) , "
				+ "outcome varchar2(40)");
		
		createTable(dbconn, "persona", 
				  "persona_id number(15) PRIMARY KEY , "
				+ "persona_name varchar2(30) , "
				+ "instruction_text varchar(240) , "
				+ "created_by_userid number(12) , "
				+ "CONSTRAINT idnamedupeless UNIQUE (created_by_userid, persona_name)");
		
		createTable(dbconn, "workspace", 
				  "workid number(15) PRIMARY KEY , "
				+ "workspace_name varchar2(25) , "
				+ "owner_userid number(12) , "
				+ "visibility varchar2(30)"); // This maybe should be a bool?
		
		createTable(dbconn, "workspacemember", 
				  "workid number(15) , "
				+ "userid number(12) , "
				+ "role varchar2(30) , "
				+ "CONSTRAINT workandid PRIMARY KEY (workid, userid)");
		
		createTable(dbconn, "conversation", 
				  "chatid number(15) PRIMARY KEY , "
				+ "userid number(12) , "
				+ "workid number(15) , "
				+ "persona_id number(15) , "
				+ "title varchar2(30) , "
				+ "persona_snapshot varchar2(240) , "
				+ "created_at DATE , "
				+ "status varchar2(40) , "
				+ "CONSTRAINT idandtitle UNIQUE (userid, title)");
		
		createTable(dbconn, "message", 
				  "message_id number(20) PRIMARY KEY , "
				+ "chatid number(15) , "
				+ "sender_role varchar2(10) , "
				+ "content varchar2(500) , "
				+ "timestamp DATE , "
				+ "bookmarked number(1) , "
				+ "feedback_id number(15)");
		
		createTable(dbconn, "feedback", 
				  "feedback_id number(15) PRIMARY KEY , "
				+ "message_id number(20) , " // This may be unnecessary.
				+ "rating number(5) , "
				+ "review varchar2(200)");
		
		createTable(dbconn, "prompttemplate", 
				  "prompt_id number(15) PRIMARY KEY , "
				+ "created_by_userid number(12) , "
				+ "workid number(15) , "
				+ "prompt_name varchar2(30) , "
				+ "category varchar2(20) , "
				+ "prompt_text varchar2(240) , "
				+ "visibility varchar2(30) , " // This one might not be as avoidable
				+ "CONSTRAINT idandname UNIQUE (created_by_userid, prompt_name)");
        
		System.out.println("Successfully reached end of execution.");
		scanUse.close();
	}
	
	public static void createTable(Connection dbconn, String tablename, String data) {
		Statement stmt = null;
		try {
	    	// This shouldn't error.
	    	dbconn.setAutoCommit(false);
            stmt = dbconn.createStatement();
            // This may error, if it doesn't exist already. That's okay.
            stmt.executeQuery("drop table " + tablename);
		} catch (SQLException e) {
		    // This running is fine.
		}
		try {
    		// Now, let's remake the table.
    		stmt = dbconn.createStatement();
    		stmt.executeUpdate("create table " + tablename
    				+ " ( " + data + " )");
    		/*
    		 * stmt.executeUpdate("create table " + tablename
    				+ " ( rc varchar2(20) , "
    				+ "incnum varchar2(30) , " // This failed as a number.
    				+ "grade varchar2(30) , "
    				+ "dateof varchar2(30) , "
    				+ "timeof varchar2(20) , "
    				+ "sname varchar2(50) , "
    				+ "highuser varchar2(60) , " // Failed at a value of 15.
    				+ "temp number(10) , "
    				+ "visibility varchar2(30) , "
    				+ "weather varchar2(40) , "
    				+ "loccount number(6) , "
    				+ "carcount number(6) "
    				+ ")");
    		 */
    		stmt.executeUpdate("grant select on " + tablename + " to public");
	    	// Done. This shouldn't of errored.
    		dbconn.commit();
    		System.out.println("Made table " + tablename);
		} catch (SQLException e) {
	        System.err.println("*** SQLException:  "
	            + "Could not fetch query results.");
	        System.err.println("\tMessage:   " + e.getMessage());
	        System.err.println("\tSQLState:  " + e.getSQLState());
	        System.err.println("\tErrorCode: " + e.getErrorCode());
	        System.exit(-1);
		}
	}
}
