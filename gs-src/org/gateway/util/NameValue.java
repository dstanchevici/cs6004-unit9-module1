package org.gateway.util;

import org.gateway.util.*;
import java.util.*;

public class NameValue {

    public String name;
    public String value;

    public NameValue () {}

    public NameValue (String name, String value)
    {
	this.name = name;
	this.value = value;
    }

    public String toString ()
    {
	return name + ": " + value;
    }

}
