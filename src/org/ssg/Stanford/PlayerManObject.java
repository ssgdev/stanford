package org.ssg.Stanford;
import java.util.ArrayList;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.Color;

import net.java.games.input.*;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig; 

public class PlayerManObject extends PlayerObject implements KeyListener {
    //animation variables
    //sprite
    public SpriteSheet sprite;
    public SpriteSheet ammoSprite;
    //0-stand 1-left1 2-left2 3-right 4-right2	5-up 6-jleft 7-lright
    public Animation walkLeftAnim, walkRightAnim;
    public Animation walkLeftAnimLaser, walkRightAnimLaser;
    public Animation walkLeftAnimJump, walkRightAnimJump;
    
    //powerup variables
    public int dJumpNum;
    public int laserNum;
    
    //laser variables
    static final int laserDamage = 100;
    static final float laserSpeed = 30.0f;
    static final int laserLength = 55;
    public Polygon laser;
    public int laserTime, laserDir;
    
    public PlayerManObject(StateBasedGame sbg, int a, int b, Image i, SpriteSheet ss, int[] arr, int index, SoundSystem mSS, Controller c, boolean cExist) throws SlickException{//a and b are coordinates
        l = 16;
        h = 40;
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
        ammoSprite = ss;
        sprite = new SpriteSheet(i,20,40);//Player spritesheet image
        walkLeftAnim = new Animation(new Image[]{sprite.getSubImage(1,0),sprite.getSubImage(2,0)}, 100, true);
        walkRightAnim =new Animation(new Image[]{sprite.getSubImage(3,0),sprite.getSubImage(4,0)}, 100, true); 
        walkLeftAnimLaser = new Animation(new Image[]{sprite.getSubImage(1,1),sprite.getSubImage(2,1)}, 100, true);
        walkRightAnimLaser =new Animation(new Image[]{sprite.getSubImage(3,1),sprite.getSubImage(4,1)}, 100, true);
        walkLeftAnimJump = new Animation(new Image[]{sprite.getSubImage(1,2),sprite.getSubImage(2,2)}, 100, true);
        walkRightAnimJump =new Animation(new Image[]{sprite.getSubImage(3,2),sprite.getSubImage(4,2)}, 100, true);
        dir = 1;
        p = new Polygon(new float[]{pos.x,pos.y,pos.x+l,pos.y,pos.x+l,pos.y+h,pos.x,pos.y+h});
        p.setClosed(true);
        coolDown = 0;
        
        //sabre stuff
        sabreArray = new int[][] {
        		{-5, 20, 28, 25, 12, 15, 16, 13},
        		{37, 25, 11, -8, -15, -5, 9, 20}
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
        sabreCoolDown = 10;
//        sabreImg = new Image("resources/swords.png");
//        sabreSprite = new SpriteSheet(sabreImg, 10, 7);
        
        //laser stuff
        laserDir = 0;
        laserTime = 0;
        
        //player input
        inputArr = arr;
        
        //score
        score = 0;
        scoreCounter = 0;
        scoreAdded=0;
        scoreAddedTimer=0;
        
        //powerups
        dJumpNum = 0;
        laserNum = 0;
        
        //tk
        teamKill = false;
        stunnedBat = false;

        mySoundSystem = mSS;
        
        stepCounter = 500;
        prevVelY=0;
        justJumping = false;
        
        swingSound = "ManSwing.wav";
    }
    
    public void shiftDown(double d){
        pos.y+=d;
        sabre.setY((float) (sabre.getY()+d));
        sabreLeft.setY((float) (sabreLeft.getY()+d));
        if (shoot)
        	laser.setY((float) (laser.getY()+d));
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
    		mySoundSystem.quickPlay( true, "ManJump2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    	}
    	vel.y = -1 * jumpVel;
    }
    
    public void shootLaser (float d, ArrayList<StaticObject> staticArr, ArrayList<PlayerObject> playerArr, 
    		ArrayList<EnemyObject> enemyArr,
    		ArrayList<AxeObject> axeArr,
    		ArrayList<PowerObject> powerArr) { //laser shooting code
    	if (laserTime == 0) {
    		mySoundSystem.quickPlay( true, "Laser.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    		laser = new Polygon (new float[] {
    				p.getCenterX(), p.getCenterY()+9,
    				p.getCenterX()+dir*laserLength, p.getCenterY()+9, 
    				p.getCenterX()+dir*laserLength, p.getCenterY()+11, 
    				p.getCenterX(), p.getCenterY()+11
    				}); //end laser polygon
    		laserDir = dir;
    	} //end initializing laser
    	
    	laser.setX(laser.getX()+laserSpeed*laserDir*d);
    	
    	for (PlayerObject playerOb : playerArr) {
    		if (playerOb.alive() && laser.intersects(playerOb.p)) {
//    			laserDir = 0;
    			playerOb.damage(laserDamage);
    			teamKill = true;
    		} //end player collision code
    		else if (playerOb.alive() && playerOb.swing && (laser.intersects(playerOb.sabre) || laser.intersects(playerOb.sabreLeft))) {
    			laserDir = playerOb.dir;
    		}
    	} //end player iterator
    	
//    	for (staticObject staticOb : staticArr) {
//    		if (laser.intersects(staticOb.p)) {
////    			laserDir = 0;
//    		} //end static object collision code
//    	} //end static object iterator
    	
    	for (EnemyObject enemyOb : enemyArr) {
    		if (laser.intersects(enemyOb.p) && enemyOb.alive()) {
    			EnemyHellBat hellBatOb = null;
        		if (enemyOb.enemyType.equals("hellbat")) {
        			hellBatOb = (EnemyHellBat)enemyOb;
        			if (!stunnedBat) {
        				mySoundSystem.quickPlay( true, "VengeBatSwat.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
        				hellBatOb.stun(dir, sabreDamage, playerIndex);
        				stunnedBat = true;
        			}
        		}
    			else {
    				enemyOb.damage(sabreDamage);
    			}
        		
//    			laserDir = 0;
    			if (!enemyOb.alive()) {
    				//score += enemyOb.pointValue;
    				addScore(enemyOb.pointValue);
    			} //end score adding if enemy is killed
    		} //end enemy collision code
    	} //end enemy iterator
    	
    	for (PowerObject powerOb : powerArr) {
    		if (laser.intersects(powerOb.p)) {
//    			laserDir = 0;
    			mySoundSystem.quickPlay( true, "BoxRicochet2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    			powerOb.vel.x = dir*laserSpeed/2;
    			powerOb.vel.y = -10;
    		} //end powerup code
    	} //end powerup iterator
    	
    	//axe collision code
    	for (AxeObject axeOb : axeArr) {
    		if (laser.intersects(axeOb.p)) {
    			mySoundSystem.quickPlay( true, "AxeRicochetAlt.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    			axeOb.dir = dir;
    			axeOb.vel.x = axeOb.velX*axeOb.dir;
    		}
    	}
    	
    	laserTime++;
    	
    	if (laser.getX() < 0 || laser.getX() > 800) {
    		laserDir = 0;
    	}
    	
    	//laser ending
    	if (laserDir == 0) {
    		laser = new Polygon();
    		laserTime = 0;
    		shoot = false;
    		interact = false;
    		laserNum--;
    		stunnedBat = false;
    	} //end laser decrement
    	
    } //end shooting code
    
    @Override
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
    		dJumpNum = 0;
    		laserNum = 0;
    		respawnTime = revivalTime;
    	}
    }
    
    public void powerUp(PowerObject pow, ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr) {
    	mySoundSystem.quickPlay( true, "Powerup.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    	if (pow.index == 1) { //if double jump powerup
    		dJumpNum += pow.dJumpNum;
    		laserNum = 0;
    		if (dJumpNum > pow.dJumpNum*2) {
    			dJumpNum = pow.dJumpNum*2;
    		}
    	} //end doublejump
    	else if (pow.index == 0) { //if lasergun powerup
    		laserNum += pow.laserNum;
    		dJumpNum = 0;
    		if (laserNum > pow.laserNum * 2) {
    			laserNum = pow.laserNum * 2;
    		}
    	} //end laser
    	else if (pow.index == 2) {
    		for (EnemyObject enemyOb : enemyArr) {
    			enemyOb.damage(sabreDamage);
    		}
    	}
    } //end powerup method
    
    public boolean alive() {
    	return health > 0;
    }
    
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
    		if (pos.x > 800-20-18 && laserNum > 0) {
    			pos.x = 800-20-18;
    		}
    		if (pos.x < 20+2 && laserNum > 0) {
    			pos.x = 20+2;
    		}    		
    		
    		//    	System.out.println("pre=" + prevPos + ", pos=" + pos);
    		xTest = new Polygon(new float[]{pos.x,prevPos.y,pos.x+l,prevPos.y,pos.x+l,prevPos.y+h,pos.x,prevPos.y+h});
    		yTest = new Polygon(new float[]{prevPos.x,pos.y,prevPos.x+l,pos.y,prevPos.x+l,pos.y+h,prevPos.x,pos.y+h});

    		velVector = new Line(prevPos.x, prevPos.y, pos.x, pos.y);    	
    		
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
    			mySoundSystem.quickPlay( true, "ManWalk2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    		}
    		
    		if(Math.abs(vel.y)<Math.abs(prevVelY)&&prevVelY>0&&justJumping){
    			justJumping = false;
    			stepCounter=500;
    			mySoundSystem.quickPlay( true, "ManWalk2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
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
    		} else if (mJump && doubleJump && dJumpNum > 0) {
    			
    			mySoundSystem.quickPlay( true, "ManDJump.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    			
    			mJump = !mJump;
    			doubleJump = false;
    			jump();
    			dJumpNum--;
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
    		vel.y += acc.y * d;
    		if (vel.x > horizVel)
    			vel.x = horizVel;
    		if (vel.x < -horizVel)
    			vel.x = -horizVel;
    		//    	pos.x = p.getX();
    		//    	pos.y = p.getY();
    		
    		//updating sword position
    		float xDelta = pos.x - p.getX();
    		float yDelta = pos.y - p.getY();
    		p.setLocation(pos.x, pos.y);
    		sabre.setX(sabre.getX()+xDelta);
    		sabre.setY(sabre.getY()+yDelta);
    		sabreLeft.setX(sabreLeft.getX()+xDelta);
    		sabreLeft.setY(sabreLeft.getY()+yDelta);
    		
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
			if (((!heal && interact && !shoot && laserNum <= 0) || swing) && coolDown<=0) {
				swing = true;
				swingsabre(playerArr, enemyArr, powerArr, axeArr);
			} //end swinging code
    		
			if(coolDown>0)
				coolDown--;
			
    		//to shoot!
    		if (!heal && interact && laserNum > 0 || shoot) {
    			shoot = true;
    			shootLaser(d, arr, playerArr, enemyArr, axeArr, powerArr);
    		} //end shooting code
			
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
    	pos.x -= 2;
    	int lnum = 0;
    	if (laserNum > 0) {
    		lnum = 1;
    	} //if lasermode
    	else if (dJumpNum > 0 || !doubleJump) {
    		lnum = 2;
    	}
    	if (alive()) {
    		if (sabreTime>0){
    			sprite.startUse();
    			if(dir==1){
    				if(lnum==2){
    					sprite.renderInUse((int)pos.x, (int)pos.y, 8,2);
    				} else {
	    				sprite.renderInUse((int)pos.x, (int)pos.y, 8,0);
	    			}
    			}else if (dir==-1){
    				if(lnum==2){
    					sprite.renderInUse((int)pos.x, (int)pos.y, 9,2);
    				} else {
	    				sprite.renderInUse((int)pos.x, (int)pos.y, 9,0);
	    			}
    			}
    			sprite.endUse();
    		}else if (grounded) {
	    		if(mRight && !mLeft){
	    			if (lnum == 0) {
	    				walkRightAnim.draw((int)pos.x,(int)pos.y);
	    			}
	    			else if (lnum == 1) {
	    				walkRightAnimLaser.draw((int)pos.x,(int)pos.y);
	    			}
	    			else if (lnum == 2) {
	    				walkRightAnimJump.draw((int)pos.x,(int)pos.y);
	    			}
	    		}else if(mLeft && !mRight){
	    			if (lnum == 0) {
	    				walkLeftAnim.draw((int)pos.x, (int)pos.y);
	    			}
	    			else if (lnum == 1) {
	    				walkLeftAnimLaser.draw((int)pos.x,(int)pos.y);
	    			}
	    			else if (lnum == 2) {
	    				walkLeftAnimJump.draw((int)pos.x,(int)pos.y);
	    			}
	    		}else{
	    			sprite.startUse();
	    			if (dir > 0) {
		    			sprite.renderInUse((int)pos.x, (int)pos.y, 3, lnum);
	    			} else if (dir < 0) {
	    				sprite.renderInUse((int)pos.x, (int)pos.y, 1, lnum);
	    			}
	    			sprite.endUse();
	    		}
	    	}else{
	    		if (doubleJump) {
		    		sprite.startUse();
		    		if (dir == 1) {
		    				sprite.renderInUse((int)pos.x, (int)pos.y, 7, lnum);
		    			
		    		} else if (dir == -1) {
	    				sprite.renderInUse((int)pos.x, (int)pos.y, 6, lnum);
		    		}
		    		sprite.endUse();
	    		}
	    		else { //if double jumping
	    			sprite.startUse();
	    			if (dir == 1) {
	    				sprite.renderInUse((int)pos.x, (int)pos.y, 10, lnum);
	    			} else if (dir == -1) {
	    				sprite.renderInUse((int)pos.x, (int)pos.y, 11, lnum);
	    			}
	    			sprite.endUse();
	    		}
	    	} //end not grounded
	    	
	    	if (swing) {
	    		if (dir > 0) {
//	    			sabreSprite.startUse();
//	    			sabreSprite.renderInUse((int)(p.getCenterX()+4), (int)(p.getCenterY()+10), 0, playerIndex);
//	    			sabreSprite.endUse();
	    			g.draw(sabre);
	    		}
	    		else if (dir < 0) {
//	    			sabreSprite.startUse();
//	    			sabreSprite.renderInUse((int)(p.getCenterX()-4-10), (int)(p.getCenterY()+10), 1, playerIndex);
//	    			sabreSprite.endUse();
	    			g.draw(sabreLeft);
	    		}
	    	}
	    	if (heal) {
	    		g.fillOval(pos.x+l/2, pos.y-5, 5, 5);
	    	}
	    	
//	    	if (laserNum > 0) {
//	    		g.drawString("L: " + laserNum, p.getX(), p.getMinY()-15);
//	    	} else if (dJumpNum > 0) {
//	    		g.drawString("J: " + dJumpNum, p.getX(), p.getMinY()-15);
//	    	}
//	    	if (playerIndex == 0) {
//	    		System.out.println(pos.getTheta());
//	    		g.drawLine(0, 0, pos.x, pos.y);
//	    	}
//	    	g.drawString(""+score, pos.x + l/2, pos.y - 15);
    	} else {
    		g.setColor(Color.red);
    		g.drawRect(pos.x, pos.y, l, h);
    		g.setColor(Color.white);
//			g.drawString("P"+(playerIndex+1)+" respawn: "+respawnTime, 40, 610); //RESPAWN TIMER DISPLAY
    	}
    	if (shoot) {
    		g.setColor(new Color(255, 50, 255));
    		g.fillRect(laser.getX(), laser.getY(), laser.getWidth(), laser.getHeight());
    		g.setColor(Color.white);
    	}
    	pos.x += 2;
//    	g.setColor(Color.green);
//    	g.drawRect(p.getMinX(), p.getMinY(), l, h);
//    	g.drawOval(p.getCenterX(), p.getCenterY(), 5, 5);
//    	g.setColor(Color.white);
    	//Drawing powerup ammocounter
    	int ammoX=0;
    	if(playerIndex==1){
    		ammoX=1;
    	}else{
    		ammoX=781;
    	}
    	if(laserNum>0){
    		g.setColor(Color.black);
    		g.fillRect(ammoX, 1142-20*(laserNum), 18, 20*(laserNum));
    		ammoSprite.startUse();
    		ammoSprite.renderInUse(ammoX, 1142, 0, 0);
    		ammoSprite.endUse();
    		for(int i=0;i<laserNum;i++){
    			g.setColor(new Color(155, 25, 155));
    			g.fillRect(ammoX, 1122-i*20, 16,16);
    			g.setColor(new Color(255, 50, 255));
    			g.drawRect(ammoX, 1122-i*20, 16,16);
    		}

    	}
    	if(dJumpNum>0){
    		g.setColor(Color.black);
    		g.fillRect(ammoX, 1142-20*(dJumpNum), 18, 20*(dJumpNum));
    		ammoSprite.startUse();
    		ammoSprite.renderInUse(ammoX, 1142, 1, 0);
    		ammoSprite.endUse();
    		for(int i=0;i<dJumpNum;i++){
    			g.setColor(new Color(0,0,155));
    			g.fillRect(ammoX, 1122-i*20, 16, 16);
    			g.setColor(new Color(100,100,255));
    			g.drawRect(ammoX, 1122-i*20, 16, 16);
    		}
    	}
    	g.setColor(Color.white);
    	
    	
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