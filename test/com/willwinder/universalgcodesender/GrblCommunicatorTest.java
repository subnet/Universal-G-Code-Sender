/*
    Copywrite 2013-15 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.utils.CommUtils;
import com.willwinder.universalgcodesender.mockobjects.MockConnection;
import com.willwinder.universalgcodesender.mockobjects.MockGrbl;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.io.IOException;
import java.util.LinkedList;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author wwinder
 */
public class GrblCommunicatorTest {
    MockGrbl mg;
    LinkedList<String> cb;
    LinkedList<String> asl;
    
    public GrblCommunicatorTest() {
    }

    @Before
    public void setUp() {
        this.mg = new MockGrbl();
        this.cb = new LinkedList<>();
        this.asl = new LinkedList<>();
    }

    /**
     * Test of setLineTerminator method, of class GrblCommunicator.
     */
    @Test
    public void testSetLineTerminator() {
        System.out.println("setLineTerminator");
        GrblCommunicator instance = new GrblCommunicator();
        
        //((AbstractCommunicator)instance).addConnectionType(conn);
        
        String defaultTerminator = AbstractCommunicator.DEFAULT_TERMINATOR;
        
        // Initial value.
        assertEquals(defaultTerminator, instance.getLineTerminator());
        
        instance.setLineTerminator("tada");
        assertEquals("tada", instance.getLineTerminator());
        
        instance.setLineTerminator(null);
        assertEquals(defaultTerminator, instance.getLineTerminator());
        
        instance.setLineTerminator("");
        assertEquals(defaultTerminator, instance.getLineTerminator());
    }

    /**
     * Test of openCommPort method, of class GrblCommunicator.
     */
    @Test
    public void testOpenCommPort() throws Exception {
        System.out.println("openCommPort");
        System.out.println("-not testing RXTX.");
    }

    /**
     * Test of closeCommPort method, of class GrblCommunicator.
     */
    @Test
    public void testCloseCommPort() {
        System.out.println("closeCommPort");
        System.out.println("-not testing RXTX.");
    }

    /**
     * Test of queueStringForComm method, of class GrblCommunicator.
     */
    @Test
    public void testQueueStringForComm() throws Exception {
        
        System.out.println("queueStringForComm");
        String input = "someCommand";
        MockConnection mc = new MockConnection(mg.in, mg.out);
        GrblCommunicator instance = new GrblCommunicator(cb, asl, mc);
        
        try {
            instance.queueStringForComm(input);
            // The cb preloads commands so the size represents queued commands.
            assertEquals(1, cb.size());
            
            // Test that instance adds newline to improperly formed command.
            assertEquals(input + "\n", cb.peek());
            
            instance.queueStringForComm(input);
            instance.queueStringForComm(input);
            
            // Test that instance continues to queue inputs.
            assertEquals(3, cb.size());
            
            input = "someCommand\n";
            cb = new LinkedList<>();
            mc = new MockConnection(mg.in, mg.out);
            instance = new GrblCommunicator(cb, asl, mc);

            
            instance.queueStringForComm(input);
            // Test that instance doesn't add superfluous newlines.
            assertEquals(input, cb.peek());

        } catch (Exception e) {
            fail("queueStringForComm threw an exception: "+e.getMessage());
        }
    }

    
    /**
     * Test of sendStringToComm method, of class GrblCommunicator.
     */
    /* This function is private...
    @Test
    public void testSendStringToComm() {
        System.out.println("sendStringToComm");
        GrblCommunicator instance = new GrblCommunicator(mg.in, mg.out, cb, asl);

        
        String command = "someCommand";
        instance.sendStringToComm(command);
        
        // Make sure the string made it to GRBL.
        assertEquals(command, mg.readStringFromGrblBuffer());
    }
    */
    /**
     * Test of sendByteImmediately method, of class GrblCommunicator.
     */
    @Test
    public void testSendByteImmediately() {
        System.out.println("sendByteImmediately");
        MockConnection mc = new MockConnection(mg.in, mg.out);
        GrblCommunicator instance = new GrblCommunicator(cb, asl, mc);

        // Ctrl-C is a common byte to send immediately.
        byte b = 0x18;
        
        try {
            instance.sendByteImmediately(b);
        } catch (Exception e) {
            fail("sendByteImmediately threw an exception: " + e.getMessage());
        }

        // Make sure the string made it to GRBL.
        assertEquals(b, mg.readByteFromGrblBuffer());

        // Test my readByteFromGrblBuffer command (should be empty).
        assertEquals(0x0, mg.readByteFromGrblBuffer());
        
        try {
            // Buffer multiple bytes
            instance.sendByteImmediately((byte)0x18);
            instance.sendByteImmediately((byte)0x19);
            instance.sendByteImmediately((byte)0x20);
            instance.sendByteImmediately((byte)0x21);
        } catch (Exception e) {
            fail("sendByteImmediately threw an exception: " + e.getMessage());
        }

        assertEquals(0x18, mg.readByteFromGrblBuffer());
        assertEquals(0x19, mg.readByteFromGrblBuffer());
        assertEquals(0x20, mg.readByteFromGrblBuffer());
        assertEquals(0x21, mg.readByteFromGrblBuffer());
        
        // Test my readByteFromGrblBuffer command (should be empty).
        assertEquals(0x0, mg.readByteFromGrblBuffer());
    }

