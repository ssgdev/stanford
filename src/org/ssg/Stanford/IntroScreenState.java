package org.ssg.Stanford;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class IntroScreenState extends BasicGameState{

	public int stateID;
	public int choice;

	final int STARTGAME = 0;
	final int EXIT = 1;
	public static final int MENUSTATE = 10;
	public static final int GAMEPLAYSTATE = 11;
	public static final int GAMEMENUSTATE = 12;
    public static final int GAMEOVERSTATE = 13;
	public static final int GAMEOVERMENUSTATE = 14;
	public static final int INTROSCREENSTATE = 15;
	
	Image title;
	
	SoundSystem mySoundSystem;
	
	public IntroScreenState(int i){
		super();
		stateID = i;
	}

	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		title =((Stanford)sbg).titleImg;
	}

	@Override
	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException{
		mySoundSystem.quickStream(true, "Thunder.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, SoundSystemConfig.getDefaultRolloff());
	}
	
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
		g.drawImage(title,0,0);
	}

	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
		Input input = gc.getInput();
		
		if(input.isKeyPressed(Input.KEY_ESCAPE)){
			mySoundSystem.cleanup();
    		gc.exit();
    	}else if(input.isKeyPressed(Input.KEY_ENTER)){
    		sbg.enterState(MENUSTATE);
    	}else{
    		input.clearKeyPressedRecord();
    	}

	}

	public void setSoundSystem(SoundSystem ss){
		mySoundSystem=ss;
	}
	
	@Override
	public int getID() {
		return stateID;
	}
	
}
