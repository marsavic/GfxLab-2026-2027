package xyz.marsavic.gfxlab.aggregation;

import xyz.marsavic.gfxlab.arrays.*;
import xyz.marsavic.gfxlab.math.*;
import xyz.marsavic.functions.A1;
import xyz.marsavic.functions.F1;
import xyz.marsavic.gfxlab.*;
import xyz.marsavic.reactions.Event;
import xyz.marsavic.reactions.elements.ElementSingleOutput;
import xyz.marsavic.reactions.elements.HasOutput;
import xyz.marsavic.reactions.values.EventInvalidated;
import xyz.marsavic.resources.Rr;
import xyz.marsavic.utils.Hash;


/** The element of the graph editor that creates an aggregator from its inputs and outputs its frames. */
public class EAggregator extends ElementSingleOutput<F1<Rr<Matrix<Color>>, Integer>> {
	
	public final Input<Aggregator.F_Aggregator> inFAggregator;
	public final Input<ColorFunction3> inColorFunction;
	public final Input<TransformationFromSize> inTransformationFromSize;
	public final Input<Vec3> inSize;
	public final Input<Boolean> inRepeats;
	public final Input<Boolean> inMotionBlur;
	public final Input<Hash> inHash;
	
	
	private final A1<EventInvalidated> onSampleAdded = this::onSampleAdded;
	
	
	public EAggregator(
			HasOutput<Aggregator.F_Aggregator> outFAggregator,
			HasOutput<ColorFunction3> outColorFunction,
			HasOutput<TransformationFromSize> outTransformationFromSize,
			HasOutput<Vec3> outSize,
			HasOutput<Boolean> outRepeats,
			HasOutput<Boolean> outMotionBlur,
			HasOutput<Hash> outHash
	) {
		super("Aggregator");
		inFAggregator = new Input<>("aggregatorFactory", Aggregator.F_Aggregator.class, outFAggregator);
		inColorFunction = new Input<>("colorFunction", ColorFunction3.class, outColorFunction);
		inTransformationFromSize = new Input<>("transformationFromSize", TransformationFromSize.class, outTransformationFromSize);
		inSize = new Input<>("size", Vec3.class, outSize);
		inRepeats = new Input<>("repeats", Boolean.class, outRepeats);
		inMotionBlur = new Input<>("motionBlur", Boolean.class, outMotionBlur);
		inHash = new Input<>("hash", Hash.class, outHash);
		
		getAggregator(); // Just to create an aggregator, maybe it wants to start some job immediately.
	}
	

	private Aggregator aggregator;
	
	private final Object lock = new Object();
	private boolean invalidated = true;
	
	
	@Override
	protected <T> void onInputChanged(Input<T> input, Event event) {
		invalidated = true;
		outputs().forEach(Output::fireInvalidated);
	}
	

	private Aggregator getAggregator() {
		synchronized (lock) {
			if (invalidated) {
				invalidated = false;
				if (aggregator != null) {
					aggregator.release();
				}
				
				Vec3 size = inSize.get();
				TransformedColorFunction3 tcf3 =
						new TransformedColorFunction3(
								inColorFunction.get(),
								inTransformationFromSize.get().at(size)
						);
				aggregator = inFAggregator.get().at(tcf3, size, inRepeats.get(), inMotionBlur.get(), inHash.get());
				aggregator.onInvalidated().add(onSampleAdded);
			}
			
			return aggregator;
		}
	} 

	
	@Override
	public F1<Rr<Matrix<Color>>, Integer> result() {
		return iFrame -> {
			synchronized (lock) {
				return getAggregator().rFrameAt(iFrame);
			}
		};
	}
	
	
	private void onSampleAdded(EventInvalidated e) {
		out.fireInvalidated();
	}
	
}
