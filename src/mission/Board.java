package mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import mission.effects.ComponentImage;
import mission.effects.EnemyLaser;
import mission.effects.Laser;
import mission.gameobject.AllyMech;
import mission.gameobject.Asteroid;
import mission.gameobject.Base;
import mission.gameobject.Enemy;
import mission.gameobject.GameObject;
import mission.gameobject.Mech;
import mission.gameobject.Player;
import mission.map.LevelMap;
import mission.map.LevelMap.LevelType;
import mission.powers.Power;
import mission.weapon.bullet.Bullet;
import monitor.StatCounter;
import phys.DefaultShapes;
import phys.Point2D;
import phys.Shape;


/**
 * The main model.
 * @author Michael Cashmore
 *
 */
public class Board {
	
	/* size of the board in terms of game size units */
	public static float BOARD_SIZE = 100;
	public static float BOARD_DIAGONAL = (float) Math.sqrt(2*BOARD_SIZE*BOARD_SIZE);
	public static final float NUDGE = 0.01f;
	public static final float BOARD_SPEED = Mech.MECH_SPEED/4;
		
	/* time-stamp */
	float time;
	/* particle timer */
	float particleTimer = 0f;
	float particleDelay = 5f;
	boolean particleTick = false;
	float timeScale = 1;
	/* accumulated score */
	int score;
	int lives = 1;
	/* notices */
	float noticeAsteroid = 0;
	
	/* reference (including the dead) */
	List<Player> totalPlayers;
	
	/* game objects */
	List<Bullet> enemyBullets;
	List<Bullet> friendlyBullets;
	List<Player> players;
	List<Enemy> enemies;
	List<Mech> mechs;
	List<Base> base;
	List<Mech> nonCompetingMechs;
	List<Asteroid> asteroids;
	List<Asteroid> addAsteroids;
	List<Bullet> addEnemyBullets;
	List<Bullet> addFriendlyBullets;
	List<Enemy> addEnemies;
	List<Mech> addMechs;
	List<Mech> addNonCompetingMechs;
	List<Base> addBase;
	Queue<Player> deadPlayers;
	List<ComponentImage> images;
	Map<Player,Float> respawnQueue;
	Map<Mech,Float> mechRespawnQueue;
	
	Set<Laser> lasers;
	List<EnemyLaser> enemyLasers;
	
	/* level */
	LevelMap map;
	
	/* stats */
	StatCounter counter;
	
	public Board(LevelType levelType) {
		
		totalPlayers = new ArrayList<Player>();
		
		enemyBullets = new ArrayList<Bullet>();
		friendlyBullets = new ArrayList<Bullet>();
		players = new ArrayList<Player>();
		asteroids = new ArrayList<Asteroid>();
		enemies = new ArrayList<Enemy>();
		mechs = new ArrayList<Mech>();
		base = new ArrayList<Base>();
		nonCompetingMechs = new ArrayList<Mech>();
		
		addAsteroids = new ArrayList<Asteroid>();
		addEnemyBullets = new ArrayList<Bullet>();
		addFriendlyBullets = new ArrayList<Bullet>();
		addEnemies = new ArrayList<Enemy>();
		addMechs = new ArrayList<Mech>();
		addNonCompetingMechs = new ArrayList<Mech>();
		addBase = new ArrayList<Base>();
		deadPlayers = new LinkedList<Player>();
		respawnQueue = new HashMap<Player,Float>();
		mechRespawnQueue = new HashMap<Mech,Float>();
		enemyLasers = new ArrayList<EnemyLaser>();
		lasers = new HashSet<Laser>();
		images = new ArrayList<ComponentImage>(1);
		
		map = new LevelMap(levelType);
		counter = new StatCounter();
	}
	
	public void addComponentImage(ComponentImage c) {
		images.add(c);
	}
	
	public List<Player> getTotalPlayers() {
		return totalPlayers;
	}
	
	public Queue<Player> getDeadPlayers() {
		return deadPlayers;
	}

	public List<Mech> getMechs() {
		return mechs;
	}
	
	public List<ComponentImage> getImages() {
		return images;
	}
	
	public List<Mech> getNonCompetingMechs() {
		return nonCompetingMechs;
	}
	
	public List<Bullet> getEnemyBullets() {
		return enemyBullets;
	}
	
