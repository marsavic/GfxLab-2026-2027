package xyz.marsavic.gfxlab.graphics3d.solids;


import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.graphics3d.*;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.utils.Hash;
import xyz.marsavic.utils.Numeric;

import java.util.Iterator;


public class Box implements Solid, Iterable<Vec3> {
	
	public static final BoxedObjectFactory.PQ<Box> $ = Box::new;
	public static Box UNIT = Box.$.pq(Vec3.ZERO, Vec3.EXYZ);
	
	
	private final Vec3 p, q;
	private final F1<Material, Vector> mapMaterial;
	
	
	
	private Box(Vec3 p, Vec3 q, F1<Material, Vector> mapMaterial) {
		this.p = p;
		this.q = q;
		this.mapMaterial = mapMaterial;
	}
	
	private Box(Vec3 p, Vec3 q) {
		this(p, q, Material.DEFAULT);
	}
	
	public Box material(F1<Material, Vector> mapMaterial) {
		return new Box(p, q, mapMaterial);
	}
	
	
	public Vec3 p() {
		return p;
	}
	
	public Vec3 q() {
		return q;
	}
	
	public Vec3 d() {
		return q.sub(p);
	}
	
	public Vec3 c() {
		return p.add(q).div(2);
	}
	
	public Vec3 r() {
		return d().div(2);
	}
	
	
	@Override
	public boolean contains(Vec3 o) {
		return o.sub(p).sign().sub(q.sub(o).sign()).allZero();
	}
	
	
	
	
	@Override
	public Hit firstHit(Ray ray, double afterTime) {
		Vec3 tP = p().sub(ray.p()).div(ray.d());
		Vec3 tQ = q().sub(ray.p()).div(ray.d());
		
		Vec3 t0 = Vec3.min(tP, tQ);
		Vec3 t1 = Vec3.max(tP, tQ);
		
		int iMax0 = t0.maxIndex();
		int iMin1 = t1.minIndex();
		
		double max0 = t0.get(iMax0);
		double min1 = t1.get(iMin1);
		
		if (max0 < min1) {
			if (max0 > afterTime) return new HitBox(ray, max0, Vec3.E[iMax0].mul(-Numeric.sign(ray.d().get(iMax0))));
			if (min1 > afterTime) return new HitBox(ray, min1, Vec3.E[iMin1].mul( Numeric.sign(ray.d().get(iMin1))));
		}
		return Hit.AtPositiveInfinity.axisAlignedGoingIn(ray.d());
	}
	
	
	/** A random point of the box, uniformly distributed, determined by the hash. */
	public Vec3 sample(Hash hash) {
		return p.add(d().mul(Vec3.random(hash)));
	}
	
	
	@Override
	public Iterator<Vec3> iterator() {
		return new GridIterator(this);
	}
	
	
	/**
	 * Iterates through points inside the specified box, starting from the corner p.
	 */
	public static class GridIterator implements Iterator<Vec3> {
		private final Vec3 p, step;
		
		private final int dX, dY, dZ;

		private int iNextX, iNextY, iNextZ;
		private boolean hasNext;

		
		
		public GridIterator(Box b, Vec3 step) {
			if (!step.sign().equals(b.d().sign())) {
				throw new IllegalArgumentException("b.d and step must have the same direction in all coordinates.");
			}
			
			this.step = step;
			p = b.p();
			Vec3 d = b.d().div(step).abs().ceil();
			dX = (int) d.x();
			dY = (int) d.y();
			dZ = (int) d.z();
			hasNext = d.product() > 0;
		}
		
		
		public GridIterator(Vec3 d, Vec3 step) {
			this(Box.$.pd(Vec3.ZERO, d), step);
		}
		
		
		public GridIterator(Vec3 d) {
			this(d, d.sign());
		}
		
		
		public GridIterator(Box b) {
			this(b, b.d().sign());
		}
		
		
		@Override
		public boolean hasNext() {
			return hasNext;
		}
		
		
		@Override
		public Vec3 next() {
			Vec3 pos = Vec3.xyz(iNextX, iNextY, iNextZ).mul(step).add(p);
		
			if (++iNextX < dX) return pos;
			iNextX = 0;
			if (++iNextY < dY) return pos;
			iNextY = 0;
			hasNext = (++iNextZ < dZ);
			return pos;
		}
	}
	
	
	final class HitBox extends Hit.RayT {
		private final Vec3 n_;
		
		
		HitBox(Ray ray, double t, Vec3 n_) {
			super(ray, t);
			this.n_ = n_;
		}
		
		@Override public Vec3 n_() { return n_; }
		@Override public Vec3 n() { return n_; }
		
		@Override
		public Material material() {
			return mapMaterial.at(uv());
		}
		
		/**
		 * Each face gets its own coordinate system [0, 1]^2 (relative positions along two axes). On the side faces v goes
		 * along z (up), on the top and bottom faces along y. The direction of u is chosen so that (u, v, n) is a positive basis.
		 */
		@Override
		public Vector uv() {
			Vec3 c = ray().at(t()).sub(p()).div(d());       // Relative position inside the box, in [0, 1]^3
			int i = n_.abs().maxIndex();                     // The axis normal to the face
			int iV = (i == 2) ? 1 : 2;
			int iU = 3 - i - iV;
			double u = c.get(iU);
			boolean positive = Vec3.E[iU].cross(Vec3.E[iV]).dot(n_) > 0;
			return Vector.xy(positive ? u : 1 - u, c.get(iV));
		}
		
		@Override
		public Hit inverted() {
			return new HitBox(ray(), t(), n_.inverse());
		}
		
	}
	
}
