
package org.gateway.server;

import org.gateway.server.*;
import org.gateway.util.*;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 * GSServer implements a simple server that:
 *     1. Serves up files to a browser
 *     2. Receives "post" data (the contents of inputfields, buttons)
 *        and hands that off to an app.
 * The server is multi-threaded in that the main thread listens for
 * socket connections. As soon as one is made, the socket is handed off
 * to a thread. Thus, the main thread runs in main() while instances
 * of the class GSServer are used for each separate connection. This
 * is why some static variables are defined (to share across threads).
 * It's best to understand GSServer in two parts:
 *     1.  The main thread that starts up, pre-loads all apps, and waits.
 *     2.  What happens with each connection.
 * NOTE: FileClassLoader uses a deprecated Java API. This will merely
 * throw up a warning, which we'll ignore.
 */

public class GSServer implements Runnable {

    //////////////////////////////////////////////////////////////
    // Part 1. main thread, and initialization.

    // The port number we're listening on: shared across all threads.
    static int portNum;

    // Given an app name, we need to fetch the app.
    static HashMap<String,ServerApp> nameToApp = new HashMap<>();

    // An ID for each thread (for debugging/logging) is created from this.
    static int IDCount = 1;


    public static void main (String[] argv)
    {
	File logFile = new File ("logs" + File.separator + "server.log");
	Log.setLogFile (logFile);
	Log.println ("Starting GSServer");

	loadApps ();

	try {
	    ServerSocket srv = new ServerSocket (portNum);

	    System.out.println ("GSServer: waiting for a connection on port " + portNum);
	    // When a connection is made, get the socket.  
	    while (true) {

		// Wait for a connection.  
		Socket soc = srv.accept ();

		// Now create an instance of GSServer to deal with the connection.  
		new Thread ( new GSServer(soc) ).start();
	    }
	}
	catch (Exception e) {
	    e.printStackTrace ();
	    e.printStackTrace (Log.getWriter());
	    System.out.println ("Fatal error in server: main()");
	}
    }


    static void loadApps ()
    {
	try {
	    // Assumption: apps are named in the server.props file.
	    FileInputStream propsFile = new FileInputStream ("properties" + File.separator + "server.props");
	    Properties props = new Properties ();
	    props.load (propsFile);
	    portNum = Integer.parseInt(props.getProperty("portNum"));
	    int numApps = Integer.parseInt(props.getProperty("numApps"));
	    System.out.println ("GSServer: read properties: port=" + portNum + " numApps=" + numApps);

	    for (int i=0; i<numApps; i++) {
		String appName = props.getProperty("app" + i);
		// Let FileClassLoader know the path from which to load algorithm
		String path = "apps";
		FileClassLoader fcl = new FileClassLoader (path);
		// Load the class for the algorithm
		Class appClass = Class.forName (appName, true, fcl);
		ServerApp app = (ServerApp) appClass.newInstance();
		nameToApp.put (appName, app);
		System.out.println (" >> app=" + appName + " loaded");
	    }
	    Log.println ("GSServer:loadApps: complete");
	}
	catch (Exception e) {
	    e.printStackTrace();
	    Log.println ("GSServer:loadApps() exception:");
	    e.printStackTrace (Log.getWriter());
	}
    }

    //////////////////////////////////////////////////////////////
    // Part 2. What happens with each connection.

    // Instance variables (one per thread): an ID and the incoming socket.
    int ID;
    Socket soc;    


    public GSServer (Socket soc)
    {
	this.ID = IDCount ++;
	this.soc = soc;
    }


