package core.menu;

import static core.Frame.SCREEN_SIZE;
import gui.FontManager.FontType;
import gui.GameGUI;
import gui.LevelSelectionGUI;
import gui.ui.ButtonElement;
import gui.ui.HudElement.InteractionType;
import gui.ui.HudOverlay;
import gui.ui.TextElement;

import java.awt.Color;
import java.util.List;

import mission.map.LevelMap;
import mission.map.LevelMap.LevelType;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import phys.Point2D;
import phys.Shape;
import core.Frame;
import core.controller.MouseController;
import core.controller.ShipController;

public class LevelSelectionScreen extends SetupScreen {

	/* GUI */
	HudOverlay menuOverlay;
	LevelSelectionGUI gui;
		
	Point2D[] cursorPosition = new Point2D[] {
			new Point2D(Frame.SCREEN_SIZE[0]/3f,Frame.SCREEN_SIZE[1]/2f),
			new Point2D(Frame.SCREEN_SIZE[0]*2/3f,Frame.SCREEN_SIZE[1]/2f)
	};
	
	boolean hotDown[] = {false, false};
	ShipController[] controllers;
	boolean[] hotGamePadDown;
	boolean play = false;
		
	LevelType levelType = LevelType.THEHIVE;
	
	public LevelSelectionScreen(Frame frame, ShipController[] controllers) {
		super(frame);
		this.gui = new LevelSelectionGUI();
		this.controllers = controllers;
		
		// create menu UI
		menuOverlay = new HudOverlay();
		float hs = SCREEN_SIZE[1]/10;
		Shape bs = new Shape(new Point2D[] {
				new Point2D(-hs*2,-hs/2),
				new Point2D( hs*2,-hs/2),
				new Point2D( hs*2, hs/2),
				new Point2D(-hs*2, hs/2)
		});
		
		ButtonElement playButton = new ButtonElement("play_button", bs, new Point2D(SCREEN_SIZE[0]/2 + 2*hs, SCREEN_SIZE[1]-hs), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		playButton.addCommand(InteractionType.MOUSE_DOWN, "play");
		playButton.addElement(new TextElement("pbt", bs,new Point2D(), "PLAY", FontType.FONT_32));
		
		ButtonElement backButton = new ButtonElement("quit_button", bs, new Point2D(SCREEN_SIZE[0]/2 - 2*hs, SCREEN_SIZE[1]-hs), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		backButton.addCommand(InteractionType.MOUSE_DOWN, "quit");
		backButton.addElement(new TextElement("bbt", bs,new Point2D(), "BACK", FontType.FONT_32));

		menuOverlay.addElement(playButton);
		menuOverlay.addElement(backButton);
	}
	
	public void setupLevel() {
		
		Mouse.setGrabbed(true);
		finished = false;
		
		while(!finished) {
			
			pollInput();
						
			// draw view
			gui.draw(this);
			menuOverlay.draw();
			gui.drawCursors(this);
			
			// opengl update
			Display.update();
			Display.sync(60);
						
			if(Display.isCloseRequested())
				finished = true;
		}

		Mouse.setGrabbed(false);
	}

	private void pollInput() {

		// HUD commands with mouse
		List<String> commands = menuOverlay.pollInput();
		for(String com: commands) {
			switch(com) {
			case "play": finish(); return;
			case "quit": cancel(); return;
			}
		}
		
		// select level
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
					case "play": finish(); return;
					case "quit": cancel(); return;
					}
				}
			}
			
			// level buttons
			if(controllers[i].isSelecting()) {
				if(hotDown[i]) {
					hotDown[i] = false;
					selectLevel(i);
				}
			} else {
				hotDown[i] = true;
			}
		}
	}

	private void selectLevel(int player) {
		int level = gui.getSlot(player, cursorPosition[player]);
		if(level<0 || level>=LevelMap.LevelType.values().length) return;
		levelType = LevelMap.LevelType.values()[level];
	}
		
	private float clamp(float value, float min, float max) {
		if(value>max) return max;
		if(value<min) return min;
		return value;
	}

	public boolean toPlay() {
		return play;
	}
	
	public ShipController[] getControllers() {
		return controllers;
	}
	
	public Point2D[] getCursorPosition() {
		return cursorPosition;
	}
	
	public LevelType getLevelType() {
		return levelType;
	}
	
	@Override
	public void cancel() {
		play = false;
		finished = true;
	}
	
	@Override
	public void finish() {
		play = true;
		finished = true;
	}
}
