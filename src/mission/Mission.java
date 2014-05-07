package mission;

import static core.Frame.SCREEN_SIZE;
import static gui.GameGUI.VIEW_SIZE;
import gui.FontManager;
import gui.FontManager.FontType;
import gui.GameGUI;
import gui.ui.ButtonElement;
import gui.ui.HudElement;
import gui.ui.HudElement.InteractionType;
import gui.ui.HudOverlay;
import gui.ui.TextElement;

import java.awt.Color;
import java.util.List;
import java.util.TreeMap;

import mission.gameobject.Mech;
import mission.gameobject.Player;
import mission.map.LevelMap.LevelType;
import monitor.PowerShop;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import phys.Point2D;
import phys.Shape;
import sound.SoundManager;
import core.Frame;
import core.controller.MouseController;
import core.controller.ShipController;


/**
 * The main loop for the game.  Also contains some static fields
 * that are useful everywhere.
 * @author Michael Cashmore
 *
 */
public class Mission {
	
	/* Desired frame time in milliseconds (1000/desired FPS) */
	public static float GAMESPEED =100/6f;
	
	Frame frame;
	
	/* game over */
	boolean finished = false;
	boolean restartRequested = false;
	float gameOverCountdown = 180;
	/* game paused */
	boolean running = true;
	
	/* graphics and model */
	HudOverlay overlay;
	GameGUI gui;
	Board board;
	long lastTick;
	long lastFPS = 0;
	int fps;
	
	/* True if key has been pressed, but not processed */
	boolean hotPause = true;
	boolean[] hotSelect = {true,true};
	
	/* players */
	TreeMap<String,ShipController> controllers;
	
