package xyz.marsavic.gfxlab.graphics3d;

import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.graphics3d.cameras.Perspective;
import xyz.marsavic.gfxlab.graphics3d.solids.Nothing;

import java.util.Collection;
import java.util.List;


/**
 * A scene at one moment of time: a solid, a camera, lights, and a background color. Each component has a default,
 * so a scene overrides only the methods it needs.
 */
public interface Scene {
	Solid             defaultSolid           = Nothing.INSTANCE;
	Camera            defaultCamera          = Perspective.DEFAULT;
	Collection<Light> defaultLights          = List.of();
	Color             defaultColorBackground = Color.BLACK;
	
	default Solid             solid          () { return defaultSolid          ; }
	default Camera            camera         () { return defaultCamera         ; }
	default Collection<Light> lights         () { return defaultLights         ; }
	default Color             colorBackground() { return defaultColorBackground; }
	
	/** A scene given by its components. */
	record Record(
			Solid solid,
			Camera camera,
			Collection<Light> lights,
			Color colorBackground
	) implements Scene {}
	
}


