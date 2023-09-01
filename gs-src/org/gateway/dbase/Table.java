package org.gateway.dbase;

import org.gateway.dbase.*;
import org.gateway.util.*;
import java.util.*;
import java.io.*;

public class Table {

    String name;           // Its name.
    String pathToTables;   // Where its loaded from.

    // For each field in the table, we store its name and type (string, integer, double)
    HashMap<String, String> fieldInfo = new HashMap<>();

    // This is the actual data: a list of records. MyRecord is in org.gateway.util
    ArrayList<MyRecord> rows = new ArrayList<>();


    public Table (String pathToTables, MyRecord tableInfo)
    {
		// We get as input: the path to the actual table, and the
		// structure of this table (as a MyRecord instance).
		// A MyRecord is really just a hashmap.
		this.pathToTables = pathToTables;
		name = tableInfo.get ("table");
		for (String s: tableInfo.keySet()) {
			if (!s.equalsIgnoreCase("table")) {
				fieldInfo.put (s, tableInfo.get(s));
			}
		}
		loadTable ();
    }


    private Table ()
    {
	// A constructor solely for join. See join() code below.
    }

    void loadTable ()
    {
		String filePath = pathToTables + File.separator + name + ".table";
		rows = ParseEngine.readRecords (filePath);
		System.out.println ("Loaded table " + filePath + ": " + rows.size() + " rows");
    }

    public boolean saveTable ()
    {
		try {
			String filePath = pathToTables + File.separator + name + ".table";
			// Save old one.
			File file = new File (filePath);
			String altName = pathToTables + File.separator + "old." + name + ".table";
			File altFile = new File (altName);
			file.renameTo (altFile);
			return ParseEngine.saveRecords (filePath, rows);
		}
		catch (Exception e) {
			Log.println ("ERROR in Table.saveTable()");
			e.printStackTrace (Log.getWriter());
			return false;
		}
    }

    public String getName ()
    {
	return name;
    }

    public String getType (String columnName)
    {
		return fieldInfo.get(columnName);
    }


    public ArrayList<MyRecord> getRows ()
    {
		return rows;
    }

    public String toString ()
    {
		String s = "Table: " + name + "\n";
		for (String field: fieldInfo.keySet()) {
			s += "  " + field + ": " + fieldInfo.get(field) + "\n";
		}
		return s;
    }

    public String printRows ()
    {
		String s = "Table: " + name + "\n";
		for (MyRecord r: rows) {
			s += " " + r + "\n";
		}
		return s;
    }

    public boolean insert (MyRecord r)
    {
		// Check that all fields are present.
		if (r.keySet().size() != fieldInfo.keySet().size()) {
			Log.println ("Table:insert(): #fields mismatch for table=" + name);
			Log.println ("# record fields=" + r.keySet().size() + " #needed=" + fieldInfo.keySet().size());
			return false;
		}
		for (String field: fieldInfo.keySet()) {
			if (! r.keySet().contains(field) ) {
				Log.println ("Table:insert(): incorrect field: " + field);
				return false;
			}
		}

		// Check that it's not already there before adding.
		if (! rows.contains(r) ) {
			rows.add (r);
			return true;
		}
		Log.println ("Table:insert(): record already there: " + r);
		return false;
    }

    public Table join (Table t)
    {
		// First put together the fields for the result.
		Table result = new Table ();
		result.name = "result";
		for (String field: fieldInfo.keySet()) {
			String typeStr = fieldInfo.get(field);
			result.fieldInfo.put (field, typeStr);
		}
		for (String field: t.fieldInfo.keySet()) {
			if (! fieldInfo.keySet().contains(field)) {
				String typeStr = t.fieldInfo.get(field);
				result.fieldInfo.put (field, typeStr);
			}
		}

		// Now put matched rows into the result.
		for (MyRecord r: rows) {
			for (MyRecord r2: t.rows) {
				MyRecord r3 = r.join (r2);
				if (r3 != null) {
					result.rows.add (r3);
				}
			}
		}

		return result;
    }

}
