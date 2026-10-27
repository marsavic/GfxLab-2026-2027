package xyz.marsavic.gfxlab;

import xyz.marsavic.gfxlab.math.*;
import xyz.marsavic.utils.Numeric;

import java.util.function.DoubleUnaryOperator;

/**
 * An immutable color in the linear sRGB color space: the components are proportional to the amounts of red, green,
 * and blue light. The components are not limited to [0, 1] (high dynamic range); only colors with components in
 * [0, 1] can be shown on the screen. The sRGB encoding (gamma) is applied only when the color is converted to a code.
 */
public record Color(
		double r,
		double g,
		double b
) {
	public static final Color BLACK = rgb(0, 0, 0);
	public static final Color WHITE = rgb(1, 1, 1);
	/** A conspicuous color, for marking places in images while debugging. */
	public static final Color DEBUG = rgb(1, 0, 0.5);
	public static final Color NAN = rgb(Double.NaN, Double.NaN, Double.NaN);
	
	
	/** The color with the components r, g, b. */
	public static Color rgb(double r, double g, double b) {
		return new Color(r, g, b);
	}
	
	
	/** The color with the components given by the coordinates of v. */
	public static Color rgb(Vec3 v) {
		return rgb(v.x(), v.y(), v.z());
	}
	
	
	/** The gray color with all components equal to k. */
	public static Color gray(double k) {
		return rgb(k, k, k);
	}
	
	
	/** The color with the hue h (in turns), saturation s, and brightness b, all in [0, 1]. */
	public static Color hsb(double h, double s, double b) {
		h = Numeric.mod(h);
		int base = (int) (h * 6.0);
		double f = h * 6.0 - base;
		double p = b * (1.0 - s);
		double q = b * (1.0 - s * f);
		double t = b * (1.0 - s * (1.0 - f));
		return switch (base) {
			case 0, 6 -> Color.rgb(b, t, p);   // base = 6 when h is slightly below 0, and mod(h) rounds to 1
			case 1 -> Color.rgb(q, b, p);
			case 2 -> Color.rgb(p, b, t);
			case 3 -> Color.rgb(p, q, b);
			case 4 -> Color.rgb(t, p, b);
			case 5 -> Color.rgb(b, p, q);
			default -> throw new RuntimeException("Can't happen");
		};
	}
	
	
	public static Color hsb(Vec3 v) {
		return hsb(v.x(), v.y(), v.z());
	}
	
	
	/** The color with the coordinates (l, a, b) in the perceptual Oklab color space. */
	public static Color oklab(double l, double a, double b) {
		double l_ = l + 0.3963377774 * a + 0.2158037573 * b;
		double m_ = l - 0.1055613458 * a - 0.0638541728 * b;
		double s_ = l - 0.0894841775 * a - 1.2914855480 * b;
		
		double cl = l_ * l_ * l_;
		double cm = m_ * m_ * m_;
		double cs = s_ * s_ * s_;
		
		double cr =  4.0767245293 * cl - 3.3072168827 * cm + 0.2307590544 * cs;
		double cg = -1.2681437731 * cl + 2.6093323231 * cm - 0.3411344290 * cs;
		double cb = -0.0041119885 * cl - 0.7034763098 * cm + 1.7068625689 * cs;
		
		return Color.rgb(cr, cg, cb);
	}
	
	
	public static Color oklab(Vec3 v) {
		return oklab(v.x(), v.y(), v.z());
	}
	
	
	/** The color with the hue h (in turns), chroma c, and lightness l in the Oklab color space. */
	public static Color okhcl(double h, double c, double l) {
		return oklab(
			l,
			c * Numeric.cosT(h),
			c * Numeric.sinT(h)
		);
	}
	
	
	public static Color okhcl(Vec3 v) {
		return okhcl(v.x(), v.y(), v.z());
	}
	
	
	/** The coordinates (l, a, b) of this color in the Oklab color space. */
	public Vec3 oklab() {
		double l_ = Math.cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b);
		double m_ = Math.cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b);
		double s_ = Math.cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b);
		
		double okl = 0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_;
		double oka = 1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_;
		double okb = 0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_;
		
		return Vec3.xyz(okl, oka, okb);
	}
	
	
	/** The coordinates (h, c, l) of this color in the cylindrical form of the Oklab color space. */
	public Vec3 okhcl() {
		Vec3 oklab = oklab();
		
		double c = Math.sqrt(oklab.y() * oklab.y() + oklab.z() * oklab.z());
		double h = Numeric.atan2T(oklab.z(), oklab.y());
		return Vec3.xyz(h, c, oklab.x());
	}
	
	
	/** The color with the given 32-bit ARGB code (8-bit sRGB encoded components; alpha is ignored). */
	public static Color code(int code) {
		return rgb(
				byteToValue((code >> 16) & 0xFF),
				byteToValue((code >>  8) & 0xFF),
				byteToValue((code      ) & 0xFF)
		);
	}
	

	/** The componentwise sum. */
	public Color add(Color o) {
		return rgb(r + o.r, g + o.g, b + o.b);
	}
	
	
	/** The componentwise difference. */
	public Color sub(Color o) {
		return rgb(r - o.r, g - o.g, b - o.b);
	}
	
	
	/** All components multiplied by c. */
	public Color mul(double c) {
		return rgb(r * c, g * c, b * c);
	}
	
	
	/** The componentwise product, e.g. light of color this reflected from a surface of color o. */
	public Color mul(Color o) {
		return rgb(r * o.r, g * o.g, b * o.b);
	}
	
	
	/** All components divided by c. */
	public Color div(double c) {
		return rgb(r / c, g / c, b / c);
	}
	
	
	/** All components raised to the power c. */
	public Color pow(double c) {
		return rgb(
				Math.pow(r, c),
				Math.pow(g, c),
				Math.pow(b, c)
		);
	}
	
	
	/** The function f applied to each component. */
	public Color f(DoubleUnaryOperator f) {
		return rgb(
				f.applyAsDouble(r),
				f.applyAsDouble(g),
				f.applyAsDouble(b)
		);
	}
	
	
	/** The luminance Y: the physical brightness of the color, a weighted sum of its components. */
	public double luminance() {
		return
				0.212655 * r +
				0.715158 * g +
				0.072187 * b;
	}
	
	
	/** The perceived lightness L* (CIE 1976), in [0, 100], for colors with components in [0, 1]. */
	public double perceivedLightness() {
		double y = luminance(); // y should be between 0.0 and 1.0
		
		if (y <= 216.0 / 24389.0 ) {      // The CIE standard states   0.008856, but ( 6/29)^3 =   216/24389 is the intent for   0.008856451679036.
			return y * (24389.0 / 27.0);  // The CIE standard states 903.3     , but (29/ 3)^3 = 24389/   27 is the intent for 903.296296296296296.
		} else {
			return Math.cbrt(y) * 116.0 - 16.0;
		}
	}
	
	
	/** The coordinates (h, s, b) of this color in the HSB color model. */
	public Vec3 hsb() {
		double brightness = Math.max(Math.max(r, g), b);
		double delta = brightness - Math.min(Math.min(r, g), b);
		double saturation = brightness == 0 ? 0 : delta / brightness;
		double hue;
		
		if (saturation == 0) {
			hue = 0;
		} else {
			double redC   = (brightness - r) / delta;
			double greenC = (brightness - g) / delta;
			double blueC  = (brightness - b) / delta;
			if (r == brightness) {
				hue = blueC - greenC;
			} else if (g == brightness) {
				hue = 2.0 + redC - blueC;
			} else {
				hue = 4.0 + greenC - redC;
			}
			hue = hue / 6.0;
			if (hue < 0) {
				hue = hue + 1.0;
			}
		}
		
		return Vec3.xyz(hue, saturation, brightness);
	}
	
	
	/** The largest component. */
	public double max() {
		return Math.max(r, Math.max(g, b));
	}
	
	
	/** The 32-bit ARGB code of this color, with 8-bit sRGB encoded components; the components must be in [0, 1]. */
	public int code() {
		return
				(0xFF000000)    |
				(valueToByte(r) << 16) |
				(valueToByte(g) <<  8) |
				(valueToByte(b)      );
	}
	
	
	/** The code of this color, with the components first clamped to [0, 1]. */
	public int codeClamp() {
		return
				(0xFF000000)    |
				(valueToByte(Numeric.clamp(r)) << 16) |
				(valueToByte(Numeric.clamp(g)) <<  8) |
				(valueToByte(Numeric.clamp(b))      );
	}
	
	
	/** The code of this color if its components are in [0, 1], and d otherwise. */
	public int codeOrDefault(int d) {
		return this.in01() ? code() : d;
	}
	
	
	/** The code of this color if its components are in [0, 1], and the code of black otherwise. */
	public int codeOrBlack() {
		return this.in01() ? code() : 0xFF000000;
	}
	
	
	public boolean zero() {
		return (r == 0.0 && g == 0.0 && b == 0.0);
	}
	
	public boolean notZero() {
		return (r != 0.0 || g != 0.0 || b != 0.0);
	}
	
	public boolean one() {
		return (r == 1.0 && g == 1.0 && b == 1.0);
	}
	
	public boolean notOne() {
		return (r != 1.0 || g != 1.0 || b != 1.0);
	}
	
	
	/** Whether all components are in [0, 1]. */
	public boolean in01() {
		return
				r >= 0 && r <= 1 &&
				g >= 0 && g <= 1 &&
				b >= 0 && b <= 1;
	}
	
	
	/** This color if all its components are in [0, 1], and d otherwise. */
	public Color if01or(Color d) {
		return in01() ? this : d;
	}
	
	
	/** The color with components clamped to [0, 1]. */
	public Color clampTo01() {
		return new Color(
				Numeric.clamp(r),
				Numeric.clamp(g),
				Numeric.clamp(b)
		);
	}
	
	
	/** The linear interpolation between this (t = 0) and o (t = 1). */
	public Color lerp(Color o, double t) {
		return this.add(o.sub(this).mul(t));
	}
	
	
	@Override
	public String toString() {
		return String.format("(r: %6.3f, g: %6.3f, b: %6.3f | Y: %6.3f)", r, g, b, luminance());
	}
	

	// ====================================================================================================
	
	
	/** Decodes an sRGB encoded value in [0, 1] into the linear value (approximately v^2.2). */
	public static double sRGBtoLinear(double v) {
		if (v <= 0.04045) {
			return v / 12.92;
		} else {
			return Math.pow((v + 0.055) / 1.055, 2.4);
		}
	}
	
	
	/** Encodes a linear value in [0, 1] into the sRGB encoded value (approximately v^(1/2.2)); inverse of sRGBtoLinear. */
	public static double linearToSRGB(double v) {
		if (v <= 0.0031308) {
			return v * 12.92;
		} else {
			return 1.055 * Math.pow(v, 1.0 / 2.4) - 0.055;
		}
	}
	
	
	/** Encodes a linear component value in [0, 1] into a byte (0 to 255) by the sRGB encoding. */
	public static int valueToByte(double x) {
		return (int) (linearToSRGB(x) * 255 + 0.5);
	}
	
	
	/** Decodes a byte (0 to 255) of an sRGB encoded component into the linear value. */
	public static double byteToValue(int x) {
		return sRGBtoLinear(x / 255.0);
	}
	
}
