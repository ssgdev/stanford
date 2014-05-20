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

public class EnemyBat extends EnemyObject {
	public float moveParam;
	public Animation anim, deathAnim;
	
	public EnemyBat(int a, int b, Image i, int d, SoundSystem mSS) {
		//static variables that are final in playerObject
		l = 20;
		h = 20;
		grav = 1.0f;
		horizVel = 3.0f;
		horizAcc = 8.0f;
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
        health = 1;
        dir = d;
        
        sprite = new SpriteSheet(i,20,20);

        p = new Polygon(new float[]{pos.x,pos.y,pos.x+l,pos.y,pos.x+l,pos.y+h,pos.x,pos.y+h});
        p.setClosed(true);
        
        sprite.startUse();
        anim = new Animation(new Image[]{sprite.getSubImage(0,0),sprite.getSubImage(1,0)}, 100, true);
        deathAnim = new Animation(new Image[]{sprite.getSubImage(2,0),sprite.getSubImage(3,0)}, 100, true);
        sprite.endUse();
        deathFrames = 20;
        
        mySoundSystem = mSS;
        
        //enemy variables
        contactDamage = 5;
        collPlayer = true;
        collStatic = false;
        pointValue = 50;
        enemyType = "bat";
        
        //bat specific variables
        moveParam = 0;
	}

	@Override
	public void moveLeft() {
		// TODO Auto-generated method stub
		vel.x = -horizVel;
		vel.y = -jumpVel*(float)Math.sin(moveParam);
	}

	@Override
	public void moveRight() {
		// TODO Auto-generated method stub
		vel.x = horizVel;
		vel.y = -jumpVel*(float)Math.sin(moveParam);
	}

	@Override
	public void attack(ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(StateBasedGame sbg, int delta, ArrayList<StaticObject> arr,
			ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr,
			ArrayList<AxeObject> axeArr) {
		// TODO Auto-generated method stub
		if (alive()) {
			float d = delta/20;
			moveParam += .05f;
			Vector2f prevPos = new Vector2f(pos.x, pos.y);
    		pos.x += vel.x * d;
    		pos.y += vel.y * d;
    		xTest = new Polygon(new float[]{pos.x,prevPos.y,pos.x+l,prevPos.y,pos.x+l,prevPos.y+h,pos.x,prevPos.y+h});
    		yTest = new Polygon(new float[]{prevPos.x,pos.y,prevPos.x+l,pos.y,prevPos.x+l,pos.y+h,prevPos.x,pos.y+h});
    		velVector = new Line(prevPos.x, prevPos.y, pos.x, pos.y);
    		if (dir > 0) {
    			moveRight();
    		}
    		else if (dir < 0) {
    			moveLeft();
    		}
    		
//    		for (playerObject playerOb : playerArr) {  //collision with players
//    			if (playerOb.alive()) {
//    				if (xTest.intersects(playerOb.p) && playerOb.alive()) {
////    					System.out.println("x hit!");
//    					playerOb.damage(contactDamage);
//    				}
//    				if (yTest.intersects(playerOb.p) && playerOb.alive()) {
//    					playerOb.damage(contactDamage);
////    					System.out.println("y hit!");
//    				}
//    				if (p.intersects(playerOb.p) && playerOb.alive()) { //are you already stuck in your partner???
//    					playerOb.damage(contactDamage);
//    				}
//    			}
//    		}
    		
    		//COLLISION CODE FOR BLOCKS
    		if (collStatic) {
	    		for (StaticObject staticOb : arr) { //  collision with blocks
	    			if (xTest.intersects(staticOb.p)) {
	    				pos.x = prevPos.x;
	    				if (vel.x > 0) {
	    					moveLeft();
	    					dir = -1;
	    				}
	    				else if (vel.x < 0) {
	    					moveRight();
	    					dir = 1;
	    				}
	    			}
	    			if (yTest.intersects(staticOb.p)) {
	    				//vertical static block collision?
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
    		p.setX(pos.x);
    		p.setY(pos.y);
    		
		} //end if alive statement
		if(pos.x< -100 || pos.x> 900){
			health=-99;
		}

		
//		System.out.println(pos);
	} //end method
	
	@Override
    public void damage(int d) {
		mySoundSystem.quickPlay( true, "BatDeath2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    	health -= d;
    }
	
	@Override
	public void render(Graphics g) {
		// TODO Auto-generated method stub
		if(alive()){
			anim.draw(pos.x,pos.y);
		}else{
//			g.drawString(""+pointValue, pos.x + l/2, pos.y - 10);
			deathAnim.draw(pos.x,pos.y);
			deathFrames--;
		}
	}
	
}
