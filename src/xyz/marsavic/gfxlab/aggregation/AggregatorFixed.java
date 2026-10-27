package xyz.marsavic.gfxlab.aggregation;

import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunction3;
import xyz.marsavic.gfxlab.arrays.Matrix;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.resources.Rr;
import xyz.marsavic.utils.Hash;


/** A fixed number of iterations per frame. Suitable for rendering to files. */
public class AggregatorFixed extends Aggregator {
	
	/** The factory of aggregators with nIterations iterations per frame. */
	public static F_Aggregator constructor(int nIterations) {
		return (colorFunction3, size, repeats, motionBlur, hash) -> new AggregatorFixed(colorFunction3, size, repeats, motionBlur, hash, nIterations);
	}
	
	
	private final int nIterations;
	
	

	public AggregatorFixed(ColorFunction3 colorFunction3, Vec3 size, boolean repeats, boolean motionBlur, Hash hash, int nIterations) {
		super(colorFunction3, size, repeats, motionBlur, hash);
		this.nIterations = nIterations;
	}
	
	
	@Override
	public Rr<Matrix<Color>> rFrame(int iFrame) {
		Aggregate aggregate;
		
		aggregate = createAggregate(iFrame);
		for (int i = 0; i < nIterations; i++) {
			aggregate.addSample();
		}
		var avg = aggregate.avg();
		aggregate.release();
		return avg;		
	}
	
	
	@Override
	public void release() {
	}
}
