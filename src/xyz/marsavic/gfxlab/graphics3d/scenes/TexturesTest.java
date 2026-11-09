package xyz.marsavic.gfxlab.graphics3d.scenes;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.*;
import xyz.marsavic.gfxlab.aggregation.AggregatorFrameLast;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.entry_points.App;
import xyz.marsavic.gfxlab.graphics3d.*;
import xyz.marsavic.gfxlab.graphics3d.raytracers.RayTracerMaterial;
import xyz.marsavic.gfxlab.graphics3d.solids.*;
import xyz.marsavic.gfxlab.graphics3d.textures.Checkerboard;
import xyz.marsavic.gfxlab.graphics3d.textures.Grid;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Identity;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Hash;
import xyz.marsavic.utils.Numeric;

import static xyz.marsavic.gfxlab.math.Vec3.*;
import static xyz.marsavic.reactions.elements.Elements.*;


/** Procedural textures: tiles on the floor, a checkerboard, and spiral stripes on the balls. */
public record TexturesTest(int a, int b, int c, int d) implements FFSceneT {

	@Override
	public Solid solid() {
		HalfSpace floor = HalfSpace.pn(xyz(0, 0, -1), EZ,
				new Grid(Vector.xy(1), Vector.xy(0.06), Material.matte(0.6), Material.matte(0.15)));

		Ball ballChecker = Ball.cr(xyz(-1.15, 3.2, 0), 1,
				new Checkerboard(Vector.xy(1.0 / a, 1.0 / b),
						Material.matte(Color.hsb(0.08, 0.85, 0.8)),
						Material.matte(0.8)));

		Material stripe = Material.matte(Color.hsb(0.6, 0.85, 0.6));
		Material base = Material.matte(0.8);
		F1<Material, Vector> spiral = uv -> Numeric.mod(uv.dot(Vector.xy(c, d))) < 0.3 ? stripe : base;
		Ball ballSpiral = Ball.cr(xyz(1.15, 3.2, 0), 1, spiral);

		return Group.of(floor, ballChecker, ballSpiral);
	}

	@Override
	public Color colorBackground() {
		return Color.hsb(0.6, 0.4, 0.9);
	}


	// ================================================================================================================


	static void main() { App.run(); }


	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorFrameLast::new),
								e(RayTracerMaterial.class,
										e(TexturesTest.class
												, e(16)
												, e(4)
												, e(5)
												, e(3)
										)
								),
								e(TransformationFromSize.ToGeometricFitT0_.class),
								e(xyz(1, 640, 640)),
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
