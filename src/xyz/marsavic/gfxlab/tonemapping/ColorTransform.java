package xyz.marsavic.gfxlab.tonemapping;

import xyz.marsavic.functions.F1;
import xyz.marsavic.gfxlab.arrays.Array2;
import xyz.marsavic.gfxlab.Color;


/** A transformation of colors, applied to each pixel independently. */
public interface ColorTransform extends F1<Color, Color> {
	
	/** The function that gives this transformation for every image. */
	default F1<ColorTransform, Array2<Color>> asColorTransformFromMatrixColor() {
		return colorMatrix -> ColorTransform.this;
	}
	
}

