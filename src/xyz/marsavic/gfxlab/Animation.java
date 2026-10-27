package xyz.marsavic.gfxlab;

import xyz.marsavic.gfxlab.arrays.*;
import xyz.marsavic.functions.F1;
import xyz.marsavic.resources.Rr;

/** An animation: gives the frame with the given index, as a matrix of pixel codes (a borrowed resource). */
public interface Animation extends F1<Rr<Matrix<Integer>>, Integer> {
}
