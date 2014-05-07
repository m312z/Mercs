package mission.map.event;

import java.util.List;

import mission.Board;
import mission.gameobject.Base;
import mission.map.LevelMap;

public class BaseEvent implements Event {

	List<Base> base;
	
	public BaseEvent(List<Base> base) {
		this.base = base;
	}

	@Override
	public void apply(Board board, LevelMap map) {
		for(Base b: base)
			board.addBase(b);
	}

	public void setBase(List<Base> base) {
		this.base = base;
	}
}
