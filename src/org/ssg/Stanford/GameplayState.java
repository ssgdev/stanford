package org.ssg.Stanford;
import java.util.ArrayList;
import java.io.*;

import net.java.games.input.*;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig; 
import paulscode.sound.SoundSystemException; 
//import paulscode.sound.libraries.LibraryJavaSound; 
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.codecs.CodecWav;

//Have #of empty rows random - min:1 max:jump height
//Have countdowner: one shift is --
//Add setpieces to the statics arraylist

//Possible rowGen methods:     pass through, erase single blocks and fill in single gaps
//                            gen ledges within in the row, distributed around a certain ledge length and spacing, with random start from ledge
//Have pause at beginning

//blocks:
//0: space
//1: platform
//2: spawn bat ten[subject to change] blocks left;

/**
000000 - top
11111
22222...world[0].length
world.length-screenblocks //where screenblocks is screenHeight/blocksize
world.length
 **/

public class GameplayState extends BasicGameState {

	public static final int MENUSTATE = 10;
	public static final int GAMEPLAYSTATE = 11;
	public static final int GAMEMENUSTATE = 12;
	public static final int GAMEOVERSTATE = 13;
	public static final int GAMEOVERMENUSTATE = 14;

	static final int blockSize = 20;
	static int screenWidth = 800;
	static int screenHeight = 600;
	static int maxJump = 10;

	static double BATSPAWNLIMIT = 600;
	static double BATSPAWNHIGH = 90;
	static double BATSPAWNMED = 400;
	static double BATSPAWNLOW = 1200;
	
	static double KNIGHTSPAWNMED = 850;

	public boolean black;
	public boolean gameOver;
	public boolean solo;
	public boolean spawns;
	public boolean knightMode;

	//Set pieces
	int[][] world;//Note: world is upside down
	ArrayList<StaticObject> statics;
	ArrayList<int[][]> setpieces;
	//Player Array
	ArrayList<PlayerObject> players;
	//Enemy Array
	ArrayList<EnemyObject> enemies;
	//PowerUp Array
	ArrayList<PowerObject> powerups;
	//axe array
	ArrayList<AxeObject> axes;

	//images/sprites/spritesheets

	Image block,  player1Img, player2Img, batImg, bgImage;
	Image playerKnightImg, playerAxeKnightImg;
	Image knightImage;
	Image axeKnightImage;
	Image powerUpImg;
	//pause menu image
	Image tempImage;
	SpriteSheet powerUpSprite;
	SpriteSheet axeSheet;
	
	static Sound[] knightSounds;
	
	int bgHeight;

	int genDex; //counts down to next genBlock event.
	//Each block of scroll decrements genDex, if world.length-screenblocks, gen a new section of tower
	boolean firstTime;
	double accScroll;//accumulatedScroll
	boolean start, firstStart;
	double timeElapsed;
	double batSpawnProb;
	double batSpawnRateCountdown;	
	double knightSpawnProb;
	double knightSpawnRateCountdown;
	double KNIGHTSPAWNLIMIT = 1000;
	double powerUpProb;
	double powerUpRateCountdown;
	double POWERUPLIMIT = 5000;
	int powerupNum = 2;
	
	int knightConfig;

	boolean bgParity;

	int totalScore;
	int p1ScoreDelayed;
	int p2ScoreDelayed;
	int gameOverDelay;
	public int stateID = 1;
		
	SoundSystem mySoundSystem;
	float musicStart;//SUPER HACKY
	
	Controller c1, c2;
	boolean c1Exist, c2Exist;
	
	public GameplayState(int i, boolean renderoff) {
		super();
		stateID = i;
		black = renderoff;
	}

	@Override
	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException{
		mySoundSystem.queueSound("MainMenuMusic", "jumpSong.wav");
		//black = true;
		//init(gc, sbg);
	}

	@Override
	public void leave(GameContainer gc, StateBasedGame sbg) throws SlickException{
		//black = true;
	}

	public void setBlack(boolean b){
		black = b;
	}
	
	public void setControllers(Controller c1, Controller c2) {
		this.c1 = c1;
		this.c2 = c2;
		c1Exist = (c1 != null);
		c2Exist = (c2 != null);
	}

	//@Override
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
		
		//SoundStore.get().setMaxSources(32);
		
		black = true;
		
		statics = new ArrayList<StaticObject>();
		axes = new ArrayList<AxeObject>();
		
		enemies = new ArrayList<EnemyObject>();

//		enemies.add(new enemyAxeKnight(100, 1100, axeKnightImage, 1));
		
		powerups = new ArrayList<PowerObject>();

