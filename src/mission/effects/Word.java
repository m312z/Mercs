package mission.effects;

import gui.FontManager;
import gui.FontManager.FontType;
import phys.Point2D;

public class Word
{
	public String word;
	public Point2D pos;
	public float life;
	public boolean back;
	public FontManager.FontType fontType;

	public Word(String word, FontType font, Point2D pos, int life) {
		this(word, pos, life);
		this.fontType = font;
	}
	
	public Word(String word, Point2D pos, int life, boolean back) {
		this(word, pos, life);
		this.back = back;
		this.fontType = FontType.FONT_24;
	}
	
	public Word(String word, Point2D pos, int life) {
		this.word = word;
		this.pos = pos;
		this.life = life;
		this.fontType = FontType.FONT_24;
	}
}	
