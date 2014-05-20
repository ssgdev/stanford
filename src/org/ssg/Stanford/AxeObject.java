package org.ssg.Stanford;

import java.util.ArrayList;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig; 
import paulscode.sound.SoundSystemException; 
//import paulscode.sound.libraries.LibraryJavaSound; 
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.codecs.CodecWav;

public class AxeObject {
	public static final int damage = 5;
	public int frameCounter;
	public boolean deadly;
	public static final int velX = 20;
	public static final int velY = 3;
	public Image axeImage;
	public SpriteSheet axeSprite;
	public Animation leftAnim, rightAnim;

	// actual physical object stuff
	float grav = .3f;
	public Polygon p;
	public Vector2f pos;
	public Vector2f vel;
	public Vector2f acc;
	public int l;
	public int frames;
	public boolean collH, collV;
	public Polygon xTest, yTest;
	public Line velVector;
	public int dir;
	public boolean blue;//Was this a player spawned axe?
	boolean justPlayed;//So that you don't play the ricochet for both x and y
	boolean stunnedBat;
	int hitter;
	final int NO_HITTER = -99;
	
	SoundSystem mySoundSystem;
	
	public AxeObject(int a, int b, int length, int d, Image i, SoundSystem mSS) {
		pos = new Vector2f(a, b);
		dir = d;
		vel = new Vector2f(d * velX, -velY);
		acc = new Vector2f(0, 0);
		velVector = new Line(0, 0);
		xTest = new Polygon();
		yTest = new Polygon();
		l = length;
		p = new Polygon(new float[] { pos.x, pos.y, pos.x + l, pos.y,
				pos.x + l, pos.y + l, pos.x, pos.y + l });
		collH = false;
		collV = false;
		frameCounter = 80;
		deadly = true;
		frames = 0;
		axeImage = i;
		axeSprite = new SpriteSheet(i, 20, 20);
		rightAnim = new Animation(new Image[] { axeSprite.getSubImage(0, 0),
				axeSprite.getSubImage(1, 0), axeSprite.getSubImage(2, 0),
				axeSprite.getSubImage(3, 0) }, 200, true);
		leftAnim = new Animation(new Image[] { axeSprite.getSubImage(0, 1),
				axeSprite.getSubImage(1, 1), axeSprite.getSubImage(2, 1),
				axeSprite.getSubImage(3, 1) }, 200, true);
		justPlayed = false;
		blue = false;
		mySoundSystem = mSS;
		hitter = NO_HITTER;
	} // end constructor
	
	public AxeObject(int a, int b, int length, int d, Image i, SoundSystem mSS, boolean bool, int h) {
		pos = new Vector2f(a, b);
		dir = d;
		vel = new Vector2f(d * velX, -velY);
		acc = new Vector2f(0, 0);
		velVector = new Line(0, 0);
		xTest = new Polygon();
		yTest = new Polygon();
		l = length;
		p = new Polygon(new float[] { pos.x, pos.y, pos.x + l, pos.y,
				pos.x + l, pos.y + l, pos.x, pos.y + l });
		collH = false;
		collV = false;
		frameCounter = 80;
		deadly = true;
		frames = 0;
		axeImage = i;
		axeSprite = new SpriteSheet(i, 20, 20);
		rightAnim = new Animation(new Image[] { axeSprite.getSubImage(0, 0),
				axeSprite.getSubImage(1, 0), axeSprite.getSubImage(2, 0),
				axeSprite.getSubImage(3, 0) }, 200, true);
		leftAnim = new Animation(new Image[] { axeSprite.getSubImage(0, 1),
				axeSprite.getSubImage(1, 1), axeSprite.getSubImage(2, 1),
				axeSprite.getSubImage(3, 1) }, 200, true);
		justPlayed = false;
		blue = false;
		mySoundSystem = mSS;
		hitter = h;
		blue = bool;
	} // end constructor
	public void shiftDown(double d) { // scrolling method
		pos.y += d;
		p.setY(pos.y);
	} // end shift method

