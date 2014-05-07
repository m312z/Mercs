package mission.boss;

import gui.GameGUI;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import mission.Board;
import mission.ParticleManager;
import mission.behaviour.MechAttack;
import mission.behaviour.MechBehaviour;
import mission.effects.WordManager;
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

public class HoneycombBoss extends Enemy {
	
	public static final Color honey = new Color(252,179,25);
	public static final Color darker = new Color(152,100,15);
	
	List<Component> innerRing;
	List<Component> midRing;
	List<Component> outerRing;
	
	boolean roaming;
	
	public HoneycombBoss(List<Component> components) {
		super(components, new Point2D(
					Board.BOARD_SIZE/2f,
					(float) -(9*Mech.MECH_RADIUS*Math.sin(Math.toRadians(60)))),
				new Point2D(0, 1), 200, Mech.MECH_SPEED/2f);
	}
	
	public void setInnerRing(List<Component> innerRing) {
		this.innerRing = innerRing;
	}
	
	public void setMidRing(List<Component> midRing) {
		this.midRing = midRing;
	}
	
	public void setOuterRing(List<Component> outerRing) {
		this.outerRing = outerRing;
	}
	
	private List<Component> getRing(int ring) {
		if(ring==0) return innerRing;
		if(ring==1) return midRing;
		return outerRing;
	}
	
	public boolean isRoaming() {
		return roaming;
	}
	
	public void destroyRing(int i) {
		List<Component> ring = getRing(i);
		getComponents().removeAll(ring);
		for(Component c: ring)
			ParticleManager.makeExplosion(new Point2D(c.getPos().x+pos.x,c.getPos().y+pos.y));
		getAttackCycle().removeVolley(ring.get(0));
		ring.clear();
		
		// inner ring destroyed -- engage final form
		if(innerRing.isEmpty() && midRing.isEmpty() && outerRing.isEmpty()) {
			getAttackCycle().removeVolley(components.get(0));
			Bullet b = new Bullet(this, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
			b.getMods().put(BulletMod.HOMING,1);
			b.setSpeed(Bullet.BULLET_SPEED/2f);
			b.setRange(Bullet.BULLET_RANGE*2f);
			components.get(0).setWeapon(new Weapon(b, 3f, ShotType.FASTSPIRAL));
			getAttackCycle().addVolley(getAttackCycle().new Volley(new Component[] {components.get(0)},120,60));
			roaming = true;
		}
	}
	
	public void destroy(Board board, boolean score) {
		dead = true;
		if(score) {
			board.getCounter().addEnemiesDestroyed(1);
			board.addScore(198*6);
			WordManager.addWord("+"+(198*6*board.getMap().getLevel()),new Point2D(pos.x,pos.y),30);
		}
		for(Component c: components)
			ParticleManager.makeExplosion(new Point2D(c.getPos().x+pos.x,c.getPos().y+pos.y));
	}
		
	public static Enemy makeBoss() {
		List<Component> components = new ArrayList<Component>();
		
		// head
		Component head = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS*1.5f),
				new Point2D(),-1);
		head.setColour(honey);		
		components.add(head);
		
		HoneycombBoss e = new HoneycombBoss(components);
		e.setBase(true);
		
		// rings
		List<Component> ring = makeRing(e, 2);
		e.getComponents().addAll(ring);
		e.setInnerRing(ring);

		ring = makeRing(e, 5);
		e.getComponents().addAll(ring);
		e.setMidRing(ring);
		
		ring = makeRing(e, 8);
		e.getComponents().addAll(ring);
		e.setOuterRing(ring);
		
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		b.getMods().put(BulletMod.MEGA,1);
		b.setSpeed(Bullet.BULLET_SPEED/2f);
		b.setRange(Bullet.BULLET_RANGE*2f);
		components.get(0).setWeapon(new Weapon(b, 6f, ShotType.SPREAD));
		
		// behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(e.getRing(2),120,300));
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(e.getRing(1),120,60));
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(e.getRing(1),120,60));
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(e.getRing(1),120,60));
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(e.getRing(0),120,60));
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {head},60,300));
		e.setBehaviour(e.new HoneycombBehaviour());
		
		// start position
		e.getPos().y = -e.getMaxY();
		
		return e;
	}

	public static List<Component> makeBossImage() {
		List<Component> components = new ArrayList<Component>();
		
		// head
		Component head = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS*.5f),
				new Point2D(),-1);
		head.setColour(darker);		
		components.add(head);
		
		// rings
		components.addAll(makeRingImage(2));
		components.addAll(makeRingImage(5));
		components.addAll(makeRingImage(8));
		
		return components;
	}
	
	public static List<Component> makeRing(Mech e, int rad) {
		List<Component> components = new ArrayList<Component>();
		for(int j=0;j<2;j++) {
			for(int i=0;i<rad*6;i++) {
				
				float x = Math.abs(((i+(rad*3)/2f)%(rad*3))-(rad*3)/2f);
				if(x>rad) x=rad;
				if(i>rad*3) x = -x;
				x = x*3*Mech.MECH_RADIUS/2f;
				
				float y = rad*3 - Math.abs(i-rad*3);
				if(y<rad) y = y - 2*rad;
				else if(y > 2*rad) y = y - rad;
				else y = 2*(y-rad) - rad;
				y = (float) (y*Mech.MECH_RADIUS*Math.sin(Math.toRadians(60)));
				
				Component c = new Component(
						Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
						new Point2D(x,y),5);
				if(j==0) {
					c.setIndestructable(true);
					c.setColour(GameGUI.wreckage);
				} else {
					c.setColour(honey.darker());
					switch(rad) {
					case 9:
						if(i%3==0) {
							Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
							c.setWeapon(new Weapon(b, 2f, ShotType.STRAIGHT));
						}
						break;
					case 6:
						if(i%6==0) {
							Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
							b.getMods().put(BulletMod.HOMING,1);
							c.setWeapon(new Weapon(b, 2f, ShotType.TARGETEDBEAM));
						}
						break;
					case 3:
						if(i%6==0) {
							Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
							b.getMods().put(BulletMod.GARGANTUA,1);
							c.setWeapon(new Weapon(b, 2f, ShotType.SPIRAL));
						}
						break;
					}
				}
				components.add(c);
			}
			rad++;
		}
		return components;
	}
	
	public static List<Component> makeRingImage(int rad) {
		List<Component> components = new ArrayList<Component>();
		for(int j=0;j<2;j++) {
			for(int i=0;i<rad*6;i++) {
				
				float x = Math.abs(((i+(rad*3)/2f)%(rad*3))-(rad*3)/2f);
				if(x>rad) x=rad;
				if(i>rad*3) x = -x;
				x = x*3*Mech.MECH_RADIUS/6f;
				
				float y = rad*3 - Math.abs(i-rad*3);
				if(y<rad) y = y - 2*rad;
				else if(y > 2*rad) y = y - rad;
				else y = 2*(y-rad) - rad;
				y = (float) (y*Mech.MECH_RADIUS*Math.sin(Math.toRadians(60)))/3f;
				
				Component c = new Component(
						Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS/3f),
						new Point2D(x,y),5);
				if(j==0)
					 c.setColour(GameGUI.wreckage);
				else c.setColour(darker);
				components.add(c);
			}
			rad++;
		}
		return components;
	}
	
	class HoneycombBehaviour implements MechBehaviour
	{
		Point2D target = new Point2D(
				(float) (1/4f + Math.random()/2f)*Board.BOARD_SIZE/2f,
				(float) (1/4f + Math.random()/2f)*Board.BOARD_SIZE/2f);
		
		@Override
		public void tick(float dt, Board board, Mech mech) {
			if(mech.getPos().y >= MECH_RADIUS) {
				// rotate
				for(int i=0;i<3;i++) {
					boolean destroyed = true;
					List<Component> ring = ((HoneycombBoss)mech).getRing(i);
					boolean rotate = true;
					if(i==1 && ring.size()>0
							&& mech.getAttackCycle().getCurrentComponents().contains(
									ring.get(0)))
						rotate = false;
					float angle = dt/((2+i)*(2*(i%2)-1));
					for(Component c: ring) {
						if(rotate) {
							c.getShape().rotate(angle);
							c.getPos().rotate(angle);
						}
						if(!c.isDestroyed()&&!c.isIndestructable())
							destroyed = false;
					}
					if(destroyed && ring.size()>0)
						((HoneycombBoss)mech).destroyRing(i);
				}
				// shoot cycle
				if(((HoneycombBoss)mech).isRoaming()) {
					// move around
					mech.getDirection().x = (target.x - mech.getPos().x);
					mech.getDirection().y = (target.y - mech.getPos().y);
					
					float mag = Point2D.magnitude(mech.getDirection());
					Point2D.normalise(mech.getDirection());
					
					if(mag < Mech.MECH_RADIUS) {
						target.x = (float) (1/4f + Math.random()/2f)*Board.BOARD_SIZE;
						target.y = (float) (1/4f + Math.random()/2f)*Board.BOARD_SIZE;
					} else {
						Point2D.normalise(mech.getDirection());
						mech.getDirection().x *= 15;
						mech.getDirection().y *= 15;
						mech.getDirection().x = mech.getDirection().x + mech.getDirection().x;
						mech.getDirection().y = mech.getDirection().y + mech.getDirection().y;
						mech.setSpeed(Mech.MECH_SPEED);	
					}
				} else {
					mech.setSpeed(0);
				}
				mech.getAttackCycle().tick(dt, board);
			} else {
				// move into position
				mech.getDirection().x = 0;
				mech.getDirection().y = 1;
				mech.setSpeed(Mech.MECH_SPEED/2f);
			}
		}	
	}
}
