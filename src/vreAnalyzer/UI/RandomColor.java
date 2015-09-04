package vreAnalyzer.UI;

import java.awt.Color;
import java.util.Random;

public class RandomColor {
	private Color randomColor;
	public RandomColor(){
		Random rand = new Random();
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		randomColor = new Color(r,g,b);
	}
	public Color getColor(){
		return randomColor;
	}
}
