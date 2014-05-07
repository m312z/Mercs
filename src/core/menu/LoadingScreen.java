package core.menu;

import gui.FontManager;
import gui.LoadingScreenGUI;
import gui.TextureLoader;
import gui.ui.HudOverlay;

import org.lwjgl.opengl.Display;

import sound.SoundManager;
import core.Frame;
import core.Frame.GameState;

/**
 * Loading bar controller.
 * @author Michael Cashmore
 */
public class LoadingScreen extends SetupScreen {

	/* GUI */
	HudOverlay menuOverlay;
		
	public LoadingScreen(Frame frame) {
		super(frame);
		
		FontManager.makeFonts();
		
		// create menu UI
		menuOverlay = new HudOverlay();
		/*
		TextElement text = new TextElement(
				"loading_string",
				Shape.nullShape,
				new Point2D(
						(Frame.SCREEN_SIZE[0] - FontManager.trueTypeFont_32.getWidth("Loading"))/2f,
						FontManager.trueTypeFont_32.getHeight()),
				"Loading",
				FontType.FONT_32);
		menuOverlay.addElement(text);
		*/
	}

	public void start() {

		LoadingScreenGUI gui = new LoadingScreenGUI();
		
		int state = 0;
		SoundLoader loader = new SoundLoader();
		Thread thread = new Thread(loader);
		thread.start();
		
		finished = false;
		while(!finished) {

			// load next object
			switch(state) {
			case 0:
				// images
				TextureLoader.getInstance().loadOneTexture();
				if(TextureLoader.getInstance().getLoadedImages()
						== TextureLoader.getInstance().getTotalImages().size()) {
					state++;
					/*
					((TextElement)menuOverlay.getElement("loading_string")).setText("Loading music");
					menuOverlay.getElement("loading_string").getPos().x = (Frame.SCREEN_SIZE[0] - FontManager.getFont(FontType.FONT_32).getWidth("Loading music"))/2f;
					*/
				}
				break;
			case 1: 
				// sounds
				if(!thread.isAlive()) finish();
				break;
			}
			
			// draw view
			gui.draw( (float)(TextureLoader.getInstance().getLoadedImages() + SoundManager.soundsLoaded*TextureLoader.getInstance().getTotalImages().size())
					/ (float)(TextureLoader.getInstance().getTotalImages().size()*3));
			menuOverlay.draw();
			
			// opengl update
			Display.update();
			Display.sync(60);
						
			if(Display.isCloseRequested())
				finished = true;
		}
	}
		
	@Override
	public void cancel() {
		frame.state = GameState.MAINMENU;
		finished = true;
	}
	
	@Override
	public void finish() {
		frame.state = GameState.MAINMENU;
		finished = true;
	}
	
	class SoundLoader implements Runnable
	{
		@Override
		public void run() {
			SoundManager.init();
		}	
	}
}