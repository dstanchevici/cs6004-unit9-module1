
import org.gateway.server.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class ExampleApp implements ServerApp {

    // This is the single method to implement from the ServerApp interface.

    // Note: a browser's "post" is a list of name/value pairs, so these
    // name/values are passed as a hashmap. The only other thing an
    // app needs is a way to write to the browser. This occurs through
    // a PrintWriter, via the familiar println() method.

    public void handlePost (HashMap<String,String> pageParameters, PrintWriter outputToBrowser)
    {
	// Here, we need to write out the whole gsml document starting 
	// from the <doc> tag.
	outputToBrowser.println ("<doc background=FFFFFF foreground=000000>");
	outputToBrowser.println ("<title size=20> Response from Example App");
	outputToBrowser.println ("<text size=20>");
	outputToBrowser.println ("Parameters:");

	// We are merely mirroring back what the browser sent us. This is
	// of course NOT what a real app does, but it's useful for debugging.
	for (String name: pageParameters.keySet()) {
	    String value = pageParameters.get (name);
	    System.out.println ("name=[" + name + "] and value=[" + value + "]");

	    outputToBrowser.println ("name=[" + name + "] and value=[" + value + "]");
	}
	outputToBrowser.println ("</text>");
	outputToBrowser.flush ();
    }


}
