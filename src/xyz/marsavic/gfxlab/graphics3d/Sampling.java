package xyz.marsavic.gfxlab.graphics3d;

import xyz.marsavic.gfxlab.math.GeometryUtils;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.utils.Hash;
import xyz.marsavic.utils.Numeric;

import java.util.random.RandomGenerator;

/**
 * Random directions and points with various distributions, determined by a hash. In the names, the suffix N means
 * that the given normal must be normalized, and the suffix _ that the result is normalized.
 * TODO: Test
 */
public class Sampling {
	
	
	private Sampling() {}		
	
	
	/** A unit direction uniformly distributed over the hemisphere around n_. */
	public static Vec3 hemisphereUniform_          (Vec3 n_, Hash hash ) { return HemisphereUniform.phiX_               (n_, hash); }	
	
	/** A direction (not normalized) distributed over the hemisphere around n_ with the density proportional to the cosine of the angle to n_. */
	public static Vec3 directionCosineDistributedN (Vec3 n_, Hash hash ) { return DirectionCosineDistributed.movedBallN(n_, hash); }
	/** A unit direction distributed over the hemisphere around n_ with the density proportional to the cosine of the angle to n_. */
	public static Vec3 directionCosineDistributedN_(Vec3 n_, Hash hash ) { return hemisphereCosineDistributedN_(n_, hash); }

	/** A vector of length |n| distributed over the hemisphere around n with the density proportional to the cosine of the angle to n. */
	public static Vec3 hemisphereCosineDistributed  (Vec3 n , Hash hash ) { return HemisphereCosineDistributed.phiXSqr  (n , hash); }	
	/** A unit vector distributed over the hemisphere around n_ with the density proportional to the cosine of the angle to n_. */
	public static Vec3 hemisphereCosineDistributedN_(Vec3 n_, Hash hash ) { return HemisphereCosineDistributed.phiXSqrN_(n_, hash); }
	
	/** A unit vector uniformly distributed over the sphere. */
	public static Vec3 sphereUniform_              (Hash hash) { return SphereUniform.sampleSphereUniform_(hash); }
	
	/** A point uniformly distributed in the unit ball. */
	public static Vec3 ballUniform                 (Hash hash) { return BallUniform.sampleBallUniform(hash); }
	

	
	// Most of the things are super untested
	
	
	private static class HemisphereUniform {
	
		private static Vec3 phiX_(Vec3 n, Hash hash) {
			double phi = hash.getDouble();
			double x   = hash.add(0).getDouble();
			
			double r = Math.sqrt(1 - x*x);
			double y = Numeric.cosT(phi) * r;
			double z = Numeric.sinT(phi) * r;			
			
			return (x * n.x() + y * n.y() + z * n.z() < 0) ?
					Vec3.xyz(-x, -y, -z) :
					Vec3.xyz(x, y, z);
		}
		

		private static Vec3 rejection(Vec3 n, Hash hash) {
			RandomGenerator rng = hash.rng();
			double x, y, z, lVSqr;
			
			do {
				x = rng.nextDouble() - 0.5;
				y = rng.nextDouble() - 0.5;
				z = rng.nextDouble() - 0.5;
				lVSqr = x * x + y * y + z * z;
			} while (lVSqr > 0.25);
			
			return (x * n.x() + y * n.y() + z * n.z() < 0) ?
					Vec3.xyz(-x, -y, -z) :
					Vec3.xyz(x, y, z);
		}
		
	
		private static Vec3 rejection_(Vec3 n, Hash hash) {
			RandomGenerator rng = hash.rng();
			double x, y, z, lVSqr;
			
			do {
				x = rng.nextDouble() - 0.5;
				y = rng.nextDouble() - 0.5;
				z = rng.nextDouble() - 0.5;
				lVSqr = x * x + y * y + z * z;
			} while (lVSqr > 0.25);
			
			double c = 1 / Math.sqrt(lVSqr);
			return (x * n.x() + y * n.y() + z * n.z() < 0) ?
					Vec3.xyz(-x * c, -y * c, -z * c) :
					Vec3.xyz(x * c, y * c, z * c);
		}
		
	}

	public static class DirectionCosineDistributed {

