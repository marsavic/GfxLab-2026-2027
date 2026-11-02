package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Animation;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunction3;
import xyz.marsavic.gfxlab.TransformationFromSize;
import xyz.marsavic.gfxlab.aggregation.AggregatorOneAhead;
import xyz.marsavic.gfxlab.aggregation.EAggregator;
import xyz.marsavic.gfxlab.entry_points.App;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.gfxlab.tonemapping.ColorTransform;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping2;
import xyz.marsavic.gfxlab.tonemapping.ToneMapping3;
import xyz.marsavic.gfxlab.tonemapping.colortransforms.Identity;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.Hash;

import static xyz.marsavic.reactions.elements.Elements.*;


/**
 * Gradients from black (left) to white (right), with the sRGB encoded value quantized to nLevels levels, in three
 * bands, top to bottom:
 * - Without quantization (only the usual 8-bit encoding)
 * - Quantization by rounding
 * - Quantization with dithering: a random number from [0, 1), the same for all samples in a pixel, is added to the
 *   value before rounding down
 * The coordinates p are in pixels, and size is the size of the image.
 */
public record Dithering(
		Vector size,
		int nLevels
) implements ColorFunction3 {
	
	@Override
	public Color at(double t, Vector p) {
		double v = p.x() / size.x() * (nLevels - 1);    // The encoded value, in units of one level.
		double q = switch ((int) (3 * p.y() / size.y())) {
			case 0  -> v;
			case 1  -> Math.round(v);
			case 2  -> Math.floor(v + new Hash(0x2C5A3B1D7E9F4061L).add(p.xInt()).add(p.yInt()).getDouble());
			default -> throw new IllegalArgumentException("Invalid band index");
		};
		return Color.gray(Color.sRGBtoLinear(q / (nLevels - 1)));
	}
	
	
	// ================================================================================================================
	
	
	static void main() { App.run(); }
	
	
	public static ElementF<Animation> setupApp() {
		return
				e(ToneMapping3.class,
						new EAggregator(
								e(AggregatorOneAhead::new),
								e(Dithering.class,
										e(Vector::xy, e(640.0), e(240.0)),
										e(16)
								),
								e(TransformationFromSize.ToIdentity_.class),
								e(Vec3.xyz(1, 640, 240)),
								e(true),
								e(false),
								e(Hash.class, e(0x8EE6B0C4E02CA7B2L))
						),
						e(ToneMapping2.class,
								e(ColorTransform::asColorTransformFromMatrixColor, e(Identity.class))
						)
				);
	}
	
}
