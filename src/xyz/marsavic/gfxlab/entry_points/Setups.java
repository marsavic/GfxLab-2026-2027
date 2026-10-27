package xyz.marsavic.gfxlab.entry_points;

import xyz.marsavic.gfxlab.Animation;
import xyz.marsavic.reactions.elements.ElementF;


/** Finds the class that calls an entry point, and calls a static setup method (setupApp, setupBenchmark, setupRender) of a class. */
final class Setups {
	
	static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
	
	
	@SuppressWarnings("unchecked")
	static ElementF<Animation> invoke(Class<?> c, String methodName) {
		try {
			return (ElementF<Animation>) c.getMethod(methodName).invoke(null);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
}