	public List<Bullet> getFriendlyBullets() {
		return friendlyBullets;
	}
		
	public List<Player> getPlayers() {
		return players;
	}
	
	public List<Asteroid> getAsteroids() {
		return asteroids;
	}
	
	public List<EnemyLaser> getEnemyLasers() {
		return enemyLasers;
	}
	
	public List<Enemy> getEnemies() {
		return enemies;
	}
	
	public float getNoticeAsteroid() {
		return noticeAsteroid;
	}
	
	public void setNoticeAsteroid(float noticeAsteroid) {
		this.noticeAsteroid = noticeAsteroid;
	}
	
	public Map<Player, Float> getRespawnQueue() {
		return respawnQueue;
	}
	
	public Map<Mech, Float> getMechRespawnQueue() {
		return mechRespawnQueue;
	}
	
	public Set<Laser> getLasers() {
		return lasers;
	}
	
	public List<Base> getBase() {
		return base;
	}
	
	public void addBase(Base b) {
		addBase.add(b);
	}
	
	public void addPlayer(Player p, float spawnProtection) {
		if(!totalPlayers.contains(p)) totalPlayers.add(p);
		respawnQueue.put(p,spawnProtection);
	}
	
	public void addMech(Mech o) {
		addMechs.add(o);
	}
	
	public void addMech(Mech o, float spawnProtection) {
		mechRespawnQueue.put(o,spawnProtection);
	}
	
	public void addNonCompetingMech(Mech o) {
		addNonCompetingMechs.add(o);
	}
	
	public void addAsteroid(Asteroid a) {
		addAsteroids.add(a);
	}
	
	public void addBullet(Bullet b) {
		if(b.isEnemyBullet())
			addEnemyBullets.add(b);
		else if(friendlyBullets.size() < 1024*4) {
			addFriendlyBullets.add(b);
			counter.addBulletsFired(b.getParent().getId(),1);
		}
	}
	
	public void addEnemy(Enemy e) {
		addEnemies.add(e);
	}
	
	public void addLaser(Laser laser) {
		lasers.add(laser);
	}
		
	public int getScore() {
		return score;
	}
	
	public void addScore(int score) {
		this.score += score*map.getLevel();
		counter.addScore(score*map.getLevel());
	}
	
	public int getLives() {
		return lives;
	}
	
	public float getTime() {
		return time;
	}
	
	public void setTime(float time) {
		this.time = time;
	}
	
	public LevelMap getMap() {
		return map;
	}
	
	public boolean isParticleTick() {
		return particleTick;
	}
	
	public void setTimeScale(float timeScale) {
		this.timeScale = timeScale;
	}
	
	public float getTimeScale() {
		return timeScale;
	}
	
	public StatCounter getCounter() {
		return counter;
	}
	
	public void setLives(int lives) {
		this.lives = lives;
	}
	
	/*-------------*/
	/* main update */
	/*-------------*/
	
