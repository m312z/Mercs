package mission.shield;

import mission.Board;

public class Shield {
	
	float maxCapacity;
	float currentCapacity;
	float rechargeRate;
	float rechargeTimer; 
	
	public Shield(float maxCapacity, float rechargeRate) {
		this.maxCapacity = maxCapacity;
		this.currentCapacity = maxCapacity;
		this.rechargeRate = rechargeRate;
		this.rechargeTimer = 0;
	}
	
	public float damage(Board board, float amnt) {
		float remaining = 0;
		// absorb damage with shields
		if(currentCapacity > amnt) {
			currentCapacity-= amnt;
		} else {
			remaining = amnt - currentCapacity;
			currentCapacity = 0;
		}
		// return remaining damage
		return remaining;
	}
	
	public void tick(float dt, Board board) {
		// charge shields
		rechargeTimer += dt*rechargeRate;
		if(rechargeTimer>1) {
			currentCapacity++;
			rechargeTimer = rechargeTimer%1;
			if(currentCapacity > maxCapacity)
				currentCapacity = maxCapacity;
		}
	}
	
	public float getCurrentCapacity() {
		return currentCapacity;
	}
	
	public float getMaxCapacity() {
		return maxCapacity;
	}
	
	public float getRechargeRate() {
		return rechargeRate;
	}
	
	public void setCurrentCapacity(float currentCapacity) {
		if(currentCapacity > maxCapacity)
			this.currentCapacity = maxCapacity;
		else if(currentCapacity < 0)
			this.currentCapacity = 0;
		else
			this.currentCapacity = currentCapacity;
	}
	
	public void setMaxCapacity(float maxCapacity) {
		this.maxCapacity = maxCapacity;
	}
	
	public void setRechargeRate(float rechargeRate) {
		this.rechargeRate = rechargeRate;
	}

	public void addRechargeRate(float addedRate) {
		rechargeRate = (rechargeRate + addedRate)/2f;
	}
}
