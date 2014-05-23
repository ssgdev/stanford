package org.ssg.Stanford;
import java.util.ArrayList;

import net.java.games.input.*;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.Color;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig; 

public class PlayerAxeKnightObject extends PlayerObject implements KeyListener{
    
	public SpriteSheet sprite;
    public SpriteSheet ammoSprite;
	public Animation stunRightAnim, stunLeftAnim, walkLeftAnim, walkRightAnim;
	public Image lStand, lStep, rStand, rStep;
	public int deathFrames;
	public final float chargeVel = 10.0f;
	public boolean charging;
	public int dirPressed;//Stores last direction key pressed, to see if you doubled pressed, so you can charge
	public int chargeFrames;//If you hit left/right again during chargeframes, you charge
	//sword axe variables
	public boolean swing;
	public int swingTime;
	public int swingCoolDown;
	static final int powerSwingCoolDown = 5;
	
	//movement/behavior variables
	public boolean charge;
	public int coolDown;
	StateBasedGame sbg;
	public final int maxAmmo = 25;
	public int ammo;
    
    public PlayerAxeKnightObject(StateBasedGame statbasgam, int a, int b, Image i, SpriteSheet ss, int[] arr, int index, SoundSystem mSS, Controller c, boolean cExist) throws SlickException{//a and b are coordinates
        l = 28;
        h = 50;
        horizVel = 5.0f;
        horizAcc = 3.0f;
        jumpVel = 18.0f;
        jumpAcc = 200.0f;
    	playerIndex = index;
    	controllerIndex = 0;
    	controller = c;
    	controllerExist = cExist;
    	initControllers(controller, controllerExist);
        pos = new Vector2f (a, b);
        vel = new Vector2f (0, 0);
        acc = new Vector2f (0, 0);
        velVector = new Line(0,0);
        grounded = false;
        groundBuffer = 0;
        doubleJump = true;
        mRight = false;
        mLeft = false;
        mJump = false;
        interact = false;
        swing = false;
        shoot = false;
        heal = false;
        respawnTime = 0;
        respawn = false;
        justHealed = false;
        xTest = new Polygon();
        yTest = new Polygon();
        health = maxHealth;
        
		charge = false;
		swing = false;
		coolDown = 0;
		swingTime = 0;
		swingCoolDown = 50;
		
    	sprite = new SpriteSheet(i,46,50);
    	ammoSprite = ss;
    	
		sprite.startUse();
		stunRightAnim =new Animation(new Image[]{sprite.getSubImage(0,1),sprite.getSubImage(0,0)}, 100, true);
		stunLeftAnim =new Animation(new Image[]{sprite.getSubImage(3,1),sprite.getSubImage(3,0)}, 100, true);
		walkLeftAnim = new Animation(new Image[]{sprite.getSubImage(3,0),sprite.getSubImage(5,0)},70,true);
		walkRightAnim = new Animation(new Image[]{sprite.getSubImage(0,0),sprite.getSubImage(2,0)},70,true);
		sprite.endUse();
    	
		p = new Polygon(new float[]{pos.x,pos.y,pos.x+l,pos.y,pos.x+l,pos.y+h,pos.x,pos.y+h});
		p.setClosed(true);
		sabreArray = new int[][] {
				{-11, 7, 28, 39, 44, 39, 35, 28, 19, 7, -11},
				{35, 41, 36, 24, -5, -19, 0, 16, 26, 32, 35}
		};
		sabre = new Polygon();
		sabreLeft = new Polygon();
		for (int j = 0; j < sabreArray[0].length; j++) {
			sabre.addPoint(p.getCenterX()+sabreArray[0][j], p.getCenterY()-sabreArray[1][j]);
			sabreLeft.addPoint(p.getCenterX()-sabreArray[0][j], p.getCenterY()-sabreArray[1][j]);
		}
		sabre.setClosed(true);
		sabreLeft.setClosed(true);

		sabreTime = 0;
        //player input
        inputArr = arr;
        
        //score
        score = 0;
        scoreCounter = 0;
        
        //tk
        teamKill = false;
        stunnedBat = false;

        mySoundSystem = mSS;
        
        stepCounter = 500;
        prevVelY=0;
        justJumping = false;
        deathFrames = 70;
        dir = 1;
        swingSound = "KnightSwing.wav";
        
        sbg = statbasgam;
        coolDown = 0;
        ammo = 0;
    }
    
