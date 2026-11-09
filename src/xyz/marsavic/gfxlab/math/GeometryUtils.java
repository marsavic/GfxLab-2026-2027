package xyz.marsavic.gfxlab.math;


/** Geometric constructions on vectors: orthogonal vectors and bases, reflection, and refraction. */
public final class GeometryUtils {
	
	private GeometryUtils() {}
	
	
	/**
	 * An orthonormal basis (e_, f_, v_) for a unit vector v_, with e_ × f_ = v_. The vector v_ must be normalized,
	 * otherwise e_ and f_ are neither normalized nor orthogonal.
	 * Branchless method from T. Duff et al., "Building an Orthonormal Basis, Revisited", JCGT 6(1), 2017.
	 */
	public static Basis3 orthonormalBasis_(Vec3 v_) {
		double sign = Math.copySign(1.0, v_.z());
		double a = -1.0 / (sign + v_.z());
		double b = v_.x() * v_.y() * a;
		return new Basis3(
				Vec3.xyz(1.0 + sign * v_.x() * v_.x() * a, sign * b, -sign * v_.x()),
				Vec3.xyz(b, sign + v_.y() * v_.y() * a, -v_.y()),
				v_
		);
	}
	
	
	/** An orthogonal basis (e, f, v) for a nonzero vector v, with e and f of the same length as v, and e × f = |v| v. */
	public static Basis3 orthogonalBasis(Vec3 v) {
		double l = v.length();
		Basis3 b = orthonormalBasis_(v.div(l));
		return new Basis3(b.e().mul(l), b.f().mul(l), v);
	}
	
	
	/** An orthogonal vector to v. */
	public static Vec3 normal(Vec3 v) {
		if (Math.abs(v.x()) > Math.abs(v.z())) {         // Then x != 0, so the result is not zero.
			return Vec3.xyz(-v.y(), v.x(), 0);
		} else {                                         // Then z != 0 or v is parallel to y, so the result is not zero.
			return Vec3.xyz(0, -v.z(), v.y());
		}
	}

	/** A normalized orthogonal vector to v. */
	public static Vec3 normal_(Vec3 v) {
		if (Math.abs(v.x()) > Math.abs(v.z())) {
			double lInv = 1 / Math.sqrt(v.x() * v.x() + v.y() * v.y());
			return Vec3.xyz(-v.y() * lInv, v.x() * lInv, 0);
		} else {
			double lInv = 1 / Math.sqrt(v.y() * v.y() + v.z() * v.z());
			return Vec3.xyz(0, -v.z() * lInv, v.y() * lInv);
		}
	}

	/** The vector d reflected over the line with direction n (n need not be normalized). */
	public static Vec3 reflected(Vec3 n, Vec3 d) {
		throw new UnsupportedOperationException();
	}
	
	
	/** The vector d reflected over the line with direction n_ (n_ must be normalized). */
	public static Vec3 reflectedN(Vec3 n_, Vec3 d) {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * The direction of the refracted ray, for the normalized normal n_ and the normalized direction i_ towards the
	 * incoming ray. When n_ and i_ are on the same side of the surface, the light passes into the medium with the
	 * relative refractive index refractiveIndex (otherwise it passes out of it). In the case of total internal
	 * reflection, the reflected direction is returned. The result is not normalized.
	 */
	public static Vec3 refractedNN(Vec3 n_, Vec3 i_, double refractiveIndex) {
		throw new UnsupportedOperationException();
	}
	
}
