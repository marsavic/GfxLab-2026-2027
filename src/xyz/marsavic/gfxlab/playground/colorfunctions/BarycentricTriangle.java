package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.gfxlab.entry_points.*;
import xyz.marsavic.gfxlab.math.*;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.*;
import xyz.marsavic.gfxlab.aggregation.AggregatorFrameLast;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Identity;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Hash;

import static xyz.marsavic.reactions.elements.Elements.*;


/**
 * A triangle with colors given at the vertices, interpolated inside the triangle using barycentric coordinates
 * (as in Gouraud shading). The barycentric coordinates are ratios of signed areas, computed with cross products.
 */
public class BarycentricTriangle implements ColorFunction3 {

	private static final Vector A = Vector.xy(-0.1,  0.85);
	private static final Vector B = Vector.xy(-0.85, -0.7);
	private static final Vector C = Vector.xy( 0.8, -0.45);
	private static final Color cA = Color.rgb(1, 0, 0);
	private static final Color cB = Color.rgb(0, 1, 0);
	private static final Color cC = Color.rgb(0, 0, 1);

	/** Twice the signed area of the triangle pqr: positive iff p, q, r are counterclockwise. */
	private static double area2(Vector p, Vector q, Vector r) {
		return q.sub(p).cross(r.sub(p));
	}

	@Override
	public Color at(double t, Vector p) {
		double abc = area2(A, B, C);
		double alphaA = area2(p, B, C) / abc;    // The triangle opposite to A
		double alphaB = area2(A, p, C) / abc;    // The triangle opposite to B
		double alphaC = 1 - alphaA - alphaB;
		if (alphaA < 0 || alphaB < 0 || alphaC < 0) return Color.BLACK;   // Outside of the triangle
		return cA.mul(alphaA).add(cB.mul(alphaB)).add(cC.mul(alphaC));
	}


	// ================================================================================================================

	static void main() { App.run(); }
	
	
	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorFrameLast::new),
								e(BarycentricTriangle.class),
								e(TransformationFromSize.ToGeometricFitT0_.class),
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
