package tests;

import java.util.*;
import java.io.*;
import java.net.*;

public class Module1TestProject {

    public static void main (String[] argv)
    {
        testQuery ("PROJECT module1:actors [{actor:_} ]");
        System.out.println ("--------------------------------------");
        testQuery ("PROJECT module1:genres [{genre:_}]");
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