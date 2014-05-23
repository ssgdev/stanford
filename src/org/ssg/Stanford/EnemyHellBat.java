package org.ssg.Stanford;
import java.util.ArrayList;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class EnemyHellBat extends EnemyObject {
	public float moveParam, moveCount;
	public Animation anim, stunAnim;
	public PlayerObject victim;
	public static final int maxHealth = 1;
	public double theta;
	public double radius;
	public Vector2f victimVector;
	public boolean stun;
	public boolean inLine;
	public boolean collV, collH;
	public int stunCountDown, yDir, yLine;
	public int lastHitIndex;
	public static final int stunTime = 100;
	public static final int turnTime = 10;
	
	//lunging mechanics
	public float lungeTimer;
	public int missTimer;
	public static final int lungeTime = 100;
	public static final int missTime = 100;
	public Vector2f lungeVector;
	public static final int lungeVel = 8;
	
	public EnemyHellBat(int a, int b, Image i, int d, PlayerObject v, SoundSystem mSS) {
		//static variables that are final in playerObject
		l = 20;
		h = 20;
		grav = 1.0f;
		horizVel = 3.0f;
		horizAcc = 0.5f;
		jumpVel = 3.0f;
		jumpAcc = 1.0f;
		
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
        health = maxHealth;
        dir = d;
        
        sprite = new SpriteSheet(i,20,20);

        p = new Polygon(new float[]{pos.x,pos.y,pos.x+l,pos.y,pos.x+l,pos.y+h,pos.x,pos.y+h});
        p.setClosed(true);
        
        sprite.startUse();
        anim = new Animation(new Image[]{sprite.getSubImage(0,0),sprite.getSubImage(1,0)}, 100, true);
        stunAnim = new Animation(new Image[]{sprite.getSubImage(2,0),sprite.getSubImage(3,0)}, 100, true);
        sprite.endUse();
        deathFrames = 20;
        
        //enemy variables
        contactDamage = 5;
        collPlayer = true;
        collStatic = false;
        pointValue = 0;
        enemyType = "hellbat";
        
        //hellbat specific variables
        moveParam = 0;
        moveCount = 0;
        victim = v;
        victimVector = new Vector2f(victim.p.getCenter());
        radius = pos.distance(victim.pos);
        theta = 0;
        yDir = 0;
        inLine = false;
        stunCountDown = 0;
        yLine = (int)p.getCenterY();
        lastHitIndex = -1;
        collV = false;
        collH = false;
        lungeTimer = 0;
        missTimer = 0;
        lungeVector = new Vector2f();
        
        mySoundSystem = mSS;
	}

	@Override
	public void moveLeft() {
		// TODO Auto-generated method stub
		acc.x = -horizAcc;
		if (inLine) {
			vel.y = -jumpAcc*(float)Math.sin(moveParam) - yDir*2;
		} else {
			vel.y = -jumpVel*(float)Math.sin(moveParam) - yDir*2;
		}
	}

	@Override
	public void moveRight() {
		// TODO Auto-generated method stub
		acc.x = horizAcc;
		if (inLine) {
			vel.y = -jumpAcc*(float)Math.sin(moveParam) - yDir*2;
		} else {
			vel.y = -jumpVel*(float)Math.sin(moveParam) - yDir*2;
		}
	}

	@Override
	public void attack(ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr) {
		// TODO Auto-generated method stub
		
	}
	
	public void stun(int x, int d, int i) {
		stun = true;
		stunCountDown = stunTime;
		lungeTimer = 0;
		missTimer = 0;
		vel.x = x * 10;
		acc.x = 0;
		acc.y = 0;
		vel.y = -vel.y;
		contactDamage = 0;
	}
	
	@Override
	public void shiftDown(double d){
        pos.y+=d;
        p.setY(pos.y);
        yLine += d;
    }

	@Override
	public void update(StateBasedGame sbg, int delta, ArrayList<StaticObject> arr,
			ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr,
			ArrayList<AxeObject> axeArr) {
		// TODO Auto-generated method stub
		if (alive()) {
			float d = delta/20;
		
            if (stun) {
    			stunCountDown--;
//    			acc.y = grav;
    	    	acc.x = -vel.x/40;
//    	    	collStatic = true;
    		}
            else {
            	if (missTimer > 0 && lungeTimer < 0) {
            		missTimer--;
            		acc.x = -vel.x/(200-199*Math.abs((victimVector.x-400)/400));
            		acc.y = -vel.y/(200-199*Math.abs((victimVector.y-1200)/600));
            	}
            	else {
            		if (missTimer <= 0) {
            			mySoundSystem.quickPlay( true, "VengeBatSpawn.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
            			//mySoundSystem.quickStream(true, "VengeBatSpawn.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f);
            			victimVector.set(victim.p.getCenter());
                		lungeVector.set(victimVector.x - pos.x, victimVector.y - pos.y);
                		lungeTimer = pos.distance(victimVector)/lungeVel*d+5;
                		theta = Math.toRadians(lungeVector.getTheta());
                		missTimer = missTime;
                	}
            		else {
	            		vel.x = (float) (lungeVel*Math.cos(theta));
	            		vel.y = (float) (lungeVel*Math.sin(theta));
	            		lungeTimer--;
            		}
            	}
            	
            	
//        		xDiff = p.getCenterX() - victimVector.x;
//        		
//        		//delaying hellbat turn around horizontally
//        		moveCount++;
//    			moveParam += Math.PI / 90;
//    			if (moveCount == 45 || moveCount == 135) { //checking location stuff around middle height
//    				if (yDir == 0) {
//    					inLine = true;
//    				}
//    				else {
//    					yLine = (int)p.getCenterY();
//    				}
//    				if (xDiff > 0) {
//    					dir = -1;
//    				}
//    				else if (xDiff < 0) {
//    					dir = 1;
//    				}
//    			}
//    			if (moveCount > 179) {
//    				moveCount = 0;
//    			}
//        		
//                yDiff = yLine - victimVector.y;
//                if (yDiff > 0) {
//                	yDir = 1;
//                	inLine = false;
//                }
//                else if (yDiff < 0) {
//                	yDir = -1;
//                	inLine = false;
//                }
//                else {
//                	yDir = 0;
//                }
//                yLine -= yDir*2;
//                radius = pos.distance(victim.pos);
//                vector.set(xDiff, yDiff);
//                theta = vector.getTheta();
//                
//            	if (dir > 0) {
//    				moveRight();
//    			}
//    			else if (dir < 0) {
//    				moveLeft();
//    			}
            }
            
            Vector2f prevPos = new Vector2f(pos.x, pos.y);
    		pos.x += vel.x * d;
    		pos.y += vel.y * d;
            
    		//COLLISION CODE FOR BLOCKS
    		
    		if (stun && collStatic) {
    			
	    		xTest = new Polygon(new float[]{pos.x,prevPos.y,pos.x+l,prevPos.y,pos.x+l,prevPos.y+h,pos.x,prevPos.y+h});
	    		yTest = new Polygon(new float[]{prevPos.x,pos.y,prevPos.x+l,pos.y,prevPos.x+l,pos.y+h,prevPos.x,pos.y+h});
	    		velVector = new Line(prevPos.x, prevPos.y, pos.x, pos.y);
	    		
	    		collH = false;
	    		collV = false;
	    		
	    		for (StaticObject staticOb : arr) { //  collision with blocks
	    			if (!p.intersects(staticOb.p)) { //are you NOT already stuck in a block???
	    				if (xTest.intersects(staticOb.p) && !collH) {
	    					vel.x = -vel.x/2;
	    					pos.x = prevPos.x;
	    					collH = true;
	    				}
	    				if (yTest.intersects(staticOb.p) && !collV) {
	    					staticOb.collided();
	    					collV = true;
	    					vel.y = 0;
	    					if (pos.y > prevPos.y) {
	    						pos.y = staticOb.p.getY()-l-1;
	    						acc.x = -vel.x/10;
	    					}
	    					else if (pos.y < prevPos.y) {
	    						//    				pos.y = prevPos.y;
	    						pos.y = staticOb.p.getY()+staticOb.l+2;
	    						prevPos.y = pos.y;
	    					}
	    				}
	    			}
	    			else {
//	    				vel.y = -1;
	    			}
	    		} //end iterator for static objects
	    		
    		} //end stunned code
    		
    		if (stunCountDown <= 0) {
    			stun = false;
    			collStatic = false;
    			stunCountDown = stunTime;
    			contactDamage = 5;
    			yLine = (int)p.getCenterY();
    			moveCount = 45;
    			moveParam = (float)(45*Math.PI/90);
    			inLine = false;
    			
    			if (pos.y > 1200) {
	    			pos.y = 1200;
	    			yLine = 1210;
	    		}
    			if (pos.x > 800) {
    				pos.x = 800;
    			}
    			if (pos.x < 0) {
    				pos.x = 0;
    			}
    		}
    		
    		//movement updates
    		vel.x += acc.x * d;
    		vel.y += acc.y * d;
    		if (!stun) { //speedchecking if actively tracking
	    		if (vel.x > horizVel) {
	    			vel.x = horizVel;
	    		} else if (vel.x < -horizVel) {
	    			vel.x = -horizVel;
	    		}
	    		
	    		if (vel.y > jumpVel) {
	    			vel.y = jumpVel;
	    		} else if (vel.y < -jumpVel) {
	    			vel.y = -jumpVel;
	    		}
    		}
    		else {
    			if (vel.x > 15) {
    				vel.x = 15;
    			} else if (vel.x < -15) {
    				vel.x = -15;
    			}
    			if (vel.y > 15) {
    				vel.y = 15;
    			}
    		}
    		
    		p.setLocation(pos.x, pos.y);
    		
		} //end if alive statement
		
	} //end method
	
	@Override
	public void render(Graphics g) {
		// TODO Auto-generated method stub
		if(alive()){
			if(stun){
				stunAnim.draw(pos.x,pos.y);
			}else{
				anim.draw(pos.x,pos.y);
			}
//			g.drawLine(0, yLine, 800, yLine);
//			g.setColor(Color.red);
//			g.drawRect(pos.x, pos.y, l, h);
//			g.drawLine(p.getCenterX(), p.getCenterY(), p.getCenterX()+lungeVector.x, p.getCenterY()+lungeVector.y);
//			g.setColor(Color.white);
		}else{
//			g.drawString(""+pointValue, pos.x + l/2, pos.y - 10);
			stunAnim.draw(pos.x,pos.y);
			deathFrames--;
		}
	}
	
	@Override
    public void damage(int d) {
		mySoundSystem.quickPlay( true, "BatDeath2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    	health -= d;
    }
	
	
}