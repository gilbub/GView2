
/*
testUsb4Relay - JAVA Serial Port USB 4relay control demo test
Copyright (c) 2009 by M. Sillano (sillano@mclink.it)

You are allowed to include the source code in any product (commercial, shareware, freeware or otherwise)
when your product is released in binary form. You are allowed to modify the source code in any way you want
except you cannot modify the copyright details at the top of each module.
*/

/**
 * Demo class for usb4Relay class, shows usb4Relay usage and mesures some execution times.
 * @author M. Sillano
 * @author sillano@mclink.it
 * @version 1.00 2009/2/14

 */

package gvdecoder;

public class testUsb4Relay {
     /**
     * @param args the command line arguments
     */
   public static void main(String[] args) {
        usb4Relay USB4PRM;
   	    if (args.length >1)
            USB4PRM = new usb4Relay(args[1]);
        else
            USB4PRM = new usb4Relay("COM3");

 // Init Relays:   4DIn + 4Relay = 0xF0
 //                4DOut + 4Relay = 0x00
        USB4PRM.usbInitPorts(0x00); // all output

// sends FF data
        long startTime = System.nanoTime();
        USB4PRM.usbSetOut(0xfF);
  		System.out.println("elapsed time [microsec] for write "+((System.nanoTime() - startTime)/1000));
// gets status
        startTime = System.nanoTime();
  		System.out.println("getting ["+Integer.toHexString(USB4PRM.usbGetStatusSync() )+"]");
  		System.out.println("elapsed time [microsec] for read "+((System.nanoTime() - startTime)/1000));
  		System.out.println("expected value: [ff]");

// gets bit value
        startTime = System.nanoTime();
        System.out.println("bit 6 is " +USB4PRM.usbGetInput(6) );
  		System.out.println("elapsed time [microsec] for get input "+((System.nanoTime() - startTime)/1000));
  		System.out.println("expected value: 1");

 // wait 1 s
       try
	      {
	      Thread.sleep(1000);
	            }catch (InterruptedException ie) {
	                 System.out.println(ie.getMessage()); }

// sends 55 data
        startTime = System.nanoTime();
        USB4PRM.usbSetOut(0x55);
  		System.out.println("55) elapsed time [microsec] for write "+((System.nanoTime() - startTime)/1000));

// gets status
        startTime = System.nanoTime();
  		System.out.println("status) getting ["+Integer.toHexString(USB4PRM.usbGetStatusSync() )+"]");
  		System.out.println("elapsed time [microsec] for read "+((System.nanoTime() - startTime)/1000));
  		System.out.println("expected value: [55]");
 // gets status again
        startTime = System.nanoTime();
  		System.out.println("status2) getting ["+Integer.toHexString(USB4PRM.usbGetStatusSync() )+"]");
  		System.out.println("elapsed time [microsec] for read "+((System.nanoTime() - startTime)/1000));
  		System.out.println("expected value: [55]");

// gets bit value
        startTime = System.nanoTime();
        System.out.println("bitvalue) bit 6 is " +USB4PRM.usbGetInput(6) );
  		System.out.println("elapsed time [microsec] for get input "+((System.nanoTime() - startTime)/1000));
  		System.out.println("expected value: 0");

// sets single relay/output ON
        startTime = System.nanoTime();
  		USB4PRM.usbSetOutputON(2);
   		System.out.println("single relay on) elapsed time [microsec] for set ON "+((System.nanoTime() - startTime)/1000));
        System.out.println("1 before nanotime");
        startTime = System.nanoTime();
 		USB4PRM.usbSetOutputON(4);
  		System.out.println("2 elapsed time [microsec] for set ON "+((System.nanoTime() - startTime)/1000));
         System.out.println("3 before nanotime");
// sets single relay/output OFF
        startTime = System.nanoTime();
        System.out.println("4 after nanotime");
  		USB4PRM.usbSetOutputOFF(1);
   		System.out.println("after usbSetOutputOFF");
   		System.out.println("single relay off) elapsed time [microsec] for set OFF "+((System.nanoTime() - startTime)/1000));
        startTime = System.nanoTime();
 		USB4PRM.usbSetOutputOFF(3);
  		System.out.println("elapsed time [microsec] for set OFF "+((System.nanoTime() - startTime)/1000));

// gets status
        startTime = System.nanoTime();
  		System.out.println("status) getting ["+Integer.toHexString(USB4PRM.usbGetStatusSync() )+"]");
  		System.out.println("elapsed time [microsec] for read "+((System.nanoTime() - startTime)/1000));
  		System.out.println("expected value: [5a]");

        System.out.println("end");
   }

}
