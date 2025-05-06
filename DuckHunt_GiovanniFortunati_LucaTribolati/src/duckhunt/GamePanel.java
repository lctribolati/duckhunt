package duckhunt;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import java.awt.Toolkit;

@SuppressWarnings("serial")
public class GamePanel extends JPanel implements Runnable{
	
	// SCREEN SETTINGS
	private final Dimension ORIGINAL_FRAME_SIZE = new Dimension(256, 224);
    private final int SCALE_FACTOR = 3;
    private final int SCALED_WIDTH = (int)ORIGINAL_FRAME_SIZE.getWidth() * SCALE_FACTOR; //256*3 = 768px
    private final int SCALED_HEIGHT = (int)ORIGINAL_FRAME_SIZE.getHeight() * SCALE_FACTOR; //244*3 = 732px
    private final int FPS = 60;
    
    // GAME MANAGERS
    private KeyHandler keyHandler;
    private HitHandler hitHandler;
    private GameManager gameManager;
    private HUDManager hud;
    private Thread gameThread;
    
    // HIGH SCORE MANAGERs
    private ScoreManager scoreManager;
    private Image[] greenNumbers;
    private int[] scoreToDigits;
    
    private int gameOverTextTimer;
        
	private boolean isFlashing = false; // Flash flag 
	private long flashStartTime = 0;
	private final int FLASH_DURATION = 50;
	
	private boolean isPaused = false;
	
	private Sound sound;
	private long lastMusicPosition;
	private int musicCurrentIndex;
	private boolean isLooping;
    
    // GAME STATE
    public enum GameState{
    	MENU,
    	RULES,
    	PLAY,
    }
    private GameState gameState;

    // CURSOR POSITIONS
    private final int[][] positions = {
        {95, 131}, // First position
        {71, 147}, // Second position
        {95, 163}  // Third position
    };
    private int currentPosition;  // Initial cursor index (0-2)
    private int cursorX, cursorY;
    
    // IMAGES
    private Image menuImage, cursorImage, backgroundImage, pause, instructions, pressESC;
    private Color backgroundColor = new Color(0x64, 0xb0, 0xff);
    private Color shotColor = new Color(0xFF, 0xCC, 0xC5);
    
    // GAME CHARACTERS
    private Dog dog;
    private Duck duck;
    
    
    public GamePanel() {
    	gameManager = new GameManager();
    	dog = new Dog();
    	hud = new HUDManager(this);
    	duck = new Duck(hud);
    	
    	sound = new Sound();
    	lastMusicPosition = 0;
    	musicCurrentIndex = 0;
    	isLooping = false;
    	
		playMusic(22); //Plays start menu music
    	
    	//Score
    	greenNumbers = new Image[10];
    	scoreManager = new ScoreManager();
    	scoreToDigits = new int[6];
    	for(int i=0; i<scoreToDigits.length; i++) {
    		scoreToDigits[i] = 0;
    	}
    	
    	keyHandler = new KeyHandler(this, gameManager, dog, duck);
    	
    	currentPosition = 0;
    	cursorX = positions[currentPosition][0]; // Init cursor x coordinate
    	cursorY = positions[currentPosition][1]; // Init cursor y coordinate
    	
    	this.setPreferredSize(new Dimension(SCALED_WIDTH, SCALED_HEIGHT));
    	this.setDoubleBuffered(true);
    	this.setFocusable(true);
    	this.addKeyListener(keyHandler);
    	
    	loadMenuImages();
    	setTransparentCursor(); 
    	
    	gameState = GameState.MENU; // Game starts in MENU state
    	
    	hitHandler = new HitHandler(duck, dog, this, hud);

    	this.addMouseListener(hitHandler);
    	
    	gameOverTextTimer = 0;
    }
    
