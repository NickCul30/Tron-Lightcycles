//Tron.java
//Nicholas Culmone

/* A classic game in which can be played with either one or two players. Each player goes at a certain speed and they leave a trail behind them where ever they have been.
 * The basic way to win is to attempt to "cut off" your opponents path, or to simply outlast them. This must be done whilst avoiding the borders, your own path and the
 * enemy's path. The player also has the option to use a boost (2 per round). This allows them to go double the speed for a short amount of time, and can be a good strategy when trying
 * to either cut off an enemy or escape them. Both players start off with 5 lives, and when one of the player loses all of them the game is declared over, and you are returned
 * to the menu.
 */

/*CONTROLS:
 * Player 1 (Red Cycle) : WASD to control direction ,  SPACE to use boost
 * Player 2 (Blue Cycle) : Arrow Keys to control direction , Right SHIFT to use boost
 * One Player Game : In one player games, either control scheme can be used
 * Menu : WS or UP & DOWN Arrow keys are used to navigate the menu, ENTER is used to select the mode. X toggles on/off the secret seizure mode
 */

/*1 PLAYER MODE:
 * The player uses either WASD or the arrow keys to control the red lightcycle and has to attempt to beat the computer player. The AI attempts to avoid the paths of it's
 * own and that of the player, and also attempts to not hit the walls. If the AI is within a certain distance of the player it has a chance to use a boost to attempt to
 * throw off the player.
 */

