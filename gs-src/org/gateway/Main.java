package org.gateway;

import org.gateway.browser.*;
import org.gateway.server.*;
import org.gateway.dbase.*;

public class Main {

    public static void main (String[] argv)
    {
	if (argv.length != 1) {
	    System.out.println ("Usage: java -jar gspackage.jar [browser|server|dbase]");
	    System.exit (0);
	}
	if (argv[0].equalsIgnoreCase("browser")) {
	    GSBrowser.main (null);
	}
	else if (argv[0].equalsIgnoreCase("server")) {
	    GSServer.main (null);
	}
	else if (argv[0].equalsIgnoreCase("dbase")) {
	    DBServer.main (null);
	}
	else {
	}
    }

}
