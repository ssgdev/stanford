package org.ssg.Stanford;
import java.util.ArrayList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig; 
import paulscode.sound.SoundSystemException; 
//import paulscode.sound.libraries.LibraryJavaSound; 
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.codecs.CodecWav;

public abstract class EnemyObject {
	
    public int l, h;
    public Polygon p;
    public float grav, horizVel, horizAcc, jumpVel, jumpAcc;

    //motion variables
    public Vector2f vel;
	public Vector2f pos;
	public Vector2f acc;
	public Line velVector;
    public boolean mRight;
    public boolean mLeft;
    public boolean mJump;
    public boolean grounded;
    public Polygon xTest, yTest;
    public String enemyType;

    //animation variables
    public SpriteSheet sprite;
    
    //combat variables
    public int health;
    public int deathFrames;
    public int dir;
    public int contactDamage;
    public boolean collPlayer;
    public boolean collStatic;
    public int pointValue;
    
	SoundSystem mySoundSystem;
    
    public void shiftDown(double d){
        pos.y+=d;
        p.setY(pos.y);
    }
    
    public void damage(int d) {
    	health -= d;
    }
    
    public boolean alive() {
    	return health>0;
    }
    
    public abstract void moveLeft();
    public abstract void moveRight();    
    public abstract void update(StateBasedGame sbg, int delta, ArrayList<StaticObject> arr, 
    		ArrayList<PlayerObject> playerArr, 
    		ArrayList<EnemyObject> enemyArr,
    		ArrayList<AxeObject> axeArr);
	public abstract void render(Graphics g);

	public void attack(ArrayList<PlayerObject> playerArr,
			ArrayList<EnemyObject> enemyArr) {
	}
	
	public void setSoundSystem(SoundSystem ss){
		mySoundSystem=ss;
	}
    
}
