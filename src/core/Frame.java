package core;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_GREATER;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glAlphaFunc;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glShadeModel;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;
import gui.FontManager;

import java.awt.Toolkit;
import java.util.Iterator;

import mission.Mission;
import mission.PlayerFactory;
import mission.gameobject.Player;
import mission.map.LevelMap.LevelType;
import mission.powers.Power;
import monitor.PowerShop;
import monitor.StatCounter;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import sound.SoundManager;
import core.FileManager.LevelScore;
import core.controller.MouseController;
import core.controller.ShipController;
import core.menu.ControllerSelectionScreen;
import core.menu.LevelSelectionScreen;
import core.menu.LoadingScreen;
import core.menu.MainMenuScreen;
import core.menu.MenuBackground;
import core.menu.PowerSelectionScreen;
import core.menu.ResultsScreen;
/**
 * The main game class.  Begins the main menu loop and handles
 * setting up the Display.
 * @author Michael Cashmore
 * 
 */
public class Frame {
	
	public enum GameState {
		LOADING,
		MAINMENU,
		SETUP_CONTROL,
		SETUP_POWERS,
		SETUP_LEVEL,
		ARCADEGAME,
		RESULTS,
		END;
	}
	
	/* Various dimensions */
	public static boolean FULLSCREEN = false;
	public static int[] WINDOW_SIZE = { (int)(1280/2),(int)(720/2) };
	public static int[] SCREEN_SIZE = { WINDOW_SIZE[0], WINDOW_SIZE[1] };
	public static float FONT_SCALE = SCREEN_SIZE[0]/1280f;
	
	/* GUI stuff */
	MenuBackground background;
	private MainMenuScreen menuController;
	private StatCounter lastResults;
	FileManager fileManager;

	/* Game state: main menu, choose players, or playing */
	public GameState state = GameState.LOADING;
	
	/* created players */
	Player[] players = new Player[2];
	ShipController[] controllers = new ShipController[2];
	
	/* chosen level */
	LevelType levelType = LevelType.THEHIVE;
	
	private void start() {
		
		// unlock starting powers
		PowerShop.unlock(Power.SHIELD_FAT);
		PowerShop.unlock(Power.HOMINGPOWER);
		PowerShop.unlock(Power.SPIRALPOWER);
		
		try {

			background = new MenuBackground();
			fileManager = new FileManager();
			fileManager.readHighScores();

			// main loop
			boolean done = false;
			while (!done) {

				if (Display.isCloseRequested())
					done = true;

				switch(state) {
				case LOADING:
					
					/*----------------*/
					/* LOADING SCREEN */
					/*----------------*/
					LoadingScreen loadingController = new LoadingScreen(this);
					loadingController.start();
					break;
					
				case MAINMENU:

					/*-----------*/
					/* MAIN MENU */
					/*-----------*/
					menuController = new MainMenuScreen(this);
					menuController.start();
					done = (state==GameState.END);
					break;
										
				case SETUP_CONTROL:

					/*--------------------*/
					/* CHOOSE CONTROLLERS */
					/*--------------------*/
					ControllerSelectionScreen controllerSelection = new ControllerSelectionScreen(this);
					controllerSelection.setupControllers();
					boolean controllerSet = false;
					for(int i=0;i<2;i++) {
						controllers[i] = controllerSelection.getControllers()[i];
						if(controllerSelection.getControllers()[i]!=null)
							controllerSet = true;
					}
					if(controllerSet) state = GameState.SETUP_POWERS;
					else state = GameState.MAINMENU;
					break;
					
				case SETUP_POWERS:
					
					/*---------------*/
					/* CHOOSE POWERS */
					/*---------------*/
					players[0] = null;
					players[1] = null;
					PowerSelectionScreen powerSelection = new PowerSelectionScreen(this,controllers);
					powerSelection.setupPowers();
					for(int i=0;i<2;i++) {
						if(controllers[i]!=null) {
							int p = 0;
							int activePowers = 0;
							Power[] pwr = new Power[6];
							Iterator<Integer> pit = powerSelection.getPowers(i).iterator();
							while(pit.hasNext() && p<6) {
								// create power list
								pwr[p] = Power.values()[pit.next()];
								// count active powers for mouse control
								if(pwr[p].active) activePowers++;
								p++;
							}
							players[i] = PlayerFactory.makePlayer("player"+i,i,pwr);
							if(controllers[i] instanceof MouseController) {
								players[i].setMouseControlled(true);
								((MouseController)controllers[i]).setMaxPowers(activePowers);
							}
						}
					}
					if(powerSelection.toPlay()) state = GameState.SETUP_LEVEL;
					else state = GameState.SETUP_CONTROL;
					break;
					
				case SETUP_LEVEL:
					
					/*--------------*/
					/* CHOOSE LEVEL */
					/*--------------*/
					LevelSelectionScreen levelSelection = new LevelSelectionScreen(this,controllers);
					levelSelection.setupLevel();
					levelType = levelSelection.getLevelType();
					if(levelSelection.toPlay()) state = GameState.ARCADEGAME;
					else state = GameState.SETUP_POWERS;
					break;
					
				case ARCADEGAME:
					
					/*-----------*/
					/* PLAY GAME */
					/*-----------*/
					Mouse.setGrabbed(true);
					Mission localGame = new Mission(this, levelType);
					for(int i=0;i<2;i++) {
						if(players[i]!=null) {
							localGame.addPlayer(players[i]);
							localGame.addController(controllers[i]);
							if(controllers[i] instanceof MouseController)
								Mouse.setGrabbed(false);
						}
					}
					
					if(!SoundManager.isMusicMute())
						SoundManager.startMusic(levelType);
					// PLAY
					localGame.start();
					// GAME FINISHED
					Mouse.setGrabbed(false);
					SoundManager.stopMusic();
					
					lastResults = localGame.getBoard().getCounter();
					fileManager.saveHighResults(lastResults,levelType);
					PowerShop.resetAchievementMonitor();
					
					if(!localGame.isRestartRequested())
						state = GameState.RESULTS;
					break;
					
				case RESULTS:
					
					/*-----------------*/
					/* DISPLAY RESULTS */
					/*-----------------*/
					ResultsScreen results = new ResultsScreen(this,controllers);
					results.start();
					break;
					
				default:
					state = GameState.MAINMENU;
					break;
				}
				
				// opengl update
				Display.update();
				Display.sync(60);
			}

			// quit
			AL.destroy();
			FontManager.destroyFonts();
			Display.destroy();
			System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public MenuBackground getBackground() {
		return background;
	}
		
	public FileManager getFileManager() {
		return fileManager;
	}
	
	public StatCounter getLastResults() {
		return lastResults;
	}
	
	public LevelScore getHighScore() {
		return fileManager.getLevelScore(levelType);
	}
	
	/*--------------------*/
	/* (re)setup graphics */
	/*--------------------*/

	public Frame() {
		
		WINDOW_SIZE[1] = (int)(WINDOW_SIZE[0]*(
				Toolkit.getDefaultToolkit().getScreenSize().height
				/(float)Toolkit.getDefaultToolkit().getScreenSize().width));
		setDisplayMode(WINDOW_SIZE[0], WINDOW_SIZE[1], FULLSCREEN);

		try {
			Display.setTitle("");//("Mercenaries");
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.out.println("Cannot create display!");
		}

		glEnable(GL_TEXTURE_2D);
		glShadeModel(GL_SMOOTH);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);

		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_ALPHA_TEST);
		glAlphaFunc(GL_GREATER, 0.1f);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		glViewport(0, 0, Display.getDisplayMode().getWidth(), Display
				.getDisplayMode().getHeight());
		glMatrixMode(GL_MODELVIEW);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, Display.getDisplayMode().getWidth(), Display
				.getDisplayMode().getHeight(), 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
	}

