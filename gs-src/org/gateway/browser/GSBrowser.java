package org.gateway.browser;

// This imports enum's (text, inputfield etc)
import static org.gateway.browser.ElementType.*;

// Other imports.
import org.gateway.util.*;
import java.util.*;
import java.time.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*
 * GSBrowser is the browser GUI, implemented as a JFrame that contains
 * the top part (the URL field and go button), the File menu. It cannot
 * be understood without also looking at GSDoc.java which stores a
 * document after parsing. It is this parsed document that drives
 * the rendering of the document. There are four parts to understanding
 * the browser:
 *   1.  The rendering of the GSML page using Graphics
 *   2.  The actions taken when the user clicks or types
 *   3.  The fetching of files (local or remote) and posting of data
 *   4.  GUI construction.
 * We will present these in order.
 */


public class GSBrowser extends JPanel implements MouseListener, KeyListener {

    // MouseListener for mouse clicks, KeyListener for what the user types.

    //////////////////////////////////////////////////////////////////
    // Variables global to the class 

    // The GSML document and its elements:
    GSDoc doc = null;

    // The textfield on top for entering the URL
    JTextField urlField = new JTextField (30);

    // When the user clicks on a textfield, this is the one from which
    // to extract the "typed in" text.
    GSDocElement selectedInputField = null;

    // We need to maintain the scrollPane reference to re-size if the text is long.
    JScrollPane scrollPane;

    // Some references don't need a full URL, in which case they
    // are relative to the current host/port.
    String currentHost = null;   
    int currentPort = -1;


    public GSBrowser ()
    {
		this.addMouseListener (this);
		this.addKeyListener (this);
		build ();
		Log.println ("GSBrowser: initialized and build complete");
    }


    //////////////////////////////////////////////////////////////////
    // Part 1.  Rendering


    public void paintComponent (Graphics g)
    {
		super.paintComponent (g);

		// Default: white background.
		Dimension D = this.getSize ();
		int drawWidth = D.width;
		int drawHeight = D.height;
		g.setColor (Color.WHITE);
		g.fillRect (0,0, D.width, D.height);

		if (doc == null) {
			return;
		}

		// Otherwise, use the document's colors.
		g.setColor (doc.backgroundColor);
		g.fillRect (0,0, D.width, D.height);

		g.setColor (doc.foregroundColor);

		// Render the document now. We'll use currentY to track the
		// rendering along the y axis. This keeps increasing as we proceed,
		// depending on what's rendered.
		int currentY = 0;

		// The title tag.
		Font font = new Font ("Serif", Font.BOLD, doc.titleFontSize);
		g.setFont (font);
		FontMetrics fm = g.getFontMetrics();
		int h = fm.getHeight ();
		// h is how much we need to descend each time text is written.
		// This is half the job in layout: calculating where to draw next.
		currentY += h;
		g.drawString (doc.titleString, 10, currentY);

		// Now go serially through the document elements.

		for (GSDocElement element: doc.elements) {

			// Default for buttons etc
			font = new Font ("Serif", Font.PLAIN, doc.defaultFontSize);
			g.setFont (font);
			fm = g.getFontMetrics();
			h = fm.getHeight ();

			if (element.type == hline) {               // Horizontal line.
				g.drawLine (0,currentY+h, D.width,currentY+h);
				currentY += 2*h;
			}
			else if (element.type == inputfield) {     // A box for input.
				drawInputField (g, currentY+h, h, element);
				currentY += 4*h;
				g.setColor (doc.foregroundColor);
			}
			else if (element.type == button) {         // A box as button.
				currentY += h;
				drawButton (g, currentY, h, element);
				currentY += 2*h;
			}
			else if (element.type == image) {          // Get image, draw it.
				currentY += h;
				Image img = getImage (element.urlString);
				g.drawImage (img, 10, currentY, element.width, element.height, this);
				currentY += element.height;
			}
			else if (element.type == vspace) {
				currentY += element.height;
			}
			else if (element.type == text) {           // Text is complicated.
				currentY = drawText (g, currentY, element);
				if (currentY > D.height) {
					// If the text went past the bottom, make the scrollbar appear.
					this.setPreferredSize (new Dimension (D.width,currentY));
					scrollPane.revalidate();
				}
			}
			else {
				// Other elements like "<server>" have no rendering.
			}

		} // end-for-elements

    }


