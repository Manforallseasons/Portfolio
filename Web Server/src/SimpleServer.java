/**
 * A simple program demonstrating server sockets.
 *
 * @author Greg Gagne.
 * @author Micheal Zambos - Edited for Project
 */

import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SimpleServer
{
	public static final int PORT = 8080;
	
	
	public static void main(String[] args) throws java.io.IOException, ConfigurationException {
		// create a server socket listening to port 8080
		String location = args[0]; // location of XML configuration file
		Configuration configurator = new Configuration(location);
		Executor exec = Executors.newCachedThreadPool();
		ServerSocket server = new ServerSocket(PORT);
		System.out.println("Waiting for connections ....");

		try{
			while (true){
				Runnable runnable = new Connection(server.accept(), configurator);
				exec.execute(runnable);
				
			}
		}
		catch(IOException ioe){ }
		finally {
			if (server != null){
				System.out.println("Closing Connection...");
				server.close();
				System.out.println("Connection Closed.");
			}
		}

	}
}
