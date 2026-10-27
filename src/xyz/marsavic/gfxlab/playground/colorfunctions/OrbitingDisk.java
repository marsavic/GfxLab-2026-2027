package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.gfxlab.entry_points.*;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Animation;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunction3;
import xyz.marsavic.gfxlab.TransformationFromSize;
import xyz.marsavic.gfxlab.aggregation.AggregatorOneAhead;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Identity;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Hash;

import static xyz.marsavic.reactions.elements.Elements.*;


/** A white disk orbiting the center, {@code nTurns} times during the animation. Fast enough to show motion blur. */
public class OrbitingDisk implements ColorFunction3 {
	
	private final double nTurns;
	
	
	public OrbitingDisk(double nTurns) {
		this.nTurns = nTurns;
	}
	
	
	@Override
	public Color at(double t, Vector p) {
		Vector c = Vector.polar(0.6, nTurns * t);
		return p.sub(c).lengthSquared() < 0.02 ? Color.WHITE : Color.BLACK;
	}
	
	
	// ================================================================================================================
	
	static void main() { App.run(); }
	
	
	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorOneAhead::new),
								e(OrbitingDisk.class, e(4.0)),
								e(TransformationFromSize.ToGeometricFitT0_.class),
								e(Vec3.xyz(60, 640, 640)),
								e(true),
								e(true),
								e(Hash.class, e(0x8EE6B0C4E02CA7B2L))
						),
						e(ToneMapping2.class,
								e(ColorTransform::asColorTransformFromMatrixColor, e(Identity.class))
						)
				);
	}
	
}
