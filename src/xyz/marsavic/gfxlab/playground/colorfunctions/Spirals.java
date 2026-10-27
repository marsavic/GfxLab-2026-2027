package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.gfxlab.entry_points.*;
import xyz.marsavic.gfxlab.math.*;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.*;
import xyz.marsavic.gfxlab.aggregation.AggregatorOneAhead;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Identity;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Hash;
import xyz.marsavic.utils.Numeric;

import static xyz.marsavic.reactions.elements.Elements.*;


public record Spirals(
		int nRays,
		double twisting,
		double blue
) implements ColorFunction3 {
		
	@Override
	public Color at(double t, Vector p) {
		return Color.rgb(
				Math.max(0, Numeric.sinT(-t + nRays * p.angle())),
				Math.max(0, Numeric.sinT(2 * t + twisting / p.length() + p.angle())),
				blue
		);
	}
	

	// ================================================================================================================
	
	static void main() { App.run(); }
	
	
	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorOneAhead::new),
								e(Spirals.class
										, e(7)
										, e(0.25)
										, e(0.4)
								),
								e(TransformationFromSize.ToGeometricT0_.class),
								e(Vec3.xyz(360, 640, 640)),
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
