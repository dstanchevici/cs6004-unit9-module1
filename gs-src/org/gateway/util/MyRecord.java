package org.gateway.util;

import org.gateway.util.*;
import java.util.*;
import java.io.*;

/**
 * This class is critical for both the database and for apps. It is
 * the data structure to store a single row of a dbase table. To simplify,
 * we're going to store a row as name/value pairs in a hashmap.
 * Of course, no real dbase is going to be stored as such.
 * Calling keySet() from this hashmap gives you a set of fields.
 */

public class MyRecord {

    HashMap<String,String> nameValues = new HashMap<>();

    private MyRecord ()
    {
		// This empty constructor is for join() - see below.
    }

    public MyRecord (HashMap<String,String> nameValues)
    {
	this.nameValues = nameValues;
    }


    public void add (String name, String value)
    {
	nameValues.put (name, value);
    }

    public void add (NameValue nv)
    {
	nameValues.put (nv.name, nv.value);
    }

    public String get (String name)
    {
	return nameValues.get(name);
    }

    public double getDouble (String name)
    {
		try {
			return Double.parseDouble (get(name));
		}
		catch (Exception e) {
			return 0;
		}
    }

    public int getInt (String name)
    {
		try {
			return Integer.parseInt (get(name));
		}
		catch (Exception e) {
			return 0;
		}
    }

    public Set<String> keySet ()
    {
	return nameValues.keySet();
    }

    public boolean isField (String field)
    {
	return keySet().contains(field);
    }

    public boolean equals (Object obj)
    {
		MyRecord r = (MyRecord) obj;
		if (r.keySet().size() != keySet().size()) {
			return false;
		}
		for (String name: keySet()) {
			if (! this.get(name).equals(r.get(name))) {
				return false;
			}
		}
		return true;
    }

    public String toString ()
    {
		String s = "[";
		boolean first = true;
		for (String name: keySet()) {
			s += " {" + name + ":" + get(name) + "} ";
		}
		s += "]";
		return s;
    }
    
    public String project (Set<String> names)
    {
		// INSERT YOUR CODE HERE so that you return a String
		// version of a record (in the proper format) where
		// only the columns in the Set names are present.


		String projection = "[ ";
		for (String name: names) {
			if (! isField(name))
				continue;
			projection += "{" + name + ":" + this.get(name) + "} ";
		}
		projection += "]";

		// Temporarily:
		return projection;
    }

    public boolean matches (MyRecord r)
    {
		// Another record r matches this one if the values are the same
		// on the fields that are common.

		// First, check whether there's at least one field that matches.
		boolean noCommonFields = true;
		for (String name: r.keySet()) {
			if (isField(name)) {
				noCommonFields = false;
			}
		}
		if (noCommonFields) {
			return false;
		}

		// OK, there's at least one field in common.
		for (String name: r.keySet()) {
			if (! isField(name) ) {
			continue;               // Ignore fields that don't match.
			}
			String rVal = r.get(name);
			String val = get(name);
			if (!val.equals(rVal)) {
				return false;           // Check match on common fields.
			}
		}
		return true;
    }

    public MyRecord join (MyRecord r)
    {
		if (! matches(r) ) {
			return null;
		}

		// At this point the record r matches. We only need
		// to build the "joined" result.

		MyRecord result = new MyRecord ();

		// First put our name-value pairs in.
		for (String field: nameValues.keySet()) {
			String value = nameValues.get(field);
			result.nameValues.put (field, value);
		}

		// Look for name-value pairs where the name (field)
		// does not already exist in the first record.
		for (String field2: r.nameValues.keySet()) {
			if (! nameValues.keySet().contains(field2)) {
				String value2 = r.nameValues.get(field2);
				result.nameValues.put (field2,value2);
			}
		}

		return result;
    }

}
