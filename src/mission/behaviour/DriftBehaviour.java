package mission.behaviour;

import mission.Board;
import mission.gameobject.Mech;
import phys.Point2D;

public class DriftBehaviour implements MechBehaviour {

	Point2D direction;
	float speed;
	
	public DriftBehaviour(Point2D direction, float speed) {
		this.direction = direction;
		this.speed = speed;
	}

	@Override
	public void tick(float dt, Board board, Mech mech) {

		// shoot cycle
		mech.getAttackCycle().tick(dt, board);
		
		// move
		mech.getDirection().x = direction.x;
		mech.getDirection().y = direction.y;
		mech.setSpeed(speed);
	}
}
