package xyz.marsavic.gfxlab.tonemapping;

import xyz.marsavic.functions.F1;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.UtilsGL;
import xyz.marsavic.gfxlab.arrays.Array2;
import xyz.marsavic.gfxlab.arrays.Matrix;
import xyz.marsavic.resources.Br;
import xyz.marsavic.resources.Rr;

/**
 * The tone mapping of one image: chooses a color transformation for the image, applies it to each pixel, and
 * converts the colors into pixel codes.
 */
public record ToneMapping2(
		F1<ColorTransform, Array2<Color>> fColorTransformToMatrixColor
) implements F1<Rr<Matrix<Integer>>, Br<Matrix<Color>>> {
	
	@Override
	public Rr<Matrix<Integer>> at(Br<Matrix<Color>> bMC) {
		return bMC.f(mC -> {
			ColorTransform f = fColorTransformToMatrixColor.at(mC);
			Rr<Matrix<Integer>> rMatI = UtilsGL.matricesInt.obtain(mC.size(), true);
			rMatI.a(mI -> mI.fill(p -> f.at(mC.at(p)).code()));
			return rMatI;
		});
	}

}