/*
 * 2 PLAYAER
 * The same idea as the one player game, two humans play, both starting at five lives and 2 boosts per round, and have to try to beat each other.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*; 
import java.io.*; 
import javax.imageio.*;
import java.util.*;
import javax.swing.Timer;

public class Tron extends JFrame implements ActionListener{
	Timer myTimer;
	GamePanel game;
	public Tron(){
		super("Tron Lightcycles");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(806,628);
		
		myTimer = new Timer(10, this);
		
		game = new GamePanel(this);
		add(game);

		setResizable(false);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent evt){
		
		//When the game is running 
		if(game.getRLose() == false && game.getBLose() == false){
			//2 Player Game
			if(game.getPlayerCount() == 2){
				game.boost();
				game.move();
				game.collide();
			}
			//One Player Game
			else if(game.getPlayerCount() == 1){
				game.collide();
				game.boost();
				game.onePlayerMove();
				
			}
		}
		//When one player has lost all of their lives, it goes to the menu
		else{
			if(game.getRLives() <= -1 || game.getBLives() <= -1){
				game.menu();
			}
			game.lose();
			game.reset();
		}
		game.repaint();
	}
	
	public static void main(String[]args){
		Tron frame = new Tron();
	}
	
	public void start(){
		myTimer.start();
	}
}

class GamePanel extends JPanel implements KeyListener{
	private ArrayList<Image> redCycle = new ArrayList<Image>(); //sprites for the red cycle (only used for displaying the lives, the images were too big for the actual game)
	private ArrayList<Image> blueCycle = new ArrayList<Image>();// ^^
	
	private ArrayList<Point> redPos = new ArrayList<Point>(); //every position the red cycle has ever been in
	private ArrayList<Point> bluePos = new ArrayList<Point>();// ^^
	
	private int rFacing,bFacing; //The direction that each cycle is facing (0: UP, 1: RIGHT, 2: DOWN, 3: LEFT)
	private int bLives = 0,rLives = 0; //the amt of lives each player has (both start on 5)
	
	private int bBoost = 2,rBoost = 2, //how many boosts each player has left in that round
			bBoostCount,rBoostCount, //the timer for when a boost is hit; only boosts for a certain amount of times through the loop
			menuSel = 0, //var for what the player is selecting in the menu
			playerCount; //how many players are in the current game (1 or 2)
	private int redX,redY,blueX,blueY; //the current position of the red & blue cycles
	
	private boolean[]keys;
	
	private boolean rLose = true,bLose = true, //a var that is true when a player has 0 lives
			canLose = true, //so the game isn't reset over and over
			bIsBoost,rIsBoost, //true when a player is boosting
			gameStart,dCanPress,uCanPress,xCanPress,eCanPress,seizure = false; //menu vars
	
	private Image back,explosion,rWinRound,bWinRound,roundTie,rWinGame,bWinGame,gameTie,enter,rBoostIcon,bBoostIcon,menu,rect;
	private Tron mainFrame;
	private Point red,blue; //the current position of a player, taken from (redX,redY) & (blueX,blueY)
	private Random dice = new Random();
	
	public GamePanel(Tron m){
		setVisible(false);
		//Loading images
		//======
		keys = new boolean[KeyEvent.KEY_LAST+1];
		menu = new ImageIcon("menu.png").getImage();
		rect = new ImageIcon("rect.png").getImage();
		back = new ImageIcon("background.png").getImage();
		explosion = new ImageIcon("explosion.png").getImage();
		rWinRound = new ImageIcon("rWinRound.png").getImage();
		bWinRound = new ImageIcon("bWinRound.png").getImage();
		rWinGame = new ImageIcon("rWinGame.png").getImage();
		bWinGame = new ImageIcon("bWinGame.png").getImage();
		roundTie = new ImageIcon("roundTie.png").getImage();
		gameTie = new ImageIcon("gameTie.png").getImage();
		enter = new ImageIcon("enter.png").getImage();
		rBoostIcon = new ImageIcon("rBoost.png").getImage();
		bBoostIcon = new ImageIcon("bBoost.png").getImage();
		for(int i=0;i<4;i++){
			String rName = "red" + i + ".png";
			String bName = "blue" + i + ".png";
			Image rImg = new ImageIcon(rName).getImage();
			Image bImg = new ImageIcon(bName).getImage();
			redCycle.add(rImg);
			blueCycle.add(bImg);
		}
		//======
		
		mainFrame = m;
		addKeyListener(this);
		setVisible(true);
	}
	
	public void addNotify() {
        super.addNotify();
        requestFocus();
        mainFrame.start();
    }
	
	public void keyTyped(KeyEvent e) {	}

    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }
    
    //Move the characters depending on their direction
    //===========
    public void bMove(){
    	//moves the player 3 pixels every time the loop is run (depending on direction)
    	if(bFacing == 0) blueY -= 3;
		else if(bFacing == 1) blueX += 3;
		else if(bFacing == 2) blueY += 3;
		else if(bFacing == 3) blueX -= 3;
    }
    public void rMove(){
    	if(rFacing == 0) redY -= 3;
		else if(rFacing == 1) redX += 3;
		else if(rFacing == 2) redY += 3;
		else if(rFacing == 3) redX -= 3;
    }
    //===========
    
    //Collision
    //===========
    public void rCollide(){
    	blue = new Point(blueX,blueY);
    	red = new Point(redX,redY);
    	
    	//If the players hit each other head-on
    	if(red.equals(blue)){
    		rLose = true;
    		bLose = true;
    	}
    	//When the point collides with one of the lines
    	else if(redPos.contains(red)){
    		rLose = true;
    	}
    	else if(bluePos.contains(red)){
    		rLose = true;
    	}  
    	//Out of bounds detector
    	else if(red.x <= 29 || red.x >= 764 || red.y <= 29 || red.y >=536) rLose = true;
    }
    
    public void bCollide(){
    	blue = new Point(blueX,blueY);
    	red = new Point(redX,redY);
    	
    	//If the players hit each other head-on
    	if(blue.equals(red)){
    		rLose = true;
    		bLose = true;
    	}
    	//When the point collides with one of the lines
    	else if(redPos.contains(blue)){
    		bLose = true;
    	}
    	else if(bluePos.contains(blue)){
    		bLose = true;
    	}
    	//Out of bounds detector
    	else if(blue.x <= 29 || blue.x >= 764 || blue.y <= 29 || blue.y >=536) bLose = true;
    }
    //===========
    
    //Get Statements
    //===========
    public boolean getRLose(){
    	return rLose;
    }
    public boolean getBLose(){
    	return bLose;
    }
    public int getBLives(){
    	return bLives;
    }
    public int getRLives(){
    	return rLives;
    }
    public int getPlayerCount(){
    	return playerCount;
    }
    //===========
    
    //Resets the game when a player loses
    public void reset(){
    	if(keys[KeyEvent.VK_ENTER]){
    		//used ot activate the menu screen
    		if(bLives == 0 || rLives == 0){
    			bLives = -1;
    			rLives = -1;
    		}
    		//resets vars to their beginning positions
    		eCanPress = false;
	    	bLose = false;rLose = false;
			canLose = true;
			redX = 59;redY = 254;
			blueX = 734;blueY = 254;
			rFacing = 1;bFacing = 3;
			bBoost = 2;rBoost = 2;
			bIsBoost = false;rIsBoost = false;
			bBoostCount = 0;rBoostCount = 0;
			
			redPos.clear();
			bluePos.clear();
    	}
    }
    
    public void menu(){
    	
    	//Activates the seizure mode
    	if(keys[KeyEvent.VK_X]){
    		xCanPress = true;
    	}
    	else if(keys[KeyEvent.VK_X] == false && xCanPress){
    		if(seizure) seizure = false;
    		else seizure = true;
    		xCanPress = false;
    	}
    	
    	//used to navigate the menu (move up, down, select)
    	if(keys[KeyEvent.VK_UP] || keys[KeyEvent.VK_W]){
    		uCanPress = true;
    	}
    	else if(keys[KeyEvent.VK_UP] == false && keys[KeyEvent.VK_W] == false && uCanPress){
    		menuSel -= 1;
    		uCanPress = false;
    	}
    	
    	if(keys[KeyEvent.VK_DOWN] || keys[KeyEvent.VK_S]){
    		dCanPress = true;
    	}
    	else if(keys[KeyEvent.VK_DOWN] == false && keys[KeyEvent.VK_S] == false && dCanPress){
    		menuSel += 1;
    		dCanPress = false;
    	}
    	//failsafe
    	if(menuSel > 2) menuSel = 2;
    	else if(menuSel < 0) menuSel = 0;
    	
    	if(keys[KeyEvent.VK_ENTER] == false){
    		eCanPress = true;
    	}
    	else if(keys[KeyEvent.VK_ENTER]){
    		eCanPress = false;
    		if(menuSel == 2){
    			System.exit(0);
    		}
    		playerCount = menuSel + 1;
    		gameStart = true;
    		rLives = 5;bLives = 5;
    		reset();
    	}
    }
    
    //Boost when pressed
    public void boost(){
    	if(bIsBoost){
    		bMove(); //have to do another move, otherwise it would make a dotted line
    		bCollide();
    		bluePos.add(blue);
    		
    		//the timer for the boost, runs 15 times through and then stops
    		bBoostCount += 1;
    		if(bBoostCount == 15){
    			bIsBoost = false;
    			bBoostCount = 0;
    		}
    	}
    	if(rIsBoost){ //same as above
    		rMove();
    		rCollide();
    		redPos.add(red);
    		rBoostCount += 1;
    		if(rBoostCount == 15){
    			rIsBoost = false;
    			rBoostCount = 0;
    		}
    	}
    }
    
    
    //Key Presses for directional change
    //===============================	
    public void move(){
		rMove();
		bMove();
		
		//changes the direction when a key is pressed, cannot instantly turn around, as that would immediately cause you to lose
		if(keys[KeyEvent.VK_RIGHT] && bFacing != 3) bFacing = 1;
		else if(keys[KeyEvent.VK_LEFT] && bFacing != 1) bFacing = 3;
		else if(keys[KeyEvent.VK_UP] && bFacing != 2) bFacing = 0;
		else if(keys[KeyEvent.VK_DOWN] && bFacing != 0) bFacing = 2;
		
		if(keys[KeyEvent.VK_D] && rFacing != 3) rFacing = 1;
		else if(keys[KeyEvent.VK_A] && rFacing != 1) rFacing = 3;
		else if(keys[KeyEvent.VK_W] && rFacing != 2) rFacing = 0;
		else if(keys[KeyEvent.VK_S] && rFacing != 0) rFacing = 2;
		
		//boosts when SHIFT or SPACE is pressed
		if(keys[KeyEvent.VK_SPACE] && rIsBoost == false && rBoost > 0){
			rBoost -= 1;
			rIsBoost = true;
		}
		if(keys[KeyEvent.VK_SHIFT] && bIsBoost == false && bBoost > 0){
			bBoost -= 1;
			bIsBoost = true;
		}
	}
    
    //Detects when a round is lost
    //===============================
    public void lose(){
    	if(canLose == true){
    		//resets variables and takes away a life from the player
    		if(bLose == true && rLose == true){
    			bIsBoost = false;
    			rIsBoost = false;
    			bLives -= 1;
    			rLives -= 1;
    			canLose = false;
    		}
    		else if(bLose == true){
    			bIsBoost = false;
    			rIsBoost = false;
    			rLose = false;
    			bLives -= 1;
    			canLose = false;
    		}
    		else if(rLose == true){
    			bIsBoost = false;
    			rIsBoost = false;
    			bLose = false;
    			rLives -= 1;
    			canLose = false;
    		}
    	}
    }
    
    // 2 Player Collision
    public void collide(){
    	bCollide();
    	rCollide();
    	
    	//adds the current position to the ArrayList of points that the lightcycle has already been
    	bluePos.add(blue);
    	redPos.add(red);
	}
    
    
    
    //========================================================================
    //=============================== 1 PLAYER ===============================
    //========================================================================
    public void onePlayerMove(){
    	rMove();
    	bMove();
    	
    	//movement & boost detection for the human player
    	//==========
    	if((keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT]) && rFacing != 3) rFacing = 1;
		else if((keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT]) && rFacing != 1) rFacing = 3;
		else if((keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]) && rFacing != 2) rFacing = 0;
		else if((keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN]) && rFacing != 0) rFacing = 2;
		
		if((keys[KeyEvent.VK_SPACE] || keys[KeyEvent.VK_SHIFT]) && rIsBoost == false && rBoost > 0){
			rBoost -= 1;
			rIsBoost = true;
		}
		//==========
		
		//Computer movement
		//==========
		int comTurn = dice.nextInt(150); //the random variable to determine if the computer will turn randomly
		int comBoost = 0; //random var for the computer's boost
		
		//uses distance formula to determine when to boost; if the computer is within 100 pixels of the human player's current position it has a high chance of boosting
		if(Math.sqrt(Math.pow((blue.x - red.x), 2) + Math.pow((blue.y - red.y), 2)) <= 100){
			comBoost = dice.nextInt(30);
		}
		
		if(comTurn == 1) comDoTurn(2,1); //this is to add randomness to the computer's movement, if this is not done, the computer will only move when avoiding obstacles
		
		else if((bFacing == 1 && blue.x + 30 >= 764) || (bFacing == 3 && blue.x - 30 <= 29) || (bFacing == 2 && blue.y + 30 >= 536) || (bFacing == 0 && blue.y - 30 <= 29)){
			comDoTurn(5,2);
		}
		else{
			for(int i=3;i<=45;i+=3){
				Point bUp = new Point (blue.x,blue.y - i);
				Point bRight = new Point (blue.x + i,blue.y);
				Point bDown = new Point (blue.x,blue.y + i);
				Point bLeft = new Point (blue.x - i,blue.y);				
				
				if(bFacing == 0 && (redPos.contains(bUp) || bluePos.contains(bUp))){
					comDoTurn(3,2);
					break;
				}
				else if(bFacing == 1 && (redPos.contains(bRight) || bluePos.contains(bRight))){
					comDoTurn(3,2);
					break;
				}
				else if(bFacing == 2 && (redPos.contains(bDown) || bluePos.contains(bDown))){
					comDoTurn(3,2);
					break;
				}
				else if(bFacing == 3 && (redPos.contains(bLeft) || bluePos.contains(bLeft))){
					comDoTurn(3,2);
					break;
				}
			}
		}
		
		if(comBoost == 1 && bIsBoost == false && bBoost > 0){
			bBoost -= 1;			
			bIsBoost = true;			
		}
		
		//failsafe
		if(bFacing == -1) bFacing = 3;
		else if(bFacing == 4) bFacing = 0;	
    }
    
    //This is run when the computer player desires to make a turn. Whether it be to avoid an obstacle, or because of randomness.
    //"chance" is used in the randomization process, as some type of moves have a 100% chance of working while others don't
    //	eg. If the cycle is within 30 pixels of the wall or another line and is heading in its direction, there is not a 100% chance that the AI will turn
    
    //"turnBackAmt" is a var that changes according to the type of move that is being done. What this var does is turns the cycle back a certain angle depending on what kind of turn failed to
    //work. A turn can fail when an obstacle right in front of the cycle if it were to make such turn.
    //	eg. A move done randomly attempts to turn the car in to the wall, the turnBackAmt for this move is only 1, and will simply cancel out the move and set the direction equal to what is
    //		was before it was made.
    //		Alternatively, if the turn that failed was one that was done in order to avoid an obstacle, it wouldn't make sense for the cycle to revert back to the original direction, so it turns
    //		the cycle the opposite way, in order to attempt to better avoid obstacles.
    public void comDoTurn(int chance, int turnBackAmt){
    	
    	//An arraylist of the next position of the cycle depending on what direction it is facing
    	ArrayList<Point> nextPos = new ArrayList<Point>(); 
		Point nextU = new Point (blue.x,blue.y - 3); nextPos.add(nextU);
		Point nextR = new Point (blue.x + 3,blue.y); nextPos.add(nextR);
		Point nextD = new Point (blue.x,blue.y + 3); nextPos.add(nextD);
		Point nextL = new Point (blue.x - 3,blue.y); nextPos.add(nextL);
		
		int comTurn = dice.nextInt(chance); //randomizes the direction the cycle turns (or in some cases, if it turns at all)
		
		//turns the cycle (right turns)
		if(comTurn == 0){
			bFacing += 1;
			if(bFacing == 4) bFacing = 0; //failsafe
			
			for(int i=0;i<4;i++){
				//if the new direction the cycle is going leads the AI straight into an obstacle, the turn is either cancelled or is done in the opposite direction
				if(bFacing == i && (bluePos.contains(nextPos.get(i)) || redPos.contains(nextPos.get(i)) ||nextPos.get(i).x <= 29 || nextPos.get(i).x >= 764 || nextPos.get(i).y <= 29 || nextPos.get(i).y >=536)){
					bFacing -= turnBackAmt;
				}
			}
			
		}
		else if(comTurn == 1){ //same as above (for left turns)
			bFacing -= 1;
			if(bFacing == -1) bFacing = 0; //failsafe	
			
			for(int i=0;i<4;i++){
				if(bFacing == i && (bluePos.contains(nextPos.get(i)) || redPos.contains(nextPos.get(i)) ||nextPos.get(i).x <= 29 || nextPos.get(i).x >= 764 || nextPos.get(i).y <= 29 || nextPos.get(i).y >=536)){
					bFacing += turnBackAmt;
				}
			}
		}
		//failsafe
		if(bFacing == 4) bFacing = 0;
		else if(bFacing == 5) bFacing = 1;
		else if(bFacing == -1) bFacing = 3;
		else if(bFacing == -2) bFacing = 2;
    }
    
    //Drawing
    //===============================    
	public void paintComponent(Graphics g){
		g.drawImage(back,0,0,this);
		
		//draws the amount of lives & boosts left for both players
		for(int i=0;i<rLives;i++) g.drawImage(redCycle.get(0),140 + 30*i,550,this);
		for(int i=bLives;i>0;i--) g.drawImage(blueCycle.get(0),660 - 30*i,550,this);
			
		for(int i=bBoost;i>0;i--) g.drawImage(bBoostIcon,460 - 30*i,550,this);
		for(int i=0;i<rBoost;i++) g.drawImage(rBoostIcon,340 + 30*i,550,this);
		
		if(rLives > 0 && bLives > 0){
			
			//draws the blue trail
			g.setColor(Color.blue);
			for(Point p : bluePos){
				if(seizure){
					int rRand = dice.nextInt(256);
					int gRand = dice.nextInt(256);
					int bRand = dice.nextInt(256);
					Color randCol = new Color(rRand,gRand,bRand);
					g.setColor(randCol);
				}
				g.fillRect(p.x,p.y,3,3);
			}
			//draws the red trail
			g.setColor(Color.red);
			for(Point p : redPos){
				if(seizure){
					int rRand = dice.nextInt(256);
					int gRand = dice.nextInt(256);
					int bRand = dice.nextInt(256);
					Color randCol = new Color(rRand,gRand,bRand);
					g.setColor(randCol);
				}
				g.fillRect(p.x,p.y,3,3);
			}
			
			//When a round is lost, an explosion is drawn along with a "press enter to continue" screen
			if(rLose == true && bLose == true){
				g.drawImage(explosion,redX-15,redY-12,this);
				g.drawImage(explosion,blueX-15,blueY-12,this);
				g.drawImage(enter,0,0,this);
				
				if(rLives > 0 && bLives > 0) g.drawImage(roundTie,0,0,this);
			}
			else if(rLose == true){
				g.drawImage(explosion,redX-15,redY-12,this);
				g.drawImage(enter,0,0,this);
				
				if(rLives > 0) g.drawImage(bWinRound,0,0,this);
			}
			else if(bLose == true){
				g.drawImage(explosion,blueX-15,blueY-12,this);
				g.drawImage(enter,0,0,this);
				
				if(bLives > 0) g.drawImage(rWinRound,0,0,this);
			}
		}
		//When the game is lost, draws game over screen
		else if(rLives == 0 || bLives == 0){
			if(bLives == 0 && rLives == 0) g.drawImage(gameTie,0,0,this);
			else if(bLives == 0) g.drawImage(rWinGame,0,0,this);
			else if(rLives == 0) g.drawImage(bWinGame,0,0,this);
			
			g.drawImage(enter,0,0,this);
			reset();
		}
		//When in the menus
		else{
			g.drawImage(back,0,0,this);
			g.drawImage(menu,0,0,this);
			g.drawImage(rect,187,250 + 93*menuSel,this);
		}
	}
}