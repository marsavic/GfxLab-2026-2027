package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.gfxlab.entry_points.*;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Animation;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunction3;
import xyz.marsavic.gfxlab.TransformationFromSize;
import xyz.marsavic.gfxlab.aggregation.AggregatorFrameLast;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Identity;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Hash;

import static xyz.marsavic.reactions.elements.Elements.*;
import static xyz.marsavic.utils.Numeric.*;


/**
 * A zone plate: concentric rings, cos(k r^2). The phase grows quadratically with the distance r from the center, so
 * the frequency of the rings (their number per unit length) grows linearly. Far enough from the center the rings are
 * finer than the pixels, which makes it a good test for aliasing.
 */
public class ZonePlate implements ColorFunction3 {
	
	private final double k;
	
	
	public ZonePlate(double k) {
		this.k = k;
	}
	
	
	@Override
	public Color at(double t, Vector p) {
		return Color.gray(mpToZo(cosT(k * p.lengthSquared())));
	}
	
	
	// ================================================================================================================
	
	static void main() { App.run(); }
	
	
	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorFrameLast::new),
								e(ZonePlate.class, e(250.0)),
								e(TransformationFromSize.ToGeometricFit_.class),
								e(Vec3.xyz(1, 640, 640)),
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
