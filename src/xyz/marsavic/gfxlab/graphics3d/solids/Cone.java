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


/**
 * An infinite double cone with the apex p, the axis direction d, and the slope k (the tangent of the half-angle at the apex):
 * the points x for which the distance from the axis is at most k times the distance along the axis from the apex.
 */
public class Cone implements Solid {

	private final Vec3 p, d;
	private final double k;
	private final F1<Material, Vector> mapMaterial;

	// transient
	private final Vec3 d_, e_, f_;   // An orthonormal basis with d_ along the axis, used for uv coordinates.
	private final double kk;         // 1 + k^2



	private Cone(Vec3 p, Vec3 d, double k, F1<Material, Vector> mapMaterial) {
		this.p = p;
		this.d = d;
		this.k = k;
		this.mapMaterial = mapMaterial;

		Basis3 basis = GeometryUtils.orthonormalBasis_(d.normalized_());
		d_ = basis.g();
		e_ = basis.e();
		f_ = basis.f();
		kk = 1 + k * k;
	}

	/** A cone with the apex p, the axis direction d, and the slope k (the tangent of the half-angle at the apex). */
	public static Cone pdk(Vec3 p, Vec3 d, double k, F1<Material, Vector> mapMaterial) {
		return new Cone(p, d, k, mapMaterial);
	}

	/** A cone with the apex p, the axis direction d, and the slope k (the tangent of the half-angle at the apex). */
	public static Cone pdk(Vec3 p, Vec3 d, double k) {
		return new Cone(p, d, k, Material.DEFAULT);
	}

	public Cone material(F1<Material, Vector> mapMaterial) {
		return new Cone(p, d, k, mapMaterial);
	}


	public Vec3 p() {
		return p;
	}

	public Vec3 d() {
		return d;
	}

	public double k() {
		return k;
	}


	/**
	 * The point x = p + y is inside iff f(y) = |y|^2 - (1 + k^2) (y·d_)^2 <= 0 (the rejection of y on the axis is at most
	 * k times the projection). For the ray, f(e + t w) = a t^2 + 2 b t + c, where e is the ray origin relative to p.
	 */
	@Override
	public Hit firstHit(Ray ray, double afterTime) {
		Vec3 e = ray.p().sub(p);
		Vec3 w = ray.d();
		double eA = e.dot(d_);
		double wA = w.dot(d_);

		double a = w.lengthSquared() - kk * wA * wA;
		double b = e.dot(w) - kk * eA * wA;
		double c = e.lengthSquared() - kk * eA * eA;

		double disc = b * b - a * c;
		if (disc > 0) {
			// For a > 0, the ray is inside between the roots; for a < 0, it is inside before the first and after the second root.
			double s = Math.sqrt(disc) * Math.signum(a);
			double t0 = (-b - s) / a;
			double t1 = (-b + s) / a;
			if (t0 > afterTime) return new HitCone(ray, t0);
			if (t1 > afterTime) return new HitCone(ray, t1);
		}
		return Hit.AtPositiveInfinity.axisAligned(ray.d(), a < 0);
	}


	class HitCone extends Hit.RayT {

		protected HitCone(Ray ray, double t) {
			super(ray, t);
		}

		/** Half of the gradient of f. */
		@Override
		public Vec3 n() {
			Vec3 y = ray().at(t()).sub(p);
			return y.sub(d_.mul(kk * y.dot(d_)));
		}

		@Override
		public Material material() {
			return mapMaterial.at(uv());
		}

		/**
		 * u is the angle around the axis (in turns), v is the position along the axis (in units of |d|), so that
		 * (u, v, n) is a positive basis on both nappes.
		 */
		@Override
		public Vector uv() {
			Vec3 y = ray().at(t()).sub(p);
			return Vector.xy(
					Numeric.atan2T(y.dot(f_), y.dot(e_)),
					y.dot(d) / d.lengthSquared()
			);
		}

	}

}
