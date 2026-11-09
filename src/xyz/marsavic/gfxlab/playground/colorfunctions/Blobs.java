package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.gfxlab.entry_points.*;
import xyz.marsavic.gfxlab.math.*;
import xyz.marsavic.geometry.Box;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.*;
import xyz.marsavic.gfxlab.aggregation.AggregatorOneAhead;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Identity;
import xyz.marsavic.random.sampling.Sampler;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Hash;

import static xyz.marsavic.reactions.elements.Elements.*;
import static xyz.marsavic.utils.Numeric.*;


public class Blobs implements ColorFunction3 {

	private final int n;
	private final double m;
	private final double threshold;
	
	
	private final Vector[] o, c;

	
	
//	public static final Defaults<Blobs> $ = Defaults.args(5, 0.1, 0.2);

	public Blobs(int n, double m, double threshold, Hash hash) {
		this.n = n;
		this.m = m;
		this.threshold = threshold;
		
		o = new Vector[n];
		c = new Vector[n];
		
		Sampler sampler = new Sampler(hash);
		for (int i = 0; i < n; i++) {
			o[i] = sampler.randomInBox(Box.cr(10)).round();
			c[i] = sampler.randomInBox();
		}
	}
	
	
	@Override
	public Color at(double t, Vector p) {
		double s = 0;
		for (int i = 0; i < n; i++) {
			Vector c = Vector.polar(0.5, o[i].mul(t).add(this.c[i]));
			double d = p.sub(c).lengthSquared();
			s += Math.exp(-d / m);
		}
		double k = 0.3 * s - threshold;
		double o = sinT(2 * p.length() - 12 * t) / (1 + 2 * p.length());
		double v = mpToZo(o * sinT(p.angle()) * 1);
		Vector q = p.f(x -> x*0.495 + 0.5).add(p.normalizedTo(o).mul(0.01)).mul(64);
		return (k < 0 ?
				Color.gray(0.5 * (q.xInt() ^ q.yInt()) / 64.0 * v) :
				Color.oklch(0.8, 0.13, mpToZo(p.y()) + t)
		).mul(Math.abs(k));
	}
	
	
	// ================================================================================================================
	
	static void main() { App.run(); }
	
	
	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorOneAhead::new),
								e(Blobs.class
										, e(5)
										, e(0.1)
										, e(0.2)
										, e(Hash.class, e(0xB182847F9F621EB1L))
								),
								e(TransformationFromSize.ToGeometricFitT0_.class),
								e(Vec3.xyz(360, 640, 640)),
								e(false),
								e(false),
								e(Hash.class, e(0x8EE6B0C4E02CA7B2L))
						),
						e(ToneMapping2.class,
								e(ColorTransform::asColorTransformFromMatrixColor, e(Identity.class))
						)
				);
	}
	
}
