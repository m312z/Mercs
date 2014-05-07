package mission.map.event;

import java.util.ArrayList;
import java.util.List;

import mission.Board;
import mission.gameobject.Base;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;
import mission.map.LevelMap;
import mission.weapon.bullet.Bullet;

public class BaseSpawnEvent implements Event {

	Base origin;
	List<Bullet> bullets;
	List<Enemy> enemies;
	List<Mech> mechs;

	public BaseSpawnEvent() {
		bullets = new ArrayList<Bullet>();
		enemies = new ArrayList<Enemy>();
		mechs = new ArrayList<Mech>();
	}
	
	public void setOrigin(Base origin) {
		this.origin = origin;
	}
	
	public void addMech(Mech o) {
		mechs.add(o);
	}
	
	public void addBullet(Bullet b) {
		bullets.add(b);
	}
	
	public void addEnemy(Enemy e) {
		enemies.add(e);
	}
	
	@Override
	public void apply(Board board, LevelMap map) {
		for(Bullet b: bullets) {
			b.getPos().x += origin.getPos().x;
			b.getPos().y += origin.getPos().y;
			board.addBullet(b);
		}
		for(Enemy e: enemies) {
			e.getPos().x += origin.getPos().x;
			e.getPos().y += origin.getPos().y;
			board.addEnemy(e);
		}
		for(Mech m: mechs) {
			m.getPos().x += origin.getPos().x;
			m.getPos().y += origin.getPos().y;
			board.addMech(m);
		}
	}
}
