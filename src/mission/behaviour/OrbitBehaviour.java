package mission.behaviour;

import mission.Board;
import mission.gameobject.Mech;
import phys.Point2D;

public class OrbitBehaviour implements MechBehaviour {

	float angle;
	float distance;
	float speed;
	Point2D target;
	
	public OrbitBehaviour(Point2D target, float speed, float distance, float angle) {
		this.target = target;
		this.speed = speed;
		this.distance = distance;
		this.angle = angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}
	
	@Override
	public void tick(float dt, Board board, Mech mech) {
		
		// shoot cycle
		mech.getAttackCycle().tick(dt, board);
			
		angle = (angle+dt*speed)%360;
		mech.getPos().x = target.x + (float)Math.cos(Math.toRadians(angle))*distance;
		mech.getPos().y = target.y + (float)Math.sin(Math.toRadians(angle))*distance;
		
		if((mech.getMaxX()+mech.getPos().x > Board.BOARD_SIZE && mech.getDirection().x > 0)
			|| (mech.getMinX()+mech.getPos().x <0 && mech.getDirection().x < 0))
			mech.getDirection().x = mech.getDirection().x*-1;
		if((mech.getMaxY()+mech.getPos().y > Board.BOARD_SIZE && mech.getDirection().y > 0)
			|| (mech.getMinY()+mech.getPos().y < 0 && mech.getDirection().y < 0))
			mech.getDirection().y = mech.getDirection().y*-1;
	}

}
