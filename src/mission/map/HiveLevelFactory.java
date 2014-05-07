package mission.map;

import static mission.EnemyFactory.WAITTIME;

import java.util.List;
import java.util.Map;

import mission.BaseFactory;
import mission.Board;
import mission.EnemyFactory;
import mission.EnemyFactory.EnemyType;
import mission.boss.HoneycombBoss;
import mission.effects.ComponentImage;
import mission.gameobject.Base;
import mission.gameobject.Component;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;
import mission.map.event.BaseEvent;
import mission.map.event.CompositeEvent;
import mission.map.event.Event;
import mission.map.event.ForeshadowEvent;
import mission.map.event.PauseEvent;
import mission.map.event.SpawnEvent;
import phys.Point2D;

public class HiveLevelFactory implements LevelFactory {
	
	/* (non-Javadoc)
	 * @see mission.map.LevelFactory#populate(int, java.util.Map, java.util.Map)
	 */
	@Override
	public float populate(int level, Map<Integer,Event> events, Map<Base,Event> baseEvents) {

		int eventTime  = 0;

		// wave details
		switch(level) {
		case 10:
			// boss wave
			events.put(0, new PauseEvent());
			events.put(2,makeBossWave(level));
			events.put(4, new PauseEvent());
			eventTime = 4;
			break;
		case 5:
			// base wave
			BaseEvent event = new BaseEvent(BaseFactory.makeLayer(baseEvents));
			events.put(0,event);
			eventTime = 3500;
			break;
		default:
			
			// trains
			for(int i=0;i<5+level;i++) {
				int time = (int) (Math.random()*(200+4*WAITTIME));
				events.put(time, makeTrainEvent());
			}
			
			// 200: start
			eventTime = 200;		
			for(int i=0;i<2;i++) {
				events.put(eventTime, makeSpawnEvent(level));
				eventTime+=WAITTIME;
			}

			// 200 + 2*W: mid-boss 
			events.put(eventTime, new PauseEvent());
			events.put(eventTime+2, makeSmallBossSpawnEvent());
			events.put(eventTime+4, new PauseEvent());
			eventTime+=100;

			// 300 + 2*W: more waves
			for(int i=0;i<2;i++) {
				events.put(eventTime, makeSpawnEvent(level));
				eventTime+=WAITTIME;
			}

			// 300 + 4*W: boss
			events.put(eventTime, new PauseEvent());
			events.put(eventTime+2, makeMidBossSpawnEvent());
			events.put(eventTime+4, new PauseEvent());
			eventTime+=200;

			// boss image
			if(level==6 || level==9) {

				// get free time
				int freetime = 500;
				while(events.keySet().contains(freetime))
					freetime++;

				// place foreshadowing event
				List<Component> bossImage = HoneycombBoss.makeBossImage();
				Point2D startPos = new Point2D(
						(float)(Math.random()*Board.BOARD_SIZE),
						-Board.BOARD_SIZE/2f);
				if(level==9)
					startPos.y = 3*Board.BOARD_SIZE/2f;
				Point2D vel = new Point2D(
						Board.BOARD_SIZE/2f - startPos.x,
						Board.BOARD_SIZE/2f - startPos.y);
				Point2D.normalise(vel);
				vel.x *= Mech.MECH_SPEED/4f;
				vel.y *= Mech.MECH_SPEED/4f;
				ForeshadowEvent fe = new ForeshadowEvent(
						new ComponentImage(bossImage, startPos, vel));
				events.put(freetime,fe);
			}
			break;
		}
		
		// 500 + 4*W: finish
		events.put(eventTime, new PauseEvent());
		return eventTime;
	}

	private Event makeBossWave(int boss) {

		SpawnEvent event = new SpawnEvent();
		event.addEnemy(HoneycombBoss.makeBoss());
		CompositeEvent mainEvent = new CompositeEvent();	
		mainEvent.addEvent(event);
		mainEvent.addEvent(new PauseEvent());

		return mainEvent;	
	}

	private Event makeSpawnEvent(int level) {

		int difficulty = level;

		// basic enemies
		SpawnEvent event = new SpawnEvent();	
		for(int i=0;i<(int)(1+(Math.random()*difficulty/2f));i++) {
			EnemyType type = EnemyType.DRONE;
			switch(level) {
			case 1:
				type = EnemyType.SPIRAL;
				break;
			case 2:
				type = EnemyType.SPREADER;
				break;
			case 3:
				type = EnemyType.HOMER;
				break;
			case 4:
				type = EnemyType.BEAMER; 
				break;
			case 6:
				type = EnemyType.SPIRAL;
				if(Math.random()*2>1)
					type = EnemyType.HOMER;
				break;
			case 7:
			case 8:
			case 9:
				int t = (int)(Math.random()*4);
				switch(t) {
				case 0: type = EnemyType.BEAMER; break;
				case 1: type = EnemyType.HOMER; break;
				case 2: type = EnemyType.SPREADER; break;
				case 3: type = EnemyType.SPIRAL; break;
				}
				break;
			}
			event.addEnemy(EnemyFactory.makeBasicEnemy(type));
		}
		
		// bees and drones
		for(int i=0;i<difficulty;i++) {
			if(level%10>5 && level<10) {
				event.addEnemy(EnemyFactory.makeBasicEnemy(EnemyType.HONEYBEE));
			} else event.addEnemy(EnemyFactory.makeBasicEnemy(EnemyType.DRONE));
		}
		
		return event;
	}

	private Event makeTrainEvent() {
		SpawnEvent event = new SpawnEvent();
		List<Enemy> train = EnemyFactory.makeTrain();
		for(Enemy e: train)
			event.addEnemy(e);	
		return event;
	}

	private Event makeSmallBossSpawnEvent() {
		SpawnEvent event = new SpawnEvent();
		event.addEnemy(EnemyFactory.makeSmallBoss());
		return event;
	}

	private Event makeMidBossSpawnEvent() {

		SpawnEvent event = new SpawnEvent();
		int type = (int) (Math.random()*2);
		if (type<1) {
			event.addEnemy(EnemyFactory.makeSmallBoss());
			event.addEnemy(EnemyFactory.makeSmallBoss());
			event.addEnemy(EnemyFactory.makeSmallBoss());
		} else {
			event.addEnemy(EnemyFactory.makeSmallBoss());
			event.addEnemy(EnemyFactory.makeSmallBoss());
			event.addEnemy(EnemyFactory.makeMidBoss());
		}

		CompositeEvent mainEvent = new CompositeEvent();	
		mainEvent.addEvent(event);
		mainEvent.addEvent(new PauseEvent());

		return mainEvent;
	}

}
