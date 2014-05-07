package mission.gameobject;

import mission.Board;
import phys.Point2D;

public abstract class MoveableObject extends GameObject {

	/* offset for collision testing */
	protected static Point2D collisionOffset = new Point2D(0,0);
	
	/* moving object attributes */
	protected Point2D direction;
	protected float speed;
	protected boolean dead = false;
	protected boolean bounded = false;
		
	public MoveableObject(Point2D pos, Point2D vel, float speed) {
		super(pos);
		this.pos = pos;
		this.direction = vel;
		this.speed = speed;
	}

	public Point2D getDirection() {
		return direction;
	}
	
	public boolean isDead() {
		return dead;
	}
	
	public void setDead(boolean dead) {
		this.dead = dead;
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	public float getSpeed() {
		return speed;
	}
		
	public void tick(float dt, Board board) {
		move(dt);
		concreteTick(dt,board);
	}

	protected void move(float dt) {
		// move
		float mag = Point2D.magnitude(direction);
		if(mag>0) {
			pos.x += dt*speed*direction.x/mag;
			pos.y += dt*speed*direction.y/mag;
		}
		
		// stay in bounds
		if(bounded) {
			if(pos.x < 0) pos.x = 0;
			else if(pos.x > Board.BOARD_SIZE)
				pos.x = Board.BOARD_SIZE;

			if(pos.y < 0) pos.y = 0;
			if(pos.y > Board.BOARD_SIZE)
				pos.y = Board.BOARD_SIZE;
		}
	}
	
	protected abstract void concreteTick(float dt, Board board);
}
