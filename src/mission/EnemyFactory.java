package mission;

import gui.GameGUI;

import java.util.ArrayList;
import java.util.List;

import mission.behaviour.BaseBehaviour;
import mission.behaviour.BeeBehaviour;
import mission.behaviour.BounceDriftBehaviour;
import mission.behaviour.BuilderBehaviour;
import mission.behaviour.DriftBehaviour;
import mission.behaviour.DroneMakerBehaviour;
import mission.behaviour.LaserStarBehaviour;
import mission.behaviour.LocustBossBehaviour;
import mission.behaviour.MechAttack;
import mission.behaviour.MechAttack.Volley;
import mission.behaviour.PathBehaviour;
import mission.behaviour.PositionBehaviour;
import mission.behaviour.StaticPositionBehaviour;
import mission.boss.BuilderBaseBoss;
import mission.boss.HoneycombBoss;
import mission.boss.LocustSpawnBoss;
import mission.gameobject.Component;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;
import mission.weapon.Weapon;
import mission.weapon.Weapon.ShotType;
import mission.weapon.bullet.Bullet;
import mission.weapon.bullet.BulletMod;
import phys.DefaultShapes;
import phys.Point2D;
import phys.Shape;

public class EnemyFactory {

	public static final int WAITTIME = 800;
	
	public enum EnemyType
	{
		// HIVE
		TRAVELDRONE,
		DRONE,
		HOMER,
		SPIRAL,
		SPREADER,
		BEAMER,
		HONEYBEE,	

		// BUILDER
		BUILDER_ARMED,
		BUILDER_BASIC,
		BUILDER_LASER_STAR,
		BUILDER_PILLAR,
		DIRECTEDDRONE,
		BUILDER_DRONEMAKER,
		
		// LOCUST
		LOCUST_ARMOURED_DRONE,
		LOCUST_DRONE,
		LOCUST_SPIRAL
	}
	
	/**
	 * Get xCoord of a hex in a grid.
	 * Assumes the size of the hex is scaled to MECH_RADIUS;
	 * the coordinate can be appropriately scaled afterwards.
	 * @param rad	hex ring; central hex r=0
	 * @param i		the clockwise position, starting from 0
	 * @return		the xCoord of the hex
	 */
	public static float getXCoord(int rad, int i) {
		float x = Math.abs(((i+(rad*3)/2f)%(rad*3))-(rad*3)/2f);
		if(x>rad) x=rad;
		if(i>rad*3) x = -x;
		x = x*3*Mech.MECH_RADIUS/2f;
		return x;
	}

	/**
	 * Get yCoord of a hex in a grid.
	 * Assumes the size of the hex is scaled to MECH_RADIUS;
	 * the coordinate can be appropriately scaled afterwards.
	 * @param rad	hex ring; central hex r=0
	 * @param i		the clockwise position, starting from negative
	 * @return		the yCoord of the hex
	 */
	public static float getYCoord(int rad, int i) {
		float y = rad*3 - Math.abs(i-rad*3);
		if(y<rad) y = y - 2*rad;
		else if(y > 2*rad) y = y - rad;
		else y = 2*(y-rad) - rad;
		y = (float) (y*Mech.MECH_RADIUS*Math.sin(Math.toRadians(60)));
		return y;
	}
	
	public static Enemy makeBasicEnemy(EnemyType type) {
		
		switch(type)
		{
		case BEAMER:
			return makeBeamer();
		case SPREADER:
			return makeSpreader();
		case HOMER:
			return makeHomer();
		case SPIRAL:
			return makeSpiral();
		case HONEYBEE:
			return makeHoneybee();
		case BUILDER_ARMED:
			return makeBuilder(true);
		case BUILDER_BASIC:
			return makeBuilder(false);
		case BUILDER_LASER_STAR:
			return makeLaserStar();
		case BUILDER_PILLAR:
			return makePillar(1);
		case TRAVELDRONE:
			return makeTravellingDrone();
		case DIRECTEDDRONE:
			return makeDirectedDrone();
		case BUILDER_DRONEMAKER:
			return makeDroneMaker();
		case LOCUST_ARMOURED_DRONE:
			return makeArmouredStraightShot();
		case LOCUST_DRONE:
			return makeLocustDrone();
		case LOCUST_SPIRAL:
			return makeLocustSpiral();
		case DRONE:
		default:
			return makeDrone();	
		}
	}

