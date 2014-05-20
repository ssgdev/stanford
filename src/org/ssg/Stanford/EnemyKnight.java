package org.ssg.Stanford;
import java.util.ArrayList;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.Color;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class EnemyKnight extends EnemyObject {
	public Animation stunRightAnim, stunLeftAnim;
	public Image lStand, lStep, rStand, rStep;
	
	//sword attack variables
	public boolean swing;
	public Polygon sabre, sabreLeft;
	public float sabreTime;
	static int[][] sabreArray;
	static final int sabreDamage = 5;
	static final float sabreDuration = 3;
	static final float sabreOffset = 10;
	static final int swingCoolDown = 30;
	static final int stunCoolDown = 100;

	//movement/behavior variables
	public boolean charge;
	public final float chargeVel = 8.0f;
	public boolean inRange;
	public boolean idleMove;
	public boolean jump;
	public boolean stun;
	public int coolDown;
	public float moveParam;
	public Polygon jumpTest;
	public final int turnTime = 300;
	public float stepCounter;//Counts steps for walk anim and sound sync
	boolean walkAnimParity;
	
	public EnemyKnight(int a, int b, Image i, int d, SoundSystem mSS) {
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

		sprite.startUse();
		stunRightAnim =new Animation(new Image[]{sprite.getSubImage(0,1),sprite.getSubImage(0,0)}, 100, true);
		stunLeftAnim =new Animation(new Image[]{sprite.getSubImage(3,1),sprite.getSubImage(3,0)}, 100, true);
		lStand = sprite.getSubImage(3,0);
		lStep = sprite.getSubImage(5,0);
		rStand = sprite.getSubImage(0,0);
		rStep = sprite.getSubImage(2,0);
		sprite.endUse();
		deathFrames = 70;

		//enemy variables
		contactDamage = 0;
		collPlayer = true;
		collStatic = true;
		pointValue = 200;
		enemyType = "knight";

		//knight specific variables
		moveParam = turnTime;
		charge = false;
		jump = false;
		inRange = false;
		idleMove = false;
		swing = false;
		stun = false;
		
		mySoundSystem = mSS;
		
		stepCounter = 0;
		walkAnimParity = false;
	}

	@Override
	public void shiftDown(double d){
		pos.y+=d;
		sabre.setY((float) (sabre.getY()+d));
		sabreLeft.setY((float) (sabreLeft.getY()+d));
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

	@Override
	public void attack (ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr) {
		// TODO Auto-generated method stub
		mySoundSystem.quickPlay( true, "KnightSwing.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		
		if (sabreTime == 0) {
			sabreTime = sabreDuration;
		}
		boolean hit = false;
		for (PlayerObject playerOb : playerArr) {  //collision with players
			if (dir < 0) {
				if (sabreLeft.intersects(playerOb.p)) {
					hit = true;
				}
			}
			else if (dir > 0) {
				if (sabre.intersects(playerOb.p)) {
					hit = true;
				}
			}
			if (hit) {
				if (playerOb.alive()) {
					playerOb.damage(sabreDamage);
				} 
				hit = false;
			}
		}

		for (EnemyObject enemyOb : enemyArr) {
			hit = false;
			if (dir < 0) {
				if (sabreLeft.intersects(enemyOb.p)) {
					hit = true;
				}
			}
			else if (dir > 0) {
				if (sabre.intersects(enemyOb.p)) {
					hit = true;
				}
			}
			if (hit) {
				if (enemyOb.alive()) {
					if (enemyOb.enemyType.equals("hellbat")) {
						EnemyHellBat hellbatOb = (EnemyHellBat)enemyOb;
						if (hellbatOb.alive()) {
							hellbatOb.damage(hellbatOb.maxHealth);
						}
					}
					else {
						enemyOb.damage(sabreDamage);
					}
					hit = false;
				}
			}
		}
		sabreTime--;
		if (sabreTime <= 0) {
			swing = false;
			charge = false;
			sabreTime = 0;
			coolDown = swingCoolDown;
		}

	}

	@Override
	public void update(StateBasedGame sbg, int delta, ArrayList<StaticObject> arr,
			ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr,
			ArrayList<AxeObject> axeArr) {
		// TODO Auto-generated method stub

		if(grounded)
			stepCounter+=(Math.abs(vel.x)*delta);
		if(stepCounter>=2000){
			stepCounter-=2000;
			walkAnimParity = !walkAnimParity;
			mySoundSystem.quickPlay( true, "KnightWalk.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		}
		
		float d = delta/20;
		acc.y = grav;
		grounded = false;
		if (!charge && alive()) {
			moveParam--;
		}
		if (moveParam < 0) {
			dir = -dir;
			moveParam = turnTime;
		}
		Vector2f prevPos = new Vector2f(pos.x, pos.y);
		pos.x += vel.x * d;
		pos.y += vel.y * d;
		xTest = new Polygon(new float[]{pos.x,prevPos.y,pos.x+l,prevPos.y,pos.x+l,prevPos.y+h,pos.x,prevPos.y+h});
		yTest = new Polygon(new float[]{prevPos.x,pos.y,prevPos.x+l,pos.y,prevPos.x+l,pos.y+h,prevPos.x,pos.y+h});
		velVector = new Line(prevPos.x, prevPos.y, pos.x, pos.y);

		//special checker for jumping!
		jumpTest = p.copy();
		jumpTest.setX(pos.x);
		jumpTest.setY(pos.y);

		if (alive()) {

			if (swing || coolDown > 0) {
				mRight = false;
				mLeft = false;
			}
			else if (idleMove || charge) {
				if (dir > 0) {
					mRight = true;
					mLeft = false;
				}
				else if (dir < 0) {
					mRight = false;
					mLeft = true;
				}
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

			//presetting jump boolean
			jump = true;

		} //end if alive
		else {
			moveNo();
			mRight = false;
			mLeft = false;
		}

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
					System.out.println("bump");
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

				//SPECIAL CHECK FOR JUMPING!
				if (grounded && jumpTest.intersects(staticOb.p)) {
					jump = false;
				}

			} //end iterator for static objects
		} //end static collision code

		if (alive()) {

			if (jump && grounded) {
				mySoundSystem.quickPlay( true, "KnightJump.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				vel.y = -1 * jumpVel;
				grounded = false;
				jump = false;
			}
			else if (!grounded) {
				jump = false;
			}

			//player checking code!
			for (PlayerObject playerOb: playerArr) {
				if (playerOb.alive()) {
					//sword collision checking code!
					if (dir > 0) {
						if ((sabre.intersects(playerOb.p) && coolDown <= 0)
								|| (sabre.intersects(playerOb.sabre) && playerOb.swing && coolDown <= 0)
								|| (sabre.intersects(playerOb.sabreLeft) && playerOb.swing && coolDown <= 0)) {
							swing = true;
						}
					} //end if for right
					else if (dir < 0) {
						if ((sabreLeft.intersects(playerOb.p) && coolDown <= 0)
								|| (sabreLeft.intersects(playerOb.sabre) && playerOb.swing && coolDown <= 0) 
								|| (sabreLeft.intersects(playerOb.sabreLeft) && playerOb.swing && coolDown <= 0)) {
							swing = true;
						}
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
						if (sabre.intersects(enemyObj.p) && coolDown <= 0) {
							swing = true;
						}
					}
					else if (dir < 0) {
						if (sabreLeft.intersects(enemyObj.p) && coolDown <= 0) {
							swing = true;
						}
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

			//speed check!
			if (charge) {
				if (Math.abs(vel.x) > chargeVel) {
					if (dir > 0)
						vel.x = chargeVel;
					else if (dir < 0)
						vel.x = -chargeVel;
				}
			} //end if
			else { //if not charging
				if (Math.abs(vel.x) > horizVel) {
					if (dir > 0)
						vel.x = horizVel;
					else if (dir < 0)
						vel.x = -horizVel;
				}
			}

			//updating sword position
			float xDelta = pos.x - p.getX();
			float yDelta = pos.y - p.getY();
			p.setLocation(pos.x, pos.y);
			sabre.setX(sabre.getX()+xDelta);
			sabre.setY(sabre.getY()+yDelta);
			sabreLeft.setX(sabreLeft.getX()+xDelta);
			sabreLeft.setY(sabreLeft.getY()+yDelta);

			//if swinging
			if (swing && coolDown <= 0) {
				attack(playerArr, enemyArr);
			}

			if (coolDown > 0)
				coolDown--;

			if (coolDown <= 0) {
				stun = false;
			}

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
			if (stun) {
				//System.out.println("stunned!");		
				if (dir > 0) {
					stunRightAnim.draw((int)pos.x,(int)pos.y);
				} else if (dir < 0) {
					stunLeftAnim.draw((int)pos.x-15, (int)pos.y);
				}
			}
			else if (grounded && !swing){ //if on the ground and not swinging
				if(vel.x>.1){
					if(!walkAnimParity){
						rStand.draw((int)pos.x,(int)pos.y);
					}else{
						rStep.draw((int)pos.x,(int)pos.y);
					}
				}else if(vel.x<-.1){
					if(!walkAnimParity){
						lStand.draw((int)pos.x-15, (int)pos.y);
					}else{
						lStep.draw((int)pos.x-15, (int)pos.y);
					}
				}else{
					sprite.startUse();
					if (dir > 0) {
						sprite.renderInUse((int)pos.x, (int)pos.y, 0, 0);
					} else if (dir < 0) {
						sprite.renderInUse((int)pos.x-15, (int)pos.y, 3, 0);
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
					sprite.renderInUse((int)pos.x-15, (int)pos.y, 5, 0);
					//	    			}
				}
				sprite.endUse();
			} else if (swing) { //if swinging at all
				g.setColor(Color.yellow);
				if (dir > 0) {
					sprite.startUse();
					sprite.renderInUse((int)pos.x, (int)pos.y, 1, 0);
					sprite.endUse();
					g.draw(sabre);
				}
				else if (dir < 0) {
					sprite.startUse();
					sprite.renderInUse((int)pos.x-15, (int)pos.y, 4, 0);
					sprite.endUse();
					g.draw(sabreLeft);
				}
				g.setColor(Color.white);
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
					sprite.renderInUse((int)pos.x, (int)pos.y, 2, 1);
				} else if (dir < 0) {
					sprite.renderInUse((int)pos.x-15, (int)pos.y, 5, 1);
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
