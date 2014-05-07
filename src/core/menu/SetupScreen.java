package core.menu;

import core.Frame;

public abstract class SetupScreen {

	Frame frame;
	public boolean finished = false;
	
	public SetupScreen(Frame frame) {
		this.frame = frame;
	}
	
	public abstract void cancel();
	
	public void finish() {
		finished = true;
	}
}
