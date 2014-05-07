package mission.behaviour;

import mission.Board;
import mission.gameobject.Mech;
import phys.Point2D;

public class StaticPositionBehaviour implements MechBehaviour {

	Point2D position;
	boolean arrived = false;
	float waitTime;
	float timer;
	
	public StaticPositionBehaviour(Point2D position, float waitTime) {
		this.position = position;
		this.waitTime = waitTime;
		timer = 0;
	}

	@Override
	public void tick(float dt, Board board, Mech mech) {
		
		if((position.x - mech.getPos().x)*(position.x - mech.getPos().x)
				+ (position.y - mech.getPos().y)*(position.y - mech.getPos().y)
				< 5) {
			arrived = true;
		}
		
		if(arrived) {
			timer += dt;
			if(timer > waitTime && waitTime > 0) {
				// move out
				mech.setSpeed(Mech.MECH_SPEED);
			} else {
				// shoot cycle
				mech.setSpeed(0);
				mech.getAttackCycle().tick(dt, board);
			}
		} else {
			// move into position
			mech.getDirection().x = position.x - mech.getPos().x;
			mech.getDirection().y = position.y - mech.getPos().y;
			mech.setSpeed(Mech.MECH_SPEED);
		}
	}

}
