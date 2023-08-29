package org.gateway.browser;

import org.gateway.util.*;
import java.util.*;
import java.awt.*;

import static org.gateway.browser.ElementType.*;

/**
 * GSDoc stores the elements of a GSML document, and parses them.
 * The extracted info is kept here so that the browser can call
 * on this as needed for rendering.
 */

public class GSDoc {

    // A GSDoc is just a collection of GSDocElements.
    ArrayList<GSDocElement> elements = new ArrayList<>();

    // The original document as plain text lines.
    ArrayList<String> lines;

    // What was extracted:
    Color backgroundColor=Color.WHITE, foregroundColor=Color.BLACK;
    int titleFontSize = 20;
    int defaultFontSize = 12;    // For use with buttons, textfields
    String titleString = "";
    String serverURL = null;
    String serverApp = null;
    int currentTextSize = 15;    // Default text size.

    public GSDoc (ArrayList<String> lines) 
    {
	this.lines = lines;
    }

    public boolean parse () 
    {
		if (lines == null) return false;

		// First line must be: <doc background=x foreground=y>
		processDocTag ( lines.get(0) );

		// Second line must be: <title size=x> title-text
		processTitleTag (lines.get(1) );


		// The parsing algorithm is rudimentary: process a line
		// at a time. If we see a tag, extract its parameters.
		// If the line is plain text, accumulate the lines of text.

		boolean inText = false;
		int i = 2;
		ArrayList<String> textLines = new ArrayList<>();

		while (i < lines.size()) {

			String line = lines.get(i).trim();

			if (line.length() == 0) {  // Skip blank lines.
				i++;
				continue;
			}

			if (inText) {
				// We're inside text now.
				if (line.startsWith("</text>")) { // End the text blob.
					if (textLines.size() > 0) {   // And store it.
						GSDocElement element = new GSDocElement ();
						element.type = text;
						element.fontSize = currentTextSize;
						element.textLines = textLines;
						processText (element);
						elements.add (element);
					}
					inText = false;
				}
				else if (line.startsWith("<")) {
					// Illegal start of tag: can't have a tag inside text.
					fatalError ("Illegal start of tag: line=" + line);
				}
				else {
					// Continue adding lines.
					textLines.add (line);
				}
			}
			else {  // not inText
				if (line.startsWith("<text")) {
					inText = true;
					textLines = new ArrayList<>();  // New blob.
					extractTextSize ( line.substring(6, line.length()-1) ) ;
				}
				else if (line.startsWith("<")) {
					processTag (line);            // Some other tag.
				}
				else {
					// Ignore.
				}
	    	}

	    	i++;
		}

		// At end, process all text elements so that a text-element
		// will be a collection of GSWord's. If we encountered links,
		// we'll need to associate those with words (in case the rendered
		// word is clicked.

		for (GSDocElement e: elements) {
			if (e.type == text) {
				processText (e);
			}
		}

		Log.print ("GSDoc.parse(): Scan complete: elements:\n");
		for (GSDocElement e: elements) {
			Log.print (e + "\n");
		}

		return true;
    }

    void fatalError (String msg)
    {
		// We're dealing rather badly with parse errors.
		System.out.println ("ERROR: " + msg);
		System.exit (0);
    }

    void processDocTag (String line)
    {
		// Remove "<doc" and ">"
		line = line.trim();
		String docAttributes = line.substring (5,line.length()-1);
		HashMap<String,String> attributes = ParseEngine.extractAttributes (docAttributes, 2);
		// extractAttributes() pulls out the name=value pairs found.

		if (attributes == null) fatalError ("<doc> tag: line=" + line);

		Log.println ("GSDoc.processDocTag(): tag attributes=" + attributes);

		// Hide most of the ugly parsing in the ParseEngine class.
		Color backColor = ParseEngine.extractColor (attributes.get("background"));
		Color foreColor = ParseEngine.extractColor (attributes.get("foreground"));

		if ( (backColor == null) || (foreColor == null) ) {
			fatalError ("<doc> tag: line=" + line);
		}
		else {
			backgroundColor = backColor;
			foregroundColor = foreColor;
		}
    }


    void processTitleTag (String line)
    {
		// Need to split to extract the title tag, and the title text.
		int k = line.indexOf ('>');
		// Everything after is the actual title
		titleString = line.substring (k+1,line.length());
		// Now for attributes part:
		String titleAttributes = line.substring (7,k);
		HashMap<String,String> attributes = ParseEngine.extractAttributes (titleAttributes, 1);
		Log.println ("GSDoc.processTitleTag(): tag attributes=" + attributes);
		if (attributes == null) fatalError ("<title> tag: line=" + line);
		titleFontSize = ParseEngine.stringToInt (attributes.get("size"),20);
    }


