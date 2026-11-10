package xyz.marsavic.gfxlab.aggregation;

import xyz.marsavic.gfxlab.arrays.*;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.*;
import xyz.marsavic.resources.Rr;
import xyz.marsavic.utils.Hash;
import xyz.marsavic.utils.Utils;

import java.util.random.RandomGenerator;


/** The sum of the iterations of one frame and their number, from which the average is computed. */
public class Aggregate {
	private final ColorFunction3 colorFunction3;
	private final double t;
	private final Vector size;
	private final Hash hash;
	

	private State state; // Read and replaced only while holding the monitor of this. Null after release.

	// Held for the whole addSample, so that samples are added one at a time and release waits for the current sample.
	// The monitor of this is held only briefly, so that the average can be computed while a sample is being added.
	private final Object lockSampling = new Object();



	private record State(
			int count,
			Rr<Matrix<Color>> rSum
	) {
		private void release() {
			if (rSum != null) {
				rSum.release();
			}
		}

		private Rr<Matrix<Color>> avg() {
			if (count == 0) {
				throw new IllegalStateException("No samples. Cannot compute average.");
			}
			return rSum.f(sum -> {
				Rr<Matrix<Color>> rAvg = UtilsGL.matricesColor.obtain(sum.size(), true);
				rAvg.a(count == 1 ? 
						avg -> avg.copyFrom(sum) :                         // Optimization only, so we don't divide by 1
						avg -> MatrixColor.mul(sum, 1.0 / count, avg)
				);
				return rAvg;
			});
		}
	}
	
	

	public Aggregate(ColorFunction3 colorFunction3, double t, Vector size, Hash hash) {
		// TODO add a boolean parameter stating whether to immediately do the first sample.
		//  If so, then optimize by sending the first sample straight into the sum matrix
		//  (instead of making a zero matrix, computing the sample, and them adding them together).
		this.colorFunction3 = colorFunction3;
		this.t = t;
		this.size = size;
		this.hash = hash.add(t);
		state = new State(0, null);
	}
	
	
	private synchronized State getState() {
		return state;
	}
	
	
	private synchronized void setState(State stateNew) {
		State stateOld = state;
		state = stateNew;
		stateOld.release();
		notifyAll();
	}
	
	
	public void release() {
		synchronized (lockSampling) {
			synchronized (this) {
				if (state != null) {
					state.release();
					state = null;
				}
			}
		}
	}


	/** Does nothing if this aggregate is released. AggregatorFrameLast can call it on an aggregate that a frame change has just released. */
	public void addSample() {
		synchronized (lockSampling) {
			// Instead of rewriting the same "sum" matrix, we borrow a new one and release the old one. This is
			// done to avoid dealing with concurrency issues. It would be faster and more memory efficient if we
			// added the new sample in-place, but it's more troublesome.

			State stateOld = getState();
			if (stateOld == null) {
				return;
			}
			Hash hashFrame = hash.add(stateOld.count);
			State stateNew = new State(stateOld.count + 1, UtilsGL.matricesColor.obtain(size, true));
			int sizeX = size.xInt();
			stateNew.rSum.a(mSumNew -> {
				if (stateOld.count == 0) {                // For optimization only, no other reason to consider this case separately.
					UtilsGL.parallelSampling.parallelY(size, y -> {
						RandomGenerator rng = hashFrame.add(y).rng();
						for (int x = 0; x < sizeX; x++) {
							Color c = colorFunction3.at(t + rng.nextDouble(), Vector.xy(x + rng.nextDouble(), y + rng.nextDouble()));
							mSumNew.set(x, y, c);
						}
					});
				} else {
					stateOld.rSum.a(mSumOld -> {
						UtilsGL.parallelSampling.parallelY(size, y -> {
							RandomGenerator rng = hashFrame.add(y).rng();
							for (int x = 0; x < sizeX; x++) {
								Color c = colorFunction3.at(t + rng.nextDouble(), Vector.xy(x + rng.nextDouble(), y + rng.nextDouble()));
								mSumNew.set(x, y, mSumOld.at(x, y).add(c));
							}
						});
					});
				}
			});

			setState(stateNew);
		}
	}


	/** Computed while holding the monitor of this, so that addSample cannot replace and release the state in the meantime. */
	public synchronized Rr<Matrix<Color>> avg() {
		return state.avg();
	}


	/** If there are no samples, it will block while waiting for at least one sample before computing the average. */
	public Rr<Matrix<Color>> avgAtLeastOne() {
		return Utils.waitWhile(this, () -> state.count == 0, this::avg);
	}
	
	
}
