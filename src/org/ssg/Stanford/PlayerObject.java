package org.ssg.Stanford;
import java.util.ArrayList;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.state.StateBasedGame;

import net.java.games.input.*;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig; 


public abstract class PlayerObject implements KeyListener{
	
    public int l;
    public int h;
    public Polygon p;
    static final float grav = 1.0f;
    public float horizVel;
    public float horizAcc;
    public float jumpVel;
   	public float jumpAcc;
    
    //game variables
    public int score;
    public int scoreCounter;
    static final int scoreFactor = 50;
    public int playerIndex;
    
    public int scoreAdded;//Displays next to score bar
    public int scoreAddedTimer;//How long scoreAdded is displayed on screen;
    
    //All drawing variables are in the subclass
    
    //input variables
    public int[] inputArr;
    public int controllerIndex;
    
    //motion variables
    public Vector2f vel;
	public Vector2f pos;
	public Vector2f acc;
	public Line velVector;
    public boolean mRight;
    public boolean mLeft;
    public boolean mJump;
    public boolean grounded;
    public boolean doubleJump;
    public int groundBuffer;
    public Polygon xTest, yTest;
    public boolean justHealed;
   
    //Powerup variables are PlayerMan only
    
    //swording variables
    public boolean interact, swing, shoot;
    public int health;
    static final int maxHealth = 1;
    public int dir;
    public Polygon sabre, sabreLeft;
    public float sabreTime;
    static int[][] sabreArray;
    static final int sabreDamage = 100;
    static final float sabreDuration = 10;
    static int sabreCoolDown;
    public int coolDown;
    
    //lasers are PlayeMan only
    
    //revival variables
    public static final int revivalTime = 1000;
    public int respawnTime;
    public boolean heal;
    public boolean respawn;
    public boolean teamKill;
    public boolean stunnedBat;
 
    public float stepCounter;
    float prevVelY;//Used to play step sound on hitting ground
    boolean justJumping;
    
    String swingSound;
    
	SoundSystem mySoundSystem;
	Controller controller;
	Component lStickX, lStickY, jumpButton, actionButton, menuButton; //gamepad buttons
	boolean controllerExist;
	boolean menuPressed;
	boolean menuCalled;//If the menu button has being sent already
	int jumpCoolDown, actionCoolDown, dirCoolDown; //Prevent rapid firing of controller events
    int MAXCOOLDOWN = 20;
	
    public PlayerObject() throws SlickException{//a and b are coordinates
    	
    }
    
    public void initControllers(Controller c, boolean cExist){
		if (cExist) {
			jumpCoolDown = 0;
			actionCoolDown = 0;
			dirCoolDown = 0;
			lStickX = c.getComponent(Component.Identifier.Axis.X);
			lStickY = c.getComponent(Component.Identifier.Axis.Y);
			actionButton = c.getComponent(Component.Identifier.Button._2);
			jumpButton = c.getComponent(Component.Identifier.Button._0);
			menuButton = c.getComponent(Component.Identifier.Button._7);
		}
    }
    
    //Each have their own shiftdowns
    
    public abstract void shiftDown(double d);
    
    public void moveLeft() {
    	if (dir < 0)
    		acc.x = horizAcc * -1;
//    	vel.x = -1 * horizVel;
    	dir = -1;
    }
    
    public void moveRight() {
    	if (dir > 0)
    		acc.x = horizAcc;
//    	vel.x = horizVel;
    	dir = 1;
    }
    
    public void moveNo() {
    	stepCounter=500;
    	acc.x = -vel.x;
    }
    
    //Have their own jump code
    
