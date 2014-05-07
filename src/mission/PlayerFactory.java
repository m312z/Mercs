package mission;

import gui.GameGUI;

import java.util.ArrayList;
import java.util.List;

import mission.gameobject.Component;
import mission.gameobject.ComponentShapes;
import mission.gameobject.Mech;
import mission.gameobject.Player;
import mission.powers.Power;
import mission.shield.Shield;
import mission.weapon.Weapon.ShotType;
import mission.weapon.WeaponFactory;
import phys.Point2D;

public class PlayerFactory {

	public static Player makePlayer(String pilotName, int id, Power[] powers) {
		
		// components
		List<Component> list = new ArrayList<Component>(7);
		Component c = new Component(ComponentShapes.ship1[0], new Point2D(),-1);
		c.setColour(GameGUI.playerLight[id]);
		list.add(c);
		
		Player p = new Player(pilotName, id, list,
				new Point2D(Board.BOARD_SIZE/2, 4*Board.BOARD_SIZE/5),
				new Point2D(), Mech.MECH_SPEED);
		
		// powers
		for(int i=0; i < 6; i++) {
			if(powers[i]!=null) {
				switch(powers[i]) {
				case SHIELD_FAT:
					if(p.getShield()==null)
						p.setShield(new Shield(4,1/120f));
					else {
						p.getShield().setMaxCapacity(p.getShield().getMaxCapacity() + 4);
						p.getShield().setCurrentCapacity(p.getShield().getMaxCapacity());
						p.getShield().addRechargeRate(1/120f);
					}	
					break;
				case SHIELD_FAST:
					if(p.getShield()==null)
						p.setShield(new Shield(2,1/60f));
					else {
						p.getShield().setMaxCapacity(p.getShield().getMaxCapacity() + 2);
						p.getShield().setCurrentCapacity(p.getShield().getMaxCapacity());
						p.getShield().addRechargeRate(1/60f);
					}	
					break;
				default:
					p.getPowerEater().addActivePower(powers[i]);
					break;
				}
			}
		}
		
		// weapon
		p.setPlayerWeapon(WeaponFactory.makeWeapon(p,ShotType.STRAIGHTDUAL));
		
		return p;
	}
}