    void processTag (String line)
    {
		GSDocElement element = new GSDocElement ();

		// Deal with each tag separately.

		if (line.startsWith("<hline>")) {
			element.type = hline;
			Log.println ("GSDoc:processTag(): hline detected: line=" + line);
		}
		else if (line.startsWith("<vspace")) {
			// INSERT YOUR CODE HERE
			element.type = vspace;
			String vspaceAttributes = line.substring (8, line.length()-1);
			HashMap<String,String> attributes = ParseEngine.extractAttributes (vspaceAttributes, 1);
			if (attributes == null) fatalError ("<vspace> tag: line=" + line);
			element.height = ParseEngine.stringToInt (attributes.get("height"), 10); // 10 is the default value
			Log.println ("GSDoc:processTag(): vspace detected: height=" + element.height);
		}
		else if (line.startsWith("<inputfield")) {
			element.type = inputfield;
			String inputAttributes = line.substring (12,line.length()-1);
			HashMap<String,String> attributes = ParseEngine.extractAttributes (inputAttributes, 2);
			if (attributes == null) fatalError ("<inputfield> tag: line=" + line);
			element.width = ParseEngine.stringToInt (attributes.get("width"),100);
			element.name = attributes.get("name");
			Log.println ("GSDoc:processTag(): inputfield detected: width=" + element.width + " name=" + element.name);
		}
		else if (line.startsWith("<button")) {
			element.type = button;
			String buttonAttributes = line.substring (8,line.length()-1);
			HashMap<String,String> attributes = ParseEngine.extractAttributes (buttonAttributes, 1);
			if (attributes == null) fatalError ("<button> tag: line=" + line);
			element.name = attributes.get("name");
			Log.println ("GSDoc:processTag(): button detected: name=" + element.name);
		}
		else if (line.startsWith("<image")) {
			element.type = image;
			String imageAttributes = line.substring (7,line.length()-1);
			HashMap<String,String> attributes = ParseEngine.extractAttributes (imageAttributes, 3);
			if (attributes == null) fatalError ("<image> tag: line=" + line);

			element.width = ParseEngine.stringToInt (attributes.get("width"),100);
			element.height = ParseEngine.stringToInt (attributes.get("height"),100);
			element.urlString = attributes.get("url");

			Log.println ("GSDoc:processTag(): image tag: width=" + element.width + " height=" + element.height + " url=" + element.urlString);
		}
		else if (line.startsWith("<server")) {
			element.type = server;
			String serverAttributes = line.substring (8,line.length()-1);
			HashMap<String,String> attributes = ParseEngine.extractAttributes (serverAttributes, 2);
			if (attributes == null) fatalError ("<image> tag: line=" + line);
			serverURL = attributes.get("url");
			serverApp = attributes.get("app");
			Log.println ("GSDoc:processTag(): server tag: url=" + serverURL + " app=" + serverApp);
		}
		else {
			element.type = none;
		}
		elements.add (element);
    }


    void extractTextSize (String line)
    {
		HashMap<String,String> attributes = ParseEngine.extractAttributes (line, 1);
		currentTextSize = ParseEngine.stringToInt (attributes.get("size"),10);
    }


    void processText (GSDocElement element)
    {
		// element.textLines has the text. What we need:
		// a list of words (arraylist of strings), and if
		// links exist, the start and end (which word starts, which ends),
		// for each such link.

		// First, build the whole string because this is what we'll
		// use in rendering. The actual original source formatting is ignored.
		String fullText = "";
		for (String s: element.textLines) {
			fullText += " " + s;
		}

		// Then, segment into parts that are linked, and non-linked.
		element.words = extractWords (fullText);
    }


    ArrayList<GSWord> extractWords (String line)
    {
	ArrayList<GSWord> words = new ArrayList<>();
	int k = line.indexOf ("<link");
	while (k >= 0) {
	    String before = line.substring (0,k);
	    int m = line.indexOf (">", k);
	    String linkTagStr = line.substring (k+1,m);
	    int n = line.indexOf ("</link>",m);
	    String linkTextStr = line.substring (m+1,n);
	    String after = line.substring (n+7,line.length());
	    // Extract the url.
	    int e = linkTagStr.indexOf("=");
	    String urlStr = linkTagStr.substring (e+1,linkTagStr.length());
	    Log.println ("GSDoc:extractWords(): beforetag=" + before + " aftertag=" + after + " linkTag=" + linkTagStr + " linkText=" + linkTextStr + " url=" + urlStr);
	    ArrayList<String> wordStrings = ParseEngine.extractWordsFromString (before);
	    for (String s: wordStrings) {
			words.add (new GSWord(s,null));    // Un-linked words
	    }
	    wordStrings = ParseEngine.extractWordsFromString (linkTextStr);
	    for (String s: wordStrings) {
			words.add (new GSWord(s,urlStr));  // Linked words
	    }
	    line = after;
	    k = line.indexOf ("<link");
	}

	// At this point, line has no links.
	ArrayList<String> wordStrings = ParseEngine.extractWordsFromString (line);
	for (String s: wordStrings) {
	    words.add (new GSWord(s,null));       // Un-linked words
	}
	
	return words;
    }


    public String toString () 
    {
	String s = "GSDoc: background=" + backgroundColor + "  foreground=" + foregroundColor + "  titlesize=" + titleFontSize + "  title=" + titleString + " serverURL=" + serverURL + " serverApp=" + serverApp + " textsize=" + currentTextSize;
	if (elements == null) {
	    s += "\nEmpty document: elements=null";
	    return s;
	}
	s += "  #elements=" + elements.size() + "\nElements:";
	for (GSDocElement element: elements) {
	    s += element + "\n";
	}
	return s;
    }

}
