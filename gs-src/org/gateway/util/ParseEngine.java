package org.gateway.util;

import org.gateway.util.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/**
 * A catch-all class for a bunch of static methods useful in parsing.
 * The main purpose is to hide all the ugly parsing code here.
 */

public class ParseEngine {

    // This method returns name/value pairs, extracting them from
    // a single string (line).

    public static HashMap<String, String> extractAttributes (String line, int numAttributes)
    {
	try {
	    // Input: "name1=val1 name2=val2 ..."
	    String[] parts = line.split ("\\s");

	    if (parts.length != numAttributes) return null;

	    HashMap<String,String> attributes = new HashMap<>();
	    
	    for (int i=0; i<parts.length; i++) {
		int p = parts[i].indexOf ('=');
		String attrName = parts[i].substring(0,p);
		String attrVal = parts[i].substring(p+1,parts[i].length());
		attributes.put (attrName, attrVal);
	    }

	    return attributes;
	}
	catch (Exception e) {
	    return null;
	}
    }


    public static Color extractColor (String s)
    {
	// Assume: input is a string with a HEX red-green-blue value,
	// such as FF23BB
	try {
	    return Color.decode("0x"+s);
	}
	catch (Exception e) {
	    System.out.println ("Improper color: " + s);
	    return null;
	}
    }


    public static int stringToInt (String s, int defaultValue)
    {
	try {
	    return Integer.parseInt (s.trim());
	}
	catch (NumberFormatException e) {
	    return defaultValue;
	}
    }

    public static ArrayList<String> extractWordsFromString (String line)
    {
	ArrayList<String> words = new ArrayList<>();

	// Split by whitespace.
	String[] parts = line.split ("\\s");

	if ( (parts == null) || (parts.length==1) ) {
	    words.add (line);
	    return words;
	}

	for (String s: parts) {
	    s = s.trim();
	    if (s.length() > 0) {
		words.add (s);
	    }
	}
	return words;
    }


    public static ParseResult extractHostPortFilename (String url)
    {
	// Input: url starting with gstp://, e.g. gstp://localhost:40013/test.gsml
	// Goal: extract "localhost", 40013, and test.gsml.
	try {
	    String fullpath = url.substring(7,url.length());
	    int k = fullpath.indexOf('/');
	    String hostAndPort = fullpath.substring(0,k);
	    String filename = fullpath.substring(k+1,fullpath.length());
	    k = hostAndPort.indexOf(':');
	    String host = hostAndPort.substring (0,k);
	    String portStr = hostAndPort.substring (k+1,hostAndPort.length());
	    int port = Integer.parseInt (portStr.trim());
	    return new ParseResult (host, port, filename);
	}
	catch (Exception e) {
	    e.printStackTrace (Log.getWriter());
	}
	Log.println ("ParseEngine: hostPortFile(): error: url=" + url);
	return null;
    }


    public static ParseResult extractHostPort (String url)
    {
	// Input: url starting with gstp://, e.g. gstp://localhost:40013
	// Goal: extract "localhost", 40013.
	try {
	    String fullpath = url.substring(7, url.length());
	    int k = fullpath.indexOf(':');
	    String host = fullpath.substring (0,k);
	    String portStr = fullpath.substring (k+1, fullpath.length());
	    int port = Integer.parseInt (portStr.trim());
	    return new ParseResult (host, port, null);
	}
	catch (Exception e) {
	    e.printStackTrace (Log.getWriter());
	}
	Log.println ("ParseEngine: hostPortFile(): error: url=" + url);
	return null;
    }


    public static ArrayList<MyRecord> readRecords (String filePath)
    {
	try {
	    LineNumberReader lnr = new LineNumberReader (new FileReader(filePath));
	    
	    ArrayList<MyRecord> records = new ArrayList<>();
	    HashMap<String, String> currentMap = null;

	    String line = lnr.readLine ();
	    while (line != null) {
		line = line.trim ();

		// Comment or empty line.
		if ((line.length() == 0) || (line.startsWith("#"))) {
		    line = lnr.readLine ();
		    continue;
		}

		if (line.startsWith(".")) {
		    // New record:
		    if (currentMap != null) {
			records.add (new MyRecord(currentMap));
		    }
		    currentMap = new HashMap<>();
		    line = lnr.readLine ();
		    continue;
		}
		
		// Otherwise, it's data.
		NameValue nv = parseNameValue (line);
		if (nv == null) {
		    System.out.println ("ParseEngine FATAL ERROR: see log file");
		    Log.println ("ParseEngine: DB: parse error: line=" + line);
		    System.exit (0);
		}
		currentMap.put (nv.name, nv.value);
		line = lnr.readLine ();
	    }

	    // Add last table.
	    if (currentMap != null) {
		records.add (new MyRecord(currentMap));
	    }

	    // Write to log
	    Log.println ("ParseEngine: end of reading from " + filePath);
	    return records;
	}
	catch (Exception e) {
	    Log.println ("ParseEngine():readRecords: ERROR in loading DB: " + filePath);
	    e.printStackTrace (Log.getWriter());
	    return null;
	}
	
    }


    public static NameValue parseNameValue (String line)
    {
	line = line.trim ();
	// Find first colon, save name.
	int k = line.indexOf (':');
	if (k < 0) {
	    // Catastrophic error.
	    return null;
	}
	NameValue nv = new NameValue ();
	nv.name = line.substring(0,k).trim();
	nv.value = line.substring(k+1,line.length()).trim();
	return nv;
    }


    public static MyRecord parseRecord (String recordStr)
    {
	// A record is a collection of name-value pairs as in
	// [{name1:value1} {name2:value2} {name3:value3}]
	HashMap<String, String> map = new HashMap<>();
	int L = recordStr.indexOf ('{');
	int R = recordStr.indexOf ('}',L);
	while ( (L>0) && (R>0) ) {
	    // Extract. 
	    String nameValStr = recordStr.substring (L+1,R);
	    int k = nameValStr.indexOf (':');
	    String name = nameValStr.substring (0,k);
	    String val = nameValStr.substring (k+1,nameValStr.length());
	    map.put (name, val);
	    L = recordStr.indexOf ('{',R);
	    R = recordStr.indexOf ('}',L);
	}

	return new MyRecord (map);
    }


    public static boolean saveRecords (String filePath, ArrayList<MyRecord> records)
    {
	// The format to write is a comment line, followed by a single period
	// (to indicate the start of a record), followed by one name:value
	// pair per line.
	try {
	    PrintWriter pw = new PrintWriter (new FileWriter(filePath));
	    for (MyRecord r: records) {
		pw.println ("############################################");
		pw.println (".");
		for (String name: r.keySet()) {
		    String value = r.get (name);
		    pw.println (name + ": " + value);
		}
	    }
	    pw.flush(); pw.close();
	    return true;
	}
	catch (Exception e) {
	    Log.println ("ERROR in ParseEngine.saveRecords()");
	    e.printStackTrace (Log.getWriter());
	    return false;
	}
    }


    public static void main (String[] argv)
    {
	// Some test code.
	MyRecord r = ParseEngine.parseRecord ("[ {genre:Animation}  {movieID:8} ]");
	System.out.println (r);
	r = ParseEngine.parseRecord ("[ {actor:April Marie Thomas}  {movieID:1} ]");
	System.out.println (r);

	r = ParseEngine.parseRecord ("[ {revenue:2.787965087E9}  {director:James Cameron}  {popularity:150.437577}  {movieID:1}  {title:Avatar}  {budget:2.37E8} ]");
	System.out.println (r);
    }

}