	public Mission(Frame frame, LevelType levelType) {
		this.frame = frame;
		this.gui = new GameGUI();
		this.overlay = new HudOverlay();
		this.controllers = new TreeMap<String,ShipController>();
		this.board = new Board(levelType);
		
		Shape tbs = new Shape(new Point2D[] {
		});
		
		float offX = 0f;
		if(SCREEN_SIZE[0]/VIEW_SIZE > SCREEN_SIZE[1]/VIEW_SIZE) {
			offX = (SCREEN_SIZE[0] - 10*SCREEN_SIZE[1]/9f)/2f + SCREEN_SIZE[1]/9f;
		}
		HudElement scoreText = new TextElement("scoreText", tbs,
				new Point2D(offX,0), "SCORE: 000000000", Color.BLACK, FontType.FONT_32);
		HudElement livesText = new TextElement("livesText", tbs,
				new Point2D(offX + FontManager.getFont(FontType.FONT_32).getWidth("SCORE: 000000000   ")*Frame.FONT_SCALE,0),
				"LIVES: 000", Color.BLACK, FontType.FONT_32);
		HudElement levelText = new TextElement("levelText", tbs,
				new Point2D(offX + FontManager.getFont(FontType.FONT_32).getWidth("SCORE: 000000000   LIVES: 000   ")*Frame.FONT_SCALE,0),
						"LEVEL: 00", Color.BLACK, FontType.FONT_32);
		overlay.addElement(scoreText);
		overlay.addElement(livesText);
		overlay.addElement(levelText);
		
		float hs = SCREEN_SIZE[1]/10;
		Shape bs = new Shape(new Point2D[] {
				new Point2D(-hs*2,-hs/2),
				new Point2D( hs*2,-hs/2),
				new Point2D( hs*2, hs/2),
				new Point2D(-hs*2, hs/2)
		});
		
		ButtonElement startButton = new ButtonElement("start_button", bs, new Point2D(SCREEN_SIZE[0]/2, SCREEN_SIZE[1]/2 - hs*2), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		startButton.addCommand(InteractionType.MOUSE_DOWN, "start");
		startButton.addElement(new TextElement("sbt", bs, new Point2D(0,0), "RESUME", FontType.FONT_32));
		startButton.setVisible(false);
		
		ButtonElement soundButton = new ButtonElement("sound_button", bs, new Point2D(SCREEN_SIZE[0]/2, SCREEN_SIZE[1]/2 - hs), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		soundButton.addCommand(InteractionType.MOUSE_DOWN, "sound");
		soundButton.addElement(new TextElement("sbt_on", bs, new Point2D(), "SOUNDS ARE ON", FontType.FONT_32));
		soundButton.addElement(new TextElement("sbt_off", bs, new Point2D(), "SOUNDS ARE OFF", FontType.FONT_32));
		soundButton.setVisible(false);
		
		ButtonElement musicButton = new ButtonElement("music_button", bs, new Point2D(SCREEN_SIZE[0]/2, SCREEN_SIZE[1]/2), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		musicButton.addCommand(InteractionType.MOUSE_DOWN, "music");
		musicButton.addElement(new TextElement("mbt_on", bs, new Point2D(), "MUSIC IS ON", FontType.FONT_32));
		musicButton.addElement(new TextElement("mbt_off", bs, new Point2D(), "MUSIC IS OFF", FontType.FONT_32));
		musicButton.setVisible(false);
		
		ButtonElement restartButton = new ButtonElement("restart_button", bs, new Point2D(SCREEN_SIZE[0]/2, SCREEN_SIZE[1]/2 + hs), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		restartButton.addCommand(InteractionType.MOUSE_DOWN, "restart");
		restartButton.addElement(new TextElement("rbt", bs,new Point2D(), "RESTART", FontType.FONT_32));
		restartButton.setVisible(false);
		
		ButtonElement quitButton = new ButtonElement("quit_button", bs, new Point2D(SCREEN_SIZE[0]/2, SCREEN_SIZE[1]/2 + 2*hs), Color.BLACK, GameGUI.wreckage, Color.WHITE);
		quitButton.addCommand(InteractionType.MOUSE_DOWN, "quit");
		quitButton.addElement(new TextElement("qbt", bs,new Point2D(), "QUIT", FontType.FONT_32));
		quitButton.setVisible(false);
		
		overlay.addElement(startButton);
		overlay.addElement(soundButton);
		overlay.addElement(musicButton);
		overlay.addElement(restartButton);
		overlay.addElement(quitButton);
	}
	
	public void addController(ShipController controller) {
		controllers.put(controller.getPlayer(),controller);
	}
	
	public void addPlayer(Player player) {
		board.addPlayer(player,100);
		player.reset(board);
	}
	
	public Board getBoard() {
		return board;
	}
	
	/*-----------*/
	/* Main loop */
	/*-----------*/
	 
	public void start() {	
	
		lastTick = getTime();
		float dt = 1f;		
		
		while(!finished) {

			dt = (getTime() - lastTick) / GAMESPEED;
			lastTick = getTime();
			
			pollInput();
			
			// update model
			if(running && board.tick(dt)) {
				gameOverCountdown -= dt;
				if(gameOverCountdown<0)
					finished = true;
			}
			
			// monitor achievements
			if(running) PowerShop.monitorAchievements(board);
			
			// draw view
			gui.draw(board, dt);
			for(Player p: board.getPlayers())
				gui.drawHud(controllers.get(p.getPlayerID()), p, board);
			overlay.draw();
			if(!running) {
				for(ShipController sc: controllers.values())
					gui.drawCursors(sc, sc.getPlayerID());
			}
			
			TextElement te = (TextElement)overlay.getElement("scoreText");
			if(te!=null) te.setText("SCORE: " + ("000000000"+board.getScore()).substring((board.getScore()+"").length()));
			
			te = (TextElement)overlay.getElement("levelText");
			if(te!=null) te.setText("LEVEL: " +  ("00"+board.getMap().getLevel()).substring((board.getMap().getLevel()+"").length()));
			
			te = (TextElement)overlay.getElement("livesText");
			if(te!=null) te.setText("LIVES: " + ("000"+board.getLives()).substring((board.getLives()+"").length()));
			
			updateFPS();
			
			// opengl update
			Display.update();
			Display.sync(60);
			SoundManager.update();

			if(Display.isCloseRequested())
				finished = true;
		}
	}
	
	private void pollInput() {
		
		// UI input
		List<String> commands = overlay.pollInput();
		for(String com: commands) {
			switch(com) {
			case "start":
				running = true;
				overlay.getElement("start_button").setVisible(false);
				overlay.getElement("sound_button").setVisible(false);
				overlay.getElement("music_button").setVisible(false);
				overlay.getElement("restart_button").setVisible(false);
				overlay.getElement("quit_button").setVisible(false);
				break;
			case "sound":
				SoundManager.setSoundEffects(!SoundManager.isSoundPlaying());
				overlay.getElement("sbt_on",overlay.getElement("sound_button")).setVisible(SoundManager.isSoundPlaying());
				overlay.getElement("sbt_off",overlay.getElement("sound_button")).setVisible(!SoundManager.isSoundPlaying());
				break;
			case "music":
				if(SoundManager.isMusicPlaying())
					SoundManager.stopMusic();
				else SoundManager.startMusic(board.getMap().getLevelType());
				overlay.getElement("mbt_on",overlay.getElement("music_button")).setVisible(SoundManager.isMusicPlaying());
				overlay.getElement("mbt_off",overlay.getElement("music_button")).setVisible(!SoundManager.isMusicPlaying());
				break;
			case "restart":
				restartRequested = true;
				finished = true;
				break;
			case "quit":
				finished = true;
				break;
			}
		}
				
		// cheat/debug
		if(Keyboard.isKeyDown(Keyboard.KEY_P)) {
			for(Mech e: board.getEnemies()) {
				e.getPos().x = Board.BOARD_SIZE/2f;
				e.getPos().y = Board.BOARD_SIZE/2f;
			}
		}
		
		// poll controllers
		boolean pausedPressed = false; int i=0;
		for(ShipController controller: controllers.values()) {
			
			controller.pollInput();
			
			if(!running && controller.isSelecting() && !(controller instanceof MouseController) && hotSelect[i]) {
				// HUD commands
				commands = overlay.interact(controller.getCursorPosition(), InteractionType.MOUSE_DOWN);
				for(String com: commands) {
					switch(com) {
					case "start":
						running = true;
						overlay.getElement("start_button").setVisible(false);
						overlay.getElement("sound_button").setVisible(false);
						overlay.getElement("music_button").setVisible(false);
						overlay.getElement("restart_button").setVisible(false);
						overlay.getElement("quit_button").setVisible(false);
						break;
					case "sound":
						SoundManager.setSoundEffects(!SoundManager.isSoundPlaying());
						overlay.getElement("sbt_on",overlay.getElement("sound_button")).setVisible(SoundManager.isSoundPlaying());
						overlay.getElement("sbt_off",overlay.getElement("sound_button")).setVisible(!SoundManager.isSoundPlaying());
						break;
					case "music":
						if(SoundManager.isMusicPlaying())
							SoundManager.stopMusic();
						else SoundManager.startMusic(board.getMap().getLevelType());
						overlay.getElement("mbt_on",overlay.getElement("music_button")).setVisible(SoundManager.isMusicPlaying());
						overlay.getElement("mbt_off",overlay.getElement("music_button")).setVisible(!SoundManager.isMusicPlaying());
						break;
					case "restart":
						restartRequested = true;
						finished = true;
						break;
					case "quit": 
						finished = true;
						break;
					}
				}
				hotSelect[i] = false;
			} else if(running || !controller.isSelecting()) {
				hotSelect[i] = true;
			}
			
			// escape
			if(controller.isPausing()) {
				pausedPressed = true;
				if(hotPause) {
					running = running^true;
					
					overlay.getElement("start_button").setVisible(!running);
					overlay.getElement("sound_button").setVisible(!running);
					overlay.getElement("music_button").setVisible(!running);
					overlay.getElement("restart_button").setVisible(!running);
					overlay.getElement("quit_button").setVisible(!running);
					overlay.getElement("sbt_on",overlay.getElement("sound_button")).setVisible(SoundManager.isSoundPlaying());
					overlay.getElement("sbt_off",overlay.getElement("sound_button")).setVisible(!SoundManager.isSoundPlaying());
					overlay.getElement("mbt_on",overlay.getElement("music_button")).setVisible(SoundManager.isMusicPlaying());
					overlay.getElement("mbt_off",overlay.getElement("music_button")).setVisible(!SoundManager.isMusicPlaying());
					
					for(ShipController sc : controllers.values()) {
						sc.getCursorPosition().x = Frame.SCREEN_SIZE[0]/2f;
						sc.getCursorPosition().y = 3*Frame.SCREEN_SIZE[1]/4f;
					}
					hotPause = false;
				}
			}			
			
			if(running) {
				// player control
				for(Player p : board.getPlayers())
					pollPlayer(controller, p, true);
				
				// respawning player control
				for(Player p : board.getRespawnQueue().keySet())
					pollPlayer(controller, p, false);
			}
			
			i++;
		}
		if(!pausedPressed) hotPause = true;
	}

	private void pollPlayer(ShipController controller, Player p, boolean powers) {
		if(p.getPlayerID().equals(controller.getPlayer())) {
			p.getDirection().x = controller.getDirection().x;
			p.getDirection().y = controller.getDirection().y;
			p.setShooting(controller.isShooting());
			if(powers) {
				for(int i=0;i<4;i++)
					if(controller.getPowerButtons()[i]) p.getPowerEater().activatePower(i, board, p);
			}
			if(p.isMouseControlled()) {
				((MouseController)controller).getPlayerPos().x = p.getPos().x;
				((MouseController)controller).getPlayerPos().y = p.getPos().y;
				p.setMouseSelection(((MouseController)controller).getPower());
			}
		}
	}

	private void updateFPS() {
		if (Mission.getTime() - lastFPS > 1000) {
			Display.setTitle("FPS: " + fps);
			fps = 0;
			lastFPS = Mission.getTime();
		}
		fps++;
	}
	
	public boolean isRestartRequested() {
		return restartRequested;
	}
	
	public static long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
}

