package mission.gameobject;

import java.util.List;

import mission.Board;
import phys.Point2D;

public class AllyMech extends Enemy {

	Player master;
	float lifeTime = -1;
	
	public AllyMech(Player master, List<Component> components, Point2D pos, Point2D vel, float health, float speed) {
		super(components, pos, vel, health, speed);
		id = master.getId();
		this.master = master;
	}
	
	public void setMaster(Player master) {
		this.master = master;
	}
	
	public Player getMaster() {
		return master;
	}
	
	public void setLifeTime(float lifeTime) {
		this.lifeTime = lifeTime;
	}
	
	@Override
	protected void makeDecisions(float dt, Board board) {
		
		behaviour.tick(dt, board, this);
		
		if(lifeTime>=0) {
			lifeTime -= dt;
			if(lifeTime<=0)
				dead=true;
		}
		
		shooting = false;
		currentComponents = attackCycle.getCurrentComponents();
		if(attackCycle.isShooting()) {
			for(Component c: attackCycle.getCurrentComponents()) {
				if(!c.isDestroyed() && c.getWeapon()!=null)
					shooting  = true;
			}
		} else if(attackCycle.isPoweringUp()) {
			// nothing yet
		}
		
		behaviour.tick(dt, board, this);
		
		// board destroy
		if(onScreen) {
			if(destroyOffScreen &&
				((pos.x + getMaxX() < 0) || (pos.x + getMinX() > Board.BOARD_SIZE)
				|| (pos.y + getMinY() > Board.BOARD_SIZE) || (pos.y + getMaxY() < 0)))
					dead = true;
		} else {
			if((pos.x + getMaxX() > 0) && (pos.x + getMinX() < Board.BOARD_SIZE)
				&& (pos.y + getMaxY() > 0) && (pos.y + getMinY() < Board.BOARD_SIZE))
			onScreen = true;
		}
	}

}
