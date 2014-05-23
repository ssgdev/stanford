package org.ssg.Stanford;
import net.java.games.input.Component;
import net.java.games.input.Controller;

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
	
	boolean c1Exist, c2Exist;
	Component selectButton1, selectButton2;
	Component menuButton1, menuButton2;
	
	public IntroScreenState(int i){
		super();
		stateID = i;
	}

	public void setControllers(Controller c1, Controller c2){
		c1Exist = (c1 != null);
		c2Exist = (c2 != null);
		
		if(c1Exist){
			selectButton1 = c1.getComponent(Component.Identifier.Button._0);
			menuButton1 = c1.getComponent(Component.Identifier.Button._7);
		}
		if(c2Exist){
			selectButton2 = c2.getComponent(Component.Identifier.Button._0);
			menuButton2 = c2.getComponent(Component.Identifier.Button._7);
		}
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
		
		if(c1Exist){
			g.getFont().drawString(250, 320, "[Press Start]");
		}else{
			g.getFont().drawString(250, 320, "[Press Enter]");
		}
	}

	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
		Input input = gc.getInput();
		
		if(input.isKeyPressed(Input.KEY_ESCAPE)){
			mySoundSystem.cleanup();
    		gc.exit();
    	}else if(input.isKeyPressed(Input.KEY_ENTER)
    			|| (c1Exist? selectButton1.getPollData() == 1.0 || menuButton1.getPollData() == 1.0 : false)
    			|| (c2Exist? selectButton2.getPollData() == 1.0 || menuButton2.getPollData() == 1.0 : false)){
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
