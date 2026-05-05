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
 * It also requires the file queriesMap.txt, which maps query inputs to the actual queries.
 * 
 * Known Bugs: None!
 * Unknown Bugs: Hopefully None!
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

public class Main {
	// Super convenient.
	static ArrayList<String> validYears = new ArrayList<String>(Arrays.asList("1980", "1995", "2010", "2025"));
	public static void main(String[] args) {
		final String oracleURL = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";
		String username = null,
	               password = null;
		
		Scanner scanUse = new Scanner(System.in);
		
		HashMap<String, String> toQuery = new HashMap<>();
		BufferedReader readr = null;
		try {
			readr = new BufferedReader(new FileReader(new File("queriesMap.txt")));
		} catch (FileNotFoundException e) {
			System.out.println("Error finding/reading queriesMap!");
			scanUse.close();
			return;
		}
		
		// Read the file for the queries
		
		String line = "";
		try {
			while ((line = readr.readLine()) != null) {
				String[] splitLine = line.split(":", 2);
				if (splitLine.length == 2) {
					toQuery.put(splitLine[0], splitLine[1].strip());
				}
			}
		} catch (IOException e) {
			System.out.println("Read Failure!");
			scanUse.close();
			return;
		}
		
		try {
			readr.close();
		} catch (IOException e) {
			System.out.println("Failed to close reader..?");
			scanUse.close();
			return;
		}
		
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
        
        // We loop until it's over.
		
        
        while (true) {
        	// This is the MAIN MENU. Not any SQL happening here.
        	ArrayList<String> inputList = new ArrayList<String>();
        	System.out.println("Please, first select method of usage:");
        	System.out.println("a : Admin. Get access to most direct-modification queries.");
        	System.out.println("b : User. Use it like an actual user! (ARGS: 1)");
        	System.out.println("c : Debug. We needed these for testing anyways.");
        	System.out.println("EXIT : End program.");
        	
        	String[] nextLine = convertToUsable(scanUse.nextLine());
        	if (nextLine.length == 0) continue;
        	
        	if (nextLine[0].toLowerCase().startsWith("a")) {
        		// No args are needed.
        		adminpanel(scanUse, inputList, dbconn, toQuery);
        	} else if (nextLine[0].toLowerCase().startsWith("b")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Username: ") == 1) {
        			continue;
        		}
        		userpanel(scanUse, inputList, dbconn, toQuery);
        	} else if (nextLine[0].toLowerCase().startsWith("c")) {
        		// Same here
        		extraspanel(scanUse, inputList, dbconn, toQuery);
        	} else if (nextLine[0].toLowerCase().startsWith("exit")) {
        		break;
        	}
        }
		
