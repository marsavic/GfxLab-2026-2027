package xyz.marsavic.gfxlab.aggregation;

import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunction3;
import xyz.marsavic.gfxlab.arrays.Matrix;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.gfxlab.UtilsGL;
import xyz.marsavic.resources.Rr;
import xyz.marsavic.time.Profiler;
import xyz.marsavic.utils.Hash;
import xyz.marsavic.utils.Loop;


/**
 * Keeps adding iterations to the last requested frame, which gets less noisy over time. Suitable for still images and
 * ray tracing.
 */
public class AggregatorFrameLast extends Aggregator {
	
	private record IFrameAggregate(int iFrame, Aggregate aggregate) {}
	
	private IFrameAggregate iFrameAggregateLast = null; // Must not be changed outside synchronized blocks.
	private final Profiler profilerLoop = UtilsGL.profiler(this, "add sample");
	private final Loop loop;
	
	
	
	public AggregatorFrameLast(ColorFunction3 colorFunction3, Vec3 size, boolean repeats, boolean motionBlur, Hash hash) {
		super(colorFunction3, size, repeats, motionBlur, hash);
		
		changeActiveAggregate(0);
		
		loop = new Loop(this::addSample, this::loopFinalize);
	}
	
	
	private synchronized IFrameAggregate changeActiveAggregate(int iFrame) {
		IFrameAggregate iFrameAggregateLastOld = iFrameAggregateLast;
		iFrameAggregateLast = new IFrameAggregate(iFrame, createAggregate(iFrame));
		
		if (iFrameAggregateLastOld != null) {
			iFrameAggregateLastOld.aggregate.release();
		}
		
		return iFrameAggregateLast;
	}
	
	
	@Override
	public Rr<Matrix<Color>> rFrame(int iFrame) {
		IFrameAggregate iFrameAggregate;
		synchronized (this) {
			iFrameAggregate = (iFrame == iFrameAggregateLast.iFrame) ?
					iFrameAggregateLast :
					changeActiveAggregate(iFrame);
		}
		
		return iFrameAggregate.aggregate.avgAtLeastOne();
	}
	
	
	private void addSample() {
		profilerLoop.measure(() -> {
			iFrameAggregateLast.aggregate.addSample();
		});
		fireInvalidatedAsync(UtilsGL.parallelReactions.executorService()); // TODO Fire EventInvalidatedFrame instead
	}

	private void loopFinalize() {
		iFrameAggregateLast.aggregate.release();
	}
		
	
	@Override
	public synchronized void release() {
		loop.demand(Loop.State.STOPPED);
	}
}
