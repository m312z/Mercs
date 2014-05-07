package core.controller;

import core.Frame;
import net.java.games.input.Component;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

public class XboxController extends core.controller.ShipController {

	net.java.games.input.Controller controller;

	boolean pausing;
	
	public XboxController(String player, int playerID, net.java.games.input.Controller controller) {
		super(player, playerID);
		this.controller = controller;
	}

	public net.java.games.input.Controller getController() {
		return controller;
	}
	
	@Override
	public void pollInput() {
		
		controller.poll();
		
		EventQueue queue = controller.getEventQueue();
		Event event = new Event();			

		pausing = false;
		
		while(queue.getNextEvent(event)) {
			Component comp = event.getComponent();

			// movement
			if(comp.getIdentifier().equals(Component.Identifier.Axis.X)) {
				if(Math.abs(event.getValue())>0.2f)
					direction.x = event.getValue();
				else direction.x = 0;
			} else if(comp.getIdentifier().equals(Component.Identifier.Axis.Y)) {
				if(Math.abs(event.getValue())>0.2f)
					direction.y = event.getValue();
				else direction.y = 0;
			}

			// buttons
			else if(comp.getIdentifier().equals(Component.Identifier.Button._0))
				powerButtons[1] = (event.getValue()>0.2f);
			else if(comp.getIdentifier().equals(Component.Identifier.Button._1))
				powerButtons[2] = (event.getValue()>0.2f);
			else if(comp.getIdentifier().equals(Component.Identifier.Button._2))
				powerButtons[0] = (event.getValue()>0.2f);
			else if(comp.getIdentifier().equals(Component.Identifier.Button._3))
				powerButtons[3] = (event.getValue()>0.2f);
			else if(comp.getIdentifier().equals(Component.Identifier.Button._5))
				shooting = (event.getValue()>0.2f);
			else if(comp.getIdentifier().equals(Component.Identifier.Button._7))
				pausing = (event.getValue()>0.2f);
			else continue;
		}
		
		cursorPosition.x += direction.x*Frame.SCREEN_SIZE[0]/160f;
		cursorPosition.y += direction.y*Frame.SCREEN_SIZE[0]/160f;
		cursorPosition.x = clampCursor(cursorPosition.x,0,Frame.SCREEN_SIZE[0]-10);
		cursorPosition.y = clampCursor(cursorPosition.y,0,Frame.SCREEN_SIZE[1]-10);
	}

	@Override
	public boolean isSelecting() {
		return (powerButtons[0] || powerButtons[1] || powerButtons[2] || powerButtons[3] || shooting);
	}

	@Override
	public boolean isPausing() {
		return pausing;
	}
}