		private static Vec3 movedBallN(Vec3 n_, Hash hash) {
			// Sample the sphere with radius 1, add n_

			double x = 2 * hash.getDouble() - 1;
			double r = Math.sqrt(1 - x * x);
			double phi = hash.add(0).getDouble();
			double y = Numeric.cosT(phi);
			double z = Math.sqrt(1 - y * y) * Math.signum(0.5 - phi);  // same as z = Numeric.sinT(phi)
			
			return Vec3.xyz(x, y * r, z * r).add(n_);
		}

		
		private static Vec3 rejectionMovedSphere(Vec3 n, Hash hash) {
			// Sample the sphere with radius 1, add n_
			RandomGenerator rng = hash.rng();
			double x, y, z, lVSqr;
			
			do {
				x = rng.nextDouble() - 0.5;
				y = rng.nextDouble() - 0.5;
				z = rng.nextDouble() - 0.5;
				lVSqr = x * x + y * y + z * z;
			} while (lVSqr > 0.25);
			
			double c = Math.sqrt(n.lengthSquared() / lVSqr);
			return Vec3.xyz(x * c, y * c, z * c).add(n);
		}
		
		
		private static Vec3 rejectionMovedSphereN(Vec3 n_, Hash hash) {
			// Sample the sphere with radius 1, add n_
			RandomGenerator rng = hash.rng();
			
			double x, y, z, lVSqr;
			
			do {
				x = rng.nextDouble() - 0.5;
				y = rng.nextDouble() - 0.5;
				z = rng.nextDouble() - 0.5;
				lVSqr = x * x + y * y + z * z;
			} while (lVSqr > 0.25);
			
			double c = 1 / Math.sqrt(lVSqr);
			return Vec3.xyz(x * c, y * c, z * c).add(n_);
		}
		
	}
	
	
	public static class HemisphereCosineDistributed {
		
		private static Vec3 phiXSqr(Vec3 n, Hash hash) {
			double phi = hash.getDouble();
			double xSqr = hash.add(0).getDouble();
			double x = Math.sqrt(xSqr);
			
			double r = Math.sqrt(1 - xSqr);
			double y = Numeric.cosT(phi) * r;
			double z = Numeric.sinT(phi) * r;
			
			return GeometryUtils.orthogonalBasis(n).at(y, z, x);
		}
		
		
		private static Vec3 phiXSqrN_(Vec3 n_, Hash hash) {
			double phi = hash.getDouble();
			double xSqr = hash.add(0).getDouble();
			double x = Math.sqrt(xSqr);
			
			double r = Math.sqrt(1 - xSqr);
			double y = Numeric.cosT(phi) * r;
			double z = Numeric.sinT(phi) * r;
			
			return GeometryUtils.orthonormalBasis_(n_).at(y, z, x);
		}
		
		
		public static Vec3 rejectionFromDiskN(Vec3 n_, Hash hash) {
			RandomGenerator rng = hash.rng();
			
			// Sample the unit disk uniformly (by rejection), and raise the point to the unit hemisphere.
			double x, y, rSqr;
			
			do {
				x = 2 * rng.nextDouble() - 1;
				y = 2 * rng.nextDouble() - 1;
				rSqr = x * x + y * y;
			} while (rSqr > 1);
			
			double z = Math.sqrt(1 - rSqr);
			
			return GeometryUtils.orthonormalBasis_(n_).at(x, y, z);
		}
		
	}

	
	private static class SphereUniform {
		
		// TODO (phi, x)
		
		private static Vec3 sampleSphereUniform_(Hash hash) {
			RandomGenerator rng = hash.rng();			
			double x, y, z, lVSqr;			
			do {
				x = rng.nextDouble() - 0.5;
				y = rng.nextDouble() - 0.5;
				z = rng.nextDouble() - 0.5;
				lVSqr = x * x + y * y + z * z;
			} while (lVSqr > 0.25);
			
			double c = 1 / Math.sqrt(lVSqr);
			return Vec3.xyz(x * c, y * c, z * c);
		}
		
	}


	private static class BallUniform {

		// TODO (phi, x, r)
		
		private static Vec3 sampleBallUniform(Hash hash) {
			RandomGenerator rng = hash.rng();
			double x, y, z;
			do {
				x = rng.nextDouble() - 0.5;
				y = rng.nextDouble() - 0.5;
				z = rng.nextDouble() - 0.5;
			} while (x*x + y*y + z*z > 0.25);
			return Vec3.xyz(2*x, 2*y, 2*z);
		}
		
	}
	
}
