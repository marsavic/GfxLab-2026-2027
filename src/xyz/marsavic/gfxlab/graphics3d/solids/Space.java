package xyz.marsavic.gfxlab.graphics3d.solids;

import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.Solid;


public class Space implements Solid {
	
	public static final Space INSTANCE = new Space();
	
	@Override
	public Hit firstHit(Ray ray, double afterTime) {
		return Hit.AtPositiveInfinity.axisAlignedGoingOut(ray.d());
	}
	
}
