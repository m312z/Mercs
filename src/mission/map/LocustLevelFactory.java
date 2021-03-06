package mission.map;

import static mission.Board.BOARD_SIZE;
import static mission.EnemyFactory.WAITTIME;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mission.EnemyFactory;
import mission.EnemyFactory.EnemyType;
import mission.behaviour.RepeatingStaticBehaviour;
import mission.boss.LocustBaseBoss;
import mission.boss.LocustGiftBoss;
import mission.boss.LocustSpawnBoss;
import mission.gameobject.Base;
import mission.gameobject.Enemy;
import mission.map.event.Event;
import mission.map.event.PauseEvent;
import mission.map.event.SpawnEvent;
import phys.Point2D;

public class LocustLevelFactory implements LevelFactory {
		
	/* (non-Javadoc)
	 * @see mission.map.LevelFactory#populate(int, java.util.Map, java.util.Map)
	 */
	@Override
	public float populate(int level, Map<Integer,Event> events, Map<Base,Event> baseEvents) {

		int eventTime  = 0;
		
		// wave details
		switch(level) {
		case 1:
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
			events.put(eventTime+2, makeMidBossSpawnEvent(false));
			events.put(eventTime+4, new PauseEvent());
			eventTime+=200;
			
			// 500 + 4*W: finish
			break;
			
		case 2:
			
			// 200: start
			eventTime = 200;		
			for(int i=0;i<2;i++) {
				events.put(eventTime, makeSpawnEvent(level));
				eventTime+=WAITTIME;
			}
			eventTime+=200;
			
			// 400 + 2*W: locust boss (spawn)
			events.put(eventTime, new PauseEvent());
			events.put(eventTime+2, makeLocustSpawnBossEvent());
			events.put(eventTime+4, new PauseEvent());
			eventTime+=100;
											
			// 500 + 2*W: finish
			break;

		case 3:
		case 5:
			
			// 200: start
			eventTime = 200;
			events.put(eventTime, makeLocustSpawnEvent(level));
			for(int i=0;i<4;i++) {
				eventTime += 10;
				events.put(eventTime, makeDroneSpawnEvent());
				if(i==2) {
					SpawnEvent event = new SpawnEvent();	
					event.addEnemy(EnemyFactory.makeBasicEnemy(EnemyType.LOCUST_SPIRAL));
					events.put(eventTime+20, event);
				}
				eventTime += WAITTIME;
			}
			eventTime += 100;
			
			// 340 + 4*W: mid-boss 
			events.put(eventTime, new PauseEvent());
			events.put(eventTime+2, makeMidBossSpawnEvent(true));
			events.put(eventTime+4, new PauseEvent());
			eventTime+=100;
			
			// 440 + 4*W: more drones
			events.put(eventTime, makeLocustSpawnEvent(level));
			for(int i=0;i<4;i++) {
				eventTime += 10;
				events.put(eventTime, makeDroneSpawnEvent());
				if(i==2) {
					SpawnEvent event = new SpawnEvent();	
					event.addEnemy(EnemyFactory.makeBasicEnemy(EnemyType.LOCUST_SPIRAL));
					events.put(eventTime+20, event);
				}
				eventTime += WAITTIME;
			}
			eventTime += 100;
			
			// 580 + 8*W:
			events.put(eventTime, new PauseEvent());
			switch(level) {
			case 3:
				//locust boss (base)
				events.put(eventTime+2, makeLocustBaseBossEvent());
				break;
			case 5:
			default:
				// locust boss (base)
				events.put(eventTime+2, makeLocustBaseBossEvent());
				break;
			}
			events.put(eventTime+4, new PauseEvent());
			eventTime+=100;
			
			// 680 + 8*W: finish
			break;
			
		case 4:
			
			// 200: start / locust boss (gift)
			eventTime = 200;
			events.put(eventTime, new PauseEvent());
			events.put(eventTime+2, makeLocustGiftBossEvent());
			events.put(eventTime+4, new PauseEvent());
			eventTime+=100;
			
			// 300 finish
			break;
			
		case 6:
			break;
		}
		
		events.put(eventTime, new PauseEvent());
		return eventTime;
	}
	
	/*--------------*/
	/* Locust waves */
	/*--------------*/
	
	private Event makeLocustSpawnEvent(int level) {

		SpawnEvent event = new SpawnEvent();	
		List<Point2D> positions = new ArrayList<Point2D>();
		for(int i=0;i<5;i++) {
			positions.add(new Point2D(
					(i+1)*BOARD_SIZE/6f, 
					(i%2+1)*BOARD_SIZE/6f));
		}
		Enemy e = EnemyFactory.makeBasicEnemy(EnemyType.LOCUST_ARMOURED_DRONE);
		e.setBehaviour(new RepeatingStaticBehaviour(positions, WAITTIME));
		event.addEnemy(e);
		
		return event;
	}
	
	private Event makeDroneSpawnEvent() {
		
		SpawnEvent event = new SpawnEvent();
		for(int i=0;i<3;i++)
			event.addEnemy(EnemyFactory.makeBasicEnemy(EnemyType.LOCUST_DRONE));
		return event;
	}
	
	/*-------------------*/
	/* Locust Boss waves */
	/*-------------------*/

	private Event makeLocustSpawnBossEvent() {
		
		SpawnEvent event = new SpawnEvent();
		event.addEnemy(LocustSpawnBoss.makeBoss());
		return event;
	}
	
	private Event makeLocustBaseBossEvent() {
	
		SpawnEvent event = new SpawnEvent();
		event.addEnemy(LocustBaseBoss.makeBoss());
		return event;
	}
	
	private Event makeLocustGiftBossEvent() {
		
		SpawnEvent event = new SpawnEvent();
		event.addEnemy(LocustGiftBoss.makeBoss());
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

	private Event makeMidBossSpawnEvent(boolean locust) {
		SpawnEvent event = new SpawnEvent();
		event.addEnemy(EnemyFactory.makeLocustBoss(locust));
		return event;
	}	
}