	public void update(StateBasedGame sbg, int delta, ArrayList<StaticObject> arr,
			ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr, ArrayList<PowerObject> powerArr,
			ArrayList<AxeObject> axeArr) { // update
																					// method
		float d = delta / 20;
		if (deadly) {
			grav = .3f;
		} else {
			grav = 1f;
			stunnedBat = false;
		}
		acc.y = grav;
		if (!deadly) { // slower if broken
			acc.x = -vel.x / 2;
		} else {
			acc.x = 0;
		}

		Vector2f prevPos = new Vector2f(pos.x, pos.y);
		pos.x += vel.x * d;
		pos.y += vel.y * d;

		xTest = new Polygon(new float[] { pos.x, prevPos.y, pos.x + l,
				prevPos.y, pos.x + l, prevPos.y + l, pos.x, prevPos.y + l });
		yTest = new Polygon(new float[] { prevPos.x, pos.y, prevPos.x + l,
				pos.y, prevPos.x + l, pos.y + l, prevPos.x, pos.y + l });
		// xTest = new Circle(pos.x, prevPos.y, l);
		// yTest = new Circle(prevPos.x, pos.y, l);
		velVector = new Line(prevPos.x, prevPos.y, pos.x, pos.y);

		collH = false;
		collV = false;

		if (deadly) {
			// collision code for players!
			for (PlayerObject playerOb : playerArr) {
				if (p.intersects(playerOb.p)) {
					if (playerOb.alive()) {
						playerOb.damage(damage);
						if(blue){
							//playerArr.get((playerOb.playerIndex+1)%2).setTeamkill(true);
							if(playerOb.getPlayerIndex()!=hitter)
								playerArr.get(hitter).setTeamkill(true);
						}
					}
				}
			}

			// collision code for enemies!
			for (EnemyObject enemyOb : enemyArr) {
				if (p.intersects(enemyOb.p)) {
					if (enemyOb.alive()) {
						if (blue && enemyOb.enemyType.equals("hellbat")) {
		    				if (!stunnedBat) {
		    					if(!((EnemyHellBat)enemyOb).stun)
		    						mySoundSystem.quickPlay( true, "VengeBatSwat.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		        				((EnemyHellBat)enemyOb).stun(dir, 10, 1);//1 is the playerindex
		        				stunnedBat = true;
		        			}
		    			}else{
		    				enemyOb.damage(damage);
		    			}
						if(blue){
							playerArr.get(hitter).addScore(enemyOb.pointValue);
						}else if(hitter != NO_HITTER){
							playerArr.get(hitter).addScore(enemyOb.pointValue);
						}
					}
				}
			}
		}

		justPlayed = false;//Stops it from playing for both x and y collisions on one collision 
		// COLLISION CODE FOR BLOCKS
		for (StaticObject staticOb : arr) { // collision with blocks
			if (xTest.intersects(staticOb.p) && !collH) {
				if(deadly){
					mySoundSystem.quickPlay( true, "AxeRicochetAlt.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}
				justPlayed=true;
				vel.x = -vel.x / 2;
				pos.x = prevPos.x;
				collH = true;
				dir = -dir;
			}
			if (yTest.intersects(staticOb.p) && !collV) {
				if(!justPlayed && deadly){
					mySoundSystem.quickPlay( true, "AxeRicochetAlt.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}
				staticOb.collided();
				collV = true;
				if (pos.y > prevPos.y) {
					pos.y = staticOb.p.getY() - l - 1;
					acc.x = -vel.x / 5;
					if (deadly) {
						vel.y = -vel.y / 2;
					} else {
						vel.y = -vel.y / 50;
					}
				} else if (pos.y < prevPos.y) {
					// pos.y = prevPos.y;
					vel.y = 0;
					pos.y = staticOb.p.getY() + staticOb.l + 2;
					prevPos.y = pos.y;
				}
			}
			if (p.intersects(staticOb.p)) { // are you already stuck in a
											// block???
				float xDiff = p.getCenterX() - staticOb.p.getCenterX();
				float yDiff = p.getCenterY() - staticOb.p.getCenterY();
				//System.out.println("axebump");
				staticOb.collided();
				if (Math.abs(xDiff) < Math.abs(yDiff)) {
					if (xDiff < 0)
						pos.x -= 2;
					else
						pos.x += 2;
				} else {
					if (yDiff < 0)
						pos.y -= 2;
					else
						pos.y += 2;
				}
			}
		} // end static object collision
		justPlayed = false;
		
		//Collisions for powerups
		boolean hit = false;
		if(deadly){
			for (PowerObject powerOb : powerArr) {
	    		hit = false;
	    		if (vel.x < 0) { //checking for left
					if (p.intersects(powerOb.p)) { //checking for hitting powerUp
						mySoundSystem.quickPlay( true, "BoxRicochet2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
						hit = true;
						powerOb.vel.x = -15*(-vel.x/20);
						powerOb.vel.y = -10;
					} //end sabreleft check
	    		} //end left check
	    		else if (vel.x > 0) {
	    			if (p.intersects(powerOb.p)) { //checking for hitting powerUp
	    				mySoundSystem.quickPlay( true, "BoxRicochet2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
	    				hit = true;
	    				powerOb.vel.x = 15*(vel.x/20);
	    				powerOb.vel.y = -10;
	    			} //end sabreright check
	    		} //end right check
	    	} //end powerup check
		}
		// movement updates
		vel.x += acc.x * d;
		vel.y += acc.y * d;

		if (Math.abs((int) vel.x) <= 3) {
			deadly = false;
			frameCounter--;
		} else {
			frames++;
		}

		p.setLocation(pos.x, pos.y);
	}

	public void render(Graphics g) { // render method
		if (deadly) {
			if (dir > 0) {
				rightAnim.draw((int) pos.x, (int) pos.y);
			} else if (dir < 0) {
				leftAnim.draw((int) pos.x, (int) pos.y);
			}
		} // end if deadly
		else {
			axeSprite.startUse();
			if (frameCounter > 30) {
				axeSprite.renderInUse((int) pos.x, (int) pos.y, 4, 0);
			} else if (frameCounter > 0) {
				axeSprite.renderInUse((int) pos.x, (int) pos.y, 4, 1);
			} else {

			}
			axeSprite.endUse();
		}
	} // end rendering method
	
	public void setHitter(int h){
		hitter = h;
	}
	
	public void setSoundSystem(SoundSystem ss){
		mySoundSystem=ss;
	}
	
	
} // end class
