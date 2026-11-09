package xyz.marsavic.gfxlab.graphics3d;

import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.utils.Hash;


/**
 * The parameters of the Phong model with ideal reflection and refraction: the colors diffuse, specular (with
 * shininess), reflective, and refractive (with refractiveIndex), used by RayTracerSimple. As a BSDF, for the path
 * tracer, it is the sum of the diffuse, specular, reflective, and refractive BSDFs. The specular term is normalized
 * (see {@link BSDF#specular}), so the specular color is the fraction of light reflected around the mirror direction,
 * and the highlights get smaller and brighter as the shininess grows.
 */
public final class Phong implements BSDF {
	
	private final Color diffuse;
	private final Color specular;
	private final double shininess;
	private final Color reflective;
	private final Color refractive;
	private final double refractiveIndex;
	
	/** The BSDF used for sampling, chosen at construction from the parameters. */
	private final BSDF bsdf;
	
	
	public Phong(Color diffuse, Color specular, double shininess, Color reflective, Color refractive, double refractiveIndex) {
		this.diffuse = diffuse;
		this.specular = specular;
		this.shininess = shininess;
		this.reflective = reflective;
		this.refractive = refractive;
		this.refractiveIndex = refractiveIndex;
		
		// A term is sampled with probability proportional to the luminance of its color. A single nonzero term is
		// sampled directly, and with no nonzero terms the BSDF is absorptive.
		bsdf = BSDF.importanceSum(
				new BSDF[] {
						BSDF.diffuse   (diffuse),
						BSDF.specular  (specular, shininess),
						BSDF.reflective(reflective),
						BSDF.refractive(refractive, refractiveIndex),
				},
				new double[] {
						diffuse   .luminance(),
						specular  .luminance(),
						reflective.luminance(),
						refractive.luminance(),
				}
		);
	}
	
	
	public Color  diffuse        () { return diffuse        ; }
	public Color  specular       () { return specular       ; }
	public double shininess      () { return shininess      ; }
	public Color  reflective     () { return reflective     ; }
	public Color  refractive     () { return refractive     ; }
	public double refractiveIndex() { return refractiveIndex; }
	
	// Copies of these parameters with one parameter changed.
	public Phong diffuse        (Color  diffuse        ) { return new Phong(diffuse, specular, shininess, reflective, refractive, refractiveIndex); }
	public Phong specular       (Color  specular       ) { return new Phong(diffuse, specular, shininess, reflective, refractive, refractiveIndex); }
	public Phong shininess      (double shininess      ) { return new Phong(diffuse, specular, shininess, reflective, refractive, refractiveIndex); }
	public Phong reflective     (Color  reflective     ) { return new Phong(diffuse, specular, shininess, reflective, refractive, refractiveIndex); }
	public Phong refractive     (Color  refractive     ) { return new Phong(diffuse, specular, shininess, reflective, refractive, refractiveIndex); }
	public Phong refractiveIndex(double refractiveIndex) { return new Phong(diffuse, specular, shininess, reflective, refractive, refractiveIndex); }
	
	/** A copy with the specular color equal to the diffuse color, as with metals. */
	public Phong specularCopyDiffuse() { return specular(diffuse); }
	
	
	@Override
	public Result sample(Vec3 n_, Vec3 i, Hash hash) {
		return bsdf.sample(n_, i, hash);
	}
	
	
	/** The linear interpolation of all parameters between a (k = 0) and b (k = 1). */
	public static Phong lerp(Phong a, Phong b, double k) {
		return new Phong(
				Color.lerp(a.diffuse   , b.diffuse   , k),
				Color.lerp(a.specular  , b.specular  , k),
				a.shininess + (b.shininess - a.shininess) * k,
				Color.lerp(a.reflective, b.reflective, k),
				Color.lerp(a.refractive, b.refractive, k),
				a.refractiveIndex + (b.refractiveIndex - a.refractiveIndex) * k
		);
	}


	// --- Utility constants and factory methods ---

	public static final Phong BLACK = new Phong(Color.BLACK, Color.BLACK, 32, Color.BLACK, Color.BLACK, 1.5);

	/** A conspicuous material, for marking places in images while debugging. */
	public static final Phong DEBUG = BLACK.diffuse(Color.DEBUG);

	public static Phong matte (Color  c) { return BLACK.diffuse(c); }
	public static Phong matte (double k) { return matte(Color.gray(k)); }
	public static Phong matte (        ) { return matte(1.0); }

	public static Phong mirror(Color  c) { return BLACK.reflective(c); }
	public static Phong mirror(double k) { return mirror(Color.gray(k)); }
	public static Phong mirror(        ) { return mirror(1.0); }

	public static Phong glass (Color  c) { return BLACK.refractive(c); }
	public static Phong glass (double k) { return glass(Color.gray(k)); }
	public static Phong glass (        ) { return glass(1.0); }

}
