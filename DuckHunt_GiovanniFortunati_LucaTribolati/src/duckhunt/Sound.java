package duckhunt;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Sound {
	
	private Clip clip;
	private URL soundUrl[] = new URL[23];
	
	public Sound() {
		soundUrl[0] = getClass().getResource("/sounds/dog_Barking.wav");
		soundUrl[1] = getClass().getResource("/sounds/dog_Laughing.wav");
		soundUrl[2] = getClass().getResource("/sounds/dog_Single_Barking.wav"); //NOT USED
		soundUrl[3] = getClass().getResource("/sounds/dog_Success.wav");
		soundUrl[4] = getClass().getResource("/sounds/duck_Counting.wav"); 
		soundUrl[5] = getClass().getResource("/sounds/duck_Falling_With_Drop.wav"); //NOT USED
		soundUrl[6] = getClass().getResource("/sounds/duck_Falling.wav");
		soundUrl[7] = getClass().getResource("/sounds/duck_Flap_No_Quack.wav"); //NOT USED
		soundUrl[8] = getClass().getResource("/sounds/duck_Flap.wav");
		soundUrl[9] = getClass().getResource("/sounds/duck_Hitting_Ground.wav");
		soundUrl[10] = getClass().getResource("/sounds/duck_Single_Flap.wav"); //NOT USED
		soundUrl[11] = getClass().getResource("/sounds/duck_Single_Quack.wav"); //NOT USED
		soundUrl[12] = getClass().getResource("/sounds/gameover_1.wav");
		soundUrl[13] = getClass().getResource("/sounds/gameover_2.wav");
		soundUrl[14] = getClass().getResource("/sounds/gameover_Complete.wav"); //NOT USED
		soundUrl[15] = getClass().getResource("/sounds/gunshot.wav");
		soundUrl[16] = getClass().getResource("/sounds/intro_Bark.wav");
		soundUrl[17] = getClass().getResource("/sounds/intro_No_Bark.wav"); //NOT USED
		soundUrl[18] = getClass().getResource("/sounds/instructions_Panel.wav");
		soundUrl[19] = getClass().getResource("/sounds/next_Round.wav");
		soundUrl[20] = getClass().getResource("/sounds/pause.wav");
		soundUrl[21] = getClass().getResource("/sounds/perfect.wav"); //NOT USED
		soundUrl[22] = getClass().getResource("/sounds/start.wav");
	}
	
	public void setFile(int i) {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(soundUrl[i]);
			clip = AudioSystem.getClip();
			clip.open(ais);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} 	
	}
	
	public void play() {
		clip.start();
	}
	
	public void loop() {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	public void stop() {
		clip.stop();
	}
	
	public long pause() {
		return clip.getMicrosecondPosition();
	}
	
	public void resume(long position) {
		clip.setMicrosecondPosition(position);
	}

}
