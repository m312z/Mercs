package monitor;

import gui.GameGUI;

import java.awt.Color;

import mission.Board;
import mission.boss.BuilderBaseBoss;
import mission.boss.HoneycombBoss;
import mission.effects.WordManager;
import mission.gameobject.Mech;
import mission.gameobject.Player;
import mission.map.LevelMap.LevelType;
import mission.powers.Power;
import phys.Point2D;

public class PowerShop {
	
	static boolean[] powerUnlocked = new boolean[Power.values().length];
	
	/* unlock achievements */
	public static boolean[] shieldsDownButNotOut = {false,false};
	public static boolean projectileLimitReached = false; 
	public static float[] survivalTime = {0f, 0f};
	
	public enum Challenge
	{
		SURVIVE(
				GameGUI.player[0], "challenge_survive",
				new Power[] {Power.DEFLECTORSHIELD,Power.SUPPORT_DRONE,Power.RAPIDFIRE}),
		CLOSECALL(
				GameGUI.player[1], "challenge_closecall",
				new Power[] {Power.FIREPOWER,Power.CLUSTERPOWER,Power.MINEPOWER}),
		MODS(
				GameGUI.energy, "challenge_mods",
				new Power[] {Power.CLONE,Power.TELEPORT,Power.DEFENCELASER}),
		PROJECTILES(
				GameGUI.enemyDarkest, "challenge_projectiles",
				new Power[] {Power.SHIELD_FAST,Power.EXPLOSIVECHARGE,Power.BULLETREFLECT}),
		CLONEBOSS(
				GameGUI.enemyLight, "challenge_cloneboss",
				new Power[] {Power.SHIELD_CHARGING,Power.GARGANTUA,Power.MEGAPOWER}),
		CLONEBUILDER(
				BuilderBaseBoss.builderBossColor, "challenge_clonebuilder",
				new Power[] {Power.PORT_AUTODEFENCE,Power.SUPPORT_GADGETS}),
		HIVEBASE(
				HoneycombBoss.darker, "challenge_hivebase",
				new Power[] {Power.CLONE_GREATERCLONE,Power.SUPPORT_KILLSTAR,Power.IONBEAMS}),
		HIVEBOSS(
				HoneycombBoss.honey, "challenge_hiveboss",
				new Power[] {Power.SUPPORT_DRONEMASTER,Power.SLOW}),
		BUILDERFORM(
				BuilderBaseBoss.builderBossColorDark[2], "challenge_builderform",
				new Power[] {Power.LASER_ASTEROIDTARGETING,Power.CLONE_SELFDESTRUCT,Power.PORT_EXPLOSION}),
		BUILDERBOSS(
				BuilderBaseBoss.builderBossColorDark[0], "challenge_builderboss",
				new Power[] {Power.LASER_MICROTARGETING,Power.SUPPORT_DECOY});
		
		public Color color;
		public String image;
		public Power[] powers;
		
		Challenge(Color color, String image, Power[] powers) {
			this.color = color;
			this.image = image;
			this.powers = powers;
		}
	}
	
	public static boolean isPowerUnlocked(int p) {
		return powerUnlocked[p];
	}
	
	public static void unlock(Power p) {
		for(int i=0;i<Power.values().length;i++)
			if(Power.values()[i] == p)
				powerUnlocked[i] = true;
	}
	
	public static void resetAchievementMonitor() {
		shieldsDownButNotOut[0] = false;
		shieldsDownButNotOut[1] = false;
		projectileLimitReached = false;
		survivalTime[0] = 0;
		survivalTime[1] = 0;
	}
	
