package org.ssg.Stanford;
import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig; 
//import paulscode.sound.libraries.LibraryJavaSound; 

public class MainMenuState extends BasicGameState{

	public int stateID;
	public int choice;

	final int REGULAR = 0;
	final int PACIFIST = 1;
	final int SOLO = 2;
	final int PACSOLO = 3;
	final int EXIT = 4;
	final int KNIGHT = 99;
	
	public static final int MENUSTATE = 10;
	public static final int GAMEPLAYSTATE = 11;
	public static final int GAMEMENUSTATE = 12;

	Image bg, tutImg, tutImgK;
	SpriteSheet icons, sprites;
	
	float cursorXTarget;
	float cursorX;
	float cursorY;
	int cursorDex;
	
	int x, xTarget;
	
	final int s = 15;
	// PlayerNum | Spawns or Pacifist | Start or Exit
	int[] regTargets;
	int[] regCoords;
	int[] regSelection;
	
	public boolean knight;
	
	String[][] boxTitle = {{"Two Player:","Adventure Mode:","Start:"},{"Single Player:", "Jump Mode:", "Exit:"}};
	String[][] boxText = {{"Play with a friend...\nOr an enemy!","Das Towerung awaits!\nEnemies abound these crooked steps!","Begin game with selected options!"},
						  {"Play with yourself.\n", "Climb the tower without fail!\nHurry, the tower is falling fast!", "You can never leave Das Towerung!"}};
	
	String[][] boxTitleK = {{"Two Player - Knight Mode:","Schwert Ritter:","Start:"},{"Single Player - Knight Mode:", "Axt Ritter:", "Exit:"}};
	String[][] boxTextK = {{"Play with a friend...\nOr an enemy!","They took everything from him...\nbut they couldn't take his knighthood!","Begin knight mode game with\n selected options!"},
						  {"Play with yourself.\n", "Fling your mighty axe!\nSmite thine foes from range!", "You can never leave Das Towerung!"}};
	
	SoundSystem mySoundSystem;
	//public int musicStart;
	
	boolean[] cExist;
	Component[] lStickX, lStickY, selectButton;
	boolean up, down, left, right, select = false;
	boolean[] upPressed, downPressed, leftPressed, rightPressed, selectPressed;
	
	public MainMenuState(int state){
		super();
		stateID = state;
		
		lStickX = new Component[2];
		lStickY = new Component[2];
		selectButton = new Component[2];

		cExist = new boolean[2];
		upPressed = new boolean[2];
		downPressed = new boolean[2];
		leftPressed = new boolean[2];
		rightPressed = new boolean[2];
		selectPressed = new boolean[2];

		for(int i=0; i<2; i++){
			cExist[i] = false;
			upPressed[i] = true;
			downPressed[i] = true;
			leftPressed[i] = true;
			rightPressed[i] = true;
			selectPressed[i] = true;
		}
	}

	public void setControllers(Controller c1, Controller c2){
		cExist[0] = (c1 != null);
		cExist[1] = (c2 != null);
		
		
		if(cExist[0]){
			lStickX[0] = c1.getComponent(Component.Identifier.Axis.X);
			lStickY[0] = c1.getComponent(Component.Identifier.Axis.Y);
			selectButton[0] = c1.getComponent(Component.Identifier.Button._0);
		}
		if(cExist[1]){
			lStickX[1] = c2.getComponent(Component.Identifier.Axis.X);
			lStickY[1] = c2.getComponent(Component.Identifier.Axis.Y);
			selectButton[1] = c2.getComponent(Component.Identifier.Button._0);
		}
	}
	
	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
		mySoundSystem.backgroundMusic("MainMenuMusic", "menuSong.ogg", true);

		cursorX = 550-s;
		cursorXTarget = 550-s;
		cursorDex = 0;
		
