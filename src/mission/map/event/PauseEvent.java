package mission.map.event;

import mission.Board;
import mission.map.LevelMap;

public class PauseEvent implements Event {

	@Override
	public void apply(Board board, LevelMap map) {
		map.setTimerPaused(true);
	}	
}
