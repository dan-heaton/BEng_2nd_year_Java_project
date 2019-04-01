package MiniProjectv2;

import java.awt.*;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Board {											//There is only one board made and it is in the server class 
																//which is used to determine the current positions of players, roll the dice for 
	int currentClients;											//them, and determine where they should end up.
	ArrayList<Integer> playerPositions = new ArrayList<>();
															
	public Board(int currentClients) {							//The constructor takes the nunber of clients playing the game and assigns it to 
		this.currentClients = currentClients;					//the instance variable 'currentClients'. We also have an ArrayList containing
		for (int i = 0; i < currentClients; i++)				//all the players' positions and initialises 'currentClients' entries to '0'.
			playerPositions.add(0);			
	}
	
	
	public int rollDice() {										//This rolls the dice for a player using the 'random' function and adding '1' to
																//the result to get a result between 1 and 6.
		int roll = (int)(Math.random()*6 + 1);
		return roll;
	}
	

	public int assessPosition(int playerNum, int roll) throws InterruptedException {
																//This method looks at the player's current position and roll and determines
																//if it landed on a snake or ladder and where it should go.
		int[] startPositions = {1, 6, 11, 14, 21, 24, 31, 35, 44, 51,
				56, 62, 64, 73, 78, 84, 91, 95, 98};
		int[] endPositions = {38, 16, 49, 4, 60, 87, 9, 54, 26, 67,
				53, 19, 42, 92, 100, 28, 71, 75, 80};			//The positions of the starts of the snakes and ladders are given in the 'startPositions'
																//array, while the ending positions are given in the 'endPositions' array. They are
		int position = playerPositions.get(playerNum) + roll;	//mapped on a 1-to-1 basis, i.e. the 5th number in the 'startPositions' array equates 
		int temp = 0;											//to the 5th number in the 'endPositions' array.
		for (int i = 0; i < startPositions.length; i++) {
			if (position == startPositions[i])					//We get the player in quesion's position, adds its roll value to its position to get
				temp = i + 1;									//'position' then see if this value occurs in 'startPositions'. If it does, it means it
		}														//has landed on a snake or ladder and so we change 'temp' to a non-zero value based on
																//where it found the number in the 'startPositions' array.
		int newPosition = position;
		if (temp != 0) 											//If it has landed on a snake or ladder (i.e. temp != 0), then we get the relevenat value
			newPosition = endPositions[temp - 1];				//from the 'endPositions' array and assign it to the 'newPosition' variable. Otherwise,
																//the 'newPosition' variable gets the original 'position's value.
		if (newPosition > 100)						
			newPosition -= roll;								//If the roll takes the piece over 100, move it back to where it was (i.e. if the 
																//piece was on '97', rolls a '5' to take it to '102', we take it back '5' to put it on '97'.
		playerPositions.set(playerNum, newPosition);
		return newPosition;										//The player's position is updated in the ArrayList and the new position of the 
	}															//player is returned.
	
	
	public boolean playerHasWon(int playerNum) {
		
		int position = playerPositions.get(playerNum);			//This method takes in the player's number, uses that to look up the player in the 
		if (position == 100)									//array and, if it is on square 100, it returns 'true' to say it has won;
			return true;										//otherwise, it returns false.
		return false;
		
	}
}
