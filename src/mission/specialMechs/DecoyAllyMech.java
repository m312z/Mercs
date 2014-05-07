package mission.specialMechs;

import java.util.List;

import mission.Board;
import mission.gameobject.AllyMech;
import mission.gameobject.Component;
import mission.gameobject.Player;
import mission.weapon.bullet.Bullet;
import phys.Point2D;

public class DecoyAllyMech extends AllyMech {

	public DecoyAllyMech(Player master, List<Component> components,
			Point2D pos, Point2D vel, float health, float speed) {
		super(master, components, pos, vel, health, speed);
	}
	
	/**
	 * Damage the Mech and spawn bullets
	 * @param amount	the amount of damage
	 */
	@Override
	public void damage(float amount, Component component,  Board board) {
		amount = damageShield(amount, component, board);
		if(amount==0)
			return;
		if(!component.isIndestructable())
			damageHull(amount, component, board);
		
		// reflection
		Bullet bullet = components.get(0).getWeapon().getBullet();
		for(int i=0;i<6;i++) {
			Bullet b = bullet.clone();
			b.getPos().x = pos.x;
			b.getPos().y = pos.y;
			b.getDirection().x = (float) Math.cos(Math.toRadians(i*60 + board.getTime()));
			b.getDirection().y = (float) Math.sin(Math.toRadians(i*60 + board.getTime()));
			board.addBullet(b);
		}
	}

}