    void drawInputField (Graphics g, int currentY, int height, GSDocElement element)
    {
		if (element == selectedInputField) {  // If the user has selected it.
			g.setColor (Color.BLUE);
		}
		else {
			g.setColor (doc.foregroundColor);
		}
		g.drawRect (10, currentY, element.width, 2*height);
		g.drawString (element.inputText, 20, currentY+height);
		// MyRecord coordinates to activate textbox. The dimensions are
		// extracted when parsing occured in GSDOc.
		element.topLeftX = 10;  element.topLeftY = currentY;
		element.boxWidth = element.width;  element.boxHeight = 2*height;
    }

    void drawButton (Graphics g, int currentY, int height, GSDocElement element)
    {
		// A button is just a rectangle.
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth (element.name);
		g.drawString (element.name, 20, currentY+height);
		g.drawRect (10, currentY, w+20, 2*height);
		element.topLeftX = 10;  element.topLeftY = currentY;
		element.boxWidth = w+20;  element.boxHeight = 2*height;
    }

    int drawText (Graphics g, int currentY, GSDocElement element) 
    {
		// We get the dimension of the panel so that we know when
		// we've gone past (if the text is long). Then, we'll re-size the
		// scrollpane.
		Dimension D = this.getSize();

		// Set the font and get its height.
		Font textFont = new Font ("Serif", Font.PLAIN, element.fontSize);
		g.setFont (textFont);
		FontMetrics textfm = g.getFontMetrics();
		int fontHeight = textfm.getHeight ();

		currentY += fontHeight;
		int wordStart = 10;       // Inset from left edge.

		for (GSWord gword: element.words) {

			// See if we need to go to the next line.
			String spacing = " ";
			if (wordStart <= 10) {
				spacing = "";        // No spacing if word starts a new line.
			}
			String fullWord = spacing + gword.word;

			// If the word does not fit at the right, go to next line.
			int len = textfm.stringWidth (fullWord);
			if (wordStart+len > D.width) {
				wordStart = 10;
				currentY += fontHeight;
				spacing = "";
				fullWord = spacing + gword.word;
			}

			// Render the word.
			g.drawString (fullWord, wordStart, currentY);

			// Now draw link underline if needed
			if (gword.linkURL != null) {
				g.drawLine (wordStart, currentY+fontHeight/2, wordStart+len, currentY+fontHeight/2);
				// Store coordinates for possible click.
				gword.topLeftX = wordStart;
				gword.topLeftY = currentY-fontHeight;
				gword.boxWidth = len;
				gword.boxHeight = fontHeight;
			}
			wordStart += len;

		} //for-words

		currentY += fontHeight;
		return currentY;
    }



    //////////////////////////////////////////////////////////////////
    // Part 2.  Mouseclicks and typing


