package mission;

import gui.GameGUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mission.behaviour.BeeBehaviour;
import mission.behaviour.BuilderBehaviour;
import mission.behaviour.FollowBehaviour;
import mission.behaviour.MechAttack;
import mission.behaviour.MechAttack.Volley;
import mission.behaviour.OrbitBehaviour;
import mission.behaviour.StaticPositionBehaviour;
import mission.gameobject.AllyMech;
import mission.gameobject.Component;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;
import mission.gameobject.Player;
import mission.powers.Power;
import mission.powers.PowerEater.ActivePower;
import mission.specialMechs.DecoyAllyMech;
import mission.weapon.Weapon;
import mission.weapon.Weapon.ShotType;
import mission.weapon.bullet.Bullet;
import phys.DefaultShapes;
import phys.Point2D;
import phys.Shape;

public class SupportFactory {

	public static AllyMech makeKillstar(Board board, Player player) {
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.player[player.getPlayerNumber()]);
		list.add(c);
		
		// make killstar
		AllyMech k = new AllyMech(player, list,
				new Point2D(player.getPos().x,Board.BOARD_SIZE+Mech.MECH_RADIUS),
				new Point2D(), 80, Mech.MECH_SPEED);
		k.setLifeTime(Power.SUPPORT_KILLSTAR.duration);
		
		
		// weapon
		Bullet b = new Bullet(k, false, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		list.get(0).setWeapon(new Weapon(b, 6f, ShotType.SPIRAL));
		k.setAttackCycle(new MechAttack());
		k.getAttackCycle().addVolley(k.getAttackCycle().new Volley(new Component[] {},1,1));
		k.getAttackCycle().addVolley(k.getAttackCycle().new Volley(new Component[] {c},10,Power.SUPPORT_KILLSTAR.duration));
		
		k.setBehaviour(new StaticPositionBehaviour(new Point2D(player.getPos().x,player.getPos().y),Power.SUPPORT_KILLSTAR.duration));
		return k;
	}

	public static AllyMech makeDrone(Board board, Player player) {
		
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.player[player.getPlayerNumber()]);
		list.add(c);
		
		// make drone
		AllyMech d = new AllyMech(player, list,
				new Point2D(player.getPos().x,player.getPos().y),
				new Point2D(), 25, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(d, false, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		c.setWeapon(new Weapon(b, 4f, ShotType.STRAIGHTDUAL));
		
		// create behaviour
		d.setAttackCycle(new MechAttack());
		d.getAttackCycle().addVolley(d.getAttackCycle().new Volley(new Component[] {c},60,120));
		
		d.setBehaviour(new StaticPositionBehaviour(new Point2D(player.getPos().x,player.getPos().y),-1));
		return d;
	}
	
	public static AllyMech makeGadget(Board board, Player player, float startAngle) {
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS/4f), new Point2D(), -1);
		c.setColour(GameGUI.player[player.getPlayerNumber()]);
		c.setIndestructable(true);
		list.add(c);
		
