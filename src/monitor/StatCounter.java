package monitor;

public class StatCounter {

	static final int maxScore = 999999999;
	
	// external things that count
	float distance;
	int score;
	int[] damageDealt;
	int enemiesDestroyed;
	int[] asteroidsBroken;
	int[] bulletsFired;
	float[] damageTaken;
	float fireDamage;

	public StatCounter() {
		damageDealt = new int[2];
		asteroidsBroken = new int[2];
		bulletsFired = new int[2];
		damageTaken = new float[2];
	}
	
	public void addScore(int score) {
		this.score += score;
		if(score > maxScore) score = maxScore;
	}
	
	public int getScore() {
		return score;
	}
	 
	public void addDamageTaken(int player, float damage) {
		damageTaken[player] += damage;
	}
	
	public void addBulletsFired(int player, int amt) {
		bulletsFired[player] += amt;
	}
	
	public void addAsteroidsBroken(int player, int amt) {
		asteroidsBroken[player] += amt;
	}
	
	public void addEnemiesDestroyed(int amt) {
		enemiesDestroyed += amt;
	}
	
	public void addDamageDealt(int player, int amt) {
		damageDealt[player] += amt;
	}
	
	public void addFireDamage(float damage) {
		fireDamage += damage;
	}
	
	public float getFireDamage() {
		return (int)fireDamage;
	}
	
	public int[] getAsteroidsBroken() {
		return asteroidsBroken;
	}
	
	public int[] getBulletsFired() {
		return bulletsFired;
	}
	
	public int[] getDamageDealt() {
		return damageDealt;
	}
	
	public int[] getDamageTaken() {
		return new int[] {(int)damageTaken[0], (int)damageTaken[1]};
	}
	
	public int getEnemiesDestroyed() {
		return enemiesDestroyed;
	}
	
	public float getDistance() {
		return distance;
	}

	public void addTime(float dt) {
		distance += dt;
	}
}
