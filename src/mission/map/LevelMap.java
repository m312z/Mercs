package mission.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import mission.Board;
import mission.gameobject.Base;
import mission.map.event.Event;

public class LevelMap {

	public enum LevelType
	{
		THEHIVE("THE HIVE"),
		BUILDERCOLONY("BUILDER COLONY"),
		LOCUSTSWARM("LOCUST SWARM");
		
		public String displayName;
		
		private LevelType(String name) {
			this.displayName = name;
		}
	}
	
	LevelType levelType;
	LevelFactory factory;
	
	boolean timerPaused;
	float timer;
	int level;
	float finalEventTime = 0;
	Map<Integer,Event> events;
	Map<Base,Event> baseEvents;
	
	public LevelMap(LevelType levelType) {
		timer = 0;
		level = 1;
		events = new TreeMap<Integer,Event>();
		baseEvents = new HashMap<Base,Event>();
		
		this.levelType = levelType;
		switch(levelType) {
		case THEHIVE:
			factory = new HiveLevelFactory(); break;
		case BUILDERCOLONY:
			factory = new BuilderLevelFactory(); break;
		case LOCUSTSWARM:
			factory = new LocustLevelFactory(); break;
		}
		
		finalEventTime = factory.populate(level, events, baseEvents);
	}
	
	public Map<Integer, Event> getEvents() {
		return events;
	}
	
	public Map<Base, Event> getBaseEvents() {
		return baseEvents;
	}

	public LevelType getLevelType() {
		return levelType;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public float getFinalEventTime() {
		return finalEventTime;
	};
	
	public float getTimer() {
		return timer;
	}
	
	public void setTimer(float timer) {
		this.timer = timer;
	}
	
	public void setTimerPaused(boolean timerPaused) {
		this.timerPaused = timerPaused;
	}
	
	public boolean isTimerPaused() {
		return timerPaused;
	}
	
	public boolean tick(float dt, Board board) {
		
		if(!timerPaused) {
			timer += dt;
		} else {
			// start time once enemies are dead
			timerPaused = !(board.getEnemies().isEmpty() && board.getEnemyBullets().isEmpty());
		}

		// timed events
		int eventTime = 0;
		Iterator<Integer> eit = events.keySet().iterator();
		while(eit.hasNext() && eventTime < timer) {
			eventTime = eit.next();
			if(eventTime < timer) {
				events.get(eventTime).apply(board, this);
				eit.remove();
			}
		}
		
		// positional events
		Iterator<Base> bit = baseEvents.keySet().iterator();
		while(bit.hasNext()) {
			Base b = bit.next();
			if(b.getPos().y+b.getShape().getMaxY() > b.getMinY()) {
				baseEvents.get(b).apply(board, this);
				bit.remove();
			}
		}
		
		// end of wave
		if(!timerPaused && events.isEmpty() && level<11) {
			board.setLives(board.getLives()+1);
			level = level+1;
			timer = 0;
			if(level < 11)
				finalEventTime = factory.populate(level, events, baseEvents);
		}
		
		return (level==11);
	}
}
