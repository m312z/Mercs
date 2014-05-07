package core.menu;

import static core.Frame.SCREEN_SIZE;
import static gui.OpenGLDraw.fillRect;
import gui.OpenGLDraw;
import gui.Texture;
import gui.TextureLoader;

import java.awt.Color;


/**
 * The helpful background of the menu screen. 
 * @author Michael Cashmore
 *
 */
public class MenuBackground {
	
	/* ratio of SCREEN_SIZE/VIEW_SIZE */
	public static float scale;
	/* offset to center the screen when full-screen */
	public static float offX = 0;
	public static float offY = 0;
	
	/* colour of the background */
	static final Color back = new Color(80,20,80);
	
	public void draw() {
		
		// draw background
		fillRect(Color.BLACK, 0, 0, SCREEN_SIZE[0], SCREEN_SIZE[1]);

		Texture tex = TextureLoader.getInstance().getTexture("stars");
		OpenGLDraw.bindTexture(tex);
		OpenGLDraw.drawTexture(0, 0, SCREEN_SIZE[0], SCREEN_SIZE[1]);
		OpenGLDraw.unbindTexture();
	}
}
