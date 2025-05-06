package duckhunt;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;


public class Dog {

	private Image dogWalk[];
	private Image dogNuzzle[];
	private Image dogLaugh[];
	private Image dogClue, dogJumpUp, dogJumpDown, dogSuccess;
	
	private final int START_X = -180;
	private final int START_Y = 130;
	private final int STOP_X = 70;
	private final int END_NUZZLE = 4;
	private final int CLUE_TIME = 64;
	private final int LOWER_Y = 140;
	
	private int frameCounter;
	private int frameDelay;
	private int timer;
	private int dogX;
	private int dogY;
	private int animationIndex;
	
	private DogState state;
	private DogAnimation animation;
	
	private final double vX = 1; // Horizontal jump speed
	private double vY = 4; // Vertical jump speed
	private final double gravity = 0.2; // "Gravitational" acceleration

	private boolean salito = false;
	
	public enum DogState {
		INITIAL_POSITION,
		INTRO,
		SHORT_INTRO,
		COMEUP,
		WAIT,
		GAMEOVER
	}
	
	public enum DogAnimation {
		WALK,
		NUZZLE,
		CLUE,
		JUMPUP,
		JUMPDOWN,
		SUCCESS,
		LAUGH
	}
	
	public Dog() {
		reset();
	    loadSprites();
	}

	//Update
	public void update(Duck duck, HUDManager hud) {
		
		// Dog starts intro scene
		if(state == DogState.INTRO) {
			
			frameCounter++;
			
			walk_firstHalf();
		}
		else if(state == DogState.SHORT_INTRO) {
			
			frameCounter++;
			
			walk_secondHalf();
			
			clueAnimation();
			
			jumpAnimation(duck);
			
		}
		else if(state == DogState.COMEUP) {
			
			frameCounter++;
			
			chooseDogX(duck);
			
			comeUp();
			
			comeDown(duck, hud);
			
			gameOver(duck);
		}
	}
	
	//Repaint
	public void draw(Graphics2D g2d) {
		
		if(state == DogState.INTRO || state == DogState.SHORT_INTRO) {
			
			if(animation == DogAnimation.WALK) {
				g2d.drawImage(dogWalk[animationIndex], dogX, dogY, null);	
	        }
			else if(animation == DogAnimation.NUZZLE) {
				g2d.drawImage(dogNuzzle[animationIndex], dogX, dogY, null);
			}
			else if(animation == DogAnimation.CLUE) {
				g2d.drawImage(dogClue, dogX, dogY, null);
			}
			else if(animation == DogAnimation.JUMPUP) {
				g2d.drawImage(dogJumpUp, dogX, dogY, null);
			}
			else if(animation == DogAnimation.JUMPDOWN) {
				g2d.drawImage(dogJumpDown, dogX, dogY, null);
				
			}
			
		}

		else if(state == DogState.COMEUP) {
			
			if (animation == DogAnimation.SUCCESS) {
				g2d.drawImage(dogSuccess, dogX, dogY, null);
			}
			if (animation == DogAnimation.LAUGH) {
				g2d.drawImage(dogLaugh[animationIndex], dogX, dogY, null);
			}
		}
			
	}
	
	private void loadSprites() {
		try {
			
			dogWalk = new Image[4];
			dogNuzzle = new Image[2];
			
			dogLaugh = new Image [2];
			
			for(int i=0; i<dogWalk.length; i++) {
				dogWalk[i] = ImageIO.read(getClass().getResource("/images/dog/walkingDog_" + (i+1) + ".png"));
			}
			for(int i=0; i<dogNuzzle.length; i++) {				
				dogNuzzle[i] = ImageIO.read(getClass().getResource("/images/dog/dogNuzzle_" + (i+1) + ".png"));
			}
			
			for(int i=0; i<dogLaugh.length; i++) {				
				dogLaugh[i] = ImageIO.read(getClass().getResource("/images/dog/dogLaugh_" + (i+1) + ".png"));
			}
			
			dogClue = ImageIO.read(getClass().getResource("/images/dog/dogClue.png"));
			dogJumpUp = ImageIO.read(getClass().getResource("/images/dog/dogJumpUp.png"));
			dogJumpDown = ImageIO.read(getClass().getResource("/images/dog/dogJumpDown.png"));
			dogSuccess = ImageIO.read(getClass().getResource("/images/dog/dogSuccess.png"));
			
    	} catch (IOException e) {
    		e.printStackTrace();
        }
	}
	
	public void reset() {
		frameCounter = 0;
		frameDelay = 8;
		
		dogX = START_X;
		dogY = START_Y;
		timer = 0;
		
		state = DogState.INITIAL_POSITION;
		animation = DogAnimation.WALK;
		
		animationIndex = 0;
	}
	
	public boolean isJumpDown() {
		if(animation == DogAnimation.JUMPDOWN) {
			return true;
		}
		else 
			return false;
	}
	
	public DogAnimation getAnimation() {
		return animation;
	}
	
	public DogState getState() {
		return state;
	}
	
	public void setAnimation(DogAnimation anim) {
		animation = anim;
	}
	
	public void setState(DogState state) {
		this.state = state;
	}
	
