package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Animation;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunction3;
import xyz.marsavic.gfxlab.TransformationFromSize;
import xyz.marsavic.gfxlab.aggregation.AggregatorFrameLast;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.entry_points.App;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Identity;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Hash;
import xyz.marsavic.utils.Numeric;

import static xyz.marsavic.reactions.elements.Elements.*;


record Ray(Vec3 p, Vec3 d) {}

record Ball(Vec3 c, double r) {}


public record FirstRayTracer (
		double yC
) implements ColorFunction3 {
	
	double firstHitBall(Ray ray, Ball b) {
		Vec3 e = b.c().sub(ray.p());                                // Vector from the ray origin to the ball center
		
		double dSqrInv = 1 / ray.d().lengthSquared();
		double tM = e.dot(ray.d()) * dSqrInv;                       // Time at which the ray is closest to the center
		double dtSqr = tM * tM - (e.lengthSquared() - b.r() * b.r()) * dSqrInv;
		
		if (dtSqr > 0) {
			double dt = Math.sqrt(dtSqr);
			if (tM - dt > 0) return tM - dt;
			if (tM + dt > 0) return tM + dt;
		}
		return Double.POSITIVE_INFINITY;
	}
	
	double firstHitFloor(Ray r) {
		return r.d().z() >= 0 ? Double.POSITIVE_INFINITY : -1 / r.d().z();
	}
		
		
	@Override
	public Color at(double t, Vector p) {
		Ray r = new Ray(Vec3.ZERO, Vec3.xyz(p.x(), 1, p.y()));
		
		Ball ball = new Ball(Vec3.xyz(0, yC, Numeric.cosT(t) - 1), 1);
		
		double tHitBall  = firstHitBall (r, ball);
		double tHitFloor = firstHitFloor(r);
		double tHit = Math.min(tHitBall, tHitFloor);
		Color c = tHitFloor < tHitBall ? Color.hsb(0.3, 0.8, 1) : Color.hsb(0.8, 0.8, 1);
		
		return Double.isInfinite(tHit) ? Color.BLACK : c.mul(1 / (1 + tHit));
	}

	
	// ================================================================================================================
	
	static void main() { App.run(); }
	
	
	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorFrameLast::new),
	                            e(FirstRayTracer.class,
			                            e(2.2)
						        ),
								e(TransformationFromSize.ToGeometricFitT0_.class),
								e(Vec3.xyz(300, 720, 720)),
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