    @Override
    public void run() {
    	
    	// Delta method
    	double drawInterval = 1000000000/FPS;
    	double delta = 0;
    	long lastTime = System.nanoTime();
    	long currentTime;
    	
    	while(gameThread != null) {
    		
    		if (isPaused) { // PAUSE
    			synchronized (gameThread) {
    				try {
    					gameThread.wait();
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
    			}
    			
    			lastTime = System.nanoTime();
    	        delta = 0;
    		}
    		
    		currentTime = System.nanoTime();
    		delta += (currentTime - lastTime) / drawInterval;
    		lastTime = currentTime;
    		
    		if(delta >= 1) {
    			update();
    			repaint();
    			delta--;
    		}

    		if (delta < 1) {
    		    try {
    		        long sleepTime = (long) ((1 - delta) * drawInterval / 1_000_000); // Convert to milliseconds
    		        if (sleepTime > 0) {
    		            Thread.sleep(sleepTime);
    		        }
    		    } catch (InterruptedException e) {
    		        e.printStackTrace();
    		    }
    		}
    		

    	}
    	
    }
    
    public void update() {
    	
    	if(gameState == GameState.PLAY) {
    		
    		gameManager.update(dog, duck, hitHandler, hud, this);
    	}
    	else if(gameState == GameState.MENU) {
    		
    		scoreManager.updateHighScore(duck.getPoints());
			scoreManager.saveHighScore();
			updateScoreToDigits();
    	}
    }
	
	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		
		if(gameState == GameState.PLAY) {
			
			g2d.scale(SCALE_FACTOR, SCALE_FACTOR); // Scales everything to SCALE_FACTOR

			g2d.setColor(backgroundColor);
			
			if(duck.getState() == Duck.DuckState.ESCAPING) {
				g2d.setColor(shotColor);
			}
			g2d.fillRect(0, 0, (int)ORIGINAL_FRAME_SIZE.getWidth(), (int)ORIGINAL_FRAME_SIZE.getHeight());
			
			gameManager.draw(g2d, dog, duck, backgroundImage, hud); // Manages drawing on screen
			
			shotAnimation(g2d);
			
			if(isPaused) {
				g2d.drawImage(pause, 104, 48, this);
			}
			
		}
		
		else if(gameState == GameState.MENU) {
			
			g2d.scale(SCALE_FACTOR, SCALE_FACTOR);
			g2d.drawImage(menuImage, 0, 0, this);
			
			drawTopScore(g2d);
			
			g2d.drawImage(cursorImage, cursorX, cursorY, this);
		}
		
		else {
			
			g2d.scale(SCALE_FACTOR, SCALE_FACTOR);
			g2d.drawImage(instructions, 0, 0, null);
			
			drawFlashingBottomText(g2d);
		}
		
	}

