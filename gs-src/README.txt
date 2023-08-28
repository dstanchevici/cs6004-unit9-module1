README file for gspackage

A README file is usually a plain-text file that accompanies
an application that's distributed free. The purpose of a README
file is to:
1. Explain what the application is about
2. Describe where to find what
3. Point to the copyright license or policy
4. Describe system requirements for running the application
5. Explain how to install and run the application
6. Identify a point of contact

******************** README FOR gspackage **********************

1. This package has three main programs to run, each run separately
   and independently. There are also a couple of test programs
   that constitute a fourth category of program. The three main
   programs are:
      * A browser called GSBrowser 
      * A webserver called GSServer
      * A database server called DBServer
   And the other test programs are in /tests

2. When unzipped, there are several directories of interest
      /apps          - This where one writes apps that run in the server.
      /databases     - Where data is stored in files for the database
      /gsmlpages     - Where the server looks for webpages written in GSML
      /logs          - Where any of the three write logs (for debugging)
      /properties    - Some parameters to set at startup
      /tests         - Test code
   There are also some GSML files and images outside these folders.
   These are for testing. And a file called gspackage.jar that
   contains the executables.

3. Look at LICENSE.txt for the license.

4. To run this application requires Java 10.02 or higher.

5. Installation and execution
    (1) First, make a directory called gspackage
    (2) Download gsdemo.zip into this directory
    (3) Unpack gsdemo.zip. This will create the sub-directories
        named above, along with some files in the /gspackage directory.
    (4) To run the browser, you need to open Terminal (Mac) or DOS-Prompt
        (Windows) and be in the main /gspackage directory. 
        At the commandline, type
            java -jar gspackage.jar browser
    (5) Similarly, to run the server
            java -jar gspackage.jar server
    (6) And to run the database server
            java -jar gspackage.jar dbase
    Each of these will tie up the command-line window.

    IMPORTANT: To exit any of these, type Control-C.

    Configurations under which you run combinations:
    (I) Browser only (just run the browser)
    (II) Browser-server
        - In this case, run the server first in one Terminal
          and then bring up the browser in another Terminal.
    (III) Browser-server-database
        - First run the database server in one Terminal
        - Then run the server in another
        - Then run the browser in a third Terminal
    (IV) Server alone or Database alone
        - Run any of these in a Terminal window

6. GSPackage has been written by Prof. Rahul Simha to help
   students understand how 3-tier applications work.

******************** END-README FOR gspackage **********************
   



