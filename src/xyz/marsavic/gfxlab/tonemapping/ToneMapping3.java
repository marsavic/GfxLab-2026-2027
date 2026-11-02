package xyz.marsavic.gfxlab.tonemapping;

import xyz.marsavic.functions.F1;
import xyz.marsavic.gfxlab.Animation;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Frames;
import xyz.marsavic.gfxlab.arrays.Matrix;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.resources.Br;
import xyz.marsavic.resources.Rr;


/** An animation obtained by tone mapping the frames given by frFrame (usually by an aggregator). */
public record ToneMapping3(
		Frames<Color> frFrame,
		F1<Rr<Matrix<Integer>>, Br<Matrix<Color>>> frToneMapping2
) implements Animation {

	@Override
	public Rr<Matrix<Integer>> at(Integer iFrame) {
		Rr<Matrix<Color>> rFrame = frFrame.at(iFrame);
		Rr<Matrix<Integer>> res = frToneMapping2.at(rFrame.lend());
		rFrame.release();
		return res;
	}

	@Override
	public Vec3 size() {
		return frFrame.size();
	}

	@Override
	public boolean repeats() {
		return frFrame.repeats();
	}

}