    public void shiftDown(double d){
        pos.y+=d;
        sabre.setY((float) (sabre.getY()+d));
        sabreLeft.setY((float) (sabreLeft.getY()+d));
        p.setY(pos.y);
        if (alive()) {
	        scoreCounter++;
	        if (scoreCounter >= scoreFactor) {
	        	scoreCounter = 0;
	        	score += 10;
	        }
        }
    }
    
    public void jump() {
    	justJumping = true;
    	if(doubleJump){
    		mySoundSystem.quickPlay( true, "KnightJump.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    	}
    	vel.y = -1 * jumpVel;
    }
    @Override
    public void damage(int d) {
    	//System.out.println("SWING");
    	if(d>0){
    		mySoundSystem.quickPlay( true, "KnightDeath.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
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
    		ammo=0;
    		swingCoolDown = 50;
    	}
    }
    
    public boolean alive() {
    	return health > 0;
    }
    
    public void throwAxe(ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr, 
    		ArrayList<PowerObject> powerArr,
    		ArrayList<AxeObject> axeArr) {
		mySoundSystem.quickPlay( true, "KnightSwing.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		// TODO Auto-generated method stub
		if (dir < 0) {
			axeArr.add(new AxeObject((int)p.getMinX()-2-20, (int)p.getMinY(), 20, dir, ((Stanford)sbg).blueAxeSheet, mySoundSystem, true, playerIndex));
		}
		else if (dir > 0) {
			axeArr.add(new AxeObject((int)p.getMaxX()+2, (int)p.getMinY(), 20, dir, ((Stanford)sbg).blueAxeSheet, mySoundSystem, true, playerIndex));
		}
		swing = false;
		charge = false;
		swingTime = swingCoolDown;
		coolDown = swingCoolDown;
		
		if(swingCoolDown == powerSwingCoolDown){
			ammo--;
			if(ammo==0){
				swingCoolDown = 50;
			}
		}
		
    } //end axe code
    
    public void powerUp(PowerObject pow, ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr) {
    	mySoundSystem.quickPlay( true, "Powerup.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    	ammo = maxAmmo;
    	swingCoolDown = powerSwingCoolDown;
    } //end powerup method
    
    
    public void update(StateBasedGame sbg, int delta, ArrayList<StaticObject> arr, 
    		ArrayList<PlayerObject> playerArr, 
    		ArrayList<EnemyObject> enemyArr,
    		ArrayList<AxeObject> axeArr,
    		ArrayList<PowerObject> powerArr) throws SlickException {
    		float d = delta/20;
    		
    		if(controllerExist)
    			pollControllers();
    		
    		if(scoreAdded>0){
    			scoreAddedTimer-=5;
    		}
    		if(scoreAddedTimer<=0){
    			scoreAdded=0;
    		}
    		
    		grounded = false;
    		acc.y = grav;
    		Vector2f prevPos = new Vector2f(pos.x, pos.y);
    		pos.x += vel.x * d;
    		pos.y += vel.y * d;
    		chargeFrames-=delta;
    		if(chargeFrames<0)
    			chargeFrames = 0;
//    		if (pos.x > 800-20-18 && laserNum > 0) {
//    			pos.x = 800-20-18;
//    		}
//    		if (pos.x < 20+2 && laserNum > 0) {
//    			pos.x = 20+2;
//    		}    		
    		
    		//    	System.out.println("pre=" + prevPos + ", pos=" + pos);
    		xTest = new Polygon(new float[]{pos.x,prevPos.y,pos.x+l,prevPos.y,pos.x+l,prevPos.y+h,pos.x,prevPos.y+h});
    		yTest = new Polygon(new float[]{prevPos.x,pos.y,prevPos.x+l,pos.y,prevPos.x+l,pos.y+h,prevPos.x,pos.y+h});

    		velVector = new Line(prevPos.x, prevPos.y, pos.x, pos.y);    	
    		if(coolDown>0)
    			coolDown--;
    		if(swingTime>0)
    			swingTime--;
    		//System.out.println(buttonsHeld);
    		//System.out.println(mLeft);
    		//System.out.println(walkS.playing());
    		
    		//COLLISION CODE FOR OTHER PLAYERS
    		for (PlayerObject playerOb : playerArr) {  //collision with players
    			if (playerOb.p != p && playerOb.alive() && alive() && !playerOb.justHealed && !justHealed) {
    				if (xTest.intersects(playerOb.p)) {
    					float tempX = vel.x;
    					vel.x = 0;
    					pos.x = prevPos.x;
    					if (!playerOb.mLeft && !playerOb.mRight) {
    						playerOb.vel.x = tempX;
//    						playerOb.dir = dir; //changing direction of collided player to match?
    					}
    				}
    				if (yTest.intersects(playerOb.p)) {
    					if (pos.y > prevPos.y) {
    						grounded = true;
    						doubleJump = true;
        					groundBuffer = 5; //BUFFER SO YOU CAN JUMP RUNNING OFF THE CORNER OF A BOX!
    						pos.y = playerOb.p.getY()-h-1;
    						vel.y = 0;
    					}
    					else {
    						pos.y = prevPos.y;
    						vel.y = 0;
    					}
    				}
    				if (p.intersects(playerOb.p)) { //are you already stuck in your partner???
    					//float xDiff = p.getX()-playerOb.p.getX();
    					float yDiff = p.getY()-playerOb.p.getY();	
    					//	    			System.out.println("playerbump" + " " + Input.getKeyName(inputArr[0]) + " " + yDiff);

    					if (yDiff < 0) {
    						pos.y = playerOb.p.getY()-h-1;
    						vel.y = 0;
    					}
    					else {
    						pos.y = prevPos.y;
    					}
    				}
    			}
    		}
    		
    		//COLLISION CODE FOR BLOCKS
    		for (StaticObject staticOb : arr) { //  collision with blocks
    			if (xTest.intersects(staticOb.p)) {
    				vel.x = 0;
    				pos.x = prevPos.x;
    			}
    			if (yTest.intersects(staticOb.p)) {
    				staticOb.collided();
    				vel.y = 0;
    				if (pos.y > prevPos.y) {
    					grounded = true;
    					doubleJump = true;
    					groundBuffer = 5; //BUFFER SO YOU CAN JUMP RUNNING OFF THE CORNER OF A BOX!
    					//    				System.out.println(pos.y + ", " + staticOb.p.getY());
    					pos.y = staticOb.p.getY()-h-1;
    				}
    				else if (pos.y < prevPos.y) {
    					//    				pos.y = prevPos.y;
    					pos.y = staticOb.p.getY()+staticOb.l+2;
    					prevPos.y = pos.y;
    				}
    			}
    			if (p.intersects(staticOb.p)) { //are you already stuck in a block???
    				float xDiff = p.getCenterX() - staticOb.p.getCenterX();
    				float yDiff = p.getCenterY() - staticOb.p.getCenterY();
    				//System.out.println("bump");
    				staticOb.collided();
    				if (Math.abs(xDiff) < Math.abs(yDiff)) {
    					if (xDiff < 0)
    						pos.x -= 2;
    					else 
    						pos.x += 2;
    				}
    				else {
    					if (yDiff < 0)
    						pos.y -= 2;
    					else
    						pos.y += 2;
    				}
    			}
    		} //end static object collision
    		
    		groundBuffer--;
    		if (groundBuffer > 0) {
    			grounded = true;
    		} else {
    			justJumping=true;
    			grounded = false;
    			groundBuffer = 0;
    		}
    		
    		if(grounded)
    			stepCounter+=(Math.abs(vel.x)*delta);
    		if(stepCounter>=1000){
    			stepCounter-=1000;
    			mySoundSystem.quickPlay( true, "KnightWalk.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    		}
    		
    		if(Math.abs(vel.y)<Math.abs(prevVelY)&&prevVelY>0&&justJumping){
    			justJumping = false;
    			stepCounter=500;
    			mySoundSystem.quickPlay( true, "KnightWalk.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    		}
    		
    		//COLLISION CODE FOR ENEMIES
    		for(EnemyObject enemObj: enemyArr){
    			if(((p.intersects(enemObj.p))&& health>0 && enemObj.alive())){
    				damage(enemObj.contactDamage);
    			}
    		}
    		
    		//jumping
    		if (mJump && grounded) {
    			mJump = !mJump;
    			jump();
    		}
    		
    		if (grounded) {
    			//    		acc.y = 0;
    		}
    		
    		if (mRight && !mLeft) {
    			moveRight();
    		}
    		if (mLeft && !mRight) {
    			moveLeft();
    		}
    		if ((!mLeft && !mRight) || (mLeft && mRight)) {
    			moveNo();
    		}
    		
    		//RESPAWN TIMER
//    		if (!alive()) {
//    			respawnTime--;
//    			if (respawnTime < 0) {
//    				respawn = true;
//    			} //end respawn code
//    		}
    		
    		//movement updates		
    		vel.x += acc.x * d;
    		if (vel.x > horizVel)
    			vel.x = horizVel;
    		if (vel.x < -horizVel)
    			vel.x = -horizVel;
    		vel.y += acc.y * d;
    		//    	pos.x = p.getX();
    		//    	pos.y = p.getY();
    		
    		//updating sword position
    		//float xDelta = pos.x - p.getX();
    		//float yDelta = pos.y - p.getY();
    		p.setLocation(pos.x, pos.y);
    		
    		//CONTEXT SENSITIVE SWINGING/HEALING/SHOOTING
    		heal = false;
    		if (interact) {
    			for (PlayerObject playerOb : playerArr) {
    				if (p.intersects(playerOb.p) && !playerOb.alive() && !swing) {
    					mySoundSystem.quickPlay( true, "Revive.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    					swing = false;
    					sabreTime = 0;
    					heal = true;
//    					playerOb.heal(10);
    					playerOb.health = maxHealth;
    					if (playerOb.alive()) {
    						justHealed = true;
    						playerOb.justHealed = true;
//    						score += 100;
    						heal = false;
    						interact = false;
    					} //end if player alive
    				} //end if player is intersecting
    			} //end player array iteration
    			if (!heal && interact && !swing) {
    	    		for (PowerObject powerOb : powerArr) {
    	    			if (p.intersects(powerOb.p) && interact) {
    	    				powerUp(powerOb, playerArr, enemyArr);
    	    				powerOb.pickUp();
    	    				interact = false;
    	    			} //end intersect check
    	    		} //end powerup iterator
    			} //end powerup if
    		} //end context sensitive button
    		
    		//REALLY CONVOLUTED HEALING/COLLISION CHECKING STUFF
    		int numHealed = 0;
    		for (PlayerObject playerOb : playerArr) {
    			if (!p.intersects(playerOb.p) && justHealed && playerOb.justHealed) {
    				justHealed = false;
//    				playerOb.justHealed = false;
    			}
    			if (playerOb.respawn && alive() && playerOb.p != p) { //respawn functionality code.
    				playerOb.respawn = false;
    				playerOb.health = maxHealth;
    				playerOb.pos.set(pos);
    				playerOb.p.setLocation(playerOb.pos);
    				playerOb.vel = new Vector2f(0,0);
    				justHealed = true;
    				numHealed++;
    				playerOb.justHealed = true;
    				playerOb.sabre = new Polygon();
    				playerOb.sabreLeft = new Polygon();
    		        for (int j = 0; j < sabreArray[0].length; j++) {
    		        	playerOb.sabre.addPoint(playerOb.p.getCenterX()+sabreArray[0][j], playerOb.p.getCenterY()-sabreArray[1][j]);
    		        	playerOb.sabreLeft.addPoint(playerOb.p.getCenterX()-sabreArray[0][j], playerOb.p.getCenterY()-sabreArray[1][j]);
    		        } //refreshing sabres
    			} //end respawn code
    			
    			if (playerOb.justHealed)
    				numHealed++;
    		}
    		for (PlayerObject playerOb : playerArr) {
    			if (p.intersects(playerOb.p) && playerOb.justHealed) {
    				justHealed = true;
    			}
    		}
    		if (justHealed && numHealed == 1) {
    			justHealed = false;
    		}
    		//END CONVOLUTION!
    		
    		//to swing!
			if ((!heal && interact) || swing) {
				if(coolDown==0){
					swing = true;
					throwAxe(playerArr, enemyArr, powerArr, axeArr);
				}
			} //end swinging code
			
    		//teamkill check to spawn bat!
    		if (teamKill) {
    			teamKill = false;
    			//System.out.println("hellbatspawn?!");
//    			double rand = Math.random();
    			if (p.getCenterX() > 400) {
    				enemyArr.add(new EnemyHellBat(-40, (int)p.getCenterY(), ((Stanford)sbg).hellBatImg, 1, this, mySoundSystem));
    			}
    			else {
    				enemyArr.add(new EnemyHellBat(840, (int)p.getCenterY(), ((Stanford)sbg).hellBatImg, 1, this,mySoundSystem));
    			}
    		} //end hellbat spawning!
    		
			//Checking for dropdown death
			if (p.getMinY() > 1250) {
				if (alive())
					damage(5000);
			}
			
			prevVelY = vel.y;
    }
    
	public void render(Graphics g) {
		// TODO Auto-generated method stub
		if (alive()){
			int animGap;
			if(ammo>0){
				animGap = 3;
			}else{
				animGap = 15;
			}
			if(coolDown > swingCoolDown - animGap){
				if(dir>0){
					sprite.startUse();
					sprite.renderInUse((int)pos.x, (int)pos.y, 1, 0);
					sprite.endUse();
				}else if(dir<0){
					sprite.startUse();
					sprite.renderInUse((int)pos.x-l/2, (int)pos.y, 4, 0);
					sprite.endUse();
				}
			}else if (grounded && !swing){ //if on the ground and not swinging
				if(Math.abs(vel.x)>.1){
					if(dir>0){
						walkRightAnim.draw((int)pos.x,(int)pos.y);
					}else if(dir<0){
						walkLeftAnim.draw((int)pos.x-l/2,(int)pos.y);
					}
				}else{
					sprite.startUse();
					if (dir > 0) {
						sprite.renderInUse((int)pos.x, (int)pos.y, 0, 0);
					} else if (dir<0){
						sprite.renderInUse((int)pos.x-l/2, (int)pos.y, 3, 0);
					}
					sprite.endUse();
				}
			} else if (!swing) { //if not grounded and not swinging
				sprite.startUse();
				//	    		if(Math.abs(vel.x)<.1){
				//	    			sprite.renderInUse((int)pos.x, (int)pos.y, 5, 0);
				//	    		}else if(dir==1){
				if (dir > 0) {
					//	    			if(vel.x < 4){
					//	    				sprite.renderInUse((int)pos.x, (int)pos.y, 5, 0);
					//	    			}else{
					sprite.renderInUse((int)pos.x, (int)pos.y, 2, 0);
					//	    			}
				} else if (dir < 0) {
					//	    			if(vel.x > -4){
					//	    				sprite.renderInUse((int)pos.x, (int)pos.y, 5, 0);
					//	    			}else{
					sprite.renderInUse((int)pos.x-l/2, (int)pos.y, 5, 0);
					//	    			}
				}
				sprite.endUse();
			} else if (swing) { //if swinging at all
				if (dir > 0) {
					sprite.startUse();
					sprite.renderInUse((int)pos.x, (int)pos.y, 1, 0);
					sprite.endUse();
				}
				else if (dir < 0) {
					sprite.startUse();
					sprite.renderInUse((int)pos.x-l/2, (int)pos.y, 4, 0);
					sprite.endUse();
				}
			} //end if swinging
//			g.getFont().drawString(pos.x, pos.y, pos.x + ", " + pos.y);
		} else { //if dead? something like that. but this doesnt happen cuz knights are just getting deleted haha
			//    		g.setColor(Color.red);
			//    		g.drawRect(pos.x, pos.y, l, h);
			//    		g.setColor(Color.white);
			sprite.startUse();
			if (deathFrames > 50) {
//				g.setColor(Color.white);
//				g.drawString(""+pointValue, pos.x + l/2, pos.y - 10);
				deathFrames--;
				if (dir > 0) {
					sprite.renderInUse((int)pos.x, (int)pos.y, 0, 1);
				} else if (dir < 0) {
					sprite.renderInUse((int)pos.x-l/2, (int)pos.y, 3, 1);
				}
			} else if (deathFrames > 20) {
				deathFrames--;
				if (dir > 0) {
					sprite.renderInUse((int)pos.x, (int)pos.y, 1, 1);
				} else if (dir < 0) {
					sprite.renderInUse((int)pos.x-l/2, (int)pos.y, 4, 1);
				}
			} else {
				if (dir > 0) {
					sprite.renderInUse((int)pos.x, (int)pos.y, 2, 1);
				} else if (dir < 0) {
					sprite.renderInUse((int)pos.x-l/2, (int)pos.y, 5, 1);
				}
			} //end deathFrames bit
			sprite.endUse();
		}
		//		g.draw(sabre);
		//    	g.setColor(Color.green);
		//    	g.drawRect(p.getMinX(), p.getMinY(), l, h);
		//    	g.drawOval(p.getCenterX(), p.getCenterY(), 5, 5);
		//    	g.setColor(Color.white);
		
		//Draw Powerups
    	int ammoX=0;
    	if(playerIndex==1){
    		ammoX=1;
    	}else{
    		ammoX=781;
    	}
    	if(ammo>0){
    		sprite.startUse();
    		if(dir>0){
    			sprite.renderInUse((int)pos.x,(int)pos.y,6,1);
    		}else if(dir<0){
    			sprite.renderInUse((int)pos.x -l/2,(int)pos.y,6,0);
    		}
    		sprite.endUse();
    		g.setColor(Color.black);
    		g.fillRect(ammoX, 1142-20*(ammo), 18, 20*(ammo));
    		ammoSprite.startUse();
    		ammoSprite.renderInUse(ammoX, 1142, 2, 0);
    		ammoSprite.endUse();
    		for(int i=0;i<ammo;i++){
    			g.setColor(new Color(100, 230, 100));
    			g.fillRect(ammoX, 1122-i*20, 16,16);
    			g.setColor(new Color(70, 180, 70));
    			g.drawRect(ammoX, 1122-i*20, 16,16);
    		}

    	}

		
	}
		
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

	@Override
	public void keyPressed(int arg0, char arg1) {
		// TODO Auto-generated method stub
		if (!controllerExist && alive()) {
			if (arg0 == inputArr[0]){
				mJump = true;
			}
			if (arg0 == inputArr[1]){	
				mLeft = true;
			}
			if (arg0 == inputArr[2]){
				mRight = true;
			}
			if (arg0 == inputArr[3]){
				interact = true;
			}
			if(arg0 == Input.KEY_Q){
				if(swingCoolDown==2){
					swingCoolDown = 50;
				}else{
					swingCoolDown = 2;
				}
			}
		}
	}

	@Override
	public void keyReleased(int arg0, char arg1) {
		// TODO Auto-generated method stub
		if(!controllerExist){
			if (arg0 == inputArr[0]){
				mJump = false;
			}
			if (arg0 == inputArr[1]){
				mLeft = false;
			}
			if (arg0 == inputArr[2]){			
				mRight = false;
			}
			if (arg0 == inputArr[3]){
				interact = false;
			}
		}
	}

	public void setSoundSystem(SoundSystem ss){
		mySoundSystem=ss;
	}
	
	public void inputStarted() {
		// TODO Auto-generated method stub
		
	}

}