xmodem - quick and dirty XMODEM client for Java
===============================================

(c) 2009 Jason Frame (jason@magiclamp.co.uk)  
Released under The MIT License.

Everything You Need To Know
---------------------------

XMODEM is a prehistoric protocol that was used to send files over modem links. As we discovered today, it's also used by some (otherwise cutting-edge) RFID readers.

Usage
-----

Import the shizzle (there are only two classes, `Client` and `TransferException`):

    import uk.co.magiclamp.xmodem.*;

Create an instance thusly:

    Client c = new Client(myInputStream, myOutputStream);
    
If you're using a non-standard padding character (default is `SUB`, i.e. `0x1A`), you may pass it as a third paramater:

    Client c = new Client(myInputStream, myOutputStream, 0x0);
  
Reading your file is as simple as:

    byte[] myFile = c.read();
  
That's it.

Reporting Bugs
--------------

Send to jason@magiclamp.co.uk please.