	/**
	 * @return true if the game is to continue, false if it is to be paused.
	 */
	public boolean tick(float dt) {

		dt = dt*timeScale;
		
		time += dt;
		counter.addTime(dt);
		
		// notices
		if(noticeAsteroid>0)
			noticeAsteroid-=dt;
		
		// images
		Iterator<ComponentImage> ciit = images.iterator();
		while(ciit.hasNext()) {
			ComponentImage image = ciit.next();
			image.tick(dt);
			if(image.isDead())
				ciit.remove();
		}
		
		// particles
		particleTimer += dt;
		particleTick = particleTimer > particleDelay;
		particleTimer = particleTimer % particleDelay;
		ParticleManager.tick(this, dt);
		
		// map
		boolean bossClear = map.tick(dt, this);
		
		// base
		base.addAll(addBase);
		addBase.clear();
		
		Iterator<Base> bait = base.iterator();
		while(bait.hasNext()) {
			Base b = bait.next();
			b.tick(dt, this);
			if(b.isDead())
				bait.remove();
		}
		
		// lasers
		Iterator<Laser> lit = lasers.iterator();
		while(lit.hasNext()) {
			Laser g = lit.next();
			g.timer += dt;
			if(g.timer>g.duration)
				lit.remove();
		}
		
		Iterator<EnemyLaser> elit = enemyLasers.iterator();
		while(elit.hasNext()) {
			EnemyLaser g = elit.next();
			g.collide(dt, this);
			g.timer += dt;
			if(g.timer>g.duration)
				elit.remove();
		}
		
		// enemy bullets
		enemyBullets.addAll(addEnemyBullets);
		addEnemyBullets.clear();
		
		Iterator<Bullet> buit = enemyBullets.iterator();
		while(buit.hasNext()) {
			Bullet b = buit.next();
			if(b.isDead())
				buit.remove();
			else
				b.tick(dt,this);
		}
		
		// friendly bullets
		friendlyBullets.addAll(addFriendlyBullets);
		addFriendlyBullets.clear();
		
		buit = friendlyBullets.iterator();
		while(buit.hasNext()) {
			Bullet b = buit.next();
			if(b.isDead())
				buit.remove();
			else
				b.tick(dt,this);
		}
		
		// asteroids
		asteroids.addAll(addAsteroids);
		addAsteroids.clear();
		
		Iterator<Asteroid> ait = asteroids.iterator();
		while(ait.hasNext()) {
			Asteroid a = ait.next();
			if(a.isDead())
				ait.remove();
			else
				a.tick(dt,this);
		}
		
		// spawn asteroids on hive if not a base wave
		if(map.getLevelType()==LevelType.THEHIVE && asteroids.size()<2 && map.getLevel()%5!=0) {
			for(int i=0;i<4;i++) {
				int l = (int) (Math.random()*6);
				asteroids.add(new Asteroid(Shape.scale(DefaultShapes.basicHex, 2*(l+1)),
						new Point2D((float) (BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS*(l+1)),
						new Point2D((float) (Math.random()-0.5),1f),
						(float) (Math.random()*0.2+0.2),l));
			}
		}
		
		// enemies
		enemies.addAll(addEnemies);
		addEnemies.clear();
		
		Iterator<Enemy> eit = enemies.iterator();
		while(eit.hasNext()) {
			Mech g = eit.next();
			g.tick(dt,this);
			if(g.isDead())
				eit.remove();
		}
				
		// mechs
		mechs.addAll(addMechs);
		addMechs.clear();
		
		Iterator<Mech> git = mechs.iterator();
		while(git.hasNext()) {
			GameObject g = git.next();
			g.tick(dt,this);
			if(g.isDead())
				git.remove();
		}
		
		nonCompetingMechs.addAll(addNonCompetingMechs);
		addNonCompetingMechs.clear();
		
		git = nonCompetingMechs.iterator();
		while(git.hasNext()) {
			GameObject g = git.next();
			g.tick(dt,this);
			if(g.isDead())
				git.remove();
		}
		
		// players
		Iterator<Player> pit = players.iterator();
		while(pit.hasNext()) {
			Player g = pit.next();
			g.tick(dt/timeScale,this);
			if(g.isDead()) {
				pit.remove();
				g.reset(this);
				if(lives>0) {
					lives--;
					respawnQueue.put(g,250f);
				} else {
					deadPlayers.add(g);
				}
			}
		}
		
		// dead players
		while(!deadPlayers.isEmpty() && lives>0) {
			Player p = deadPlayers.poll();
			respawnQueue.put(p,300f);
			lives--;
		}

		// respawns
		Iterator<Player> rpit = respawnQueue.keySet().iterator();
		while(rpit.hasNext()) {
			Player g = rpit.next();
			g.tick(dt/timeScale,this);
			float time = respawnQueue.get(g)-dt;
			if(time<=0) {
				rpit.remove();
				players.add(g);
				// support satellites
				if(g.getPowerEater().hasPower(Power.SUPPORT_GADGETS)) {
					for(AllyMech d: g.getPowerEater().getGadgets())
						addMech(d);
				}
			} else {
				respawnQueue.put(g,time);
			}
		}
		
		Iterator<Mech> rmit = mechRespawnQueue.keySet().iterator();
		while(rmit.hasNext()) {
			Mech g = rmit.next();
			g.tick(dt,this);
			float time = mechRespawnQueue.get(g)-dt;
			if(g.isDead())
				rmit.remove();
			else if(time<=0) {
				rmit.remove();
				mechs.add(g);
			} else {
				mechRespawnQueue.put(g,time);
			}
		}
		
		return (players.size()==0 && respawnQueue.size()==0) || (map.getEvents().size()==0 && bossClear);
	}
}
