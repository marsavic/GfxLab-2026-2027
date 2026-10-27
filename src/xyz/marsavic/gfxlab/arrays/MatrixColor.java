package xyz.marsavic.gfxlab.arrays;

import xyz.marsavic.gfxlab.*;
import xyz.marsavic.geometry.Vector;


public class MatrixColor implements Matrix<Color> {
	
	private final int width, height;
	private final int width3;
	private final double[] data;  // Can change to float for performance.
	
	
	public MatrixColor(Vector size, Color initialValue) {
		width = size.xInt();
		width3 = width * 3;
		height = size.yInt();
		data = new double[height * width3];
		
		if (initialValue != null) {
			fill(initialValue);
		}
	}
	
	
	public MatrixColor(Vector size) {
		this(size, null);
	}
	
	
	public int height() {
		return height;
	}

	
	public int width() {
		return width;
	}
	
	
	@Override
	public Vector size() {
		return Vector.xy(width, height);
	}
	
	
	@Override
	public Color at(int x, int y) {
		int i = 3 * (y * width + x);
		return Color.rgb(data[i  ], data[i+1], data[i+2]);
	}
	
	
	@Override
	public void set(int x, int y, Color value) {
		int i = 3 * (y * width + x);
		data[i  ] = value.r();
		data[i+1] = value.g();
		data[i+2] = value.b();
	}
	
	
	public void fillBlack() {
		UtilsGL.parallel.parallel(height, y -> {
			int i0 = y * width3;
			int i1 = i0 + width3;
			for (int i = i0; i < i1; i++) {
				data[i] = 0;
			}
		});
	}

	
	@Override
	public void copyFrom(Array2<Color> source) {
		if (source instanceof MatrixColor mcSource) {
			copyFrom(mcSource);
			return;
		}
		//noinspection RedundantCast
		((Matrix<Color>)this).copyFrom(source);
	}

	
	public void copyFrom(MatrixColor source) {
		Array2.assertEqualSizes(this, source);
		System.arraycopy(source.data, 0, data, 0, source.data.length);
/*
		// Optimize: Test if doing it in parallel is faster.
		UtilsGL.parallel(height, y -> {
			int o = y * width;
			System.arraycopy(source.array(), o, data, o, width);
		});
*/
	}
	
	
	// ------------------------------------------------------------------------------------------------
	
	
	
	public static void add(Array2<Color> a, Array2<Color> b, Matrix<Color> result) {
		if (a instanceof MatrixColor mcA && b instanceof MatrixColor mcB && result instanceof MatrixColor mcResult) {
			add(mcA, mcB, mcResult);
			return;
		}
		
		Vector size = Array2.assertEqualSizes(result, a, b);

		int sizeX = size.xInt();
		UtilsGL.parallel.parallelY(size, y -> {
			for (int x = 0; x < sizeX; x++) {
				result.set(x, y, a.at(x, y).add(b.at(x, y)));
			}
		});
	}

	public static void add(MatrixColor a, MatrixColor b, MatrixColor result) {
		Array2.assertEqualSizes(result, a, b);

		UtilsGL.parallel.parallel(result.height, y -> {        // TODO A block doesn't have to be a row, try other lengths.
			int i0 = y * result.width3;
			int i1 = i0 + result.width3;
			for (int i = i0; i < i1; i++) {
				result.data[i] = a.data[i] + b.data[i];
			}
		});		
	}
	
	
	public static Array2<Color> add(Array2<Color> a, Array2<Color> b) {
		Matrix<Color> result = new MatrixColor(a.size());
		add(a, b, result);
		return result;
	}
	
	
	
	public static void addInPlace(Matrix<Color> toChange, Array2<Color> byHowMuch) {
		if (toChange instanceof MatrixColor mcToChange && byHowMuch instanceof MatrixColor mcByHowMuch) {
			addInPlace(mcToChange, mcByHowMuch);
			return;
		}
		Vector size = Array2.assertEqualSizes(toChange, byHowMuch);
		
		int sizeX = size.xInt();
		UtilsGL.parallel.parallelY(size, y -> {
			for (int x = 0; x < sizeX; x++) {
				toChange.set(x, y, toChange.at(x, y).add(byHowMuch.at(x, y)));
			}
		});
	}
	
	public static void addInPlace(MatrixColor toChange, MatrixColor byHowMuch) {
		Array2.assertEqualSizes(toChange, byHowMuch);

		UtilsGL.parallel.parallel(toChange.height, y -> {
			int i0 = y * toChange.width3;
			int i1 = i0 + toChange.width3;
			for (int i = i0; i < i1; i++) {
				toChange.data[i] += byHowMuch.data[i];
			}
		});		
	}



	public static void mul(Array2<Color> a, double k, Matrix<Color> result) {
		if (a instanceof MatrixColor mcA && result instanceof MatrixColor mcResult) {
			mul(mcA, k, mcResult);
			return;
		}
		Vector size = Array2.assertEqualSizes(result, a);
		
		int sizeX = size.xInt();
		UtilsGL.parallel.parallelY(size, y -> {
			for (int x = 0; x < sizeX; x++) {
				result.set(x, y, a.at(x, y).mul(k));
			}
		});
	}
	
	public static void mul(MatrixColor a, double k, MatrixColor result) {
		Array2.assertEqualSizes(result, a);
		
		UtilsGL.parallel.parallel(result.height, y -> {
			int i0 = y * result.width3;
			int i1 = i0 + result.width3;
			for (int i = i0; i < i1; i++) {
				result.data[i] = a.data[i] * k;
			}
		});	
	}
	
	public static Array2<Color> mul(Array2<Color> a, double k) {
		Matrix<Color> result = new MatrixColor(a.size());
		mul(a, k, result);
		return result;
	}
	
	
	public static void mulInPlace(Matrix<Color> toChange, double factor) {
		if (toChange instanceof MatrixColor mcToChange) {
			mulInPlace(mcToChange, factor);
			return;
		}
		Vector size = toChange.size();
		
		int sizeX = size.xInt();
		UtilsGL.parallel.parallelY(size, y -> {
			for (int x = 0; x < sizeX; x++) {
				toChange.set(x, y, toChange.at(x, y).mul(factor));
			}
		});
	}
	
	public static void mulInPlace(MatrixColor toChange, double factor) {
		UtilsGL.parallel.parallel(toChange.height, y -> {
			int i0 = y * toChange.width3;
			int i1 = i0 + toChange.width3;
			for (int i = i0; i < i1; i++) {
				toChange.data[i] *= factor;
			}
		});
	}	
	
}
