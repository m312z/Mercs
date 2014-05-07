package core.menu;

import static core.Frame.SCREEN_SIZE;
import gui.FontManager.FontType;
import gui.GameGUI;
import gui.PowerSelectionGUI;
import gui.ui.ButtonElement;
import gui.ui.HudElement.InteractionType;
import gui.ui.HudOverlay;
import gui.ui.TextElement;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import mission.powers.Power;
import monitor.PowerShop;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import phys.Point2D;
import phys.Shape;
import core.Frame;
import core.controller.MouseController;
import core.controller.ShipController;

public class PowerSelectionScreen extends SetupScreen {

	/* GUI */
	HudOverlay menuOverlay;
	PowerSelectionGUI gui;
		
	Point2D[] cursorPosition = new Point2D[] {
			new Point2D(Frame.SCREEN_SIZE[0]/3f,Frame.SCREEN_SIZE[1]/2f),
			new Point2D(Frame.SCREEN_SIZE[0]*2/3f,Frame.SCREEN_SIZE[1]/2f)
	};
	
	ShipController[] controllers;
	boolean hotDown[] = {false, false};
	boolean play = true;
	
	Set<Integer> powers0;
	Set<Integer> powers1;
	
	public PowerSelectionScreen(Frame frame, ShipController[] controllers) {
		super(frame);
		
		this.gui = new PowerSelectionGUI();
		this.controllers = controllers;
		powers0 = new TreeSet<Integer>();
		powers1 = new TreeSet<Integer>();
		
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
	
	public void setupPowers() {
		
		Mouse.setGrabbed(true);
		
		// load saved power builds
		frame.getFileManager().readPowers();
		for(int p: frame.getFileManager().getPlayerPowers(0))
			if(p>=0 && PowerShop.isPowerUnlocked(p)) powers0.add(p);
		for(int p: frame.getFileManager().getPlayerPowers(1))
			if(p>=0 && PowerShop.isPowerUnlocked(p)) powers1.add(p);
		
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
		
		// save new builds
		frame.getFileManager().savePowers(powers0,powers1);
		
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
		
		// select powers
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
			
			// power buttons
			if(controllers[i].isSelecting()) {
				if(hotDown[i]) {
					hotDown[i] = false;
					selectPower(i);
				}
			} else {
				hotDown[i] = true;
			}
		}
	}

	private void selectPower(int player) {
				
		int slot = gui.getSlot(player, cursorPosition[player]);
		if(slot==-1) return;
		
		if(!PowerShop.isPowerUnlocked(slot))
			return;
		
		Set<Integer> powers = powers0;
		if(player==1) powers = powers1;
		
		if(!powers.contains(slot) && powers.size()<6 && (!Power.values()[slot].active || getNoActivePowers(player)<4))
			powers.add(slot);
		else if(powers.contains(slot))
			powers.remove(slot);
	}
	
	public int getNoActivePowers(int player) {
		int amnt = 0;
		Set<Integer> list = getPowers(player);
		for(Integer i: list) {
			if(Power.values()[i].active)
				amnt++;
		}
		return amnt;
	}

	public Set<Integer> getPowers(int player) {
		if(player==1) return powers1;
		return powers0;
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
	
	@Override
	public void cancel() {
		play = false;
		finished = true;
	}
}
