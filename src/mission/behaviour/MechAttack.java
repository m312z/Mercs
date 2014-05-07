package mission.behaviour;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mission.Board;
import mission.gameobject.Component;

public class MechAttack {
		
	List<Volley> shootCycle;
	Volley currentVolley;
	float timer;
	
	public MechAttack() {
		shootCycle = new ArrayList<Volley>();
		currentVolley = null;
	}

	public void tick(float dt, Board board) {
		
		// no volleys
		if(shootCycle.size()==0)
			return;
		
		if(currentVolley == null || !shootCycle.contains(currentVolley))
			currentVolley = shootCycle.get(0);
				
		timer += dt; 
		
		// next volley
		if(timer>currentVolley.fireTime + currentVolley.powerupTime) {
			int i=(shootCycle.indexOf(currentVolley)+1)%shootCycle.size();
			currentVolley = shootCycle.get(i);
			timer = 0;
		}
	}
	
	public List<Volley> getShootCycle() {
		return shootCycle;
	}
		
	public boolean isShooting() {
		if(currentVolley!=null)
			return timer > currentVolley.powerupTime;
		return false;
	}
	
	public boolean isPoweringUp() {
		if(currentVolley!=null)
			return (timer > 0 && timer <= currentVolley.powerupTime);
		return false;
	}
	
	public float getPowerupTime() {
		if(currentVolley!=null)
			return currentVolley.powerupTime;
		return 0;
	}
	
	public float getRemainingPowerupTime() {
		if(currentVolley!=null)
			return currentVolley.powerupTime - timer;
		return 0;
	}
	
	public Set<Component> getCurrentComponents() {
		if(currentVolley!=null)
			return currentVolley.components;
		return new HashSet<Component>(0);
	}
	
	public void addVolley(Volley v) {
		shootCycle.add(v);
	}
	
	public void removeVolley(Component member) {
		Iterator<Volley> vit = shootCycle.iterator();
		while(vit.hasNext()) {
			Volley v = vit.next();
			if(v.components.contains(member))
				vit.remove();
		}
		if(currentVolley!=null && shootCycle.size()>0)
			currentVolley = shootCycle.get(0);
	}
	

	public void removeComponent(Component c) {
		Iterator<Volley> vit = shootCycle.iterator();
		while(vit.hasNext()) {
			Volley v = vit.next();
			Iterator<Component> cit = v.components.iterator();
			while(cit.hasNext())
				if(cit.next() == c)
					cit.remove();
		}
	}
	
	public void removeEmptyVolleys() {
		boolean removed = false;
		Iterator<Volley> vit = shootCycle.iterator();
		while(vit.hasNext()) {
			Volley v = vit.next();
			if(v.components.isEmpty()) {
				if(v == currentVolley)
					removed = true;
				vit.remove();
			}
		}
		if(currentVolley!=null && removed && shootCycle.size()>0)
			currentVolley = shootCycle.get(0);
	}

	public class Volley
	{
		public Set<Component> components;
		public float powerupTime;
		public float fireTime;
		
		public Volley(List<Component> components, float powerupTime, float time) {
			this.components = new HashSet<Component>();
			this.components.addAll(components);
			this.powerupTime = powerupTime;
			this.fireTime = time;
		}
		
		public Volley(Component[] components, float powerupTime, float time) {
			this.components = new HashSet<Component>();
			for(int i=0;i<components.length;i++)
				this.components.add(components[i]);
			this.powerupTime = powerupTime;
			this.fireTime = time;
		}
	}
}
