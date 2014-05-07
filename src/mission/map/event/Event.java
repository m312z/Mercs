package mission.map.event;

import mission.Board;
import mission.map.LevelMap;

public abstract interface Event {
	public abstract void apply(Board board, LevelMap map);
	
}
