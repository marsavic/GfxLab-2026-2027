package xyz.marsavic.gfxlab.graphics3d.scenes;

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
import xyz.marsavic.gfxlab.graphics3d.solids.Ball;
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

import java.util.stream.IntStream;

import static xyz.marsavic.gfxlab.math.Vec3.*;
import static xyz.marsavic.reactions.elements.Elements.*;


/**
 * The Earth as in {@link Earth}, at the given date: a fraction of the year in [0, 1], from January 1 to the next January 1.
 * The texture is interpolated linearly between the monthly images. Each monthly image is placed at the middle of its
 * month, with all months of equal length (1/12 of the year).
 * The images: NASA Earth Observatory, Blue Marble: Next Generation (Reto Stöckli), 2004, public domain.
 * https://science.nasa.gov/earth/earth-observatory/blue-marble-next-generation/base-map/
 */
public record EarthSeasons(
		double date,
		double rotationZ,
		double rotationX
) implements FFSceneT {

	/** The monthly textures, loaded once, from January (index 0) to December (index 11). */
	private static final ImageTexture[] months = IntStream.rangeClosed(1, 12)
			.mapToObj(n -> new ImageTextureNearest(UtilsFX.matrixFromImage(Resources.class.getResourceAsStream("textures/earth-monthly/%02d.jpg".formatted(n)))))
			.toArray(ImageTexture[]::new);


	@Override
	public Solid solid() {
		double m = date * 12 - 0.5;
		int i = (int) Math.floor(m);
		double k = m - i;

		ImageTexture texture0 = month(i);
		ImageTexture texture1 = month(i + 1);

		// The ball's v goes from -1 (south pole) to 1 (north pole), the image's v from 0 to 1.
		return Ball.cr(ZERO, 1, uv -> {
			Vector p = Vector.xy(uv.x(), (uv.y() + 1) / 2);
			return Material.lerp(texture0.at(p), texture1.at(p), k);
		});
	}
	
	
	@Override
	public Camera camera() {
		return new TransformedCamera(
				new Perspective(0.3),
				Affine3.chain(
						Affine3.translation(xyz(0, -4, 0)),
						Affine3.rotationAboutX(rotationX),
						Affine3.rotationAboutZ(rotationZ)
				)
		);
	}
	
	/** The texture of the month i, counted from 0 (January) cyclically. */
	private static ImageTexture month(int i) {
		return months[Math.floorMod(i, 12)];
	}


	// ================================================================================================================


	static void main() { App.run(); }


	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorFrameLast::new),
								e(RayTracerMaterial.class,
										e(EarthSeasons.class, e(0.5), e(0.0), e(0.0))
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