    public void mouseClicked (MouseEvent e)
    {
	selectedInputField = null;

	if (doc == null) return;

	// Find out where.

	for (GSDocElement element: doc.elements) {

	    // Three cases where we need to deal with a click: textbox, button, link.
	    if (element.type == inputfield) {
		boolean within = (element.topLeftX < e.getX()) && 
		    (element.topLeftX + element.boxWidth > e.getX()) && 
		    (element.topLeftY < e.getY()) && 
		    (element.topLeftY + element.boxHeight > e.getY());
		if (within) {
		    selectedInputField = element;
		    this.requestFocusInWindow();
		    Log.println ("Mouseclick in inputfield: element=" + element);
		    break;
		}
	    }
	    else if (element.type == button) {
		// Need to flash button.
		boolean within = (element.topLeftX < e.getX()) && 
		    (element.topLeftX + element.boxWidth > e.getX()) && 
		    (element.topLeftY < e.getY()) && 
		    (element.topLeftY + element.boxHeight > e.getY());
		if (within) {
		    Graphics g = this.getGraphics ();
		    g.setColor (Color.BLACK);
		    g.fillRect (element.topLeftX, element.topLeftY, element.boxWidth, element.boxHeight);  // Cover the button's rectangle.
		    try {
			Thread.sleep (400);   // Pause while dark.
		    }
		    catch (Exception ex) {
		    }
		    // Now post info to server.
		    postToServer (element.name);
		    Log.println ("Mouseclick: on button: element=" + element);
		    break;
		}
	    }
	    else if (element.type == text) {
		// See if any word got clicked, and then follow link.
		for (GSWord gword: element.words) {
		    boolean within = (gword.topLeftX < e.getX()) && 
			(gword.topLeftX + gword.boxWidth > e.getX()) && 
			(gword.topLeftY < e.getY()) && 
			(gword.topLeftY + gword.boxHeight > e.getY());
		    if (within) {
			Log.println ("Mouseclick: on word: element=" + element + " word=" + gword);
			jumpToLink (gword.linkURL);
			break;
		    }
		}
	    } // if-else

	} // end-outer-for: element: doc.elements
	this.repaint ();
    }
    
    // Remaining methods in MouseListener interface: empty
    public void mouseEntered (MouseEvent e){}
    public void mouseExited (MouseEvent e){}
    public void mousePressed (MouseEvent e){}
    public void mouseReleased (MouseEvent e){}

    public void keyTyped (KeyEvent e)
    {
	// When the user types a key on the keyboard, this method is called.
	// We need to accumulate the chars into a string to later send
	// to the server.
	if (selectedInputField != null) {
	    selectedInputField.inputText += e.getKeyChar();
	    this.repaint ();
	}
    }

    // Other methods in interface
    public void keyPressed (KeyEvent e){}
    public void keyReleased (KeyEvent e)
    {
	// A bug in mac-OSX. Keytyped() does not give you the keycode,
	// which we need for delete.
	if (selectedInputField != null) {
	    if ( (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) ||
		 (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) ) {
		if (selectedInputField.inputText.length() == 1) {
		    selectedInputField.inputText = "";
		}
		else if (selectedInputField.inputText.length() > 1) {
		    selectedInputField.inputText = selectedInputField.inputText.substring (0,selectedInputField.inputText.length()-2);
		}
	    }
	    this.repaint ();
	}
    }


    //////////////////////////////////////////////////////////////////
    // Part 3. Fetching files, posting data to the server.


    void goURL ()
    {
	// A click on the "Go" button brings us here. Extract the
	// string in the URL textfield and take action.
	jumpToLink (urlField.getText().trim());
    }

    void jumpToLink (String url)
    {
	String fullURL = url;

	if (url.startsWith("file://")) {
	    // It's a local file.
	    currentHost = null;
	    doc = readLocalFile (url.substring(7,url.length()));
	}
	else if (url.startsWith("gstp://")) {
	    // Extract server, and then file name. Then do a GGET.
	    ParseResult p = ParseEngine.extractHostPortFilename (url);
	    // Save host/port for other file references.
	    currentHost = p.host;
	    currentPort = p.port;
	    doc = getRemoteFile (p.host, p.port, p.filename);
	}
	else {
	    // It's the same server/port, so the URL is a file name.
	    if (currentHost == null) {  // local file
		doc = readLocalFile (url);
		fullURL = "file://" + url;
	    }
	    else {
		doc = getRemoteFile (currentHost, currentPort, url);
		fullURL = "gstp://" + currentHost + ":" + currentPort + "/" + url;
	    }
	}

	if (doc != null) {
	    doc.parse ();
	    urlField.setText (fullURL);
	    this.repaint ();
	}
    }

