package xyz.marsavic.gfxlab.graphics3d;

import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.math.GeometryUtils;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.utils.Hash;
import xyz.marsavic.utils.Numeric;


/**
 * A BSDF given by a sampling procedure, as used in path tracing. For the normal and the direction i towards the
 * viewer (the reversed direction of the traced ray), it randomly chooses a direction out in which the path
 * continues, and a color by which the light coming from that direction is multiplied. The expected value of the
 * product is the light scattered towards the viewer. The cosine factor is included in the color, and solid angles
 * are measured so that the hemisphere has measure 1 (there is no factor pi).
 */
public interface BSDF {
	// TODO: Hemisphere or Sphere? Since it is BSDF not BRDF
	
	/** The sampled direction in which the path continues, and the color by which the light from it is multiplied. */
	record Result(
			Vec3 out,
			Color color
	) {
		public static final Result ABSORBED = new Result(null, Color.BLACK);
		
		public Result mul(Color k) {
			return new Result(out, color.mul(k));
		}
		
		public Result mul(double k) {
			return new Result(out, color.mul(k));
		}
	}
	
	
	/** A random sample for the normalized normal n_ and the direction i towards the viewer, determined by hash. */
	Result sample(Vec3 n_, Vec3 i, Hash hash);
	
	
	
	/** This BSDF with the resulting colors multiplied by color. */
	default BSDF mul(Color color) {
		return (n_, i, hash) -> this.sample(n_, i, hash).mul(color);
	}
	
	
	default BSDF mul(double k) {
		return mul(Color.gray(k));
	}
	
	
	// ===================================================================================================
	// Utility instances and factories.
	
	
	
	/** The blend (1 - k) bsdf0 + k bsdf1: each call samples bsdf1 with probability k, and bsdf0 otherwise. */
	static BSDF blend(BSDF bsdf0, BSDF bsdf1, double k) {
		if (k == 0) return bsdf0;
		if (k == 1) return bsdf1;
		
		return (n_, i, hash) ->
				hash.getDouble() < k ?
						bsdf1.sample(n_, i, hash.add(0xADF296FC86CB9676L)) :
						bsdf0.sample(n_, i, hash.add(0x54CA6DACF51FD045L));
	}
	
	
	/**
	 * Returns the sum of the specified bsdfs. Each call samples one of them, chosen with probability proportional to
	 * its importance, and divides the result by that probability. The importances affect only the noise: the expected
	 * value is the same for any choice of positive importances. The bsdfs with zero importance are omitted.
	 */
	static BSDF importanceSum(BSDF[] bsdfs, double[] importances) {
		if (bsdfs.length != importances.length) {
			throw new IllegalArgumentException();
		}
		
		double sum = 0;
		int m = 0;
		for (double w : importances) {
			if (w > 0) {
				m++;
				sum += w;
			}
		}
		
		if (m == 0) return BSDF.ABSORPTIVE;
		
		BSDF[] bsdfs_ = new BSDF[m];
		double[] weights_ = new double[m];
		m = 0;
		for (int j = 0; j < bsdfs.length; j++) {
			double w = importances[j];
			if (w > 0) {
				bsdfs_[m] = bsdfs[j];
				weights_[m] = importances[j] / sum;
				m++;
			}
		}
		
		// Optimization
		if (m == 1) return bsdfs_[0];
		
		
		return (n_, i, hash) -> {
			double u = hash.getDouble();
			double s = 0;
			int j = 0;
			while (s <= u) {
				s += weights_[j++];
			}
			j--;
			
			return bsdfs_[j].sample(n_, i, hash.add(0)).mul(1.0 / weights_[j]);
		};
	}
	
	
	
	
	BSDF ABSORPTIVE = (n_, i, hash) -> Result.ABSORBED;
	
	BSDF REFLECTIVE = (n_, i, hash) -> new Result(GeometryUtils.reflected(n_, i), Color.WHITE);
	
