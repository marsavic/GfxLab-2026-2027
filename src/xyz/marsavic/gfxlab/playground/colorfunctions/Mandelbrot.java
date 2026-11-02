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

import static xyz.marsavic.reactions.elements.Elements.*;


/**
 * The Mandelbrot set: the points c of the complex plane for which the sequence z_0 = 0, z_{k+1} = z_k^2 + c stays bounded.
 * Points outside the set are colored by the number of iterations after which |z_k| > 2. During the animation
 * (t from 0 to 1) the view zooms in on the point {@code target}, by the factor {@code zoom}.
 */
public class Mandelbrot implements ColorFunction3 {
	
	private final int maxIterations;
	private final Vector target;
	private final double zoom;
	
	
	public Mandelbrot(int maxIterations, Vector target, double zoom) {
		this.maxIterations = maxIterations;
		this.target = target;
		this.zoom = zoom;
	}
	
	
	@Override
	public Color at(double t, Vector p) {
		Vector c = target.add(p.mul(1.5 * Math.pow(zoom, -t)));
		double x = 0, y = 0;
		for (int k = 0; k < maxIterations; k++) {
			double x_ = x * x - y * y + c.x();
			y = 2 * x * y + c.y();
			x = x_;
			if (x * x + y * y > 4) {
				return Color.oklch(0.75, 0.1, k / 40.0);
			}
		}
		return Color.BLACK;
	}
	
	
	// ================================================================================================================
	
	static void main() { App.run(); }
	
	
	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorOneAhead::new),
								e(Mandelbrot.class, e(1000), e(Vector.xy(-0.743643887, 0.131825904)), e(1000.0)),
								e(TransformationFromSize.ToGeometricFitT0_.class),
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
