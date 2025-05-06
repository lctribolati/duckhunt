package duckhunt;
import java.awt.Graphics2D;
import java.awt.Image;

public class GameManager{
	
	private GamePhase phase;
	
	public enum GamePhase{
		INTRO_ANIMATION,
		DUCK_SPAWN,
		DUCK_HIT,
		DUCK_COUNTING,
		GAMEOVER
	}
	
	public GameManager() {
		
		phase = GamePhase.INTRO_ANIMATION;
	}
	
	public void update(Dog dog, Duck duck, HitHandler hitHandler, HUDManager hud, GamePanel gameP) {
		
		if(dog.getState() == Dog.DogState.INITIAL_POSITION) {
			gameP.stopMusic();
			gameP.playMusic(16);
			dog.setState(Dog.DogState.INTRO);
		}
			
		if(dog.getState() == Dog.DogState.INTRO || dog.getState() == Dog.DogState.SHORT_INTRO) {
			dog.update(duck, hud);
		}

		if(dog.getState() == Dog.DogState.WAIT && duck.getState() == Duck.DuckState.NOT_STARTED_YET) {
			duck.setState(Duck.DuckState.FLYING);
			phase = GamePhase.DUCK_SPAWN;
			gameP.playLoopMusic(8);
		}

		if(dogIsWaiting_and_duckIsIn_Flying_orHit_orFalling_orEscaping(dog, duck)) {
			duck.update(dog, gameP);
		}
		
		if(duckHit_or_Escaped(duck, dog)) {
			dog.update(duck, hud);
			phase = GamePhase.DUCK_HIT;
		}
		
		if(dog.getState() == Dog.DogState.WAIT && duck.getState() == Duck.DuckState.DUCK_COUNTING) {
			phase = GamePhase.DUCK_COUNTING;
		}
		
		if(ifPlayerPassedRound(dog, duck)) {
			
			phase = GamePhase.INTRO_ANIMATION;
			
			//Plays only bark sound effect when starting another round after passing the previous one
			gameP.playMusic(0);
			
			dog.setState(Dog.DogState.SHORT_INTRO);
			dog.setAnimation(Dog.DogAnimation.WALK);
			
			duck.setState(Duck.DuckState.NOT_STARTED_YET);
			duck.incrementRoundNumber();
            duck.resetDuckArray();
            duck.setSpeed();
		}
		
		if(ifPlayerHasNOTPassedRound(dog, duck)) {
			
			dog.setState(Dog.DogState.COMEUP);
			dog.setAnimation(Dog.DogAnimation.LAUGH);
			duck.resetSpeed();
			
			gameP.playMusic(13); //PLays the secondo part of the gameover sound
		}
		
		if(dog.getState() == Dog.DogState.COMEUP && duck.getState() == Duck.DuckState.GAMEOVER) {
			dog.update(duck, hud);
			phase = GamePhase.GAMEOVER;
		}
		
		
		hud.update(duck);
	}
	
	public void draw(Graphics2D g2d, Dog dog, Duck duck, Image backgroundImage, HUDManager hud) {
					
		if(phase == GameManager.GamePhase.INTRO_ANIMATION && 
			dog.getAnimation() != Dog.DogAnimation.JUMPDOWN) {
			g2d.drawImage(backgroundImage, 0, 0, null);
			dog.draw(g2d);
		}
		
		if(phase == GameManager.GamePhase.INTRO_ANIMATION && 
			dog.getAnimation() == Dog.DogAnimation.JUMPDOWN) {
			dog.draw(g2d);
			g2d.drawImage(backgroundImage, 0, 0, null);
		}
		
		if(phase == GameManager.GamePhase.DUCK_SPAWN) {
			duck.draw(g2d);
			g2d.drawImage(backgroundImage, 0, 0, null);
		}
		
		if(phase == GameManager.GamePhase.DUCK_HIT) {
			dog.draw(g2d);
			g2d.drawImage(backgroundImage, 0, 0, null);
		}
		
		if(phase == GameManager.GamePhase.DUCK_COUNTING) {
			g2d.drawImage(backgroundImage, 0, 0, null); 
		}
		
		if(phase == GameManager.GamePhase.GAMEOVER) {
			dog.draw(g2d);
			g2d.drawImage(backgroundImage, 0, 0, null);	
		}
	        
		hud.draw(g2d, dog, duck, this);
	}

	public GamePhase getGamePhase() {
		return phase;
	}
	
	public void setGamePhase(GamePhase phase) {
		this.phase = phase;
	}
	
	private boolean dogIsWaiting_and_duckIsIn_Flying_orHit_orFalling_orEscaping(Dog dog, Duck duck) {
		return dog.getState() == Dog.DogState.WAIT && (duck.getState() == Duck.DuckState.FLYING || 
				duck.getState() == Duck.DuckState.HIT || duck.getState() == Duck.DuckState.FALLING||
				duck.getState() == Duck.DuckState.ESCAPING || duck.getState() == Duck.DuckState.WAITING);
	}
	
	private boolean duckHit_or_Escaped(Duck duck, Dog dog) {
		return dog.getState() == Dog.DogState.COMEUP && duck.getState() != Duck.DuckState.GAMEOVER;
	}
	
	private boolean ifPlayerPassedRound(Dog dog, Duck duck) {
		return dog.getState() == Dog.DogState.WAIT && duck.getState() == Duck.DuckState.NEXT_ROUND;
	}
	
	private boolean ifPlayerHasNOTPassedRound(Dog dog, Duck duck) {
		return dog.getState() == Dog.DogState.WAIT && duck.getState() == Duck.DuckState.GAMEOVER;
	}

}
