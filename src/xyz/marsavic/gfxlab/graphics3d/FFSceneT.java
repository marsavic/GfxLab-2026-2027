package xyz.marsavic.gfxlab.graphics3d;

import xyz.marsavic.functions.F0;
import xyz.marsavic.functions.F1;
import xyz.marsavic.gfxlab.Color;

import java.util.Collection;


/**
 * A producer for a scene with time dependency.
 * <p>
 * For scene elements that are independent of time, override methods solid(), camera(), lights(), colorBackground().
 * <p>
 * For scene elements that are dependent on time, override methods fSolidT(), fCameraT(), fLightsT(), fColorBackgroundT().
 * These must return a function that maps time to the corresponding scene element.
 * <p>
 * Do not override both fSolidT() and solid() at the same time, fCameraT() and camera(), etc.
 */
public interface FFSceneT extends F0<F1<Scene, Double>> {
	
	default F1<Solid            , Double> fSolidT          () { var solid           = solid          (); return t -> solid          ; }
	default F1<Camera           , Double> fCameraT         () { var camera          = camera         (); return t -> camera         ; }
	default F1<Collection<Light>, Double> fLightsT         () { var lights          = lights         (); return t -> lights         ; }
	default F1<Color            , Double> fColorBackgroundT() { var colorBackground = colorBackground(); return t -> colorBackground; }
	
	default Solid             solid          () { return Scene.defaultSolid          ; }
	default Camera            camera         () { return Scene.defaultCamera         ; }
	default Collection<Light> lights         () { return Scene.defaultLights         ; }
	default Color             colorBackground() { return Scene.defaultColorBackground; }

	
	
	@Override
	default F1<Scene, Double> at() {
		// Caching
		
		F1<Solid            , Double> fSolidT           = fSolidT          ();
		F1<Camera           , Double> fCameraT          = fCameraT         ();
		F1<Collection<Light>, Double> fLightsT          = fLightsT         ();
		F1<Color            , Double> fColorBackgroundT = fColorBackgroundT();
		
		return t -> new Scene.Record(
				fSolidT          .at(t),
				fCameraT         .at(t),
				fLightsT         .at(t),
				fColorBackgroundT.at(t)
		);
	}
}
