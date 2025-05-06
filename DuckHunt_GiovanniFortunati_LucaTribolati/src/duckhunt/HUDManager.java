package duckhunt;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;


import javax.imageio.ImageIO;

public class HUDManager {

	private int bullet;
	private Image blackTileImage;
	private Image redDuck;
	private Image greenTile;
	private Image roundOneDigit;
	private Image roundTwoDigit;
	private Image gameOver;
	private Image pressESC;
	private Image blackTileLongImage;
	private Image perfect;
	private Image[] greenNumbers, whiteNumbers;
	private Image blackScore, blueScore, redScore;
	
	// SCORE
	private int[] points_to_digits;
	
	private boolean[] duckArray;
	private int flashDuckTimer;
	private int flashAllDucksTimer;
	private int duckAnimationTimer;
	private int flashShotTimer;
	private int gameOverTextTimer;
	
	private GamePanel gamePanel;
	
	private boolean playMusic = true;
	private boolean isFlashing = false;

	public HUDManager(GamePanel gamePanel) {
		this.gamePanel = gamePanel;
		
		bullet = 3;
		flashDuckTimer = 0;
		flashAllDucksTimer = 0;
		duckAnimationTimer = 0;
		flashShotTimer = 0;
		
		loadImages();
		
		gameOverTextTimer = 0;

		duckArray = new boolean[10];
		for(int i=0; i<duckArray.length; i++) {
			duckArray[i] = false;
		}
		
		points_to_digits = new int[6];
	}
	
