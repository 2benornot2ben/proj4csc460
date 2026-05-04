/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Name: Benjamin Kanter & Zhaoyi Li
 * Course: CSC 460
 * Program: ToUsable
 * Instructor: Mr. McCann
 * Due: 2026-05-5
 * 
 * Description: Converts oracle queries to java ones!
 * Very simple, its just a quick way of doing so without making mistakes.
 * 
 * Written by Ben to expedite creation speed.
 * 
 * Known Bugs: None!
 * Unknown Bugs: Hopefully None!
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.util.Scanner;

public class ToUsable {
	public static void main(String args[]) {
		/* Just paste in the query here!
		 * Then end it with [enter] and then "STOP"
		 * Note, for quick pasting, click the start of the query, hold shift, press
		 * the down arrow, then ctrl + c. Just a quick select.
		 */
		Scanner scanner = new Scanner(System.in);
		String input = scanner.useDelimiter("STOP").next();
		input = input.replaceAll("\\r\\n", " ");
		input = input.replaceAll("\\s+", " ").strip().trim();
		input = input.replaceAll("\\s*;+$", "");
		System.out.print(input);
		scanner.close();
	}
}
