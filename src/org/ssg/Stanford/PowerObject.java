package org.ssg.Stanford;
import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig; 
import paulscode.sound.SoundSystemException; 
//import paulscode.sound.libraries.LibraryJavaSound; 
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.codecs.CodecWav;

public class PowerObject {
	public int index;
	public static final int laserNum = 6;
	public static final int dJumpNum = 5;
	
	//actual physical object stuff
	static final float grav = 1.0f;
	public Polygon p;
	public Vector2f pos;
	public Vector2f vel;
	public Vector2f acc;
	public int l;
	public boolean coll, collH, collV;
	public Polygon xTest, yTest;
	public Line velVector;
	boolean justPlayed;//So that you don't play the ricochet for both x and y
	
	SoundSystem mySoundSystem;
	
	public PowerObject(int a, int b, int i, int length, SoundSystem mSS) {
		pos = new Vector2f (a, b);
		vel = new Vector2f (0, 0);
		acc = new Vector2f (0, 0);
		velVector = new Line(0,0);
		xTest = new Polygon();
        yTest = new Polygon();
		index = i;
		l=length;
		p = new Polygon(new float[]{pos.x,pos.y,pos.x+l,pos.y,pos.x+l,pos.y+l,pos.x,pos.y+l});
		coll = false;
		collH = false;
		collV = false;
		justPlayed = false;
		mySoundSystem = mSS;
	} //end constructor
	
    public void shiftDown(double d){ //scrolling method
        pos.y+=d;
        p.setY(pos.y);
    } //end shift method
    
    public void pickUp() {
    	coll = true;
    }
    
    public void update(StateBasedGame sbg, int delta, ArrayList<StaticObject> arr, 
    		ArrayList<PlayerObject> playerArr, ArrayList<EnemyObject> enemyArr) { //update method
    	float d = delta/20;
    	acc.y = grav;
    	acc.x = -vel.x/30;
    	
		Vector2f prevPos = new Vector2f(pos.x, pos.y);
		pos.x += vel.x * d;
		pos.y += vel.y * d;
    	
		xTest = new Polygon(new float[]{pos.x,prevPos.y,pos.x+l,prevPos.y,pos.x+l,prevPos.y+l,pos.x,prevPos.y+l});
		yTest = new Polygon(new float[]{prevPos.x,pos.y,prevPos.x+l,pos.y,prevPos.x+l,pos.y+l,prevPos.x,pos.y+l});
		velVector = new Line(prevPos.x, prevPos.y, pos.x, pos.y);    	
		
		collH = false;
		collV = false;
		
		justPlayed = false;		
    	//COLLISION CODE FOR BLOCKS
		for (StaticObject staticOb : arr) { //  collision with blocks
			if (xTest.intersects(staticOb.p) && !collH) {
				if(Math.abs(vel.x)>1){
					mySoundSystem.quickPlay( true, "BoxRicochet2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}
				justPlayed = true;
				vel.x = -vel.x/2;
				pos.x = prevPos.x;
				collH = true;
			}
			if (yTest.intersects(staticOb.p) && !collV) {
				if(!justPlayed && Math.abs(vel.y)>1){
					mySoundSystem.quickPlay( true, "BoxRicochet2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}
				staticOb.collided();
				collV = true;
				vel.y = 0;
				if (pos.y > prevPos.y) {
					pos.y = staticOb.p.getY()-l-1;
					acc.x = -vel.x/5;
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
		} //end static object collision
		justPlayed=false;
		
		//movement updates
		vel.x += acc.x * d;
		vel.y += acc.y * d;
		
		p.setLocation(pos.x, pos.y);
    }
    
	public void setSoundSystem(SoundSystem ss){
		mySoundSystem=ss;
	}
    
    public void render(Graphics g) { //render method
//    	if (index == 0) { //dJump powerup
//    		g.setColor(new Color(0, 100, 255));
//    	} //end dJump
//    	else if (index == 1) { //laser powerup
//    		g.setColor(new Color(255, 50, 255));
//    	} //end laser powerup
//    	else if (index == 2) {
//    		g.setColor(new Color(0, 255, 0));
//    	}
//		g.fillRect(pos.x, pos.y, l, l);
//		g.setColor(Color.white);
    } //end rendering method
	
} //end class
