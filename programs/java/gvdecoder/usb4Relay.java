
/*
usb4Relay - JAVA Serial Port USB 4relay control lib
Copyright (c) 2009 by M. Sillano (sillano@mclink.it)

You are allowed to include the source code in any product (commercial, shareware, freeware or otherwise)
when your product is released in binary form. You are allowed to modify the source code in any way you want
except you cannot modify the copyright details at the top of each module.
*/
package gvdecoder;

import java.io.*;
import gnu.io.*;  // RXTX-2.1-7 serial java library
import java.util.*;

/**
 *  Driver class for <a href="http://www.easydaq.biz/PagesUSB/USB4PRSRMx.htm">USB4xxx</a> USB 4 channel relay/DIO card.<BR>
 *  Allows easy development of portables applications for USB4xxx using java.
 *
 * @see  <a href="http://www.rxtx.org/">RXTX-2.1-7 required serial java library.</a>
 * @author M. Sillano
 * @author sillano@mclink.it
 * @version 1.00 2009/2/14
 */

public class usb4Relay  implements SerialPortEventListener {

    private final static long READTIMEOUT = 14;  // timeout for read [ms]

	static CommPortIdentifier portId;
	static Enumeration		  portList;
	SerialPort			      serialPort;

	InputStream			  inputStream;
	static OutputStream	  outputStream;

    boolean readStatusOk = false;
    long readStatusTimeout = 0;
    int  readStatus;

