
package org.gateway.util;

import java.util.*;
import java.time.*;
import java.io.*;

public class Log {

    static File logFile = null;
    static PrintWriter logWriter = null;

    public static PrintWriter getWriter ()
    {
	return logWriter;
    }

    public static void setLogFile (File file)
    {
	try {
	    logFile = file;
	    if (file != null) {
		logWriter = new PrintWriter (new FileWriter(file));
		return;
	    }
	}
	catch (IOException e) {
	}
	System.out.println ("ERROR: Log: could not open Log file=" + file);
	System.exit (0);
    }
  
    public static void print (String str)
    {
	if (logFile != null) {
	    logWriter.print (str);
	}
    }

    public static void println (String str)
    {
	if (logFile != null) {
	    logWriter.println (str);
	    logWriter.println ("-----  Above log entry written at " + LocalTime.now() + "    -------------\n");
	    logWriter.flush ();
	}
    }
  
}
