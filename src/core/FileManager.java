package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import mission.map.LevelMap;
import mission.map.LevelMap.LevelType;
import mission.powers.Power;
import monitor.PowerShop;
import monitor.StatCounter;

public class FileManager {

	public class LevelScore
	{
		public float distance;
		public int score = 0;
		public int damageDealt = 0;
		public int enemiesDestroyed = 0;
		public int asteroidsBroken = 0;
		public int bulletsFired = 0;
		public float damageTaken = 0;
		public float fireDamage = 0;
		
		public int coop_damageDealt = 0;
		public int coop_asteroidsBroken = 0;
		public int coop_bulletsFired = 0;
		public float coop_damageTaken = 0;
	}
	
	Map<LevelType,LevelScore> scores;
	int[] player1Powers = new int[6];
	int[] player2Powers = new int[6];
	
	public void readPowers() {
				
		// read old scores
		File file = new File("resource/powers");
		if(file.exists()) { 
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = reader.readLine();
				for(int i=0;i<6;i++) {
					player1Powers[i] = Integer.parseInt(line);
					line = reader.readLine();
				}
				for(int i=0;i<6;i++) {
					player2Powers[i] = Integer.parseInt(line);
					line = reader.readLine();
				}
				reader.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void readHighScores() {
		
		scores = new HashMap<LevelMap.LevelType, FileManager.LevelScore>();
		
		// read old scores
		File file = new File("resource/highscores");
		if(file.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				for(LevelType lt: LevelType.values()) {
					LevelScore ls = new LevelScore();
					String line = reader.readLine();
					ls.score = Integer.parseInt(line);
					line = reader.readLine();
					ls.distance = Float.parseFloat(line);
					line = reader.readLine();
					ls.damageDealt = Integer.parseInt(line);
					line = reader.readLine();
					ls.enemiesDestroyed = Integer.parseInt(line);
					line = reader.readLine();
					ls.asteroidsBroken = Integer.parseInt(line);
					line = reader.readLine();
					ls.bulletsFired = Integer.parseInt(line);
					line = reader.readLine();
					ls.damageTaken = Float.parseFloat(line);
					line = reader.readLine();
					ls.fireDamage = Float.parseFloat(line);
					
					line = reader.readLine();
					ls.coop_damageDealt = Integer.parseInt(line);
					line = reader.readLine();
					ls.coop_asteroidsBroken = Integer.parseInt(line);
					line = reader.readLine();
					ls.coop_bulletsFired = Integer.parseInt(line);
					line = reader.readLine();
					ls.coop_damageTaken = Float.parseFloat(line);
					scores.put(lt,ls);
				}
				
				// unlocked powers
				String line = reader.readLine();
				while(line!=null) {
					PowerShop.unlock(Power.values()[Integer.parseInt(line)]);
					line = reader.readLine();
				}
				
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void savePowers(Set<Integer> p0, Set<Integer> p1) {
		File file = new File("resource/powers");
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(file));
			
			int i=0;
			Iterator<Integer> it = p0.iterator();
			while(it.hasNext()) {
				writer.println(it.next());
				i++;
			}
			for(;i<6;i++)
				writer.println(-1);
			
			i=0;
			it = p1.iterator();
			while(it.hasNext()) {
				writer.println(it.next());
				i++;
			}
			for(;i<6;i++)
				writer.println(-1);
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveHighResults(StatCounter results, LevelType levelType) {
		
		// write scores
		File file = new File("resource/highscores");
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(file));
				
			for(LevelType lt: LevelType.values()) {
				
				LevelScore ls = scores.get(lt);
				
				if(levelType == lt) {
					// find best scores
					for(int i=0;i<2;i++) {
						if(results.getDamageDealt()[i]>ls.damageDealt)
							ls.damageDealt = results.getDamageDealt()[i];
						if(results.getAsteroidsBroken()[i]>ls.asteroidsBroken)
							ls.asteroidsBroken = results.getAsteroidsBroken()[i];
						if(results.getBulletsFired()[i]>ls.bulletsFired)
							ls.bulletsFired = results.getBulletsFired()[i];
						if(results.getDamageTaken()[i]>ls.damageTaken)
							ls.damageTaken = results.getDamageTaken()[i];
					}
				
					if(results.getDistance()>ls.distance)
						ls.distance = results.getDistance();
					if(results.getScore()>ls.score)
						ls.score = results.getScore();
					if(results.getEnemiesDestroyed()>ls.enemiesDestroyed)
						ls.enemiesDestroyed = results.getEnemiesDestroyed();
					if(results.getFireDamage()>ls.fireDamage)
						ls.fireDamage = results.getFireDamage();
					
					if(results.getDamageDealt()[0]+results.getDamageDealt()[1]>ls.coop_damageDealt)
						ls.coop_damageDealt = results.getDamageDealt()[0]+results.getDamageDealt()[1];
					if(results.getAsteroidsBroken()[0]+results.getAsteroidsBroken()[1]>ls.coop_asteroidsBroken)
						ls.coop_asteroidsBroken = results.getAsteroidsBroken()[0]+results.getAsteroidsBroken()[1];
					if(results.getBulletsFired()[0]+results.getBulletsFired()[1]>ls.coop_bulletsFired)
						ls.coop_bulletsFired = results.getBulletsFired()[0]+results.getBulletsFired()[1];
					if(results.getDamageTaken()[0]+results.getDamageTaken()[1]>ls.coop_damageTaken)
						ls.coop_damageTaken = results.getDamageTaken()[0]+results.getDamageTaken()[1];
				}
				
				writer.println(ls.score);
				writer.println(ls.distance);
				writer.println(ls.damageDealt);
				writer.println(ls.enemiesDestroyed);
				writer.println(ls.asteroidsBroken);
				writer.println(ls.bulletsFired);
				writer.println(ls.damageTaken);
				writer.println(ls.fireDamage);
				
				writer.println(ls.coop_damageDealt);
				writer.println(ls.coop_asteroidsBroken);
				writer.println(ls.coop_bulletsFired);
				writer.println(ls.coop_damageTaken);
			}
			

			// unlocked powers
			for(int i=0;i<Power.values().length;i++) {
				if(PowerShop.isPowerUnlocked(i))
					writer.println(i);
			}
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public LevelScore getLevelScore(LevelType levelType) {
		return scores.get(levelType);
	}
	
	public int[] getPlayer1Powers() {
		return player1Powers;
	}
	
	public int[] getPlayerPowers(int player) {
		if(player==0)
			return player1Powers;
		return player2Powers;
	}
}
