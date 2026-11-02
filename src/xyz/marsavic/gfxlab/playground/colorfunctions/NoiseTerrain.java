package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.gfxlab.entry_points.*;
import xyz.marsavic.gfxlab.math.*;
import xyz.marsavic.functions.FD_O;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.*;
import xyz.marsavic.gfxlab.aggregation.AggregatorOneAhead;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Multiply;
import xyz.marsavic.random.fixed.continuous.PerlinNoise;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Generating;
import xyz.marsavic.utils.Hash;

import static xyz.marsavic.reactions.elements.Elements.*;
import static xyz.marsavic.utils.Numeric.*;

public final class NoiseTerrain implements ColorFunction3 {
	private final int mode;
	private final double c;
	private final int crk;
	private final double z;	
	private final double zoom;
	private final double nLevels;
	private final double b;
	
	
	private static double noise(PerlinNoise pn, Vec3 p) {
		return pn.getValue(p.x(), p.y(), p.z());
	}
	
	private static double noise(PerlinNoise pn, Vector p, double z) {
		return pn.getValue(p.x(), p.y(), z);
	}
	
	private static double noise(PerlinNoise pn, double x, double y, double z) {
		return pn.getValue(x, y, z);
	}
	
	
	private final PerlinNoise[] pn;
	
	private static double eps = 1e-6;
	
	private static Vec3 gradient(FD_O<Vec3> f, Vec3 p) {
		double v = f.at(p);
		return Vec3.xyz(
				f.at(p.add(Vec3.EX)) - v,
				f.at(p.add(Vec3.EY)) - v,
				f.at(p.add(Vec3.EZ)) - v
		).div(eps);		
	}
	
	
	private static final double kStep = PHI;
	
	private double terrain(double x, double y, double z) {
		double k = 1.0;
		double v = 0.0;
		for (int level = 0; level < nLevels; level++) {
			v += noise(pn[level], x * k, y * k, z) / k;
			k *= kStep;
		}
		return v;
	}
	
	private double terrain(Vec3 p) {
		return terrain(p.x(), p.y(), p.z());
	}
	
	public NoiseTerrain(int mode, double c, int crk, double z, double zoom, int nLevels, double b, Hash hash) {
		this.mode = mode;
		this.c = c;
		this.crk = crk;
		this.z = z;
		this.zoom = zoom;
		this.nLevels = nLevels;
		this.b = b;
		pn = Generating.array(nLevels, PerlinNoise.class, i -> new PerlinNoise(hash.add(i).getLong()));
	}
	
	
	@Override
	public Color at(double t, Vector p2) {
		Vec3 p = Vec3.zp(t, p2.mul(zoom));
		
		Vec3 g = gradient(this::terrain, p);
		double gang = g.p01().angle();
		double glen = 1 - 1 / (1 + 8e-6 * g.length());
		double tr = mpToZo(Math.tanh(terrain(p) * b));
		double cr = mpToZo(sinT(crk*tr)) * mpToZo(sinT(crk * gang));
		
		return switch (mode) {
			case 0 -> Color.oklch(0.6*cr, 0.10*cr, gang);
			case 1 -> Color.hsb(gang, 1.0,  cr);
			case 2 -> Color.gray(tr);
			case 3 -> Color.hsb(gang, glen, 1.0);
			case 4 -> Color.gray(glen);
			default -> Color.BLACK;
		};
	}
	
		
	// ================================================================================================================
	
	
	static void main() { App.run(); }
	
	
	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorOneAhead::new),
								e(NoiseTerrain.class,
										e(0),
										e(0.9),
										e(12),
										e(0.33),
										e(1.0),
										e(2),
										e(1.0),
										e(Hash.class, e(0x5B7903A62B6AB343L))
								),
								e(TransformationFromSize.ToUnitBox_.class),
								e(Vec3.xyz(180, 640, 640)),
								e(false),
								e(false),
								e(Hash.class, e(0x8EE6B0C4E02CA7B2L))
						),
						e(ToneMapping2.class,
								e(ColorTransform::asColorTransformFromMatrixColor, e(Multiply.class, e(1.0)))
						)
				);
	}
	
}
