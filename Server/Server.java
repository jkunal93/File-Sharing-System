// This is Server file for a dropbox-prototype program

package Server;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Hashtable;
import java.util.Scanner;

public class Server{

	public static void main(String [] args){
		int port = 9876;
		
		//Establish the listen socket
		ServerSocket welcomeSocket=null;
		try {
			welcomeSocket = new ServerSocket(port);
			//System.out.println("Server waiting at: "+port);
		} catch (IOException e1) {
			System.out.println("Sorry can not run server on port "+port);
			e1.printStackTrace();
		}
		
		//Listen for a TCP connection request
		while(true){
			Socket ClientSocket=null;
			try {
				ClientSocket = welcomeSocket.accept();
				System.out.println("Connection established");
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
			//Construct an Object to Process an http request
			Implementation request = new Implementation(ClientSocket);
			
			//Create new thread to process the request
			Thread thread = new Thread(request);
			
			//start the thread
			thread.start();

		}

	}
}

final class Implementation implements Runnable{
	Socket ClientSocket;
	Hashtable<String,String> hashTable;
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;
	InputStream inFromClientByte = null;
	OutputStream outToClientByte = null;

	//Constructor
	public Implementation(Socket ClientSocket){
		this.ClientSocket = ClientSocket;
		hashTable = new Hashtable<String,String>();
		try{
			inFromClient = new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
			outToClient = new DataOutputStream(ClientSocket.getOutputStream());
			inFromClientByte = ClientSocket.getInputStream();
			outToClientByte = ClientSocket.getOutputStream();
		}catch(Exception e){
			System.out.println("Something wrong happen creating stream...");
			return;
		}
	}

	@Override
	public void run() {
		
		try{
			processRequest();
		}
		catch(Exception e){
			System.out.println(e);
		}
	}

