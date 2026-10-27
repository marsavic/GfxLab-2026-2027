package xyz.marsavic.gfxlab.math;


/**
 * Three vectors (e, f, g), used as a basis: the vector with coordinates (x, y, z) in this basis is x e + y f + z g.
 * The vectors need not be orthogonal or normalized. For an orthonormal basis, see {@link GeometryUtils#orthonormalBasis_}.
 */
public record Basis3(Vec3 e, Vec3 f, Vec3 g) {
	
	/** The vector with coordinates (x, y, z) in this basis, x e + y f + z g. */
	public Vec3 at(double x, double y, double z) {
		return Vec3.xyz(
				x * e.x() + y * f.x() + z * g.x(),
				x * e.y() + y * f.y() + z * g.y(),
				x * e.z() + y * f.z() + z * g.z()
		);
	}
	
	
	/** The vector with coordinates a in this basis. */
	public Vec3 at(Vec3 a) {
		return at(a.x(), a.y(), a.z());
	}
	
	
	/** The linear transformation mapping EX, EY, EZ to e, f, g, so that it maps coordinates in this basis to vectors. */
	public Affine3 asAffine3() {
		return Affine3.unitVectors(e, f, g);
	}
	
}
