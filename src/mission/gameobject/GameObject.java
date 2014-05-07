package mission.gameobject;

import mission.Board;
import phys.Point2D;

public abstract class GameObject {

	protected Point2D pos;

	public GameObject(Point2D pos) {
		this.pos = pos;
	}

	public Point2D getPos() {
		return pos;
	}
	
	public abstract void tick(float dt, Board board);
	public abstract boolean isDead();

}
