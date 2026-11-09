package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.gfxlab.entry_points.*;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.*;
import xyz.marsavic.gfxlab.aggregation.AggregatorOneAhead;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.math.*;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Identity;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Hash;

import static xyz.marsavic.reactions.elements.Elements.*;


/**
 * Gradients from the color a (left) to the color b (right), in three bands, top to bottom:
 * - Interpolation of the sRGB encoded components
 * - Interpolation of the linear components
 * - Interpolation of the Oklab coordinates
 */
public record ColorInterpolation(
		Color a,
		Color b
) implements ColorFunction3 {

	@Override
	public Color at(double t, Vector p) {
		double k = p.x();
		return switch ((int) (3 * p.y())) {
			case 2 -> Color.lerp(a.f(Color::linearToSRGB), b.f(Color::linearToSRGB), k).f(Color::sRGBtoLinear);
			case 1 -> Color.lerp(a, b, k);
			case 0 -> Color.oklab(Vec3.lerp(a.oklab(), b.oklab(), k));
			default -> throw new IllegalArgumentException("Invalid band index");
		};
	}


	// ================================================================================================================


	static void main() { App.run(); }
	
	
	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorOneAhead::new),
								e(ColorInterpolation.class,
										e(Color.rgb(0, 1, 0)),
										e(Color.rgb(1, 0, 0))
								),
								e(TransformationFromSize.ToUnitBox_.class),
								e(Vec3.xyz(1, 1200, 480)),
								e(true),
								e(false),
								e(Hash.class, e(0x8EE6B0C4E02CA7B2L))
						),
						e(ToneMapping2.class,
								e(ColorTransform::asColorTransformFromMatrixColor, e(Identity.class))
						)
				);
	}

}
