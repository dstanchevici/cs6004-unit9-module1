
import org.gateway.server.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class RegisterApp implements ServerApp {

    public void handlePost (HashMap<String,String> pageParameters, PrintWriter outputToBrowser)
    {
	// Write the preamble.
	outputToBrowser.println ("<doc background=FFFFFF foreground=000000>");
	outputToBrowser.println ("<title size=20> Registration Successful!");
	outputToBrowser.println ("<text size=20>");

	// Extract the parameters.
	String username = pageParameters.get("username");
	String password = pageParameters.get("password");
	outputToBrowser.println ("Your username and password: " + username + "  " + password);
	outputToBrowser.println ("</text>");

	// Write out something to the browser.
	outputToBrowser.println ("<text size=15>");
	outputToBrowser.println ("We promise to keep you updated with pledge drives");
	outputToBrowser.println ("</text>");

	outputToBrowser.flush ();
    }


}
