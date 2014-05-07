package mission.weapon;

import mission.gameobject.Mech;
import mission.weapon.Weapon.ShotType;
import mission.weapon.bullet.Bullet;
import phys.DefaultShapes;
import phys.Point2D;
import phys.Shape;

public class WeaponFactory {
	
	public static Weapon makeWeapon(Mech mech, ShotType shot) {
		Weapon weapon;
		Bullet b = new Bullet(mech, false, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		switch(shot) {
		case STRAIGHTDUAL:
			weapon = new Weapon(b, 6f, ShotType.STRAIGHTDUAL);
			break;
		case SPREAD:
		default:
			weapon = new Weapon(b, 12f, ShotType.SPREAD);
			break;
		}
		return weapon;
	}
}