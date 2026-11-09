package xyz.marsavic.gfxlab.graphics3d.solids;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.gfxlab.math.Basis3;
import xyz.marsavic.gfxlab.math.GeometryUtils;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.utils.Numeric;


/** An infinite cylinder. Negative r will make the cylinder inverted (the resulting solid is a complement of a cylinder). */
public class Cylinder implements Solid {

	private final Vec3 p, d;
	private final double r;
	private final F1<Material, Vector> mapMaterial;

	// transient
	private final double rSqr, rInv, dSqrInv;
	private final Vec3 e_, f_;    // An orthonormal basis of the plane normal to the axis, used for uv coordinates.



	private Cylinder(Vec3 p, Vec3 d, double r, F1<Material, Vector> mapMaterial) {
		this.p = p;
		this.d = d;
		this.r = r;
		this.mapMaterial = mapMaterial;

		rSqr = r * r;
		rInv = 1 / r;
		dSqrInv = 1 / d.lengthSquared();
		Basis3 basis = GeometryUtils.orthonormalBasis_(d.normalized_());
		e_ = basis.e();
		f_ = basis.f();
	}

	/** A cylinder with the axis passing through p in the direction d, and with radius r. */
	public static Cylinder pdr(Vec3 p, Vec3 d, double r, F1<Material, Vector> mapMaterial) {
		return new Cylinder(p, d, r, mapMaterial);
	}

	/** A cylinder with the axis passing through p in the direction d, and with radius r. */
	public static Cylinder pdr(Vec3 p, Vec3 d, double r) {
		return new Cylinder(p, d, r, Material.DEFAULT);
	}

	public Cylinder material(F1<Material, Vector> mapMaterial) {
		return new Cylinder(p, d, r, mapMaterial);
	}


	public Vec3 p() {
		return p;
	}


	public Vec3 d() {
		return d;
	}


	public double r() {
		return r;
	}


	/** Rejection of v on the axis direction d. */
	private Vec3 rejectionAxis(Vec3 v) {
		return v.sub(d.mul(v.dot(d) * dSqrInv));
	}


	@Override
	public Hit firstHit(Ray ray, double afterTime) {
		// The same as for the ball, after rejecting all vectors on the axis direction.
		Vec3 eP = rejectionAxis(p.sub(ray.p()));       // Vector from the ray origin to the axis, normal to the axis
		Vec3 dP = rejectionAxis(ray.d());
		double dPSqrInv = 1 / dP.lengthSquared();
		double tM = eP.dot(dP) * dPSqrInv;
		double dtSqr = tM * tM - (eP.lengthSquared() - rSqr) * dPSqrInv;

		if (dtSqr > 0) {
			double dt = Math.sqrt(dtSqr);
			if (tM - dt > afterTime) return new HitCylinder(ray, tM - dt);
			if (tM + dt > afterTime) return new HitCylinder(ray, tM + dt);
		}
		return Hit.AtPositiveInfinity.axisAligned(ray.d(), rInv < 0);
	}


	class HitCylinder extends Hit.RayT {

		protected HitCylinder(Ray ray, double t) {
			super(ray, t);
		}

		@Override
		public Vec3 n() {
			return rejectionAxis(ray().at(t()).sub(p)).mul(rInv);   // Divided by r, not |r|: for an inverted cylinder (r < 0), the normal points to the axis, which is outside the solid.
		}

		@Override
		public Vec3 n_() {
			return n();
		}

		@Override
		public Material material() {
			return mapMaterial.at(uv());
		}

		/**
		 * u is the angle around the axis (in turns), v is the position along the axis (in units of |d|), so that
		 * (u, v, n) is a positive basis. For inverted cylinders, n is flipped, so u is mirrored to keep (u, v, n) positive.
		 */
		@Override
		public Vector uv() {
			Vec3 b = ray().at(t()).sub(p);
			return Vector.xy(
					Numeric.atan2T(b.dot(f_) * Math.signum(rInv), b.dot(e_)),
					b.dot(d) * dSqrInv
			);
		}

	}

}