	public static Weapon makeBossWeapon(Mech e, ShotType st) {
		
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		float fireDelay = (float) (Math.random()*10);
		
		switch(st) {
		case STRAIGHT:
			st = ShotType.TARGETEDBEAM;
			fireDelay = 3f;
			break;
		case TARGETEDBEAM:
			fireDelay = 3f;
			break;
		case LASER:
			st = ShotType.FASTSPIRAL;
		case FASTSPIRAL:
			fireDelay = 10;
			b.setSpeed(Bullet.BULLET_SPEED/3);
			b.setRange(Bullet.BULLET_RANGE*3);
			break;
		case CLONERAY:
			st = ShotType.SPREAD;
			fireDelay = 2;
			break;
		case IONBEAM:
			st = ShotType.STRAIGHTDUAL;
			fireDelay = 2;
			break;
		case SPREAD:
		case STRAIGHTDUAL:
		case SPIRAL:
			fireDelay = 2;
			break;
		case ASTEROID:
			fireDelay = 120;
			break;
		default:
			break;
		}
		
		Weapon bossweapon = new Weapon(b, fireDelay, st);
		return bossweapon;
	}
	
	private static Volley makeVolley(List<Component> list, Mech e, int weaponPair) {
		
		ShotType st = list.get(weaponPair).getWeapon().getShotType();
		float fireTime = 0;
		float powerupTime = 60;
		switch(st) {
		case ASTEROID:
			 fireTime = ((int)(Math.random()*3)+1)*121;
			 powerupTime = 120;
			break;
		default:
			fireTime = (float) (Math.random()*240.0+120);
			break;
		}
		
		return e.getAttackCycle().new Volley(new Component[] {
				list.get(weaponPair),list.get(weaponPair+1)},powerupTime,fireTime);		
	}
	
	/*----------------*/
	/* SHARED ENEMIES */
	/*----------------*/
	