    public void run ()
    {
	// This is the per-request thread, and the method needed for the
	// Runnable() interface. This is where we handle GET or POST.
	try {
	    LineNumberReader lnr = new LineNumberReader (new InputStreamReader(soc.getInputStream()));
	    String line = lnr.readLine ();
	    System.out.println ("GSServer(): received line=" + line);

	    if (line.startsWith("GECHO")) {
		// INSERT YOUR CODE HERE
	    }
	    else if (line.startsWith("GGET")) {
		// Fetch the file specified and deliver it to the browser.
		String fileName = line.substring(5,line.length());
		Log.println ("GSServer.run(): ID=" + ID + " GGET: received request for file=" + fileName);
		deliverFileToBrowser (fileName);
	    }
	    else if (line.startsWith("GIMAGE")) {
		// Fetch the image file and deliver that.
		String fileName = line.substring(7,line.length());
		Log.println ("GSServer.run():  ID=" + ID + " GIMAGE: received request for file=" + fileName);
		deliverImageFileToBrowser (fileName);
	    }
	    else if (line.startsWith("GPOST")) {
		// A post message is the most complex since it may have
		// multiple lines, one per parameter. This is when the browser
		// is sending form data to an app.
		String numParamsStr = line.substring(6,line.length());
		int numParams = Integer.parseInt(numParamsStr.trim());
		Log.println ("GSServer.run():  ID=" + ID + " GPOST: received POST request with numParams=" + numParams);
		HashMap<String,String> params = new HashMap<>();
		for (int i=0; i<numParams; i++) {
		    line = lnr.readLine ();
		    String[] parts = line.split("=");
		    if ( (parts == null) || (parts.length == 1) || (parts[0] == null) || (parts[1] == null) ) {
			Log.print (">> GPOST: ID=" + ID + " ERROR in params: line=" + line);
		    }
		    else {
			Log.print (">> GPOST: ID=" + ID + " extracted name=" + parts[0] + " value=" + parts[1]);
			params.put (parts[0], parts[1]);
		    }
		}

		// Find out which app.
		String appName = params.get ("app");
		ServerApp app = nameToApp.get (appName);
		if (app != null) {
		    PrintWriter pw = new PrintWriter(soc.getOutputStream());
		    app.handlePost (params, pw);
		    pw.flush ();  pw.close ();
		}
		else {
		    Log.println ("GSServer.run():  ID=" + ID + " ERROR: Unknown app: " + appName);
		}
	    }
	    else {
		Log.println ("GSServer.run():  ID=" + ID + " ERROR: Unknown GSTP command: " + line);
		System.out.println ("GSServer.run():  ID=" + ID + " ERROR: Unknown GSTP command: " + line);
	    }
	}
	catch (Exception e) {
	    System.out.println ("Fatal error in run(): Server thread: ID=" + ID);
	    e.printStackTrace (Log.getWriter());
	}
    }


    void deliverFileToBrowser (String filename)
    {
	try {
	    String path = "gsmlpages" + File.separator + filename;
	    Log.println ("GSServer.deliverFile(): ID=" + ID + " request for filename=[" + filename + "] sep=[" + File.separator + "] path=[" + path + "]");
	    File file = new File (path);
	    if (file.exists()) {
		LineNumberReader lnr = new LineNumberReader (new FileReader (file));
		PrintWriter pw = new PrintWriter (soc.getOutputStream());
		String line = lnr.readLine ();
		while (line != null) {
		    pw.println (line);
		    line = lnr.readLine ();
		}
		pw.flush ();
		pw.close ();
	    }
	    else {
		System.out.println ("ERROR: GSServer: file does not exist: path=" + path);
		Log.println ("GSServer:deliverFile(): ID=" + ID + " file does not exist: path=" + path);
	    }
	}
	catch (Exception e) {
	    System.out.println ("ERROR: in GSServer.deliverFileToBrowser(): see log");
	    e.printStackTrace (Log.getWriter());
	}
    }


    void deliverImageFileToBrowser (String filename)
    {
	String path = "gsmlpages" + File.separator + filename;
	try {
	    System.out.println ("GSServer.deliverImage(): filename=[" + filename + "] sep=[" + File.separator + "] path=[" + path + "]");
	    File file = new File (path);
	    if (file.exists()) {
		FileInputStream fis = new FileInputStream (file);
		OutputStream outStream = soc.getOutputStream ();
		int inValue = fis.read ();
		while (inValue >= 0) {
		    byte b = (byte) inValue;
		    outStream.write (b);
		    inValue = fis.read ();
		}
		fis.close ();
		outStream.flush ();
		outStream.close ();
	    }
	    else {
		System.out.println ("ERROR: GSServer: file does not exist: path=" + path);
	    }
	}
	catch (Exception e) {
	    Log.println ("GSServer:deliverFile(): ID=" + ID + " file does not exist: path=" + path);
	    e.printStackTrace (Log.getWriter());
	    System.out.println ("ERROR: in GSServer.deliverFileToBrowser()");
	}
    }


}


