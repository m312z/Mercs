package mission.map;

import static mission.EnemyFactory.WAITTIME;

import java.util.List;
import java.util.Map;

import mission.EnemyFactory;
import mission.EnemyFactory.EnemyType;
import mission.boss.LocustSpawnBoss;
import mission.gameobject.Base;
import mission.gameobject.Enemy;
import mission.map.event.Event;
import mission.map.event.PauseEvent;
import mission.map.event.SpawnEvent;

public class LocustLevelFactory implements LevelFactory {
		
	/* (non-Javadoc)
	 * @see mission.map.LevelFactory#populate(int, java.util.Map, java.util.Map)
	 */
	@Override
	public float populate(int level, Map<Integer,Event> events, Map<Base,Event> baseEvents) {

		level = 3;
		
		int eventTime  = 0;

		// wave details
		switch(level) {
		case 1:
		case 2:
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
			
			// 500 + 4*W: finish
			break;
			
		case 3:
			
			// 200: start
			eventTime = 200;		
			for(int i=0;i<2;i++) {
				events.put(eventTime, makeSpawnEvent(level));
				eventTime+=WAITTIME;
			}
			eventTime+=200;
			
			// 400 + 2*W: locust boss (spawn)
			events.put(eventTime, new PauseEvent());
			events.put(eventTime+2, makeLocustSpawnEvent());
			events.put(eventTime+4, new PauseEvent());
			eventTime+=100;
			
			// 500 + 2*W: finish
			break;
		}
		
		events.put(eventTime, new PauseEvent());
		return eventTime;
	}
	
	/*-------------------*/
	/* Locust Boss waves */
	/*-------------------*/
	
	private Event makeLocustSpawnEvent() {
		
		SpawnEvent event = new SpawnEvent();
		event.addEnemy(LocustSpawnBoss.makeBoss());
		return event;
		
	}
	
	/*-------------*/
	/* First waves */
	/*-------------*/
	
	private Event makeTrainEvent() {
		SpawnEvent event = new SpawnEvent();
		List<Enemy> train = EnemyFactory.makeTrain();
		for(Enemy e: train)
			event.addEnemy(e);	
		return event;
	}
	
	private Event makeSpawnEvent(int level) {

		// basic enemies
		SpawnEvent event = new SpawnEvent();	
		for(int i=0;i<(int)(1+(Math.random()*level/2f));i++) {
			EnemyType type = EnemyType.DRONE;
			switch(level) {
			case 1:
			case 2:
			case 3:
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
		
		// drones
		for(int i=0;i<level;i++)
			event.addEnemy(EnemyFactory.makeBasicEnemy(EnemyType.DRONE));
		
		return event;
	}
	
	private Event makeSmallBossSpawnEvent() {
		SpawnEvent event = new SpawnEvent();
		event.addEnemy(EnemyFactory.makeSmallBoss());
		return event;
	}

	private Event makeMidBossSpawnEvent() {
		SpawnEvent event = new SpawnEvent();
		event.addEnemy(EnemyFactory.makeLocustBoss());
		return event;
	}	
}
