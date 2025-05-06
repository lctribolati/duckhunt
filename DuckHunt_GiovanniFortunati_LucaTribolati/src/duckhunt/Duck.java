package duckhunt;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class Duck {
	
	private Image duckFlyRight[];
	private Image duckFlyLeft[];
	private Image duckFlyStraightRight[];
	private Image duckFlyStraightLeft[];
	private Image currentDuckImage;
	private Image duckHitImage;
	private Image duckFallingImage[];
	private Image duckFlyStraightUp[];
	private Image flyAway;
	
	private DuckState state;
	private DuckAnimation animation;
	private DuckType type;
	
	private int points;
	private int points_to_digits[]; //Translates score to single digits

	private int frameCounter;
	private final int frameDelay = 5;
	private int animationIndex;
	private double duckX;
	private double duckY;
	private double xSpeed;
	private double ySpeed;
	private double lastX = 0;
	
	private int duckIndex;
	private int hittenDucks;
	private int roundNumber;
	
	private boolean duckArray[];
	
	// Trajectories - Coordinate system: Y axis to the right, X axis to the bottom
	private Random random = new Random();
	private double speed = 2.3;
	
	// Ducks start position - X random, Y fixed under the grass
	private int minSpawnX = 30;
    private int maxSpawnX = 226;
    private final double START_Y = 140.0;
    private AffineTransform transform;
    
    private int timer;
    private final int HIT_ANIMATION_TIME = 30;
    private final double DUCK_FALLING_SPEED = 2;
    
    private HUDManager hud;
    
    private long startTime;
    private int scoreX, scoreY;
	
	public enum DuckState{
		NOT_STARTED_YET,
		FLYING,
		HIT,
		FALLING,
		ESCAPING,
		NEXT_ROUND,
		GAMEOVER,
		DUCK_COUNTING,
		WAITING
	}
	
	public enum DuckAnimation{
		FLYLEFT,
		FLYRIGHT,
		FLYSTRAIGHTLEFT,
		FLYSTRAIGHTRIGHT,
		FLYSTRAIGHTUP,
		HIT,
		FALLING
	}
	
	public enum DuckType{
		BLACK,
		BLUE,
		RED
	}
	

	public Duck(HUDManager hud) {
		
		this.hud = hud;
		transform = new AffineTransform();
		
		duckArray = new boolean[10];
		for(int i=0; i<duckArray.length; i++) {
			duckArray[i] = false;
		}
		
		points_to_digits = new int[6];
		for(int i=0; i<points_to_digits.length; i++) {
			points_to_digits[i] = 0;
		}
		
		duckIndex = -1;
		roundNumber = 1;
		
		reset();

	}
	
	public void update(Dog dog, GamePanel gamePanel) {
		
		if(duckIsFlying_and_stillHaveBullets_and_timeIsNotOut()) {
			frameCounter++;
			
			// SX border
			if(duckIsFlyingTowards_SX_Border()) {
				if(ySpeed < 0) { //SX UP
					flightManager(Math.PI / 2 + Math.random() * (Math.PI / 3));					
				}
				else { //SX DOWN
					flightManager((Math.random() * (Math.PI / 3)) + (Math.PI / 6));
				}
			}
			
			// DX border
			else if(duckIsFlyingTowards_DX_Border()) {
				if(ySpeed < 0) {
					flightManager(-(Math.PI / 2 + Math.random() * (Math.PI / 3)));				
				}
				else {
					flightManager(-((Math.random() * (Math.PI / 3)) + (Math.PI / 6)));
				}
			}
			
			// Lower border
			else if(duckIsFlyingTowards_LOW_Border()) {
				if(xSpeed < 0) {
					flightManager(-(Math.PI / 2 + Math.random() * (Math.PI / 3)));				
				}
				else {
					flightManager(Math.PI / 2 + Math.random() * (Math.PI / 3));				
					}
			}
			
			// Upper border
			else if(duckIsFlyingTowards_TOP_Border()) {
				if(xSpeed < 0) {
					flightManager(-((Math.random() * (Math.PI / 3)) + (Math.PI / 6)));
				}
				else {
					flightManager((Math.random() * (Math.PI / 3)) + (Math.PI / 6));					
				}
			}
			
			alternateSprites(duckFlyRight.length);
			
			// Updates position
	        duckX += xSpeed;
	        duckY += ySpeed;
		}
		
		if(noMoreBullets_OR_timeFinished()) {
			state = DuckState.ESCAPING;
			animation = DuckAnimation.FLYSTRAIGHTUP;
		}
		
		if(state == DuckState.ESCAPING) {
			
			if(duckY >= -40) {
				
				frameCounter++;
				
				alternateSprites(duckFlyStraightUp.length);

				duckY -= DUCK_FALLING_SPEED;
			}
			else {
				
				gamePanel.stopMusic(); //Stop flapping sound
				gamePanel.playMusic(1); //Start laughing dog sound
				
				dog.setState(Dog.DogState.COMEUP);
				dog.setAnimation(Dog.DogAnimation.LAUGH);
				lastX = duckX;
				reset();
			}
		}
		
		if(state == DuckState.HIT) {
			
			// Hit animation
			if(timer < HIT_ANIMATION_TIME) {
				animation = DuckAnimation.HIT;
				timer++;
			}
			// Duck set to falling
			else if(timer >= HIT_ANIMATION_TIME) {
				
				gamePanel.playMusic(6); // Plays falling sound

				state = DuckState.FALLING;
				animation = DuckAnimation.FALLING;
				animationIndex = 0;
				frameCounter = 0;
			}
		}
		
		// Duck starts falling
		if(state == DuckState.FALLING) {
			
			if(duckY <= START_Y) {
				
				frameCounter++;
				
				alternateSprites(duckFallingImage.length);
				
				duckY += DUCK_FALLING_SPEED;

			}
			else {
				
				gamePanel.stopMusic(); //Stops falling sound
				gamePanel.playMusic(9); //Plays hitting ground sound
				
				state = DuckState.WAITING; 
			}
		}
		
		if (state == DuckState.WAITING) {
		    timer++; 
		    
		    //Delays dog animation to fully reproduce hitting ground sound
		    if (timer >= 45) {
		        
		    	gamePanel.playMusic(3); //Plays dog success sound
		    	
		    	dog.setState(Dog.DogState.COMEUP);
		        dog.setAnimation(Dog.DogAnimation.SUCCESS);
		        lastX = duckX;
				reset();
		        
		    }
		}
		
	}
	
	public void draw(Graphics2D g2d) {
		
		if(state == DuckState.FLYING) {
			transform.translate(duckX, duckY);
			
			if (animation == DuckAnimation.FLYSTRAIGHTRIGHT && hud.getRemainingBullets() > 0) {
				g2d.drawImage(duckFlyStraightRight[animationIndex], transform, null);
				currentDuckImage = duckFlyStraightRight[animationIndex];
			}
			if (animation == DuckAnimation.FLYSTRAIGHTLEFT && hud.getRemainingBullets() > 0) {
				g2d.drawImage(duckFlyStraightLeft[animationIndex], transform, null);
				currentDuckImage = duckFlyStraightLeft[animationIndex];
			}
			if (animation == DuckAnimation.FLYRIGHT && hud.getRemainingBullets() > 0) {
				g2d.drawImage(duckFlyRight[animationIndex], transform, null);
				currentDuckImage = duckFlyRight[animationIndex];
			}
			if (animation == DuckAnimation.FLYLEFT && hud.getRemainingBullets() > 0) {
				g2d.drawImage(duckFlyLeft[animationIndex], transform, null);
				currentDuckImage = duckFlyLeft[animationIndex];
			}
			if (animation == DuckAnimation.FLYSTRAIGHTUP) {	
				g2d.drawImage(duckFlyStraightUp[animationIndex], transform, null);
				currentDuckImage = duckFlyStraightUp[animationIndex];
			}
			
			transform.setToIdentity(); //Resets translate
		}
		
		else if (state == DuckState.ESCAPING) {
			
			transform.translate(duckX, duckY);
			
			if (animation == DuckAnimation.FLYSTRAIGHTUP) {
				g2d.drawImage(duckFlyStraightUp[animationIndex], transform, null);
				currentDuckImage = duckFlyStraightUp[animationIndex];
				g2d.drawImage(flyAway, 92, 50, null);
			}
				
			transform.setToIdentity(); //Resets translate
		}
		
		else if(state == DuckState.HIT) {
			
			if(animation == DuckAnimation.HIT) {
				
				transform.translate(duckX, duckY);

				g2d.drawImage(duckHitImage, transform, null);
				transform.setToIdentity();
			}
		}
		
		else if(animation == DuckAnimation.FALLING) {

			transform.translate(duckX, duckY);
			g2d.drawImage(duckFallingImage[animationIndex], transform, null);
			

			transform.setToIdentity();
		}
	}
	
	
	private void flightManager(double phi) {
		// Speed components
		xSpeed = speed * Math.sin(phi);
		ySpeed = speed * Math.cos(phi);
		
		// Decides which animation to select
		if(duckIsGoing_Up(phi)) {
			animation = DuckAnimation.FLYSTRAIGHTUP;
		} 
		else if(duckIsGoing_Left_or_Right(phi)) {
			animation = (xSpeed > 0) ? DuckAnimation.FLYSTRAIGHTRIGHT : DuckAnimation.FLYSTRAIGHTLEFT;
		} 
		else {
			animation = (xSpeed > 0) ? DuckAnimation.FLYRIGHT : DuckAnimation.FLYLEFT;
		}
	}
	
	private void loadSprites(String color) {
		
		try {
			
			duckFlyRight = new Image[3];			
			for(int i=0; i<duckFlyRight.length; i++) {
				duckFlyRight[i] = ImageIO.read(getClass().getResource("/images/duck/" + color + "/duckFlyRight_" + (i+1) + ".png"));
			}

			duckFlyLeft = new Image[3];
			for(int i=0; i<duckFlyLeft.length; i++) {
				duckFlyLeft[i] = ImageIO.read(getClass().getResource("/images/duck/" + color + "/duckFlyLeft_" + (i+1) + ".png"));
			}

			duckFlyStraightRight = new Image[3];			
			for(int i=0; i<duckFlyStraightRight.length; i++) {
				duckFlyStraightRight[i] = 
						ImageIO.read(getClass().getResource("/images/duck/" + color + "/duckFlyHorizontalRight_" + (i+1) + ".png"));
			}
			
			duckFlyStraightLeft = new Image[3];
			for(int i=0; i<duckFlyStraightLeft.length; i++) {
				duckFlyStraightLeft[i] = 
						ImageIO.read(getClass().getResource("/images/duck/" + color + "/duckFlyHorizontalLeft_" + (i+1) + ".png"));
			}
			
			duckHitImage = ImageIO.read(getClass().getResource("/images/duck/" + color + "/DuckHit.png"));
			
			duckFallingImage = new Image[2];
			for(int i=0; i<duckFallingImage.length; i++) {
				duckFallingImage[i] = ImageIO.read(getClass().getResource("/images/duck/" + color + "/DuckFalling_" + (i+1) + ".png"));
			}
				
			duckFlyStraightUp = new Image[3];
			for(int i=0; i<duckFlyStraightUp.length; i++) {
				duckFlyStraightUp[i] = ImageIO.read(getClass().getResource("/images/duck/" + color + "/DuckEscaping_" + (i+1) + ".png"));	
			}
			
			flyAway = ImageIO.read(getClass().getResource("/images/flyAway.png"));
			
		}	
    	 catch (IOException e) {
    		e.printStackTrace();
        }
	}
	
	
	public void setHit(boolean hit) {
		
		duckArray[duckIndex] = hit;
		for(int i=0; i<duckArray.length; i++) {
		}
	}
	
	public void setPointsToDigits() {
    	int nextNumber = points;
    	for(int i=points_to_digits.length - 1; i >= 0; i--) {
    		points_to_digits[i] = nextNumber % 10;
    		nextNumber = nextNumber / 10;
    	}
    }
	
	public void setState (DuckState state) {
    	this.state = state;
    }
	
	public void setSpeed() {
    	speed = speed + 0.8;
    }
	
	public void resetSpeed() {
		speed = 2.3;
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public boolean getDuckStatus(int i) {
    	return duckArray[i];
    }
    
    public int getRoundNumber() {
    	return roundNumber;
    }
    
    public int getDuckIndex() {
    	return duckIndex;
    }
    
    public int getPointDigit(int i) {
    	return points_to_digits[i];
    }
    
    public int getPoints() {
    	return points;
    }
    
    public int getScoreX() {
    	return scoreX;
    }
    
    public int getScoreY() {
    	return scoreY;
    }
    
    public DuckType getType() {
    	return type;
    }
    
    public int getHittenDucks() {
    	return hittenDucks;
    }
    
    public double getX() {
    	return duckX;
    }
    
    public double getY() {
    	return duckY;
    }
    
    public Image getCurrentDuckImage() {
    	return currentDuckImage;
    }
    
    public DuckState getState() {
    	return state;
    }
    
    public double getLastX() {
    	return lastX;
    }
	
    
    public void assignDuckType() {
        int randomValue = random.nextInt(100);
        
        if (randomValue < 60) { // BLACK: 60%
            type = DuckType.BLACK;
        } else if (randomValue < 90) { // BLUE: 30%
        	type = DuckType.BLUE;
        } else {					// RED: 10%
        	type = DuckType.RED;
        }
    }
    
    public void loadRandomSprites() {
    	if(type == DuckType.BLACK) {
    		loadSprites("black");
    	}
    	if(type == DuckType.BLUE) {
    		loadSprites("blue");
    	}
    	if(type == DuckType.RED) {
    		loadSprites("red");
    	}
    }
    
    public void assignPoints() {
    	if(type == DuckType.BLACK) {
    		points += 500;
    	}
    	if(type == DuckType.BLUE) {
    		points += 1000;
    	}
    	if(type == DuckType.RED) {
    		points += 1500;
    	}
    }
    
    public void duckCounting() {

    	if(duckIndex > 9) {
    		
    		for(int i=0; i<duckArray.length; i++) {
    			if(duckArray[i] == true) {
    				hittenDucks++;
    			}
    		}
    		state = DuckState.DUCK_COUNTING;
    		
    		if (hittenDucks == 10) {
    			points += 10000;
    			setPointsToDigits();
    		}
    	}
    }
    
    public void incrementRoundNumber() {
    	roundNumber++;
    }
    
    private void alternateSprites(int lenght) {
    	if(frameCounter >= frameDelay) {
			animationIndex = (animationIndex + 1) % lenght; // Alternates walk sprites
			frameCounter = 0;
		}
    }
    
    public void updateScoreCoordinates(int x, int y) {
    	scoreX = x;
    	scoreY = y;
    }
    
    
    private boolean duckIsFlying_and_stillHaveBullets_and_timeIsNotOut() {
    	if(state == DuckState.FLYING && hud.getRemainingBullets() > 0 && 
    			(System.nanoTime() - startTime) <= 10_000_000_000L) {
    		return true;
    	}
    	else {
			return false;
		}
    }
    
    private boolean duckIsFlyingTowards_SX_Border() {
    	return duckX < 0 && xSpeed < 0;
    }
    
    private boolean duckIsFlyingTowards_DX_Border() {
    	return duckX > 256 - duckFlyRight[animationIndex].getWidth(null) && xSpeed > 0;
    }
    
    private boolean duckIsFlyingTowards_LOW_Border() {
    	return duckY > 120 && ySpeed > 0;
    }
    
    private boolean duckIsFlyingTowards_TOP_Border() {
    	return duckY < 0 && ySpeed < 0;
    }
    
    private boolean duckIsGoing_Up(double phi) {
    	return phi >= (11 * Math.PI / 12) && phi <= (13 * Math.PI / 12);
    }
    
    private boolean duckIsGoing_Left_or_Right(double phi) {
    	return (phi >= Math.PI / 3 && phi <= 2 * Math.PI / 3) || (phi <= -(Math.PI / 3) && phi >= -(2 * Math.PI / 3));
    }
    
    private boolean noMoreBullets_OR_timeFinished() {
    	return hud.getRemainingBullets() == 0 && state == DuckState.FLYING || 
				(System.nanoTime() - startTime) >= 10_000_000_000L && state == DuckState.FLYING;
    }
    
    
    private void reset() {
		
		assignDuckType();
		
		loadRandomSprites();
		
		flightManager((2 * Math.PI / 3) + (Math.random() * 2 * Math.PI / 3));
		
		frameCounter = 0;
		
		duckX = (Math.random() * (maxSpawnX - minSpawnX + 1)) + minSpawnX; // Random for each duck
		duckY = START_Y; // Fixed under the grass

		state = DuckState.NOT_STARTED_YET;
		
		animationIndex = 0;
		
		timer = 0;
		
		duckIndex ++;
		
	}
    
    public void resetDuckArray() {
    	
    	for(int i=0; i<duckArray.length; i++) {
    		duckArray[i] = false;
    	}
    	
    	duckIndex = 0;
    	hittenDucks = 0;
    }
    
    public void resetDuckTime() {
    	startTime = System.nanoTime();
    }
    
    //Used in keyhandler to manage the pause effects on the duck 10s timer
    public void setResumedTime(long time) {
    	this.startTime = startTime - time;
    }
    
    public void resetPoints() {
    	points = 0;
    	for(int i=0; i<points_to_digits.length; i++) {
    		points_to_digits[i] = 0;
    	}
    }
    
    public void resetRoundNumber() {
    	roundNumber = 1;
    }
    
    public boolean minimumNumberOfHittenDucksToPassRoundHaveBeenHit() {
    	
    	boolean roundPassed = false;
    	
    	if (roundNumber == 1) {
    		if (hittenDucks >=4) {
    			roundPassed = true;
    		}
    	}
    	if (roundNumber > 1 && roundNumber < 4 ) {
    		if (hittenDucks >= 5) {
    			roundPassed = true;
    		}
    	}
    	if (roundNumber >= 4) {
    		if (hittenDucks >= 6) {
    			roundPassed = true;
    		}
    	}
    	
    	return roundPassed;
    }
}
