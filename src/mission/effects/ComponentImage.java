package mission.effects;

import java.util.List;

import mission.Board;
import mission.gameobject.Component;
import phys.Point2D;

public class ComponentImage {
	
	List<Component> image;
	Point2D pos;
	Point2D vel;
	boolean dead = false;
	
	public ComponentImage(List<Component> image, Point2D pos, Point2D vel) {
		this.image = image;
		this.pos = pos;
		this.vel = vel;
	}
	
	public void tick(float dt) {
		pos.x += vel.x*dt;
		pos.y += vel.y*dt;
		
		if(pos.x < -Board.BOARD_SIZE || pos.x > 2*Board.BOARD_SIZE)
			dead = true;
		if(pos.y < -Board.BOARD_SIZE || pos.y > 2*Board.BOARD_SIZE)
			dead = true;
	}
	
	public boolean isDead() {
		return dead;
	}
	
	public List<Component> getImage() {
		return image;
	}
	
	public Point2D getPos() {
		return pos;
	}
}
