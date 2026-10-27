package xyz.marsavic.gfxlab.arrays;

import xyz.marsavic.gfxlab.math.*;
import xyz.marsavic.functions.FO_III;


public interface Array3<E> extends FO_III<E> {
	
	Vec3 size();
	
	@Override
	E at(int x, int y, int z);
	
	default E at(Vec3 p) {
		return at((int) p.x(), (int) p.y(), (int) p.z());
	}
	
	
	default Array2<E> slice0(int x) {
		return Array2.of(size().p12(), (y, z) -> at(x, y, z));
	} 
	
	
	static <E> Array3<E> of(Vec3 size, FO_III<E> f) {
		return new Array3<>() {
			@Override public Vec3 size() { return size; }			
			@Override public E at(int x, int y, int z) { return f.at(x, y, z); }
		};
	}
	
	
	static void assertSize(Array3<?> a, Vec3 size) {
		if (!a.size().equals(size)) {
			throw new IllegalArgumentException("Array3 is not of the designated size.");
		}
	}
	
	
	static Vec3 assertEqualSizes(Array3<?> a, Array3<?> b) {
		if (!b.size().equals(a.size())) {
			throw new IllegalArgumentException("Array3 sizes are not equal.");
		}
		return a.size();
	}
	
	
}
