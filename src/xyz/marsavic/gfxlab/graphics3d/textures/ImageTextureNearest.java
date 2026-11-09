package xyz.marsavic.gfxlab.graphics3d.textures;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.arrays.MatrixColor;


/** An image texture without interpolation: the color of the pixel containing the point. */
public class ImageTextureNearest extends ImageTexture {

	public ImageTextureNearest(MatrixColor image) {
		super(image);
	}


	@Override
	protected Color colorAt(Vector p) {
		return image.at(p);
	}

}
