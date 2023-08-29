package org.gateway.browser;

import java.util.*;
import static org.gateway.browser.ElementType.*;

public class GSDocElement {

    // We're using this single class to store any kind of element,
    // so every element attribute has a variable here, which is why
    // this is a hodgepodge of variables. A more elegant design would
    // use a sub-class of GSDocElement, one per tag.

    ElementType type = none;       // What type of element.
    int fontSize;                  
    int width, height;             // What do we know about its size?
    String name;                   // For button name.
    String fileName;               // If its a local file.
    ArrayList<String> textLines;   // The text within a text-tag.
    ArrayList<GSWord> words;       // The same text as words.
    String inputText = "";         // The user-typed text (which comes later).
    String urlString;              // URL associated with server tag.

    // Size info for rectangles: inputfield, buttons.
    int topLeftX=0, topLeftY=0, boxWidth=0, boxHeight=0;

    public String toString ()
    {
        String s = "Element: type=" + type;
        if (type == inputfield) s+= " w=" + width + " name=" + name;
        else if (type == button) s+= " name=" + name;
        else if (type == image) s+= " w=" + width + " h=" + height + " filename=" + fileName;
        else if (type == vspace) s+= " h=" + height;
        else if (type == text) {
            s += " Text(size=" + fontSize + ") [";
            for (String str: textLines) {
                s += str;
            }
            s += "]";
        }
        return s;
    }
}
