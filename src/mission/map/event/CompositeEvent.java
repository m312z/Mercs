package mission.map.event;

import java.util.ArrayList;
import java.util.List;

import mission.Board;
import mission.map.LevelMap;

public class CompositeEvent implements Event {

	List<Event> events;
	
	public CompositeEvent() {
		events = new ArrayList<Event>();
	}
	
	public void addEvent(Event event) {
		events.add(event);
	}
	
	@Override
	public void apply(Board board, LevelMap map) {
		for(Event e: events)
			e.apply(board, map);
	}

}
