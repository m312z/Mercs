package core.menu;

import static core.Frame.SCREEN_SIZE;
import gui.ControllerSelectionGUI;
import gui.GameGUI;
import gui.FontManager.FontType;
import gui.ui.ButtonElement;
import gui.ui.HudElement.InteractionType;
import gui.ui.HudOverlay;
import gui.ui.TextElement;

import java.awt.Color;
import java.util.List;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.java.games.input.ControllerEnvironment;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import phys.Point2D;
import phys.Shape;
import core.Frame;
import core.controller.KeyboardController;
import core.controller.MouseController;
import core.controller.ShipController;
import core.controller.XboxController;


public class ControllerSelectionScreen extends SetupScreen {
	
	/* GUI */
	HudOverlay menuOverlay;
	ControllerSelectionGUI gui;
	
	ShipController[] controllers;
	
	boolean mouseSelected = false;
	boolean hotMouseDown = false;
	
	boolean keyboardSelected = false;
	boolean hotKeyboardDown = true;
	
	boolean[] hotGamePadDown;
	boolean[] gamepadSelected;
	
	public ControllerSelectionScreen(Frame frame) {
		super(frame);
		
		gui = new ControllerSelectionGUI();
		hotGamePadDown = new boolean[ControllerEnvironment.getDefaultEnvironment().getControllers().length];
		gamepadSelected = new boolean[ControllerEnvironment.getDefaultEnvironment().getControllers().length];
		controllers = new ShipController[2];
		
		// create menu UI
		menuOverlay = new HudOverlay();
		float hs = SCREEN_SIZE[1]/10;
		Shape bs = new Shape(new Point2D[] {
				new Point2D(-hs*2,-hs/2),
				new Point2D( hs*2,-hs/2),
				new Point2D( hs*2, hs/2),
				new Point2D(-hs*2, hs/2)
		});
				
		ButtonElement backButton = new ButtonElement("quit_button", bs, new Point2D(SCREEN_SIZE[0]/2, SCREEN_SIZE[1]-hs), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		backButton.addCommand(InteractionType.MOUSE_DOWN, "quit");
		backButton.addElement(new TextElement("bbt", bs,new Point2D(), "BACK", FontType.FONT_32));

		menuOverlay.addElement(backButton);
	}
	
	public void setupControllers() {
		
		finished = false;
		
		while(!finished) {
			
			pollInput();
						
			// draw view
			frame.getBackground().draw();
			gui.draw(this);
			menuOverlay.draw();
			
			// opengl update
			Display.update();
			Display.sync(60);
						
			if(Display.isCloseRequested())
				finished = true;
		}
	}

	public ShipController[] getControllers() {
		return controllers;
	}

	private void pollInput() {
		
		// HUD commands
		List<String> commands = menuOverlay.pollInput();
		for(String com: commands) {
			switch(com) {
			case "quit": cancel(); return;
			}
		}
				
		// keyboard
		if(Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			if(hotKeyboardDown && keyboardSelected) {
				keyboardSelected = false;
				if(controllers[0]!=null && controllers[0] instanceof KeyboardController) controllers[0] = null;
				else if(controllers[1]!=null && controllers[1] instanceof KeyboardController) controllers[1] = null;
			}
			hotKeyboardDown = false;
		} else if(Keyboard.isKeyDown(Keyboard.KEY_T)) {
			if(hotKeyboardDown && !keyboardSelected) {
				int p = getFirstFree();
				if(p>=0) {
					keyboardSelected = true;
					controllers[p] = new KeyboardController("player"+p,p);
				}
			} else if(hotKeyboardDown) {
				// start game
				finished = true;
			}
			hotKeyboardDown = false;
		} else {
			hotKeyboardDown = true;
		}
		
		// mouse
		if(Mouse.isButtonDown(1)) {
			if(hotMouseDown && mouseSelected) {
				mouseSelected = false;
				if(controllers[0]!=null && controllers[0] instanceof MouseController) controllers[0] = null;
				else if(controllers[1]!=null && controllers[1] instanceof MouseController) controllers[1] = null;
			}
			hotMouseDown = false;
		} else if(Mouse.isButtonDown(0)) {
			if(hotMouseDown && !mouseSelected) {
				int p = getFirstFree();
				if(p>=0) {
					mouseSelected = true;
					controllers[p] = new MouseController("player"+p,p);
				}
			} else if (hotMouseDown) {
				// start game
				finished = true;
			}
			hotMouseDown = false;
		} else {
			hotMouseDown = true;
		}
		
		// controller
		for(int i=0;i<ControllerEnvironment.getDefaultEnvironment().getControllers().length;i++) {
			Controller c = ControllerEnvironment.getDefaultEnvironment().getControllers()[i];
			if(c.getType()!=Type.GAMEPAD && c.getType()!=Type.STICK) continue;
			c.poll();
			if(c.getComponent(Component.Identifier.Button._1).getPollData()>0) {
				if(!hotGamePadDown[i] && gamepadSelected[i]) {
					gamepadSelected[i] = false;
					if(controllers[0]!=null && controllers[0] instanceof XboxController) {
						controllers[0] = null;
					} else if(controllers[1]!=null && controllers[1] instanceof XboxController) {
						controllers[1] = null;
					}
				}
				hotGamePadDown[i] = true;
			} else if(c.getComponent(Component.Identifier.Button._0).getPollData()>0) {
				if(!hotGamePadDown[i]) {
					if(!gamepadSelected[i]) {
						int p = getFirstFree();
						if(p>=0) {
							gamepadSelected[i] = true;
							controllers[p] = new XboxController("player"+p,p,c);			
						}
					} else {
						// start game
						finished = true;
					}
				}
				hotGamePadDown[i] = true;
			} else {
				hotGamePadDown[i] = false;
			}
		}
	}

	private int getFirstFree() {
		if(controllers[0]==null)
			return 0;
		else if(controllers[1]==null)
			return 1;
		else return -1;
	}

	public void cancel() {
		controllers[0] = null;
		controllers[1] = null;
		this.finished = true;
	}
}
