package mission.powers;

public enum Power {

	GARGANTUA(0, true, 1800,600, "gargantua"),
	MINEPOWER(1, true, 60,0, "mine"),
	RAPIDFIRE(2, true, 1800,600, "rapid"),
	HOMINGPOWER(3, true, 1800,600, "homing"),
	FIREPOWER(4, true, 1800,1200, "fire"),
	
	CLUSTERPOWER(5, true, 3600,600, "cluster"),
	MEGAPOWER(6, true, 3600,120, "mega"),
	SPIRALPOWER(7, true, 1800,600, "spiral"),
	SHIELD_CHARGING(8, true, 1800,600, "charge"),
	IONBEAMS(9, true, 2400,0, "ionbeam"),

	CLONE(10, true, 10,0, "clone"),
	SUPPORT_DRONE(11, true, 300,2400, "drone"),
	SUPPORT_DECOY(12, true, 1800,600, "decoy"),
	TELEPORT(13, true, 60,0, "teleport"),
	DEFENCELASER(14, true, 3600,600, "defencebeams"),
	
	CLONE_SELFDESTRUCT(15, true, 60,0, "clonedestroy"),
	SUPPORT_KILLSTAR(16, true, 3600,600, "killstar"),
	SLOW(17, true, 3600,600, "slow"),
	PORT_AUTODEFENCE(18, false, 0,0, "portdefence"),
	LASER_MICROTARGETING(19, false, 0,0, "microtargeting"),
	
	CLONE_GREATERCLONE(20, false, 0,0, "greaterclone"),
	SUPPORT_GADGETS(21, false, 1800,0, "gadgets"),
	SUPPORT_DRONEMASTER(22, false, 0,0, "supportcommander"),
	PORT_EXPLOSION(23, false, 0,0, "portexplosion"),
	LASER_ASTEROIDTARGETING(24, false, 0,0, "asteroidtargeting"),
	
	DEFLECTORSHIELD(25, false, 0,0, "deflection"),
	EXPLOSIVECHARGE(26, false, 360,360, "explosivecharge"),
	BULLETREFLECT(27, false, 0,0, "reflect"),
	SHIELD_FAT(28, false, 0,0, "shieldfat"),
	SHIELD_FAST(29, false, 0,0, "shieldfast");

	public int id;
	public boolean active;
	public float coolDown;
	public int duration;
	public String image;
	
	
	private Power(int id, boolean active, float coolDown, int duration, String image) {
		this.id = id;
		this.active = active;
		this.coolDown = coolDown;
		this.duration = duration;
		this.image = image;
	}
}