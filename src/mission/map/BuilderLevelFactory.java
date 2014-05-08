package mission.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mission.BaseFactory;
import mission.Board;
import mission.EnemyFactory;
import mission.EnemyFactory.EnemyType;
import mission.behaviour.BuilderBehaviour;
import mission.behaviour.BuilderBounceDriftBehaviour;
import mission.behaviour.DriftBehaviour;
import mission.boss.BuilderBaseBoss;
import mission.gameobject.Base;
import mission.gameobject.Component;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;
import mission.map.event.BaseEvent;
import mission.map.event.CompositeEvent;
import mission.map.event.Event;
import mission.map.event.PauseEvent;
import mission.map.event.SpawnEvent;
import mission.weapon.Weapon.ShotType;
import phys.Point2D;

public class BuilderLevelFactory implements LevelFactory {

	static DriftBehaviour driftDownBehaviour = new DriftBehaviour(new Point2D(0,1),Mech.MECH_SPEED);
	
	@Override
	public float populate(int level, Map<Integer,Event> events, Map<Base,Event> baseEvents) {
		
		int waitTime = 240 + 40*(10-level);
		
		// base
		if(level==1) {
			Event e = makeWallPieces();
			events.put(0,e);
		}
		
		// wave details
		boolean bossWave = false;
		boolean pillarWave = false; 
		switch(level) {
		case 3:
		case 6:
		case 8:
		case 10:
			// boss wave
			int encounter = (level+2)/3;
			events.put(0, new PauseEvent());
			events.put(2,makeBossWave(encounter));
			events.put(4, new PauseEvent());
			bossWave = true;
			break;
		case 1:
		case 4:
			// pillars
			pillarWave = true;
			for(int i=waitTime;i<3390;i+=waitTime)
				events.put(i,makePillarEvent(2+level/2,2));
			break;
		case 2:
		case 5:
			// lasers
			for(int i=waitTime;i<3390;i+=waitTime) {
				events.put(i,makeLaserEvent()); i++;
				events.put(i,makeBuilderEvent(level/4));
			}
			break;
		case 7:
			// spawners
			for(int i=waitTime;i<3390;i+=waitTime)
				events.put(i,makeDroneSpawnerEvent());
			break;
		case 9:
			// mix
			for(int i=waitTime;i<3390;i+=waitTime) {
				int type = (int)(Math.random()*2);
				switch(type) {
				case 0:
					events.put(i,makeDroneSpawnerEvent());
					break;
				case 1:
					events.put(i,makeLaserEvent()); i++;
					events.put(i,makeBuilderEvent(level/4));
					break;
				}
			}
			break;
			
		}			
			
		// trains
		if(!pillarWave && !bossWave)
		for(int i=0;i<5+level;i++) {
			int time = 1 + (int) (Math.random()*2999);
			events.put(time, makeTrainEvent());
		}
						
		// midBoss
		if(!bossWave) {
			events.put(3390, new PauseEvent());
			events.put(3395, makeMidBossSpawnEvent(level));
			events.put(3399, new PauseEvent());
		}

		// 500 + 4*W: finish
		return 3400;
	}
	
	private Event makeTrainEvent() {
		float x = ((int)(Math.random()*6)+1)*(Board.BOARD_SIZE/8f);
		SpawnEvent event = new SpawnEvent();
		for(int i=0;i<8;i++) {
			Enemy e = EnemyFactory.makeBasicEnemy(EnemyType.DIRECTEDDRONE);
			e.getPos().x = x;
			e.getPos().y = i*-BaseFactory.hexOffsetY;
			e.setBehaviour(driftDownBehaviour);
			event.addEnemy(e);	
		}
		return event;
	}

	private Event makeMidBossSpawnEvent(int level) {
		SpawnEvent event = new SpawnEvent();
		Enemy e = EnemyFactory.makeSmallBoss();
		for(Component c: e.getComponents()) {
			if(c.getWeapon()!=null && c.getWeapon().getShotType()==ShotType.ASTEROID)
				c.setWeapon(BuilderBehaviour.makeBuilderWeapon(e));
		}
		e.setBehaviour(new BuilderBounceDriftBehaviour(
				new Point2D(
						(float)(Math.random()*2-1), 
						(float)(0.5f+Math.random())),
				Mech.MECH_SPEED/3f,
				4 + 2*level));
		event.addEnemy(e);
		return event;
	}

	private Event makeDroneSpawnerEvent() {
		SpawnEvent event = new SpawnEvent();
		Enemy e = EnemyFactory.makeBasicEnemy(EnemyType.BUILDER_DRONEMAKER);
		event.addEnemy(e);
		return event;
	}

	private Event makeLaserEvent() {
		SpawnEvent event = new SpawnEvent();
		Enemy e = EnemyFactory.makeBasicEnemy(EnemyType.BUILDER_LASER_STAR);
		event.addEnemy(e);
		return event;
	}

	private Event makePillarEvent(int weaponCount, int height) {
		SpawnEvent event = new SpawnEvent();
		Enemy p = EnemyFactory.makePillar(height);
		event.addEnemy(p);
		for(int i=0;i<height*6-weaponCount;i++) {
			Enemy e = EnemyFactory.makeBasicEnemy(EnemyType.BUILDER_BASIC);
			e.getPos().x = (e.getMaxX() - e.getMinX()) + (i%2)*Board.BOARD_SIZE+(e.getMaxX() - e.getMinX());
			e.getPos().y = (i+1)*(Board.BOARD_SIZE/(float)(i+2));
			event.addEnemy(e);
		}
		for(int i=0;i<weaponCount;i++) {
			Enemy e = EnemyFactory.makeBasicEnemy(EnemyType.BUILDER_ARMED);
			e.getPos().x = (e.getMaxX() - e.getMinX()) + (i%2)*Board.BOARD_SIZE+(e.getMaxX() - e.getMinX());
			e.getPos().y = (i+1)*(Board.BOARD_SIZE/(float)(i+2));
			event.addEnemy(e);
		}
		return event;
	}

	private Event makeBuilderEvent(int amnt) {
		SpawnEvent event = new SpawnEvent();
		for(int i=0;i<amnt;i++) {
			Enemy e = EnemyFactory.makeBasicEnemy(EnemyType.BUILDER_ARMED);
			e.getPos().x = -(e.getMaxX() - e.getMinX()) + (i%2)*(Board.BOARD_SIZE+2*(e.getMaxX() - e.getMinX()));
			e.getPos().y = (i+1)*(Board.BOARD_SIZE/(float)(i+2));
			event.addEnemy(e);
		}
		return event;
	}

	private Event makeBossWave(int boss) {

		SpawnEvent event = new SpawnEvent();
		event.addEnemy(BuilderBaseBoss.makeBoss(boss));
		CompositeEvent mainEvent = new CompositeEvent();	
		mainEvent.addEvent(event);
		mainEvent.addEvent(new PauseEvent());

		return mainEvent;
	}
	
	private BaseEvent makeWallPieces() {
		
		Base b;
		List<Base> list = new ArrayList<Base>();
			
		float y = -BaseFactory.hexOffsetY;
		while(y<Board.BOARD_SIZE) {
			b = new Base(BaseFactory.hexShape,new Point2D(0,y));
			b.setWrap(true);
			list.add(b);
			b = new Base(BaseFactory.hexShape,new Point2D(Board.BOARD_SIZE,y));
			b.setWrap(true);
			list.add(b);
			y += BaseFactory.hexOffsetY;
		}
		
		BaseEvent spawn = new BaseEvent(list);
		return spawn;
	}
}
