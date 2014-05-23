package org.ssg.Stanford;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

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
import org.newdawn.slick.Sound;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

//reads in high scores, adds new one if necessary
//show if you made a high score that time
//lets you restart or exit

//names limit length
//sort things proper

public class GameOverState extends BasicGameState{

	int stateID;
	int[] scores;
	String[] names;
	int newScore;
	String newName;
	int p1finalScore;
	int p2finalScore;
	int totalfinalScore;
	
	SpriteSheet blockSprites;
    Image player1Img, player2Img, batImg, bgImage, tempImage;
	Image knightImage;
	Image axeKnightImage;
	Image powerUpImg;
	SpriteSheet powerUpSprite;
	SpriteSheet axeSheet;
	SpriteSheet heads;
	
	static Sound[] knightSounds;
	
	int bgHeight;
    
    int genDex; //counts down to next genBlock event.
                //Each block of scroll decrements genDex, if world.length-screenblocks, gen a new section of tower
    boolean firstTime;
    double accScroll;//accumulatedScroll
    double timeElapsed;
    double batSpawnProb;
    double batSpawnRateCountdown;
    boolean bgParity;
    
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
	
    double knightSpawnProb;
    double knightSpawnRateCountdown;
    double KNIGHTSPAWNLIMIT;
    
    double powerUpProb;
	double powerUpRateCountdown;
	double POWERUPLIMIT = 5000;
	int powerupNum = 2;
	
	boolean spawns;
	boolean solo;
    boolean knight;
    int knightConfig;
	
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
    
    boolean nameSet;
	String scoresName; 
	boolean playApplause;
	
	SoundSystem mySoundSystem;
	
	boolean c1Exist;
    
	public GameOverState(int i){
		super();
		stateID = i;
		scores = new int[20];
		names = new String[20];
	}
	
	public void setController(Controller c1) {
		c1Exist = (c1 != null);
	}
	
