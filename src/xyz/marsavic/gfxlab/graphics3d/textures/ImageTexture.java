package xyz.marsavic.gfxlab.graphics3d.textures;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.arrays.MatrixColor;
import xyz.marsavic.gfxlab.graphics3d.Material;


/**
 * A texture given by an image, covering the square [0, 1]^2 of uv coordinates and repeated in both directions.
 * Subclasses define how the color at a point of the image is obtained from the colors of the pixels.
 */
public abstract class ImageTexture implements F1<Material, Vector> {

	protected final MatrixColor image;


	protected ImageTexture(MatrixColor image) {
		this.image = image;
	}


	/** The color at the point p, in pixel coordinates (from (0, 0) to the image size). */
	protected abstract Color colorAt(Vector p);


	@Override
	public Material at(Vector uv) {
		// v goes up, while the rows of the image go down.
		Vector p = uv.inversedY().mod().mul(image.size());
		return Material.matte(colorAt(p));
	}

}
