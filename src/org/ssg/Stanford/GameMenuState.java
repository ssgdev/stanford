package org.ssg.Stanford;
import java.util.ArrayList;

import net.java.games.input.*;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig; 
import paulscode.sound.SoundSystemException; 
//import paulscode.sound.libraries.LibraryJavaSound; 
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.codecs.CodecWav;

public class GameMenuState extends BasicGameState{

	public int stateID;
	public int choice;

	final int RESTART = 0;
	final int RESUME = 1;
	final int QUIT = 2;
	
	public static final int MENUSTATE = 10;
	public static final int GAMEPLAYSTATE = 11;
	public static final int GAMEMENUSTATE = 12;
	public static final int GAMEOVERSTATE = 13;
	
	Image bgImg;
	
	SoundSystem mySoundSystem;
	
	boolean[] cExist;
	Component[] lStickY, selectButton, menuButton;
	boolean up, down, select, menu = false;
	boolean[] upPressed, downPressed, selectPressed, menuPressed;

	public GameMenuState(int state){
		super();
		stateID = state;
		
		lStickY = new Component[2];
		selectButton = new Component[2];
		menuButton = new Component[2];
		
		cExist = new boolean[2];
		upPressed = new boolean[2];
		downPressed = new boolean[2];
		selectPressed = new boolean[2];
		menuPressed = new boolean[2];
		
		for(int i=0; i<2; i++){
			cExist[i] = false;
			upPressed[i] = true;
			downPressed[i] = true;
			selectPressed[i] = true;
			menuPressed[i] = true;
		}
	}

	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		choice = RESTART;
	}
	
	public void setControllers(Controller c1, Controller c2){
		cExist[0] = (c1 != null);
		cExist[1] = (c2 != null);
		
		if(cExist[0]){
			lStickY[0] = c1.getComponent(Component.Identifier.Axis.Y);
			selectButton[0] = c1.getComponent(Component.Identifier.Button._0);
			menuButton[0] = c1.getComponent(Component.Identifier.Button._7);
		}
		if(cExist[1]){
			lStickY[1] = c2.getComponent(Component.Identifier.Axis.Y);
			selectButton[1] = c2.getComponent(Component.Identifier.Button._0);
			menuButton[1] = c2.getComponent(Component.Identifier.Button._7);
		}
	}
	
	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
		mySoundSystem.quickPlay( true, "MenuSelect.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		choice = RESTART;
		up = false;
		down = false;
		select = false;
		menu = false;
		for(int i=0; i<2; i++){
			upPressed[i] = true;
			downPressed[i] = true;
			selectPressed[i] = true;
			menuPressed[i] = true;
		}
	}

	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
		g.drawImage(bgImg, 0,0, Color.darkGray);
		
		int y=180;
		g.setColor(new Color(0, 0, 0, 0.7f));
		g.fillRect(300, y, 200, 80);
		g.fillRect(300, y+100, 200, 80);
		g.fillRect(300, y+200, 200, 80);
		
		g.setColor(Color.white);
		g.drawRect(300, y, 200, 80);
		g.drawString("Restart", 371, y+30);
		g.drawRect(300, y+100, 200, 80);
		g.drawString("Resume", 376, y+130);
		g.drawRect(300, y+200, 200, 80);
		g.drawString("Main Menu", 364, y+230);	
		
		if(Math.abs(choice) == RESTART){
			g.setLineWidth(4);
			g.drawRect(296, y-4, 208, 88);
		}else if(Math.abs(choice) == RESUME){
			g.setLineWidth(5);
			g.drawRect(296, y+96, 208, 88);
		}else if(Math.abs(choice) == QUIT){
			g.setLineWidth(5);
			g.drawRect(296, y+196, 208, 88);
		}
		g.setLineWidth(1);
	}

	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
		up = false;
		down = false;
		select = false;
		menu = false;
		
		for(int i=0; i<2; i++){			
			if(cExist[i]){
				if(lStickY[i].getPollData() > 0.5 && !downPressed[i]){
					downPressed[i] = true;
					down = true;
				}else if(lStickY[i].getPollData() < -0.5 && !upPressed[i]){
					upPressed[i] = true;
					up = true;
				}else if(lStickY[i].getPollData() > -0.5 && lStickY[i].getPollData() < 0.5){
					upPressed[i] = false;
					downPressed[i] = false;
				}
				
				if(selectButton[i].getPollData()==1.0 && !selectPressed[i]){
					select = true;
					selectPressed[i] = true;
				}else if(selectButton[i].getPollData() == 0.0){
					selectPressed[i] = false;
				}
				
				if(menuButton[i].getPollData()==1.0 && !menuPressed[i]){
					menu = true;
					menuPressed[i] = true;
				}else if(menuButton[i].getPollData() == 0.0){
					menuPressed[i] = false;
				}
			}
		}
		
		Input input = gc.getInput();
		
		//If get arrow key, move selection
		if(input.isKeyPressed(Input.KEY_DOWN) || down){
			mySoundSystem.quickPlay( true, "MenuShift.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    		choice = mod(choice+1,3);
    	}else if(input.isKeyPressed(Input.KEY_UP) || up){
    		mySoundSystem.quickPlay( true, "MenuShift.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			choice = mod(choice-1,3);
		}else if(input.isKeyPressed(Input.KEY_ESCAPE) || menu ){
			mySoundSystem.quickPlay( true, "MenuSelect.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			leave(gc, sbg);
			sbg.enterState(GAMEPLAYSTATE);
		}else if(input.isKeyPressed(Input.KEY_R)){
			mySoundSystem.quickPlay( true, "MenuSelect.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			sbg.getState(GAMEPLAYSTATE).init(gc, sbg);
			((GameplayState) sbg.getState(GAMEPLAYSTATE)).setBlack(false);
			leave(gc, sbg);
			sbg.enterState(GAMEPLAYSTATE);
		}else if(input.isKeyPressed(Input.KEY_ENTER) || select){
			mySoundSystem.quickPlay( true, "MenuSelect.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    		if(choice == RESTART){
    			sbg.getState(GAMEPLAYSTATE).init(gc, sbg);
    			((GameplayState) sbg.getState(GAMEPLAYSTATE)).setBlack(false);
    			leave(gc, sbg);
    			sbg.enterState(GAMEPLAYSTATE);
    		}else if(choice == RESUME){
    			leave(gc, sbg);
    			sbg.enterState(GAMEPLAYSTATE);
    		}else if(choice == QUIT){
    			((GameOverState)sbg.getState(GAMEOVERSTATE)).newName = "";
    			leave(gc, sbg);
    			sbg.enterState(MENUSTATE);
    		}
    	}else{
    		input.clearKeyPressedRecord();
    	}
	}

	@Override
	public int getID() {
		return stateID;
	}
	
	public void setImage(Image i){
		bgImg = i;
	}
	
	public void setSoundSystem(SoundSystem ss){
		mySoundSystem=ss;
	}
	
	private int mod(int x, int y)
	{
	    int result = x % y;
	    return result < 0? result + y : result;
	}
	
}
