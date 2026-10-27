package xyz.marsavic.gfxlab.arrays;

import xyz.marsavic.gfxlab.*;
import xyz.marsavic.functions.F1;
import xyz.marsavic.functions.F2;
import xyz.marsavic.geometry.Vector;


/** A two-dimensional array whose elements can be set. */
public interface Matrix<E> extends Array2<E> {
	
	
	/** Sets the element at (x, y). */
	void set(int x, int y, E value);
	
	
	/** Sets the element at the integer parts of the coordinates of p. */
	default void set(Vector p, E value) {
		set(p.xInt(), p.yInt(), value);
	}
	
	
	/** Sets all elements to value. */
	default void fill(E value) {
		UtilsGL.parallel.parallelYVec(size(), p -> set(p, value));
	}
	
	
	/** Sets each element to the value of f at its position. */
	default void fill(F1<E, Vector> f) {
		UtilsGL.parallel.parallelYVec(size(), p -> set(p, f.at(p)));
	}
	
	
	/** Sets each element to the value of f at its coordinates (x, y). */
	default void fill(F2<E, Integer, Integer> f) {
		UtilsGL.parallel.parallelY(size(), (x, y) -> set(x, y, f.at(x, y)));
	}
	
	
	/** Copies all elements from o, which must have the same size. */
	default void copyFrom(Array2<E> o) {
		Vector size = Array2.assertEqualSizes(this, o);
		UtilsGL.parallel.parallelYVec(size, p -> set(p, o.at(p)));
	}
	
	// ...........................
	
	
}
