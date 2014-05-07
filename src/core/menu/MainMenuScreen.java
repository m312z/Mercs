package core.menu;

import static core.Frame.FULLSCREEN;
import static core.Frame.SCREEN_SIZE;
import static core.Frame.WINDOW_SIZE;
import gui.GameGUI;
import gui.FontManager.FontType;
import gui.ui.ButtonElement;
import gui.ui.HudElement.InteractionType;
import gui.ui.HudOverlay;
import gui.ui.TextElement;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.Display;

import phys.Point2D;
import phys.Shape;
import sound.SoundManager;
import core.Frame;
import core.Frame.GameState;

/**
 * Main menu controller.
 * @author Michael Cashmore
 */
public class MainMenuScreen extends SetupScreen {

	/* GUI */
	HudOverlay menuOverlay;
		
	public MainMenuScreen(Frame frame) {
		super(frame);
		
		// create menu UI
		menuOverlay = new HudOverlay();
		float hs = SCREEN_SIZE[1]/10;
		Shape bs = new Shape(new Point2D[] {
				new Point2D(-hs*2,-hs/2),
				new Point2D( hs*2,-hs/2),
				new Point2D( hs*2, hs/2),
				new Point2D(-hs*2, hs/2)
		});
		
		ButtonElement startButton = new ButtonElement("start_button", bs, new Point2D(SCREEN_SIZE[0]/2, SCREEN_SIZE[1]/2 - 2*hs), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		startButton.addCommand(InteractionType.MOUSE_DOWN, "start");
		startButton.addElement(new TextElement("sbt", bs, new Point2D(0,0), "PLAY", FontType.FONT_32));
		
		ButtonElement optionsButton = new ButtonElement("options_button", bs, new Point2D(SCREEN_SIZE[0]/2, SCREEN_SIZE[1]/2 - hs), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		optionsButton.addCommand(InteractionType.MOUSE_DOWN, "options");
		optionsButton.addElement(new TextElement("obt", bs, new Point2D(), "FULLSCREEN", FontType.FONT_32));
		
		ButtonElement soundButton = new ButtonElement("sound_button", bs, new Point2D(SCREEN_SIZE[0]/2, SCREEN_SIZE[1]/2), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		soundButton.addCommand(InteractionType.MOUSE_DOWN, "sound");
		soundButton.addElement(new TextElement("sbt_on", bs, new Point2D(), "SOUNDS ARE ON", FontType.FONT_32));
		soundButton.addElement(new TextElement("sbt_off", bs, new Point2D(), "SOUNDS ARE OFF", FontType.FONT_32));
		
		ButtonElement musicButton = new ButtonElement("music_button", bs, new Point2D(SCREEN_SIZE[0]/2, SCREEN_SIZE[1]/2 + hs), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		musicButton.addCommand(InteractionType.MOUSE_DOWN, "music");
		musicButton.addElement(new TextElement("mbt_on", bs, new Point2D(), "MUSIC IS OFF", FontType.FONT_32));
		musicButton.addElement(new TextElement("mbt_off", bs, new Point2D(), "MUSIC IS ON", FontType.FONT_32));
		
		ButtonElement quitButton = new ButtonElement("quit_button", bs, new Point2D(SCREEN_SIZE[0]/2, SCREEN_SIZE[1]/2 + hs*2), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		quitButton.addCommand(InteractionType.MOUSE_DOWN, "quit");
		quitButton.addElement(new TextElement("qbt", bs,new Point2D(), "QUIT", FontType.FONT_32));
		
		menuOverlay.addElement(startButton);
		menuOverlay.addElement(optionsButton);
		menuOverlay.addElement(soundButton);
		menuOverlay.addElement(musicButton);
		menuOverlay.addElement(quitButton);
	}

	public void start() {
		
		menuOverlay.getElement("mbt_on",menuOverlay.getElement("music_button")).setVisible(SoundManager.isMusicMute());
		menuOverlay.getElement("mbt_off",menuOverlay.getElement("music_button")).setVisible(!SoundManager.isMusicMute());
		menuOverlay.getElement("sbt_on",menuOverlay.getElement("sound_button")).setVisible(SoundManager.isSoundPlaying());
		menuOverlay.getElement("sbt_off",menuOverlay.getElement("sound_button")).setVisible(!SoundManager.isSoundPlaying());
		
		finished = false;
		
		while(!finished) {

			// pollInput
			pollInput();
						
			// draw view
			frame.getBackground().draw();
			menuOverlay.draw();
			
			// opengl update
			Display.update();
			Display.sync(60);
						
			if(Display.isCloseRequested())
				finished = true;
		}
	}

	private void pollInput() {
		List<String> commands = menuOverlay.pollInput();
		for(String com: commands) {
			switch(com) {
			case "start": finish(); break;
			case "options": fullScreen(); break;
			case "music":
				SoundManager.setMusicMute(!SoundManager.isMusicMute());
				menuOverlay.getElement("mbt_on",menuOverlay.getElement("music_button")).setVisible(SoundManager.isMusicMute());
				menuOverlay.getElement("mbt_off",menuOverlay.getElement("music_button")).setVisible(!SoundManager.isMusicMute());
				break;
			case "sound":
				SoundManager.setSoundEffects(!SoundManager.isSoundPlaying());
				menuOverlay.getElement("sbt_on",menuOverlay.getElement("sound_button")).setVisible(SoundManager.isSoundPlaying());
				menuOverlay.getElement("sbt_off",menuOverlay.getElement("sound_button")).setVisible(!SoundManager.isSoundPlaying());
				break;
			case "quit": cancel(); break;
			}
		}
	}
		
	@Override
	public void cancel() {
		frame.state = GameState.END;
		finished = true;
	}
	
	@Override
	public void finish() {
		frame.state = GameState.SETUP_CONTROL;
		finished = true;
	}
	
	public void fullScreen() {
		FULLSCREEN = FULLSCREEN^true;
		frame.setDisplayMode(WINDOW_SIZE[0], WINDOW_SIZE[1], FULLSCREEN);
		finished = true;
	}
}