	public void processRequest(){
		try {
			String input=null;
			while((input = inFromClient.readLine())!="exit"){
			if(input.equals("regNew()")){
				NewReg();
			}
			else if(input.equals("login()")){
				loginAcc();
			}
			}
			inFromClient.close();
			inFromClientByte.close();
			outToClient.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	//Login for existing user
	public void loginAcc(){
		hashTable.put("kunal", "joshi");
		hashTable.put("k", "j");
		
		String value = null;
		String dirName = null;
		String logout = null;
		try{
			String receiveLine = inFromClient.readLine();
			dirName = receiveLine;
			//System.out.println("Flag 2");
			if(!hashTable.contains(receiveLine)){
				outToClient.writeBytes("true\n");
				outToClient.flush();
				value = hashTable.get(receiveLine);
				//System.out.println("value is: "+value);
			
				String tempPass = inFromClient.readLine();
				if(tempPass.equals(value)){
					outToClient.writeBytes("true\n");
					outToClient.flush();
					System.out.println("Server responded for Pass");
					String optionForLoggedin = null;

					while(true){
						optionForLoggedin = inFromClient.readLine();
						if(optionForLoggedin.equals("seeFiles()")){
							showFiles(dirName);
						}
						else if(optionForLoggedin.equals("uploadFiles()")){
							String fileName = null;
							uploadFile(dirName);
						}
						else if(optionForLoggedin.equals("download()")){
							downloadFiles(dirName);
						}
						else if(optionForLoggedin.equals("sync()")){
							syncFiles(dirName);
						}
						else if(optionForLoggedin.equals("logout()")){
							//System.out.println("in logout");
							outToClient.writeBytes("true\n");
							outToClient.flush();
							break;
						}
						else if(optionForLoggedin.equals("deleteFiles()")){
							deleteFiles(dirName);
						}
						else if(optionForLoggedin.equals("fileShare()")){
							fileShare(dirName);
						}
					}

				}
				else{
					outToClient.writeBytes("false\n");
					outToClient.flush();
					//System.out.println("Server responded for Pass");
				}
			}
			else{
				outToClient.writeBytes("false\n");
				outToClient.flush();
				System.out.println("Server responded for email not exist");
			}

		}
		catch(IOException e){
			System.out.println("IO Exception");
		}
	}

	// Synchronize Files between Client and Server
	public void syncFiles(String dirName){
		byte[] bytes = new byte[100];
		File path = new File("/Dropbox/src\\Server\\".concat(dirName));
		String bool ="";
		    File [] files = path.listFiles();
		    int length = files.length;
		    String slength = Integer.toString(length);
		    System.out.println("slength is: "+slength);
		    try {
		    	//System.out.println("Sent slength: "+slength+"\n");
				outToClient.writeBytes(slength.concat("\n"));
				outToClient.flush();
			} catch (IOException e1) {
				System.out.println("Opps Exception");
				//e1.printStackTrace();
			}
		    for (int i = 0; i < files.length; i++){
		        if (files[i].isFile()){ //this line weeds out other directories/folders
		            System.out.println(files[i].getName());
		            try {
						outToClient.writeBytes(files[i].getName()+"\n");
						outToClient.flush();
						
					} catch (IOException e) {
						System.out.println("Exception is happening here");
						//e.printStackTrace();
					}

		        }
		    }
		    System.out.println("Sync completed");

	}
	
	// Delete file from server side
	public void deleteFiles(String dirName){
		String pathForCurrentUser = "/Dropbox/src\\Server\\".concat(dirName);
		String fileName;
		
		try {
			fileName = inFromClient.readLine();
			//System.out.println(fileName);
			File source = new File(pathForCurrentUser+"\\"+fileName);
			Files.delete((source).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//Share file among users
	public void fileShare(String dirName){
		try {
			
			String newDir = inFromClient.readLine();
			//System.out.println("New User Name is: "+newDir);
			String pathForCurrentUser = "/Dropbox/src/Server/".concat(dirName);
			String pathForNewUser = "/Dropbox/src/Server/".concat(newDir);
			//System.out.println(pathForCurrentUser+"\n"+pathForNewUser);
			String fileName = inFromClient.readLine();
			//System.out.println("file Name is: "+fileName);
			File source = new File(pathForCurrentUser+"/"+fileName);
			File dest = new File(pathForNewUser+"/"+fileName);
			dest.createNewFile();
			try {
			    Files.copy(source.toPath(),dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
			    e.printStackTrace();
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Download file from server side to client side directory of the user
	public void downloadFiles(String dirName){
		
		try{
			String fileName = inFromClient.readLine();

			String pathForCurrentUser = "/Dropbox/src/Server/".concat(dirName);
			String pathForNewUser = "/Dropbox/src/Client/".concat(dirName);
			//System.out.println(pathForCurrentUser+"\n"+pathForNewUser);
			//System.out.println("file Name is: "+fileName);
			File source = new File(pathForCurrentUser+"/"+fileName);
			File dest = new File(pathForNewUser+"/"+fileName);
			dest.createNewFile();
			try {
			    Files.copy(source.toPath(),dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
			    e.printStackTrace();
			}
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("Sorry something went wrong while downloading");
			}
	}
	
	//Upload files from home directory to server folder of the user
	public void uploadFile(String dirName){
		//System.out.println("Inside upload()");
		
		try{
			
		String pathForCurrentUser = "/Dropbox/src/Client/".concat(dirName);
		String pathForNewUser = "/Dropbox/src/Server/".concat(dirName);
		//System.out.println(pathForCurrentUser+"\n"+pathForNewUser);
		String fileName = inFromClient.readLine();
		//System.out.println("file Name is: "+fileName);
		File source = new File(pathForCurrentUser+"/"+fileName);
		File dest = new File(pathForNewUser+"/"+fileName);
		dest.createNewFile();
		try {
		    Files.copy(source.toPath(),dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Sorry something went wrong while uploading");
		}
	}

	//Show Files to user
	public void showFiles(String dirName){
		 File path = new File("/Dropbox/src/Server/".concat(dirName));
		 String fileList ="";
		    File [] files = path.listFiles();
		    int length = files.length;
		    String slength = Integer.toString(length);
		    //System.out.println("slength is: "+slength);
		    try {
		    	//System.out.println("Sent slength: "+slength+"\n");
				outToClient.writeBytes(slength.concat("\n"));
				outToClient.flush();
			} catch (IOException e1) {
				
				System.out.println("Opps Exception");
				
				e1.printStackTrace();
			}

		    for (int i = 0; i < files.length; i++){
		        if (files[i].isFile()){ //this line weeds out other directories/folders
		            //System.out.println(files[i].getName());
		            try {
						outToClient.writeBytes(files[i].getName()+"\n");
						outToClient.flush();
					} catch (IOException e) {
						
						System.out.println("Exception is happening here");
						e.printStackTrace();
					}

		        }
		    }
		  // System.out.println("File names sent");

	}
	
	//Registering New User
	public void NewReg(){
		Scanner scan = new Scanner(System.in);
		//System.out.println("New Registration");
		try{
			String receiveLine = inFromClient.readLine();
			//System.out.println("CHechpoint 2");
			//System.out.println("Server ree");
			if(hashTable.contains(receiveLine)){
				outToClient.writeBytes("false\n");
				outToClient.flush();
				//System.out.println("Server responded for email");
			}
			else{
				outToClient.writeBytes("true\n");
				outToClient.flush();
				//System.out.println("Ooops...!!");
			}
			String receiveLine1="";
			receiveLine1 = inFromClient.readLine();
			hashTable.put(receiveLine, receiveLine1);
			outToClient.writeBytes("true\n");
			outToClient.flush();
			String spath = "";
			spath = spath.concat(receiveLine);
			String cpath = "";
			cpath = cpath.concat(receiveLine);
			File SDir = new File(spath);
			File CDir = new File(cpath);
			if (!SDir.exists()) {
			   // System.out.println("creating directory: " + receiveLine);
			    boolean result = false;

			    try{
			    	System.out.println("Making directory");
			        SDir.mkdir();
			        result = true;
			    }
			    catch(SecurityException se){
			        System.out.println("Sorry something went wrong creating dir in server");
			    }
			    if(result) {
			        System.out.println("DIR created in Server");
			    }
			}
			if (!CDir.exists()) {
			    System.out.println("creating directory: " + receiveLine);
			    boolean result = false;

			    try{
			        CDir.mkdir();
			        result = true;
			    }
			    catch(SecurityException se){
			        System.out.println("Sorry something went wrong creating dir in client");
			    }
			    if(result) {
			        System.out.println("DIR created in Client");
			    }
			}

			//System.out.println("Server responded for final one");
		}
		catch(IOException e){
			System.out.println("IO Exception");
		}
		
	}
}
