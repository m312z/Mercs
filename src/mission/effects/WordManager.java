package mission.effects;

import gui.FontManager.FontType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import phys.Point2D;

public class WordManager {
	
	static List<Word> words = new ArrayList<Word>();
	
	public static void tick() {
		
		Iterator<Word> wit = words.iterator();
		while(wit.hasNext()) {
			Word w = wit.next();
			w.life--;
			if(w.life<0)
				wit.remove();
		}
		
	}
	
	public static void addWord(String word, FontType font, Point2D pos, int life) {
		words.add(new Word(word, font, pos, life));
	}
	
	public static void addWord(String word, Point2D pos, int life,boolean back) {
		words.add(new Word(word, pos, life,back));
	}
	
	public static void addWord(String word, Point2D pos, int life) {
		words.add(new Word(word, pos, life));
	}
	
	public static List<Word> getWords() {
		return words;
	};
}
