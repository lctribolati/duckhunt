package duckhunt;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class HitHandler implements MouseListener{
	
	private Duck duck;
	private GamePanel gamePanel;
	private HUDManager hud;

	private int duckWidth, duckHeight;
	
	public HitHandler(Duck duck, Dog dog, GamePanel gameP, HUDManager hud) {
		
		this.gamePanel = gameP;
		this.duck = duck;
		this.hud = hud;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		if(duckIsFlying_and_gameNotInPause()) {
			
			gamePanel.pauseMusic(); //Stops duck flapping sound
			gamePanel.playMusicWithoutUpdatingIndexOrBoolean(15); //Play shooting sound
			
			duckWidth = duck.getCurrentDuckImage().getWidth(null);
			duckHeight = duck.getCurrentDuckImage().getHeight(null);
			
			hud.bulletShot();
			
			if(userClickedInDuckHitbox(e)) {
				
				duck.setState(Duck.DuckState.HIT);
				
				duck.setHit(true);
				
				duck.assignPoints();
				duck.setPointsToDigits();
				
				duck.updateScoreCoordinates((int)duck.getX(), (int)duck.getY());
			}
			else { //If duck missed resumes flapping sound
				 
				gamePanel.resumeLoopMusic(gamePanel.getMusicCurrentIndex(), gamePanel.getlastMusicPosition());
			}
			
			gamePanel.triggerFlash();
			
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	
	private boolean duckIsFlying_and_gameNotInPause() {
		return duck.getState() == Duck.DuckState.FLYING && !gamePanel.returnStatus();
	}
	
	private boolean userClickedInDuckHitbox(MouseEvent e) {
		return e.getX() >= duck.getX() * gamePanel.getScale() && 
				e.getX() <= duck.getX() * gamePanel.getScale() + duckWidth * gamePanel.getScale() &&
				e.getY() >= duck.getY() * gamePanel.getScale() && 
				e.getY() <= duck.getY() * gamePanel.getScale() + duckHeight * gamePanel.getScale();
	}

}