	public static void monitorAchievements(Board board) {
		
		// more than 2000 projectiles
		if(!projectileLimitReached && board.getFriendlyBullets().size() > 1999) {
			projectileLimitReached = true;
			if(!isPowerUnlocked(Power.BULLETREFLECT.id))
				WordManager.addWord("UNLOCKED: Advanced Shielding (3 new powers!)",new Point2D(0,Mech.MECH_RADIUS),200,true);
			unlock(Power.SHIELD_FAST);
			unlock(Power.EXPLOSIVECHARGE);
			unlock(Power.BULLETREFLECT);
		}
		
		// reach hive base
		if(board.getMap().getLevelType() == LevelType.THEHIVE && board.getMap().getLevel() == 5) {
			if(!isPowerUnlocked(Power.IONBEAMS.id))
				WordManager.addWord("UNLOCKED: Extending the Demolition Limit (3 new powers!)",new Point2D(0,Mech.MECH_RADIUS),200,true);
			unlock(Power.CLONE_GREATERCLONE);
			unlock(Power.SUPPORT_KILLSTAR);
			unlock(Power.IONBEAMS);
		}
		
		// reach the hive
		if(board.getMap().getLevelType() == LevelType.THEHIVE && board.getMap().getLevel() == 10) {
			if(!isPowerUnlocked(Power.SLOW.id))
				WordManager.addWord("UNLOCKED: Combat Tradeoffs (2 new powers!)",new Point2D(0,Mech.MECH_RADIUS),200,true);
			unlock(Power.SUPPORT_DRONEMASTER);
			unlock(Power.SLOW);
		}

		// reach builder boss (stage 1)
		if(board.getMap().getLevelType() == LevelType.BUILDERCOLONY && board.getMap().getLevel() == 3) {
			if(!isPowerUnlocked(Power.CLONE_SELFDESTRUCT.id))
				WordManager.addWord("UNLOCKED: Acceptable Losses (3 new powers!)",new Point2D(0,Mech.MECH_RADIUS),200,true);
			unlock(Power.LASER_ASTEROIDTARGETING);
			unlock(Power.PORT_EXPLOSION);
			unlock(Power.CLONE_SELFDESTRUCT);
		}
		
		// reach builder boss (stage 4)
		if(board.getMap().getLevelType() == LevelType.BUILDERCOLONY && board.getMap().getLevel() == 10) {
			if(!isPowerUnlocked(Power.SUPPORT_DECOY.id))
				WordManager.addWord("UNLOCKED: Automated Targeting (2 new powers!)",new Point2D(0,Mech.MECH_RADIUS),200,true);
			unlock(Power.LASER_MICROTARGETING);
			unlock(Power.SUPPORT_DECOY);
		}
		
		// clone a boss
		for(Mech m: board.getMechs()) {
			if(m.getComponents().size() > 6) {
				if(!isPowerUnlocked(Power.MEGAPOWER.id))
					WordManager.addWord("UNLOCKED: Projectiles: revisited (3 new powers!)",new Point2D(0,Mech.MECH_RADIUS),200,true);
				unlock(Power.SHIELD_CHARGING);
				unlock(Power.GARGANTUA);
				unlock(Power.MEGAPOWER);
			}
		}

		for(Player p:board.getPlayers()) {
			
			// clone a builder and get an extra gun
			if(p.getComponents().size() > 1) {
				if(!isPowerUnlocked(Power.SUPPORT_GADGETS.id))
					WordManager.addWord("UNLOCKED: DDDs: Decomposition of Malice (2 new powers!)",new Point2D(0,Mech.MECH_RADIUS),200,true);
				unlock(Power.PORT_AUTODEFENCE);
				unlock(Power.SUPPORT_GADGETS);
			}
			
			// two bullet mods together
			if(p.getBullet().getMods().size()>1) {
				if(!isPowerUnlocked(Power.DEFENCELASER.id))
					WordManager.addWord("UNLOCKED: Theoretical Combat (3 new powers!)",new Point2D(0,Mech.MECH_RADIUS),200,true);
				unlock(Power.CLONE);
				unlock(Power.TELEPORT);
				unlock(Power.DEFENCELASER);
			}
		}
		
		// survive a close call
		for(Player p : board.getTotalPlayers()) {
			if(p.getShield()!=null) {
				if(p.getShield().getCurrentCapacity()==0)
					shieldsDownButNotOut[p.getId()] = true;
				if(board.getDeadPlayers().contains(p) || board.getRespawnQueue().containsKey(p))
					shieldsDownButNotOut[p.getId()] = false;
				if(shieldsDownButNotOut[p.getId()] && (p.getShield().getCurrentCapacity()>1)) {
					shieldsDownButNotOut[p.getId()] = false;
					if(!isPowerUnlocked(Power.MINEPOWER.id))
						WordManager.addWord("UNLOCKED: Projectiles (3 new powers!)",new Point2D(0,Mech.MECH_RADIUS),200,true);
					unlock(Power.FIREPOWER);
					unlock(Power.CLUSTERPOWER);
					unlock(Power.MINEPOWER);
					WordManager.addWord("CLOSE CALL!",new Point2D(p.getPos().x,p.getPos().y),120);
				}
			}
			
			// survive 100 seconds
			if(board.getDeadPlayers().contains(p) || board.getRespawnQueue().containsKey(p))
				survivalTime[p.getId()] = board.getTime();
			if(!isPowerUnlocked(Power.RAPIDFIRE.id) && board.getTime()-survivalTime[p.getId()] > 10000) {
				WordManager.addWord("UNLOCKED: A Novel Approach to Encroaching (3 new powers!)",new Point2D(0,Mech.MECH_RADIUS),200,true);
				unlock(Power.DEFLECTORSHIELD);
				unlock(Power.SUPPORT_DRONE);
				unlock(Power.RAPIDFIRE);
			}
		}
	}
}