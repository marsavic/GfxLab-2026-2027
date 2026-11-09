package xyz.marsavic.gfxlab.graphics3d.scenes;

import xyz.marsavic.gfxlab.entry_points.*;
import xyz.marsavic.gfxlab.math.*;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.*;
import xyz.marsavic.gfxlab.aggregation.AggregatorFrameLast;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.graphics3d.*;
import xyz.marsavic.gfxlab.graphics3d.cameras.Perspective;
import xyz.marsavic.gfxlab.graphics3d.cameras.TransformedCamera;
import xyz.marsavic.gfxlab.graphics3d.raytracers.RayTracerMaterial;
import xyz.marsavic.gfxlab.graphics3d.solids.Box;
import xyz.marsavic.gfxlab.graphics3d.solids.Cylinder;
import xyz.marsavic.gfxlab.graphics3d.solids.Group;
import xyz.marsavic.gfxlab.graphics3d.textures.Checkerboard;
import xyz.marsavic.gfxlab.graphics3d.textures.Grid;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.matrixcolor_to_colortransforms.AutoSoft;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Hash;

import java.util.Collection;
import java.util.List;

import static xyz.marsavic.gfxlab.math.Vec3.*;
import static xyz.marsavic.reactions.elements.Elements.*;


/**
 * A tunnel (an inverted cylinder) with a tilted cylinder and a box inside.
 * Tests cylinder intersections, normals and uv coordinates, for both positive and negative radii, and box uv coordinates.
 */
public record CylinderTest(
		double phi,        // Rotation of the tilted cylinder's axis around the z axis, in turns.
		double cameraD
) implements FFSceneT {

	@Override
	public Solid solid() {
		Cylinder tunnel = Cylinder.pdr(ZERO, EY, -1.5,
				new Checkerboard(Vector.xy(1.0 / 16, 0.5), Material.matte(0.8), Material.matte(0.4)));

		Vec3 axis = xyz(Math.cos(phi * 2 * Math.PI), 0.5, Math.sin(phi * 2 * Math.PI));
		Cylinder cylinder = Cylinder.pdr(xyz(0, 5, 0), axis, 0.35,
				new Checkerboard(Vector.xy(1.0 / 8, 0.5),
						new Material(Phong.matte(Color.hsb(0.08, 0.7, 0.9)).specular(Color.gray(0.06)).shininess(32)),
						new Material(Phong.matte(Color.hsb(0.58, 0.7, 0.9)).specular(Color.gray(0.06)).shininess(32))));

		Box box = Box.$.pq(xyz(0.1, 3.0, -1.0), xyz(0.7, 2.4, -0.4))
				.material(Grid.standardQuarter(Color.hsb(0.33, 0.5, 0.9)));

		return Group.of(tunnel, cylinder, box);
	}

	@Override
	public Collection<Light> lights() {
		return List.of(
				Light.pc(xyz(-0.6, 1.5, 0.6), Color.gray(2.0)),
				Light.pc(xyz( 0.6, 6.0, 0.6), Color.gray(4.0))
		);
	}
	
	@Override
	public Camera camera() {
		return new TransformedCamera(
						new Perspective(1 / 3.0),
						Affine3.chain(
								Affine3.translation(Vec3.xyz(0, cameraD, 0))
						)	
				);
	}
	


	// ================================================================================================================


	static void main() { App.run(); }
	
	
	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorFrameLast::new),
								e(RayTracerMaterial.class,
										e(CylinderTest.class, e(0.11), e(-1.5))
								),
								e(TransformationFromSize.ToGeometricFitT0_.class),
								e(xyz(1, 640, 640)),
								e(true),
								e(false),
								e(Hash.class, e(0x8EE6B0C4E02CA7B2L))
						),
						e(ToneMapping2.class,
								e(AutoSoft.class, e(0x1p-4), e(1.0))
						)
				);
	}

}
