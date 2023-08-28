package tests;

import java.util.*;
import java.io.*;
import java.net.*;

public class DBJoinTiming {

    public static void main (String[] argv)
    {
	long startTime = System.currentTimeMillis();
	testQuery ("JOIN movies:movies movies:actors");
	long endTime = System.currentTimeMillis();
	System.out.println ("Time taken (in ms): " + (endTime-startTime));
	
    }

    static void testQuery (String query)
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
