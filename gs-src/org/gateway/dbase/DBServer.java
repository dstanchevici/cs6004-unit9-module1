
package org.gateway.dbase;

import org.gateway.dbase.*;
import org.gateway.util.*;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 * This class is similar to GSServer in some ways. It starts off
 * by listening to a port. When a connection comes in, a thread
 * is fired off (with an instance of DBServer) to deal with it.
 * Whoever talks to the server talks in the language of GSQL.
 * Thus, it is DBServer's job to parse GSQL, figure out what
 * what the result needs to be, and to send that to the client.
 * There are two parts to understanding this:
 *    1.  The part that initializes by reading in the databases.
 *    2.  Handling GSQL queries.
 */

public class DBServer implements Runnable {

    ///////////////////////////////////////////////////////////////
    // Part 1.  Initialization and listening for a connection.

    // Each database has a name, and an associated object.
    static HashMap<String, DB> nameToDB = new HashMap<>();

    // The port number we're listening on (read in from the props file).
    static int portNum;

    // To help in debugging.
    static int IDCount = 1;


    public static void main (String[] argv)
    {
		// Set up the log file (for debugging).
		File logFile = new File ("logs" + File.separator + "db.log");
		Log.setLogFile (logFile);
		Log.println ("Starting DBServer");

		// Read in the databases from /databases. We know the names
		// from the props file.
		loadDatabases ();

		try {
			ServerSocket srv = new ServerSocket (portNum);
			while (true) {
				System.out.println ("DBServer: waiting for a connection on port " + portNum);
				// Wait for a connection.
				Socket soc = srv.accept ();

				// Now create an instance of the class that can deal with it.
				new Thread ( new DBServer(soc) ).start();
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
			e.printStackTrace (Log.getWriter());
			System.out.println ("Fatal error in DB server: main()");
		}
    }


    static void loadDatabases ()
    {
		try {
			// The file /properties/db.props has what's needed.
			FileInputStream propsFile = new FileInputStream ("properties" + File.separator + "db.props");
			Properties props = new Properties ();
			props.load (propsFile);
			portNum = Integer.parseInt(props.getProperty("portNum"));
			int numDatabases = Integer.parseInt(props.getProperty("numDatabases"));
			System.out.println ("DBServer: read properties: port=" + portNum + " numDatabases=" + numDatabases);

			// Now read each dbase one by one.
			for (int i=0; i<numDatabases; i++) {
				String databaseName = props.getProperty("database" + i);
				DB db = new DB (databaseName);
				nameToDB.put (databaseName, db);
				System.out.println (" >> db=" + databaseName + " loaded");
			}
			Log.println ("DBServer:loadDatabases: complete");

		}
		catch (Exception e) {
			e.printStackTrace();
			Log.println ("DBServer:loadDatabases() exception:");
			e.printStackTrace (Log.getWriter());
		}
    }



    ///////////////////////////////////////////////////////////////
    // Part 2. Handling each connection.

    // Instance variables (one per thread):
    int ID;
    Socket soc;
    PrintWriter pw;       // To write on the socket.


    public DBServer (Socket soc)
    {
		this.ID = IDCount ++;
		this.soc = soc;
    }

    public void run ()
    {
		// This method implements the Runnable interface and is the
		// starting point for thread execution.
		try {
			pw = new PrintWriter (soc.getOutputStream());
			LineNumberReader lnr = new LineNumberReader (new InputStreamReader(soc.getInputStream()));
			String line = lnr.readLine ();

			System.out.println ("DBServer.run(): received line=" + line);

			// Now handle each type of query accordingly, starting with
			// the removal of the query word so that the rest of the string
			// gives us useful info.

			int k = 0;
			if (line.startsWith("FETCH")) {
				k = 6;
			}
			else if (line.startsWith("SELECT")) {
				k = 7;
			}
			else if (line.startsWith("PROJECT")) {
				k = 8;
			}
			else if (line.startsWith("JOIN")) {
				k = 5;
			}
			else if (line.startsWith("INSERT")) {
				k = 7;
			}
			else if (line.startsWith("SAVE")) {
				k = 5;
			}

			// FETCH, SAVE and JOIN are slightly different from the rest.

			if (line.startsWith("FETCH")) {
				String dbTableStr = line.substring (k);
				NameValue nv = ParseEngine.parseNameValue (dbTableStr);
				doSelect (nv.name, nv.value, null);
				return;
			}
			else if (line.startsWith("SAVE")) {
				String dbTableStr = line.substring (k);
				NameValue nv = ParseEngine.parseNameValue (dbTableStr);
				doSave (nv.name, nv.value);
				return;
			}
			else if (line.startsWith("JOIN")) {
				String dbTablesStr = line.substring (k);
				doJoin (dbTablesStr);
				return;
			}

			// SELECT, PROJECT, INSERT all share similar query structure.

			String params = line.substring (k);
			int m = params.indexOf ('[');
			String dbTableStr = params.substring(0,m).trim();
			NameValue nv = ParseEngine.parseNameValue (dbTableStr);
			MyRecord r = ParseEngine.parseRecord (line.substring(m));

			if (line.startsWith("SELECT")) {
				// This will be something like SELECT db:table [{col1:val1} {col2:val2}]
				doSelect (nv.name, nv.value, r);
			}
			else if (line.startsWith("PROJECT")) {
				// This will be something like PROJECT db:table [{col1:_} {col2:_}]
				doProject (nv.name, nv.value, r);
			}
			else if (line.startsWith("INSERT")) {
				// e.g., INSERT db:table [{name1:val1} {name2:val2} ...]
				doInsert (nv.name, nv.value, r);
			}
			else {
				Log.println ("Unknown Command: line=" + line);
				writeError ();
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
			System.out.println ("Fatal error in run(): DBServer thread: ID=" + ID);
			writeError ();
		}
	
    }

    void writeError ()
    {
		pw.println ("ERROR");
		pw.flush ();
		pw.close ();
    }

    void doSelect (String dbName, String tableName, MyRecord conditions)
    {
		try {
			Log.println ("DBServer.doSelect(): db=" + dbName + " table=" + tableName + " conditions=" + conditions);

			// Pull up the db and table.
			DB db = nameToDB.get (dbName);
			Table table = db.getTable (tableName);

			for (MyRecord row: table.getRows()) {
			if (conditions == null) {
				// This is FETCH, so all rows are returned.
				pw.println (row);
			}
			else if ( row.matches(conditions) )  {
				// Otherwise, only the rows that match conditions are returned.
				pw.println (row);
			}
			// Note: the MyRecord class has toString() implemented,
			// which writes out a String version of the record.
			}
			pw.flush(); pw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			writeError ();
		}
    }


    void doProject (String dbName, String tableName, MyRecord columns)
    {
		try {
			Log.println ("DBServer.doProject(): db=" + dbName + " table=" + tableName + " columns=" + columns);

			// INSERT YOUR CODE HERE. Make sure that you track
			// duplicates so that the end result has no duplicates.
			// First implement the project() method in MyRecord and
			// then use that here.


			DB db = nameToDB.get (dbName);
			Table table = db.getTable (tableName);

			Set<String> projSet = new HashSet<String>(); // to ensure that there are no duplications
			Set<String> colNames = columns.keySet(); // to send to project() in MyRecord
			ArrayList<MyRecord> records = table.getRows();
			// Send each row to project() in MyRecord and receie a formatted string
			for (MyRecord r: records) {
				projSet.add( r.project(colNames) );
			}

			// B/c the String projections are in a set,
			// there should be no duplicates.
			// Now send back to client.
			for (String projection: projSet) {
				pw.println(projection);
			}
			pw.flush(); pw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			writeError ();
		}
    }


    void doJoin (String dbTableInfo)
    {
		try {
			// First split by whitespace, to obtain db1:table1 and db2:table2
			// Note: we enforce db1==db2
			String[] parts = dbTableInfo.trim().split("\\s");
			if ( (parts.length!=2) || (parts[0].indexOf(':')<0) || (parts[0].indexOf(':')<0) ) {
				writeError ();
				return;
			}
			NameValue nv1 = ParseEngine.parseNameValue (parts[0]);
			NameValue nv2 = ParseEngine.parseNameValue (parts[1]);

			Log.println ("DBServer.doJoin(): db1=" + nv1.name + " table1=" + nv1.value + " db2=" + nv2.name + " table2=" + nv2.value);

			if (! nv1.name.equals(nv2.name) ) {
				Log.println ("ERROR: DBServer.doJoin(): tables in different databases");
				writeError ();
				return;
			}

			DB db = nameToDB.get (nv1.name);
			Table t1 = db.getTable (nv1.value);
			Table t2 = db.getTable (nv2.value);
			Table result = t1.join (t2);
			if (result == null) {
				Log.println ("ERROR: DBServer.doJoin(): null join");
				writeError ();
				return;
			}

			// It worked. Write the results out.
			for (MyRecord row: result.getRows()) {
				pw.println (row);
			}
			pw.flush(); pw.close();
		}
		catch (Exception e) {
			Log.println ("ERROR in DBServer(): processJoin");
			e.printStackTrace (Log.getWriter());
			writeError ();
		}
    }


    void doInsert (String dbName, String tableName, MyRecord r)
    {
		try {
			Log.println ("DBServer.doInsert(): db=" + dbName + " table=" + tableName + " record=" + r);

			DB db = nameToDB.get (dbName);
			Table table = db.getTable (tableName);

			if (table.insert(r)) {
				pw.println ("SUCCESS");
				pw.flush(); pw.close();
			}
			else {
					writeError ();
			}
		}
		catch (Exception e) {
			Log.println ("ERROR in DBServer(): processInsert");
			e.printStackTrace (Log.getWriter());
			writeError ();
		}

    }


    void doSave (String dbName, String tableName)
    {
		Log.println ("DBServer.doSave(): db=" + dbName + " table=" + tableName);

		DB db = nameToDB.get (dbName);
		Table table = db.getTable (tableName);

		if (table.saveTable()) {
			pw.println ("SUCCESS");
			pw.flush(); pw.close();
		}
		else {
			writeError ();
		}
    }


}
