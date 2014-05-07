package mission.gameobject;

import java.util.List;

import mission.Board;
import mission.behaviour.MechBehaviour;
import phys.Point2D;
import phys.Shape;

public class Enemy extends Mech {

	boolean onScreen = false;
	boolean destroyOffScreen = true;
	
	MechBehaviour behaviour;
	
	public Enemy(List<Component> components, Point2D pos, Point2D vel, float health, float speed) {
		super(components, pos, vel, health, speed);
	}

	public MechBehaviour getBehaviour() {
		return behaviour;
	}
	
	public void setBehaviour(MechBehaviour behaviour) {
		this.behaviour = behaviour;
	}
	
	@Override
	protected void makeDecisions(float dt, Board board) {
		
		behaviour.tick(dt, board, this);
		
		// collision with player
		for(Player p: board.getPlayers()) {
			for(Component c: components) {
				for(Component pcomp: p.getComponents()) {
					collisionOffset.x = (p.getPos().x+pcomp.getPos().x) - (pos.x+c.getPos().x);
					collisionOffset.y = (p.getPos().y+pcomp.getPos().y) - (pos.y+c.getPos().y);
					Point2D mtv = Shape.collide(c.getShape(),pcomp.getShape(),collisionOffset);
					if(mtv!=Point2D.nullVector) {
						p.damage(components.size(),pcomp,board);
						if(components.size()<2 && !base)
							damage(1,components.get(0),board);
						break;
					}
				}
			}
		}
		
		shooting = false;
		currentComponents = attackCycle.getCurrentComponents();
		if(attackCycle.isShooting()) {
			for(Component c: attackCycle.getCurrentComponents()) {
				if(!c.isDestroyed() && c.getWeapon()!=null)
					shooting  = true;
			}
		} else if(attackCycle.isPoweringUp()) {
			for(Component c: attackCycle.getCurrentComponents()) {
				if(!c.isDestroyed() && c.getWeapon()!=null) {
					switch(c.getWeapon().getShotType()) {
					case ASTEROID:
						board.setNoticeAsteroid(attackCycle.getRemainingPowerupTime());
					default:
						break;
					}
				}
			}
		}
		
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

	public boolean isOnScreen() {
		return onScreen;
	}
	
	public void setDestroyOffScreen(boolean destroyOffScreen) {
		this.destroyOffScreen = destroyOffScreen;
	}
}
