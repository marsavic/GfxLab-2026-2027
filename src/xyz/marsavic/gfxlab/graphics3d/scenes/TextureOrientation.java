package xyz.marsavic.gfxlab.graphics3d.scenes;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Animation;
import xyz.marsavic.gfxlab.TransformationFromSize;
import xyz.marsavic.gfxlab.aggregation.AggregatorFrameLast;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.entry_points.App;
import xyz.marsavic.gfxlab.graphics3d.Camera;
import xyz.marsavic.gfxlab.graphics3d.FFSceneT;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.gfxlab.graphics3d.cameras.Perspective;
import xyz.marsavic.gfxlab.graphics3d.cameras.TransformedCamera;
import xyz.marsavic.gfxlab.graphics3d.raytracers.RayTracerMaterial;
import xyz.marsavic.gfxlab.graphics3d.solids.*;
import xyz.marsavic.gfxlab.graphics3d.textures.ImageTexture;
import xyz.marsavic.gfxlab.graphics3d.textures.ImageTextureNearest;
import xyz.marsavic.gfxlab.math.Affine3;
import xyz.marsavic.gfxlab.resources.Resources;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Identity;
import xyz.marsavic.javafx.UtilsFX;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Hash;

import static xyz.marsavic.gfxlab.math.Vec3.*;
import static xyz.marsavic.reactions.elements.Elements.*;


/**
 * A floor, a wall, a ball, a box, and a cylinder, all with an image texture of the letter F. The letter has no
 * symmetry, so the orientation of the texture on each solid is visible.
 */
public record TextureOrientation() implements FFSceneT {
	
	/** The texture with the uv coordinates multiplied by the given factors. */
	private static F1<Material, Vector> scaled(ImageTexture texture, Vector factors) {
		return uv -> texture.at(uv.mul(factors));
	}
	
	@Override
	public Solid solid() {
		ImageTexture f = new ImageTextureNearest(UtilsFX.matrixFromImage(Resources.class.getResourceAsStream("textures/F.png")));
		return Group.of(
				HalfSpace.pn(xyz(0, 0, -1), EZ, f),                                                 // floor
				HalfSpace.pn(xyz(0, 7, 0), xyz(0, -1, 0), scaled(f, Vector.xy(0.5))),               // wall
				Ball.cr(xyz(-1.6, 4.2, -0.1), 0.9, scaled(f, Vector.xy(4, 1))),
				Box.$.pq(xyz(-0.45, 3.2, -1), xyz(0.55, 4.2, 0)).material(f),
				Cylinder.pdr(xyz(1.7, 4.6, 0), EZ, 0.7, scaled(f, Vector.xy(4, 1)))
		);
	}
	
	@Override
	public Camera camera() {
		// Raised and looking down; the window is sheared instead of rotated, so that vertical lines stay vertical.
		return new TransformedCamera(
				new Perspective(0.75),
				Affine3.unitVectors(EX, xyz(0, 1, -0.42), EZ).then(Affine3.translation(xyz(0, -0.5, 1.6)))
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
										e(TextureOrientation.class)
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
