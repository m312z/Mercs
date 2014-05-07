package mission.map;

import java.util.Map;

import mission.gameobject.Base;
import mission.map.event.Event;

public interface LevelFactory {

	public abstract float populate(int level, Map<Integer, Event> events,
			Map<Base, Event> baseEvents);

}