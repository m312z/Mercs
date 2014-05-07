package mission.effects;

import mission.weapon.bullet.Bullet;
import phys.Point2D;

public class Laser {
	
	public static final float LASER_WIDTH = Bullet.BULLET_RADIUS*4f;

	public boolean doubleEnded = false;
	public boolean bright = true;
	
	public Point2D start;
	public Point2D end;
	public float duration;
	public float timer;
	public float width;
	
	/* glow colour, 0 to 1 */
	public float r = 1f;
	public float g = 0f;
	public float b = 0f;
	
	public Laser(Point2D start, Point2D end, float duration, float width) {
		this.duration = duration;
		this.width = width;
		this.start = start;
		this.end = end;
		this.timer = 0;
	}
		
	public float length() {
		return (float) Math.sqrt((start.y-end.y)*(start.y-end.y) + 
				(start.x-end.x)*(start.x-end.x));
	}
}
