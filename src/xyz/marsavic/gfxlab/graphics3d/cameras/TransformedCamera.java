package xyz.marsavic.gfxlab.graphics3d.cameras;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.graphics3d.Camera;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.math.Affine3;


public record TransformedCamera(
		Camera source,
		Affine3 transformation
) implements Camera {
	
	@Override
	public Ray exitingRay(Vector sensorPosition) {
		Ray ray = source.exitingRay(sensorPosition);
		return ray.transformed(transformation);
	}
	
}