    static GSDoc readLocalFile (String filename)
    {
	try {
	    File f = new File (filename);
	    if (! f.exists()) {
		Log.println ("GSBrowser(): readLocalFile(): no such file=" + filename);
		return null;
	    }
	    LineNumberReader lnr = new LineNumberReader (new FileReader(f));
	    ArrayList<String> lines = new ArrayList<>();
	    String line = lnr.readLine ();
	    while (line != null) {
		lines.add (line);
		line = lnr.readLine ();
	    }
	    GSDoc doc = new GSDoc (lines);
	    Log.println ("GSBrowser():readFile(): successful read of location file=" + filename);
	    return doc;
	}
	catch (Exception e) {
	    System.out.println ("File not found: " + filename);
	    Log.println ("GSBrowser():readLocalFile(): exception:\n");
	    e.printStackTrace (Log.getWriter());
	    return null;
	}
    }


    static GSDoc getRemoteFile (String host, int port, String filename)
    {
	try {
	    // Connect to remote server and write the string "GGET ..." (with filename)
	    Socket soc = new Socket (host, port);    
	    PrintWriter pw = new PrintWriter (soc.getOutputStream());
	    pw.println ("GGET " + filename);
	    pw.flush ();

	    // Now read what the server sends over, line-by-line.
	    LineNumberReader lnr = new LineNumberReader (new InputStreamReader(soc.getInputStream()));
	    ArrayList<String> lines = new ArrayList<>();
	    String line = lnr.readLine ();
	    while (line != null) {
		lines.add (line);
		line = lnr.readLine ();
	    }

	    // GSDoc is set up to parse the tags from the list of lines.
	    GSDoc doc = new GSDoc (lines);
	    pw.close();
	    lnr.close();

	    Log.println ("GSBrowser():getRemoteFile(): successful retrieval of remote file from host=" + host + " port=" + port + " filename=" + filename);
	    return doc;
	}
	catch (Exception e) {
	    Log.println ("GSBrowser():getRemoteFile(): exception\n");
	    e.printStackTrace (Log.getWriter());
	    System.out.println ("Possible: Server connection failed? See log.");
	    return null;
	}
    }


    Image getImage (String urlString)
    {
	if (urlString.startsWith("gstp://")) {
	    ParseResult p = ParseEngine.extractHostPortFilename (urlString);
	    Log.println ("GSBrowser.getImage(): RemoteImage full url=" + urlString);
	    return getRemoteImage (p.host, p.port, p.filename);
	}
	else {
	    // It's either local or remote.
	    if (currentHost == null) {  // local
		Log.println ("GSBrowser.getImage(): Local image: file=" + urlString);
		return Toolkit.getDefaultToolkit().getImage(urlString);
	    }
	    else {
		Log.println ("GSBrowser.getImage(): RemoteImage: file=" + urlString);
		return getRemoteImage (currentHost, currentPort, urlString);
	    }
	}
    }

    static Image getRemoteImage (String host, int port, String filename)
    {
	try {
	    // Open a socket connection and write "GIMAGE ..." (image file)
	    Socket soc = new Socket (host, port);
	    PrintWriter pw = new PrintWriter (soc.getOutputStream());
	    pw.println ("GIMAGE " + filename);
	    pw.flush ();

	    // Get the bytes and put those into a local temp file.
	    String filepath = "gsmlpages" + File.separator + "temp" + filename;
	    FileOutputStream fis = new FileOutputStream (filepath);
	    InputStream inStream = soc.getInputStream ();
	    int inValue = inStream.read ();
	    while (inValue >= 0) {
		byte b = (byte) inValue;
		fis.write (b);
		inValue = inStream.read ();
	    }
	    fis.flush (); 
	    fis.close ();
	    pw.close ();

	    Log.println ("GSBrowser.getImage: remote: image written to local file: " + filepath);
	    // Now it's a temp file in the gsmlpages dir. Java Swing's
	    // Toolkit handles retrieval from a local file.
	    return Toolkit.getDefaultToolkit().getImage(filepath);
	}
	catch (Exception e) {
	    Log.println ("GSBrowser():getImage(): exception\n");
	    e.printStackTrace(Log.getWriter());
	    System.out.println ("Possible: Server connection failed? See log.");
	    return null;
	}
    }

