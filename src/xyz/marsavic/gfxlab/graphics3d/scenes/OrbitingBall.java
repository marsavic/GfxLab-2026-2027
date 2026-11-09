package xyz.marsavic.gfxlab.graphics3d.scenes;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Animation;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.TransformationFromSize;
import xyz.marsavic.gfxlab.aggregation.AggregatorFrameLast;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.entry_points.App;
import xyz.marsavic.gfxlab.graphics3d.FFSceneT;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.gfxlab.graphics3d.raytracers.RayTracerMaterial;
import xyz.marsavic.gfxlab.graphics3d.solids.Ball;
import xyz.marsavic.gfxlab.graphics3d.solids.Group;
import xyz.marsavic.gfxlab.graphics3d.solids.HalfSpace;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Identity;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Hash;

import static xyz.marsavic.gfxlab.math.Vec3.*;
import static xyz.marsavic.reactions.elements.Elements.*;


/** A small ball orbiting around a big ball once during the animation: a scene that depends on time. */
public record OrbitingBall() implements FFSceneT {

	@Override
	public F1<Solid, Double> fSolidT() {
		Vec3 c = xyz(0, 4, 0);
		HalfSpace floor = HalfSpace.pn(xyz(0, 0, -1), EZ, Material.matte(0.5));
		Ball ball = Ball.cr(c, 1, Material.matte(Color.hsb(0.0, 0.85, 0.8)));
		Material material = Material.matte(Color.hsb(1 / 3.0, 0.85, 0.8));

		return t -> Group.of(
				floor,
				ball,
				Ball.cr(c.add(zp(0, Vector.polar(1.8, t))), 0.4, material)
		);
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
										e(OrbitingBall.class)
								),
								e(TransformationFromSize.ToGeometricFitT0_.class),
								e(xyz(60, 640, 640)),
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
