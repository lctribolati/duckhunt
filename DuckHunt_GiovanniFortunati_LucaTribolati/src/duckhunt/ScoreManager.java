package duckhunt;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ScoreManager {

	private static final String FILE_PATH = getResPath() + "/docs/highscore.txt";

	private int highScore;
	private int[] score;
	
	public ScoreManager() {
		highScore = loadHighScore();
		score = new int[6];
	}
	
	public int loadHighScore() {
		File file = new File(FILE_PATH);
	    if (!file.exists()) {
	        try {
	            file.createNewFile(); // Creates file if not exists
	        } catch (IOException e) {
	            e.printStackTrace();
	            return 0;
	        }
	        return 0;
	    }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
	}
	
	// UPDATES MAX SCORE
    public void updateHighScore(int currentScore) {
        if (currentScore > highScore) {
            highScore = currentScore;
            saveHighScore();
        }
    }
    
    // SAVES MAX SCORE IN FILE
    public void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public int getHighScore() {
        return highScore;
    }
    
    
    public int getPointDigit(int i) {
    	int nextNumber = highScore;
    	for(int j=score.length - 1; j >= 0; j--) {
    		score[j] = nextNumber % 10;
    		nextNumber = nextNumber / 10;
    	}
    	return score[i];
    }
    
    private static String getResPath() {
        try {
            File codeSource = new File(ScoreManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            File resFolder;

            if (codeSource.isFile()) { // JAR
                File distFolder = codeSource.getParentFile();
                File gameFolder = distFolder.getParentFile();
                resFolder = new File(gameFolder, "res");
            } 
            else { // IDE
                File projectFolder = new File("").getAbsoluteFile();
                resFolder = new File(projectFolder, "res");
            }
            return resFolder.getAbsolutePath();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "res";
        }
    }

}
