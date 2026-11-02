package xyz.marsavic.gfxlab.arrays;


import xyz.marsavic.gfxlab.*;
import xyz.marsavic.geometry.Vector;

import java.util.Arrays;


public final class MatrixInts implements Matrix<Integer> {
	
	private final int width, height;
	private final int[] data;
	
	
	
	public MatrixInts(Vector size) {
		width = size.xInt();
		height = size.yInt();
		data = new int[width * height];
	}
	
	
	public int height() {
		return height;
	}
	
	
	public int width() {
		return width;
	}
	
	
	@Override
	public Vector size() {
		return Vector.xy(width(), height());
	}
	
	
	@Override
	public Integer at(int x, int y) {
		return data[y * width + x];
	}
	
	
	@Override
	public void set(int x, int y, Integer value) {
		data[y * width + x] = value;
	}
	
	
	@Override
	public void copyFrom(Array2<Integer> source) {
		if (source instanceof MatrixInts miSource) {
			copyFrom(miSource);
			return;
		}
		Array2.assertEqualSizes(this, source);
		
		UtilsGL.parallel.parallel(height, y -> {
			int o = y * width;
			for (int x = 0; x < width; x++) {
				data[o++] = source.at(x, y);
			}
		});
	}

	
	public void copyFrom(MatrixInts source) {
		Array2.assertEqualSizes(this, source);
		UtilsGL.parallel.parallel(height, y -> {
			int o = y * width;
			System.arraycopy(source.data, o, data, o, width);
		});
	}
	
	
	@Override
	public void fill(Integer value) {
		Arrays.fill(data, value);
	}
	
	
	public int[] array() {
		return data;
	}
	
}
