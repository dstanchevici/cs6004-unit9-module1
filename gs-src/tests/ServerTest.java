package tests;

import java.util.*;
import java.io.*;
import java.net.*;

public class ServerTest {

    public static void main (String[] argv)
    {
	sendMessage ("GECHO Hello");
    }

    static void sendMessage (String message)
    {
		try {
			Socket soc = new Socket ("localhost", 40013);
			PrintWriter pw = new PrintWriter (soc.getOutputStream());
			pw.println (message);
			pw.flush ();
			LineNumberReader lnr = new LineNumberReader (new InputStreamReader(soc.getInputStream()));
			System.out.println ("Result received: ");
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
