package dk.itu.ai;

import org.apache.log4j.Logger;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import mu.nu.nullpo.gui.slick.DummyMenuScrollState;
import mu.nu.nullpo.gui.slick.NormalFontSlick;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.slick.ResourceHolderSlick;
import mu.nu.nullpo.gui.slick.StateTitle;

/**
 * Custom selection screen for AI training (In-game Dojo).
 * 
 * NOTE: This is a copy of mu.nu.nullpo.gui.slick.StateSelectMode that has been customized for training purposes.
 */
public class StateSelectTraining  extends DummyMenuScrollState {
	
	/** Logger */
	static Logger log = Logger.getLogger(StateSelectTraining.class);

	/** This state's ID */
	public static final int ID = 420;

	/** Number of game modes in one page */
	public static final int PAGE_HEIGHT = 24;

	/** true if top-level folder */
	public static boolean isTopLevel = false;

	/** Current folder name */
	protected String strCurrentFolder = "MANIA MODES";
	
	private String[] rules;
	private String[] rulePaths; 
	
	/**
	 * Constructor
	 */
	public StateSelectTraining() {
		super();
		super.pageHeight = PAGE_HEIGHT;
		super.list = new String[] {"AI TRAINING"};
		rules = new String[] {"STANDARD AI TRAINING", "STANDARD"};
		rulePaths = new String[] {"config/rule/StandardAITraining.rul", "config/rule/Standard.rul"};
		super.maxCursor = list.length - 1;
		super.cursor = 0;
	}

	/*
	 * Fetch this state's ID
	 */
	@Override
	public int getID() {
		return ID;
	}

	/*
	 * State initialization
	 */
	public void init(GameContainer container, StateBasedGame game) throws SlickException { }


	/**
	 * Get mode ID (not including netplay modes)
	 * @param name Name of mode
	 * @return ID (-1 if not found)
	 */
	protected int getIDbyName(String name) {
		if((name == null) || (list == null)) return -1;

		for(int i = 0; i < list.length; i++) {
			if(name.equals(list[i])) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Get game mode description
	 * @param str Mode name
	 * @return Description
	 */
	protected String getModeDesc(String str) {
		String str2 = str.replace(' ', '_');
		str2 = str2.replace('(', 'l');
		str2 = str2.replace(')', 'r');
		String result = NullpoMinoSlick.propModeDesc.getProperty(str2);
		if(result == null) {
			result = NullpoMinoSlick.propDefaultModeDesc.getProperty(str2, str2);
		}
		return result;
	}

	/*
	 * Enter
	 */
	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException { }

	/*
	 * Render screen
	 */
	@Override
	public void onRenderSuccess(GameContainer container, StateBasedGame game, Graphics graphics) 
	{
		try 
		{
			NormalFontSlick.printFontGrid(1, 1, rules[cursor] + " RULES", NormalFontSlick.COLOR_ORANGE);			
		} catch(Exception e)
		{
			NormalFontSlick.printFontGrid(1, 1, "TRAINING MODES", NormalFontSlick.COLOR_ORANGE);
		}
	}

	/*
	 * Decide
	 */
	@Override
	protected boolean onDecide(GameContainer container, StateBasedGame game, int delta) {
		ResourceHolderSlick.soundManager.play("decide");
		
		NullpoMinoSlick.propGlobal.setProperty("name.mode." + strCurrentFolder, list[cursor]);
		NullpoMinoSlick.propGlobal.setProperty("name.mode", list[cursor]);
		NullpoMinoSlick.saveConfig();
		
		String strRulePath = rulePaths[cursor];
		
		NullpoMinoSlick.stateInTraining.startNewGame(strRulePath);
		game.enterState(StateInTraining.ID);
		return false;
	}

	/*
	 * Cancel
	 */
	@Override
	protected boolean onCancel(GameContainer container, StateBasedGame game, int delta) {
		
		game.enterState(StateTitle.ID);
		
		return false;
	}
}