    //Swingsabre is shared
    public void swingsabre(ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr, 
    		ArrayList<PowerObject> powerArr,
    		ArrayList<AxeObject> axeArr) {
    	if (sabreTime == 0) {
    		//System.out.println("SWING");
    		mySoundSystem.quickPlay( true, swingSound, false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    		sabreTime = sabreDuration;
    	}
		boolean hit = false;
		boolean parry = false;
//    	System.out.println(p.getCenterY() + "  " + sabre.getMaxY() + " " + sabre.getMinY());
    	for (PlayerObject playerOb : playerArr) {  //collision with players
    		if (playerOb.p != p) {
    			if (dir < 0) { //checking if facing left
    				if (sabreLeft.intersects(playerOb.p)) {
//    					playerOb.vel.x -= 20;
//    					playerOb.vel.y = -2;
    					hit = true;
    				} //end if checking hitting players
    				//checking for parrying!
    				else if ((sabreLeft.intersects(playerOb.sabreLeft) && playerOb.dir < 0  && playerOb.swing)
    						|| (sabreLeft.intersects(playerOb.sabre) && playerOb.dir > 0 && playerOb.swing)) {
    					parry = true;
    					hit = false;
    				} //end else if checking for parrying
    			} //end if checking for left side
    			else if (dir > 0) { //checking if facing right
    				if (sabre.intersects(playerOb.p)) {
//    					playerOb.vel.x += 20;
//    					playerOb.vel.y = -2;
    					hit = true;
    				} //end if checking for hitting actual players
    				//checking for parrying!
    				else if ((sabre.intersects(playerOb.sabreLeft) && playerOb.dir < 0  && playerOb.swing)
    						|| (sabre.intersects(playerOb.sabre) && playerOb.dir > 0 && playerOb.swing)) {
    					parry = true;
    					hit = false;
    				} //end checking for parrying
    			} //end else if checking for right side
    		} //end if player isn't THE player in question
    		if (hit) {
    			if (playerOb.alive()) {
    				playerOb.damage(sabreDamage);
    				teamKill = true;
//    				score -= 100;
    			}
    			hit = false;
    		} //end if hit
    		if (parry) {
    			if(playerIndex==1){//Only one player plays the parry sound
    				mySoundSystem.quickPlay( true, "SwordClash.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    			}
    			vel.x = -dir * 5;
    			vel.y = -5;
    		}
    	} //end player checking
    	
    	for (EnemyObject enemyOb : enemyArr) {
    		hit = false;
    		parry = false;
    		EnemyKnight knightOb = null;
    		EnemyHellBat hellBatOb = null;
    		if (enemyOb.enemyType.equals("knight")) {
    			knightOb = (EnemyKnight)enemyOb;
    		} else if (enemyOb.enemyType.equals("hellbat")) {
    			hellBatOb = (EnemyHellBat)enemyOb;
    		}
    		if (dir < 0) { //checking for left
				if (sabreLeft.intersects(enemyOb.p)) { //checking for hitting enemy
//					enemyOb.vel.x -= 20;
//					enemyOb.vel.y = -2;
					hit = true;
				} //end checking for hitting enemy
				//checking for parrying!!!
				else if (enemyOb.enemyType.equals("knight")) { //if enemy is a knight
					if ((sabreLeft.intersects(knightOb.sabreLeft) && knightOb.dir < 0  && knightOb.swing)
    						|| (sabreLeft.intersects(knightOb.sabre) && knightOb.dir > 0 && knightOb.swing)) {
    					parry = true;
    					hit = false;
    					knightOb.swing = false;
    					knightOb.coolDown = knightOb.stunCoolDown;
    					knightOb.stun = true;
    					knightOb.charge = false;
    					knightOb.vel.x = -5;
    					knightOb.vel.y = -5;
					} //end parry checking
				} //end knight checking
				if (parry) {
					mySoundSystem.quickPlay( true, "SwordClash.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
	    			vel.x = dir * 5;
	    			vel.y = 5;
	    		}
			} //end checking in left direction
			else if (dir > 0) { //checking for right
				if (sabre.intersects(enemyOb.p)) { //checking for hitting enemy
//					enemyOb.vel.x += 20;
//					enemyOb.vel.y = -2;
					hit = true;
				} //end checking for hitting enemy
				//checking for parrying!!
				else if (enemyOb.enemyType.equals("knight")) { //if enemy is a knight
					if ((sabre.intersects(knightOb.sabreLeft) && knightOb.dir < 0  && knightOb.swing)
    						|| (sabre.intersects(knightOb.sabre) && knightOb.dir > 0 && knightOb.swing)) {
    					parry = true;
    					hit = false;
    					knightOb.swing = false;
    					knightOb.coolDown = knightOb.stunCoolDown;
    					knightOb.stun = true;
    					knightOb.charge = false;
    					knightOb.vel.x = -5;
    					knightOb.vel.y = -5;
					} //end parry checking
				} //end knight checking
				if (parry) {
					mySoundSystem.quickPlay( true, "SwordClash.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
	    			vel.x = -dir * 5;
	    			vel.y = -5;
	    		}
			}
    		if (hit) {
    			if (enemyOb.alive()) {
	    			if (enemyOb.enemyType.equals("hellbat")) {
	    				if (!stunnedBat) {
	    					mySoundSystem.quickPlay( true, "VengeBatSwat.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
	        				hellBatOb.stun(dir, sabreDamage, playerIndex);
	        				stunnedBat = true;
	        			}
	    			}
	    			else {
	    				enemyOb.damage(sabreDamage);
	    			}
	    			hit = false;
	    			//System.out.println("hit enemy!" + enemyOb.enemyType + " " + enemyOb.health);
	    			if (!enemyOb.alive()) {
	    				//score += enemyOb.pointValue;
	    				addScore(enemyOb.pointValue);
	    			} //end score adding if enemy is killed
    			} //end checking if enemy is alive
    		} //end hit block
    		
    		//parrying code?
    		if (parry) {
    			
    		}
    		
    	} //end enemy checking code
    	
    	
    	//powerUp checking code
    	for (PowerObject powerOb : powerArr) {
    		hit = false;
    		if (dir < 0) { //checking for left
				if (sabreLeft.intersects(powerOb.p)) { //checking for hitting powerUp
					mySoundSystem.quickPlay( true, "BoxRicochet2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
					hit = true;
					powerOb.vel.x = -15;
					powerOb.vel.y = -10;
				} //end sabreleft check
    		} //end left check
    		else if (dir > 0) {
    			if (sabre.intersects(powerOb.p)) { //checking for hitting powerUp
    				mySoundSystem.quickPlay( true, "BoxRicochet2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    				hit = true;
    				powerOb.vel.x = 15;
    				powerOb.vel.y = -10;
    			} //end sabreright check
    		} //end right check
    	} //end powerup check
    	
    	//axe collision check!
    	for (AxeObject axe : axeArr) {
    		hit = false;
    		if (dir < 0) {
    			if (sabreLeft.intersects(axe.p)) {
    				hit = true;
    			}
    		}
    		else if (dir > 0) {
    			if (sabre.intersects(axe.p)) {
    				hit = true;
    			}
    		}
    		if (hit) {
    			if (axe.deadly) {
    				mySoundSystem.quickPlay( true, "AxeRicochetAlt.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    				axe.dir = dir;
    				axe.vel.x = axe.velX*dir;
    				axe.vel.y = -axe.velY;
    				axe.setHitter(playerIndex);
    			}
				hit = false;
    		}
    	}
    	
    	sabreTime--;
    	if (sabreTime <= 0) {
    		swing = false;
    		sabreTime = 0;
    		interact = false;
    		stunnedBat = false;
        	coolDown = sabreCoolDown;
    	} //end sabre decrement
    	
    } //end sabre code
    
    //Laser is PlayerMan only
    
    public void damage(int d) {
    	//System.out.println("SWING");
    	if(d>0){
    		mySoundSystem.quickPlay( true, "ManDead.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    	}
    	health -= d;
    	if (!alive()) {
    		mLeft = false;
    		mRight = false;
    		mJump = false;
    		interact = false;
    		swing = false;
    		heal = false;
//    		shoot = false;
    		respawnTime = revivalTime;
    	}
    }
    
    
    public void heal(int h) {
    	health += h;
    	if (health > maxHealth) {
    		health = maxHealth;
    		respawnTime = 0;
    	}
    }
    
    //Powerup is PlayerMan only
    
    public boolean alive() {
    	return health > 0;
    }
    
    public int getPlayerIndex(){
    	return playerIndex;
    }
    
    public void addScore(int n){
    	score+=n;
    	if(scoreAddedTimer<=0){
    		scoreAdded=n;
    	}else{
    		scoreAdded+=n;
    	}
    	scoreAddedTimer=250;
    	
    }
    
    public abstract void update(StateBasedGame sbg, int delta, ArrayList<StaticObject> arr, 
    		ArrayList<PlayerObject> playerArr, 
    		ArrayList<EnemyObject> enemyArr,
    		ArrayList<AxeObject> axeArr,
    		ArrayList<PowerObject> powerArr) throws SlickException;
	public abstract void render(Graphics g);
    
	public void inputEnded() {
		// TODO Auto-generated method stub
		
	}

	public boolean isAcceptingInput() {
		// TODO Auto-generated method stub
		return true;
	}

	public void setInput(Input arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void setTeamkill(boolean b){
		teamKill = b;
	}
	
//	public void keyPressed(int arg0, char arg1) {
//		// TODO Auto-generated method stub
//		if (!controllerExist && alive()) {
//			if (arg0 == inputArr[0]){
//				mJump = true;
//			}
//			if (arg0 == inputArr[1]){	
//				mLeft = true;
//			}
//			if (arg0 == inputArr[2]){
//				mRight = true;
//			}
//			if (arg0 == inputArr[3]){
//				interact = true;
//			}
//		}
//	}
//
//	public void keyReleased(int arg0, char arg1) {
//		// TODO Auto-generated method stub
//		if(!controllerExist){
//			if (arg0 == inputArr[0]){
//				mJump = false;
//			}
//			if (arg0 == inputArr[1]){
//				mLeft = false;
//			}
//			if (arg0 == inputArr[2]){			
//				mRight = false;
//			}
//			if (arg0 == inputArr[3]){
//				interact = false;
//			}
//		}
//	}

	public void pollControllers(){
//		if(actionCoolDown > 0)
//			actionCoolDown --;
//		if(jumpCoolDown > 0)
//			jumpCoolDown --;
		
		if (jumpButton.getPollData() == 1.0 && alive() && jumpCoolDown <= 1){
			mJump = true;
			jumpCoolDown = MAXCOOLDOWN;
		}else if(jumpButton.getPollData() == 0.0){
			jumpCoolDown = 0;
			mJump = false;
		}else {
			mJump = false;
		}
		if (actionButton.getPollData() == 1.0 && alive() && actionCoolDown <=1){
			interact = true;
			actionCoolDown = MAXCOOLDOWN;
		}else if(actionButton.getPollData() == 0.0){
			actionCoolDown = 0;
			interact = false;
		}else{
			interact = false;
		}
		if (lStickX.getPollData() <= -0.5 && alive() && dirCoolDown<=1){
			mLeft = true;
			mRight = false;
			dirCoolDown = MAXCOOLDOWN;
		}else if(lStickX.getPollData() >= 0.5 && alive() && dirCoolDown<=1){
			mRight = true;
			mLeft = false;
			dirCoolDown = MAXCOOLDOWN;
		}else if(Math.abs(lStickX.getPollData()) < 0.5){
			mLeft = false;
			mRight = false;
			dirCoolDown = 0;
		}
		if(menuButton.getPollData() == 1.0){
			menuPressed = true;
		}else{
			menuPressed = false;
			menuCalled = false;
		}
	}
	
	public void setSoundSystem(SoundSystem ss){
		mySoundSystem=ss;
	}
	
	public int getScoreAdded(){
		return scoreAdded;
	}
	
	public int getScoreAddedTimer(){
		return scoreAddedTimer;
	}
	
	public boolean getMenuCall(){
		if(menuPressed){
			if(!menuCalled){
				menuCalled = true;
				return true;
			}
		}
		return false;
	}
	
	public void inputStarted() {
		// TODO Auto-generated method stub
		
	}
	

}