	/**
	 * @return directed-shot enemy drone with no behaviour 
	 */
	private static Enemy makeTravellingDrone() {
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.enemyLight);
		list.add(c);
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS),
				new Point2D(), 1, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		c.setWeapon(new Weapon(b, 120f, ShotType.TARGETEDBEAM));
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {c},60,120));
		
		return e;
	}
		
	/*------*/
	/* HIVE */
	/*------*/
	
	/**
	 * @return honeyBee enemy with BeeBehaviour
	 */
	private static Enemy makeHoneybee() {
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(HoneycombBoss.honey);
		list.add(c);
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS),
				new Point2D(), 1, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		c.setWeapon(new Weapon(b, 30f, ShotType.LASER));
		c.getWeapon().getLaserTargets().add(new Point2D(0,Mech.MECH_RADIUS*2));
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {c},30,30));
		
		e.setBehaviour(BeeBehaviour.getInstance());

		return e;
	}
	
	/**
	 * @return directed-shot enemy with StaticPositionBehaviour
	 */
	private static Enemy makeBeamer() {
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.enemyLight);
		list.add(c);
		Component ca = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(
						-3f*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(60)),
						Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
		ca.setColour(GameGUI.enemy);
		list.add(ca);
		Component cb = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(
						3f*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(60)),
						Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
		cb.setColour(GameGUI.enemy);
		list.add(cb);
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS),
				new Point2D(), 10, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		c.setWeapon(new Weapon(b, 3f, ShotType.TARGETEDBEAM));
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[0],1,0));
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {c},60,120));
		
		e.setBehaviour(new StaticPositionBehaviour(new Point2D(
				(float)(Math.random()*Board.BOARD_SIZE), 
				(float)(Math.random()*Board.BOARD_SIZE/3f)),WAITTIME));

		return e;
	}
	
	/**
	 * @return straight-dual-shot enemy with StaticPositionBehaviour
	 */
	private static Enemy makeDrone() {
		
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.enemyLight);
		list.add(c);
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS),
				new Point2D(), 1, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		c.setWeapon(new Weapon(b, 30f, ShotType.STRAIGHTDUAL));
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {c},60,120));
		
		e.setBehaviour(new StaticPositionBehaviour(new Point2D(
				(float)(Math.random()*Board.BOARD_SIZE), 
				(float)(Math.random()*Board.BOARD_SIZE/3f)),WAITTIME));

		return e;
	}
	
	/**
	 * @return a list of travelling drones with a curved or straight path
	 */
	public static List<Enemy> makeTrain() {
		
		int p = (int) (Math.random()*12);
		Point2D[] direction = makePath(p);
		
		float x = 0;
		float y = 0;
		float offsetY = 0;
		float offsetX = 0;
		
		switch(p) {
		case 0: case 8: offsetX = 0; x = Board.BOARD_SIZE/4f; break;
		case 1: case 7: offsetX = 0; x = Board.BOARD_SIZE/2f; break;
		case 2: case 6: offsetX = 0; x = Board.BOARD_SIZE*3/4f; break;
		case 3: case 4: case 5: offsetX = 1; x = Board.BOARD_SIZE; break;
		case 9: case 10: case 11: offsetX = -1; x = 0; break;
		}
		
		switch(p) {
		case 11: case 3: offsetY = 0; y = Board.BOARD_SIZE/4f; break;
		case 10: case 4: offsetY = 0; y = Board.BOARD_SIZE/2f; break;
		case 9: case 5: offsetY = 0; y = Board.BOARD_SIZE*3/4f; break;
		case 8: case 7: case 6: offsetY = 1; y = Board.BOARD_SIZE; break;
		case 0: case 1: case 2: offsetY = -1; y = 0; break;
		}
		
		int size = 12;
		
		List<Enemy> list = new ArrayList<Enemy>();
		for(int i=0;i<size;i++) {
			
			Enemy e  = makeTravellingDrone();
			
			// position
			float dx = offsetX*(e.getMaxX()-e.getMinX())*(i+1);
			float dy = offsetY*(e.getMaxY()-e.getMinY())*(i+1);
			e.getPos().x = x + dx;
			e.getPos().y = y + dy;
			
			// behaviour
			PathBehaviour pb = new PathBehaviour(direction);			
			pb.setTurn(-(i+1)*Mech.MECH_RADIUS*2/(240f*pb.getSpeed()));
			e.setBehaviour(pb);
			
			list.add(e);
		}
		return list;
	}

	/**
	 * @param p the chosen path type; one of any combination of two axis-aligned vectors
	 * @return the two direction vectors that comprise the path	
	 */
	private static Point2D[] makePath(int p) {
		Point2D[] direction = new Point2D[2];
		direction[0] = new Point2D(
				Math.abs(p/3 - 1) - 1,
				Math.abs(p/3 - 2) - 1);
		direction[1] = new Point2D(
				((p%2)-1),
				(p%2));
		if(p==0 || p==8 || p==10)
			direction[1].x *= -1;
		if(p==5 || p==7 || p==9)
			direction[1].y *= -1;
		
		return direction;
	}
	
	/**
	 * @return spiral-shot enemy with StaticPositionBehaviour
	 */
	private static Enemy makeSpiral() {
		
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.enemyLight);
		list.add(c);
		Component gunA = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(
						-3f*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(60)),
						Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
		gunA.setColour(GameGUI.enemy);
		list.add(gunA);
		Component gunB = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(
						3f*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(60)),
						Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
		gunB.setColour(GameGUI.enemy);
		list.add(gunB);
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS),
				new Point2D(), 30, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		gunA.setWeapon(new Weapon(b, 1f, ShotType.SPIRAL));
		gunB.setWeapon(new Weapon(b, 1f, ShotType.SPIRAL));
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[0],1,0));
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {},60,120));
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {gunA},60,60));
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {gunB},60,60));	

		float w = e.getMaxX() - e.getMinX();
		e.setBehaviour(new StaticPositionBehaviour(new Point2D(
				(float)(Math.random()*(Board.BOARD_SIZE-w)+w/2f), 
				(float)(Board.BOARD_SIZE/6f + Math.random()*Board.BOARD_SIZE/3f)),
				WAITTIME));
		
		return e;
	}
	
	/**
	 * @return spread-shot enemy with DriftBehaviour
	 */
	private static Enemy makeSpreader() {
		
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.enemyLight);
		list.add(c);
		Component gunA = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(
						-3f*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(60)),
						Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
		gunA.setColour(GameGUI.enemy);
		list.add(gunA);
		Component gunB = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(
						3f*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(60)),
						Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
		gunB.setColour(GameGUI.enemy);
		list.add(gunB);
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS),
				new Point2D(), 30, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		gunA.setWeapon(new Weapon(b, 2f, ShotType.SPREAD));
		gunB.setWeapon(new Weapon(b, 2f, ShotType.SPREAD	));
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {gunA,gunB},120,120));	
		
		float w = e.getMaxX() - e.getMinX();
		e.getPos().x = -w/2;
		e.getPos().y = (float)Math.random()*(Board.BOARD_SIZE/2);
		e.setBehaviour(new DriftBehaviour(new Point2D(1,0),Mech.MECH_SPEED/2));
		
		return e;
	}
	
	/**
	 * @return homing projectile enemy with StaticPositionBehaviour
	 */
	private static Enemy makeHomer() {
		
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.enemyLight);
		list.add(c);
		Component gunC = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(0,-(float)Math.sin(Math.toRadians(60))*Mech.MECH_RADIUS*2), -1);
		gunC.setColour(GameGUI.enemy);
		list.add(gunC);
		Component gunA = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(
						-3f*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(60)),
						-Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
		gunA.setColour(GameGUI.enemy);
		list.add(gunA);
		Component gunB = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(
						3f*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(60)),
						-Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
		gunB.setColour(GameGUI.enemy);
		list.add(gunB);
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS),
				new Point2D(), 30, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		b.getMods().put(BulletMod.HOMING,1);
		b.setSpeed(Bullet.BULLET_SPEED/2f);
		b.setRange(Bullet.BULLET_RANGE*2f);
		gunA.setWeapon(new Weapon(b, 6f, ShotType.FASTSPIRAL));
		gunB.setWeapon(new Weapon(b, 6f, ShotType.FASTSPIRAL));
		gunC.setWeapon(new Weapon(b, 6f, ShotType.FASTSPIRAL));
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[0],1,0));
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {gunA,gunB,gunC},400,40));
		
		float w = e.getMaxX() - e.getMinX();
		e.setBehaviour(new StaticPositionBehaviour(new Point2D(
				(float)(Math.random()*(Board.BOARD_SIZE-w)+w/2f), 
				(float)(Board.BOARD_SIZE/6f + Math.random()*Board.BOARD_SIZE/3f)),
				WAITTIME));
		
		return e;
	}
	
	/** 
	 * @return flower shaped mid-boss with BounceDriftBehaviour
	 */
	public static Enemy makeSmallBoss() {
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.enemyLight);
		list.add(c);
		for(int i=0;i<4;i++) {
			c = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(
						(-1+(i%2)*2)*3f*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(60)),
						(-1+(i/2)*2)*Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
			c.setColour(GameGUI.enemy);
			list.add(c);
		}
		for(int i=0;i<2;i++) {
			c = new Component(
					Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
					new Point2D(
							0,(-1+i*2)*2f*Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
			c.setColour(GameGUI.enemy);
			list.add(c);
		}
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS*2),
				new Point2D(), 30, Mech.MECH_SPEED);

		
		// weapon pairs (1,2), (3,4), and (5,6)
		for(int i=0;i<3;i++) {
			int weaponPair = i*2+1;
			ShotType st = ShotType.values()[(int) (Math.random()*ShotType.values().length-1)+1];
			e.getComponents().get(weaponPair).setWeapon( makeBossWeapon(e, st));
			e.getComponents().get(weaponPair+1).setWeapon( makeBossWeapon(e, st));	
		}
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		for(int i=0;i<3;i++) {
			int weaponPair = i*2+1;
			e.getAttackCycle().addVolley(makeVolley(list, e, weaponPair));
		}
		
		Point2D start = new Point2D(
				(float)(Math.random()*2-1), 
				(float)(0.5f+Math.random()));
		e.setBehaviour(new BounceDriftBehaviour(start,Mech.MECH_SPEED/3f));
		return e;
	}
	
	/** 
	 * @return diamond shaped mid-boss with PositionBehaviour
	 */
	public static Enemy makeMidBoss() {
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.enemyLight);
		list.add(c);
		for(int i=0;i<4;i++) {
			c = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(
						(-1+(i%2)*2)*3f*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(60)),
						(-1+(i/2)*2)*Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
			if(i<2) c.setColour(GameGUI.enemy);
			else c.setColour(GameGUI.enemyDark);
			list.add(c);
		}
		for(int i=0;i<2;i++) {
			c = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(
						(-1+i*2)*6f*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(60)),0), 15);
			c.setColour(GameGUI.enemy);
			list.add(c);
		}
		for(int i=0;i<2;i++) {
			c = new Component(
					Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
					new Point2D(
							0,(-1+i*2)*2f*Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
			if(i<1) c.setColour(GameGUI.enemyLight);
			else c.setColour(GameGUI.enemyDark);
			list.add(c);
		}
		for(int i=0;i<5;i++) {
			c = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(
						((i*3)-6)*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(60)),
						(4-Math.abs(i-2))*Mech.MECH_RADIUS*(float)Math.sin(Math.toRadians(60))), 15);
			c.setColour(GameGUI.enemyDarkest);
			list.add(c);
		}
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS),
				new Point2D(), 40, Mech.MECH_SPEED);
		
		
		// weapon pairs (1,2), (3,4), (5,6) and (7,8)
		for(int i=0;i<4;i++) {
			int weaponPair = i*2+1;
			ShotType st = ShotType.values()[(int) (Math.random()*ShotType.values().length-1)+1];
			e.getComponents().get(weaponPair).setWeapon( makeBossWeapon(e, st));
			e.getComponents().get(weaponPair+1).setWeapon( makeBossWeapon(e, st));	
		}
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[0],1,0));
		for(int i=0;i<4;i++) {
			int weaponPair = i*2+1;
			e.getAttackCycle().addVolley(makeVolley(list, e, weaponPair));
		}
		
		float width = e.getMaxX() - e.getMinX();
		e.setBehaviour(new PositionBehaviour(new Point2D(
				(float)(width/2f+Math.random()*(Board.BOARD_SIZE-width)), 
				(1/3f)*Board.BOARD_SIZE)));
		
		return e;
	}
	
	/*---------*/
	/* BUILDER */
	/*---------*/
	
	/**
	 * @return builder enemy with BuilderBehaviour, and a weapon
	 */
	private static Enemy makeBuilder(boolean weapon) {
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,2*Mech.MECH_RADIUS/3f), new Point2D(), -1);
		c.setColour(BuilderBaseBoss.builderBossColor);
		list.add(c);
				
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS),
				new Point2D(), 20, Mech.MECH_SPEED);

		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {},600,600));
		
		e.setBehaviour(new BuilderBehaviour(weapon));
	
		return e;
	}
	
	/**
	 * @return directed-shot enemy drone with no behaviour 
	 */
	private static Enemy makeDirectedDrone() {
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.enemyLight);
		list.add(c);
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS),
				new Point2D(), 1, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		c.setWeapon(new Weapon(b, 8f, ShotType.TARGETEDBEAM));
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {c},60,60));
		
		return e;
	}

	/**
	 * @return Laser-star enemy with LaserStarBehaviour
	 */
	private static Enemy makeLaserStar() {
		
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component center = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		center.setColour(BuilderBaseBoss.builderBossColor);
		list.add(center);
		
		// make more body
		for(int i=0;i<6;i++) {
			
			float x = getXCoord(1, i);
			float y = getYCoord(1, i);
							
			Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
					new Point2D(x,y), 10);
			c.setColour(BuilderBaseBoss.builderBossColorDark[0]);
			list.add(c);
		}
		
		// make enemy
		float x = ((int)(Math.random()*6)+1)*(Board.BOARD_SIZE/8f);
		Enemy e = new Enemy(list,
				new Point2D(x,-3*Mech.MECH_RADIUS),
				new Point2D(), 30, Mech.MECH_SPEED);
		e.setBase(true);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		center.setWeapon(new Weapon(b, 120f, ShotType.LASER));
		for(int i=0;i<6;i++) {
			center.getWeapon().getLaserTargets().add(new Point2D(
					(float)(Board.BOARD_DIAGONAL*Math.cos(Math.toRadians(i*60))),
					(float)(Board.BOARD_DIAGONAL*Math.sin(Math.toRadians(i*60)))));
		}
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {center},120f,120f));
		float dir = 0.2f * ((int)(2*Math.random())*2-1);
		e.setBehaviour(new LaserStarBehaviour(dir));

		return e;
	}
	
	/**
	 * @return pillar enemy with BaseBehaviour
	 */
	public static Enemy makePillar(int height) {
		
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component center = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		center.setColour(BuilderBaseBoss.builderBossColor);
		list.add(center);
		
		// make more body
		for(int h=1;h<height+1;h++)
		for(int i=0;i<h*3+1;i+=h*3) {
			float x = getXCoord(h, i);
			float y = getYCoord(h, i);
							
			Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(x,y), 10);
			c.setColour(BuilderBaseBoss.builderBossColorDark[0]);
			list.add(c);
		}
		
		// make enemy
		float x = ((int)(Math.random()*6)+1)*(Board.BOARD_SIZE/8f);
		Enemy e = new Enemy(list,
				new Point2D(x,-2*Mech.MECH_RADIUS*height),
				new Point2D(), 30, Mech.MECH_SPEED);
		e.setBase(true);
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.setBehaviour(BaseBehaviour.getInstance());

		return e;
	}
	
	/**
	 * @return DroneMaker enemy with DroneMakerBehaviour
	 */
	private static Enemy makeDroneMaker() {
		
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component center = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		center.setColour(BuilderBaseBoss.builderBossColor);
		list.add(center);
		
		// make more body
		for(int i=0;i<6;i++) {
			float x = getXCoord(1, i);
			float y = getYCoord(1, i);
			Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
					new Point2D(x,y), 10);
			c.setColour(BuilderBaseBoss.builderBossColorDark[0]);
			list.add(c);
		}
		
		// make enemy
		float x = ((int)(Math.random()*6)+1)*(Board.BOARD_SIZE/8f);
		Enemy e = new Enemy(list,
				new Point2D(x,-3*Mech.MECH_RADIUS),
				new Point2D(), 30, Mech.MECH_SPEED);
		e.setBase(true);
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.setBehaviour(new DroneMakerBehaviour());

		return e;
	}
	
	/*--------*/
	/* LOCUST */
	/*--------*/
	
	/**
	 * @param e the mech to carry the weapon.
	 * @return A targeted/gargantua, spiral/homing, spiral,
	 * 			or straight/mega weapon.
	 */
	public static Weapon makeLocustBossWeapon(Mech e) {
		
		ShotType st = ShotType.values()[(int) (Math.random()*ShotType.values().length)];
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		float fireDelay = (float) (Math.random()*10);
		
		switch(st) {
		case STRAIGHT:
		case TARGETEDBEAM:
			st = ShotType.TARGETEDBEAM;
			b.getMods().put(BulletMod.GARGANTUA, 1);
			fireDelay = 40f;
			break;
		case LASER:
		case FASTSPIRAL:
			st = ShotType.FASTSPIRAL;
			fireDelay = 20;
			b.setSpeed(Bullet.BULLET_SPEED/3);
			b.setRange(Bullet.BULLET_RANGE*2);
			b.getMods().put(BulletMod.HOMING, 1);
			break;
		case CLONERAY:
		case IONBEAM:
		case ASTEROID:
		case SPIRAL:
			st = ShotType.SPIRAL;
			fireDelay = 5;
			break;
		case PAUSE:
		case DIRECTEDBEAM:
		case SPREAD:
		default:
			st = ShotType.STRAIGHTDUAL;
		case STRAIGHTDUAL:
			fireDelay = 10;
			b.getMods().put(BulletMod.MEGA, 1);
			break;
		}
		
		Weapon bossweapon = new Weapon(b, fireDelay, st);
		return bossweapon;
	}

	/**
	 * @return Locust mid-boss with vulnerable channels.
	 */
	public static Enemy makeLocustBoss(boolean locust) {
		
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		if(locust) c.setColour(LocustSpawnBoss.locustColor);
		else c.setColour(GameGUI.enemy);
		list.add(c);
		
		Component weaponComps[] = new Component[4];
		int index = 0;
		for(int r=1;r<3;r++) {
		for(int i=0;i<r*6;i++) {
			
			c = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(getXCoord(r, i),getYCoord(r,i)), 25);
			list.add(c);
			
			// make invulnerable parts
			if((i!=0 && i!=r*3)) {
				c.setIndestructable(true);
				c.setColour(GameGUI.wreckage);
			} else {
				c.setShowHealth(true);
				if(locust) c.setColour(LocustSpawnBoss.locustColorDark);
				else c.setColour(GameGUI.enemyDark);
			}
			
			// remember weapon positions
			if(r==2 && (i==2 || i==4 || i==8 || i==10)) {
				c.setShowHealth(false);
				if(locust) c.setColour(LocustSpawnBoss.locustColorDark);
				else c.setColour(GameGUI.enemyDark);
				weaponComps[index] = c;
				index++;
			}
		}};
				
		// make enemy
		Enemy e = new Enemy(list, new Point2D(), new Point2D(), 30, Mech.MECH_SPEED);
		e.getPos().y = -e.getMaxY();

		// weapon pairs (1,2), (2,3), (3,4) and (4,1)
		for(int i=0;i<4;i++) {
			weaponComps[i].setWeapon( makeLocustBossWeapon(e) );
		}
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		for(int i=0;i<4;i++) {
			Component[] w = new Component[2];
			w[0] = weaponComps[(i+1)%weaponComps.length];
			w[1] = weaponComps[(i+2)%weaponComps.length];
			e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(w, 24, 400));
		}
		
		e.setBehaviour(new LocustBossBehaviour(new Point2D(Board.BOARD_SIZE/2,Board.BOARD_SIZE/3)));
		return e;
	}
	
	/**
	 * @return straight-shot armoured enemy with no behaviour
	 */
	private static Enemy makeArmouredStraightShot() {
		
		// body
		List<Component> list = new ArrayList<Component>(1);
		Component head = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		head.setColour(LocustSpawnBoss.locustColor);
		list.add(head);
		
		for(int i=0; i<6; i++) {
			if(i==3) continue;
			Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
			c.getPos().x = getXCoord(1, i);
			c.getPos().y = getYCoord(1, i);
			if(i==2 || i==4) {
				c.setColour(GameGUI.wreckage);
				c.setIndestructable(true);
			} else {
				c.setColour(LocustSpawnBoss.locustColorDark);
				c.setHealth(15);
			}
			list.add(c);
		}
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D(),
				new Point2D(), 60, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		b.getMods().put(BulletMod.MEGA, 1);
		b.getMods().put(BulletMod.CLUSTER, 4);
		head.setWeapon(new Weapon(b, 30f, ShotType.STRAIGHTDUAL));
		
		// behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {head},120,30));	
		
		// starting position
		e.getPos().x = (float)(Board.BOARD_SIZE*Math.random());
		e.getPos().y = -e.getMaxY();
		
		return e;
	}
	
	/**
	 * @return straight-dual-shot enemy with StaticPositionBehaviour
	 */
	private static Enemy makeLocustDrone() {
		
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(LocustSpawnBoss.locustColor);
		list.add(c);
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS),
				new Point2D(), 6, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		c.setWeapon(new Weapon(b, 10f, ShotType.TARGETEDBEAM));
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {c},60,120));
		
		e.setBehaviour(new StaticPositionBehaviour(new Point2D(
				(float)(Math.random()*Board.BOARD_SIZE), 
				(float)(Math.random()*Board.BOARD_SIZE/3f)),WAITTIME));

		return e;
	}
	
	/**
	 * @return straight-dual-shot enemy with StaticPositionBehaviour
	 */
	private static Enemy makeLocustSpiral() {
		
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(LocustSpawnBoss.locustColor);
		list.add(c);
		
		for(int i=0; i<6; i++) {
			c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), 10);
			c.setColour(LocustSpawnBoss.locustColorDark);
			c.getPos().x = getXCoord(1, i);
			c.getPos().y = getYCoord(1, i);
			list.add(c);
		}
		
		// make enemy
		Enemy e = new Enemy(list,
				new Point2D((float) (Board.BOARD_SIZE*Math.random()),-Mech.MECH_RADIUS),
				new Point2D(), 30, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		b.getMods().put(BulletMod.GARGANTUA, 1);
		c.setWeapon(new Weapon(b, 5f, ShotType.SPIRAL));
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {c},120,360));
		
		e.setBehaviour(new StaticPositionBehaviour(new Point2D(
				Board.BOARD_SIZE/2f, Board.BOARD_SIZE/2f),WAITTIME));

		return e;
	}
}