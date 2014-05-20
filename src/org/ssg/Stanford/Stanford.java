package org.ssg.Stanford;

import net.java.games.input.*;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.openal.SoundStore;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig; 
import paulscode.sound.SoundSystemException; 
//import paulscode.sound.libraries.LibraryJavaSound; 
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;

public class Stanford extends StateBasedGame {

	public static final int MENUSTATE = 10;
	public static final int GAMEPLAYSTATE = 11;
	public static final int GAMEMENUSTATE = 12;
    public static final int GAMEOVERSTATE = 13;
	public static final int GAMEOVERMENUSTATE = 14;
	public static final int INTROSCREENSTATE = 15;
	
	Image menuBG;
	Image block,  player1Img, player2Img, batImg, hellBatImg, bgImage, titleImg;
	Image regImg, pacImg, solImg, pSolImg, exiImg, knightModeImg;
	Image knightImage, axeKnightImage;
	Image blueKnightImage, blueAxeKnightImage;
	Image powerUpImg, pwUpAmmo;
	Image tutorial, tutorialKnight;
	
	SpriteSheet powerUpSprite;
	SpriteSheet axeSheet, blueAxeSheet;
	SpriteSheet pwupAmmoSprite;
	SpriteSheet menuIcons, menuSprites;
	SpriteSheet headIcons;
	
	SoundSystem mySoundSystem;
	Controller c1, c2; //gamepad controllers
	
	public Stanford() throws SlickException {
		super("Das Towerung");

        try {
        	SoundSystemConfig.addLibrary( LibraryLWJGLOpenAL.class );
        	//SoundSystemConfig.addLibrary( LibraryJavaSound.class );
			SoundSystemConfig.setCodec( "wav", CodecWav.class );
			SoundSystemConfig.setCodec( "ogg", CodecJOrbis.class );
			//SoundSystemConfig.setSoundFilesPackage( "C:/Users/lufamily/workspace/ProjectStanfordSounds/resources/sounds/");
			//SoundSystemConfig.setSoundFilesPackage( "org.ssg.Stanford.Sounds/" );
			SoundSystemConfig.setSoundFilesPackage( "org/ssg/Stanford/Sounds/" );
			SoundSystemConfig.setStreamQueueFormatsMatch( true );
		} catch (SoundSystemException e) {
			e.printStackTrace();
		} 
		
        c1 = null;
        c2 = null;
		mySoundSystem = new SoundSystem();		
		
        for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
        	if (c.getType() == Controller.Type.GAMEPAD) {
        		if (c1 == null) {
        			c1 = c;
        			System.out.println("First Controller Found: " + c1.getName());
        		} else if (c2 == null) {
        			c2 = c;
        			System.out.println("Second Controller Found: " + c2.getName());
        		} //end if-else block
        	} //end if statement
        } //end for each loop
		
