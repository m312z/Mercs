package mission.behaviour;

import java.util.List;

import mission.Board;
import mission.gameobject.Mech;
import phys.Point2D;

public class RepeatingStaticBehaviour implements MechBehaviour {

	List<Point2D> positions;
	boolean arrived = false;
	int current_pos;
	float waitTime;
	float timer;
	
	public RepeatingStaticBehaviour(List<Point2D> position, float waitTime) {
		this.positions = position;
		this.waitTime = waitTime;
		timer = 0;
	}

	@Override
	public void tick(float dt, Board board, Mech mech) {
				
		if(arrived) {
			
			timer += dt;
			
			if(timer > waitTime && waitTime > 0) {
				
				// next position
				arrived = false;
				current_pos++;
				timer = 0;
				
				// move out
				if(current_pos >= positions.size())
					mech.setSpeed(Mech.MECH_SPEED);
				
			} else {
				
				// shoot cycle
				mech.setSpeed(0);
				mech.getAttackCycle().tick(dt, board);
			}
		} else {
						
			// move into position
			mech.getDirection().x = positions.get(current_pos).x - mech.getPos().x;
			mech.getDirection().y = positions.get(current_pos).y - mech.getPos().y;
			mech.setSpeed(Mech.MECH_SPEED);
			
			if(Point2D.distance(positions.get(current_pos),mech.getPos())< 5)
				arrived = true;
		}
	}
}