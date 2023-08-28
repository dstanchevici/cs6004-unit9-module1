package org.gateway.browser;

import java.util.*;

public class GSWord {
    
    String word = "";
    String linkURL = null;

    // We need to know how it's rendered so that we can determine
    // if a mouseclick occurred on the word.
    int topLeftX, topLeftY, boxWidth, boxHeight;

    public GSWord (String word, String linkURL) 
    {
	this.word = word;
	this.linkURL = linkURL;
    }

    public String toString ()
    {
	String s = "Word=" + word;
	if (linkURL != null) {
	    s += "(Link=" + linkURL + ")";
	}
	return s;
    }

}