	BSDF TRANSMISSIVE = transmissive(Color.WHITE);
	
	
	static BSDF transmissive(Color c) {
		return (n_, i, hash) -> new Result(i.inverse(), c);
	}
	
	
	static BSDF diffuse(Color c) {
		return (n_, i, hash) -> new Result(Sampling.directionCosineDistributedN(n_, hash), c);
		
/*
//		Alternative, using uniform sampling
		return (n_, i, hash) -> {
			Vec3 out = Sampling.hemisphereUniform_(n_, hash);
			return new Result(out, c.mul(out.dot(n_) * 2));
		};
*/
	}
	
	
	static BSDF diffuse(double k) {
		return diffuse(Color.gray(k));
	}
	
	
	static BSDF reflective(double k) {
		return reflective(Color.gray(k));
	}
	
	
	static BSDF reflective(Color c) {
		return (n_, i, hash) -> new Result(GeometryUtils.reflectedN(n_, i), c);
	}
	
	
	/**
	 * The specular term of the Phong model, normalized: the BRDF is c (shininess + 2) / 2 cos^shininess(a), where a is the
	 * angle between the outgoing direction and the reflected direction r. For light coming along the normal, the
	 * fraction of reflected light is c, for any shininess; for other directions, a part of the lobe is below the surface
	 * and is lost. The directions are sampled with the density proportional to cos^shininess(a).
	 */
	static BSDF specular(Color c, double shininess) {
		return (n_, i, hash) -> {
			Vec3 r_ = GeometryUtils.reflectedN(n_, i).normalized_();
			double cosA = Math.pow(hash.getDouble(), 1 / (shininess + 1));
			double sinA = Math.sqrt(1 - cosA * cosA);
			double phi = hash.add(1).getDouble();
			Vec3 out = GeometryUtils.orthonormalBasis_(r_).at(sinA * Numeric.cosT(phi), sinA * Numeric.sinT(phi), cosA);
			double cosN = out.dot(n_);
			return cosN > 0 ?
					new Result(out, c.mul((shininess + 2) / (shininess + 1) * cosN)) :
					Result.ABSORBED;
		};
	}
	
	
	// An ad-hoc attempt at designing a glossy BSDF.
	// s = how diffuse it is. 0 is perfectly specular, and it gets more diffuse as s approaches 1. 1 does not mean perfectly diffuse.
	// YES: 0 = perfectly specular
	// NO : 1 = perfectly diffuse.
	// YES: Reflected direction in expectation.
	static BSDF glossy(Color c, double s) {
		return (n_, i, hash) -> {
			Vec3 r = GeometryUtils.reflected(n_, i);
			Vec3 d = Sampling.directionCosineDistributedN(n_, hash);
			// TODO d is not normalized!
			
			Vec3 b = r.div(r.dot(n_)).sub(n_);
			d = d.add(b.mul(d.dot(n_)));
			
			Vec3 o = Vec3.lerp(r.normalized_(), d, s);
			return new Result(o, c);
		};
	}

/*
	// YES: 0 = perfectly specular
	// YES: 1 = perfectly diffuse.
	// NO : Reflected direction in expectation.
	static BSDF glossy(Color c, double s) {
		return (n_, i, hash) -> {
			Vec3 d = Utils.sampleHemisphereCosineDistributed(n_, hash);
			Vec3 r = Utils.reflected(n_, i.normalized_());
			Vec3 o = Vec3.lerp(r, d, s);
			return new Result(o, c);
		};
	}
*/
	
/*
	// YES: 0 = perfectly specular (experimentally shown)
	// NO : 1 = perfectly diffuse.
	// ?  : Reflected direction in expectation. (Probably not, but can't confirm visually)
	// Subjective: Better looking than previous one.
	static BSDF glossy(Color c, double s) {
		return (n_, i, hash) -> {
			Vec3 r = i;
			while (true) {
				Vec3 u = Utils.sampleHemisphereCosineDistributed(n_, hash);
				Vec3 n = Vec3.lerp(n_, u, s);
				r = Utils.reflected(n, r);
				if (r.dot(n_) > 0) {
					return new Result(r, c);
				}
			}
		};
	}
*/
	
	
	static BSDF glossy(double k, double s) {
		return glossy(Color.gray(k), s);
	}
	
	
	static BSDF glossyRefractive(Color c, double refractiveIndex, double s) {
		return (n_, i, hash) -> {
			Vec3 r = GeometryUtils.refractedNN(n_, i.normalized_(), refractiveIndex); // TODO Maybe use GeometryUtils.refractedN, without normalizing, to avoid one sqrt.
			Vec3 d = Sampling.directionCosineDistributedN(n_, hash).mul(Numeric.sign(n_.dot(r)));
			// TODO d is not normalized!
			
			Vec3 b = r.div(r.dot(n_)).sub(n_);
			d = d.add(b.mul(d.dot(n_)));
			
			Vec3 o = Vec3.lerp(r, d, s);
			return new Result(o, c);
		};
	}
	
	
	static BSDF glossyRefractive(double k, double refractiveIndex, double s) {
		return glossyRefractive(Color.gray(k), refractiveIndex, s);
	}
	
	
	static BSDF refractive(double refractiveIndex) {
		return refractive(1.0, refractiveIndex);
	}
	
	
	static BSDF refractive(double k, double refractiveIndex) {
		return refractive(Color.gray(k), refractiveIndex);
	}
	
	
	static BSDF refractive(Color c, double refractiveIndex) {
		// TODO: Fresnel coefficients for the ratio between the refracted and the reflected part.

		return (n_, i, hash) -> new Result(GeometryUtils.refractedNN(n_, i.normalized_(), refractiveIndex), c);  // TODO Maybe use GeometryUtils.refractedN, without normalizing, to avoid one sqrt.
	}
	
	
}
