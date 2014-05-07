package core.controller;

import static core.Frame.SCREEN_SIZE;
import static gui.GameGUI.scale;
import gui.GameGUI;

import mission.gameobject.Mech;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import core.Frame;

import phys.Point2D;

public class MouseController extends ShipController {

	boolean pausing;
	Point2D playerPos;
	int power = 0;
	int maxPowers = 4;
	
	public MouseController(String player, int playerID) {
		super(player,playerID);
		playerPos = new Point2D();
	}

	public Point2D getPlayerPos() {
		return playerPos;
	}
	
	@Override
	public void pollInput() {
		
		// movement
		direction.x = (Mouse.getX()-GameGUI.offX) - playerPos.x*scale;
		direction.y = (SCREEN_SIZE[1]-Mouse.getY()-GameGUI.offY) - playerPos.y*scale;
		if(Point2D.magnitude(direction)<Mech.MECH_SPEED*scale) {
			direction.x = 0;
			direction.y = 0;
		}
			
		// shooting
		shooting = (Mouse.isButtonDown(0));
		
		// powers
		for(int i=0;i<4;i++)
			powerButtons[i] = false;
		
		if(maxPowers>0) {
			float wheel = Mouse.getDWheel();
			if(wheel > 0)
				power = (power+1)%maxPowers;
			if(wheel < 0)
				power = (power-1+maxPowers)%maxPowers;
		}
		
		powerButtons[power] = (Mouse.isButtonDown(1));
		
		cursorPosition.x = Mouse.getX();
		cursorPosition.y = Frame.SCREEN_SIZE[1] - Mouse.getY();

		pausing = (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE));
	}

	@Override
	public boolean isSelecting() {
		return Mouse.isButtonDown(0);
	}
	
	public int getPower() {
		return power;
	}
	
	public void setMaxPowers(int maxPowers) {
		this.maxPowers = maxPowers;
	}

	@Override
	public boolean isPausing() {
		return pausing;
	}
}