     /**
     * @param myport the serial port (in Windows: COMXX)
     * @see <a href="http://www.easydaq.biz/Datasheets/Data%20Sheet%2032%20(USB%20DAQ%20&%20Relay%20Card%20devices%20-%20Installation%20&%20Quickstart%20Guide).pdf"> USB4xxx card installation </a>
     */
	public usb4Relay(String myport) {

		boolean			  portFound = false;
		String			  defaultPort = "COM1";

		if (!myport.equals("")) {
			defaultPort = myport;
		} else {
			String osname = System.getProperty("os.name","").toLowerCase();
			if ( osname.startsWith("windows") ) {

	 			// windows
	 			defaultPort = "COM1";

	 		} else if (osname.startsWith("linux")) {
	 			// linux
				defaultPort = "/dev/ttyS0";
	 		} else if ( osname.startsWith("mac") ) {
	 			// mac
	 			defaultPort = "???";
	 		} else {
	 			System.out.println("Sorry, your operating system is not supported");
	 			System.exit(1);
	 		}
		}

    	System.out.println("Set default port to "+defaultPort);

		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println("Available port: " + portId.getName());
				if (portId.getName().equals(defaultPort)) {
					System.out.println("Found port: "+defaultPort);
					portFound = true;
					//nulltest reader = new nulltest();
					break;
				}
			}
		}
		if (!portFound) {
			System.out.println("port " + defaultPort + " not found.");
			System.exit(1);
		}


		try {
			serialPort = (SerialPort) portId.open("usb4Relay", 2000);
		} catch (PortInUseException e) {}

		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {}

		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {}

		serialPort.notifyOnDataAvailable(true);

		try {
// standard port params for USB4PRM
			serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
						   SerialPort.STOPBITS_1,
						   SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {}

		initwritetoport();

	}

	private void wait1ms() {
     try
      {
      Thread.sleep(1);
            }catch (InterruptedException ie) {
        System.out.println(ie.getMessage()); }
      }

	private void initwritetoport() {
		// initwritetoport() assumes that the port has already been opened and initialized

		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {}

		try {
			serialPort.notifyOnOutputEmpty(true);
		} catch (Exception e) {
			System.out.println("Error setting event notification");
			System.out.println(e.toString());
			System.exit(-1);
		}
	}


	public void serialEvent(SerialPortEvent event) {

		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer = new byte[200];
			try {
			   int numBytes = 0;
				while (inputStream.available() > 0) {
					numBytes = inputStream.read(readBuffer);
				}
				readStatus = 0xFF &(int)readBuffer[0];
//		     	System.out.println("Status ["+Integer.toHexString(readStatus)+"]");
				readStatusOk = true;
				readStatusTimeout = 0;
			} catch (IOException e) {}
			break;
		}
	}

 public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
 }

   public void sendBytes(byte[] b)  {

//		System.out.println("Send: "+ b.length+" BYTES " + Integer.toHexString(b[0]));

		try {
			outputStream.write(b);
			outputStream.flush();
		} catch (IOException e) {}
	}
    /**
     * Initialises the card (sets the port & channel I/O directions).
     * @param dir byte direction for Port B, 1=Input, 0=Output. (i.e. where dir= 0xAF -10111111- = sets bit 7 as an output, the rest as inputs). Relays must be outpus:  0xF0.
     */

   public void usbInitPorts(int dir){

        byte[] b = {0x42, (byte) (0x000000FF & dir)};
        sendBytes(b);
        readStatusOk = false;

    }

     /**
     * Writes data to Port B (i.e. data= 0x01 -00000001-, sets channel 1 to active).
     * Valid data bits are latched by the card until a further valid data byte is written to it.
.
     * @param data to be write.
     *
     */
   public void usbSetOut(int data){
       byte[] b = {0x43,(byte) (0x000000FF & data)};
        sendBytes(b);
        readStatusOk = false;
    }
    /**
     * Writes "1" to a single bit of Port B, i.e. closes a specific relay.
     * @param bit indicates the relay/DO to be set (values: 1-4 for relays, 5-8 for DO)
     *
     */
    public void usbSetOutputON(int bit){
// 8 output bits
    bit = 0x0f& (bit -1);
    int mask = 0x01 << bit;
    int s = usbGetStatusSync();
    //do
     {
      usbSetOut(s | mask);
      s = usbGetStatusSync();
//      System.out.println("Status "+ Integer.toHexString(s)+ " - mask " + Integer.toHexString(mask));
      }
    //while ((s & mask )== 0);
    }
    /**
     * Writes "0" to a single bit of Port B, i.e. open a specific relay.
     * @param bit indicates the relay/DO to be reset (values: 1-4 for relays, 5-8 for DO)
     */
     public void usbSetOutputOFF(int bit){
// 8 output bits
    bit = 0x0f& (bit -1);
    int mask = 0x01 << bit;
    int s = usbGetStatusSync();
    //do
    {
      usbSetOut(s & (~mask));
      s = usbGetStatusSync();
//      System.out.println("Status "+ Integer.toHexString(s)+ " - mask " + Integer.toHexString(mask));
      }
    //while ((s & mask )!= 0);
    }


   private void askStatus(){
      if (readStatusTimeout <= 0) { ;
        readStatusOk = false;
        readStatusTimeout = READTIMEOUT;
        byte[] b = {0x41,'x'};
        sendBytes(b);
      }
      else
        readStatusTimeout--;
      return;
    }


   /**
     * Get last valid port B read or starts a new port B read.<BR>
     * Fast, returns soon.
     *@return  port B data (0-256 = 0x00-0xFF) or -1 if data not availables.
     *@use. <pre>
          while (usbGetStatusAsyn() == -1);
     	  int data = usbGetStatusAsyn(); </pre>
     */
  public int usbGetStatusAsyn(){

      if (readStatusOk) return readStatus ;
      if (readStatusTimeout <= 0)  {
// delay before read, for better performances
      	 wait1ms();
         byte[] b = {0x41,'x'};
         sendBytes(b);
         readStatusTimeout = READTIMEOUT;
      }
      else
        readStatusTimeout--;

      return -1;
    }
    /**
     * Gets last valid port B data or reads port B.<BR>
     * Returns fast if data valid (40 microsec) or waits for data availables (15 ms - 100 ms).
     *@return  port B data (0-256 = 0x00-0xFF)

      */

  public int usbGetStatusSync(){
     while (usbGetStatusAsyn() == -1)
     	wait1ms();
     return usbGetStatusAsyn();
    }
    /**
     * Forces a new read of port B and gets the bit value.
     * @param bit indicates the relay/DI to be read (values: 1-4 for relays, 5-8 for DI)
     *@return  the bit value (0 or 1): waits for data availables (15 ms - 100 ms).
     */

  public int usbGetInput(int bit){

    askStatus();
    int s = usbGetStatusSync();
    bit = 0x0f& (bit -1);
    int mask = 0x01 << bit;
//      System.out.println("Status "+ Integer.toHexString(s)+ " - mask " + Integer.toHexString(mask));
    if ((s & mask ) == 0)  return 0;
    return 1;
    }

}
