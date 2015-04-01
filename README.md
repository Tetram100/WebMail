WebMail
=======

WebMail Assignment for the IK2213 course. It is composed of a basic java web server and a basic webmail. On the front page you can send and plan the sending of emails. An admin page shows the pending emails.


How to compile
-----------------------
The program is already compiled and ready to use. But if you want to change a java file in the src and compile it again, here is the steps:

First you must go on the root of the folder.  
Then write "javac -cp lib/dnsjava-2.1.7.jar src/* -d bin/" in order to make the compiled files of the .java files.  
Then write "jar cvfm WebMail.jar META-INF/MANIFEST.MF -C bin/ ." to create the Jar file Webmail.jar (the "." at the end is important).


How to configure
-----------------------
The jar file is ready to lunch and you have nothing to configure. But you still can change the default value of the SMTP server in the Email.java file (in case of MX lookup failure) and the ports of the web server and SMTP server in the Email.java and Main.java class.


How to run
-----------------------
Juste write "sudo java -jar WebMail.jar". You must run the program as a root user in order to listen on the 80 port.  
The jar file must be launch in the same folder as the lib folder and the Web folder in order to have access to these resources otherwise it won't work.


How to use
-----------------------
Just write your server IP on the browser and you will access to the web page. Everything is ovious from here.