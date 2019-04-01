package MiniProjectv2;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientThread extends Thread {						//The thread inherits the properties of the 'Thread' class.

	Socket clientSocket;
	ArrayList<ClientThread> clientThreads;						//The socket and thread list that are passed to the ClientThread and made
	BufferedReader br = null;									//instance variables here to be referenced outside of the constructor.
	PrintWriter pw = null;
																//The reader and writer objects are declared here to be used across several methods.
	
	ClientThread(Socket s, ArrayList<ClientThread> ct) {
		clientSocket = s;										//The constructor takes the socket and thread list and assigns them to instance variables.
		clientThreads = ct;
	}
	
	public void run() {											//This method is run when the threads are started when the game is started in
																//the server class.
		try {
			br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));	
			pw = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			pw.write("START " + clientThreads.size() + "\n");
			pw.flush();											//The reader and writer objects are initialised here and the start message is passed
																//to the thread's socket (belonging to a client).
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void clientRollMessage() throws IOException {
		
		pw = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		pw.write("ROLL\n");										//This message is for when the server needs to tell a client it is their turn to roll.
		pw.flush();
	}
	
	public void sendRollValueMessage(int playerRoll) throws IOException {
		
		pw = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		pw.write(playerRoll + " ROLL NUMBER " + "\n");			//This message is for when the client in question needs to update the value of their 
		pw.flush();												//roll on their 'roll dice' window.
	}
	
	public void sendBoardUpdateMessage(int playerNum, int newPosition) throws IOException {
		pw = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		pw.write("UPDATE " + playerNum + " to " + newPosition + "\n");
		pw.flush();												//This message is for when all the clients need to update the new position of a player 
	}															//who has just rolled.
	
	public void sendGameOverMessage(int playerNum) throws IOException {
		pw = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		pw.write("GAME OVER " + playerNum +"\n");				//This message is sent to all clients when it is determined that the client in
		pw.flush();												//question has won the game and to stop all other operations.
	}
	
}
