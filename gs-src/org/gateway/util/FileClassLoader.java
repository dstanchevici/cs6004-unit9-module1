package org.gateway.util;

import java.io.*;
import java.util.*;

/**
 * The class <code>FileClassLoader</code> contains methods to load classes from given files.
 *
 * @author <a href="Luv Kohli"></a>
 * @version 1.0
 * @since 1.0
 * @see ClassLoader
 */
public class FileClassLoader extends ClassLoader {
    private String pathName = "";

    private HashMap<String,Class> cachedClasses = new HashMap<>();

    /**
     * Creates a new <code>FileClassLoader</code> instance.
     *
     * @param path the path to the file.
     */
    public FileClassLoader(String path) {
        this.pathName = path;
    }

    /**
     * The <code>loadClass</code> method loads a class given its name.
     *
     * @param name the name of the class
     * @param resolveIt if true then the class will be resolved
     * @return the corresponding <code>Class</code> 
     * @exception ClassNotFoundException if an error occurs
     */
    public synchronized Class loadClass(String name, boolean resolveIt) throws ClassNotFoundException {
        Class resultClass;

        // first check our local cache of classes
        resultClass = (Class) cachedClasses.get(name);
        if(resultClass != null)
            return resultClass;

        // if the class has not already been loaded, try
        // to load the class data into a byte array from
        // the specified class file
        byte[] classBytes = loadFileBytes(pathName, name);

        // if the file could not be found, try to load
        // the class using the parent class loader
        if(classBytes == null) {
            resultClass = super.loadClass(name, resolveIt);
            return resultClass;
        }

        // otherwise, define the class using the byte array
        resultClass = defineClass(name, classBytes, 0, classBytes.length);
        if(resultClass == null)
            throw new ClassFormatError();

        if(resolveIt)
            resolveClass(resultClass);

        // add the class to our cache of classes that have already
        // been loaded
        cachedClasses.put(name, resultClass);

        return resultClass;
    }

    /**
     * The <code>setPath</code> method sets the path to the give path.
     *
     * @param path the new path to be set.
     */
    public void setPath(String path) {
        pathName = path;
    }

    /**
     * The <code>loadFileBytes</code> method loads the bytes of the given file described
     * by the path and class name.
     * @param path the path
     * @param className the name of the class.
     * @return the <code>byte[]</code> of the given file
     */
    private byte[] loadFileBytes(String path, String className) {
        File classFile = new File(path, className + ".class");
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(classFile);
        }
        catch(FileNotFoundException e) {
            return null;
        }

        BufferedInputStream bis = new BufferedInputStream(fis);

        byte[] bArray = new byte[(int) classFile.length()];

        try {
            bis.read(bArray, 0, bArray.length);
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return bArray;
    }
}






