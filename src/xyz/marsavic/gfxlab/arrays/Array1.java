package xyz.marsavic.gfxlab.arrays;

import xyz.marsavic.functions.FO_I;
import xyz.marsavic.functions.FO_II;
import xyz.marsavic.geometry.Vector;


public interface Array1<E> extends FO_I<E> {
	
	int size();
	
	@Override
	E at(int x);
	
	
	static <E> Array1<E> of(int size, FO_I<E> f) {
		return new Array1<>() {
			@Override public int size() { return size; }
			@Override public E at(int x) { return f.at(x); }
		};
	}
	
	
	static void assertSize(Array1<?> a, int size) {
		if (a.size() != size) {
			throw new IllegalArgumentException("Array1 is not of the designated size.");
		}
	}

	
	static int assertEqualSizes(Array1<?> a, Array1<?>... o) {
		int size = a.size();
		for (Array1<?> b : o) {
			assertSize(b, size);
		}
		return size;
	}
	
}
