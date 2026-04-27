/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Name: Benjamin Kanter & Zhaoyi Li
 * Course: CSC 460
 * Program: Prog4 Main
 * Instructor: Mr. McCann
 * Due: 2026-05-5
 * 
 * Description: Allows you to do one of many queries and edits into a database!
 * This database is meant to handle everything related to ai talks, apparently.
 * So, there's a lot of stuff to it. Apart from the conversations themselves,
 * there's also personas, workspaces, users, with their invoices and billing profiles,
 * membership tiers, agents, etc.
 * To use this, you just type the letter, the args (either one at a time or all trailing the letter,
 * space seperated) and it does everything else for you!
 * Can be looped as many times as necessary. Type an invalid letter OR deny the rerun request to stop.
 * All queries don't accept spaces (since they're space seperated).
 *
 * This program expects two args; a username and password.
 * But you can just let it prompt you if you want.
 * 
 * Known Bugs: None!
 * Unknown Bugs: Hopefully None!
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

public class Main {
	// Super convenient.
	static ArrayList<String> validYears = new ArrayList<String>(Arrays.asList("1980", "1995", "2010", "2025"));
	public static void main(String[] args) {
		final String oracleURL = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";
		String username = null,
	               password = null;
		
		Scanner scanUse = new Scanner(System.in);
		
		// TODO: Uncomment this
		
		/*
		// Simple prompt / arg checker.
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
        */
        
        // We loop until it's over.
		
		// TODO: Comment this out
		Connection dbconn = null;
        
        while (true) {
        	ArrayList<String> inputList = new ArrayList<String>();
        	System.out.println("Please, first select method of usage:");
        	System.out.println("a : Admin. Get access to most direct-modification queries.");
        	System.out.println("b : User. Use it like an actual user! (ARGS: 1)");
        	System.out.println("c : Debug. We needed these for testing anyways.");
        	System.out.println("EXIT : End program.");
        	
        	String[] nextLine = scanUse.nextLine().split(" ");
        	if (nextLine.length == 0) continue;
        	
        	if (nextLine[0].toLowerCase().startsWith("a")) {
        		// No args are needed.
        		adminpanel(scanUse, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("b")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Username: ") == 1) {
        			continue;
        		}
        		userpanel(scanUse, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("c")) {
        		// Same here
        		extraspanel(scanUse, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("exit")) {
        		break;
        	}
        }
		
        System.out.println("Program Terminated.");
        scanUse.close();
	}
	
	public static int getInput(Scanner scanner, ArrayList<String> inputList, String[] responseString,
			String... values) {
		inputList.clear();
		for (int index = 0; index < values.length; index++) {
			if (responseString.length > index + 1) {
				inputList.add(responseString[index + 1]);
			} else {
				System.out.println(values[index]);
				String strVal = scanner.nextLine();
				if (strVal.isEmpty()) index--;
				else if (strVal.equalsIgnoreCase("Exit")) return 1;
				else {
					inputList.add(strVal.split(" ")[0]);
				}
			}
		}
		return 0;
	}
	
	public static void adminpanel(Scanner scanUse, ArrayList<String> inputList, Connection dbconn) {
		inputList.clear();
		while (true) {
        	System.out.println("Admin Panel:");
        	System.out.println("a : Add User (Args: Like, a lot, idk)");
        	System.out.println("b : Update User (Args: Also like a lot, idk)");
        	System.out.println("c : Delete User (Args: 1)");
        	System.out.println("d : Update Subscription of User (Args: 2)"); // What's a "record insertion
        	// for a message"? It sounds just like an automatic check.
        	System.out.println("e : Generate Invoice (Args: 1)"); // It's tier-based, so no 2nd arg
        	// Where's mark as complete? That's on the user side.
        	System.out.println("f : Assign Ticket (Args: 2)");
        	System.out.println("g : Modify Ticket (Args: 4)");
        	System.out.println("q1 : List all bookmarked messages for user (Args: 1)");
        	System.out.println("q2 : List all users with unpaid invoices");
        	System.out.println("q3 : List the most helpful personas");
        	System.out.println("q4 : ??? (Args: >1)");
        	System.out.println("Logout : Return to main menu.");
        	
        	String[] nextLine = scanUse.nextLine().split(" ");
        	if (nextLine.length == 0) continue;
        	
        	if (nextLine[0].toLowerCase().startsWith("a")) {
        		if (getInput(scanUse, inputList, nextLine, "idk a lot of args here") == 1) {
        			continue;
        		}
        		//queryAddUser(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("b")) {
        		if (getInput(scanUse, inputList, nextLine, "idk a lot of args here") == 1) {
        			continue;
        		}
        		//queryUpdateUser(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("c")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Username: ") == 1) {
        			continue;
        		}
        		//queryDeleteUser(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("d")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Username: ",
        				"Input Subscription Tier: ") == 1) {
        			continue;
        		}
        		//queryUpdateSub(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("e")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Username: ") == 1) {
        			continue;
        		}
        		//queryGenerateInvoice(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("f")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Ticket ID: ",
        				"Input Agent ID: ") == 1) {
        			continue;
        		}
        		// How to get the ID's? I duno, might need more queries.
        		//queryAssignTicket(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("f")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Ticket ID: ",
        				"Input Status: ", "Input Duration: ", "Input Outcome or NULL: ") == 1) {
        			continue;
        		}
        		// How to get the ID's? I duno, might need more queries.
        		//queryModifyTicket(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("q1")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Username: ") == 1) {
        			continue;
        		}
        		//querySpecialOne(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("q2")) {

        		//querySpecialTwo(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("q3")) {

        		//querySpecialThree(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("q4")) {
        		if (getInput(scanUse, inputList, nextLine, "???: ") == 1) {
        			continue;
        		}
        		//querySpecialFour(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("logout")) {
        		break;
        	}
        }
	}
	
	public static void userpanel(Scanner scanUse, ArrayList<String> inputList, Connection dbconn) {
		// TODO: Query here which validates that they exist.
		boolean userFound = true;
		if (!userFound) {
			System.out.println("User not found!");
			return;
		}
		
		String user = inputList.get(0);
		// This will probably also be found via the query
		int userId = 0;
		inputList.clear();
		while (true) {
        	System.out.println("Enter query for " + user);
        	System.out.println("aa : Start Conversation (Args: 3, I think?)");
        	System.out.println("ab : Add message to Conversation (Args: 2 (+1))");
        	System.out.println("ac : Update Message Feedback (Args: 4)");
        	System.out.println("ba : Move Conversation into Workspace (Args: 2)");
        	System.out.println("bb : Remove Conversation from Workspace (Args: 2)");
        	System.out.println("bc : Modify Workspace Visibility (Args: 2)");
        	System.out.println("ca : Create Persona (Args: 2)");
        	System.out.println("cb : Modify Persona (Args: 2)");
        	System.out.println("cc : Delete Persona (Args: 2)");
        	// I believe personas are public by default?
        	System.out.println("da : Create Prompt (Args: 3)");
        	System.out.println("db : Modify Prompt (Args: 2)");
        	System.out.println("dc : Share Prompt (Args: 2)");
        	System.out.println("ea : Create Ticket (Args: 2)");
        	
        	System.out.println("Logout : Return to main menu.");
        	
        	String[] nextLine = scanUse.nextLine().split(" ");
        	if (nextLine.length == 0) continue;
        	
        	if (nextLine[0].toLowerCase().equalsIgnoreCase("aa")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Conversation Title: ",
        				"Input Persona: ", "Input Prompt: ") == 1) {
        			// So, how do they input a persona and prompt?
        			// No idea. Sorry. Maybe name and check if they have access?
        			// Though that could have issues, too.
        			continue;
        		}
        		//queryCreateConversation(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("ab")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Conversation Title: ",
        				"Input Message: ", "(Input AI Message too): ") == 1) {
        			continue;
        		}
        		//queryAddMessage(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("ac")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Conversation Title: ",
        				"Input Identifier: ", "Input Review OR Null", "Input Rating OR Null") == 1) {
        			continue;
        		}
        		//queryReviewMessage(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("ba")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Conversation Title: ",
        				"Input Workspace Title: ") == 1) {
        			continue;
        		}
        		//queryAddConversationToWorkspace(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("bb")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Conversation Title: ",
        				"Input Workspace Title: ") == 1) {
        			continue;
        		}
        		//queryRemoveConvoFromWorkspace(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("bc")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Workspace Title: ",
        				"Input Visibility: ") == 1) {
        			continue;
        		}
        		//queryCreatePersona(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("ca")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Persona Name: ",
        				"Input Persona Instructions: ") == 1) {
        			continue;
        		}
        		//queryModifyPersona(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("cb")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Persona Name: ",
        				"Input Persona Instructions: ") == 1) {
        			continue;
        		}
        		//queryRemovePersona(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("cc")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Persona Name: ") == 1) {
        			continue;
        		}
        		//queryCreatePrompt(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("da")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Prompt Name: ",
        				"Input Prompt Category: ", "Input Prompt: ") == 1) {
        			continue;
        		}
        		//queryModifyPrompt(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("db")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Prompt Name: ",
        				"Input Prompt: ") == 1) {
        			continue;
        		}
        		//queryMovePromptToWS(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("dc")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Prompt Name: ",
        				"Input Workspace: ") == 1) {
        			continue;
        		}
        		//queryRemoveConvoFromWorkspace(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("ea")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Topic: ") == 1) {
        			continue;
        		}
        		//queryCreateTicket(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("logout")) {
        		break;
        	}
        }
	}
	
	public static void extraspanel(Scanner scanUse, ArrayList<String> inputList, Connection dbconn) {
		inputList.clear();
		while (true) {
        	System.out.println("Extras Panel:");
        	System.out.println("a : Probably something that'll fill up the DB?");
        	System.out.println("Logout : Return to main menu.");
        	
        	String[] nextLine = scanUse.nextLine().split(" ");
        	if (nextLine.length == 0) continue;
        	
        	if (nextLine[0].toLowerCase().startsWith("a")) {
        		if (getInput(scanUse, inputList, nextLine, "idk if thisll have args") == 1) {
        			continue;
        		}
        		//queryFillDB(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().startsWith("logout")) {
        		break;
        	}
        }
	}
	
	public static void exampleQuery(ArrayList<String> inputList, Connection dbconn) {
		/* An example query.
		 * Directly ripped from project 3.
		 * Note you may need to do input validation.
		 * Though, you can be assured that it is at most 1 word, for every query.
		 */
		Statement stmt = null;
        ResultSet answer = null;
        
        String year = inputList.get(0);
        String db = "bk1.highway" + year;
        
		try {
            stmt = dbconn.createStatement();
            // Here we go. Oracle handles this one almost entirely.
            answer = stmt.executeQuery("SELECT sname, count(sname) FROM " + db
            		+ " GROUP BY sname HAVING count(sname) > 0"
            		+ " ORDER BY count(sname) DESC");
            // With some setup, anyways.
            System.out.println("Top ten states for incidents in " + year + ":");
            boolean endedEarly = false;
            if (answer != null) {
            	for (int i = 0; i < 10; i++) {
            		// Oracle iterates it for us. How nice.
            		if (answer.next()) {
                		System.out.println("#" + (i + 1) + ": " + answer.getString(1) + " had " + answer.getInt(2) + " incident reports.");
                	} else {
                		endedEarly = true;
                		break;
                	}
            	}
            }
            if (endedEarly) {
            	// Since then it wouldn't be "ten". Might aswell call it out.
            	System.out.println("Out of data!");
            }
            System.out.println();

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
