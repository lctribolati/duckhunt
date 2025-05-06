package duckhunt;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener{
	
	private GamePanel gamePanel;
	private GameManager gameManager;
	private Dog dog;
	private Duck duck;
	
	private long pausedTime = 0;
	private long resumedTime = 0;
	
	public KeyHandler(GamePanel gamePanel, GameManager gameManager, Dog dog, Duck duck) {
		this.gamePanel = gamePanel;
		this.gameManager = gameManager;
		this.dog = dog;
		this.duck = duck;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            gamePanel.cursorActionDown();
		}
		
		if (e.getKeyCode() == KeyEvent.VK_UP) {
            gamePanel.cursorActionUp();
		}
		
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			
			if(cursorIn_PlayPosition()) {
				
				duck.resetPoints();
				gamePanel.setGameState(GamePanel.GameState.PLAY);
			}
			if(cursorIn_RulesPosition()) {

				gamePanel.stopMusic();
				gamePanel.playLoopMusic(18); //plays rules music
				gamePanel.setGameState(GamePanel.GameState.RULES);
			}
			if(cursorIn_ExitPosition()) {
				
				gamePanel.endThread();
				System.exit(0);
			}		
		}
		if(ESCpressed_in_RulesPanel(e)) {
			
			gamePanel.stopMusic();
			gamePanel.setGameState(GamePanel.GameState.MENU);
		}
		
		if(ESCpressed_in_GameOver(e)) {
			
		    gamePanel.stopMusic();  
		    gamePanel.playMusic(22);
			
			gamePanel.setGameState(GamePanel.GameState.MENU);
		    
		    duck.resetRoundNumber();
		    duck.resetDuckArray();
		        
		    dog.reset();
		        
		    duck.setState(Duck.DuckState.NOT_STARTED_YET);
		    gameManager.setGamePhase(GameManager.GamePhase.INTRO_ANIMATION);
			
		}
        
        if (SPACEpressed_whilePlaying(e)) {

        	if(!gamePanel.returnStatus()) {
        		gamePanel.pauseMusic(); //Pauses current music
        		gamePanel.playMusicWithoutUpdatingIndexOrBoolean(20); //Plays the pause sound effect
                gamePanel.changeStatus();
                pausedTime = System.nanoTime();
        	}
            
        	else if (gamePanel.returnStatus()) {
        		
        		gamePanel.changeStatus();
        		
        		resumedTime = System.nanoTime();
        		
        		 // Compensates on the duck timer for the time spent while in pause
        		duck.setResumedTime(pausedTime - resumedTime);
            	
        		if(gamePanel.getIsLooping()) {
            		gamePanel.resumeLoopMusic(gamePanel.getMusicCurrentIndex(), gamePanel.getlastMusicPosition());
            	}
            	else {
            		gamePanel.resumeMusic(gamePanel.getMusicCurrentIndex(), gamePanel.getlastMusicPosition());
            	}
                synchronized (gamePanel.returnThread()) {
                	gamePanel.returnThread().notify();  // Wakes up thread if paused
                }
            }
        }
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}
	
	@Override
	public void keyReleased(KeyEvent e) {

	}
	
	private boolean cursorIn_PlayPosition() {
		return gamePanel.getCurrentPosition() == 0 && gamePanel.getGameState() != GamePanel.GameState.PLAY;
	}
	
	private boolean cursorIn_RulesPosition() {
		return gamePanel.getCurrentPosition() == 1;
	}
	
	private boolean cursorIn_ExitPosition() {
		return gamePanel.getCurrentPosition() == 2;
	}
	
	private boolean ESCpressed_in_RulesPanel(KeyEvent e) {
		return e.getKeyCode() == KeyEvent.VK_ESCAPE && gamePanel.getGameState() == GamePanel.GameState.RULES;
	}
	
	private boolean ESCpressed_in_GameOver(KeyEvent e) {
		return e.getKeyCode() == KeyEvent.VK_ESCAPE && gameManager.getGamePhase() == GameManager.GamePhase.GAMEOVER;
	}
	
	private boolean SPACEpressed_whilePlaying(KeyEvent e) {
		return e.getKeyCode() == KeyEvent.VK_SPACE && gamePanel.getGameState() == GamePanel.GameState.PLAY;
	}

}