    /**
     * Test of areActiveCommands method, of class GrblCommunicator.
     */
    @Test
    public void testAreActiveCommands() {
        System.out.println("areActiveCommands");
        MockConnection mc = new MockConnection(mg.in, mg.out);
        GrblCommunicator instance = new GrblCommunicator(cb, asl, mc);
        
        boolean expResult = false;
        boolean result = instance.areActiveCommands();
        
        // Empty case.
        assertEquals(expResult, result);
        
        // Add a command and stream it (activate it).
        instance.queueStringForComm("command one");
        instance.streamCommands();
        expResult = true;
        result = instance.areActiveCommands();
        assertEquals(expResult, result);
        
        // Add another command and stream it.
        instance.queueStringForComm("command one");
        instance.streamCommands();
        expResult = true;
        result = instance.areActiveCommands();
        assertEquals(expResult, result);
        
        // Send result to communicator.
        instance.responseMessage("ok");
        instance.responseMessage("ok");
                
        expResult = false;
        result = instance.areActiveCommands();
        assertEquals(expResult, result);
    }

    /**
     * Test of sendStringToComm method, of class GrblCommunicator.
     */
    @Test
    public void testStreamCommands() {
        System.out.println("streamCommands");
        MockConnection mc = new MockConnection(mg.in, mg.out);
        GrblCommunicator instance = new GrblCommunicator(cb, asl, mc);
        String thirtyNineCharString = "thirty-nine character command here.....";

        boolean result;
        boolean expResult;
        
        // Make sure CommUtil is still an overly cautious jerk.
        LinkedList<GcodeCommand> l = new LinkedList<>();
        l.add(new GcodeCommand("12characters"));
        assertEquals(13, CommUtils.getSizeOfBuffer(l));

        // Make sure GrblUtils hasn't updated RX buffer size.
        assertEquals(123, GrblUtils.GRBL_RX_BUFFER_SIZE);

        // Add a bunch of commands so that the buffer is full.
        // 39*3 + 3 newlines + 3 CommUtils caution  = 123 == buffer size.
        instance.queueStringForComm(thirtyNineCharString);
        instance.queueStringForComm(thirtyNineCharString);
        instance.queueStringForComm(thirtyNineCharString);
        
        // Stream them so that there are active commands.
        instance.streamCommands();
        expResult = true;
        result = instance.areActiveCommands();
        assertEquals(expResult, result);

        // Add another command and stream it so that something is queued.
        instance.queueStringForComm(thirtyNineCharString);
        instance.streamCommands();
        expResult = true;
        result = instance.areActiveCommands();
        assertEquals(expResult, result);

        // Check with MockGrbl to verify the fourth command wasn't sent.
        String output = mg.readStringFromGrblBuffer();
        assertEquals(thirtyNineCharString+"\n"+thirtyNineCharString+"\n"+thirtyNineCharString+"\n",
                        output);
        
        // Make room for the next command.
        mc.sendResponse("ok");
        
        // Send it.
        instance.streamCommands();
        
        // Make sure the queued command was sent.
        output = mg.readStringFromGrblBuffer();
        assertEquals(thirtyNineCharString+"\n", output);
  
        // Wrap up.
        mc.sendResponse("ok");
        mc.sendResponse("ok");
        mc.sendResponse("ok");

        expResult = false;
        result = instance.areActiveCommands();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of pauseSend method, of class GrblCommunicator.
     */
    @Test
    public void testPauseSendAndResumeSend() {
        System.out.println("pauseSend");
        MockConnection mc = new MockConnection(mg.in, mg.out);
        GrblCommunicator instance = new GrblCommunicator(cb, asl, mc);
        String twentyCharString = "twenty characters...";
        String grblReceiveString;
        String arr[];
        int expectedInt;
        
        // Queue a bunch of commands
        for (int i=0; i < 30; i++) {
            instance.queueStringForComm(twentyCharString);
        }
        
        // Fill initial buffer, then pause.
        instance.streamCommands();
        instance.pauseSend();

        // Check that the correct number of commands were buffered (even though
        // that isn't pause/resume).
        grblReceiveString = mg.readStringFromGrblBuffer();
        arr = grblReceiveString.split("\n");
        expectedInt = GrblUtils.GRBL_RX_BUFFER_SIZE / (twentyCharString.length()+1);
        assertEquals(expectedInt, arr.length);

        for (String arr1 : arr) {
            mc.sendResponse("ok");
        }

        // Make sure we don't stream anymore.
        instance.streamCommands();
        grblReceiveString = mg.readStringFromGrblBuffer();
        expectedInt = 0;
        assertEquals(expectedInt, grblReceiveString.length());

        instance.resumeSend();
        
        // Make sure streaming continued after resuming.
        instance.streamCommands();
        grblReceiveString = mg.readStringFromGrblBuffer();
        arr = grblReceiveString.split("\n");
        expectedInt = GrblUtils.GRBL_RX_BUFFER_SIZE / (twentyCharString.length()+1);
        assertEquals(expectedInt, arr.length);
    }

    /**
     * Test of cancelSend method, of class GrblCommunicator.
     */
    @Test
    public void testCancelSend() {
        System.out.println("cancelSend");
        MockConnection mc = new MockConnection(mg.in, mg.out);
        GrblCommunicator instance = new GrblCommunicator(cb, asl, mc);
        String twentyCharString = "twenty characters...";
        String grblReceiveString;
        String arr[];
        int expectedInt;
        Boolean expectedBool;
        
        // 1.
        // Queue some commands, but cancel before sending them.
        for (int i=0; i < 30; i++) {
            instance.queueStringForComm(twentyCharString);
        }
        instance.cancelSend();

        // try streaming them and make sure nothing is received.
        instance.streamCommands();
        grblReceiveString = mg.readStringFromGrblBuffer();
        expectedInt = 0;
        assertEquals(expectedInt, grblReceiveString.length());
        
        // 2.
        // We can't un-send things to GRBL, so make sure we still acknowledge
        // that there are active commands.
        for (int i=0; i < 30; i++) {
            instance.queueStringForComm(twentyCharString);
        }
        instance.streamCommands();
        instance.cancelSend();
        // Verify that there are several active commands.
        expectedBool = true;
        assertEquals(expectedBool, instance.areActiveCommands());
        
        grblReceiveString = mg.readStringFromGrblBuffer();
        arr = grblReceiveString.split("\n");
        expectedInt = GrblUtils.GRBL_RX_BUFFER_SIZE / (twentyCharString.length()+1);
        assertEquals(expectedInt, arr.length);

        for (String arr1 : arr) {
            mc.sendResponse("ok");
        }
            
        // Make sure canceled commands are not sent.
        instance.streamCommands();
        grblReceiveString = mg.readStringFromGrblBuffer();
        expectedInt = 0;
        assertEquals(expectedInt, grblReceiveString.length());
    }

    /**
     * Test of softReset method, of class GrblCommunicator.
     */
    @Test
    public void testSoftReset() {
        System.out.println("softReset");
        MockConnection mc = new MockConnection(mg.in, mg.out);
        GrblCommunicator instance = new GrblCommunicator(cb, asl, mc);
        String twentyCharString = "twenty characters...";
        String grblReceiveString;
        String arr[];
        int expectedInt;
        Boolean expectedBool;
        
        // This is essentially a cancel that "un sends" commands.
        // Send some commands, make them active, then soft-reset to reset them.
        for (int i=0; i < 30; i++) {
            instance.queueStringForComm(twentyCharString);
        }
        instance.streamCommands();
        instance.softReset();
        // Verify that there are several active commands.
        expectedBool = false;
        assertEquals(expectedBool, instance.areActiveCommands());
    }
}
