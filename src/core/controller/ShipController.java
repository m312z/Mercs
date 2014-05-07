package core.controller;

import phys.Point2D;

public abstract class ShipController {

	String player;
	int playerID;

	protected Point2D cursorPosition;
	protected Point2D direction;
	protected boolean shooting;
	protected boolean[] powerButtons; 
	
	public ShipController(String player, int playerID) {
		this.player = player;
		this.playerID = playerID;
		direction = new Point2D();
		cursorPosition = new Point2D();
		powerButtons = new boolean[4];
	}

	public String getPlayer() {
		return player;
	}
	
	public int getPlayerID() {
		return playerID;
	}
	
	public void setPlayer(String player) {
		this.player = player;
	}
	
	public abstract void pollInput();

	public Point2D getDirection() {
		return direction;
	}

	public boolean isShooting() {
		return shooting;
	}
		
	public boolean[] getPowerButtons() {
		return powerButtons;
	}
	
	public Point2D getCursorPosition() {
		return cursorPosition;
	}
	
	protected float clampCursor(float value, float min, float max) {
		if(value>max) return max;
		if(value<min) return min;
		return value;
	}
	
	public abstract boolean isSelecting();
	public abstract boolean isPausing();
}