        System.out.println("Program Terminated.");
        scanUse.close();
	}
	
	public static int getInput(Scanner scanner, ArrayList<String> inputList, String[] responseString,
			String... values) {
		/* This is how input is gotten. This function is actually fairly complex!
		 * The user has a lot of ways to deal with data.
		 * They can put it all on the same line, in which it accepts it space-seperated (kind of)
		 * They can also let it prompt them, which means one at a time.
		 * Prompts are determines with the ... values!
		 * Parameters: scanner, the basic input scanner.
		 * inputList, the list to fill with data.
		 * responseString, the overflow data from when this function was called.
		 * It will take data from there first.
		 * values, an unknown amount of strings which determines the prompts.
		 * It also determines the amount of inputs it's looking for.
		 * Returns: N/A (Modifies inputList)
		 */
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
					inputList.add(convertToUsable(strVal)[0]);
				}
			}
		}
		return 0;
	}
	
	public static void adminpanel(Scanner scanUse, ArrayList<String> inputList, Connection dbconn,
			HashMap<String, String> queries) {
		/* The admin panel!
		 * Admin is not an account - it's closer to direct access.
		 * This is also where the 4 required queries are, q1 to q4 respectively.
		 * Parameters: scanUse, the input scanner
		 * inputList, the arraylist to use for getting input (efficiency)
		 * dbconn, the connection to the database.
		 * queries, the hashmap from basic letter queries to the actual ran ones.
		 * Returns: N/A (Prints)
		 */
		inputList.clear();
		while (true) {
        	System.out.println("Admin Panel:");
        	System.out.println("a : Add User (Args: Like, a lot, idk)");
        	System.out.println("b : Update User (Args: Also like a lot, idk)");
        	System.out.println("c : Delete User (Args: 1)");
        	System.out.println("d : Update Subscription of User (Args: 2)");
        	System.out.println("e : Generate Invoice (Args: 1)"); 
        	System.out.println("f : Assign Ticket (Args: 2)");
        	System.out.println("g : Modify Ticket (Args: 4)");
        	System.out.println("h : Modify Invoice (Args: 3)");
        	System.out.println("i : Mark Invoice as Paid (Args: 1)");
        	
        	System.out.println("q1 : List all bookmarked messages for user (Args: 1)");
        	System.out.println("q2 : List all users with unpaid invoices");
        	System.out.println("q3 : List the most helpful personas");
        	System.out.println("q4 : List activity & rating for members in certain tier (Args: 1)");
        	System.out.println("q5 : List open support tickets.");
        	System.out.println("q6 : List all users.");
        	System.out.println("q7 : List all invoices.");
        	System.out.println("Logout : Return to main menu.");
        	
        	// Modify determines which of the two query types is ran.
        	boolean modify = false;
        	// This is basically an advanced .split(" ")
        	String[] nextLine = convertToUsable(scanUse.nextLine());
        	if (nextLine.length == 0) continue;
        	
        	if (nextLine[0].toLowerCase().equals("a")) {
        		if (getInput(scanUse, inputList, nextLine, "idk a lot of args here") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryAddUser(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equals("b")) {
        		if (getInput(scanUse, inputList, nextLine, "idk a lot of args here") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryUpdateUser(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equals("c")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Username: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryDeleteUser(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equals("d")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Username: ",
        				"Input Subscription Tier: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryUpdateSub(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equals("e")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Username: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryGenerateInvoice(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equals("f")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Ticket ID: ",
        				"Input Agent ID: ") == 1) {
        			continue;
        		}
        		modify = true;

        		//queryAssignTicket(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equals("g")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Ticket ID: ",
        				"Input Status: ", "Input Duration: ", "Input Outcome or NULL: ") == 1) {
        			continue;
        		}
        		modify = true;

        		//queryModifyTicket(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equals("h")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Invoice ID: ",
        				"Input Amount Due: ", "Input Status: ") == 1) {
        			continue;
        		}
        		modify = true;

        	} else if (nextLine[0].toLowerCase().equals("i")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Invoice ID: ") == 1) {
        			continue;
        		}
        		modify = true;

        	} else if (nextLine[0].toLowerCase().equals("q1")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Username: ") == 1) {
        			continue;
        		}
        		//querySpecialOne(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equals("q2")) {

        		//querySpecialTwo(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equals("q3")) {

        		//querySpecialThree(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equals("q4")) {
        		if (getInput(scanUse, inputList, nextLine, "Membership Tier: ") == 1) {
        			continue;
        		}
        		//querySpecialFour(inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equals("q5")) {
        		
        	} else if (nextLine[0].toLowerCase().equals("q6")) {
        		
        	} else if (nextLine[0].toLowerCase().equals("q7")) {
        		
        	} else if (nextLine[0].toLowerCase().startsWith("logout")) {
        		break;
        	} else {
        		continue;
        	}
        	
        	if (!modify) queryPlugin(inputList, dbconn, queries.get("ADMIN" + nextLine[0].toLowerCase()));
        	else queryModify(inputList, dbconn, queries.get("ADMIN" + nextLine[0].toLowerCase()));
        }
	}

	public static void userpanel(Scanner scanUse, ArrayList<String> inputList, Connection dbconn,
			HashMap<String, String> queries) {
		/* The user panel!
		 * Users have accounts! Therefore, you must log into one!
		 * (Password is emitted because nobody cares, and I think it's not under doubt
		 * that I can write the code for it if it was)
		 * Users are a bit limited. Which makes sense, they're just users.
		 * Parameters: scanUse, the input scanner
		 * inputList, the arraylist to use for getting input (efficiency)
		 * dbconn, the connection to the database.
		 * queries, the hashmap from basic letter queries to the actual ran ones.
		 * Returns: N/A (Prints)
		 */
		// In every query, arg 1 will be the userId.
		int userId = queryLogIn(inputList, dbconn);
		if (userId == -1) {
			// Not an account? Then you cannot use this.
			System.out.println("User not found!");
			return;
		}
		String user = inputList.get(0);
		inputList.clear();
		while (true) {
        	System.out.println("Enter query for " + user);
        	System.out.println("aa : Start Conversation (Args: 3)");
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
        	System.out.println("ea : Create Ticket (Args: 1)");
        	
        	System.out.println("q1 : List conversations.");
        	System.out.println("q2 : List personas.");
        	System.out.println("q3 : List workspaces.");
        	System.out.println("q4 : List messages in conversation (Args: 1)");
        	System.out.println("q5 : Show review in conversation (Args: 1)");
        	
        	System.out.println("Logout : Return to main menu.");
        	
        	// Modify determines which of the two query types is ran.
        	boolean modify = false;
        	// This is basically an advanced .split(" ")
        	String[] nextLine = convertToUsable(scanUse.nextLine());
        	if (nextLine.length == 0) continue;
        	
        	if (nextLine[0].toLowerCase().equalsIgnoreCase("aa")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Conversation Title: ",
        				"Input Persona: ", "Input Prompt: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryCreateConversation(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("ab")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Conversation Title: ",
        				"Input Message: ", "(Input AI Message too): ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryAddMessage(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("ac")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Conversation Title: ",
        				"Input Identifier: ", "Input Review OR Null", "Input Rating OR Null") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryReviewMessage(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("ba")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Conversation Title: ",
        				"Input Workspace Title: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryAddConversationToWorkspace(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("bb")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Conversation Title: ",
        				"Input Workspace Title: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryRemoveConvoFromWorkspace(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("bc")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Workspace Title: ",
        				"Input Visibility: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryCreatePersona(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("ca")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Persona Name: ",
        				"Input Persona Instructions: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryModifyPersona(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("cb")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Persona Name: ",
        				"Input Persona Instructions: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryRemovePersona(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("cc")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Persona Name: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryCreatePrompt(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("da")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Prompt Name: ",
        				"Input Prompt Category: ", "Input Prompt: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryModifyPrompt(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("db")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Prompt Name: ",
        				"Input Prompt: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryMovePromptToWS(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("dc")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Prompt Name: ",
        				"Input Workspace: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryRemoveConvoFromWorkspace(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("ea")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Topic: ") == 1) {
        			continue;
        		}
        		modify = true;
        		//queryCreateTicket(userId, inputList, dbconn);
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("q1")) {

        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("q2")) {

        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("q3")) {

        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("q4")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Conversation Title: ") == 1) {
        			continue;
        		}
        		
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("q5")) {
        		if (getInput(scanUse, inputList, nextLine, "Input Conversation Title: ") == 1) {
        			continue;
        		}
        		
        	} else if (nextLine[0].toLowerCase().equalsIgnoreCase("logout")) {
        		break;
        	} else {
        		continue;
        	}
        	inputList.add(0, "" + userId);
        	if (!modify) queryPlugin(inputList, dbconn, queries.get("USER" + nextLine[0].toLowerCase()));
        	else queryModify(inputList, dbconn, queries.get("USER" + nextLine[0].toLowerCase()));
        }
	}
	
	public static void extraspanel(Scanner scanUse, ArrayList<String> inputList, Connection dbconn,
			HashMap<String, String> queries) {
		/* The fake panel!
		 * This is debug.
		 * But, it felt cool to keep around, so I did.
		 * Parameters: scanUse, the input scanner
		 * inputList, the arraylist to use for getting input (efficiency)
		 * dbconn, the connection to the database.
		 * queries, the hashmap from basic letter queries to the actual ran ones.
		 * Returns: N/A (Prints)
		 */
		inputList.clear();
		while (true) {
        	System.out.println("Extras Panel:");
        	System.out.println("a : Print Tables");
        	System.out.println("Logout : Return to main menu.");
        	
        	// Modify determines which of the two query types is ran.
        	boolean modify = false;
        	// This is basically an advanced .split(" ")
        	String[] nextLine = convertToUsable(scanUse.nextLine());
        	if (nextLine.length == 0) continue;
        	
        	if (nextLine[0].toLowerCase().startsWith("a")) {
        		
        	} else if (nextLine[0].toLowerCase().startsWith("logout")) {
        		break;
        	} else {
        		continue;
        	}
        	if (!modify) queryPlugin(inputList, dbconn, queries.get("DEBUG" + nextLine[0].toLowerCase()));
        	else queryModify(inputList, dbconn, queries.get("DEBUG" + nextLine[0].toLowerCase()));
        }
	}
	
	private static void queryPlugin(ArrayList<String> iList, Connection dbconn, String query) {
		/* Runs the queries! Yes, all go to this one function!
		 * Specifically, the SELECTS. Anything that needs to modify goes to the one below.
		 * This function is extremely clever with how it handles things.
		 * It can accept up to 10 args, or 9 if it's user (since one is prefilled)
		 * How are the args used? The strings can have an "iList.get(n)" in them!
		 * It will replace those with the user's args!
		 * As for security, a user cannot have spaces in their args, so it cannot be breached...
		 * ... Except for ones which are meant to be strings. It wouldn't make sense to limit those.
		 * So, it accepts string queries with spaces.
		 * Parameters: iList, the list of inputs, to which the fake gets pull from directly.
		 * dbconn, the connection to the database.
		 * Query, the query to run.
		 * Returns: N/A (Prints)
		 */
		Statement stmt = null;
        ResultSet answer = null;
        
        String queryMod = query;
        // Java in my SQL code? Well... Yes!
        int indexMod = queryMod.indexOf("iList.get(");
        	
        // (While there's more...)
        while (indexMod != -1) {
        	// The users cannot write these parts, so we're safe.
        	int val = Integer.parseInt("" + queryMod.charAt(indexMod + 10));
        	String str = iList.get(val);
        	// If there is a space...
        	if (str.indexOf(" ") >= 0) {
        		// Then is it a string query?
        		int indexOne = indexMod - 1;
        		int indexTwo = indexMod + 12;
        		// (Two additional checks for OOB exceptions)
        		if (indexOne < 0 || indexTwo >= queryMod.length()
        			|| queryMod.charAt(indexOne) != '\''
        			|| queryMod.charAt(indexTwo) != '\'') {
        			// Technically speaking, you could use quotes for anything, as long as you don't
        			// include spaces in them.
        			// But they will be deleted.
        			System.out.println("QUERY DENIED - Do not use quotes for non-strings!");
        			return;
        		}
        	}
        	// Replace it directly
        	queryMod = queryMod.substring(0, indexMod) + str
        			+ queryMod.substring(indexMod + 12);
        	indexMod = queryMod.indexOf("iList.get(");
        }
        
		try {
			// Create and Execute
            stmt = dbconn.createStatement();
            answer = stmt.executeQuery(queryMod);
            if (answer != null) {
            	// Grab metadata for column info
            	ResultSetMetaData deta = answer.getMetaData();
            	int columns = deta.getColumnCount();
            	while (answer.next()) {
            		// Print
            		printLine(deta, answer, columns);
            	}
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
	
	private static void queryModify(ArrayList<String> iList, Connection dbconn, String query) {
		/* Runs the modifying queries! Yes, all go to this one function!
		 * Specifically, the non-SELECTS. Anything that needs to print goes to the one above.
		 * This function is extremely clever with how it handles things.
		 * It can accept up to 10 args, or 9 if it's user (since one is prefilled)
		 * How are the args used? The strings can have an "iList.get(n)" in them!
		 * It will replace those with the user's args!
		 * As for security, a user cannot have spaces in their args, so it cannot be breached...
		 * ... Except for ones which are meant to be strings. It wouldn't make sense to limit those.
		 * So, it accepts string queries with spaces.
		 * Parameters: iList, the list of inputs, to which the fake gets pull from directly.
		 * dbconn, the connection to the database.
		 * Query, the query to run.
		 * Returns: N/A (Modifies DB)
		 */
		Statement stmt = null;
		
		String queryMod = query;
        // Java in my SQL code? Well... Yes!
        int indexMod = queryMod.indexOf("iList.get(");
        	
        // (While there's more...)
        while (indexMod != -1) {
        	// The users cannot write these parts, so we're safe.
        	int val = Integer.parseInt("" + queryMod.charAt(indexMod + 10));
        	String str = iList.get(val);
        	// If there is a space...
        	if (str.indexOf(" ") >= 0) {
        		// Then is it a string query?
        		int indexOne = indexMod - 1;
        		int indexTwo = indexMod + 12;
        		// (Two additional checks for OOB exceptions)
        		if (indexOne < 0 || indexTwo >= queryMod.length()
        			|| queryMod.charAt(indexOne) != '\''
        			|| queryMod.charAt(indexTwo) != '\'') {
        			// Technically speaking, you could use quotes for anything, as long as you don't
        			// include spaces in them.
        			// But they will be deleted.
        			System.out.println("QUERY DENIED - Do not use quotes for non-strings!");
        			return;
        		}
        	}
        	// Replace it directly
        	queryMod = queryMod.substring(0, indexMod) + str
        			+ queryMod.substring(indexMod + 12);
        	indexMod = queryMod.indexOf("iList.get(");
        }
        
		try {
			// Create and Update
            stmt = dbconn.createStatement();
            int val = stmt.executeUpdate(queryMod);
            // Autocommit is on, so no need to commit.
            if (val != 0) System.out.println("Done! Affected " + val + " rows.");
            else System.out.println("Possible Fail: Affected 0 rows. May be due to "
            		+ "user error OR simply nothing being eligible.");
            System.out.println("");

        } catch (SQLException e) {

                System.err.println("*** SQLException:  "
                    + "Could not fetch query results.");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());

        }
	}
	
	private static int queryLogIn(ArrayList<String> inputList, Connection dbconn) {
		/* A very specific query with a very specific job:
		 * Logging the user in!
		 * This is very similar to queryPlugin, except it has only one purpose,
		 * and the only reason it is used is because it needs a return value.
		 * The query is already known, so no query arg.
		 * Returns -1 if one is not found.
		 * Parameters: iList, the list of inputs, to which the fake gets pull from directly.
		 * dbconn, the connection to the database.
		 * Returns: int (userId or -1)
		 */
		Statement stmt = null;
        ResultSet answer = null;
        
        String username = inputList.get(0);
        
		try {
            stmt = dbconn.createStatement();
            // Query is already known
            answer = stmt.executeQuery("SELECT userid FROM instuser WHERE username = '" + username + "'");
            if (answer != null) {
            	while (answer.next()) {
            		// Return first. There should only be one, anyways.
            		return (answer.getInt(1));
            	}
            }

        } catch (SQLException e) {

                System.err.println("*** SQLException:  "
                    + "Could not fetch query results.");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);

        }
		// Oops, no results!
		return -1;
	}
	
	private static void printLine(ResultSetMetaData mSet, ResultSet set, int columns) throws SQLException {
		/* Prints out a line of query results!
		 * Uses the column number and names from mSet to do so.
		 * Uses a | Key: Value | Key: Value | format.
		 * Parameters: mSet, which contains the metadata of the query.
		 * set, which contains the non-meta data of the query.
		 * columns, which is just gotten from mSet, but we avoid repeating ourselves w/ an arg.
		 * Returns: N/A (Prints)
		 */
		StringBuilder buildor = new StringBuilder();
		buildor.append(" | ");
		// 1? Yes! Set is 1-indexed!
		for (int i = 1; i <= columns; i++) {
			buildor.append(mSet.getColumnName(i));
			buildor.append(": ");
			buildor.append(set.getObject(i));
			buildor.append(" | ");
		}	
		System.out.println(buildor.toString());
	}
	
	private static String[] convertToUsable(String input) {
		/* Advanced String.split(" ").
		 * It works just like it, but it accepts ' and " as markers for "connected data".
		 * While "in a quote", it won't split up the strings.
		 * Note that, because of how it's made, "hi there' is perfectly okay.
		 * Parameters: input, the input to split up.
		 * Returns: String[], the split string.
		 */
		boolean inquotes = false;
		StringBuilder buildor = new StringBuilder();
		ArrayList<String> stringParts = new ArrayList<String>();
		// Actually, a very simple method.
		for (char c : input.toCharArray()) {
			if (c == '\'' || c == '"') {
				inquotes = !inquotes;
			} else if (c == ' ' && !inquotes) {
				// (If not empty)
				if (buildor.length() > 0) {
					stringParts.add(buildor.toString());
					buildor.setLength(0);
				}
			} else {
				buildor.append(c);
			}
		}
		// Extra append since, what if it didn't end with a space?
		if (buildor.length() > 0) stringParts.add(buildor.toString());
		return stringParts.toArray(new String[0]);
	}
}
