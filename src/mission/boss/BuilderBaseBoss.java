package mission.boss;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mission.Board;
import mission.EnemyFactory;
import mission.behaviour.MechAttack;
import mission.behaviour.MechBehaviour;
import mission.gameobject.Base;
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

public class BuilderBaseBoss extends Enemy {

	public static final Color builderBossColor = new Color(135,206,250);
	public static final Color[] builderBossColorDark = {
		new Color(23,63,88),
		new Color(51,99,128),
		new Color(79,135,168),
		new Color(107,170,208)
	};
	public static final Color builderBossBackground = new Color(33,33,33);
	
	int encounter = 0;
	
	public BuilderBaseBoss(List<Component> components) {
		super(components, new Point2D(),new Point2D(), 200, Mech.MECH_SPEED/2f);
	}
	
	public void setEncounter(int encounter) {
		this.encounter = encounter;
	}
	
	public int getEncounter() {
		return encounter;
	}
	
	public static Enemy makeBoss(int encounter) {
		List<Component> components = new ArrayList<Component>();
		
		// head
		Component core = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),new Point2D(),-1);
		core.setColour(builderBossColor);		
		components.add(core);
		for(int i=0;i<6;i++) {
			if(encounter<4 && (i==2 || i==4))
				continue;
			Component c = new Component(
					Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
					new Point2D(EnemyFactory.getXCoord(1,i),EnemyFactory.getYCoord(1,i)),
					200);
			c.setColour(builderBossColorDark[3]);		
			components.add(c);
		}
		
		// secondary heads
		Component[] secondaryHead = new Component[2];
		if(encounter < 4) {
			for(int i=0;i<2;i++) {
				secondaryHead[i] = new Component(
						Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
						new Point2D(EnemyFactory.getXCoord(1,i*2+2),EnemyFactory.getYCoord(1,i*2+2)),-1);
				secondaryHead[i].setColour(builderBossColor);		
				components.add(secondaryHead[i]);	
			}
		}
		for(int i=0;i<7;i++) {
			if(encounter<3 && (i==2 || i==4))
				continue;
			Component c = new Component(
					Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
					new Point2D(EnemyFactory.getXCoord(2,i+3),EnemyFactory.getYCoord(2,i+3)),
					150);
			c.setColour(builderBossColorDark[2]);
			c.setShowHealth(true);
			components.add(c);
		}
		
		// tertiary heads
		Component[] tertiaryHead = new Component[2];
		if(encounter < 3) {
			for(int i=0;i<2;i++) {
				tertiaryHead[i] = new Component(
						Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
						new Point2D(EnemyFactory.getXCoord(2,i*2+5),EnemyFactory.getYCoord(2,i*2+5)),-1);
				tertiaryHead[i].setColour(builderBossColor);		
				components.add(tertiaryHead[i]);	
			}
		}
		for(int i=0;i<9;i++) {
			if(encounter<2 && (i==6 || i==2))
				continue;
			Component c = new Component(
					Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
					new Point2D(EnemyFactory.getXCoord(3,i+5),EnemyFactory.getYCoord(3,i+5)),
					100);
			c.setColour(builderBossColorDark[1]);
			c.setShowHealth(true);
			components.add(c);
		}
		
		// quaternary heads
		Component[] quaternaryHead = new Component[2];
		if(encounter < 2) {
			for(int i=0;i<2;i++) {
				quaternaryHead[i] = new Component(
						Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
						new Point2D(EnemyFactory.getXCoord(3,i*4+7),EnemyFactory.getYCoord(3,i*4+7)),-1);
				quaternaryHead[i].setColour(builderBossColor);		
				components.add(quaternaryHead[i]);
			}
		}
		Component[] curtainWall = new Component[6];
		for(int i=0;i<11;i++) {
			Component c = new Component(
					Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
					new Point2D(EnemyFactory.getXCoord(4,i+7),EnemyFactory.getYCoord(4,i+7)),
					50);
			if(encounter==4 && i%2==0) {
				c.setColour(builderBossColor);
				curtainWall[i/2] = c;
			} else c.setColour(builderBossColorDark[0]);
			c.setShowHealth(true);
			components.add(c);
		}
		
		BuilderBaseBoss e = new BuilderBaseBoss(components);
		e.setBase(true);
		
		// weapon
		Bullet b;
		switch(encounter) {
		case 4:
			b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
			b.getMods().put(BulletMod.MEGA,1);
			b.getMods().put(BulletMod.HOMING,1);
			core.setWeapon(new Weapon(b, 3f, ShotType.SPREAD));
			b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
			b.setSpeed(Bullet.BULLET_SPEED/3f);
			b.setRange(Bullet.BULLET_RANGE*3f);
			b.getMods().put(BulletMod.GARGANTUA,1);
			for(Component c: curtainWall) c.setWeapon(new Weapon(b, 100f, ShotType.STRAIGHT));
			break;
		case 3:
			b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
			b.getMods().put(BulletMod.GARGANTUA,1);
			core.setWeapon(new Weapon(b, 3f, ShotType.SPREAD));
			secondaryHead[0].setWeapon(new Weapon(b, 3f, ShotType.TARGETEDBEAM));
			secondaryHead[1].setWeapon(new Weapon(b, 3f, ShotType.TARGETEDBEAM));
			break;
		case 2:
			b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS*3f), new Point2D(), new Point2D());
			b.setSpeed(Bullet.BULLET_SPEED/3f);
			b.setRange(Bullet.BULLET_RANGE*3);
			core.setWeapon(new Weapon(b, 16f, ShotType.SPIRAL));
			b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
			secondaryHead[0].setWeapon(new Weapon(b, 3f, ShotType.TARGETEDBEAM));
			secondaryHead[1].setWeapon(new Weapon(b, 3f, ShotType.TARGETEDBEAM));
			b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
			b.getMods().put(BulletMod.GARGANTUA,1);
			b.setSpeed(Bullet.BULLET_SPEED/2);
			b.setRange(2*Bullet.BULLET_RANGE);
			tertiaryHead[0].setWeapon(new Weapon(b, 30f, ShotType.DIRECTEDBEAM));
			tertiaryHead[1].setWeapon(new Weapon(b, 30f, ShotType.DIRECTEDBEAM));
			break;
		case 1:
		default:
			b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
			core.setWeapon(new Weapon(b, 120f, ShotType.LASER));
			for(int i=0;i<6;i++) {
				core.getWeapon().getLaserTargets().add(new Point2D(
						(float)(Board.BOARD_DIAGONAL*Math.cos(Math.toRadians(i*60))),
						(float)(Board.BOARD_DIAGONAL*Math.sin(Math.toRadians(i*60)))));
			}
			b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
			secondaryHead[0].setWeapon(new Weapon(b, 3f, ShotType.SPREAD));
			secondaryHead[1].setWeapon(new Weapon(b, 3f, ShotType.SPREAD));
			tertiaryHead[0].setWeapon(new Weapon(b, 18f, ShotType.STRAIGHTDUAL));
			tertiaryHead[1].setWeapon(new Weapon(b, 18f, ShotType.STRAIGHTDUAL));
			quaternaryHead[0].setWeapon(new Weapon(b, 18f, ShotType.STRAIGHTDUAL));
			quaternaryHead[1].setWeapon(new Weapon(b, 18f, ShotType.STRAIGHTDUAL));
			break;
		}
		
		// behaviour
		e.setAttackCycle(new MechAttack());
		switch(encounter) {
		case 4:
			e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(curtainWall,60,600));
			e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {core},60,100));
			break;
		case 3:
			e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {core},80,300));
			e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {secondaryHead[0],secondaryHead[1]},60,300));
			break;
		case 2:
			e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {core},120,300));
			e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {tertiaryHead[0],tertiaryHead[1]},60,300));
			e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {tertiaryHead[0],tertiaryHead[1],secondaryHead[0],secondaryHead[1]},0,200));
			break;
		case 1:
		default:
			e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {core},80,240));
			e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {secondaryHead[0],secondaryHead[1]},0,300));
			e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[] {tertiaryHead[0],tertiaryHead[1],quaternaryHead[0],quaternaryHead[1]},0,300));
			break;
		}
		e.setBehaviour(e.new BuilderBossBehaviour());
		
		// start position
		e.getPos().x = Board.BOARD_SIZE/2f;
		e.getPos().y = -e.getMaxY();
		e.setEncounter(encounter);
		
		return e;
	}
	
	public class BuilderBossBehaviour implements MechBehaviour {

		Point2D targetLaser = new Point2D(
				(float) (1/4f + Math.random()/2f)*Board.BOARD_SIZE/2f,
				(float) (1/4f + Math.random()/2f)*Board.BOARD_SIZE/2f);
		private boolean laserChange = true;
		private boolean beamChange = true;
		private int laserDirection = 1;
		
		
		@Override
		public void tick(float dt, Board board, Mech mech) {
			
			if(mech.getPos().y >= MECH_RADIUS) {
				mech.setSpeed(0);
				Weapon w;
				switch(encounter) {
				case 1:
					// rotate laser star
					w = mech.getComponents().get(0).getWeapon();
					if(w!=null) {
						for(Point2D target: w.getLaserTargets())
							target.rotate(dt*0.2f*laserDirection);
						if(mech.getAttackCycle().isShooting())
							laserChange = true;
						if(mech.getAttackCycle().isPoweringUp() && laserChange) {
							laserDirection *= -1;
							laserChange = false;
						}
					}
					break;
				case 2:
					// alter directed shot target
					if(mech.getAttackCycle().isShooting())
						beamChange = true;
					if(beamChange && mech.getAttackCycle().isPoweringUp()) {
						beamChange = false;
						float targetX = (float) (-1/4f + 3*Math.random()/2f)*Board.BOARD_SIZE;
						for(Component c: mech.getComponents()) {
							w = c.getWeapon();
							if(w!=null && w.getShotType()==ShotType.DIRECTEDBEAM) {
								w.getAim().y = Board.BOARD_SIZE - mech.getPos().y;
								w.getAim().x = targetX - mech.getPos().x;
								Point2D.normalise(w.getAim());
							}
						}
					}
					break;
				}
				mech.getAttackCycle().tick(dt, board);
			} else {
				// move into position
				mech.getDirection().x = 0;
				mech.getDirection().y = 1;
				mech.setSpeed(Mech.MECH_SPEED/2f);
			}
			
			Iterator<Component> cit = mech.getComponents().iterator();
			while(cit.hasNext()) {
				Component c = cit.next();
				if(c.isDestroyed()) {
					// remove component
					cit.remove();
					mech.getAttackCycle().removeComponent(c);
					mech.getAttackCycle().removeEmptyVolleys();
					
					// add base
					Base b = new Base(c.getShape(), new Point2D(
							mech.getPos().x + c.getPos().x,
							mech.getPos().y + c.getPos().y));
					board.addBase(b);
				}
			}
		}

	}
}
