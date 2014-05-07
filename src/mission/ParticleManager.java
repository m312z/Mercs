package mission;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import mission.gameobject.Mech;
import mission.weapon.bullet.Bullet;
import phys.DefaultShapes;
import phys.Point2D;
import phys.Shape;

public class ParticleManager {
	
	public static ArrayList<Particle> particles = new ArrayList<Particle>();
	public static final int max_particles = 1000;
	
	public static void makeSplash(Shape shape, Color colour, Point2D pos, float life) {
		if(particles.size()>max_particles) return;
		for(int i=0;i<10;i++) {
			particles.add(
					new Particle(
							shape.clone(),
							life,new Color[] {colour},
						new Point2D(pos.x,pos.y),
						new Point2D((float)(Math.random()*2-1)*0.5f,(float)(Math.random()*2-1)*0.5f),0f));
		}
	}
	

	static Color explosion = new Color(255,140,0,155);
	public static void makeExplosion(Point2D pos) {
		if(particles.size()>max_particles) return;
		for(int i=0;i<6;i++) {
			particles.add(
					new Particle(
							Shape.scale(DefaultShapes.basicHex,((float)Math.random()+1)*Mech.MECH_RADIUS/2f),
							20,new Color[] {explosion},
						new Point2D(pos.x,pos.y),
						new Point2D((float)(Math.random()*2-1)*0.3f,(float)(Math.random()*2-1)*0.3f),
						(float) (Math.random()*3-6)));
		}
		for(int i=0;i<4;i++) {
			particles.add(
					new Particle(
							Shape.scale(DefaultShapes.basicHex,((float)Math.random()+1)*Mech.MECH_RADIUS/2f),
							20,new Color[] {smoke},
						new Point2D(pos.x,pos.y),
						new Point2D((float)(Math.random()*2-1)*0.3f,(float)(Math.random()*2-1)*0.3f),
						(float) (Math.random()*3-6)));
		}
	}
	
	public static void makeTinyExplosion(Point2D pos) {
		if(particles.size()>max_particles) return;
		for(int i=0;i<2;i++) {
			particles.add(
					new Particle(
							Shape.scale(DefaultShapes.basicHex,(float) ((Math.random()+1)*Bullet.BULLET_RADIUS)),
							Bullet.BULLET_RADIUS*7,new Color[] {explosion},
						new Point2D(pos.x,pos.y),
						new Point2D((float)(Math.random()*2-1)*0.3f,(float)(Math.random()*2-1)*0.3f),
						(float) (Math.random()*3-6)));
		}
	}
	
	static Color smoke = new Color(100,100,100,155);
	public static void makeSmoke(Point2D pos, float radius) {
		if(particles.size()>max_particles) return;
			particles.add(
				new Particle(
						Shape.scale(DefaultShapes.basicHex,(float)((Math.random()+0.5)*Bullet.BULLET_RADIUS*2)),
						60,new Color[] {smoke},
					new Point2D(pos.x+(float)(Math.random()*2-1)*radius,pos.y+(float)(Math.random()*2-1)*radius),
					new Point2D((float)Math.random()*0.2f-0.1f,(float)Math.random()*0.2f-0.1f),
					(float)Math.random()*3f-3));
	}

	static Color fire = new Color(255,70,0,155);
	public static void makeFire(Point2D pos, float radius) {
		if(particles.size()>max_particles) return;
			particles.add(
				new Particle(
						Shape.scale(DefaultShapes.basicHex,(float)((Math.random()+0.2)*Bullet.BULLET_RADIUS*2)),
						60,new Color[] {smoke,explosion,fire},
					new Point2D(pos.x+(float)(Math.random()*2-1)*radius,pos.y+(float)(Math.random()*2-1)*radius),
					new Point2D((float)Math.random()*0.1f-0.05f,(float)Math.random()*0.1f-0.05f),
					(float)Math.random()*3f-3));
	}
	
	public static void makeCircleDrift(Shape shape, Color colour, Point2D pos, float life) {
		if(particles.size()>max_particles) return;
		for(int i=0;i<6;i++) {
			particles.add(
					new Particle(
							shape.clone(),
							life,new Color[] {colour},
						new Point2D(pos.x,pos.y),
						new Point2D(
								(float) Math.cos(Math.toRadians(i*60))*0.2f,
								(float) Math.sin(Math.toRadians(i*60))*0.2f),0f));
		}
	}
	
	public static void tick(Board map, float dt) {
		
		// move particles
		Iterator<Particle> it = particles.iterator();
		while(it.hasNext()) {
			Particle p = it.next();
			if(p.tick(map, dt))
				it.remove();
		}
	}

	public static ArrayList<Particle> getParticles() {
		return particles;
	}
	
	/**
	 * Particle class
	 * @author cashmore
	 *
	 */
	public static class Particle {
		
		Shape shape;
		Color[] colour;
		Point2D pos;
		Point2D vel;
		float rotation;
		float lifetime;
		float maxLife;
		
		public Particle(Shape shape, float lifetime, Color[] colour, Point2D pos, Point2D vel, float rotation) {
			this.shape = shape;
			this.maxLife = this.lifetime = lifetime;			
			this.colour = colour;
			this.pos = pos;
			this.vel = vel;
			this.rotation = rotation;
		}

		public boolean tick(Board board, float dt) {
			
			// do movement
			pos.x += dt*vel.x;
			pos.y += dt*vel.y;
						
			shape.rotate(dt*rotation);
			
			// decay
			lifetime -= dt;
			return (lifetime<0);
		}
		
		public Color getColour() {
			int i = (int)(colour.length*lifetime/maxLife);
			if(i==colour.length)i--;
			return colour[i];
		}
		
		public Point2D getPos() {
			return pos;
		}
		
		public Shape getShape() {
			return shape;
		}
	}
}
