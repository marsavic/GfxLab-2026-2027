package xyz.marsavic.gfxlab;

import xyz.marsavic.functions.F1;
import xyz.marsavic.gfxlab.arrays.Matrix;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.resources.Rr;


/**
 * The frames of an animation: gives the frame with the given index, as a matrix (a resource that the caller must
 * release). The size is (number of frames, width, height). If the frames repeat, the frame index is taken modulo the
 * number of frames.
 */
public interface Frames<T> extends F1<Rr<Matrix<T>>, Integer> {

	Vec3 size();

	boolean repeats();

	/** The index of the frame given for the index t. Equal indices give the same frame. */
	default int iFrame(int t) {
		return repeats() ? Math.floorMod(t, (int) size().x()) : t;
	}

}
