/**
 * This is the separate thread that services each
 * incoming echo client request.
 *
 * @author Greg Gagne 
 */

import java.net.*;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

public class Connection implements Runnable
{
	private Socket client;
	private Configuration configuration;	
	private BufferedReader clientFrom = null;
	private DataOutputStream clientTo = null;
	private String logCall = null;
	
	public Connection(Socket client, Configuration configuration) {
		this.client = client;
		this.configuration = configuration;
	}

    /**
     * This method runs in a separate thread.
     */	
	public void run() {
		try
		{
			clientFrom = new BufferedReader(new InputStreamReader (client.getInputStream()));
			clientTo = new DataOutputStream(client.getOutputStream());
			String request = clientFrom.readLine();
			//This is a very strange way to do it I think but somebody suggested I use it.
			StringTokenizer tokenizer = new StringTokenizer(request);
			String methodToUse = tokenizer.nextToken();
			String resourceString = tokenizer.nextToken();
			//what is up with the double quotes? Whack, y'all.
			logCall = "\"" + request.trim() + "\" ";
			
			if (methodToUse.equals("GET")){
				String fileName = resourceString;
				
				//ugh jesus this is so ugly. But it works for me so eh
				//god I hope nobody ever has to look at this code ever again.
				//this is a nightmare statement the gods of code will smite me for it
				//it's a sin
				fileName = configuration.getDocumentRoot() + URLDecoder.decode(fileName, 
				StandardCharsets.UTF_8.toString());
				
				if (resourceString.equals("/")){
					fileName = configuration.getDefaultDocument();
				}
				
				if(new File(fileName).isFile()){
					serverResponse(200, fileName, true);
				}
				else if(fileName.equals(configuration.getDefaultDocument())){
					File otherDefault = new File(fileName);
					OutputStream outputStream = null;
					try{
						outputStream =  new FileOutputStream(otherDefault);
						String otherContent = "<html><title>" + configuration.getServerName() + 
						"</title><body>Do crabs think people walk sideways?</body></html>";
						//DO crabs think people walk sideways? Why isn't the mainstream
						//media reporting on this? What don't they want you to know?
						int len = otherContent.length();
						outputStream.write(otherContent.getBytes(), 0, len);
						outputStream.flush();
					}
					finally{
						outputStream.close();
					}
					serverResponse(200, fileName, true);
				}
				else{
					//Hey Greg if you see this remind me to show you that
					//XKCD comic about what you should do with 404s.
					serverResponse(404, "", false);
				}
			}
			else{
				//if this ever happens I'm gonna blow up monaco
				serverResponse(405, "", false);
			}
			
		}
			
		catch(java.io.IOException ioe){
			System.err.println(ioe);
		}
			
	}
	
	public void serverResponse(int code, String fileString, boolean fileCheck) throws IOException {
		String status = "HTTP/1.1 ";
		String statusUpdate = null;
		String date = getServerTime();
		String serverName = "Server: " + configuration.getServerName() +"\r\n";
		String contentType = "Content-Type: text/html\r\n";
		String contentLen = null;
		String fileName = null;
		FileInputStream fileData = null;
		int len = 0;
		
		if (code == 200){
			statusUpdate = "200 OK\r\n";
			status += statusUpdate;
		}
		else if(code == 404){
			statusUpdate = "404 Not Found\r\n";
			status += statusUpdate;
		}
		else{
			statusUpdate = "405 Method Not Allowed\r\n";
			status += statusUpdate;
		}
		if (fileCheck){
			fileName = fileString;
			fileData = new FileInputStream(fileName);
			len = fileData.available();
			contentLen = "Content-Length: " + len + "\r\n";
			//I keep thinking I'm missing something but I'm not sure what it would be.
			if(!fileName.endsWith(".html")){
				if(fileName.endsWith(".gif")){
					contentType = "Content-Type: image/gif\r\n";	
				}
				else if(fileName.endsWith(".jpg")){
					contentType = "Content-Type: image/jpeg\r\n";
				}
				else if(fileName.endsWith(".png")){
					contentType = "Content-Type: image/png\r\n";
				}
				else if(fileName.endsWith(".txt")){
					contentType = "Content-Type: text/plain\r\n";
				}
			}
		}
		else{
			fileString = "<html><title>HTTP Server</title><body>" + statusUpdate + "</body></html>";
			len = fileString.length();
			contentLen = "Content-Length: " + len + "\r\n";
		}
		
		String logText = client.getInetAddress().getHostAddress() + 
		" [" + date + "] " + logCall + code + " " + len + "\r\n\n";
		System.out.print(logText);
		
		File logFile = new File(configuration.getDocumentRoot() + configuration.getLogFile());
		OutputStream outputStream = null;
		
		try{
			if(logFile.isFile()){
				outputStream = new FileOutputStream(logFile, true);
			}
			else{
				outputStream = new FileOutputStream(logFile);
			}
			String otherContent = logText;
			int logLen = otherContent.length();
			outputStream.write(otherContent.getBytes(), 0, logLen);
			outputStream.flush();
		}
		finally{
			if (outputStream != null){
				outputStream.close();
			}
		}
		//There's probably a more efficient way to do this but no thanks.
		clientTo.writeBytes(status);
		clientTo.writeBytes(date);
		clientTo.writeBytes(serverName);
		clientTo.writeBytes(contentType);
		clientTo.writeBytes(contentLen);
		clientTo.writeBytes("Connection: close\r\n");
		clientTo.writeBytes("\r\n");
		
		if (fileCheck){
			sendData(fileData, clientTo);
		}
		else{
			clientTo.writeBytes(fileString);
		}
		clientTo.close();
		clientFrom.close();
	}
	//this is the last fuction. It sends data.
	//It also sends my hopes and dreams
	//because thank god I'm finally done.
	public void sendData(FileInputStream data, DataOutputStream out) throws IOException{
		byte[] BYTE_BUFFER = new byte[1024]; //God I hope that's the right number to use.
		int bytes;
		while((bytes = data.read(BYTE_BUFFER)) != -1){
			out.write(BYTE_BUFFER, 0, bytes);
		}
		data.close();
	}
	
	//I had some help from my mom on this one. Server time is weird.
	private String getServerTime() 
	{
	    Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return dateFormat.format(calendar.getTime());
	}
}
	