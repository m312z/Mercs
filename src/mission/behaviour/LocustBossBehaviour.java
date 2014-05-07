package mission.behaviour;

import java.util.Iterator;

import mission.Board;
import mission.gameobject.Component;
import mission.gameobject.Mech;
import phys.Point2D;

public class LocustBossBehaviour implements MechBehaviour {

	Point2D position;
	boolean arrived = false;
	boolean spin = false;
	float totalAngle = 0;
	float rt;
	
	public LocustBossBehaviour(Point2D position) {
		this.position = position;
	}

	@Override
	public void tick(float dt, Board board, Mech mech) {
		
		if((position.x - mech.getPos().x)*(position.x - mech.getPos().x)
			+ (position.y - mech.getPos().y)*(position.y - mech.getPos().y)
				< 5) {
			arrived = true;
		}
		
		// remove destroyed components
		Iterator<Component> cit = mech.getComponents().iterator();
		while(cit.hasNext()) {
			Component c = cit.next();
			if(c.isDestroyed())
				cit.remove();
		}
		
		if(arrived) {
			
			// attack and spin cycle
			mech.setSpeed(0);
			if(mech.getAttackCycle().isShooting()) {
				
				// shoot
				mech.getAttackCycle().tick(dt, board);
				spin = true;
				
			} else if(spin) {
				
				// rotate
				rt = dt*1.5f;
				if(totalAngle+rt>90) {
					rt = rt - (totalAngle+rt)%90;
					spin = false;
				}
				totalAngle = (totalAngle+rt)%90;
				for(Component c: mech.getComponents()) {
					c.getShape().rotate(-rt);
					c.getPos().rotate(-rt);
				}
				
			} else {
				
				// power-up
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