	public void initScores(){
		nameSet = false;
		newName = "";
		try {
			
			FileInputStream fstream = new FileInputStream(scoresName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			for(int i=0;i<20;i++){
				scores[i] = Integer.parseInt(br.readLine());
			}
			for(int i=0;i<20;i++){
				names[i] = br.readLine();
			}
			br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException{
		initScores();
	}
	
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		
		//SoundStore.get().setMaxSources(32);
		
		initScores();
		
//        block = new Image("resources/block.png");
//        bgImage = new Image("resources/bg.png");
//        
//        player1Img = new Image("resources/player1Sprite.png");
//        player2Img = new Image("resources/player2Sprite.png");
//        
//        batImg = new Image("resources/batSheet.png");
//		
//        knightImage = new Image("resources/knightSheet.png");
        
		blockSprites = new SpriteSheet(((Stanford)sbg).blockSprites, 20, 20);
		bgImage = ((Stanford)sbg).bgImage;
		player1Img = ((Stanford)sbg).player1Img;
		player2Img = ((Stanford)sbg).player2Img;
		batImg = ((Stanford)sbg).batImg;
		knightImage = ((Stanford)sbg).knightImage;
		axeKnightImage = ((Stanford)sbg).axeKnightImage;
		powerUpImg = ((Stanford)sbg).powerUpImg;
		powerUpSprite = ((Stanford)sbg).powerUpSprite;
		axeSheet = ((Stanford)sbg).axeSheet;
		heads = ((Stanford)sbg).headIcons;
		
        tempImage = new Image(screenWidth, screenHeight);
        
		statics = new ArrayList<StaticObject>();
        setpieces = new ArrayList<int[][]>();
        enemies = new ArrayList<EnemyObject>();
        players = new ArrayList<PlayerObject>();
        powerups = new ArrayList<PowerObject>();
        //enemies.add(new enemyBat(-50,800,batImages,1));
        batSpawnProb=0;
        knightSpawnProb=0;
        
        powerUpProb = 0;
		powerUpRateCountdown = 0;
        
        bgHeight = screenHeight;
        accScroll = 0;
        timeElapsed = 0;
        
        batSpawnRateCountdown = -1;
        bgParity = true;
        
        p1finalScore = 0;
		p2finalScore = 0;
		totalfinalScore = 0;
        
		knight = false;
		knightConfig = 0;
		
        newScore = -1;
	}

	public void setWorld(int[][] w, ArrayList<StaticObject> s, ArrayList<int[][]> set, ArrayList<PlayerObject> p, ArrayList<EnemyObject> e, ArrayList<AxeObject> a,
			ArrayList<PowerObject> pow, int genDexSent, double accScrollSent, double timeElapsedSent, boolean bgParitySent, int bgHeightSent, boolean spawnB, boolean soloB){
		world = w;
		statics = s;
		players = p;
		setpieces = set;
		enemies = e;
		axes = a;
		powerups = pow;
		genDex = genDexSent;
		accScroll = accScrollSent;
		timeElapsed = timeElapsedSent;
		bgParity = bgParitySent;
		bgHeight = bgHeightSent;
		spawns = spawnB;
		solo = soloB;
	}
	
	public void setScores(int p1, int p2, int tot){
		p1finalScore = p1;
		p2finalScore = p2;
		totalfinalScore = tot;
	}
	
public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
    	
        g.translate(0,-screenHeight-1);
        
        g.setColor(Color.white);
                
        g.drawImage(bgImage, 0, bgHeight%(2*screenHeight));
        g.drawImage(bgImage, 0, (bgHeight+screenHeight)%(2*screenHeight));
        
		blockSprites.startUse();
		for (StaticObject statOb : statics) {
			// g.drawRect(statOb.pos.x, statOb.pos.y, statOb.l, statOb.l);
			// g.drawRect(statOb.p.getX(), statOb.p.getY(), statOb.l,
			// statOb.l);
			blockSprites.renderInUse((int)statOb.pos.x, (int)statOb.pos.y, statOb.getSpriteX(), statOb.getSpriteY());
			if (statOb.coll) {
				// g.drawOval(statOb.p.getCenterX(), statOb.p.getCenterY(),
				// 2, 2);
				statOb.coll = false;
			}
			// g.drawOval(statOb.p.getCenterX(), statOb.p.getCenterY(), 2,
			// 2);
			// g.drawOval(statOb.pos.x, statOb.pos.y, 5, 5);
		}
		blockSprites.endUse();

        
        for (PowerObject powerOb : powerups) {
//			powerOb.render(g);
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
//			g.drawOval(axe.pos.x, axe.pos.y, axe.l, axe.l);
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
		g.setColor(new Color(0, 0, 0, .7f));
		g.fillRect(340, screenHeight + 20, 120, 30);
		g.setColor(Color.white);
		g.drawRect(340, screenHeight + 20, 120, 30);

		// p1 on the right
		// String score1Draw = ""+players.get(0).score;
		String score1Draw = "" + p1finalScore;
		while (score1Draw.length() < 8)
			score1Draw = "0" + score1Draw;
		String score2Draw = "" + p2finalScore;// players.get(1).score;
		while (score2Draw.length() < 8)
			score2Draw = "0" + score2Draw;

		if (!solo) {
			g.getFont().drawString(468, screenHeight + 37, score1Draw);
			g.getFont().drawString(258, screenHeight + 37, score2Draw);
		}
			
		String totalScoreDraw = "" + totalfinalScore;
		while (totalScoreDraw.length() < 8)
			totalScoreDraw = "0" + totalScoreDraw;

		g.getFont().drawString(screenWidth / 2 - 35, screenHeight + 27,
				totalScoreDraw);
        
        //WORK HERE WORK HERE WORK HERE
        g.setColor(new Color(0, 0, 0, 0.4f));
        g.fillRect(200,700,400,400);
        g.setColor(Color.white);
        g.drawRect(200,700,400,400);
        g.drawString("High Scores",225,725);
        for(int i=0;i<names.length;i++){
        	String[] str = names[i].split(",");
        	if(spawns){
	        	heads.startUse();
	        	for(int j=0;j<str[0].length();j++){
	        		switch(str[0].charAt(str[0].length()-1-j)){
	        		case '1':heads.renderInUse(235-(j*20), 760+15*i, 0, 0);break;
	        		case '2':heads.renderInUse(235-(j*20), 760+15*i, 0, 1);break;
	        		case 'A':heads.renderInUse(235-(j*20), 760+15*i, 0, 2);break;
	        		case 'S':heads.renderInUse(235-(j*20), 760+15*i, 0, 3);break;
	        		}
	        	}
	        	heads.endUse();
        	}

        	g.drawString(str[1],260,760+15*i);
        	g.drawString(""+scores[i], 450, 760+15*i);
        }
        
        if(c1Exist){
        	g.drawString("[Press Start]", screenWidth/2-g.getFont().getWidth("[Press Start]")/2, 1110);
        }else{
        	g.drawString("[Press Esc]", screenWidth/2-g.getFont().getWidth("[Press Esc]")/2, 1110);
        }
        
        if(newScore > scores[scores.length-1] && nameSet == false){
        	g.setColor(Color.black);
        	g.fillRect(100,850,600,100);
        	g.setColor(Color.white);
        	g.drawRect(100,850,600,100);
        	g.drawString("New High Score", 125, 870);
        	g.drawString("Enter Name: "+ newName+"_", 145, 900);
        }
    }

    public void setNewScore(int n){
    	newScore = n;
    	//smallest score at bottom
    }
    
    //Code: A for Axe, S for Sword, 1 for Hatman, 2 for Hair
    public void writeScores(){
    	FileOutputStream fstream;
		try {
			fstream = new FileOutputStream(scoresName);
			DataOutputStream out = new DataOutputStream(fstream);
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(out));
			for(int i=0;i<scores.length;i++){
				br.append(""+scores[i]+"\r\n");
			}
			for(int i=0;i<names.length;i++){
				br.append(names[i]+"\r\n");
			}			
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
    	Input input = gc.getInput();
    	
    	if(newScore > scores[scores.length-1] && playApplause){
    		playApplause = false;
    		mySoundSystem.quickStream(true, "Applause.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, SoundSystemConfig.getDefaultRolloff());
    	}else if(playApplause){
    		mySoundSystem.quickPlay( true, "GameOver.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
    		playApplause = false;
    	}
    	
    	//Check that the name is below a certain limit length
    	if(newScore > scores[scores.length-1] && nameSet == false){
    		if(input.isKeyPressed(Input.KEY_ENTER)){
    			nameSet = true;
    		}
    		else if (input.isKeyPressed(Input.KEY_ESCAPE)) {
    			gc.getGraphics().copyArea(tempImage, 0, 0);
        		((GameOverMenuState) sbg.getState(GAMEOVERMENUSTATE)).setImage(tempImage);
        		leave(gc, sbg);
        		sbg.enterState(GAMEOVERMENUSTATE);
    		}
    		else{
    			if(newName.length()<12){
    				if(input.isKeyPressed(Input.KEY_A)){
    					newName+="A";
    				}else if(input.isKeyPressed(Input.KEY_B)){
    					newName+="B";
    				}else if(input.isKeyPressed(Input.KEY_C)){
    					newName+="C";
    				}else if(input.isKeyPressed(Input.KEY_D)){
    					newName+="D";
    				}else if(input.isKeyPressed(Input.KEY_E)){
    					newName+="E";
    				}else if(input.isKeyPressed(Input.KEY_F)){
    					newName+="F";
    				}else if(input.isKeyPressed(Input.KEY_G)){
    					newName+="G";
    				}else if(input.isKeyPressed(Input.KEY_H)){
    					newName+="H";
    				}else if(input.isKeyPressed(Input.KEY_I)){
    					newName+="I";
    				}else if(input.isKeyPressed(Input.KEY_J)){
    					newName+="J";
    				}else if(input.isKeyPressed(Input.KEY_K)){
    					newName+="K";
    				}else if(input.isKeyPressed(Input.KEY_L)){
    					newName+="L";
    				}else if(input.isKeyPressed(Input.KEY_M)){
    					newName+="M";
    				}else if(input.isKeyPressed(Input.KEY_N)){
    					newName+="N";
    				}else if(input.isKeyPressed(Input.KEY_O)){
    					newName+="O";
    				}else if(input.isKeyPressed(Input.KEY_P)){
    					newName+="P";
    				}else if(input.isKeyPressed(Input.KEY_Q)){
    					newName+="Q";
    				}else if(input.isKeyPressed(Input.KEY_R)){
    					newName+="R";
    				}else if(input.isKeyPressed(Input.KEY_S)){
    					newName+="S";
    				}else if(input.isKeyPressed(Input.KEY_T)){
    					newName+="T";
    				}else if(input.isKeyPressed(Input.KEY_U)){
    					newName+="U";
    				}else if(input.isKeyPressed(Input.KEY_V)){
    					newName+="V";
    				}else if(input.isKeyPressed(Input.KEY_W)){
    					newName+="W";
    				}else if(input.isKeyPressed(Input.KEY_X)){
    					newName+="X";
    				}else if(input.isKeyPressed(Input.KEY_Y)){
    					newName+="Y";
    				}else if(input.isKeyPressed(Input.KEY_Z)){
    					newName+="Z";
    				}
    			}
    			if(newName.length()>0 && input.isKeyPressed(Input.KEY_BACK)){
    				newName = newName.substring(0,newName.length()-1);
    			}
    			input.clearKeyPressedRecord();
    		}
    	}else if (input.isKeyPressed(Input.KEY_ESCAPE)
    			|| players.get(0).getMenuCall() 
				|| (players.size()>1 ? players.get(1).getMenuCall() : false)){
    		//init(gc, sbg);		
    		gc.getGraphics().copyArea(tempImage, 0, 0);
    		((GameOverMenuState) sbg.getState(GAMEOVERMENUSTATE)).setImage(tempImage);
    		leave(gc, sbg);
    		sbg.enterState(GAMEOVERMENUSTATE);
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
    			KNIGHTSPAWNLIMIT = 1000;
    		}
    	} else{
    		input.clearKeyPressedRecord();
    	}
    	
    	timeElapsed+=delta;
    	
    	//set new score
    	if(newScore > scores[scores.length-1]){
    		if(nameSet == true){
    			
    			String prefix = "";
    			if(knight){
    				if(solo){
    					if(knightConfig ==0){
    						prefix = "S";
    					}else{
    						prefix = "A";
    					}
    				}else{
    					if(knightConfig ==0){
    						prefix = "SA";
    					}else{
    						prefix = "AS";
    					}
    				}
    			}else{
    				if(solo){
    					prefix = "1";
    				}else{
    					prefix = "21";
    				}
    			}
    			
    			if(newName.equals("")){
    				newName = "ANONYMOUSE";
    			}
    			
	    		scores[scores.length-1]=newScore;
	    		names[names.length-1]=prefix+","+newName;
	    		//need manual sort to match the scores with the names
	    		
 		     	int first =0;
     			for (int i = scores.length - 1; i > 0; i -- ) {
          		first = 0;   //initialize to subscript of first element
        			for(int j = 1; j <= i; j ++) {
              			if( scores[ j ] < scores[ first ] )         
                 				first = j;
          			}
          			int temp = scores[ first ];   //swap smallest found with element in position i.
          			scores[ first ] = scores[ i ];
          			scores[ i ] = temp; 
				
				String tempStr = names[first];
				names[first] = names[i];
				names[i] = tempStr;
      		}           

	    		//write new scores to text file
	    		writeScores();
	    		newScore = -1;
	    		newName = "";
	    		nameSet = false;
	    	}else{
	    		//nothing
	    	}
    	}    	
    	
    	if(true){

        	for (EnemyObject enemyOb : enemies) {
        		enemyOb.update(sbg, delta, statics, players, enemies, axes);
        	}
    		
        	for (PlayerObject playerOb: players){
        		playerOb.update(sbg, delta, statics, players, enemies, axes, powerups);
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
        	
    		double shift = 1.0*delta/24.0 * (1.0);//Make it faster as times goes on. Can handle x2, x3 lags world gen
//    		if(!spawns){
//				shift*=2;
//			}
    		for(StaticObject statOb : statics){
    			statOb.shiftDown(shift);
    		}
    		for (EnemyObject enemyOb : enemies) {
    			enemyOb.shiftDown(shift);
    			//System.out.println("eneH:"+enemyOb.health);
    		}
    		for (PowerObject powerOb : powerups) {
				powerOb.shiftDown(shift);
			}
    		for(PlayerObject playerOb : players){
				playerOb.shiftDown(shift);
			}
    		for (AxeObject axe : axes) {
				axe.shiftDown(shift);
			}
    		
    		//System.out.println();
    		
    		accScroll+=shift;

    		//Eliminate dead enemies
    		int enemSize = enemies.size();
        	for(int i=0;i<enemSize;i++){
        		EnemyObject tempEnemy = enemies.get(i);
        		if(!tempEnemy.alive() && tempEnemy.deathFrames<=0){
        			enemies.remove(i);
        			
        			//S=ystem.out.println("removed enemy:"+i);
        			
        			enemSize--;
        			i--;
        		}
        	}
    		
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
					knightSpawnRateCountdown = 1000;
					KNIGHTSPAWNLIMIT = 1000;
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
									enemies.add (new EnemyKnight(tempInt*blockSize, -20, knightImage, 1,mySoundSystem));
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
										enemies.add(new EnemyAxeKnight(tempInt*blockSize, -20, axeKnightImage, -1, mySoundSystem));
									} else {
										enemies.add(new EnemyKnight(tempInt*blockSize, -20, knightImage, -1, mySoundSystem));
									}
								} else {
									tempInt = 1;
									while (world[30][tempInt] != 1) {
										tempInt++;
									}
									if (axeBool) {
										enemies.add(new EnemyAxeKnight(tempInt*blockSize, -20, axeKnightImage, 1, mySoundSystem));
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
										enemies.add(new EnemyAxeKnight(tempInt*blockSize, -20, axeKnightImage, 1, mySoundSystem));
									} else {
										enemies.add(new EnemyKnight(tempInt*blockSize, -20, knightImage, 1, mySoundSystem));
									}
								} else {
									tempInt = 38;
									while (world[30][tempInt] != 1) {
										tempInt--;
									}
									if (axeBool) {
										enemies.add(new EnemyAxeKnight(tempInt*blockSize, -20, axeKnightImage, -1, mySoundSystem));
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
							powerups.add(new PowerObject(tempInt*blockSize, -20, (int)(Math.random()*powerupNum), 20, mySoundSystem));
						} else { //if no block exists
							//System.out.println("off block");
							if (tempInt > screenWidth/blockSize/2) {
								while (world[30][tempInt] != 1) {
									tempInt--;
								}
								if (tempInt > 1) {
									powerups.add(new PowerObject(tempInt*blockSize, -20, (int)(Math.random()*powerupNum), 20,mySoundSystem));
								} else {
									tempInt = 1;
									while (world[30][tempInt] != 1) {
										tempInt++;
									}
									powerups.add(new PowerObject((tempInt-1)*blockSize, -20, (int)(Math.random()*powerupNum), 20,mySoundSystem));
								}
							} else {
								while (world[30][tempInt] != 1) {
									tempInt++;
								}
								if (tempInt < 38) {
									powerups.add(new PowerObject((tempInt-1)*blockSize, -20, (int)(Math.random()*powerupNum), 20,mySoundSystem));
								} else {
									tempInt = 38;
									while (world[30][tempInt] != 1) {
										tempInt--;
									}
									powerups.add(new PowerObject(tempInt*blockSize, -20, (int)(Math.random()*powerupNum), 20,mySoundSystem));
								}
							}
						}
						//System.out.println(tempInt + " " + world[30][40]);
					}
				} //end powerup spawn code
    		} //end "if spawning stuff" code
        	
    	}//end if start
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return stateID;
	}

