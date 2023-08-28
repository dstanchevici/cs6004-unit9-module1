
import org.gateway.server.*;
import org.gateway.util.*;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 * This App is intended to show how an app connects to a database,
 * via yet another network connection (so that the dbase can actually
 * run on its own machine, if need be). For demo purposes, we'll run
 * the dbase on the same machine (localhost) but at a different port number
 * so that we don't clash with the webserver. The app is best understood
 * in two parts:
 *    1.  The part that receives the parameters and constructs the response.
 *    2.  Fetching the data from the dbase using GSQL commands
 */

public class MovieFindApp implements ServerApp {

    /////////////////////////////////////////////////////////////////
    // Part 1. Receiving parameters, constructing the response

    String dbaseServerURL = "localhost";
    int dbaseServerPort = 40014;
    String dbase = "movies";

    public void handlePost (HashMap<String,String> pageParameters, PrintWriter outputToBrowser)
    {
	outputToBrowser.println ("<doc background=DDDDFF foreground=000000>");
	outputToBrowser.println ("<title size=20> Movie-Finder Results");
	
	// Find out which button was clicked. We're writing one set
	// of code for both pages moviefinder.gsml and moviefinder2.gsml.
	// The first two are for moviefinder.gsml and demonstrate simple
	// SELECT's without a join.
	String whichButton = pageParameters.get("button");

	if (whichButton.equalsIgnoreCase("FindMovieID")) {

	    String actorName = pageParameters.get("actorname");
	    outputToBrowser.println ("<text size=12>");
	    outputToBrowser.println ("MovieIDs for actor: " + actorName + ": ");
	    // getMovieIDs() is where the real dbase work is done.
	    ArrayList<String> movieIDs = getMovieIDs (actorName);
	    for (String mID: movieIDs) {
		outputToBrowser.println ("  " + mID);
	    }
	    outputToBrowser.println ("</text>");

	}
	else if (whichButton.equalsIgnoreCase("FindMovie")) {

	    outputToBrowser.println ("<text size=12>");
	    String movieID = pageParameters.get("movieid");
	    String movieStr = getMovie (movieID);
	    outputToBrowser.println ("Movie with movieID=" + movieID + ": " + movieStr);
	    outputToBrowser.println ("</text>");
	}
	else if (whichButton.equalsIgnoreCase("FindMovies")) {

	    String actorName = pageParameters.get("actorname");
	    outputToBrowser.println ("MovieIDs for actor: " + actorName + ": ");
	    ArrayList<String> movieTitles = getMovieTitles (actorName);
	    if ((movieTitles == null) || (movieTitles.size() == 0)) {
		outputToBrowser.println ("<text size=12>");
		outputToBrowser.println ("None found");
		outputToBrowser.println ("</text>");
	    }
	    else {
		int n = 1;
		for (String title: movieTitles) {
		    outputToBrowser.println ("<text size=12>");
		    outputToBrowser.println (n + ". " + title);
		    outputToBrowser.println ("</text>");
		    n++;
		}
	    }

	}
	else {
	    Log.println ("ERROR unknown button: " + whichButton);
	}
	outputToBrowser.flush ();
    }


    /////////////////////////////////////////////////////////////////
    // Part 2. Send queries to the dbase (in GSQL), receive data back.

    ArrayList<String> getMovieIDs (String actorname)
    {
	try {
	    // Open a socket to the dbase server.
	    Socket soc = new Socket (dbaseServerURL, dbaseServerPort);
	    LineNumberReader lnr = new LineNumberReader (new InputStreamReader(soc.getInputStream()));
	    PrintWriter pw = new PrintWriter (soc.getOutputStream());

	    // Compose the string that is the GSQL query, and send it.
	    String query = "SELECT " + dbase + ":actors [{actor:" + actorname + "}]";
	    System.out.println ("MovieFindApp: connected to dbase server: sending query=" + query);
	    pw.println (query);
	    pw.flush ();

	    // Now receive what the dbase sends back, and parse it.
	    ArrayList<String> movieIDs = new ArrayList<>();
	    String line = lnr.readLine ();
	    while (line != null) {
		MyRecord r = ParseEngine.parseRecord (line);
		String mID = r.get ("movieID");
		movieIDs.add (mID);
		System.out.println (">> movieID=" + mID + " from line=" + line);
		line = lnr.readLine ();
	    }
	    pw.close(); lnr.close();
	    return movieIDs;
	}
	catch (Exception e) {
	    Log.println ("ERROR in MovieFindApp");
	    e.printStackTrace (Log.getWriter());
	    return null;
	}
    }


    String getMovie (String movieID)
    {
	try {
	    // Open a connection to the dbase server.
	    Socket soc = new Socket (dbaseServerURL, dbaseServerPort);
	    LineNumberReader lnr = new LineNumberReader (new InputStreamReader(soc.getInputStream()));
	    PrintWriter pw = new PrintWriter (soc.getOutputStream());

	    // Make the query and send it.
	    String query = "SELECT " + dbase + ":movies [{movieID:" + movieID + "}]";
	    System.out.println ("MovieFindApp: connected to dbase server: sending query=" + query);
	    pw.println (query);
	    pw.flush ();

	    // Retrieve data back and parse it.
	    ArrayList<String> movieIDs = new ArrayList<>();
	    String line = lnr.readLine ();
	    MyRecord r = ParseEngine.parseRecord (line);
	    String title = r.get ("title");
	    System.out.println (">> movie title=" + title + " from line=" + line);
	    pw.close(); lnr.close();
	    return title;
	}
	catch (Exception e) {
	    Log.println ("ERROR in MovieFindApp");
	    e.printStackTrace (Log.getWriter());
	    return null;
	}
    }


    ArrayList<String> getMovieTitles (String actorname)
    {
	try {
	    // Open the connection to the dbase server.
	    Socket soc = new Socket (dbaseServerURL, dbaseServerPort);
	    LineNumberReader lnr = new LineNumberReader (new InputStreamReader(soc.getInputStream()));
	    PrintWriter pw = new PrintWriter (soc.getOutputStream());

	    // Put the GSQL query together.
	    String query = "JOIN " + dbase + ":actors " + dbase + ":movies";
	    System.out.println ("MovieFindApp: connected to dbase server: sending query=" + query);
	    pw.println (query);
	    pw.flush ();

	    // Retrieve.
	    ArrayList<String> movieTitles = new ArrayList<>();
	    String line = lnr.readLine ();
	    while (line != null) {
		MyRecord r = ParseEngine.parseRecord (line);
		String actor = r.get ("actor");
		String title = r.get ("title");
		if (actor.equalsIgnoreCase(actorname)) {
		    movieTitles.add (title);
		    System.out.println (">> for actor=" + actor + " movie title=" + title + " record=" + r);
		}
		line = lnr.readLine ();
	    }
	    pw.close(); lnr.close();
	    return movieTitles;
	}
	catch (Exception e) {
	    Log.println ("ERROR in MovieFindApp");
	    e.printStackTrace (Log.getWriter());
	    return null;
	}
    }


}
