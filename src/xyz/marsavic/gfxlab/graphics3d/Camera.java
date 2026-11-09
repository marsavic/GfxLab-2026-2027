package xyz.marsavic.gfxlab.graphics3d;

import xyz.marsavic.geometry.Vector;


/** A camera: maps a position on the sensor to the ray exiting the camera. */
public interface Camera {
	/** The ray exiting the camera through the given position on the sensor (usually in the square [-1, 1]^2). */
	Ray exitingRay(Vector sensorPosition);
}
