package core.controller;

import org.lwjgl.input.Keyboard;

import core.Frame;

public class KeyboardController extends ShipController {

	boolean pausing;
	
	public KeyboardController(String player, int playerID) {
		super(player, playerID);
	}

	@Override
	public void pollInput() {
		
		// movement
		if(Keyboard.isKeyDown(Keyboard.KEY_UP))
			direction.y = -1;
		else if(Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			direction.y = 1;
		else 
			direction.y = 0;

		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT))
			direction.x = -1;
		else if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			direction.x = 1;
		else 
			direction.x = 0;
		
		// shooting
		shooting = (Keyboard.isKeyDown(Keyboard.KEY_T));
		
		powerButtons[0] = (Keyboard.isKeyDown(Keyboard.KEY_Q));
		powerButtons[1] = (Keyboard.isKeyDown(Keyboard.KEY_W));
		powerButtons[2] = (Keyboard.isKeyDown(Keyboard.KEY_E));
		powerButtons[3] = (Keyboard.isKeyDown(Keyboard.KEY_R));
		
		pausing = (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE));
		
		cursorPosition.x += direction.x*Frame.SCREEN_SIZE[0]/160f;
		cursorPosition.y += direction.y*Frame.SCREEN_SIZE[0]/160f;
		cursorPosition.x = clampCursor(cursorPosition.x,0,Frame.SCREEN_SIZE[0]-10);
		cursorPosition.y = clampCursor(cursorPosition.y,0,Frame.SCREEN_SIZE[1]-10);
	}

	@Override
	public boolean isSelecting() {
		return (powerButtons[0] || powerButtons[1] || powerButtons[2] || powerButtons[3] || shooting);
	}

	@Override
	public boolean isPausing() {
		return pausing;
	}
}