		this.addState(new IntroScreenState(INTROSCREENSTATE));
		((IntroScreenState) this.getState(INTROSCREENSTATE)).setSoundSystem(mySoundSystem);
		this.addState(new GameplayState(GAMEPLAYSTATE, true));
		((GameplayState) this.getState(GAMEPLAYSTATE)).setSoundSystem(mySoundSystem);
		this.addState(new MainMenuState(MENUSTATE));
		((MainMenuState) this.getState(MENUSTATE)).setSoundSystem(mySoundSystem);
		this.addState(new GameMenuState(GAMEMENUSTATE));
		((GameMenuState) this.getState(GAMEMENUSTATE)).setSoundSystem(mySoundSystem);
		((GameMenuState) this.getState(GAMEMENUSTATE)).setImage(((GameplayState)this.getState(GAMEPLAYSTATE)).bgImage);
		this.addState(new GameOverState(GAMEOVERSTATE));
		((GameOverState) this.getState(GAMEOVERSTATE)).setSoundSystem(mySoundSystem);
		((GameOverState) this.getState(GAMEOVERSTATE)).setScoresName("resources/highscores.txt");
		this.addState(new GameOverMenuState(GAMEOVERMENUSTATE));
		((GameOverMenuState) this.getState(GAMEOVERMENUSTATE)).setSoundSystem(mySoundSystem);
		((GameOverMenuState) this.getState(GAMEOVERMENUSTATE)).setImage(((GameplayState)this.getState(GAMEPLAYSTATE)).bgImage);

	}

	public void initStatesList(GameContainer gc) throws SlickException {
		
		SoundStore.get().setMaxSources(64);
		
		initImages();
		initSounds();
		
		AppGameContainer appContainer = (AppGameContainer) gc;
			
		if (!appContainer.isFullscreen()) {
			String[] icons = {"resources/sprites/icon16.png", "resources/sprites/icon32.png"};
			appContainer.setIcons(icons);
	    }
		
		((GameplayState) this.getState(GAMEPLAYSTATE)).setControllers(c1, c2);
		((GameMenuState) this.getState(GAMEMENUSTATE)).setControllers(c1, c2);
		((GameOverMenuState) this.getState(GAMEOVERMENUSTATE)).setControllers(c1, c2);
		
		//
		/**
		this.getState(MENUSTATE).init(gc, this);
		this.getState(GAMEMENUSTATE).init(gc, this);
		this.getState(GAMEOVERSTATE).init(gc, this);
		this.getState(GAMEOVERMENUSTATE).init(gc, this);
		this.getState(GAMEPLAYSTATE).init(gc, this);
		this.getState(INTROSCREENSTATE).init(gc, this);
		**/	        
	}
	
	public void initSounds() throws SlickException{
		mySoundSystem.loadSound("AxeRicochetAlt.wav");
		mySoundSystem.loadSound("BatDeath2.wav");
		mySoundSystem.loadSound("BoxRicochet2.wav");
		mySoundSystem.loadSound("KnightDeath.wav");
		mySoundSystem.loadSound("KnightJump.wav");
		mySoundSystem.loadSound("KnightSwing.wav");
		mySoundSystem.loadSound("KnightWalk.wav");
		mySoundSystem.loadSound("Laser.wav");
		mySoundSystem.loadSound("ManDead.wav");
		mySoundSystem.loadSound("ManDJump.wav");
		mySoundSystem.loadSound("ManJump2.wav");
		mySoundSystem.loadSound("ManSwing.wav");
		mySoundSystem.loadSound("ManWalk2.wav");
		mySoundSystem.loadSound("MenuSelect.wav");
		mySoundSystem.loadSound("MenuShift.wav");
		mySoundSystem.loadSound("Powerup.wav");
		mySoundSystem.loadSound("SwordClash.wav");
		mySoundSystem.loadSound("Revive.wav");
		mySoundSystem.loadSound("VengeBatSpawn.wav");
		mySoundSystem.loadSound("VengeBatSwat.wav");
		mySoundSystem.loadSound("GameOver.wav");
	}
	
	public void initImages() throws SlickException {
		block = new Image("resources/sprites/block.png");
		bgImage = new Image("resources/sprites/bg.png");

		player1Img = new Image("resources/sprites/player1Sprite.png");
		player2Img = new Image("resources/sprites/player2Sprite.png");

		batImg = new Image("resources/sprites/batSheet.png");
		hellBatImg = new Image("resources/sprites/hellBatSheet.png");

		knightImage = new Image("resources/sprites/knightSheet.png");
		axeKnightImage = new Image("resources/sprites/axeKnightSheet.png");
		blueKnightImage = new Image("resources/sprites/blueKnightSheet2.png");
		blueAxeKnightImage = new Image("resources/sprites/blueAxeKnightSheet2.png");
		
		powerUpImg = new Image("resources/sprites/pwups.png");
		powerUpSprite = new SpriteSheet(powerUpImg,20,20);
		
		axeSheet = new SpriteSheet(new Image("resources/sprites/axeSheet.png"), 20, 20);
		blueAxeSheet = new SpriteSheet(new Image("resources/sprites/blueAxeSheet.png"), 20, 20);
		
		pwUpAmmo = new Image("resources/sprites/pwupAmmoDisp.png");
		pwupAmmoSprite = new SpriteSheet(pwUpAmmo,16,16);
		
		titleImg = new Image("resources/sprites/titlescreen.png");
		menuBG = new Image("resources/sprites/mainMenu.png");
		menuIcons = new SpriteSheet(new Image("resources/sprites/menuIcons.png"), 50, 50);
		menuSprites = new SpriteSheet(new Image("resources/sprites/menuSprites.png"), 55, 55);
		
		headIcons = new SpriteSheet(new Image("resources/sprites/heads.png"), 20,20);
		
		tutorial = new Image("resources/sprites/tutorial.png");
		tutorialKnight = new Image("resources/sprites/tutorialK.png");
	}
	
	public static void main(String[] args) throws SlickException {
		AppGameContainer app = new AppGameContainer(new Stanford());
		app.setDisplayMode(800, 600, false);
		app.setVSync(true);
		app.setTargetFrameRate(60);
		app.setAlwaysRender(true);
		app.setShowFPS(false);
		app.setTitle("Das Towerung");
//		app.setSmoothDeltas(true);
//		if (app.supportsMultiSample())
//			app.setMultiSample(2);
		app.setFullscreen(false);
		app.setMaximumLogicUpdateInterval(24);
		app.setMinimumLogicUpdateInterval(24);
		app.start();
	}

}
