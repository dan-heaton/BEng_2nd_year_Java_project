package MiniProjectv2;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Client implements ActionListener {

	SnakesAndLaddersGUI gui = new SnakesAndLaddersGUI();
	static String[] turnWindowStrings = {"Waiting...", "Your turn!"};
	static boolean hasConnected = false;						//We create a GUI for use in the board's picture for the specific client, as well
	JTextField text;											//as two strings to select from for the turn window's label.
	JLabel stringLabel, rollLabel;								
	JButton rollButton;											//The features of the client's turn window are all made instance variables so they
																//can be accessed outside of the methods they were created in (in 'main' for the two
																//labels and in 'actionPerformed' for the button and text field).
	static BufferedReader br;
	static PrintWriter pw;										//The reader and writer objects are initialised here to be used in several methods.
	
	JFrame turnFrame = new JFrame();
	JFrame connectFrame = new JFrame();							//The frames for the various windows the client will see are defined here (with the
	JFrame boardFrame = new JFrame();							//exception of the "Starting game" and "Game over" windows").
	
	
	public static void main(String[] args) throws Exception {
		Client c = new Client();
		while (true) {											//This keeps the game running until the game over window appears and the user presses
			Thread.sleep(1000);									//the 'Quit Game' button, which calls 'System.exit(0)'.
			
			if (hasConnected == true) {							//The game keeps checking if it has connected (which sets 'hasConnected' to true when
																//a client socket has been made).						
				String message = br.readLine();
				c.clientInitialise(message);					//When it's connected, it waits for the startup message from the server, which it
				Thread.sleep(1000);								//reads as 'message', and calls the 'clientInitialise' message, which interprets from
				c.startGameWindow();							//the message how many players are playing (and sets the number of players in the
				Thread.sleep(1000);								//gui object). We then create the startup window showing a "Starting
																//the Game!" message.
				boolean playerWonGame = false;
				while (!playerWonGame) {						//This is the main 'while' loop that will be running while the programme is running,
					String message2 = br.readLine();				//until someone has won the game.
					
					if (message2.equals("ROLL")) {
						c.rollButton.setEnabled(true);			//We wait for another message from the server and take it in as 'message'.
						c.stringLabel.setText(turnWindowStrings[1]);
						c.rollLabel.setText("0");				//If it's "ROLL", it means it is the client's turn to roll and so the roll button
					}											//is enabled so the client can submit a roll, as well as the roll label reset to 0
					else if (message2.contains("UPDATE")) {		//and the label on the turn window now saying "Your Turn!".
						int updatePlayerNum = Integer.parseInt(message2.substring(7,8));
						int updateNewPosition = Integer.parseInt(message2.substring(12));
						c.gui.setPosition(updatePlayerNum, updateNewPosition);
						continue;								//If the message instead contains "UPDATE", we get the player's number and new
					}											//position from the message and update the client's GUI based on this.
					else if (message2.contains("GAME OVER")) {
						int winnerNum = Integer.parseInt(message2.substring(10)) + 1;
						c.gameOverWindow(winnerNum);			//If the message contains "GAME OVER", we get the winning player's number from the
						continue;								//message and create a "Game Over!" window displaying which player won.
					}
																//The two 'continue's are inserted because, if the player receives an update or
																//game over message, they will be awaiting either another roll, update, or game over
																//message instead of waiting for an update roll value message as seen below.
					message2 = br.readLine();					
					if (message2.contains("ROLL NUMBER ")) {
						String diceVal = message2.substring(0, 1);
						c.rollLabel.setText(diceVal);		
						c.stringLabel.setText(turnWindowStrings[0]);
					}											//After the player receives a "ROLL" message, it waits on the 'readLine()' line
																//until the server rolls for the player and returns with the value of the roll
				}												//for the person to update their turn window with.
				
			}
		}
	}

	public Client() throws IOException {
		
		createConnectWindow();									//When the client object is created at the beginning of 'main', we create the
		createBoardDisplay();									//connect window and board display immediately.
	}

	
	public void createConnectWindow() {							//The connect window is created immediately on Client creation to give the user
																//a place to specify where they wish to connect to.
		connectFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		connectFrame.setSize(300, 300);							//Here we set the size, close operation (exit the client if connection window is closed),
		connectFrame.setTitle("Server Connection");				//window title, and place it to the right of the screen on startup.
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        connectFrame.setLocation(3*dim.width/4-connectFrame.getSize().width/2, dim.height/2-connectFrame.getSize().height/2);
        
        Container clientContainer = connectFrame.getContentPane();
        clientContainer.setLayout(new GridLayout(3, 1));		//The container for the components is made here, with it being 3x1 to account for
        														//three components ("Server Address:" label, text window, and "Connect!" button.
        JLabel serverLabel = new JLabel("Server Address:");
        serverLabel.setFont(new Font("Verdana", 3, 30));		//The "Server Address:" label is made here, along with the font, its size, and colour.
		serverLabel.setForeground(Color.BLUE);
		
		text = new JTextField(30);
		text.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		text.setFont(new Font("Verdana", 3, 30));				//The text box is next made (with object scope rather than local scope as it will be used
																//in another method) and set with a thin border and a neutral font and text size.
		JButton button = new JButton("Connect!");
		button.addActionListener(this);						
		button.setActionCommand("CONNECT BUTTON");				//The "Connect!" button is created here, set to associate with the 'actionPerformed'
																//method, and given an action command to be referenced in the 'actionPerformed' method.
		clientContainer.add(serverLabel);
		clientContainer.add(text);								
		clientContainer.add(button);							//We add the label, text box, and button to the container, which itself is in the
																//connect window frame, and make the frame visible to the user to show the whole window.
		connectFrame.show();
		
		
	}

	public void createBoardDisplay() {
		
		boardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		boardFrame.setSize(750, 750);
		boardFrame.setTitle("Snakes and Ladders");
		Container gameContainer = boardFrame.getContentPane();
		gameContainer.add(gui);									//The board display is created here from the 'SnakesAndLaddersGUI' object we defined
		boardFrame.show();										//at instance scope. The frame is created, set the 'close Client' operation on closure,
																//set the size, the title, created a container to hold the GUI object, and made visible.
	}
	
	
public void createTurnWindow() {								//The turn window will remain visible for the client while the game is ongoing, and will
																//mention to the user if it's their turn or not and, if it is, let them roll the dice.
		turnFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		turnFrame.setSize(300, 200);							//The turn frame is set here to close the client on closer, with a small size set for it
		turnFrame.setTitle("Turn Window");						//(to not take up more room than the visible board), set with a title, and placed in the middle.
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        turnFrame.setLocation(dim.width/2-turnFrame.getSize().width/2, dim.height/2-turnFrame.getSize().height/2);
        
        Container clientContainer = turnFrame.getContentPane();
        clientContainer.setLayout(new GridLayout(3, 1));		//The content pane will be 3x1 to take a status message ("Waiting..." or "Your Turn!"),
        														//a label showing the most recent roll (or '0' if it's waiting for the client to roll),													
        stringLabel = new JLabel("\t\t" + turnWindowStrings[0]);	//and a "Roll the dice!" button which is enabled when it is the client's turn
        stringLabel.setFont(new Font("Verdana", 3, 20));
		stringLabel.setForeground(Color.BLUE);					//The label showing the status message starts with the "Waiting" message from
																//'turnWindowStrings' and is set with a font, size, and colour.
		rollLabel = new JLabel(Integer.toString(0));
		rollLabel.setFont(new Font("Verdana", 3, 30));			//The roll label is initialised to '0' and set a font, size, and colour as well.
		rollLabel.setForeground(Color.RED);
		
		rollButton = new JButton("Roll the dice!");
		rollButton.addActionListener(this);						//The "Roll the dice!" button is created, associated with the 'actionPerformed' method,
		rollButton.setActionCommand("ROLL BUTTON");				//given an action command to be referenced in the 'actionPerformed' method, and set
		rollButton.setEnabled(false);							//to be disabled at creation (i.e. grayed out button).
		
		clientContainer.add(stringLabel);
		clientContainer.add(rollLabel);							//The two labels and one button are added to the container which is within the frame,
		clientContainer.add(rollButton);						//and the frame is made visible to show the turn window when it is created.
		
		turnFrame.show();
	}
	
	public void connect(String address) {						//This message is called if the connect button is pressed on the connect window.
																//The address for the server is taken from the text box of the connect window and 
		try {													//calls the 'connect' method with this as its argument.
			Socket clientSocket = new Socket(address, 4921);
			hasConnected = true;								
			br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			pw = new PrintWriter(clientSocket.getOutputStream(), true);
			connectFrame.hide();								//If the client is able to make a connection based on this address, it creates the
			createTurnWindow();									//socket, sets 'hasConnected' to be true (so it moves into the startup phase in 'main'),
		}														//and initialises the reader and writer objects based on this newly created socket.
		catch(Exception e) {									//The connect frame is then hidden (as we have no further use for it) and we create
			System.out.println("INVALID ADDRESS");				//the turn window for the client to roll with.
		}
	}															//If an exception is thrown (i.e. can't find a server to connect based on the given
																//address), it prints an error message on the console.
	
	public void clientInitialise(String message) {
		
		if (message.equals("START 2"))
			gui.setNumberOfPlayers(2);
		else if (message.equals("START 3"))						//This is called when the client first receives a message from the server saying
			gui.setNumberOfPlayers(3);							//it is starting and how many players are playing. It interprets the server's message
		else if (message.equals("START 4"))						//and sets its GUI to the correct number of players (i.e. with the right number
			gui.setNumberOfPlayers(4);							//of counters on the board).
	
	}
	
	public void startGameWindow() throws InterruptedException {
																//This is created when the server tells the clients the game is started and it only
		JFrame startGameFrame = new JFrame();					//shows itself for 2 seconds before closing itself.
		startGameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startGameFrame.setSize(600, 300);						//The frame is created, with the exit on close operation, the size is set, and placed
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();	//in the middle of the screen.
        startGameFrame.setLocation(dim.width/2-startGameFrame.getSize().width/2, dim.height/2-startGameFrame.getSize().height/2);
        
        Container gameOverContainer = startGameFrame.getContentPane();
        gameOverContainer.setLayout(new GridLayout(1, 1));		//The container is created and set to be 1x1 (as it's only holding the one message).
        
		JLabel startGameText = new JLabel("Starting the game!");
		startGameText.setFont(new Font("Verdana", 3, 50));		//The message is set to be "Starting the game!", with the font set, the size set to
		startGameText.setForeground(Color.RED);					//a large size, with a red colour.
		
		gameOverContainer.add(startGameText);
		
		startGameFrame.show();									//This label is added to the container, which shows up on the frame. The frame is then
		Thread.sleep(2000);										//made visible, waits for 2 seconds, and hidden again.
		startGameFrame.hide();
		
	}
	
	
	public void gameOverWindow(int winnerNum) {					//This window displays when a client (either this one or another one connected to the
																//server) has won the game (i.e. reached square 100)
		JFrame gameOverFrame = new JFrame();
		gameOverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameOverFrame.setSize(500, 400);						
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();	
        gameOverFrame.setLocation(dim.width/2-gameOverFrame.getSize().width/2, dim.height/2-gameOverFrame.getSize().height/2);
        Container gameOverContainer = gameOverFrame.getContentPane();
        gameOverContainer.setLayout(new GridLayout(3, 1));		//The frame is created locally (as it isn't needed outside this method), set the close 
        														//operation, set a large size, and placed in the middle of the screen.
		JLabel gameOverText = new JLabel("Game Over!");			//The container is set to 3x1, as two labels and one button will be placed within it.
		gameOverText.setFont(new Font("Verdana", 3, 50));		//The "Game Over!" label is created, with the font set, a large size selected, and
		gameOverText.setForeground(Color.RED);					//coloured red.
		
		JLabel winnerNumText = new JLabel("Player " + winnerNum + " wins!");
		winnerNumText.setFont(new Font("Verdana", 3, 50));		//The next message will display which player won based on the argument passed to the
		winnerNumText.setForeground(Color.RED);					//game over window method (note: player numbers will start at '1' for the purposes
																//of displaying them even though in the game's logic it starts at '0').
		JButton closeButton = new JButton("Close game");
		closeButton.addActionListener(this);					//The button to close the game is created, associated with the 'actionPerformed' method,
		closeButton.setActionCommand("CLOSE BUTTON");			//and given an action command to be referenced in the 'actionPerformed' method.
		
		gameOverContainer.add(gameOverText);
		gameOverContainer.add(winnerNumText);					//The two labels and one button are added to the container within the game over frame 
		gameOverContainer.add(closeButton);						//and the whole frame is made visible to the user immediately upon creation.
		
		gameOverFrame.show();
		
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {				//This method interprets the three possible buttons that the user can press at certain 
																//points in the programme				
		if (e.getActionCommand().equals("ROLL BUTTON")) {
			rollButton.setEnabled(false);						//If the roll button is pressed in the turn window, it is immediately disabled (so 
			pw.write("ROLLING\n");								//the user can't accidently press it again) and sends a message to the server saying 
			pw.flush();											//the client wishes to roll the dice.
		}
		if (e.getActionCommand().equals("CONNECT BUTTON")) {
			String serverAddress = text.getText();				//If the connect button is pressed in the connect window, we get the text from the 
			connect(serverAddress);								//text box, and pass it as the target server's address to the 'connect' method.
		}
		if (e.getActionCommand().equals("CLOSE BUTTON")) {
			System.exit(0);										//If the close button is pressed in the game over window, we call 'System.exit(0)'
		}														//to close all of the client windows and exit the whole programme.
		
	}
}
