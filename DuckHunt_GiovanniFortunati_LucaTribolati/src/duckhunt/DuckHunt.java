package duckhunt;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class DuckHunt {

	public static void main(String[] args) {
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setTitle("Duck Hunt");
		
		ImageIcon icon = new ImageIcon(DuckHunt.class.getResource("/images/logo.png"));
		frame.setIconImage(icon.getImage());
		
		GamePanel gamePanel = new GamePanel();
		frame.add(gamePanel);

		frame.pack();
		
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		gamePanel.startGameThread();
	}

}
