package mission.map.event;

import java.util.ArrayList;
import java.util.List;

import mission.Board;
import mission.gameobject.Asteroid;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;
import mission.map.LevelMap;
import mission.weapon.bullet.Bullet;

public class SpawnEvent implements Event{

	List<Bullet> bullets;
	List<Enemy> enemies;
	List<Asteroid> asteroids;
	List<Mech> mechs;

	public SpawnEvent() {
		bullets = new ArrayList<Bullet>();
		asteroids = new ArrayList<Asteroid>();
		enemies = new ArrayList<Enemy>();
		mechs = new ArrayList<Mech>();
	}
	
	public void addMech(Mech o) {
		mechs.add(o);
	}
		
	public void addAsteroid(Asteroid a) {
		asteroids.add(a);
	}
	
	public void addBullet(Bullet b) {
		bullets.add(b);
	}
	
	public void addEnemy(Enemy e) {
		enemies.add(e);
	}
	
	@Override
	public void apply(Board board, LevelMap map) {
		for(Asteroid a: asteroids)
			board.addAsteroid(a);
		for(Bullet b: bullets)
			board.addBullet(b);
		for(Enemy e: enemies)
			board.addEnemy(e);
		for(Mech m: mechs)
			board.addMech(m);
	}
}