		up = false;
		down = false;
		left = false;
		right = false;
		select = false;
		for(int i=0; i<2; i++){
			upPressed[i] = true;
			downPressed[i] = true;
			leftPressed[i] = true;
			rightPressed[i] = true;
			selectPressed[i] = true;
		}
	}
	
	public void leave(GameContainer gc, StateBasedGame sbg) throws SlickException {
		mySoundSystem.fadeOutIn( "MainMenuMusic", "jumpSong.ogg", 1000, 2000 );
	}
	
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		choice = REGULAR;
		bg = ((Stanford)sbg).menuBG;
		icons = ((Stanford)sbg).menuIcons;
		sprites = ((Stanford)sbg).menuSprites;
		tutImg = ((Stanford)sbg).tutorial;
		tutImgK = ((Stanford)sbg).tutorialKnight;
		
		regTargets = new int[3];
		regCoords =  new int[3];
		for(int i=0;i<3;i++){
			regTargets[i]=150;
			regCoords[i]=150;
		}
		regSelection = new int[3];
		for(int i=0;i<3;i++)
			regSelection[i]=0;
		
		cursorX = 550-s;
		cursorXTarget = 550-s;
		cursorDex = 0;
		
		x=0;
		xTarget = 0;
		
		//musicStart = 0;
		knight = false;
	}

	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {

		g.drawImage(bg, 0+x,0);
		if(knight){
			g.drawImage(tutImgK, -800+x,0);
		}else{
			g.drawImage(tutImg, -800+x,0);
		}
		g.setColor(Color.black);
		if(x!=0){
			g.fillRect(0+x,250,70,100);
		}
		if(x!=800){
			g.fillRect(-60+x,250,70,100);
		}
		g.setColor(Color.white);
		g.fillRect(525+x,225,200,30);
		
		icons.startUse();		
		icons.renderInUse(550-s+x, regCoords[0], 1, 0);
		icons.renderInUse(550-s+x, regCoords[0]+50+s, 1, 1);
		icons.renderInUse(650+s+x, regCoords[2], 3, 0);
		icons.renderInUse(650+s+x, regCoords[2]+50+s, 3, 1);
		if(knight){
			if(regSelection[0]==0){//2Player
				icons.renderInUse(600+x, regCoords[1], 0, 2);
				icons.renderInUse(600+x, regCoords[1]+50+s, 1, 2);
			}else{
				icons.renderInUse(600+x, regCoords[1], 0, 0);
				icons.renderInUse(600+x, regCoords[1]+50+s, 0, 1);
			}
		}else{
			icons.renderInUse(600+x, regCoords[1], 2, 0);
			icons.renderInUse(600+x, regCoords[1]+50+s, 2, 1);
		}
		
		icons.renderInUse((int)cursorX+x, 95,2 ,2 );
		icons.renderInUse((int)cursorX+x, 95+200+10+2*s,3 ,2 );
		icons.endUse();

		if(knight){
			if(regSelection[0]==0){//2P
				sprites.startUse();
				sprites.renderInUse(200+x, 505, 0, 4);
				sprites.renderInUse(255+x, 505, 1, 4);
				sprites.endUse();
				if(regSelection[1]==0){//Blue First
					g.getFont().drawString(225+x, 485, "P1");
					g.getFont().drawString(270+x, 485, "P2");
				}else{
					g.getFont().drawString(225+x, 485, "P2");
					g.getFont().drawString(270+x, 485, "P1");
				}
			}else{
				if(regSelection[1]==0){//Blue
					sprites.startUse();
					sprites.renderInUse(200+x, 505, 0, 4);
					sprites.endUse();
					g.drawImage(sprites.getSubImage(1,4),255+x,505, Color.gray);
				}else{
					sprites.startUse();
					sprites.renderInUse(255+x, 505, 1, 4);
					sprites.endUse();
					g.drawImage(sprites.getSubImage(0,4),200+x,505, Color.gray);
				}
			}
		}else{
			sprites.startUse();
			sprites.renderInUse( 200+x,505,0,0);
			sprites.endUse();
			
			if(regSelection[0]==1){
				g.drawImage(sprites.getSubImage(1,0),225+x,505, Color.gray);
			}else{
				sprites.startUse();
				sprites.renderInUse( 225+x,505,1,0);
				sprites.endUse();
			}
		}
			
		if(regSelection[1]==1 && !knight){
			g.drawImage(sprites.getSubImage(0,1),520+x,505, Color.gray);
			g.drawImage(sprites.getSubImage(1,1),570+x,505, Color.gray);
			g.drawImage(sprites.getSubImage(0,2),560+x,435, Color.gray);
			g.drawImage(sprites.getSubImage(1,2),610+x,430, Color.gray);
			g.drawImage(sprites.getSubImage(0,3),440+x,505, Color.gray);
			g.drawImage(sprites.getSubImage(1,3),470+x,505, Color.gray);
		}else{
			sprites.startUse();
			sprites.renderInUse( 520+x, 505, 0, 1);
			sprites.renderInUse( 570+x, 505, 1, 1);
			if(knight){
				sprites.renderInUse( 470+x, 505, 0, 5);
			}else{
				sprites.renderInUse( 440+x, 505, 0, 3);
				sprites.renderInUse( 470+x, 505, 1, 3);
			}
			sprites.renderInUse( 560+x, 435, 0, 2);
			sprites.renderInUse( 610+x, 430, 1, 2);
			sprites.endUse();
		}
		
		g.setLineWidth(2);
		g.drawRect(100+x,150,400,170);
		
		if(knight){
			if(cursorDex==1 && regSelection[0]==0){
				if(regSelection[1]==0){
					g.getFont().drawString(120+x, 170,"Schwert und Axt!");
					g.getFont().drawString(120+x, 195,"The legendary blade of power!\nThe ancient axe of power!\nBattling side by side!");
				}else{
					g.getFont().drawString(120+x, 170,"Axt und Schwert!");
					g.getFont().drawString(120+x, 195,"The ancient axe of power!\nThe legendary blade of power!\nBattling side by side!");
				}
			}else{
				g.getFont().drawString(120+x, 170,boxTitleK[regSelection[cursorDex]][cursorDex]);
				g.getFont().drawString(120+x, 195,boxTextK[regSelection[cursorDex]][cursorDex]);
			}
		}else{
			g.getFont().drawString(120+x, 170,boxTitle[regSelection[cursorDex]][cursorDex]);
			g.getFont().drawString(120+x, 195,boxText[regSelection[cursorDex]][cursorDex]);
		}
	}

	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
		
		up = false;
		down = false;
		left = false;
		right = false;
		select = false;
		
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
				
				if(lStickX[i].getPollData() > 0.5 && !rightPressed[i]){
					rightPressed[i] = true;
					right = true;
				}else if(lStickX[i].getPollData() < -0.5 && !leftPressed[i]){
					leftPressed[i] = true;
					left = true;
				}else if(lStickX[i].getPollData() > -0.5 && lStickX[i].getPollData() < 0.5){
					leftPressed[i] = false;
					rightPressed[i] = false;
				}
				
				if(selectButton[i].getPollData()==1.0 && !selectPressed[i]){
					select = true;
					selectPressed[i] = true;
				}else if(selectButton[i].getPollData() == 0.0){
					selectPressed[i] = false;
				}
			}
		}
		
		
		Input input = gc.getInput();
		
