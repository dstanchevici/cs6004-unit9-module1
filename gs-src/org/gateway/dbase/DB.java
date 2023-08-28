package org.gateway.dbase;

import org.gateway.dbase.*;
import org.gateway.util.*;
import java.util.*;
import java.io.*;

/**
 * Class DB is used to hold all the objects (tables, really) for a single 
 * database. The dbase server is set up to handle multiple databases,
 * each of which can have tables.
 */


public class DB {

    // The mapping from table name to the corresponding Table object 
    HashMap<String, Table> tables = new HashMap<>();

    String dbName;           // Name of this db
    String pathToTables;     // Where to find it.

    public DB (String dbName)
    {
	pathToTables = "databases" + File.separator + dbName;
	this.dbName = dbName;
	loadDB ();
    }

    void loadDB ()
    {
	System.out.println ("DB.loadDB(): path=" + pathToTables);

	// The .db file contains the structure of each table.
	ArrayList<MyRecord> records = ParseEngine.readRecords (pathToTables+".db");
	// records.get(i) has info about the i-th table.
	for (MyRecord r: records) {
	    // Each record in the .db file describes a table.
	    Table t = new Table (pathToTables, r);
	    tables.put (t.getName(), t);
	}

	// Write to log
	Log.println ("DB: end of loading tables");
	for (String t: tables.keySet()) {
	    Table table = tables.get (t);
	    Log.println (""+table);
	}

    }

    public Table getTable (String name)
    {
	return tables.get(name);
    }

    public String toString ()
    {
	String s = "DB=" + dbName + "\n";
	for (String tableName: tables.keySet()) {
	    s += "table: " + tableName + "\n";
	}
	return s;
    }

}