	/**
	 * alters SCREEN_SIZE, but not WINDOW_SIZE.
	 * @param width		Desired window width
	 * @param height	Desired window height
	 * @param fullscreen	true if the display is to be made full-screen
	 */
	public void setDisplayMode(int width, int height, boolean fullscreen) {

		if (fullscreen) {
			width = Toolkit.getDefaultToolkit().getScreenSize().width;
			height = Toolkit.getDefaultToolkit().getScreenSize().height;
		}

		// return if requested DisplayMode is already set
		if ((Display.getDisplayMode().getWidth() == width)
				&& (Display.getDisplayMode().getHeight() == height)
				&& (Display.isFullscreen() == fullscreen)) {
			return;
		}

		try {
			DisplayMode targetDisplayMode = null;

			if (fullscreen) {
				DisplayMode[] modes = Display.getAvailableDisplayModes();
				int freq = 0;

				for (int i = 0; i < modes.length; i++) {
					DisplayMode current = modes[i];

					if ((current.getWidth() == width && current.getHeight() == height)) {
						if ((targetDisplayMode == null)
								|| (current.getFrequency() >= freq))
							if ((targetDisplayMode == null)
									|| (current.getBitsPerPixel() > targetDisplayMode
											.getBitsPerPixel())) {
								targetDisplayMode = current;
								freq = targetDisplayMode.getFrequency();
							}

						/* if we've found a match for bpp and frequence against the
						 * original display mode then it's probably best to go for
						 * this one since it's most likely compatible with the monitor
						 */
						if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel())
								&& (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
							targetDisplayMode = current;
							break;
						}
					}
				}
			} else {
				targetDisplayMode = new DisplayMode(width, height);
			}

			if (targetDisplayMode == null) {
				System.out.println("Failed to find value mode: " + width + "x"
						+ height + " fs=" + fullscreen);
				return;
			}

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);

			// set the view position
			if (Display.isCreated()) {
				glViewport(0, 0, Display.getDisplayMode().getWidth(), Display
						.getDisplayMode().getHeight());
				glMatrixMode(GL_MODELVIEW);

				glMatrixMode(GL_PROJECTION);
				glLoadIdentity();
				glOrtho(0, Display.getDisplayMode().getWidth(), Display
						.getDisplayMode().getHeight(), 0, 1, -1);
				glMatrixMode(GL_MODELVIEW);
			}

			// set the screen size vars...
			SCREEN_SIZE[0] = targetDisplayMode.getWidth();
			SCREEN_SIZE[1] = targetDisplayMode.getHeight();
			FONT_SCALE = SCREEN_SIZE[0]/1280f;
			
		} catch (LWJGLException e) {
			System.out.println("Unable to setup mode " + width + "x" + height
					+ " fullscreen=" + fullscreen + e);
		}
	}

	/*-------------*/
	/* main method */
	/*-------------*/

	public static void main(String[] args) {
		Frame p = new Frame();
		p.start();
	}
}
