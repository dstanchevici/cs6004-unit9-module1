
package org.gateway.server;

import java.util.*;
import java.io.*;
import java.net.*;

public interface ServerApp {

    /*
     * An app needs parameters from the webpage. For each such parameter, 
     * there's a name, and possibly a value. For example, a button will
     * have a name, but no value. A textfield has both a name and a value
     * (the value is the text inside the field). The app also needs 
     * a way to write GSML back to the browser, via println() calls.
     */

    public void handlePost (HashMap<String,String> pageParameters, PrintWriter outputToBrowser);

}
