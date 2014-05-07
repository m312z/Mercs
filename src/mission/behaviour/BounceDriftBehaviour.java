package mission.behaviour;

import mission.Board;
import mission.gameobject.Component;
import mission.gameobject.Mech;
import phys.Point2D;

public class BounceDriftBehaviour implements MechBehaviour {

	Point2D startingDirection;
	boolean started = false;
	float totalAngle = 0;
	float speed;
	
	public BounceDriftBehaviour(Point2D dir, float speed) {
		this.startingDirection = dir;
		this.speed = speed;
	}

	@Override
	public void tick(float dt, Board board, Mech mech) {
		
		totalAngle = (totalAngle+dt)%360;
		for(Component c: mech.getComponents()) {
			c.getShape().rotate(dt);
			c.getPos().rotate(dt);
		}
		
		// shoot cycle
		mech.getAttackCycle().tick(dt, board);

		if(!started) {
			started = true;
			mech.getDirection().x = startingDirection.x;
			mech.getDirection().y = startingDirection.y;
			mech.setSpeed(speed);
		} else {
			if((mech.getMaxX()+mech.getPos().x > Board.BOARD_SIZE && mech.getDirection().x > 0)
				|| (mech.getMinX()+mech.getPos().x <0 && mech.getDirection().x < 0))
				mech.getDirection().x = mech.getDirection().x*-1;
			if((mech.getMaxY()+mech.getPos().y > Board.BOARD_SIZE && mech.getDirection().y > 0)
				|| (mech.getMinY()+mech.getPos().y < 0 && mech.getDirection().y < 0))
				mech.getDirection().y = mech.getDirection().y*-1;
		}
	}
	
	public float getTotalAngle() {
		return totalAngle;
	}
}
