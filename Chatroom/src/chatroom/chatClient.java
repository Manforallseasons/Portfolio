package chatroom;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
//import javax.json.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area.
 */
public class chatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    String name;

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public chatClient() {

        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.pack();

        // Add Listeners
        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server.    Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
            	JSONObject objSend = new JSONObject();
            	objSend.put("from", name);
            	objSend.put("to", 0);
            	String mess = textField.getText();
            	textField.setText("");
            	objSend.put("message", mess);
            	objSend.put("type", "chatroom-send");
            	int messLen = mess.length();
            	objSend.put("message-len", messLen);
            	String objSendOut = objSend.toString();
                out.println(objSendOut);
            }
        });
        //sends the 'chatroom-end' protocol when closing the window.
        //Closing the window might cause an error to appear, but it seems
        //to work just fine.
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            	JSONObject end = new JSONObject();
            	end.put("type", "chatroom-end");
            	end.put("username", name);
            	String endString = end.toString();
            	out.println(endString);
            }
        });
    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Connects to the server then enters the processing loop.
     */

    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 8029);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        JSONParser parser = new JSONParser();
        int id = 0;
        //Makes sure the username isn't more than 20 characters
        while(true){
        	name = getName();
        	if(name.length() <= 20){
        		break;
        	}
        }
    	int len = name.length();
    	JSONObject begin = new JSONObject();
    	begin.put("type","chatroom-begin");
    	begin.put("username", name);
    	begin.put("len", len);
    	String obj = begin.toString();
        out.println(obj);
        String line = in.readLine();
        try {
			JSONObject objLine = (JSONObject) parser.parse(line);
			//makes sure the username is not already in use.
			while(true){
				if(((String) objLine.get("type")).equals("chatroom-response")){
					id = ((Long) objLine.get("id")).intValue();
					if (id == -1){
						messageArea.append("ERROR! Too many users. Closing window... \n");
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.exit(0);
					}
					else{
						textField.setEditable(true);
						break;
					}
				}
				else if (((String) objLine.get("type")).equals("chatroom-error")){
					messageArea.append("ERROR! Username is use! Closing window... \n");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.exit(0);
				}
			}
        }
        catch(ParseException e){
        	e.printStackTrace();
        }

        // Process all messages from server, according to the protocol.
        while (true) {
            String messLine = in.readLine();
            try {
				JSONObject objLine = (JSONObject) parser.parse(messLine);
				//broadcasts the message received from the server to the message area.
				if (((String) objLine.get("type")).equals("chatroom-broadcast")){
					String message = (((String) objLine.get("message")));
					String sender = ((String) objLine.get("from"));
					long messLen = ((Long) objLine.get("message-length"));
					if(messLen <= 280){
						messageArea.append(sender + ": " + message + "\n");
					}
					else{
						messageArea.append("Error! Message is too long! \n");
					}
				}
				//The client does not support special types of broadcasts.
				else if (((String) objLine.get("type")).equals("chatroom-special")){
					messageArea.append("System: This type of message is not supported! \n");
				}
				//updates when a user leaves or enters the chatroom
				else if (((String) objLine.get("type")).equals("chatroom-update")){
					String updateType = ((String) objLine.get("type-of-update"));
					String updateUser = ((String) objLine.get("username"));
					if (updateType.equals("enter")){
						messageArea.append(updateUser + " has joined the server! \n");
					}
					else if (updateType.equals("leave")){
						messageArea.append(updateUser + " has left the server! \n");
					}
					else{
						messageArea.append("Error! Undefined Update Type. \n");
					}
				}
				//various error handlings.
				else if (((String) objLine.get("type")).equals("chatroom-error")){
					String errorType = ((String) objLine.get("type-of-error"));
					if(errorType.equals("unexpected-dealio-type")){
						messageArea.append("Error! Unexpected Dealio Type! \n");
					}
					if(errorType.equals("malformed-dealio")){
						messageArea.append("Error! Malformed Dealio. \n");
					}
					if(errorType.equals("client-time-out")){
						messageArea.append("Error! Client timed out. \n");
					}
					if(errorType.equals("message-exceeded-max-length")){
						messageArea.append("Error! Message exceeded max length. \n");
					}
					if(errorType.equals("id-not-found")){
						messageArea.append("Error! ID not found. \n");
					}
					if(errorType.equals("unsupported-file-type")){
						messageArea.append("Error! Unsupported file type. \n");
					}
					if(errorType.equals("file-size-exceeded")){
						messageArea.append("Error! File size exceeded. \n");
					}
					if(errorType.equals("user-name-length-exceeded")){
						messageArea.append("Error! Username length exceeded. \n");
					}
					if(errorType.equals("special-unsupported")){
						messageArea.append("Error! Undefined error. \n");
					}
				}
				
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        chatClient client = new chatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}