	private void loadImages() {
		try {
			blackTileImage = ImageIO.read(getClass().getResource("/images/blackTile.png"));
			redDuck = ImageIO.read(getClass().getResource("/images/duck/redDuck.png"));
			greenTile = ImageIO.read(getClass().getResource("/images/greenTile.png"));
			greenNumbers = new Image[10];
			whiteNumbers = new Image[10];
			roundOneDigit = ImageIO.read(getClass().getResource("/images/round_oneDigit.png"));
			roundTwoDigit = ImageIO.read(getClass().getResource("/images/round_twoDigit.png"));
			gameOver = ImageIO.read(getClass().getResource("/images/gameOver.png"));
			pressESC = ImageIO.read(getClass().getResource("/images/pressESC.png"));			
			blackScore = ImageIO.read(getClass().getResource("/images/duck/500.png"));
			blueScore = ImageIO.read(getClass().getResource("/images/duck/1000.png"));
			redScore = ImageIO.read(getClass().getResource("/images/duck/1500.png"));
			perfect = ImageIO.read(getClass().getResource("/images/perfect.png"));
			blackTileLongImage = ImageIO.read(getClass().getResource("/images/blackTileLong.png"));
			
			for(int i=0; i<greenNumbers.length; i++) {
				greenNumbers[i] = ImageIO.read(getClass().getResource("/images/greenNumbers/number_" + i + ".png"));
			}
			
			for(int i=0; i<whiteNumbers.length; i++) {
				whiteNumbers[i] = ImageIO.read(getClass().getResource("/images/whiteNumbers/number_" + i + ".png"));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void update(Duck duck) {
	    
		copy_DuckArray(duck);

	    updatePointsToDigits(duck);

	    if(duck.getState() == Duck.DuckState.DUCK_COUNTING) {
	        
	        if(duckAnimationTimer <= 30) {
	            duckAnimationTimer++;
	            return; // Salta il resto del codice fino al prossimo update
	        }

	        if(!ducksAreOrdered(duck.getHittenDucks())) {
	            reorganizeDuckArray();
	            gamePanel.playMusic(4);
	            duckAnimationTimer = 0;
	            return;
	        }

	        if(duck.minimumNumberOfHittenDucksToPassRoundHaveBeenHit()) {
	            handleVictorySequence(duck);
	        } 
	        
	        else {
	            handleGameOverSequence(duck);
	        }
	    }
	}
	
	private void copy_DuckArray(Duck duck) {
		if(duck.getState() != Duck.DuckState.DUCK_COUNTING && duck.getState() != Duck.DuckState.GAMEOVER) {
	        updateDuckArray(duck);
	    }
	}

	private void handleVictorySequence(Duck duck) {
	    if (duckAnimationTimer <= 240) { 
	        if (playMusic) {
	            isFlashing = true;
	            gamePanel.playMusic(19);
	            playMusic = false;
	        }
	        duckAnimationTimer++;
	        return;
	    }

	    resetMusicAndEffects();
	    duck.setState(Duck.DuckState.NEXT_ROUND);
	}

	private void handleGameOverSequence(Duck duck) {
	    if (duckAnimationTimer <= 120) { 
	        if (playMusic) {
	            gamePanel.playMusic(12);
	            playMusic = false;
	        }
	        duckAnimationTimer++;
	        return;
	    }

	    resetMusicAndEffects();
	    duck.setState(Duck.DuckState.GAMEOVER);
	}

	private void resetMusicAndEffects() {
	    playMusic = true;
	    isFlashing = false;
	    duckAnimationTimer = 0;
	}

	public void draw(Graphics2D g2d, Dog dog, Duck duck, GameManager gameManager) {	
		
		drawRemainingBullets(g2d, dog);

		drawRedDucks(g2d);
		
		flashCurrentDuck(g2d, duck);
		
		flashAllDucks(g2d);
		
		showPerfetctLabel(g2d, duck);
		
		drawBigRoundLabel(g2d, dog, duck);
		
		drawSmallRoundLabel(g2d, duck);
		
		drawPoints(g2d);
		
		drawGameOverLabel(gameManager, g2d);
		
		drawPointsAboveDuckWhenHitten(g2d, duck);
		
	}
	
	private boolean ducksAreOrdered(int hittenDucks) {
		
		int count = 0;
		for(int i=0; i<hittenDucks; i++) {
			if(duckArray[i] == true) {
				count++;
			}
		}
		
		if(count == hittenDucks) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// Red ducks end round animation
	private void reorganizeDuckArray() {
		
		for(int i=0; i<duckArray.length; i++) {
			if(i >0 && duckArray[i] == true && duckArray[i-1] == false) {
				duckArray[i-1] = duckArray[i];
				duckArray[i] = false;
				
			}
		}
	}
	
	public boolean bulletShot() {
		if(bullet > 0) {
			bullet--;
			return true;
		}
		else {
			return false;
		}
	}
	
	private void updateDuckArray(Duck duck) {
		for(int i=0; i<duckArray.length; i++) {
			duckArray[i] = duck.getDuckStatus(i);
		}
	}
	
	private void updatePointsToDigits(Duck duck) {
		for(int i=0; i<points_to_digits.length; i++) {
			points_to_digits[i] = duck.getPointDigit(i);
		}
	}
	
	public void resetBullet() {
		bullet = 3;
	}
	
	public int getRemainingBullets() {
		return bullet;
	}
	
	private void showPerfetctLabel(Graphics2D g2d, Duck duck) {
		if(duck.getHittenDucks() == 10) {
			g2d.drawImage(perfect, 92, 30, null);
		}
	}
	
	private void flashCurrentDuck(Graphics2D g2d, Duck duck) {
		if(flashDuckTimer > 20 && flashDuckTimer < 40 && duck.getState() == Duck.DuckState.FLYING) {
			g2d.drawImage(blackTileImage, 96 + (8 * duck.getDuckIndex()), 196, null);
		}
		else if(flashDuckTimer >= 40) {
			flashDuckTimer = 0;
		}
		flashDuckTimer++;
	}
	
	private void flashAllDucks(Graphics2D g2d) {
		if(flashAllDucksTimer > 20 && flashAllDucksTimer < 40 && isFlashing) {
			g2d.drawImage(blackTileLongImage, 96, 196, null);
		}
		else if(flashAllDucksTimer >= 40) {
			flashAllDucksTimer = 0;
		}
		flashAllDucksTimer++;
	}
	
	private void drawRedDucks(Graphics2D g2d) {
		for(int i=0; i<duckArray.length; i++) {
			if(duckArray[i]) {
				g2d.drawImage(redDuck, 96 + (8 * i), 197, null);
			}
		}
	}
	
	private void drawRemainingBullets(Graphics2D g2d, Dog dog) {
		if(bullet == 2) {
			g2d.drawImage(blackTileImage, 40, 196, null);
		}
		else if(bullet == 1) {
			g2d.drawImage(blackTileImage, 40, 196, null);
			g2d.drawImage(blackTileImage, 32, 196, null);
		}
		else if(bullet == 0){
			g2d.drawImage(blackTileImage, 40, 196, null);
			g2d.drawImage(blackTileImage, 32, 196, null);
			g2d.drawImage(blackTileImage, 24, 196, null);
			
			makeShotLabelFlash(g2d, dog);
		}
	}
	
	// Flashing shot hud when bullet are finished
	private void makeShotLabelFlash(Graphics2D g2d, Dog dog) {
		if(flashShotTimer > 10 && flashShotTimer < 20 && dog.getState() != Dog.DogState.COMEUP) {
			g2d.drawImage(blackTileImage, 40, 196+9, null);
			g2d.drawImage(blackTileImage, 32, 196+9, null);
			g2d.drawImage(blackTileImage, 24, 196+9, null);			
		}
		else if(flashShotTimer >= 20) {
			flashShotTimer = 0;
		}
		flashShotTimer++;
	}
	
	private void drawBigRoundLabel(Graphics2D g2d, Dog dog, Duck duck) {
		if(dog.getState() == Dog.DogState.SHORT_INTRO) {
			if(duck.getRoundNumber() < 10) {
				g2d.drawImage(roundOneDigit, 104, 48, null);
				g2d.drawImage(whiteNumbers[duck.getRoundNumber()], 125, 66, null);
			}
			else {
				g2d.drawImage(roundTwoDigit, 104, 48, null);
				g2d.drawImage(whiteNumbers[duck.getRoundNumber() / 10], 120, 66, null);
				g2d.drawImage(whiteNumbers[duck.getRoundNumber() % 10], 129, 66, null);
			}
		}
	}
	
	private void drawSmallRoundLabel(Graphics2D g2d, Duck duck) {
		for(int i=0; i<2; i++) {
			if(duck.getRoundNumber() < 10) {
				g2d.drawImage(greenNumbers[duck.getRoundNumber()], 40, 180, null);
				g2d.drawImage(greenTile, 48, 180, null);
			}
			else {
				g2d.drawImage(greenNumbers[duck.getRoundNumber() / 10], 40, 180, null);
				g2d.drawImage(greenNumbers[duck.getRoundNumber() % 10], 48, 180, null);
			}
		}
	}
	
	private void drawPoints(Graphics2D g2d) {
		for(int i=0; i<points_to_digits.length; i++) {
			g2d.drawImage(whiteNumbers[points_to_digits[0]], 192, 196, null);
			g2d.drawImage(whiteNumbers[points_to_digits[1]], 192 + 8, 196, null);
			g2d.drawImage(whiteNumbers[points_to_digits[2]], 192 + 8*2, 196, null);
			g2d.drawImage(whiteNumbers[points_to_digits[3]], 192 + 8*3, 196, null);
			g2d.drawImage(whiteNumbers[points_to_digits[4]], 192 + 8*4, 196, null);
			g2d.drawImage(whiteNumbers[points_to_digits[5]], 192 + 8*5, 196, null);
		}
	}
	
	private void drawGameOverLabel(GameManager gameManager, Graphics2D g2d) {
		if (gameManager.getGamePhase() == GameManager.GamePhase.GAMEOVER) {
			g2d.drawImage(gameOver, 92, 30, null);
				
			if(gameOverTextTimer > 30 && gameOverTextTimer < 60) {
				g2d.drawImage(pressESC, 74, 60, null);
			}
			else if(gameOverTextTimer >= 60) {
						gameOverTextTimer = 0;
			}
			gameOverTextTimer++;

		}
	}
	
	private void drawPointsAboveDuckWhenHitten(Graphics2D g2d, Duck duck) {
		if(duck.getState() == Duck.DuckState.FALLING) {
			if(duck.getType() == Duck.DuckType.BLACK) {
				g2d.drawImage(blackScore, duck.getScoreX(), duck.getScoreY(), null);
			}
			else if(duck.getType() == Duck.DuckType.BLUE) {
				g2d.drawImage(blueScore, duck.getScoreX(), duck.getScoreY(), null);
			}
			else if(duck.getType() == Duck.DuckType.RED) {
				g2d.drawImage(redScore, duck.getScoreX(), duck.getScoreY(), null);
			}
		}
	}
	

}
