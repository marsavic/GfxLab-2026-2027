package xyz.marsavic.gfxlab.graphics3d;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.math.Vec3;


/** A hit: a point at which a ray crosses the surface of a solid. */
public interface Hit {
	
	/** The time of the hit. */
	double t();
	
	/** The normal at the hit point, pointing out of the solid. Not necessarily normalized. */
	Vec3 n();
	
	/** The surface material at the hit point. */
	Material material();
	
	/** The coordinates of the hit point in the coordinate system of the surface. */
	Vector uv();
	
	/** The normalized normal at the hit point. */
	default Vec3 n_() {
		return n().normalized_();
	}
	
	
	/** The same hit, with the normal n. */
	default Hit withN(Vec3 n) {
		return new Hit() {
			@Override public double   t       () { return Hit.this.t(); }
			@Override public Vec3     n       () { return n; }
			@Override public Material material() { return Hit.this.material(); }
			@Override public Vector   uv      () { return Hit.this.uv(); }
		};
	}
	
	/** The same hit, with the inverted normal (as a hit into the complement of the solid). */
	default Hit inverted() {
		return new Hit() {
			@Override public double   t       () { return Hit.this.t (); }
			@Override public Vec3     n       () { return Hit.this.n ().inverse(); }
			@Override public Vec3     n_      () { return Hit.this.n_().inverse(); }
			@Override public Vector   uv      () { return Hit.this.uv(); }
			@Override public Material material() { return Hit.this.material(); }
		};
	}
	
	
	// =====================================================================================================
	
	
	/** A base for hits that remember the ray and the time of the hit. */
	abstract class RayT implements Hit {
		private final Ray ray;
		private final double t;
		
		protected RayT(Ray ray, double t) {
			this.ray = ray;
			this.t = t;
		}
		
		public Ray ray() {
			return ray;
		}
		
		@Override
		public double t() {
			return t;
		}
	}
	
	
	
	/**
	 * The hit at positive infinity, returned when a ray does not hit the surface (anymore). Its normal is an axis
	 * direction chosen so that the hits along the ray alternate between entering and exiting. The six possible hits
	 * are preallocated, to avoid object creation.
	 */
	class AtPositiveInfinity implements Hit {
		private final Vec3 n;
		
		private AtPositiveInfinity(Vec3 n) {
			this.n = n;
		}
		
		@Override public double   t       () { return Double.POSITIVE_INFINITY; }
		@Override public Vec3     n       () { return n; }
		@Override public Vec3     n_      () { return n; }
		@Override public Vector   uv      () { return Vector.ZERO; }
		@Override public Material material() { return Material.BLACK; }

		
		private static final AtPositiveInfinity hitAtPositiveInfinityXM = new AtPositiveInfinity(Vec3.xyz(-1, 0, 0));
		private static final AtPositiveInfinity hitAtPositiveInfinityXP = new AtPositiveInfinity(Vec3.xyz( 1, 0, 0));
		private static final AtPositiveInfinity hitAtPositiveInfinityYM = new AtPositiveInfinity(Vec3.xyz( 0,-1, 0));
		private static final AtPositiveInfinity hitAtPositiveInfinityYP = new AtPositiveInfinity(Vec3.xyz( 0, 1, 0));
		private static final AtPositiveInfinity hitAtPositiveInfinityZM = new AtPositiveInfinity(Vec3.xyz( 0, 0,-1));
		private static final AtPositiveInfinity hitAtPositiveInfinityZP = new AtPositiveInfinity(Vec3.xyz( 0, 0, 1));
		
		/** The hit at infinity exiting the solid, for a ray with direction d (the normal has a positive dot product with d). */
		public static AtPositiveInfinity axisAlignedGoingOut(Vec3 d) {
			if (d.x() < 0) return hitAtPositiveInfinityXM;
			if (d.x() > 0) return hitAtPositiveInfinityXP;
			if (d.y() < 0) return hitAtPositiveInfinityYM;
			if (d.y() > 0) return hitAtPositiveInfinityYP;
			if (d.z() < 0) return hitAtPositiveInfinityZM;
			               return hitAtPositiveInfinityZP;
		}
		
		/** The hit at infinity entering the solid, for a ray with direction d (the normal has a negative dot product with d). */
		public static AtPositiveInfinity axisAlignedGoingIn(Vec3 d) {
			if (d.x() < 0) return hitAtPositiveInfinityXP;
			if (d.x() > 0) return hitAtPositiveInfinityXM;
			if (d.y() < 0) return hitAtPositiveInfinityYP;
			if (d.y() > 0) return hitAtPositiveInfinityYM;
			if (d.z() < 0) return hitAtPositiveInfinityZP;
			               return hitAtPositiveInfinityZM;
		}
		
		/** The hit at infinity for a ray with direction d, entering the solid or (if fromInside) exiting it. */
		public static AtPositiveInfinity axisAligned(Vec3 d, boolean fromInside) {
			return fromInside ? axisAlignedGoingOut(d) : axisAlignedGoingIn(d);
		}
		
	}
	
}
