package org.ssg.Stanford;
import java.util.ArrayList;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.Color;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class EnemyAxeKnight extends EnemyObject {
	public Animation walkLeftAnim, walkRightAnim, stunRightAnim, stunLeftAnim;

	//sword axe variables
	public boolean swing;
	public int swingTime;
	static final int swingCoolDown = 100;

	//movement/behavior variables
	public boolean charge;
	public boolean inRange;
	public int coolDown;
	public float moveParam;
	public final int turnTime = 500;

	public EnemyAxeKnight(int a, int b, Image i, int d, SoundSystem mSS) throws SlickException {
		//static variables that are final in playerObject
		l = 30;
		h = 50;
		grav = 1.0f;
		horizVel = 1.0f;
		horizAcc = 0.3f;
		jumpVel = 10.0f;
		jumpAcc = 0.0f;

		//variables from playerObject
		pos = new Vector2f (a, b);
		vel = new Vector2f (0, 0);
		acc = new Vector2f (0, 0);
		velVector = new Line(0,0);
		grounded = false;
		mRight = false;
		mLeft = false;
		mJump = false;
		xTest = new Polygon();
		yTest = new Polygon();
		health = 5;
		dir = d;

		sprite = new SpriteSheet(i,46,50);

		p = new Polygon(new float[]{pos.x,pos.y,pos.x+l,pos.y,pos.x+l,pos.y+h,pos.x,pos.y+h});
		p.setClosed(true);

		sprite.startUse();
		walkLeftAnim = new Animation(new Image[]{sprite.getSubImage(3,0),sprite.getSubImage(5,0)}, 100, true);
		walkRightAnim =new Animation(new Image[]{sprite.getSubImage(0,0),sprite.getSubImage(2,0)}, 100, true);
		sprite.endUse();
		deathFrames = 70;

		//enemy variables
		contactDamage = 0;
		collPlayer = true;
		collStatic = true;
		pointValue = 200;
		enemyType = "axeKnight";

		//knight specific variables
		moveParam = turnTime;
		charge = false;
		inRange = false;
		swing = false;
		coolDown = 0;
		swingTime = 0;

		mySoundSystem = mSS;
	}

	@Override
	public void shiftDown(double d){
		pos.y+=d;
		p.setY(pos.y);
	}

	@Override
	public void moveLeft() {
		// TODO Auto-generated method stub
		acc.x = -horizAcc;
	}

	@Override
	public void moveRight() {
		// TODO Auto-generated method stub
		acc.x = horizAcc;
	}

	public void moveNo() {
		acc.x = -vel.x/2;
	}

	public void jump() {
		vel.y = -jumpVel;
	}

	public void attack (StateBasedGame sbg, ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr, ArrayList<AxeObject> axeArr) {
		mySoundSystem.quickPlay( true, "KnightSwing.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		// TODO Auto-generated method stub
		if (dir < 0) {
			axeArr.add(new AxeObject((int)p.getMinX()-2-20, (int)p.getMinY(), 20, dir, ((Stanford)sbg).axeSheet, mySoundSystem));
		}
		else if (dir > 0) {
			axeArr.add(new AxeObject((int)p.getMaxX()+2, (int)p.getMinY(), 20, dir, ((Stanford)sbg).axeSheet, mySoundSystem));
		}
		swing = false;
		charge = false;
		swingTime = swingCoolDown;
		coolDown = swingCoolDown;
	}

	public void update(StateBasedGame sbg, int delta, ArrayList<StaticObject> arr,
			ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr, ArrayList<AxeObject> axeArr) {
		// TODO Auto-generated method stub

		float d = delta/20;
		acc.y = grav;
		grounded = false;
		charge = false;
		if (!charge && alive()) {
			moveParam--;
		}
		if (moveParam < 0) {
			dir = -dir;
			moveParam = turnTime;
			charge = false;
			swing = false;
		}
		Vector2f prevPos = new Vector2f(pos.x, pos.y);
		pos.x += vel.x * d;
		pos.y += vel.y * d;
		xTest = new Polygon(new float[]{pos.x,prevPos.y,pos.x+l,prevPos.y,pos.x+l,prevPos.y+h,pos.x,prevPos.y+h});
		yTest = new Polygon(new float[]{prevPos.x,pos.y,prevPos.x+l,pos.y,prevPos.x+l,pos.y+h,prevPos.x,pos.y+h});
		velVector = new Line(prevPos.x, prevPos.y, pos.x, pos.y);

		//COLLISION CODE FOR BLOCKS
		if (collStatic) {
			for (StaticObject staticOb : arr) { //  collision with blocks
				if (xTest.intersects(staticOb.p)) {
					pos.x = prevPos.x;
					if (vel.x > 0) {
						moveLeft();
						dir = -1;
						charge = false;
						mRight = false;
						vel.x = 0;
						//	    					coolDown = stunCoolDown;
					}
					else if (vel.x < 0) {
						moveRight();
						dir = 1;
						charge = false;
						mLeft = false;
						vel.x = 0;
						//	    					coolDown = stunCoolDown;
					}
				}
				if (yTest.intersects(staticOb.p)) {
					vel.y = 0;
					if (pos.y > prevPos.y) {
						grounded = true;
						pos.y = staticOb.p.getY()-h-1;
					}
					else if (pos.y < prevPos.y) {
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

			} //end iterator for static objects
		} //end static collision code

		if (alive()) {

			//player checking code!
			for (PlayerObject playerOb: playerArr) {
				if (playerOb.alive()) {
					//sword collision checking code!
					if (dir > 0) {
						
					} //end if for right
					else if (dir < 0) {
						
					} //end else if for left

					//charge checking code
					if (p.getCenterY() > playerOb.p.getMinY() && p.getCenterY() < playerOb.p.getMaxY() && !charge && grounded) {
						if (dir > 0 && playerOb.p.getX() > p.getX()) {
							charge = true;
						} //end if
						else if (dir < 0 && playerOb.p.getX() < p.getX()) {
							charge = true;
						} //end else if
					} //end charge checking code for players

				} //end if player is alive

			} //player checking code

			//enemy checking code!
			for (EnemyObject enemyObj: enemyArr) {
				if (enemyObj.alive()) {
					//sword collision checking code!
					if (dir > 0) {
						
					}
					else if (dir < 0) {
						
					}

					//charge checking code
					if ((p.getCenterY() > enemyObj.p.getMinY()) 
							&& grounded
							&& (p.getCenterY() < enemyObj.p.getMaxY())
							&& (!charge)
							//							&& (enemyObj.enemyType.equals("knight"))
							&& p.getY() > 600
							&& (enemyObj.p != p)) {
						if (dir > 0 && enemyObj.p.getX() > p.getX() && enemyObj.p.getX() < 800) {
							charge = true;
//							System.out.println("charge @ y = " + p.getY());
						} //end if
						else if (dir < 0 && enemyObj.p.getX() < p.getX()  && enemyObj.p.getX() > 0) {
							charge = true;
//							System.out.println("charge @ y = " + p.getY());
						} //end else if
					} //end charge checking code for enemies
				} //end if enemy is alive

			} //end enemy checking

			//updating sword position
			float xDelta = pos.x - p.getX();
			float yDelta = pos.y - p.getY();
			p.setLocation(pos.x, pos.y);

			if (charge && coolDown <= 0) {
				swing = true;
			}
			
			//if swinging
			if (swing && coolDown <= 0) {
				attack(sbg, playerArr, enemyArr, axeArr);
			}

			if (coolDown > 0)
				coolDown--;
			
			if (swingTime > 0)
				swingTime--;

		} //end if alive statement

		//velocity updates
		vel.x += acc.x * d;
		vel.y += acc.y * d;

		p.setX(pos.x);
		p.setY(pos.y);

		if(pos.x< -100 || pos.x> 900){
			health=-99;
		}

		//Checking for dropdown death
		if (p.getMinY() > 1200) {
			health-=1000;
			deathFrames = -1;
		}

		//		System.out.println(pos);
	} //end method

	@Override
	public void render(Graphics g) {
		// TODO Auto-generated method stub
		if (alive()) {
			if (coolDown > swingCoolDown/2) { //if swinging at all
				g.setColor(Color.yellow);
				if (dir > 0) {
					sprite.startUse();
					sprite.renderInUse((int)pos.x, (int)pos.y, 1, 0);
					sprite.endUse();
				}
				else if (dir < 0) {
					sprite.startUse();
					sprite.renderInUse((int)pos.x-15, (int)pos.y, 4, 0);
					sprite.endUse();
				}
				g.setColor(Color.white);
			} //end if swinging
			else if (grounded){ //if on the ground and not swinging
				if(vel.x>.1){
					walkRightAnim.draw((int)pos.x,(int)pos.y);
				}else if(vel.x<-.1){
					walkLeftAnim.draw((int)pos.x-15, (int)pos.y);
				}else{
					sprite.startUse();
					if (dir > 0) {
						sprite.renderInUse((int)pos.x, (int)pos.y, 0, 0);
					} else if (dir < 0) {
						sprite.renderInUse((int)pos.x-15, (int)pos.y, 3, 0);
					}
					sprite.endUse();
				}
			}
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
					sprite.renderInUse((int)pos.x-15, (int)pos.y, 3, 1);
				}
			} else if (deathFrames > 20) {
				deathFrames--;
				if (dir > 0) {
					sprite.renderInUse((int)pos.x, (int)pos.y, 1, 1);
				} else if (dir < 0) {
					sprite.renderInUse((int)pos.x-15, (int)pos.y, 4, 1);
				}
			} else {
				if (dir > 0) {
					sprite.renderInUse((int)pos.x, (int)pos.y, 5, 1);
				} else if (dir < 0) {
					sprite.renderInUse((int)pos.x-15, (int)pos.y, 2, 1);
				}
			} //end deathFrames bit
			sprite.endUse();
		}
		//		g.draw(sabre);
		//    	g.setColor(Color.green);
		//    	g.drawRect(p.getMinX(), p.getMinY(), l, h);
		//    	g.drawOval(p.getCenterX(), p.getCenterY(), 5, 5);
		//    	g.setColor(Color.white);
	}
	
	@Override
    public void damage(int d) {
		mySoundSystem.quickPlay( true, "KnightDeath.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    	health -= d;
    }

}
