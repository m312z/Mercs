package mission.behaviour;

import java.util.Iterator;

import mission.Board;
import mission.EnemyFactory;
import mission.EnemyFactory.EnemyType;
import mission.gameobject.Base;
import mission.gameobject.Component;
import mission.gameobject.Mech;
import phys.Point2D;

public class BuilderBounceDriftBehaviour extends BounceDriftBehaviour {

	int buildercount;
	
	public BuilderBounceDriftBehaviour(Point2D dir, float speed, int buildercount) {
		super(dir,speed);
		this.buildercount = buildercount;
	}

	@Override
	public void tick(float dt, Board board, Mech mech) {
		
		super.tick(dt,board,mech);
		
		Iterator<Component> cit = mech.getComponents().iterator();
		while(cit.hasNext()) {
			Component c = cit.next();
			if(c.isDestroyed()) {
				// remove component
				cit.remove();
				mech.getAttackCycle().removeComponent(c);
				mech.getAttackCycle().removeEmptyVolleys();
				
				// add base
				Base b = new Base(c.getShape(), new Point2D(
						mech.getPos().x + c.getPos().x,
						mech.getPos().y + c.getPos().y));
				board.addBase(b);
				
				// builder with replacement
				if(buildercount > 0) {
					board.addEnemy(EnemyFactory.makeBasicEnemy(EnemyType.BUILDER_ARMED));
					buildercount--;
				}
			}
		}
	}
}
