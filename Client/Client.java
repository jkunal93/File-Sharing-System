// This is Client file for a dropbox-prototype program
package Client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;



public class Client{
	public static Socket connectionSocket= null;
	public static DataOutputStream outToServer = null;
	public static BufferedReader inFromServer = null;
	public static OutputStream outToServerByte = null;
	public static Scanner KeyboardInput = new Scanner(System.in);

	public static void main(String [] args){
		try {
			connectionSocket = new Socket("localhost",9876);
			outToServer = new DataOutputStream(connectionSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int inputFromUser;
		do{
			System.out.println("\n----------Welcome----------\n\nWhat would you like to do:\n1 Signup\n2 Login\n3 Exit");
			inputFromUser = KeyboardInput.nextInt();
			switch(inputFromUser){
				case 1: regNewAccount();
					break;
				case 2: logIntoAccount();
					break;
				case 3:printExit();
				try {
					KeyboardInput.close();
					outToServer.close();
					//outToServerByte.close();
					inFromServer.close();
					connectionSocket.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
					break;
				default:System.out.println("Wrong Entry Try Again");
					break;
			}
		}while(inputFromUser!=3);
	}
	
	// Creating new account for the user 
	public static void regNewAccount(){
		int choice;
		boolean isTerminate = true;
		String email="",pass="";
		try {
			outToServer.writeBytes("regNew()\n");
			outToServer.flush();
			System.out.println("email:");
			email = KeyboardInput.next();
			outToServer.writeBytes(email+"\n");
			outToServer.flush();
			String bool=inFromServer.readLine();
			if(bool.equals("false")){
				System.out.println("Email Already Exist try with different email");
				return;
			}
			else{
				System.out.println("Password for "+email+":");
				pass = KeyboardInput.next();
				outToServer.writeBytes(pass+"\n");
				outToServer.flush();
				String bool1=inFromServer.readLine();
				if(bool1.equals("true")){
					System.out.println("Registration successfull...!!!\n\n\n");
				}
				else{
					System.out.println("Something went wrong try again...");
					return;
				}
			}
			}catch(IOException e){
				e.printStackTrace();
			}
	}



	// Logging into an existing account
	public static void logIntoAccount(){
		
		boolean isTerminate = true;
		String email="",Password="";
		try {
			outToServer.writeBytes("login()\n");
			outToServer.flush();
			System.out.println("email:");
			email = KeyboardInput.next();
			
			outToServer.writeBytes(email+"\n");
			outToServer.flush();
			String bool=inFromServer.readLine();
			//System.out.println("Check");
			if(bool.equals("true")){
				System.out.println("Password for "+email+":");
				Password = KeyboardInput.next();
				outToServer.writeBytes(Password+"\n");
				outToServer.flush(); 
				
			//	System.out.println("Client has sent password");
				String bool1=inFromServer.readLine();
				//System.out.println("bool is " +bool1);
				if(bool1.equals("true")){
					System.out.println("Login successfull...!!!\n\n");
					drive(email);
				}
				else{
					System.out.println("Incorrect email or password. Try again..!!");
					return;
				}
			}
			else{
				System.out.println("Sorry....!! E-mail doesn't exist");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	// Options available once user is logged in
	public static void drive(String email) throws IOException
	{
		int choice;
		do{
			System.out.println("\n----------Menu----------");
			System.out.println("\n1 Browse Files\n2 Upload file to My drive\n3 Download file\n4 Sync Files\n5 Delete File\n6 Share File \n7 Logout");
			choice = KeyboardInput.nextInt();
			switch(choice){
				case 1:
					seeFiles();
					break;
				case 2:
					uploadFiles();
					break;
				case 3:downloadFile(email);
					break;
				case 4: sync(email);
					break;
				case 5: deleteFile();
					break;
				case 6: shareFile();
					break;
				case 7:
					logout();
					break;
			}
		}while(choice!=7);
	}
	
	// Synchronization between server and client folders of the user
	public static void sync(String dirName){
		String fName="";
		try {
			outToServer.writeBytes("sync()\n");
			outToServer.flush();
			String sloop = inFromServer.readLine();
			int loop = Integer.parseInt(sloop);
			//System.out.println("Length is: "+loop);
			ArrayList<String> fileNames = new ArrayList<>();
			ArrayList<String> fileNames1 = new ArrayList<>();
			File path = new File("/Dropbox/src/Client/".concat(dirName));
			String temp ="";
			File [] files = path.listFiles();
			int length = files.length;
			for(int i=0;i<length;i++){
				temp = files[i].getName();
				fileNames.add(temp);
			}
			//System.out.println("Files on client side:\n");
			//for(int i=0;i<fileNames.size();i++){
			//	System.out.println(i+". "+fileNames.get(i));
		//	}
			while(loop>0){
				fName = inFromServer.readLine();
				fileNames1.add(fName);
				loop--;
			}
			//System.out.println("Files on Server Side");
			//for(int i=0;i<fileNames1.size();i++){
		//		System.out.println(i+". "+fileNames1.get(i));
		//	}
			
			
			String pathForCurrentUser = "/Dropbox/src/Client/".concat(dirName);
			String pathForNewUser = "/Dropbox/src/Server/".concat(dirName);
			//System.out.println(pathForCurrentUser+"\n"+pathForNewUser);
			//String fileName = inFromClient.readLine();
			for(int i =0;i<fileNames.size();i++){
				//for(int j=0;j<fileNames1.size();j++){
					//if(fileNames1.contains())
						//System.out.println("file Name is: "+fileNames.get(i));
						File source = new File(pathForCurrentUser+"/"+fileNames.get(i));
						File dest = new File(pathForNewUser+"/"+fileNames.get(i));
						dest.createNewFile();
						try {
							Files.copy(source.toPath(),dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							e.printStackTrace();
						}
				//}
			}
			
			for(int i =0;i<fileNames1.size();i++){
						File dest = new File(pathForCurrentUser+"\\"+fileNames1.get(i));
						File source = new File(pathForNewUser+"\\"+fileNames1.get(i));
						dest.createNewFile();
						try {
							Files.copy(source.toPath(),dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							e.printStackTrace();
						}
				//}
			}
			
			
			System.out.println("Synchronization Completed....!!");
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Download files from server to client folder of the user 
	public static void downloadFile(String dirName){
		Scanner Input = new Scanner(System.in);
		String fileName = null;
		//byte[] Bytes = new byte[100];
		try {
			outToServer.writeBytes("download()\n");
			outToServer.flush();
			System.out.println("Enter file name you want to download: ");
			fileName = Input.next();
			outToServer.writeBytes(fileName.concat("\n"));
			outToServer.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Shows the files in the directory of user
	public static void seeFiles(){
		try{
			outToServer.writeBytes("seeFiles()\n");
			outToServer.flush();
			System.out.println("Files in your drive:");
			String slength = null;
			slength = inFromServer.readLine();
			int length = Integer.parseInt(slength);
			ArrayList<String> fileNames = new ArrayList<>();
			while(length>0){
				fileNames.add(inFromServer.readLine());
				length--;
			}
			for(int i =0;i<fileNames.size();i++){
				System.out.println((i+1)+" "+fileNames.get(i));
			}
			System.out.println("\n");
		}catch(IOException e){
			
			System.out.println("Client side: ");
			e.printStackTrace();
			System.out.println("MayDay...Mayday...MayDay...!!");
		}
	}

	// Uploading files from home directory to server folder of the user
	public static void uploadFiles(){
		byte[] bytes = new byte[100];
		Scanner KeyboardInput = new Scanner(System.in);
		OutputStream outToServerByte = null;
		try{
			outToServerByte = connectionSocket.getOutputStream();
			outToServer.writeBytes("uploadFiles()\n");
			outToServer.flush();
			//System.out.println("Server sent upload files files");
			System.out.println("Enter the name of file you want to upload");
			String fileName = KeyboardInput.nextLine();
			outToServer.writeBytes(fileName+"\n");
			outToServer.flush();
		
			outToServer.flush();
			System.out.println("File uploaded");
		}catch(IOException e){
			System.out.println("Error in upload......!!");
		}
	}

	//Deleting file from server folder of the user
	public static void deleteFile(){
		Scanner Input = new Scanner(System.in);
		try {
			outToServer.writeBytes("deleteFiles()\n");
			outToServer.flush();
			System.out.println("Enter Name of file you want to delete:");
			String fileName = Input.next();
			outToServer.writeBytes(fileName+"\n");
			outToServer.flush();
			System.out.println("File Deleted...!!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void shareFile(){
		Scanner Input = new Scanner(System.in);
		System.out.println("Inside File sharing");
		try {
			outToServer.writeBytes("fileShare()\n");
			outToServer.flush();
			System.out.println("Share with:");
			String userName = Input.next();
			outToServer.writeBytes(userName+"\n");
			outToServer.flush();
			System.out.println("Enter File you want to share");
			String fileName = Input.next();
			outToServer.writeBytes(fileName+"\n");
			outToServer.flush();
			
			System.out.println("File Shared with "+userName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	public static void printExit(){
		try {
			outToServer.writeBytes("exit\n");
			outToServer.flush();
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		System.out.println("Thanks for visiting Dropbox\n\n");
	}

	//for logging out
	public static void logout(){
		String response = null;
		try {
			outToServer.writeBytes("logout()\n");
			outToServer.flush();
			response = inFromServer.readLine();
			if(response.equals("true")){
				System.out.println("Logout successfull...!!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