	//Returns an int from 00 to 13
	//The tens digit is the x index of the sprite to use in the spritesheet
	//And the ones digit is the y index
	//  wallL wallR wallN wallB
	//  platL platR platN platB
	private int getBlockSpriteIdcs(int[] newRow, int pos){
		boolean isWall = false;
		boolean blockedLeft = false;
		boolean blockedRight = false;
		if(pos == 0){
			isWall = true;
			blockedLeft = true;
			if(newRow[pos+1] != 0){
				blockedRight = true;
			}
		}else if(pos == world[0].length-2){//since length-1 is the count column
			isWall = true;
			blockedRight = true;
			if(newRow[pos-1] != 0){
				blockedLeft = true;
			}
		}else{
			if(newRow[pos-1] != 0){
				blockedLeft = true;
			}
			if(newRow[pos+1] != 0){
				blockedRight = true;
			}
		}
		
		int ans = 0;
		if(!isWall)
			ans += 10;
		
		if(blockedLeft && !blockedRight){
			//NO OP
		}else if(!blockedLeft && blockedRight){
			ans += 1;
		}else if(!blockedLeft && !blockedRight){
			ans += 2;
		}else{
			ans += 3;
		}
		
		return ans;
	}
	
	
    private void writeToWorld(int[] newRow, int startRow){//Write rows into world array starting at start and going up
    	int temp = 0;
        for(int i=0;i<newRow.length;i++){
            world[startRow][i]=newRow[i];
            if(newRow[i] == 1){
            	temp = getBlockSpriteIdcs(newRow, i);
				statics.add(new StaticObject(i*blockSize/*colPos*/, startRow*blockSize, blockSize, temp%10, temp>=10));
            }
        }        
    }
    private void writeToWorld(int[][] newRows, int start){//Write rows into world array starting at start and going up
    	int temp = 0;
        for(int i=start;i>start-newRows.length;i--)
            for(int j=0;j<newRows[0].length;j++){
                world[i][j]=newRows[start-i][j];
                if(newRows[start-i][j] == 1){
            		temp = getBlockSpriteIdcs(newRows[start-i], j);
					statics.add(new StaticObject(j*blockSize/*colPos*/, i*blockSize, blockSize, temp%10, temp>=10));

                }
        }
    }
    
