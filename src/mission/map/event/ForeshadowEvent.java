package mission.map.event;

import mission.Board;
import mission.effects.ComponentImage;
import mission.map.LevelMap;

public class ForeshadowEvent implements Event {

	ComponentImage image;
	
	public ForeshadowEvent(ComponentImage image) {
		this.image = image;
	}

	@Override
	public void apply(Board board, LevelMap map) {
		board.addComponentImage(image);
	}

}
