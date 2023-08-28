package tests;

import java.util.*;
import java.io.*;
import java.net.*;

public class DBInsert {

    public static void main (String[] argv)
    {
	testQuery ("INSERT movies:movies [ {revenue:1.44E8}  {popularity:96.4}  {movieID:8001}  {title:Gravity}  {director:Alphonso Cuaron} {budget:2.0E6} ]");
    }

    static void testQuery (String query)
    {
	try {
	    Socket soc = new Socket ("localhost", 40014);
	    PrintWriter pw = new PrintWriter (soc.getOutputStream());
	    System.out.println ("***********************************");
	    System.out.println ("SENDING query=" + query);
	    pw.println (query);
	    pw.flush ();
	    LineNumberReader lnr = new LineNumberReader (new InputStreamReader(soc.getInputStream()));
	    Thread.sleep (200);
	    String line = lnr.readLine ();
	    System.out.println ("Result from query: " + line);
	    System.out.println ("***********************************");
	    
	}
	catch (Exception e) {
	    e.printStackTrace ();
	}
    }

}
