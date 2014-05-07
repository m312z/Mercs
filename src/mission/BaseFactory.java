package mission;

import static mission.BaseFactory.TunnelType.BASE;
import static mission.BaseFactory.TunnelType.OPEN;
import gui.GameGUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mission.behaviour.BaseBehaviour;
import mission.behaviour.MechAttack;
import mission.gameobject.Base;
import mission.gameobject.Component;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;
import mission.map.event.BaseSpawnEvent;
import mission.map.event.Event;
import mission.weapon.Weapon;
import mission.weapon.Weapon.ShotType;
import mission.weapon.bullet.Bullet;
import phys.DefaultShapes;
import phys.Point2D;
import phys.Shape;

public class BaseFactory {
	
	public enum TunnelType
	{
		BASE,OPEN,WALL,TURRETS,LASERS
	}
	
	public static Shape hexShape = Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS);
	public static float hexOffsetY = (float) (2*Mech.MECH_RADIUS*Math.sin(Math.toRadians(60)));
	public static float hexOffsetX = (float) (3*Mech.MECH_RADIUS*Math.cos(Math.toRadians(60)));
	static int distanceY = (int) (3000*Board.BOARD_SPEED);
	static int noHexesX = (int)(Board.BOARD_SIZE/hexOffsetX)+2;
	static int y;
	
	public static List<Base> makeLayer(Map<Base,Event> baseEvents) {
		y = 0;
		List<Base> returnList = new ArrayList<Base>();
		for(int i=1;y>-distanceY;i++) {
			if(i%6==0) {
				makeArena(4,returnList, baseEvents);
			} else if(i%4==0) {
				TunnelType first = TunnelType.values()[(int)(Math.random()*(TunnelType.values().length-2))+2];
				TunnelType second = TunnelType.values()[(int)(Math.random()*(TunnelType.values().length-2))+2];
				makeTunnel(5+(int)(Math.random()*5),returnList, baseEvents,
						new TunnelType[] {
						BASE,first,BASE,second,BASE});
			} else if(i%2==0) {
				TunnelType first = TunnelType.values()[(int)(Math.random()*(TunnelType.values().length-2))+2];
				makeTunnel(5+(int)(Math.random()*5),returnList, baseEvents,
						new TunnelType[] {
						BASE,BASE,first,BASE,BASE});
			} else {
				makeTunnel(2,returnList, baseEvents,
						new TunnelType[] {
						BASE,OPEN,OPEN,OPEN,BASE});
			}
		}
		
		return returnList;
	}
	
	public static int getColumn(int hexColumn) {
		if(hexColumn==0 || hexColumn==1)
			return 0;
		
		if(hexColumn < noHexesX/3)
			return 1;
		
		if(hexColumn < 2*noHexesX/3-1)
			return 2;
		
		if(hexColumn < noHexesX-3)
			return 3;
		
		if(hexColumn<noHexesX)
			return 4;
		
		return 5;
	}
	
	public static boolean isEdge(int hexColumn) {
		return (hexColumn==2
				|| hexColumn==noHexesX/3-1
				|| hexColumn==noHexesX/3
				|| hexColumn==2*noHexesX/3-2
				|| hexColumn==2*noHexesX/3-1
				|| hexColumn==noHexesX-4);
	}

	public static boolean isStartEdge(int hexColumn) {
		return (hexColumn==2
				|| hexColumn==noHexesX/3
				|| hexColumn==2*noHexesX/3-1);
	}
	
	public static int opposingEdge(int hexColumn) {
		if (hexColumn==2)
			return noHexesX/3-1;
		if (hexColumn==noHexesX/3) 
			return 2*noHexesX/3-2;
		if(hexColumn==2*noHexesX/3-1)
			return noHexesX-4;
		return 0;
	}
	
	public static void makeTunnel(int height, List<Base> returnList, Map<Base,Event> baseEvents, TunnelType[] tunnel) {
		
		Base origin = null;
		BaseSpawnEvent spawn = new BaseSpawnEvent();
		
		for(int i=0;i<height;i++) {
			for(int j=0;j<noHexesX;j++) {
				int column = getColumn(j);
				switch(tunnel[column]) {
				case BASE:
					Base b = new Base(hexShape,
							new Point2D(
									j*hexOffsetX,
									y+((j%2)*0.5f-i)*hexOffsetY));
					if(origin==null) {
						origin = b;
						spawn.setOrigin(origin);
					}
					returnList.add(b);
					break;
				case WALL:
					makeWallPiece(origin, spawn, i, j);
					break;
				case TURRETS:
					if(isEdge(j) && i%3==0)
						makeTurretPiece(origin, spawn, i, j);
					break;
				case LASERS:
					if(isStartEdge(j) && i%4==0)
						makeLaserPiece(origin, spawn, i, j, opposingEdge(j)-j, 0);
					else if(isEdge(j) && i%4!=0) {
						b = new Base(hexShape,
								new Point2D(
										j*hexOffsetX,
										y+((j%2)*0.5f-i)*hexOffsetY));
						returnList.add(b);
					}
					break;
				case OPEN:
				default:
					break;
				}
			}
		}
		baseEvents.put(origin,spawn);
		y -= hexOffsetY*height;
	}
	
	private static void makeArena(int height, List<Base> returnList, Map<Base,Event> baseEvents) {
		Base origin = null;
		BaseSpawnEvent spawn = new BaseSpawnEvent();
		
		for(int i=0;i<height;i++) {
			for(int j=0;j<noHexesX;j++) {
				int column = getColumn(j);
				switch(column) {
				case 0:
				case 4:
					Base b = new Base(hexShape,
							new Point2D(
									j*hexOffsetX,
									y+((j%2)*0.5f-i)*hexOffsetY));
					if(origin==null) {
						origin = b;
						spawn.setOrigin(origin);
					}
					returnList.add(b);
					break;
				}
			}
		}
		
		Enemy boss = EnemyFactory.makeMidBoss();
		boss.setBehaviour(BaseBehaviour.getInstance());
		boss.getPos().x = Board.BOARD_SIZE/2f - origin.getPos().x;
		boss.getPos().y = y - (height*hexOffsetY)/2f - origin.getPos().y;
		spawn.addEnemy(boss);
		
		baseEvents.put(origin,spawn);
		
		y -= hexOffsetY*height;
	}

	private static void makeTurretPiece(Base origin, BaseSpawnEvent spawn, int i, int j) {
		List<Component> components = new ArrayList<Component>();
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.enemy);
		components.add(c);
		
		Enemy e = new Enemy(components,
				new Point2D(j*hexOffsetX - origin.getPos().x,
						y+((j%2)*0.5f-i)*hexOffsetY - origin.getPos().y),
				new Point2D(), 10, Mech.MECH_SPEED);
		e.setBase(true);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		c.setWeapon(new Weapon(b, 60f, ShotType.TARGETEDBEAM));
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[]{c},60,60));
		e.setBehaviour(BaseBehaviour.getInstance());
		
		spawn.addEnemy(e);
	}
	
	private static void makeLaserPiece(Base origin, BaseSpawnEvent spawn, int i, int j, int width, int height) {
		
		Point2D start = new Point2D(
				j*hexOffsetX - origin.getPos().x,
				y+((j%2)*0.5f-i)*hexOffsetY - origin.getPos().y);
		Point2D dLaser = new Point2D(
				width*hexOffsetX,
				(height+((j+width)%2 - j%2)*0.5f)*hexOffsetY);
		
		List<Component> components = new ArrayList<Component>();
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.enemyLight);
		components.add(c);
		
		Component d = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),dLaser, -1);
		d.setColour(GameGUI.enemyDark);
		components.add(d);
		
		Enemy e = new Enemy(components,start,
				new Point2D(), 10, Mech.MECH_SPEED);
		e.setBase(true);
		
		// weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		c.setWeapon(new Weapon(b, 30f, ShotType.LASER));
		c.getWeapon().getLaserTargets().add(dLaser);
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[]{c},120,120));
		e.setBehaviour(BaseBehaviour.getInstance());
		
		spawn.addEnemy(e);
	}

	private static void makeWallPiece(Base origin, BaseSpawnEvent spawn, int i, int j) {
		
		List<Component> components = new ArrayList<Component>();
		Component c = new Component(Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS), new Point2D(), -1);
		c.setColour(GameGUI.enemy);
		components.add(c);
		
		Enemy e = new Enemy(components,
				new Point2D(j*hexOffsetX - origin.getPos().x,
						y+((j%2)*0.5f-i)*hexOffsetY - origin.getPos().y),
				new Point2D(), 10, Mech.MECH_SPEED);
		e.setBase(true);
		
		// create behaviour
		e.setAttackCycle(new MechAttack());
		e.getAttackCycle().addVolley(e.getAttackCycle().new Volley(new Component[0],6000,6000));
		e.setBehaviour(BaseBehaviour.getInstance());
		
		spawn.addEnemy(e);
	}
}