		if(spawns){
			if(knightMode){
				powerups.add(new PowerObject(300, 1100, 2, 20, mySoundSystem));
				powerups.add(new PowerObject(500, 1100, 2, 20, mySoundSystem));
			}else{
				powerups.add(new PowerObject(300, 1100, 0, 20, mySoundSystem));
				powerups.add(new PowerObject(500, 1100, 1, 20, mySoundSystem));
					
			}
		}
//		block = new Image("resources/block.png");
//		bgImage = new Image("resources/bg.png");
//
//		player1Img = new Image("resources/player1Sprite.png");
//		player2Img = new Image("resources/player2Sprite.png");
//
//		batImg = new Image("resources/batSheet.png");
//
//		knightImage = new Image("resources/knightSheet.png");
//		
//		axeKnightImage = new Image("resources/axeKnightSheet.png");
//		
//		powerUpImg = new Image("resources/pwups.png");
//		
//		powerUpSprite = new SpriteSheet(powerUpImg,20,20);
//		
//		axeSheet = new SpriteSheet(new Image("resources/axeSheet.png"), 20, 20);
		
		block = ((Stanford)sbg).block;
		bgImage = ((Stanford)sbg).bgImage;
		player1Img = ((Stanford)sbg).player1Img;
		player2Img = ((Stanford)sbg).player2Img;
		batImg = ((Stanford)sbg).batImg;
		knightImage = ((Stanford)sbg).knightImage;
		axeKnightImage = ((Stanford)sbg).axeKnightImage;
		powerUpImg = ((Stanford)sbg).powerUpImg;
		playerKnightImg = ((Stanford)sbg).blueKnightImage;
		playerAxeKnightImg = ((Stanford)sbg).blueAxeKnightImage;
		
		powerUpSprite = ((Stanford)sbg).powerUpSprite;
		
		axeSheet = ((Stanford)sbg).axeSheet;
		
		tempImage = new Image(screenWidth, screenHeight);
		
		//test initialization information.
		
		int[] player1Input = {Input.KEY_UP, Input.KEY_LEFT, Input.KEY_RIGHT, Input.KEY_DOWN};
		int[] player2Input = {Input.KEY_W, Input.KEY_A, Input.KEY_D, Input.KEY_S};
		players = new ArrayList<PlayerObject>();
		if(knightMode){
			if(solo){
				if(knightConfig==0){
					players.add(new PlayerKnightObject(390,1100, playerKnightImg,((Stanford)sbg).pwupAmmoSprite, player1Input, 0, mySoundSystem, c1, c1Exist));
				}else{
					players.add(new PlayerAxeKnightObject(sbg, 390,1100, playerAxeKnightImg,((Stanford)sbg).pwupAmmoSprite, player1Input, 0, mySoundSystem, c1, c1Exist));
				}
			}else{
				if(knightConfig==1){
					players.add(new PlayerKnightObject(600,1100, playerKnightImg,((Stanford)sbg).pwupAmmoSprite, player1Input, 0, mySoundSystem, c1, c1Exist));
					players.add(new PlayerAxeKnightObject(sbg, 200,1100, playerAxeKnightImg,((Stanford)sbg).pwupAmmoSprite, player2Input, 1, mySoundSystem, c2, c2Exist));
				}else{
					players.add(new PlayerAxeKnightObject(sbg, 600,1100, playerAxeKnightImg,((Stanford)sbg).pwupAmmoSprite, player1Input, 0, mySoundSystem, c1, c1Exist));
					players.add(new PlayerKnightObject(200,1100, playerKnightImg,((Stanford)sbg).pwupAmmoSprite, player2Input, 1, mySoundSystem, c2, c2Exist));
				}
			}
		}else{
			if(!solo){
				players.add(new PlayerManObject(sbg, 600, 1100, player1Img,((Stanford)sbg).pwupAmmoSprite, player1Input, 0, mySoundSystem, c1, c1Exist));
				players.add(new PlayerManObject(sbg, 200, 1100, player2Img,((Stanford)sbg).pwupAmmoSprite, player2Input, 1, mySoundSystem, c2, c2Exist));
			}else{
				//enemies.add(new EnemyKnight(700, 1100, knightImage, -1,mySoundSystem));//testing
				//enemies.add(new EnemyAxeKnight(700, 900, axeKnightImage, -1,mySoundSystem));//testing
				players.add(new PlayerManObject(sbg, 400, 1100, player1Img,((Stanford)sbg).pwupAmmoSprite, player1Input, 0, mySoundSystem, c1, c1Exist));
			}

		}
		//test 3?
				//UNCOMMENT THIS CODE HERE TO SPAWN A THIRD PLAYER!
		//    U
		//  H J K
		//        int[] player3Input = {Input.KEY_U, Input.KEY_H, Input.KEY_K, Input.KEY_J};
		//        players.add(new playerObject(700, 1100, player1Img, player3Input, 2, false));

