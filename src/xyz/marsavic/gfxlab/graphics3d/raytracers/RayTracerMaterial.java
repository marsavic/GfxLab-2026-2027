package xyz.marsavic.gfxlab.graphics3d.raytracers;

import xyz.marsavic.functions.F0;
import xyz.marsavic.functions.F1;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.RayTracer;
import xyz.marsavic.gfxlab.graphics3d.Scene;


/** A ray tracer without lighting: the color of a ray is the diffuse color at its first hit. */
public class RayTracerMaterial extends RayTracer {

	public RayTracerMaterial(F0<F1<Scene, Double>> ffSceneT) {
		super(ffSceneT);
	}


	@Override
	protected Color sample(Scene scene, Ray ray) {
		Hit hit = scene.solid().firstHit(ray);
		if (hit.t() == Double.POSITIVE_INFINITY) {
			return scene.colorBackground();
		}
		return hit.material().phong().diffuse();
	}

}
