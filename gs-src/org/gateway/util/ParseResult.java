package org.gateway.util;

import java.util.*;
import java.awt.*;

public class ParseResult {

    public String host;
    public int port;
    public String filename;

    public ParseResult (String host, int port, String filename)
    {
	this.host = host;  this.port = port;  this.filename = filename;
    }
}
