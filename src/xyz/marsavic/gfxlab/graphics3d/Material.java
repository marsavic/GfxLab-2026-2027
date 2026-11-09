package xyz.marsavic.gfxlab.graphics3d;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;


/**
 * The material of a surface point: the BSDF, which describes how the surface scatters light, and the emittance,
 * the light emitted by the surface. A material is also a constant material map (it gives itself for every uv).
 */
public class Material implements F1<Material, Vector> {
	private final BSDF bsdf;
	private final Color emittance;
	
	// transient
	private final Phong phong;	
	
	
	
	public Material(BSDF bsdf, Color emittance) {
		this.bsdf = bsdf;
		this.emittance = emittance;
		phong = bsdf instanceof Phong p ? p : Phong.DEBUG;
	}
	
	/** A material that emits no light. */
	public Material(BSDF bsdf) {
		this(bsdf, Color.BLACK);
	}
	
	
	public BSDF bsdf() {
		return bsdf;
	}
	
	public Color emittance() {
		return emittance;
	}
	
	
	/** The Phong parameters of this material, for RayTracerSimple; Phong.DEBUG if the BSDF is not given by them. */
	public Phong phong() {
		return phong;
	}


	@Override
	public Material at(Vector uv) {
		return this;
	}


	/**
	 * The linear interpolation between the materials a (k = 0) and b (k = 1). The Phong parameters are interpolated
	 * if both BSDFs are given by them, and the BSDFs are blended otherwise.
	 */
	public static Material lerp(Material a, Material b, double k) {
		return new Material(
				a.bsdf instanceof Phong pa && b.bsdf instanceof Phong pb ?
						Phong.lerp(pa, pb, k) :
						BSDF.blend(a.bsdf, b.bsdf, k),
				Color.lerp(a.emittance, b.emittance, k)
		);
	}


	// --- Utility constants and factory methods ---

	public static final Material BLACK = new Material(Phong.BLACK);

	public static Material matte (Color  c) { return new Material(Phong.matte(c)); }
	public static Material matte (double k) { return matte(Color.gray(k)); }
	public static Material matte (        ) { return matte(1.0); }
	public static final Material MATTE = matte();

	public static Material mirror(Color  c) { return new Material(Phong.mirror(c)); }
	public static Material mirror(double k) { return mirror(Color.gray(k)); }
	public static Material mirror(        ) { return mirror(1.0); }
	public static final Material MIRROR = mirror();

	public static Material glass (Color  c) { return new Material(Phong.glass(c)); }
	public static Material glass (double k) { return glass(Color.gray(k)); }
	public static Material glass (        ) { return glass(1.0); }
	public static final Material GLASS = glass();

	public static Material light (Color  c) { return new Material(Phong.BLACK, c); }
	public static Material light (double k) { return light(Color.gray(k)); }
	public static Material light (        ) { return light(1.0); }
	public static final Material LIGHT = light();


	public static final Material DEFAULT = MATTE;

}