		Input input = gc.getInput();
		for(PlayerObject p: players)
			input.addKeyListener(p);
		
		batSpawnProb=0;

		knightSpawnProb = 0;
		knightSpawnRateCountdown = 0;

		powerUpProb = 0;
		powerUpRateCountdown = 0;

		totalScore = 0;
		p1ScoreDelayed=0;
		p2ScoreDelayed=0;

		totalScore = 0;

		//Read in setpieces
		setpieces = new ArrayList<int[][]>();

		try {
			FileInputStream fstream = new FileInputStream("resources/setpieces.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int lines = 0;
			String readLine;
			while ((strLine = br.readLine()) != null)   {
				lines = Integer.parseInt(strLine);
				int[][] setPieceBlock = new int[lines][41];
				for(int i=0;i<lines;i++){
					int count = 0;
					readLine = br.readLine();
					for(int j=0;j<readLine.length();j++){
						setPieceBlock[lines-i-1][j] = Integer.parseInt(readLine.substring(j,j+1));
						if(setPieceBlock[lines-i-1][j] == 1)
							count++;
					}
					setPieceBlock[lines-i-1][40] = count;
					
					//printRow(setPieceBlock[lines-i-1]);
					
				}
				setpieces.add(setPieceBlock);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		world = new int[2*screenHeight/blockSize][screenWidth/blockSize+1];//Add more height for preloading and deleting//+1th column for #1s
		//Generate the first screen //Have starting setpiece
		writeToWorld(setpieces.get(0),world.length-1);

		genDex = world.length-setpieces.get(0).length-1;

		//Random spaces//Should have genCoupleRows instead of genRow, each CoupleRows is a row and up to maxJump spaces
		genRowSet();

		//making all spaces in "void" walled
		for (int i = 0; i < genDex; i++) {
			writeToWorld(genSpace(), i);
		}

		//        printWorld();

		bgHeight = screenHeight;

		firstTime = true;
		start = false;
		firstStart = false;
		accScroll = 0;
		timeElapsed = 0;
		gameOver = false;
		gameOverDelay = 50;

		batSpawnRateCountdown = -1;
		bgParity = true;
		
		musicStart = 0;
	}

	//When giving screen coordinates to blocks, offset to be screen
	private void writeToWorld(int[] newRow, int startRow){//Write rows into world array starting at start and going up
		for(int i=0;i<newRow.length;i++){
			world[startRow][i]=newRow[i];
			if(newRow[i] == 1){
				statics.add(new StaticObject(i*blockSize/*colPos*/, startRow*blockSize, blockSize));
			}
		}        
	}
	private void writeToWorld(int[][] newRows, int start){//Write rows into world array starting at start and going up
		for(int i=start;i>start-newRows.length;i--)
			for(int j=0;j<newRows[0].length;j++){
				world[i][j]=newRows[start-i][j];
				if(newRows[start-i][j] == 1){
					statics.add(new StaticObject(j*blockSize/*colPos*/, i*blockSize, blockSize));
				}
			}
	}

	private boolean rowCheck(int[] row, int rowDex){//checks if row is accesible by the previous row
		boolean good = true;
		//get most recent row;
		int dex = rowDex;
		//System.out.println(dex);
		//Find the next non-empty row to compare to
		while(true){
			if(world[dex][40]>2){
				break;
			}else{
				dex++;
			}
		}
		//going from world[dex] to row
		//See if block is blocked off
		int blocks = 0;
		int impossibleblocks = 0;
		for(int i=1;i<39;i++){
			if(world[dex][i]==1){
				blocks++;
				if(row[i-1]==1 && row[i]==1 && row[i+1]==1){
					impossibleblocks++;
				}
			}
		}

		//System.out.println("blocks: "+blocks+", impBlocks: "+impossibleblocks);
		//printRow(row);

		//If all the blocks are blocked off, reject the line
		if(blocks==impossibleblocks){
			good=false;
		}
		
		//Need to be at least two blocks within jumping distance of the next row
		//If 6 tall max 6 blocks away can make jump
		//If 5 tall max 7
		blocks=0;//now counts jumptoreachable blocks
		int hDiff = dex-rowDex;
		int reqSpace=0;
		if(hDiff==5){
			reqSpace=7;
		}else{
			reqSpace=6;
		}
		for(int i=1+reqSpace;i<39-reqSpace;i++){
			if(world[dex][i]==1 && !(row[i-1]==1&&row[i]==1&&row[i+1]==1)){
				for(int j=i-reqSpace;j<=i+reqSpace;j++){
					if(row[j]==1 && j!=i){
						blocks++;
						break;
					}
				}
			}
		}
		
		if(blocks<2)
			good = false;
		
		return good;
	}

	private void genRowSet(){
		while(genDex >= screenHeight/blockSize){
			double pick = Math.random();//picks random random or setpieces
			if(pick<=.75){//random random row		
				int spaces = (int)(Math.random()*1)+6;
				for(int j=0;j<spaces;j++){
					int[] tempSpaceRow = genSpace();
					writeToWorld(tempSpaceRow,genDex);
					genDex--;
				}

				int[] tempRow = genRow(genDex);
				while(!rowCheck(tempRow,genDex)){
					tempRow = genRow(genDex);
				}

				writeToWorld(tempRow,genDex);

				genDex--;
			}else if(.75 < pick && pick < 1.0){//random setpiece

				int spaces = (int)(Math.random()*2)+5;
				for(int j=0;j<spaces;j++){
					int[] tempSpaceRow = genSpace();
					writeToWorld(tempSpaceRow,genDex);
					genDex--;
				}

				//System.out.println(genDex);
				
				int setPieceChoice = (int)(Math.random()*(setpieces.size()-1))+1;    			
				int[][] tempSetPiece = setpieces.get(setPieceChoice);
				while(!rowCheck(tempSetPiece[0],genDex)||((genDex-tempSetPiece.length)<0)){
					setPieceChoice = (int)(Math.random()*(setpieces.size()-1))+1;    			
					tempSetPiece = setpieces.get(setPieceChoice);
				}

				writeToWorld(tempSetPiece, genDex);
				genDex -= tempSetPiece.length;
			}
		}
	}

	private int[] genRow(int rowPos){
		int[] tempRow = new int[world[0].length];
		for(int i=0;i<tempRow.length-1;i++){//Put randoms except last box, which is the count		
			double blockProb = Math.random();
			if(blockProb<=.6){
				tempRow[i] = 1;//Makes 0 or 1 //Add Heuristics
			}else{
				tempRow[i] = 0;
			}
		}

		//Go through, eliminate one blocks
		for(int i=2;i<tempRow.length-2;i++){
			if(tempRow[i-1]==tempRow[i+1] && tempRow[i]!=tempRow[i-1])
				tempRow[i]=tempRow[i-1];
		}

		if(tempRow[1]!=tempRow[2])
			tempRow[1]=tempRow[2];
		if(tempRow[tempRow.length-3]!=tempRow[tempRow.length-4])
			tempRow[tempRow.length-3]=tempRow[tempRow.length-4];
		
		//No one block gaps/ledges at the edge
		tempRow[0]=1;
		tempRow[tempRow.length-2]=1;

		//Count #1s for later use in ArrayList maintenance
		int count = 0;
		for(int i=0;i<tempRow.length-1;i++)
			if(tempRow[i]==1)
				count++;
		tempRow[tempRow.length-1] = count;
		return tempRow;
	}

	private int[] genSpace(){
		int[] tempRow = new int[world[0].length];
		for(int i=0;i<tempRow.length;i++)
			tempRow[i]=0;

		tempRow[0]=1;
		tempRow[tempRow.length-2]=1;
		tempRow[tempRow.length-1]=2;

		return tempRow;
	}

	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {

		g.translate(0, -screenHeight - 1);

		if (black) {
			// Draw Nothing
		} else {

			g.drawImage(bgImage, 0, bgHeight % (2 * screenHeight));
			g.drawImage(bgImage, 0, (bgHeight + screenHeight)
					% (2 * screenHeight));

			for (StaticObject statOb : statics) {
				// g.drawRect(statOb.pos.x, statOb.pos.y, statOb.l, statOb.l);
				// g.drawRect(statOb.p.getX(), statOb.p.getY(), statOb.l,
				// statOb.l);
				g.drawImage(block, statOb.pos.x, statOb.pos.y);
				if (statOb.coll) {
					// g.drawOval(statOb.p.getCenterX(), statOb.p.getCenterY(),
					// 2, 2);
					statOb.coll = false;
				}
				// g.drawOval(statOb.p.getCenterX(), statOb.p.getCenterY(), 2,
				// 2);
				// g.drawOval(statOb.pos.x, statOb.pos.y, 5, 5);
			}

			for (PowerObject powerOb : powerups) {
//				powerOb.render(g);
				powerUpSprite.startUse();
				powerUpSprite.renderInUse((int)powerOb.p.getX(), (int)powerOb.p.getY(), powerOb.index, 0);
				powerUpSprite.endUse();
			}
			
			for (EnemyObject enemyOb : enemies) {
				enemyOb.render(g);
			}
			
			for (PlayerObject playerOb : players) {
				// g.drawImage(playerImg, playerOb.pos.x, playerOb.pos.y);
				playerOb.render(g);
			}
			
			for (AxeObject axe : axes) {
//				g.drawOval(axe.pos.x, axe.pos.y, axe.l, axe.l);
				axe.render(g);
			}
			
			// draw scores
			// score length: 7 characters: 35 pixels
			if (!solo) {
				g.setColor(new Color(0, 0, 0, 0.4f));
				g.fillRect(250, screenHeight + 30, 90, 30);
				g.fillRect(460, screenHeight + 30, 90, 30);
				g.setColor(Color.white);
				g.drawRect(250, screenHeight + 30, 90, 30);
				g.drawRect(460, screenHeight + 30, 90, 30);
			}
			g.setColor(new Color(0, 0, 0, 0.4f));
			g.fillRect(340, screenHeight + 20, 120, 30);
			g.setColor(Color.white);
			g.drawRect(340, screenHeight + 20, 120, 30);

			// p1 on the right
			// String score1Draw = ""+players.get(0).score;
			String score1Draw = "" + p1ScoreDelayed;
			while (score1Draw.length() < 8)
				score1Draw = "0" + score1Draw;
			String score2Draw = "" + p2ScoreDelayed;// players.get(1).score;
			while (score2Draw.length() < 8)
				score2Draw = "0" + score2Draw;
			
			if (!solo) {
				g.getFont().drawString(468, screenHeight + 37, score1Draw);
				g.getFont().drawString(258, screenHeight + 37, score2Draw);
			}

			String totalScoreDraw = "" + (p2ScoreDelayed + p1ScoreDelayed);
			while (totalScoreDraw.length() < 8)
				totalScoreDraw = "0" + totalScoreDraw;

			g.getFont().drawString(screenWidth / 2 - 35, screenHeight + 27,
					totalScoreDraw);
			
			//Draw the +Score on the screen
			int tempScore = players.get(0).getScoreAdded();
			int tempScoreTimer = players.get(0).getScoreAddedTimer();
			if(!solo){
				if(tempScore>0){
					if(tempScore>=100){
						g.getFont().drawString(505, screenHeight+(int)(tempScoreTimer/20)+5, "+"+tempScore);
					}else{
						g.getFont().drawString(515, screenHeight+(int)(tempScoreTimer/20)+5, "+"+tempScore);
					}
				}
				tempScore = players.get(1).getScoreAdded();
				tempScoreTimer = players.get(1).getScoreAddedTimer();
				if(tempScore>0){
					g.getFont().drawString(258,screenHeight+(int)(tempScoreTimer/20)+5, "+"+tempScore);
				}
			}else{
				if(tempScore>0){
					if(tempScore>=100){
						g.getFont().drawString(400, screenHeight+(int)(tempScoreTimer/20)-5, "+"+tempScore);
					}else{
						g.getFont().drawString(410, screenHeight+(int)(tempScoreTimer/20)-5, "+"+tempScore);
					}
				}
			}

		}
	}

	//@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {

		Input input = gc.getInput();

		musicStart+=delta;
		if(musicStart>=6000){
			mySoundSystem.queueSound("MainMenuMusic", "jumpSong.wav");
		}
		
		if (!gc.hasFocus()) {		
			gc.getGraphics().copyArea(tempImage, 0, 0);
			((GameMenuState) sbg.getState(GAMEMENUSTATE)).setImage(tempImage);
			sbg.enterState(GAMEMENUSTATE);
		}

		if (!firstStart) {
			int heightLim = 850;//Limit at which to start scrolling up
			if(!spawns)
				heightLim = 650;
			firstStart = true;
			for (PlayerObject playerOb : players) {
				
				if (playerOb.pos.y > heightLim) {
					firstStart = false;
				} //end if player height check
			} //end player iterator
			if (firstStart) {
				start = true;
			}
		} //end if

		//spacebar -> scrolling control
		if(input.isKeyPressed(Input.KEY_SPACE)){
			if (!firstStart) {
				firstStart = true;
			}
			start = !start;
			//start = true;
		}else if (input.isKeyPressed(Input.KEY_ESCAPE)
				|| players.get(0).getMenuCall() 
				|| (players.size()>1 ? players.get(1).getMenuCall() : false) ){
			//init(gc, sbg);
			gc.getGraphics().copyArea(tempImage, 0, 0);
			((GameMenuState) sbg.getState(GAMEMENUSTATE)).setImage(tempImage);
			leave(gc, sbg);
			sbg.enterState(GAMEMENUSTATE);
		}else if (input.isKeyPressed(Input.KEY_B)){
			if(BATSPAWNLIMIT!=10){
				BATSPAWNLIMIT = 10;
			}else{
				BATSPAWNLIMIT = 600;
			}
		} else if (input.isKeyPressed(Input.KEY_K)) {
			if (KNIGHTSPAWNLIMIT != 100) {
				KNIGHTSPAWNLIMIT = 100;
			} else {
				KNIGHTSPAWNLIMIT = 3000;
			}
		} else if (input.isKeyPressed(Input.KEY_P)) {
			if (POWERUPLIMIT != 10) {
				POWERUPLIMIT = 10;
			} else {
				POWERUPLIMIT = 5000;
			}
		}
		else {
			input.clearKeyPressedRecord();
		}

		timeElapsed+=delta;

		totalScore = 0;

		gameOver = true;
		for (PlayerObject playerOb : players) {
			playerOb.update(sbg, delta, statics, players, enemies, axes, powerups);
			
			totalScore += playerOb.score;
			if (playerOb.alive())
				gameOver = false;
		}

		for (EnemyObject enemyOb : enemies) {
			enemyOb.update(sbg, delta, statics, players, enemies, axes);
		}
		
		for (AxeObject axe : axes) {
			axe.update(sbg, delta, statics, players, enemies, powerups, axes);
		}
		
		int axesSize = axes.size();
		for (int i= 0; i < axesSize; i++) {
			AxeObject axe = axes.get(i);
			if (axe.frameCounter < 0) {
				axes.remove(i);
				//System.out.println("removed axe!");
				axesSize--;
				i--;
			}
		}
		
		//powerups!
		for (PowerObject powerOb : powerups) {
			powerOb.update(sbg, delta, statics, players, enemies);
		}

		//Eliminate used powerups
		int powerSize = powerups.size();
		for(int i=0;i<powerSize;i++){
			PowerObject tempPower = powerups.get(i);
			if(tempPower.coll || tempPower.p.getY() > 1200) {
				powerups.remove(i);

				//System.out.println("removed enemy:"+i);

				powerSize--;
				i--;
			} //end pickup check
		} //end array iteration

		//Eliminate dead enemies
		int enemSize = enemies.size();
		for(int i=0;i<enemSize;i++){
			EnemyObject tempEnemy = enemies.get(i);
			if(!tempEnemy.alive() && tempEnemy.deathFrames<=0){
				enemies.remove(i);

				//System.out.println("removed enemy:"+i);

				enemSize--;
				i--;
			}
		}

		// checking for player death gameover
		if (gameOver) {
			// delay gameoverstate
			gameOverDelay--;
			if (gameOverDelay <= 0) {
				// go to gameoverstate
				((GameOverState) sbg.getState(GAMEOVERSTATE)).setWorld(world,
						statics, setpieces, players, enemies, axes, powerups, genDex,
						accScroll, timeElapsed, bgParity, bgHeight, spawns, solo);
				((GameOverState) sbg.getState(GAMEOVERSTATE)).setNewScore(totalScore);
				if(!solo){
					((GameOverState) sbg.getState(GAMEOVERSTATE)).setScores(players.get(0).score,players.get(1).score,totalScore);
				}else{
					((GameOverState) sbg.getState(GAMEOVERSTATE)).setScores(players.get(0).score,0,totalScore);
				}
				((GameOverState) sbg.getState(GAMEOVERSTATE)).setKnight(knightMode);
				((GameOverState) sbg.getState(GAMEOVERSTATE)).setKnightConfig(knightConfig);
				String scoresName;
				if(!spawns && !solo){
					scoresName = "resources/highscoresP.txt";
				}else if(solo && spawns){
					scoresName = "resources/highscoresS.txt";
				}else if(solo && !spawns){
					scoresName = "resources/highscoresPS.txt";
				}else{
					scoresName = "resources/highscores.txt";
				}
				
				((GameOverState) sbg.getState(GAMEOVERSTATE)).setScoresName(scoresName);
				((GameOverState) sbg.getState(GAMEOVERSTATE)).setPlayApplause(true);
				leave(gc, sbg);
				sbg.enterState(GAMEOVERSTATE);
			} // end gameover delay chunk
		} // end gameover check

		if(p1ScoreDelayed<players.get(0).score)
			p1ScoreDelayed+=1;
		if(!solo && p2ScoreDelayed<players.get(1).score)
			p2ScoreDelayed+=1;


		if(start){

			double shift = 1.0*delta/24.0 * (1.0);//Make it faster as times goes on. Can handle x2, x3 lags world gen
			if(!spawns){
				if(bgParity){
					shift*=3;
				}else{
					shift*=4;
				}
			}
			for(StaticObject statOb : statics){
				statOb.shiftDown(shift);
			}
			for(PlayerObject playerOb : players){
				playerOb.shiftDown(shift);
			}
			for (EnemyObject enemyOb : enemies) {
				enemyOb.shiftDown(shift);
				//System.out.println("eneH:"+enemyOb.health);
			}
			for (PowerObject powerOb : powerups) {
				powerOb.shiftDown(shift);
			}
			for (AxeObject axe : axes) {
				axe.shiftDown(shift);
			}

			//System.out.println();

			accScroll+=shift;

			//generate from the top    
			if(accScroll>=blockSize){
				accScroll-=blockSize;
				//Erase blocks from statics, shift array down
				if(world[world.length-1][world[0].length-1]!=0){
					for(int i=0;i<world[world.length-1][world[0].length-1];i++){
						statics.remove(0);
					}
				}
				for(int i = world.length-1; i>0; i--){
					for(int j=0;j<world[0].length;j++){
						world[i][j]=world[i-1][j];
					}
				}
				genDex++;
			}

			genRowSet();
			if(bgParity){
				bgHeight += shift*1.5;
				bgParity=!bgParity;
			}else{
				bgParity=!bgParity;
			}

			if(spawns){
				//Randomly modify BATSPAWNLIMIT between a couple values
				if(batSpawnRateCountdown<0){
					double temp = Math.random();
					if(temp<.3){
						BATSPAWNLIMIT = BATSPAWNMED;
						batSpawnRateCountdown = 1000;
					}else if(temp<.9){
						BATSPAWNLIMIT = BATSPAWNLOW;
						batSpawnRateCountdown = 1000;
					}else{
						BATSPAWNLIMIT = BATSPAWNHIGH;
						batSpawnRateCountdown = 600;
					}
				}
				batSpawnRateCountdown -= delta;
	
	
				batSpawnProb+=(Math.random()*delta);
				if(batSpawnProb>BATSPAWNLIMIT){
					batSpawnProb=0;
					if(Math.random()<.5){
						enemies.add(new EnemyBat(-40,300+(int)(Math.random()*600),batImg,1, mySoundSystem));
					}else{
						enemies.add(new EnemyBat(840,300+(int)(Math.random()*600),batImg,-1,mySoundSystem));
					}
					//System.out.println(tempBat);
					//System.out.println("AddBat");
				}
	
				//knight spawning code
				if (knightSpawnRateCountdown < 0) {
					knightSpawnRateCountdown = 1000;//doesn't do anything right now
					KNIGHTSPAWNLIMIT = KNIGHTSPAWNMED;
				}
	
				knightSpawnRateCountdown -= delta;
				knightSpawnProb += (Math.random()*delta);
				if (knightSpawnProb > KNIGHTSPAWNLIMIT) {
					boolean axeBool = (Math.random() < .5);
					knightSpawnProb = 0;
					if (world[30][40] != 2 && world[30][40] != 0) { //checking to make sure there are said blocks
						//        			printWorld();
						//        			System.out.println("spawn knight!");
						int tempInt = (int)(Math.random()*(screenWidth/blockSize - 4) + 2);
						//        			System.out.println(tempInt);
						if (world[30][tempInt] == 1) {
							//        				System.out.println("on block");
							if (tempInt < screenWidth/blockSize/2) {
								if (axeBool) {
									enemies.add(new EnemyAxeKnight(tempInt*blockSize, -20, axeKnightImage, 1, mySoundSystem));
								} else {
									enemies.add (new EnemyKnight(tempInt*blockSize, -20, knightImage, 1, mySoundSystem));
								}
							} else {
								if (axeBool) {
									enemies.add(new EnemyAxeKnight(tempInt*blockSize, -20, axeKnightImage, -1,mySoundSystem));
								} else {
									enemies.add(new EnemyKnight(tempInt*blockSize, -20, knightImage, -1, mySoundSystem));
								}
							} //end spawning code
						} else { //if no block exists
							//        				System.out.println("off block");
							if (tempInt > screenWidth/blockSize/2) {
								while (world[30][tempInt] != 1) {
									tempInt--;
								}
								if (tempInt > 1) {
									if (axeBool) {
										enemies.add(new EnemyAxeKnight(tempInt*blockSize, -20, axeKnightImage, -1,mySoundSystem));
									} else {
										enemies.add(new EnemyKnight(tempInt*blockSize, -20, knightImage, -1, mySoundSystem));
									}
								} else {
									tempInt = 1;
									while (world[30][tempInt] != 1) {
										tempInt++;
									}
									if (axeBool) {
										enemies.add(new EnemyAxeKnight(tempInt*blockSize, -20, axeKnightImage, 1,mySoundSystem));
									} else {
										enemies.add(new EnemyKnight(tempInt*blockSize, -20, knightImage, 1, mySoundSystem));
									}
								}
							} else {
								while (world[30][tempInt] != 1) {
									tempInt++;
								}
								if (tempInt < 38) {
									if (axeBool) {
										enemies.add(new EnemyAxeKnight(tempInt*blockSize, -20, axeKnightImage, 1,mySoundSystem));
									} else {
										enemies.add(new EnemyKnight(tempInt*blockSize, -20, knightImage, 1, mySoundSystem));
									}
								} else {
									tempInt = 38;
									while (world[30][tempInt] != 1) {
										tempInt--;
									}
									if (axeBool) {
										enemies.add(new EnemyAxeKnight(tempInt*blockSize, -20, axeKnightImage, -1,mySoundSystem));
									} else {
										enemies.add(new EnemyKnight(tempInt*blockSize, -20, knightImage, -1, mySoundSystem));
									}
								}
							}
						}
						//					System.out.println(tempInt + " " + world[30][40]);
					}
				} //end knight spawn code
	
				//powerup spawning code
				if (powerUpRateCountdown < 0) {
					powerUpRateCountdown = 100;
					POWERUPLIMIT = 5000;
				}
	
				powerUpRateCountdown -= delta;
				powerUpProb += (Math.random()*delta);
				if (powerUpProb > POWERUPLIMIT) {
					powerUpProb = 0;
					if (world[30][40] != 2 && world[30][40] != 0) { //checking to make sure there are said blocks
						//        			printWorld();
						//System.out.println("spawn powerup!");
						int tempInt = (int)(Math.random()*(screenWidth/blockSize - 4) + 2);
						//        			System.out.println(tempInt);
						if (world[30][tempInt] == 1) {
							//System.out.println("on block");
							if(knightMode){
								powerups.add(new PowerObject(tempInt*blockSize, -20, 2, 20,mySoundSystem));
							}else{
								powerups.add(new PowerObject(tempInt*blockSize, -20, (int)(Math.random()*powerupNum), 20,mySoundSystem));
							}
						} else { //if no block exists
							//System.out.println("off block");
							if (tempInt > screenWidth/blockSize/2) {
								while (world[30][tempInt] != 1) {
									tempInt--;
								}
								if (tempInt > 1) {
									if(knightMode){
										powerups.add(new PowerObject(tempInt*blockSize, -20, 2, 20,mySoundSystem));
									}else{
										powerups.add(new PowerObject(tempInt*blockSize, -20, (int)(Math.random()*powerupNum), 20,mySoundSystem));
									}
								} else {
									tempInt = 1;
									while (world[30][tempInt] != 1) {
										tempInt++;
									}
									if(knightMode){
										powerups.add(new PowerObject(tempInt*blockSize, -20, 2, 20,mySoundSystem));
									}else{
										powerups.add(new PowerObject(tempInt*blockSize, -20, (int)(Math.random()*powerupNum), 20,mySoundSystem));
									}
								}
							} else {
								while (world[30][tempInt] != 1) {
									tempInt++;
								}
								if (tempInt < 38) {
									if(knightMode){
										powerups.add(new PowerObject(tempInt*blockSize, -20, 2, 20,mySoundSystem));
									}else{
										powerups.add(new PowerObject(tempInt*blockSize, -20, (int)(Math.random()*powerupNum), 20,mySoundSystem));
									}
								} else {
									tempInt = 38;
									while (world[30][tempInt] != 1) {
										tempInt--;
									}
									if(knightMode){
										powerups.add(new PowerObject(tempInt*blockSize, -20, 2, 20,mySoundSystem));
									}else{
										powerups.add(new PowerObject(tempInt*blockSize, -20, (int)(Math.random()*powerupNum), 20,mySoundSystem));
									}
								}
							}
						}
						//System.out.println(tempInt + " " + world[30][40]);
					}
				} //end powerup spawn code
				
			} //end "if spawning stuff code"
		}//end if start
	}

	private void printWorld(){
		for(int i=0;i<world.length;i++){
			for(int j=0;j<world[0].length;j++){
				System.out.print(world[i][j]);
			}
			System.out.println();
		}
		System.out.println();
	}

	private void printRow(int[] row){
		for(int i=0;i<row.length;i++){
			System.out.print(row[i]);
		}
		System.out.println();
	}

	public void setSolo(boolean b){
		solo = b;
	}
	
	public void setSpawns(boolean b){
		spawns = b;
	}
	
	public void setKnight( boolean b){
		knightMode = b;
	}
	
	public void setKnightConfiguration(int n){
		knightConfig = n;
	}
	
	public void setSoundSystem(SoundSystem ss){
		mySoundSystem=ss;
	}
	
	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return stateID;
	}

}
