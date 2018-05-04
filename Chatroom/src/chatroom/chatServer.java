package chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A multithreaded chat room server.  
 * Micheal Zambos
 * 
 * 
 */
public class chatServer {

    /**
     * The port that the server listens on.
     */
    private static final int PORT = 8029;

    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */
    private static HashSet<String> names = new HashSet<String>();
    
    private static int[] ids = new int[100];

    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    
    static int users = 0;

    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        int usersNumb = 0;
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private static class Handler extends Thread {
        private String name;
        private int nameLen;
        private String inputF;
        private Socket socket;
        private int Id;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                JSONParser parser = new JSONParser();

                while (true) {
                	
                    inputF = in.readLine();
                    try {
						JSONObject objName = (JSONObject) parser.parse(inputF);
						if(((String) objName.get("type")).equals("chatroom-begin")){
							name = (String) objName.get("username");
							nameLen = ((Long) objName.get("len")).intValue();
						}
						else{
							return;
						}
						
					} catch (ParseException e) {
						e.printStackTrace();
					}
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                        else{
                        	JSONObject usedName = new JSONObject();
                        	usedName.put("type", "chatroom-error");
                        	out.println(usedName.toString());
                        }
                    }
                    
                }

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                JSONObject accept = new JSONObject();
                accept.put("type", "chatroom-response");
                if(users<100){
                	int i = 0;
                	for(i = 0; i < 100; i++){
                		if(ids[i] == 0){
                			accept.put("id", i);
                			Id = i;
                			users++;
                			break;
                		}
                	}
                }
                else{
                	accept.put("id",-1);
                }
                accept.put("users", users);
                String response = accept.toString();
                out.println(response);
                
                //Update to inform when a user enters. 
                JSONObject updateJoin = new JSONObject();
                updateJoin.put("type", "chatroom-update");
                updateJoin.put("type-of-update", "enter");
                updateJoin.put("username", name);
                for (PrintWriter writer : writers){
                	writer.println(updateJoin.toString());
                }
                
                writers.add(out);

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    JSONObject messinp = new JSONObject();
					try {
						messinp = (JSONObject) parser.parse(input);
	                    if (((String) messinp.get("type")).equals("chatroom-send")){
	                    	String sender = ((String) messinp.get("from"));
	                    	String message = ((String) messinp.get("message"));
	                    	JSONObject messout = new JSONObject();
	                    	messout.put("from", sender);
	                    	messout.put("type", "chatroom-broadcast");
	                    	messout.put("message", message);
	                    	int messLen = message.length();
	                    	messout.put("message-length", messLen);
	                    	messout.put("to", 0);
	                    	String messoutString = messout.toString();
	                    	for (PrintWriter writer : writers) {
	                            writer.println(messoutString);
	                        }
	                    }
	                    else if (((String) messinp.get("type")).equals("chatroom-end")){
	                    	String endName = ((String) messinp.get("username"));
	                    	JSONObject updateLeave = new JSONObject();
	                    	updateLeave.put("Username", endName);
	                    	updateLeave.put("type", "chatroom-update");
	                    	updateLeave.put("type-of-update", "leave");
	                    	if (name != null){
	                    		names.remove(name);
	                    	}
	                    	if (out != null){
	                    		writers.remove(out);
	                    	}
	                    	for (PrintWriter writer : writers){
	                    		writer.println(updateLeave.toString());
	                    	}
	                    	try{
	                    		socket.close();
	                    	}
	                    	catch(IOException e){
	                    	}
	                    }
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    if (input == null) {
                        return;
                    }

                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                ids[Id] = 0;
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}