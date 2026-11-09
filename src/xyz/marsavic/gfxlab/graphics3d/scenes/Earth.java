package xyz.marsavic.gfxlab.graphics3d.scenes;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Animation;
import xyz.marsavic.gfxlab.TransformationFromSize;
import xyz.marsavic.gfxlab.aggregation.AggregatorFrameLast;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.arrays.MatrixColor;
import xyz.marsavic.gfxlab.entry_points.App;
import xyz.marsavic.gfxlab.graphics3d.FFSceneT;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.gfxlab.graphics3d.raytracers.RayTracerMaterial;
import xyz.marsavic.gfxlab.graphics3d.solids.Ball;
import xyz.marsavic.gfxlab.graphics3d.textures.ImageTexture;
import xyz.marsavic.gfxlab.graphics3d.textures.ImageTextureNearest;
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
 * The Earth: a ball with an image texture in the equirectangular projection (longitude and latitude, linearly),
 * rotated around its axis eastwards by the given angle, in turns.
 * The image: NASA Goddard Space Flight Center (Reto Stöckli, Robert Simmon), public domain.
 */
public record Earth(
		double rotation
) implements FFSceneT {
	
	@Override
	public Solid solid() {
		MatrixColor image = UtilsFX.matrixFromImage(Resources.class.getResourceAsStream("textures/earth.jpg"));
		ImageTexture earth = new ImageTextureNearest(image);
		
		// The ball's v goes from -1 (south pole) to 1 (north pole), the image's v from 0 to 1.
		return Ball.cr(xyz(0, 2, 0), 1, uv -> earth.at(Vector.xy(uv.x() - rotation, (uv.y() + 1) / 2)));
	}
	
	
	// ================================================================================================================


	static void main() { App.run(); }


	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorFrameLast::new),
								e(RayTracerMaterial.class,
										e(Earth.class, e(0.2))
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