//		musicStart+=delta;
//		if(musicStart>=6000){
//			mySoundSystem.queueSound("MainMenuMusic", "menuSong.ogg");
//		}
		
		if(cursorX != cursorXTarget){
			cursorX += (cursorXTarget-cursorX)/(Math.abs(cursorXTarget-cursorX))*5;
		}
		
		if(x!=xTarget){
			x += (xTarget-x)/(Math.abs(xTarget-x))*20;
		}
		
		for(int i=0;i<regCoords.length;i++){
			if(regCoords[i]<regTargets[i]){
				regCoords[i]+=5;
			}else if(regCoords[i]>regTargets[i]){
				regCoords[i]-=5;
			}
		}
		
		//If get arrow key, move selection
		if((input.isKeyPressed(Input.KEY_UP) || up) && x==0){
			mySoundSystem.quickPlay( true, "MenuShift.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			if(regSelection[cursorDex]>0){
				regTargets[cursorDex]-=(50+s);
				regSelection[cursorDex]--;
			}
		}else if((input.isKeyPressed(Input.KEY_DOWN) || down) && x==0){
			mySoundSystem.quickPlay( true, "MenuShift.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			if(regSelection[cursorDex]<1){
				regTargets[cursorDex]+=(50+s);
				regSelection[cursorDex]++;
			}
		}else if((input.isKeyPressed(Input.KEY_RIGHT) || right) && x==0){
			mySoundSystem.quickPlay( true, "MenuShift.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			if(cursorDex<regCoords.length-1){
				cursorDex++;
				cursorXTarget+=(50+s);
			}
    	}else if((input.isKeyPressed(Input.KEY_LEFT) || left) && x==0){
    		mySoundSystem.quickPlay( true, "MenuShift.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    		if(cursorDex>0){
    			cursorDex--;
    			cursorXTarget-=(50+s);
    		}
		}else if(input.isKeyPressed(Input.KEY_K) && x==0){
			mySoundSystem.quickPlay( true, "MenuSelect.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			knight=!knight;
			cursorDex=0;
			cursorX = 550-s;
			cursorXTarget = 550-s;
		}else if(input.isKeyPressed(Input.KEY_H) && x==xTarget){//Tutorial Screen
			mySoundSystem.quickPlay( true, "MenuShift.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			if(xTarget == 0){
				xTarget=800;
			}else{
				xTarget=0;
			}
		}else if(x==0&&((input.isKeyPressed(Input.KEY_ENTER)) || input.isKeyPressed(Input.KEY_SPACE)) || select){
			mySoundSystem.quickPlay( true, "MenuSelect.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
			if(cursorDex!=2){
					cursorDex = 2;
					cursorXTarget = 650+s;
			}else if(regSelection[2]==0){
				if(knight){
					((GameplayState) sbg.getState(GAMEPLAYSTATE)).setKnight(true);
					((GameplayState) sbg.getState(GAMEPLAYSTATE)).setSpawns(true);
					if(regSelection[0]==1){
						((GameplayState) sbg.getState(GAMEPLAYSTATE)).setSolo(true);
					}else{
						((GameplayState) sbg.getState(GAMEPLAYSTATE)).setSolo(false);
					}
					((GameplayState) sbg.getState(GAMEPLAYSTATE)).setKnightConfiguration(regSelection[1]);
					sbg.getState(GAMEPLAYSTATE).init(gc, sbg);
					((GameplayState) sbg.getState(GAMEPLAYSTATE)).setBlack(false);
					leave(gc, sbg);
					sbg.enterState(GAMEPLAYSTATE);
				}else{
					((GameplayState) sbg.getState(GAMEPLAYSTATE)).setKnight(false);
					if(regSelection[0]==1){
						((GameplayState) sbg.getState(GAMEPLAYSTATE)).setSolo(true);
					}else{
						((GameplayState) sbg.getState(GAMEPLAYSTATE)).setSolo(false);
					}
					if(regSelection[1]==1){
						((GameplayState) sbg.getState(GAMEPLAYSTATE)).setSpawns(false);
					}else{
						((GameplayState) sbg.getState(GAMEPLAYSTATE)).setSpawns(true);
					}
					sbg.getState(GAMEPLAYSTATE).init(gc, sbg);
					((GameplayState) sbg.getState(GAMEPLAYSTATE)).setBlack(false);
					leave(gc, sbg);
					sbg.enterState(GAMEPLAYSTATE);
				}				
			}else{
				mySoundSystem.cleanup();
				gc.exit();
			}
    	}else if(input.isKeyPressed(Input.KEY_ESCAPE)){
    		mySoundSystem.quickPlay( true, "MenuSelect.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    		mySoundSystem.cleanup();
			gc.exit();
    	}else{
    		input.clearKeyPressedRecord();
    	}
		//If get enter key, send back something about gameplay entered
	}

	public void setSoundSystem(SoundSystem ss){
		mySoundSystem=ss;
	}
	
	@Override
	public int getID() {
		return stateID;
	}
	
	private int mod(int x, int y)
	{
	    int result = x % y;
	    return result < 0? result + y : result;
	}
	
	
}
