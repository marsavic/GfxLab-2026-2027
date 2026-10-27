package xyz.marsavic.gfxlab.math;


import xyz.marsavic.utils.Numeric;


/**
 * An affine transformation of 3D space, x -> M x + m, given by the 3x4 matrix of the first three rows of its 4x4
 * homogeneous matrix (the last row is 0, 0, 0, 1).
 */
public record Affine3(
		double m00, double m01, double m02, double m03,
		double m10, double m11, double m12, double m13,
		double m20, double m21, double m22, double m23
//               0,          0,          0,          1
) implements Transformation3 {
	
	/** The identity transformation. */
	public static Affine3 IDENTITY = new Affine3(
			1.0, 0.0, 0.0, 0.0,
			0.0, 1.0, 0.0, 0.0,
			0.0, 0.0, 1.0, 0.0
	);
	
	
	/** The linear transformation mapping EX, EY, EZ to ex, ey, ez. */
	public static Affine3 unitVectors(Vec3 ex, Vec3 ey, Vec3 ez) {
		return new Affine3(
				ex.x(), ey.x(), ez.x(), 0,
				ex.y(), ey.y(), ez.y(), 0,
				ex.z(), ey.z(), ez.z(), 0
		);
	}
	
	
	/** The composition: this transformation followed by t. */
	public Affine3 then(Affine3 t) {
		return new Affine3(
				t.m00 * m00 + t.m01 * m10 + t.m02 * m20, t.m00 * m01 + t.m01 * m11 + t.m02 * m21, t.m00 * m02 + t.m01 * m12 + t.m02 * m22, t.m00 * m03 + t.m01 * m13 + t.m02 * m23 + t.m03,
				t.m10 * m00 + t.m11 * m10 + t.m12 * m20, t.m10 * m01 + t.m11 * m11 + t.m12 * m21, t.m10 * m02 + t.m11 * m12 + t.m12 * m22, t.m10 * m03 + t.m11 * m13 + t.m12 * m23 + t.m13,
				t.m20 * m00 + t.m21 * m10 + t.m22 * m20, t.m20 * m01 + t.m21 * m11 + t.m22 * m21, t.m20 * m02 + t.m21 * m12 + t.m22 * m22, t.m20 * m03 + t.m21 * m13 + t.m22 * m23 + t.m23
		);
	}
	
	
	/** The image of the point v. */
	@Override
	public Vec3 at(Vec3 v) {
		return new Vec3(
				m00 * v.x() + m01 * v.y() + m02 * v.z() + m03,
				m10 * v.x() + m11 * v.y() + m12 * v.z() + m13,
				m20 * v.x() + m21 * v.y() + m22 * v.z() + m23
		);
	}
	
	
	/** The image of the vector v: only the linear part is applied, without the translation. */
	public Vec3 applyWithoutTranslationTo(Vec3 v) {
		return new Vec3(
				m00 * v.x() + m01 * v.y() + m02 * v.z(),
				m10 * v.x() + m11 * v.y() + m12 * v.z(),
				m20 * v.x() + m21 * v.y() + m22 * v.z()
		);
	}
	
	
	/** The inverse transformation; the determinant must not be 0. */
	public Affine3 inverse() {
		double det = determinant();
		return new Affine3(
				(m11 * m22 - m12 * m21) / det, -(m01 * m22 - m02 * m21) / det, (m01 * m12 - m02 * m11) / det, -(m01 * m12 * m23 + m02 * m13 * m21 + m03 * m11 * m22 - m03 * m12 * m21 - m02 * m11 * m23 - m01 * m13 * m22) / det,
				-(m10 * m22 - m12 * m20) / det, (m00 * m22 - m02 * m20) / det, -(m00 * m12 - m02 * m10) / det, (m00 * m12 * m23 + m02 * m13 * m20 + m03 * m10 * m22 - m03 * m12 * m20 - m02 * m10 * m23 - m00 * m13 * m22) / det,
				(m10 * m21 - m11 * m20) / det, -(m00 * m21 - m01 * m20) / det, (m00 * m11 - m01 * m10) / det, -(m00 * m11 * m23 + m01 * m13 * m20 + m03 * m10 * m21 - m03 * m11 * m20 - m01 * m10 * m23 - m00 * m13 * m21) / det
		);
	}
	
	
	/** The transposed linear part, without the translation. */
	public Affine3 transpose() {
		return new Affine3(
				m00, m10, m20, 0,
				m01, m11, m21, 0,
				m02, m12, m22, 0
		);
	}
	
	
	/** The determinant of the linear part. Negative iff the transformation changes orientation. */
	public double determinant() {
		//noinspection UnaryPlus
		return (
				+ (m00 * m11 * m22)
				+ (m01 * m12 * m20)
				+ (m02 * m10 * m21)
				- (m02 * m11 * m20)
				- (m01 * m10 * m22)
				- (m00 * m12 * m21)
		);
	}
	
	
	/** Whether the coordinate 0 of the image (e.g. the time) depends only on the coordinate 0. */
	public boolean isIndependent0() {
		return m01 == 0 && m02 == 0;		
	}
	
	
	@Override
	public Affine2 slice0(double x0) {
		return new Affine2(
				m11, m12, m13 + m10() * x0,
				m21, m22, m23 + m20() * x0
		);
	}
	
	
	/** The translation by d. */
	public static Affine3 translation(Vec3 d) {
		return new Affine3(
				1.0, 0.0, 0.0, d.x(),
				0.0, 1.0, 0.0, d.y(),
				0.0, 0.0, 1.0, d.z()
		);
	}
	
	/** The rotation about the x axis by the angle (in turns), counterclockwise when seen from the tip of the axis. */
	public static Affine3 rotationAboutX(double angle) {
		return new Affine3(
				1.0, 0.0, 0.0, 0.0,
				0.0, Numeric.cosT(angle), -Numeric.sinT(angle), 0.0,
				0.0, Numeric.sinT(angle), Numeric.cosT(angle), 0.0
		);
	}
	
	/** The rotation about the y axis by the angle (in turns), counterclockwise when seen from the tip of the axis. */
	public static Affine3 rotationAboutY(double angle) {
		return new Affine3(
				Numeric.cosT(angle), 0.0, Numeric.sinT(angle), 0.0,
				0.0, 1.0, 0.0, 0.0,
				-Numeric.sinT(angle), 0.0, Numeric.cosT(angle), 0.0
		);
	}
	
	/** The rotation about the z axis by the angle (in turns), counterclockwise when seen from the tip of the axis. */
	public static Affine3 rotationAboutZ(double angle) {
		return new Affine3(
				Numeric.cosT(angle), -Numeric.sinT(angle), 0.0, 0.0,
				Numeric.sinT(angle), Numeric.cosT(angle), 0.0, 0.0,
				0.0, 0.0, 1.0, 0.0
		);
	}
	
	/** The uniform scaling by c. */
	public static Affine3 scaling(double c) {
		return new Affine3(
				c, 0.0, 0.0, 0.0,
				0.0, c, 0.0, 0.0,
				0.0, 0.0, c, 0.0
		);
	}
	
	/** The scaling by c.x, c.y, c.z along the coordinate axes. */
	public static Affine3 scaling(Vec3 c) {
		return new Affine3(
				c.x(), 0.0, 0.0, 0.0,
				0.0, c.y(), 0.0, 0.0,
				0.0, 0.0, c.z(), 0.0
		);
	}
	
	
	/** One possible linear transformation mapping EX to u and preserving angles between vectors. */
	public static Affine3 asEX(Vec3 u) {
		Basis3 b = GeometryUtils.orthogonalBasis(u);
		return Affine3.unitVectors(u, b.e(), b.f());
	}
	
	/** One possible linear transformation mapping EX to a vector u_ of unit length and preserving angles between vectors. */
	public static Affine3 asEXN(Vec3 u_) {
		Basis3 b = GeometryUtils.orthonormalBasis_(u_);
		return Affine3.unitVectors(u_, b.e(), b.f());
	}

	/** Rotations about z, y, x (in turns), followed by the translation. */
	public static Affine3 isometryRT(double phyZ, double phiY, double phiX, double translateX, double translateY, double translateZ) {
		return Affine3.IDENTITY
				.then(Affine3.rotationAboutZ(phyZ))
				.then(Affine3.rotationAboutY(phiY))
				.then(Affine3.rotationAboutX(phiX))
				.then(Affine3.translation(Vec3.xyz(translateX, translateY, translateZ)))
				;
	}
	
	/** The translation, followed by rotations about z, y, x (in turns). */
	public static Affine3 isometryTR(double translateX, double translateY, double translateZ, double phyZ, double phiY, double phiX) {
		return Affine3.IDENTITY
				.then(Affine3.translation(Vec3.xyz(translateX, translateY, translateZ)))
				.then(Affine3.rotationAboutZ(phyZ))
				.then(Affine3.rotationAboutY(phiY))
				.then(Affine3.rotationAboutX(phiX))
				;
	}
	
	
	/** The composition of the transformations, applied in the given order. */
	public static Affine3 chain(Affine3... transformations) {
		Affine3 res = Affine3.IDENTITY;
		for (Affine3 t : transformations) {
			res = res.then(t);
		}
		return res;
	}
	
}
