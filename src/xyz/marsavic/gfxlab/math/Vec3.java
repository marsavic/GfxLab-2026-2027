package xyz.marsavic.gfxlab.math;

import xyz.marsavic.functions.FD_D;
import xyz.marsavic.functions.FD_DD;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.utils.Hash;
import xyz.marsavic.utils.Hashable;
import xyz.marsavic.utils.Numeric;

import java.util.random.RandomGenerator;

/** An immutable three-dimensional vector (x, y, z). Also used for points, sizes, and (t, x, y) triples. */
public record Vec3(
		double x,
		double y,
		double z
) implements Hashable {
	private static final Hash HASH = new Hash(0xAFD51A92B895D35FL);
	
	public static final Vec3 ZERO = xyz(0, 0, 0);
	public static final Vec3 ONES = xyz(1, 1, 1);
	public static final Vec3 EX   = xyz(1, 0, 0);
	public static final Vec3 EY   = xyz(0, 1, 0);
	public static final Vec3 EZ   = xyz(0, 0, 1);
	public static final Vec3 EXY  = xyz(1, 1, 0);
	public static final Vec3 EYZ  = xyz(0, 1, 1);
	public static final Vec3 EZX  = xyz(1, 0, 1);
	public static final Vec3 EXYZ = xyz(1, 1, 1);
	public static final Vec3[] E  = {EX, EY, EZ};
	
	public static final Vec3 P012 = xyz(0, 1, 2);
	public static final Vec3 P021 = xyz(0, 2, 1);
	public static final Vec3 P102 = xyz(1, 0, 2);
	public static final Vec3 P120 = xyz(1, 2, 0);
	public static final Vec3 P201 = xyz(2, 0, 1);
	public static final Vec3 P210 = xyz(2, 1, 0);
	public static final Vec3[] PERMUTATIONS = {P012, P021, P102, P120, P201, P210};
	
	
	/** The vector (x, y, z). */
	public static Vec3 xyz(double x, double y, double z) {
		return new Vec3(x, y, z);
	}
	
	
	public static Vec3 fromArray(int[] i) {
		return xyz(i[0], i[1], i[2]);
	}
	
	
	public static Vec3 fromArray(double[] i) {
		return xyz(i[0], i[1], i[2]);
	}
	
	
	/** The vector (x, p.x, p.y). */
	public static Vec3 xp(double x, Vector p) {
		return xyz(x, p.x(), p.y());
	}
	
	
	/** The vector (p.x, y, p.y). */
	public static Vec3 yp(double y, Vector p) {
		return xyz(p.x(), y, p.y());
	}
	
	
	/** The vector (p.x, p.y, z). */
	public static Vec3 zp(double z, Vector p) {
		return xyz(p.x(), p.y(), z);
	}
		
	
	/** The coordinate with the index i (0 for x, 1 for y, 2 for z). */
	public double get(int i) {
		return switch (i) {
			case 0 -> x();
			case 1 -> y();
			case 2 -> z();
			default -> throw new IllegalArgumentException();
		};
	}
	
	
	public double[] toArray() {
		return new double[] { x(), y(), z() };
	}
	
	
	public int[] toArrayInt() {
		return new int[] { (int) x(), (int) y(), (int) z() };
	}
	
	
	/** The sum this + o. */
	public Vec3 add(Vec3 o) {
		return xyz(x() + o.x(), y() + o.y(), z() + o.z());
	}
	
	
	/** The difference this - o. */
	public Vec3 sub(Vec3 o) {
		return xyz(x() - o.x(), y() - o.y(), z() - o.z());
	}
	
	
	/** This vector multiplied by the scalar k. */
	public Vec3 mul(double k) {
		return xyz(x() * k, y() * k, z() * k);
	}
	
	
	/** The componentwise product of this and o. */
	public Vec3 mul(Vec3 o) {
		return xyz(x() * o.x(), y() * o.y(), z() * o.z());
	}
	
	
	/** this * m + a, componentwise. */
	public Vec3 mulAdd(Vec3 m, Vec3 a) {
		return xyz(x() * m.x() + a.x(), y() * m.y() + a.y(), z() * m.z() + a.z());
	}

	
	/** this * m + a. */
	public Vec3 mulAdd(double m, Vec3 a) {
		return xyz(x() * m + a.x(), y() * m + a.y(), z() * m + a.z());
	}

	
	/** this * m + a, componentwise, computed with fused multiply-add (one rounding per component). */
	public Vec3 fma(Vec3 m, Vec3 a) {
		return xyz(Math.fma(x(), m.x(), a.x()), Math.fma(y(), m.y(), a.y()), Math.fma(z(), m.z(), a.z()));
	}
	
	/** this * m + a, computed with fused multiply-add (one rounding per component). */
	public Vec3 fma(double m, Vec3 a) {
		return xyz(Math.fma(x(), m, a.x()), Math.fma(y(), m, a.y()), Math.fma(z(), m, a.z()));
	}
	
	
	/** This vector divided by the scalar k. */
	public Vec3 div(double k) {
		return xyz(x() / k, y() / k, z() / k);
	}
	
	
	/** The componentwise quotient of this and o. */
	public Vec3 div(Vec3 o) {
		return xyz(x() / o.x(), y() / o.y(), z() / o.z());
	}
	
	
	/** The componentwise reciprocal (1/x, 1/y, 1/z). */
	public Vec3 reciprocal() {
		return xyz(1.0 / x(), 1.0 / y(), 1.0 / z());
	}
	
	
	/** The squared length; cheaper than the length, since there is no square root. */
	public double lengthSquared() {
		return x() * x() + y() * y() + z() * z();
	}
	
	
	/** The length (Euclidean norm). */
	public double length() {
		return Math.sqrt(lengthSquared());
	}
	
	
	/** The unit vector with the same direction. */
	public Vec3 normalized_() {
		return div(length());
	}
	
	
	/** The vector with the same direction and the length l. */
	public Vec3 normalizedTo(double l) {
		return mul(l / length());
	}
	
	
	/** The opposite vector, -this. */
	public Vec3 inverse() {
		return xyz(-x(), -y(), -z());
	}
	
	
	/** The dot product. */
	public double dot(Vec3 o) {
		return x() * o.x() + y() * o.y() + z() * o.z();
	}
	
	
	/** The cross product this × o. */
	public Vec3 cross(Vec3 o) {
		return xyz(
				y() * o.z() - z() * o.y(),
				z() * o.x() - x() * o.z(),
				x() * o.y() - y() * o.x()
		);
	}
	
	
	/** The angle between this and o, in turns, in [0, 1/2]. */
	public double angleT(Vec3 o) {
		return Numeric.atan2T(cross(o).length(), dot(o));     // More precise than acos(a·b / |a||b|) for small angles and angles close to 1/2.
	}
	
	
	/**
	 * The determinant of the matrix with columns a, b, c. Positive iff (a, b, c) is a positive basis.
	 * For example, the points p, q, r are counterclockwise as seen from o iff det(q - p, r - p, o - p) > 0.
	 */
	public static double det(Vec3 a, Vec3 b, Vec3 c) {
		return a.cross(b).dot(c);
	}
	
	
	/** The projection of this onto the line with the direction d. */
	public Vec3 projection(Vec3 d) {
		return d.mul(this.dot(d) / d.lengthSquared());
	}
	
	
	/** The projection of this onto the line with the unit direction d_. */
	public Vec3 projectionN(Vec3 d_) {
		return d_.mul(this.dot(d_));
	}
	
	
	/** The rejection of this from the direction d: the component orthogonal to d. */
	public Vec3 rejection(Vec3 d) {
		return this.sub(projection(d));
	}
	
	
	/** The rejection of this from the unit direction d_: the component orthogonal to d_. */
	public Vec3 rejectionN(Vec3 d_) {
		return this.sub(projectionN(d_));
	}
	
	
	/** The smallest coordinate. */
	public double min() {
		return Math.min(Math.min(x(), y()), z());
	}
	
	
	/** The largest coordinate. */
	public double max() {
		return Math.max(Math.max(x(), y()), z());
	}
	
	
	public Vec3 minIndicator() {
		if (x() < y()) {
			return x() < z() ? Vec3.EX : Vec3.EZ;
		} else {
			return y() < z() ? Vec3.EY : Vec3.EZ;
		}
	}
	
	
	public Vec3 maxIndicator() {
		if (x() > y()) {
			return x() > z() ? Vec3.EX : Vec3.EZ;
		} else {
			return y() > z() ? Vec3.EY : Vec3.EZ;
		}
	}
	
	
	/** The index of the smallest coordinate. */
	public int minIndex() {
		if (x() < y()) {
			return x() < z() ? 0 : 2;
		} else {
			return y() < z() ? 1 : 2;
		}
	}
	
	
	/** The index of the largest coordinate. */
	public int maxIndex() {
		if (x() > y()) {
			return x() > z() ? 0 : 2;
		} else {
			return y() > z() ? 1 : 2;
		}
	}
	
	
	/** The indices of the coordinates in increasing order of the coordinates: (i, j, k) with get(i) <= get(j) <= get(k). */
	public Vec3 ranks() {
		if (x() < y()) {
			if (y() < z()) {
				return P012;
			} else {
				if (x() < z()) {
					return P021;
				} else {
					return P201;
				}
			}
		} else {
			if (x() < z()) {
				return P102;
			} else {
				if (y() < z()) {
					return P120;
				} else {
					return P210;
				}
			}
		}
	}
	
	
	/** The componentwise sign (-1, 0, or 1). */
	public Vec3 sign() {
		return xyz(Numeric.sign(x()), Numeric.sign(y()), Numeric.sign(z()));
	}
	
	
	/** The componentwise absolute value. */
	public Vec3 abs() {
		return xyz(Math.abs(x()), Math.abs(y()), Math.abs(z()));
	}
	
	
	public boolean allZero() {
		return (x() == 0) && (y() == 0) && (z() == 0);
	}
	
	public boolean anyZero() {
		return (x() == 0) || (y() == 0) || (z() == 0);
	}
	
	
	public Vec3 floor() {
		return xyz(Math.floor(x()), Math.floor(y()), Math.floor(z()));
	}
	
	public Vec3 floor(Vec3 d) {
		return this.div(d).floor().mul(d);
	}
	
	public Vec3 ceil() {
		return xyz(Math.ceil(x()), Math.ceil(y()), Math.ceil(z()));
	}
	
	public Vec3 ceil(Vec3 d) {
		return this.div(d).ceil().mul(d);
	}
	
	public Vec3 round() {
		return xyz(Math.round(x()), Math.round(y()), Math.round(z()));
	}
	
	public Vec3 round(Vec3 d) {
		return this.div(d).round().mul(d);
	}
	
	/** Snaps each component to floor or ceil, depending on whether the corresponding component of s is not one. */
	
	public Vec3 snap(Vec3 s) {
		return Vec3.xyz (
				s.x() != 1 ? Math.floor(x()) : Math.ceil(x()),
				s.y() != 1 ? Math.floor(y()) : Math.ceil(y()),
				s.z() != 1 ? Math.floor(z()) : Math.ceil(z())
		);
	}
	
	
	
	public double product() {
		return x() * y() * z();
	}
	
	
	public Vec3 f(FD_D f) {
		return Vec3.f(f, this);
	}
	
	
	public Vec3 f(FD_DD f, Vec3 v) {
		return Vec3.f(f, this, v);
	}
	
	
	public Vec3[] split() {
		return new Vec3[] {
				Vec3.xyz(x(), 0, 0),
				Vec3.xyz(0, y(), 0),
				Vec3.xyz(0, 0, z()),
		};
	}
	
	
	/** The vector with only the coordinate i kept, and the others set to 0. */
	public Vec3 only(int i) {
		return switch (i) {
			case 0 -> Vec3.xyz(x(), 0, 0);
			case 1 -> Vec3.xyz(0, y(), 0);
			case 2 -> Vec3.xyz(0, 0, z());
			default -> throw new IllegalArgumentException();
		};
	}
	
	
	/** The 2D vector (x, y). */
	public Vector p01() {
		return Vector.xy(x(), y());
	}
	
	/** The 2D vector (y, z); for a (t, x, y) triple, the point (x, y). */
	public Vector p12() {
		return Vector.xy(y(), z());
	}
	
	/** The result is obtained by applying the affine transform that maps 0 to -1 and 1 to 1. */
	public Vec3 ZOtoMP() {
		return mul(2).sub(Vec3.EXYZ);
	}
	
	/** The result is obtained by applying the affine transform that maps -1 to 0 and 1 to 1. */
	public Vec3 MPtoZO() {
		return add(Vec3.EXYZ).mul(0.5);
	}
	
	
	
	@Override
	public String toString() {
		return String.format("(%6.2f, %6.2f, %6.2f)", x(), y(), z());
	}
	
	
	public static Vec3 f(FD_D f, Vec3 u) {
		return Vec3.xyz(
				f.at(u.x()),
				f.at(u.y()),
				f.at(u.z())
		);
	}
	
	
	public static Vec3 f(FD_DD f, Vec3 u, Vec3 v) {
		return Vec3.xyz(
				f.at(u.x(), v.x()),
				f.at(u.y(), v.y()),
				f.at(u.z(), v.z())
		);
	}
	
	
	public static Vec3 min(Vec3 a, Vec3 b) {
		return xyz(
				Math.min(a.x(), b.x()),
				Math.min(a.y(), b.y()),
				Math.min(a.z(), b.z())
		);
	}
	
	
	public static Vec3 max(Vec3 a, Vec3 b) {
		return xyz(
				Math.max(a.x(), b.x()),
				Math.max(a.y(), b.y()),
				Math.max(a.z(), b.z())
		);
	}
	
	
	/** The linear interpolation (1 - t) v0 + t v1. */
	public static Vec3 lerp(Vec3 v0, Vec3 v1, double t) {
		return v0.mul(1 - t).add(v1.mul(t));
	}
	
	
	/** A vector with random coordinates in [0, 1), determined by hash. */
	public static Vec3 random(Hash hash) {
		return random(hash.rng());
	}
	
	
	public static Vec3 random(RandomGenerator rng) {
		return Vec3.xyz(rng.nextDouble(), rng.nextDouble(), rng.nextDouble());
	}
	
	
	@Override
	public Hash hash(Hash hash) {
		return hash.add(x()).add(y()).add(z());
	}
	
	@Override
	public int hashCode() {
		return hashCode(HASH);		
	}
	
}
