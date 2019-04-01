package MiniProjectv2;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class Server implements ActionListener {
	
	int currentClients = 0, numOfAllowedClients = 0;			//'currentClients' stores current connected clients (players to be playing game) and
	ArrayList<ClientThread> clientThreads = new ArrayList<>();	//'numOfAllowedClients' stores the max number selected by user (in the numClientsWindow).
	ArrayList<Socket> clientSockets = new ArrayList<>();		
	JFrame serverFrame = new JFrame();							//The two ArrayList stores threads and sockets for clients that have connected as they 
	JFrame numClientsFrame = new JFrame();						//connect. We also declare all UI components as instance attributes rather than local 
	JLabel currentClientsLabel, numOfAllowedClientsLabel;		//variables, as they are accessed outside of the method they are created in.
	JRadioButton twoButton, threeButton, fourButton;
	JButton startGameButton;
	
	public static void main(String[] args) throws Exception {	//When the server is launched, all that 'main' does is call the constructor, which
		new Server();											//creates the windows, handles connections, and manages the game code.
	}
	
	
	public Server() throws IOException, InterruptedException {
		
		ServerSocket serverSocket = new ServerSocket(4921);		//When the Server object is created, we create a server socket on port 4921,
		boolean serverAccepting = true;							//set the server accepting state to true, and create a window where the user
		createNumClientsWindow();								//accessing the server can specify how many clients they wish to allow to connect.
		
		while(serverAccepting) {
			Socket clientSocket = serverSocket.accept();		//While the server is accepting new clients, it waits for a client to connect,
			clientSockets.add(clientSocket);					//accepts this as a socket, and adds it to the list of client sockets.
			BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
			ClientThread ct = new ClientThread(clientSocket, clientThreads);		//We next define the reader and writers for the streams, create
			clientThreads.add(ct);													//a new thread from the newly created socket, and add it to
			currentClientsLabel.setText(Integer.toString(++currentClients) + " connected");		//the list of threads.
			numOfAllowedClientsLabel.setText(Integer.toString(--numOfAllowedClients) + " connections available");
			if (numOfAllowedClients == 0) serverAccepting = false;		
		}														//We then update the two labels in the server window to reflect the new client
		startGameButton.setEnabled(true);						//connecting and check if the server has reached the maximum number of connections
	}															//allowed. If it has, we exit the 'while' loop and enable the 'Start Game!' button
																//to start the game running.
	
	
			
	public void playGame() throws Exception {					//Starts here when the 'Start the Game!' button is pressed on server window and is
																//responsible for most of the running of the game in the server.
		startGameButton.setEnabled(false);
		for (ClientThread ct : clientThreads) {					//Start the client threads, which call their respective 'run' methods defined in
			ct.start();											//the ClientThread class.
		}
		Thread.sleep(4000);										//We wait 4 seconds while the initial 'Starting Game' window appears and disappears
		Board serverBoard = new Board(currentClients);			//and then create a server board, the size of which is determined by the number of
																//clients connected, which the server will refer to to determine how to update the
																//clients' UIs.
		
		while (true) {											//Stays in infinite loop until someone has won and everyone has disconnected
			for (int i = 0; i < clientThreads.size(); i++) {	//Plays a whole round of the game ('i' is the player who's turn it is)
			
				int newPosition = 0;							//The 'newPosition' variable is declared and initialised here to be user later on.
				clientThreads.get(i).clientRollMessage();		//Tells the player to roll and makes it able to.
				BufferedReader br2 = new BufferedReader(new InputStreamReader(clientSockets.get(i).getInputStream()));
				String rollMessage = br2.readLine();			//For the player who's turn it is, we create a reader object and for the message
																//to say that it is rolling.
				if (rollMessage.equals("ROLLING")) {			//If player has pressed the roll button, have the board roll the dice, assess
					int playerRoll = serverBoard.rollDice();	//if the player has landed on a special square (snake or ladder), and return their
					newPosition = serverBoard.assessPosition(i, playerRoll);	//analysed position.
					clientThreads.get(i).sendRollValueMessage(playerRoll);
				}												//We then tell the client to update their most recent value of their roll
																//on their roll window.
				for (int j = 0; j < clientThreads.size(); j++) {	
					Thread.sleep(100);							
					clientThreads.get(j).sendBoardUpdateMessage(i, newPosition);
				}												//Update each client's board with the new position of the current client
				
				
				if (serverBoard.playerHasWon(i)) {				//Checks to see if the player has won before moving onto another player.
					for (int k = 0; k < clientThreads.size(); k++) {
						Thread.sleep(1000);
						clientThreads.get(k).sendGameOverMessage(i);
					}											//If the player in question has won (i.e. reached square '100' without going
				}												//over it, we send a game over message to all clients which prevents them from
			}													//any further interactions with the game and says who has won.
		}
	}
	
	
	
		
	public void createNumClientsWindow() {						//This window first pops up when the server is launched and gets the user to select
																//how many clients they want to allow into the game. It closes after the selection is made.
		numClientsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		numClientsFrame.setSize(400, 250);						//The frame is initialised outside of method, and is set to exit the programme on closing
		numClientsFrame.setTitle("Number of Clients");			//the frame, set the size, set the title, and placed to the right of the screen on startup.
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        numClientsFrame.setLocation(3*dim.width/4-serverFrame.getSize().width/2, dim.height/2-serverFrame.getSize().height/2);
        
        Container clientsContainer = numClientsFrame.getContentPane();
        clientsContainer.setLayout(new GridLayout(5,1));		//The dimensions of the content pane is 5x1, as we place a title, 3 radio buttons,
        														//and a button within it.
        JLabel numClientsLabel = new JLabel("Select number of clients to allow:");
        numClientsLabel.setFont(new Font("Verdana", 3, 20));	//The first label of the number of clients window is set here.
        numClientsLabel.setForeground(Color.BLUE);
        
        twoButton = new JRadioButton("Two");					//The three radio buttons are set here, pointed to the instance variables.
        threeButton = new JRadioButton("Three");
        fourButton = new JRadioButton("Four");
		
        ButtonGroup group = new ButtonGroup();
        group.add(twoButton);
        group.add(threeButton);									//The groups are placed into a group here so that only one can be pressed at a time.
        group.add(fourButton);
        
        JButton enterButton = new JButton("Submit");
        enterButton.setFont(new Font("Verdana", 1, 25));		//The button for the number of clients frame is made here, and associated with the 
        enterButton.addActionListener(this);					//'actionPerformed' method and given an action command identifier to 
        enterButton.setActionCommand("ENTER BUTTON");			//the method ("ENTER BUTTON").

        clientsContainer.add(numClientsLabel);
        clientsContainer.add(twoButton);						//The label, radio buttons, and enter button are added to the container, which is
        clientsContainer.add(threeButton);						//contained within the frame.
        clientsContainer.add(fourButton);
        clientsContainer.add(enterButton);
        
        numClientsFrame.show();									//We then make the frame visible immediately on creation.
	}
	
	
	public void createServerWindow() {							//This window says how many clients are connected, how many spots are left, and when
																//there are no more clients to connect, the 'Start Game!' can be pressed to start the game.
		serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		serverFrame.setSize(300, 250);							//The window's frame is set here to close the programme on the window's close, along with
		serverFrame.setTitle("Server");							//the size, the title, and placed in the bottom left of the screen on startup.
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        serverFrame.setLocation(dim.width/4-serverFrame.getSize().width/2, 3*dim.height/4-serverFrame.getSize().height/2);
        
        Container serverContainer = serverFrame.getContentPane();
        serverContainer.setLayout(new GridLayout(4, 1));		//The content pane is made to be 4x1 as it will contain 3 labels and 1 button.
        
        JLabel serverLabel = new JLabel("Server online");
        serverLabel.setFont(new Font("Verdana", 3, 20));		//The main status message of the window is displayed in blue to make it stand out
		serverLabel.setForeground(Color.BLUE);					//against the other messages.
		
		currentClientsLabel = new JLabel(Integer.toString(currentClients) + " connected");
		currentClientsLabel.setFont(new Font("Verdana", 3, 20));	//The instance message 'currentClients' is used to make a red label showing how many
		currentClientsLabel.setForeground(Color.red);				//players are connected to the server.
																	
		numOfAllowedClientsLabel = new JLabel(Integer.toString(numOfAllowedClients) + " connections available");
		numOfAllowedClientsLabel.setFont(new Font("Verdana", 3, 20));
		numOfAllowedClientsLabel.setForeground(Color.red);			//The instance message 'numOfAllowedClients' is used to make a red label showing 
																	//how many more players can connect to the server.
		startGameButton = new JButton("Start the game!");
		startGameButton.addActionListener(this);
		startGameButton.setActionCommand("START GAME BUTTON");	//The button is for when it is time to start the game. It is grayed out (due to
		startGameButton.setEnabled(false);						//'setEnabled(false)') by default and only enabled when there are no more clients
																//to connect. It is also associated with the 'actionPerformed' method and set
		serverContainer.add(serverLabel);						//an action command identifier ("START GAME BUTTON").
		serverContainer.add(currentClientsLabel);
		serverContainer.add(numOfAllowedClientsLabel);			//The three labels and one button are added to the container which is within the frame.
		serverContainer.add(startGameButton);
		
		serverFrame.show();										//The frame is made visible immediately upon creation of the buttons and labels.
		
	}
	
	public void actionPerformed(ActionEvent e) {				//This method processes any action performed, which extends to the 'Enter' button of
																//the number of clients window and the 'Start the Game!' button of the server window.
		if (e.getActionCommand().equals("ENTER BUTTON")) {
			if (twoButton.isSelected()) {						
				numOfAllowedClients = 2;						//If the 'Enter' button of the number of clients window is pressed, it determines
			}													//which of the radio buttons is set and sets the 'numOfClients' instance variable
			else if (threeButton.isSelected()) {				//based on this.
				numOfAllowedClients = 3;
			}
			else if (fourButton.isSelected()) {
				numOfAllowedClients = 4;
			}
			numClientsFrame.hide();								//On completion, the frame is hidden, as we no longer need it, and the server window
			createServerWindow();								//is created and placed on the screen.
		}
		if (e.getActionCommand().equals("START GAME BUTTON")) {
			try {							
				playGame();										//If the server window's 'Start the Game!' button is selected, we attempt to call the
			} catch (Exception e1) {							//'playGame' method of the server, which handles most of the running of the game.
				e1.printStackTrace();
			}
		}
		
	}
}