	private void walk_firstHalf() {
		
		// First walk
		if (animation == DogAnimation.WALK && dogX < STOP_X/2) {	
			dogX += 1;
			// Alternates walk sprites
			if (frameCounter >= frameDelay) {
				animationIndex = (animationIndex + 1) % dogWalk.length;
				frameCounter = 0;
			}
			// When in STOPX/2 sets state to NUZZLE
			if (dogX == STOP_X/2) {
				animation = DogAnimation.NUZZLE;
			}
		}
			//First nuzzle
			if(timer < END_NUZZLE) {
				if (animation == DogAnimation.NUZZLE && dogX == STOP_X/2) {
					// Reset animationIndex
					if(animationIndex > 1) {
						animationIndex = 0;
					}
					// Alternates nuzzle sprites
					if (frameCounter >= frameDelay) {
						animationIndex = (animationIndex + 1) % dogNuzzle.length;
						frameCounter = 0;
						timer++;
					}
				}
			}
			// Resets animation and nuzzle counter
			if(animation == DogAnimation.NUZZLE && timer >= END_NUZZLE) {
				state = DogState.SHORT_INTRO;
				animation = DogAnimation.WALK;
				timer = 0;
			}
	}
	
	private void walk_secondHalf() {
		
		if (animation == DogAnimation.WALK && dogX >= STOP_X/2 && dogX < STOP_X) {	
			
			dogX += 1;
			// Alternates walk sprites
			if (frameCounter >= frameDelay) {
				animationIndex = (animationIndex + 1) % dogWalk.length;
				frameCounter = 0;
			}
			// When in STOPX sets state to NUZZLE
			if (dogX == STOP_X) {
				animation = DogAnimation.NUZZLE;
			}
		}
		// Second nuzzle
		if(timer < END_NUZZLE) {
			if (animation == DogAnimation.NUZZLE && dogX == STOP_X) {
				// Reset animationIndex
				if(animationIndex > 1) {
					animationIndex = 0;
				}
				// Alternates nuzzle sprites
				if (frameCounter >= frameDelay) {
					animationIndex = (animationIndex + 1) % dogNuzzle.length;
					frameCounter = 0;
					timer++;
				}
			}
		}
	}
	
	private void clueAnimation() {
		// Sets state to CLUE, waits, sets state to JUMPUP
		if((timer >= END_NUZZLE && timer <= CLUE_TIME) && dogX == STOP_X) {
			animation = DogAnimation.CLUE;
						
			timer++;
		}
		if(timer > CLUE_TIME) {
			animation = DogAnimation.JUMPUP;
			timer = 0;
		}
	}
	
	private void jumpAnimation(Duck duck) {

		if (animation == DogAnimation.JUMPUP) {
		    // Up movement
		    dogY -= vY;
		    dogX += vX; // Dog moves to the right
		    vY -= gravity; // Vertical speed reduces

		    // Checks when reached top of jump
		    if (vY <= 0) { 
		        // Starts falling phase
		        animation = DogAnimation.JUMPDOWN;
		        vY = 0; // Resets speed to start falling   
		    }   
		}
		if (animation == DogAnimation.JUMPDOWN) {
		    // Falling movement
		    dogY += vY;
		    dogX += vX;
		    vY += gravity; // "Gravity" accelertes falling
		    
		    // End of animation
		    if (dogY == LOWER_Y) {
		    	state = DogState.WAIT;
		    	frameCounter = 0; // Initialize frameCounter for COMEUP state
		    	timer = 0;
		    	
		    	duck.resetDuckTime();
		    }
		}
	}
	
	private void chooseDogX(Duck duck) {
		if(animation == DogAnimation.SUCCESS) {
			
			if(duck.getLastX() >= 60 && duck.getLastX() <= 160) {
				dogX = (int)duck.getLastX();
			}
			else if(duck.getLastX() < 60) {
				dogX = 60;
			}
			else if(duck.getLastX() > 160){
				dogX = 150;
			}
		}
		else if (animation == DogAnimation.LAUGH) {
			
			dogX = 112;
		}
	}
	
	private void comeUp() {
		if(dogY > LOWER_Y - 30 && !salito) { // comes up
			dogY--;
			
			if (frameCounter >= frameDelay) {
				animationIndex = (animationIndex + 1) % dogLaugh.length;
				frameCounter = 0;
			}

		}
	}
	
	private void comeDown(Duck duck, HUDManager hud) {
		if(duck.getState() != Duck.DuckState.GAMEOVER) {
			
			if(dogY == LOWER_Y - 30 && timer < 60) { // "normal" animation
				timer++;
				salito = true;
				
				if (frameCounter >= frameDelay) {
					animationIndex = (animationIndex + 1) % dogLaugh.length;
					frameCounter = 0;
				}
			}
			
			if(dogY >= LOWER_Y - 30 && dogY < LOWER_Y && salito && timer == 60) { // goes down
				dogY++;
				
				if (frameCounter >= frameDelay) {
					animationIndex = (animationIndex + 1) % dogLaugh.length;
					frameCounter = 0;
				}
			}
			if(dogY == LOWER_Y){
				
				duck.duckCounting();
				
				timer = 0;
				salito = false;
				state = DogState.WAIT;
				
				hud.resetBullet();
				
				frameCounter = 0;
				
				dogX = STOP_X/2; // reset for short intro
				dogY = START_Y;
				vY = 4;
				
				duck.resetDuckTime();
			
			}	
		}
	}
	
	private void gameOver(Duck duck) {
		
		if(dogY == LOWER_Y - 30 && duck.getState() == Duck.DuckState.GAMEOVER) {
			
			if (frameCounter >= frameDelay) {
				animationIndex = (animationIndex + 1) % dogLaugh.length;
				frameCounter = 0;
			}
		}
	}
	
}