    void postToServer (String buttonName)
    {
	try {
	    if (doc.serverURL == null) {
		return;   // Nothing to be done.
	    }

	    // Host and port
	    ParseResult p = ParseEngine.extractHostPort (doc.serverURL);
	    // Identify all that needs to be sent.
	    ArrayList<String> lines = new ArrayList<>();
	    lines.add ("app=" + doc.serverApp);
	    lines.add ("button=" + buttonName);         // Only one active button.
	    for (GSDocElement element: doc.elements) {
		if (element.type == inputfield) {
		    lines.add (element.name + "=" + element.inputText);
		}
	    }

	    // Now write to server, writing out the name-value pairs line by line.
	    System.out.println ("GPOST: to host=" + p.host + " port=" + p.port);
	    Socket soc = new Socket (p.host, p.port);
	    PrintWriter pw = new PrintWriter (soc.getOutputStream());
	    pw.println ("GPOST " + lines.size());
	    for (String line: lines) {
		System.out.println (" >> " + line);
		pw.println (line);
	    }
	    pw.flush (); 

	    // Receive GSML page from server.
	    LineNumberReader lnr = new LineNumberReader (new InputStreamReader(soc.getInputStream()));
	    lines = new ArrayList<>();
	    String line = lnr.readLine ();
	    while (line != null) {
		lines.add (line);
		line = lnr.readLine ();
	    }
	    pw.close ();
	    lnr.close();

	    // If parse() throws an exception, we'll keep the original doc.
	    GSDoc tempDoc = new GSDoc (lines);
	    tempDoc.parse ();
	    doc = tempDoc;
	    this.repaint ();

	    Log.println ("GSBrowser():postToServer(): successful retrieval of remote App-generated gsml from host=" + p.host + " port=" + p.port + " doc:\n" + doc);
	}
	catch (Exception e) {
	    Log.println ("GSBrowser():postToServer(): could not post");
	    e.printStackTrace (Log.getWriter());
	}
    }


    //////////////////////////////////////////////////////////////////
    // Part 4.  GUI construction and main()


    void build ()
    {
	// Frame stuff:
	JFrame f = new JFrame ();
	f.setTitle ("GS Browser");
	f.setSize (700, 600);
	f.setResizable (true);

	// The reason we have a separate panel is so that the whole
	// thing can be put into a scrollpane.
	JPanel mainPanel = new JPanel ();
	mainPanel.setLayout (new BorderLayout());
	mainPanel.add (makeTop(), BorderLayout.NORTH);   // URL field, "go"
	mainPanel.add (this, BorderLayout.CENTER);
	scrollPane = new JScrollPane(mainPanel);
	f.getContentPane().add (scrollPane, BorderLayout.CENTER);

	// The File menu.
	JMenuBar jmb = new JMenuBar ();
	JMenu menu = new JMenu ("File");
	jmb.add (menu);
	JMenuItem backItem = new JMenuItem ("Back");
	//backItem.addActionListener (a -> back());
	menu.add (backItem);
	JMenuItem quitItem = new JMenuItem ("Quit");
	quitItem.addActionListener (a -> System.exit(0));
	menu.add (quitItem);
	f.setJMenuBar (jmb);

	// Done.
	f.setVisible (true);
    }

    JPanel makeTop ()
    {
	JPanel panel = new JPanel ();
	panel.add (new JLabel("URL: "));
	panel.add (urlField);
	panel.add (new JLabel("    "));
	JButton goB = new JButton ("Go");
	goB.addActionListener (a -> goURL());
	panel.add (goB);
	return panel;
    }


    public static void main (String[] argv)
    {
	File logFile = new File ("logs" + File.separator + "browser.log");
	Log.setLogFile (logFile);
	new GSBrowser ();
    }

}