	private void loadMenuImages() {
		try {
			menuImage = ImageIO.read(getClass().getResource("/images/menu.png")); 
			cursorImage = ImageIO.read(getClass().getResource("/images/cursor.png"));
			backgroundImage = ImageIO.read(getClass().getResource("/images/background.png"));
			pause = ImageIO.read(getClass().getResource("/images/pause.png"));
			instructions = ImageIO.read(getClass().getResource("/images/instructions.png"));
			pressESC = ImageIO.read(getClass().getResource("/images/pressESC.png"));
			
			for(int i=0; i<greenNumbers.length; i++) {
				greenNumbers[i] = ImageIO.read(getClass().getResource("/images/greenNumbers/number_" + i + ".png"));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void startGameThread() {
		gameThread = new Thread(this);
		gameThread.start();
	}
	
	public void cursorActionDown() {
		currentPosition = (currentPosition + 1) % positions.length;
        cursorX = positions[currentPosition][0];
        cursorY = positions[currentPosition][1];
	}
	
	public void cursorActionUp() {
	    currentPosition = (currentPosition - 1 + positions.length) % positions.length;
	    cursorX = positions[currentPosition][0];
	    cursorY = positions[currentPosition][1];
	}
	
	public void setGameState(GameState state) {
	    this.gameState = state;

	    if (state == GameState.PLAY) {
	        setCrosshairCursor();
	    } else {
	        setTransparentCursor();
	    }
	    repaint();
	}
	
	public GameState getGameState() {
		return gameState;
	}
	
	public int getCurrentPosition() {
		return currentPosition;
	}
	
	public int getScale() {
		return SCALE_FACTOR;
	}
	
	public void triggerFlash() {
	        isFlashing = true;
	        flashStartTime = System.currentTimeMillis();
	        repaint();
	}
	
    private void setCrosshairCursor() {
        try {
            Image cursorImage = ImageIO.read(getClass().getResource("/images/crosshair.png"));
            Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImage,
                new Point(cursorImage.getWidth(null) / 2, cursorImage.getHeight(null) / 2),
                "Custom Cursor"
            );
            setCursor(customCursor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void setTransparentCursor() {
        Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            Toolkit.getDefaultToolkit().createImage(new byte[0]), // Immagine vuota
            new Point(0, 0), "Transparent Cursor"
        );
        setCursor(transparentCursor);
    }
   
    public void changeStatus() {
    	isPaused = !isPaused;
    	repaint();
    }
    
    public boolean returnStatus() {
    	return isPaused;
    }
    
    public Thread returnThread() {
    	return gameThread;
    }
	
    public void updateScoreToDigits() {
    	for(int i=0; i<scoreToDigits.length; i++) {
			scoreToDigits[i] = scoreManager.getPointDigit(i);
		}
    }
    
    public void drawFlashingBottomText(Graphics2D g2d) {
    	if(gameOverTextTimer > 30 && gameOverTextTimer < 60) {
			g2d.drawImage(pressESC, 74, 200, null);
		}
		else if(gameOverTextTimer >= 60) {
			gameOverTextTimer = 0;
		}
		gameOverTextTimer++;
    }
    
    public void shotAnimation(Graphics2D g2d) {
    	if(isFlashing && (duck.getState() == Duck.DuckState.FLYING || 
				duck.getState() == Duck.DuckState.HIT || 
				duck.getState() == Duck.DuckState.ESCAPING)) {
	        
			long currentTime = System.currentTimeMillis();
	        if (currentTime - flashStartTime <= FLASH_DURATION) {
	            g2d.setColor(new Color(0, 0, 0)); 
	            g2d.fillRect(0, 0, getWidth(), getHeight());
	        } else {
	            isFlashing = false;
	        }
	    }
    }
    
    public void drawTopScore(Graphics2D g2d) {
    	g2d.drawImage(greenNumbers[scoreToDigits[0]], 152, 186, null);
		g2d.drawImage(greenNumbers[scoreToDigits[1]], 152 + 8, 186, null);
		g2d.drawImage(greenNumbers[scoreToDigits[2]], 152 + 8*2, 186, null);
		g2d.drawImage(greenNumbers[scoreToDigits[3]], 152 + 8*3, 186, null);
		g2d.drawImage(greenNumbers[scoreToDigits[4]], 152 + 8*4, 186, null);
		g2d.drawImage(greenNumbers[scoreToDigits[5]], 152 + 8*5, 186, null);
    }
    
    public void playMusic(int i) {
    	sound.setFile(i);
    	sound.play();
    	musicCurrentIndex = i;
    	isLooping = false;
    }
    
    public void playMusicWithoutUpdatingIndexOrBoolean(int i) { 
    	sound.setFile(i);
    	sound.play();
    }
    
    public void playLoopMusic(int i) {
    	sound.setFile(i);
    	sound.play();
    	sound.loop();
    	musicCurrentIndex = i;
    	isLooping = true;	
    }
    
    public void stopMusic() {
    	sound.stop();
    }
    
    public void pauseMusic() {
    	sound.stop();
    	lastMusicPosition = sound.pause();
    }
    
    public void resumeMusic(int i, long position) {
    	sound.setFile(i);
    	sound.resume(position);
    	sound.play();
    }
    
    public void resumeLoopMusic(int i, long position) {
    	sound.setFile(i);
    	sound.resume(position);
    	sound.play();
    	sound.loop();
    	musicCurrentIndex = i;
    	isLooping = true;
    }
    
    public int getMusicCurrentIndex() {
    	return musicCurrentIndex;
    }
    
    public long getlastMusicPosition() {
    	return lastMusicPosition;
    }
    
    public boolean getIsLooping() {
    	return isLooping;
    }
    
    public void endThread() {
    	gameThread = null;
    }
}
