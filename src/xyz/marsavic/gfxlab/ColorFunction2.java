package xyz.marsavic.gfxlab;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;


/** A color function without time: gives the color at each point of the plane. */
@FunctionalInterface
public interface ColorFunction2 extends F1<Color, Vector> {
	
	/** The same function as a ColorFunction3, constant in time. */
	default ColorFunction3 asColorFunction3() {
		return new ColorFunction3() {
			@Override public Color at(double t, Vector p) { return ColorFunction2.this.at(p); }
		};
	}
	
}
