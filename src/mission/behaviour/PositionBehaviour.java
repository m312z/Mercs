package mission.behaviour;

import mission.Board;
import mission.gameobject.Mech;
import phys.Point2D;

public class PositionBehaviour implements MechBehaviour {

	Point2D position;
	boolean arrived = false;
	
	public PositionBehaviour(Point2D position) {
		this.position = position;
	}

	@Override
	public void tick(float dt, Board board, Mech mech) {
		if((position.x - mech.getPos().x)*(position.x - mech.getPos().x)
				+ (position.y - mech.getPos().y)*(position.y - mech.getPos().y)
				< 5) {
			arrived = true;
		}
		
		if(arrived) {
			// drift down
			mech.getDirection().x = 0;
			mech.getDirection().y = 1;
			mech.setSpeed(0);
			
			// shoot cycle
			mech.getAttackCycle().tick(dt, board);
		} else {
			// move into position
			mech.getDirection().x = position.x - mech.getPos().x;
			mech.getDirection().y = position.y - mech.getPos().y;
			mech.setSpeed(Mech.MECH_SPEED);
		}
	}

}
