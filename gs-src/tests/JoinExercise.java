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

    static ArrayList<String> mergedRows = new ArrayList<>();

    static void doJoinFromSelect ()
    {

            // WRITE YOUR CODE HERE
            // Perform a FETCH for each of the two tables. This will
            // give you two lists of records (in string format).
            // Write code to perform the join. You'll need to look
            // for "movieID" in each, then extract the string that
            // follows the colon up to the brace. And then compare.

        ArrayList <HashMap<String, String>> actorRows = makeRows ("FETCH module1:actors");
        ArrayList <HashMap<String, String>> movieRows = makeRows ("FETCH module1:movies");


        for (HashMap<String, String> actorRow: actorRows) {
            for (HashMap<String, String> movieRow: movieRows) {
                if ( actorRow.get("movieID").equals( movieRow.get("movieID") )  )
                    mergedRows.add( merge (actorRow, movieRow) );
            }
        }

        for (String row: mergedRows) {
            System.out.println(row);
        }

    }

    static String merge (HashMap<String, String> actorRow, HashMap<String, String> movieRow)
    {
        String mergedRow = "[ ";
        mergedRow += "{actor:" + actorRow.get("actor") + "} ";
        for (HashMap.Entry<String, String> entry: movieRow.entrySet()) {
            mergedRow += "{" + entry.getKey() + ":" + entry.getValue() + "} ";
        }
        mergedRow += "]";
        return mergedRow;
    }

    static ArrayList <HashMap<String, String>> makeRows (String query)
    {
        ArrayList <HashMap<String, String>> rows = new ArrayList<>();
        try {
            Socket soc = new Socket ("localhost", 40014);
            PrintWriter pw = new PrintWriter (soc.getOutputStream());
            pw.println (query);
            pw.flush ();

            LineNumberReader lnr = new LineNumberReader (new InputStreamReader(soc.getInputStream()));
            System.out.println ("Result from query: " + query);
            String line = lnr.readLine ();
            //System.out.println("FIRST LINE: " + line);
            while (line != null) {
                //System.out.println(line);
                line = line.substring(1, line.length() - 1).trim();
                //System.out.println(line);
                String[] parts = line.split("\\}");
                HashMap<String, String> row = new HashMap<>();
                for (String part : parts) {
                    part = part.trim().substring(1);
                    int k = part.indexOf(":");
                    String name = part.substring(0, k);
                    String value = part.substring(k + 1);
                    //System.out.println(name);
                    //System.out.println(value);
                    row.put (name, value);
                }
                rows.add (row);
                line = lnr.readLine();
            }
        }
        catch (Exception e) {
            e.printStackTrace ();
        }
        return rows;
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