    private boolean rowCheck(int[] row, int rowDex){//checks if row is accesible by the previous row
    	boolean good = false;
    	//get most recent row;
    	int dex = rowDex;
    	//System.out.println(dex);
    	while(true){
    		if(world[dex][40]>2){
    		 	break;
    		}else{
    		   	dex++;
    		}
    	}
    	//going from world[dex] to row
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
    	
    	if(blocks==impossibleblocks){
    		good=false;
    	}else{
    		good=true;
    	}
    	
    	return good;
    }
    
    private void genRowSet(){
    	while(genDex >= screenHeight/blockSize){
    		double pick = Math.random();//picks random random or setpieces
    		if(pick<=.75){//random random			
    			int spaces = (int)(Math.random()*2)+5;
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

    			int setPieceChoice = (int)(Math.random()*(setpieces.size()-1))+1;    			
    			int[][] tempSetPiece = setpieces.get(setPieceChoice);
    			while(!rowCheck(tempSetPiece[0],genDex)){
        			setPieceChoice = (int)(Math.random()*(setpieces.size()-1))+1;    			
        			tempSetPiece = setpieces.get(setPieceChoice);
    			}
    			
    			writeToWorld(tempSetPiece, genDex);
    			genDex -= tempSetPiece.length;
    			//WORKING ON THIS
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

    	for(int i=1;i<tempRow.length-2;i++){
    		if(tempRow[i-1]==tempRow[i+1] && tempRow[i]!=tempRow[i-1])
    			tempRow[i]=tempRow[i-1];
    	}


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
    
    public void setScoresName(String str){
    	scoresName = str;
    }
    
    public void setKnight(boolean b){
    	knight = b;
    }
    
    public void setKnightConfig(int n){
    	knightConfig = n;
    }
    
	public void setSoundSystem(SoundSystem ss){
		mySoundSystem=ss;
	}
	
	public void setPlayApplause(boolean b){
		playApplause = b;
	}
    
}
