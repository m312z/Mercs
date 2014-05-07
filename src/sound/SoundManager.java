package sound;

import java.io.IOException;

import mission.map.LevelMap.LevelType;

import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.openal.SoundStore;
import org.newdawn.slick.util.ResourceLoader;

public class SoundManager {

	static boolean musicPlaying;
	static boolean soundPlaying = true;
	static boolean musicMute;
	
	/* sound effects */
	public enum SoundEffect
	{
		EXPLOSION("sound/explosion.wav"),
		PROJECTILE("sound/projectile.wav");
		
		String file;
		Audio wavEffect;
		
		private SoundEffect(String file) {
			this.file = file; 
			try {
				this.wavEffect = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream(file));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
		
	/* music */
	public static int soundsLoaded;
	static Audio oggEffect01;
	static Audio oggEffect02;
	
	public static void init() {
		SoundStore.get().setMaxSources(16);
		soundsLoaded = 0;
//		try {
//			oggEffect01 = AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("sound/Smash02Locust.ogg"));
//			soundsLoaded++;
//			oggEffect02 = AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("sound/Smash05Dose.ogg"));
//			soundsLoaded++;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
		
	public static void startMusic(LevelType levelType) {
		musicPlaying = true;
		switch(levelType) {
		case THEHIVE:
			oggEffect01.playAsMusic(1.0f, 1.0f, true);
			SoundStore.get().setMusicVolume(0.4f);
			break;
		default:
			oggEffect02.playAsMusic(1.0f, 1.0f, true);
			SoundStore.get().setMusicVolume(0.4f);
			break;
		}
	}
	
	public static void stopMusic() {
		if(musicPlaying) {
			musicPlaying = false;
			if(oggEffect01.isPlaying())
				oggEffect01.stop();
			if(oggEffect02.isPlaying())
				oggEffect02.stop();
		}
	}
	
	public static void playSound(SoundEffect effect) {
		if(soundPlaying) {
			switch(effect) {
			case PROJECTILE:
				if(effect.wavEffect!=null && !effect.wavEffect.isPlaying())
					effect.wavEffect.playAsSoundEffect(1.0f, 1.0f, false);
				break;
			case EXPLOSION:
				if(effect.wavEffect!=null)
					effect.wavEffect.playAsSoundEffect(1.0f, 1.0f, false);
				break;
			}
		}
	}
	
	public static void update() {
		
		if(!musicPlaying && !soundPlaying)
			return;
		
		// chance to queue buffers
		SoundStore.get().poll(0);
	}

	public static boolean isMusicMute() {
		return musicMute;
	}

	public static boolean isMusicPlaying() {
		return musicPlaying;
	}
	
	public static boolean isSoundPlaying() {
		return soundPlaying;
	}
	
	public static void setSoundEffects(boolean sound) {
		soundPlaying = sound;
	}
	
	public static void setMusicMute(boolean mute) {
		musicMute = mute;
	}
}