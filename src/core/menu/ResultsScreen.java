package core.menu;

import static core.Frame.SCREEN_SIZE;
import gui.FontManager.FontType;
import gui.GameGUI;
import gui.ResultsGUI;
import gui.ui.ButtonElement;
import gui.ui.HudElement.InteractionType;
import gui.ui.HudOverlay;
import gui.ui.TextElement;

import java.awt.Color;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import phys.Point2D;
import phys.Shape;
import core.Frame;
import core.Frame.GameState;
import core.controller.MouseController;
import core.controller.ShipController;

/**
 * Main menu controller.
 * @author Michael Cashmore
 */
public class ResultsScreen extends SetupScreen {

	/* GUI */
	HudOverlay menuOverlay;
	ResultsGUI gui;
	
	Point2D[] cursorPosition = new Point2D[] {
			new Point2D(Frame.SCREEN_SIZE[0]/3f,Frame.SCREEN_SIZE[1]/2f),
			new Point2D(Frame.SCREEN_SIZE[0]*2/3f,Frame.SCREEN_SIZE[1]/2f)
	};
	
	ShipController[] controllers;
	boolean hotDown[] = {false, false};
	
	public ResultsScreen(Frame frame, ShipController[] controllers) {
		super(frame);
		
		this.controllers = controllers;
		
		// create menu UI
		menuOverlay = new HudOverlay();
		this.gui = new ResultsGUI(frame,menuOverlay);
		float hs = SCREEN_SIZE[1]/10;
		Shape bs = new Shape(new Point2D[] {
				new Point2D(-hs*4/3f,-hs/2),
				new Point2D( hs*4/3f,-hs/2),
				new Point2D( hs*4/3f, hs/2),
				new Point2D(-hs*4/3f, hs/2)
		});
		
		ButtonElement backButton = new ButtonElement("quit_button", bs, new Point2D(SCREEN_SIZE[0]/2 - hs*8/3f, SCREEN_SIZE[1]-hs), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		backButton.addCommand(InteractionType.MOUSE_DOWN, "quit");
		backButton.addElement(new TextElement("bbt", bs,new Point2D(), "MAIN MENU", FontType.FONT_32));
		
		ButtonElement powerButton = new ButtonElement("play_button", bs, new Point2D(SCREEN_SIZE[0]/2, SCREEN_SIZE[1]-hs), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		powerButton.addCommand(InteractionType.MOUSE_DOWN, "play");
		powerButton.addElement(new TextElement("pbt", bs,new Point2D(), "POWER SETUP", FontType.FONT_32));

		ButtonElement restartButton = new ButtonElement("restart_button", bs, new Point2D(SCREEN_SIZE[0]/2 + hs*8/3f, SCREEN_SIZE[1]-hs), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		restartButton.addCommand(InteractionType.MOUSE_DOWN, "restart");
		restartButton.addElement(new TextElement("rbt", bs,new Point2D(), "RESTART", FontType.FONT_32));
		
		menuOverlay.addElement(powerButton);
		menuOverlay.addElement(restartButton);
		menuOverlay.addElement(backButton);
	}

	public void start() {
		
		Mouse.setGrabbed(true);
		finished = false;
		
		while(!finished) {

			// pollInput
			pollInput();
						
			// draw view
			frame.getBackground().draw();
			gui.draw(frame.getLastResults(), frame.getHighScore());
			menuOverlay.draw();
			gui.drawCursors(controllers, cursorPosition);
			
			// opengl update
			Display.update();
			Display.sync(60);
						
			if(Display.isCloseRequested())
				finished = true;
		}
		
		Mouse.setGrabbed(false);
	}

	private void pollInput() {
		
		List<String> commands = menuOverlay.pollInput();
		for(String com: commands) {
			switch(com) {
			case "restart":
				frame.state = GameState.ARCADEGAME;
				finished = true;
				break;
			case "play": 
				frame.state = GameState.SETUP_POWERS;
				finished = true;
				break;
			case "quit": 
				frame.state = GameState.MAINMENU;
				finished = true;
				break;
			}
		}
		
		for(int i=0;i<2;i++) {
			if(controllers[i]==null) continue;
			
			// move cursors
			if(controllers[i] instanceof MouseController) {
				cursorPosition[i].x = Mouse.getX();
				cursorPosition[i].y = Frame.SCREEN_SIZE[1] - Mouse.getY();
			} else {
				controllers[i].pollInput();
				cursorPosition[i].x += controllers[i].getDirection().x*Frame.SCREEN_SIZE[0]/160f;
				cursorPosition[i].y += controllers[i].getDirection().y*Frame.SCREEN_SIZE[0]/160f;
				cursorPosition[i].x = clamp(cursorPosition[i].x,0,Frame.SCREEN_SIZE[0]-10);
				cursorPosition[i].y = clamp(cursorPosition[i].y,0,Frame.SCREEN_SIZE[1]-10);
			}
			
			// HUD interaction with non-mouse controllers
			if(controllers[i].isSelecting() && !(controllers[i] instanceof MouseController)) {
				
				// HUD commands
				commands = menuOverlay.interact(cursorPosition[i], InteractionType.MOUSE_DOWN);
				for(String com: commands) {
					switch(com) {
					case "restart":
						frame.state = GameState.ARCADEGAME;
						finished = true;
						break;
					case "play": 
						frame.state = GameState.SETUP_POWERS;
						finished = true;
						break;
					case "quit": 
						frame.state = GameState.MAINMENU;
						finished = true;
						break;
					}
				}
			}
		}
	}
		
	private float clamp(float value, float min, float max) {
		if(value>max) return max;
		if(value<min) return min;
		return value;
	}

	@Override
	public void cancel() {
		frame.state = GameState.MAINMENU;
		finished = true;		
	}
}
