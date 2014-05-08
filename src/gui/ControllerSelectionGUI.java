package gui;

import static core.Frame.SCREEN_SIZE;
import static gui.OpenGLDraw.fillRect;

import java.awt.Color;

import mission.Board;
import core.controller.AIController;
import core.controller.KeyboardController;
import core.controller.MouseController;
import core.controller.XboxController;
import core.menu.ControllerSelectionScreen;

public class ControllerSelectionGUI {
	
	/* size of the view window in terms of game size units */
	public static float VIEW_SIZE = Board.BOARD_SIZE;
	
	/* colour pallete */
	static final Color back = new Color(80,80,80);
	public static final Color[] playerDark = {new Color(0,60,255),new Color(6,183,0)};
	public static final Color energy = new Color(224,255,255);
	
	/* offset to center the screen when full-screen */
	public static float hudSize = 0;
	public static float panelWidth;
	public static float panelHeight;
	public static float indent;
	
	/* visual stuff */
	float[] pos = {0f,0f};
	
	/**
	 * Draw the game.
	 * @param board	the model to be drawn
	 * @param drawHUD	true if the size-bars are to be drawn
	 */
	public void draw(ControllerSelectionScreen selection) {
		
		hudSize = SCREEN_SIZE[1]/10f;
		panelHeight = SCREEN_SIZE[1]*3/4f;
		panelWidth = panelHeight * 2/3f;
		indent = hudSize/20f;

		Texture tex = null;		
		for(int i=0;i<2;i++) {
			
			float offX = SCREEN_SIZE[0]/2f - (1-i)*panelWidth;
			
			// border
			fillRect(Color.BLACK,
					offX,
					hudSize,
					panelWidth,
					panelHeight);
			
			fillRect(energy,
					offX + indent,
					hudSize + indent,
					panelWidth - 2*indent,
					panelHeight - 2*indent);
			
			fillRect(Color.BLACK,
					offX + 2*indent,
					hudSize + 2*indent,
					panelWidth - 4*indent,
					panelHeight - 4*indent);
			
			if(selection.getControllers()[i]!=null) {
				
				fillRect(playerDark[i],
						offX + 3*indent,
						hudSize + 3*indent,
						panelWidth - 6*indent,
						panelHeight - 6*indent);
				
				if(selection.getControllers()[i] instanceof AIController)
					continue;
				
				if(selection.getControllers()[i] instanceof KeyboardController) {
					tex = TextureLoader.getInstance().getTexture("keyboardHelp2");
				} else if(selection.getControllers()[i] instanceof XboxController) {
					tex = TextureLoader.getInstance().getTexture("controllerHelp2");
				} else if(selection.getControllers()[i] instanceof MouseController) {
					tex = TextureLoader.getInstance().getTexture("mouseHelp2");
				}
				
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						offX + 3*indent,
						panelHeight + hudSize - panelWidth/2,
						panelWidth - 6*indent,
						(panelWidth - 6*indent)/2f);
				OpenGLDraw.unbindTexture();
				
				if(selection.getControllers()[i] instanceof KeyboardController) {
					tex = TextureLoader.getInstance().getTexture("keyboardHelp");
				} else if(selection.getControllers()[i] instanceof XboxController) {
					tex = TextureLoader.getInstance().getTexture("controllerHelp");
					if(pos[i]<0f) pos[i] = (panelWidth - 6*indent);
				} else if(selection.getControllers()[i] instanceof MouseController) {
					tex = TextureLoader.getInstance().getTexture("mouseHelp");
					if(pos[i]<0f) pos[i] = (panelWidth - 6*indent)/2f;
				}
				
				if(pos[i]>0.1f)
					pos[i] += (0 - pos[i])*0.1f;
				else
					pos[i] = 0;
					
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						offX + 3*indent,
						hudSize + 3*indent + pos[i],
						panelWidth - 6*indent,
						(panelWidth - 6*indent)/2f);
				OpenGLDraw.unbindTexture();
								
			} else {
				
				pos[i] = -10;
				
				// darkened background
				fillRect(back,
						offX + 3*indent,
						hudSize + 3*indent,
						panelWidth - 6*indent,
						panelHeight - 6*indent);
				
				tex = TextureLoader.getInstance().getTexture("controllerSelect");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						offX + 3*indent,
						hudSize + 3*indent,
						panelWidth - 6*indent,
						1.5f*(panelWidth - 6*indent));
				OpenGLDraw.unbindTexture();
				
			}
		}
	}
	
}
