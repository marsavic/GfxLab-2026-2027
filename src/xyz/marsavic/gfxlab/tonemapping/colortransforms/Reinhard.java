package xyz.marsavic.gfxlab.tonemapping.colortransforms;

import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;


/**
 * The Reinhard tone mapping curve x / (1 + x), applied after multiplying the color by exposure. With perComponent,
 * the curve is applied to each component. Otherwise, it is applied to the luminance and the color is scaled
 * accordingly, which keeps the hue and the saturation, but can give components greater than 1; these are clamped.
 */
public record Reinhard(
		double exposure,
		boolean perComponent
) implements ColorTransform {

	@Override
	public Color at(Color color) {
		Color c = color.mul(exposure);
		if (perComponent) {
			return c.f(x -> x / (1 + x));
		}
		return c.div(1 + c.luminance()).clampTo01();
	}

}