		// make drone
		AllyMech d = new AllyMech(player, list,
				new Point2D(player.getPos().x,player.getPos().y),
				new Point2D(), 1, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(d, false, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		c.setWeapon(new Weapon(b,120,ShotType.STRAIGHT));
				
		// create behaviour
		d.setAttackCycle(new MechAttack());
		d.getAttackCycle().addVolley(d.getAttackCycle().new Volley(new Component[] {c},0,360));
		d.setBehaviour(new OrbitBehaviour(player.getPos(),1,Mech.MECH_RADIUS*2,startAngle));
		d.setDestroyOffScreen(false);
		
		return d;
	}
	
	public static boolean cloneEnemy(Player master, Point2D pos, Board board) {
		
		// find mech target
		Enemy e = null;
		List<Enemy> targetList = board.getEnemies();
		float min = Float.MAX_VALUE;
		Point2D p = new Point2D();
		for(Enemy target: targetList) {
			if(target.isBase())
				continue;
			p.x = (pos.x-target.getPos().x);
			p.y = (pos.y-target.getPos().y);
			if(Point2D.magnitude(p) < min && Point2D.magnitude(p) < Mech.MECH_RADIUS*10) {
				min = Point2D.magnitude(p);
				e = target;
			}
		}
		
		if(e!=null && isCloneable(master, e)) {
			
			if(master.getPowerEater().getClone()!=null && !master.getPowerEater().getClone().isDead()) {
				master.getPowerEater().getClone().destroy(board, false);
			}
			
			// create body
			List<Component> list = new ArrayList<Component>(e.getComponents().size());
			Map<Component,Component> compMapping = new HashMap<Component,Component>();
			for(Component c: e.getComponents()) {
				Component cloneComponent = new Component(c.getShape().clone(),new Point2D(c.getPos().x,c.getPos().y),c.getHealth());
				cloneComponent.setDestroyed(c.isDestroyed());
				cloneComponent.setColour(GameGUI.player[master.getPlayerNumber()]);
				list.add(cloneComponent);
				compMapping.put(c,cloneComponent);
			}
			
			// create clone
			AllyMech clone = new AllyMech(master, list,
					new Point2D(pos.x,pos.y),
					new Point2D(), e.getMaxHealth(), Mech.MECH_SPEED);
			
			// create weapons
			for(Component c: e.getComponents()) {
				if(c.getWeapon()!=null) {
					Bullet cloneBullet = null;
					if(c.getWeapon().getBullet()!=null) {
						cloneBullet = c.getWeapon().getBullet().clone();
						cloneBullet.setEnemyBullet(false);
						cloneBullet.setParent(clone);
					}
					Weapon cloneWeapon = new Weapon(cloneBullet, c.getWeapon().getFireDelay(), c.getWeapon().getShotType());
					compMapping.get(c).setWeapon(cloneWeapon);
				}
			}
			
			// create clone behaviour
			clone.setAttackCycle(new MechAttack());
			for(Volley v: e.getAttackCycle().getShootCycle()) {
				Component[] battery = new Component[v.components.size()];
				Iterator<Component> cit = v.components.iterator();
				for(int i=0;i<battery.length;i++) {
					battery[i] = compMapping.get(cit.next());
				}
				clone.getAttackCycle().addVolley(clone.getAttackCycle().new Volley(battery,v.powerupTime,v.fireTime));
			}
			
			if(e.getBehaviour()==BeeBehaviour.getInstance()) {
				clone.setBehaviour(BeeBehaviour.getInstance());
			} else if(e.getBehaviour() instanceof BuilderBehaviour) {
				clone.setBehaviour(new BuilderBehaviour(true));
			} else {
				clone.setBehaviour(new FollowBehaviour(master, Mech.MECH_SPEED/2f, Mech.MECH_RADIUS*5));
			}
			
			master.getPowerEater().setClone(clone);
			board.addMech(clone,120);
			return true;
		}
		
		return false;
	}

	public static boolean isCloneable(Player master, Mech e) {
		if(e.getComponents().size() < 4) return true;
		if(e.getComponents().size() < 12) {
			for(ActivePower ap: master.getPowerEater().getActivePowers())
				if(ap.type==Power.CLONE_GREATERCLONE)
					return true;
		}
		return false;
	}

	public static AllyMech makePort(Board board, Player player) {
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS/3f), new Point2D(), -1);
		c.setColour(GameGUI.player[player.getPlayerNumber()]);
		list.add(c);
		
		// make drone
		AllyMech d = new AllyMech(player, list,
				new Point2D(player.getPos().x,player.getPos().y),
				new Point2D(), 5, Mech.MECH_SPEED);
		
		// weapon
		Bullet b = new Bullet(d, false, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		if(player.getPowerEater().hasPower(Power.PORT_AUTODEFENCE))
			c.setWeapon(new Weapon(b, 8f, ShotType.TARGETEDBEAM));
		else
			c.setWeapon(new Weapon(b, 15f, ShotType.PAUSE));
		
		// create behaviour
		d.setAttackCycle(new MechAttack());
		d.getAttackCycle().addVolley(d.getAttackCycle().new Volley(new Component[] {c},60,120));
		d.setBehaviour(new StaticPositionBehaviour(new Point2D(player.getPos().x,player.getPos().y),-1));
		return d;
	}
	
	public static AllyMech makeDecoy(Board board, Player player) {
		// create body
		List<Component> list = new ArrayList<Component>(1);
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS/2f), new Point2D(), -1);
		c.setColour(GameGUI.player[player.getPlayerNumber()]);
		list.add(c);
		
		// make drone
		AllyMech d = new DecoyAllyMech(player, list,
				new Point2D(player.getPos().x,player.getPos().y),
				new Point2D(), 200, Mech.MECH_SPEED);
		d.setLifeTime(Power.SUPPORT_DECOY.duration);
		
		// weapon
		Bullet b = new Bullet(d, false, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		if(player.getPowerEater().hasPower(Power.PORT_AUTODEFENCE))
			c.setWeapon(new Weapon(b, 15f, ShotType.TARGETEDBEAM));
		else
			c.setWeapon(new Weapon(b, 15f, ShotType.PAUSE));
		
		// create behaviour
		d.setAttackCycle(new MechAttack());
		d.getAttackCycle().addVolley(d.getAttackCycle().new Volley(new Component[] {c},60,120));
		d.setBehaviour(new StaticPositionBehaviour(new Point2D(player.getPos().x,player.getPos().y),Power.SUPPORT_DECOY.duration));
		return d;
	}
	
}
