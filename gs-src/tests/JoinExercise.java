package tests;

import java.util.*;
import java.io.*;
import java.net.*;

// NOTE: save this file in the /tests directory
// Remember to start DBServer first!

public class JoinExercise {

    public static void main (String[] argv)
    {
        //runGSQLQuery ("JOIN module1:movies module1:actors");
        doJoinFromSelect ();
    }

    static void doJoinFromSelect ()
    {
        try {
            // WRITE YOUR CODE HERE
            // Perform a FETCH for each of the two tables. This will
            // give you two lists of records (in string format).
            // Write code to perform the join. You'll need to look
            // for "movieID" in each, then extract the string that
            // follows the colon up to the brace. And then compare.

            Socket soc = new Socket ("localhost", 40014);
            ArrayList <HashMap<String, String>> actorRows = makeRows ("FETCH module1:actors", soc);

        }
        catch (Exception e) {
            e.printStackTrace ();
        }

    }

    static ArrayList <HashMap<String, String>> makeRows (String query, Socket soc)
    {
        try {
            PrintWriter pw = new PrintWriter (soc.getOutputStream());
            pw.println (query);
            pw.flush ();
            LineNumberReader lnr = new LineNumberReader (new InputStreamReader(soc.getInputStream()));
            System.out.println ("Result from query: " + query);
            String line = lnr.readLine ();
            System.out.println(line);
            line = line.substring (1, line.length()-1).trim();
            System.out.println(line);
            String[] parts = line.split("\\}");
            String name = "";
            String value = "";
            for (String part: parts) {
                part = part.trim().substring(1);
                int k = part.indexOf(":");
                name = part.substring(0, k);
                value = part.substring(k+1);
                System.out.println(name);
                System.out.println(value);
            }


            /*
            while (line != null) {
                System.out.println (line);
                line = lnr.readLine ();
            }

             */
        }
        catch (Exception e) {
            e.printStackTrace ();
        }

        return null;
    }

    static void runGSQLQuery (String query)
    {
        try {
            Socket soc = new Socket ("localhost", 40014);
            PrintWriter pw = new PrintWriter (soc.getOutputStream());
            pw.println (query);
            pw.flush ();
            LineNumberReader lnr = new LineNumberReader (new InputStreamReader(soc.getInputStream()));
            System.out.println ("Result from query: " + query);
            String line = lnr.readLine ();
            while (line != null) {
                System.out.println (line);
                line = lnr.readLine ();
            }
        }
        catch (Exception e) {
            e.printStackTrace ();
        }
    }

}