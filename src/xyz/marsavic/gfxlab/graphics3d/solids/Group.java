package xyz.marsavic.gfxlab.graphics3d.solids;

import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.Solid;

import java.util.Collection;


public class Group implements Solid {
	
	private final Solid[] solids;
	
	
	
	private Group(Solid[] solids) {
		this.solids = solids;
	}
	
	public static Group of(Solid... solids) {
		return new Group(solids);
	}
	
	public static Group of(Collection<? extends Solid> solids) {
		return new Group(solids.toArray(Solid[]::new));
	}
	
	
	@Override
	public Hit firstHit(Ray ray, double afterTime) {
		Hit minHit = Nothing.INSTANCE.firstHit(ray, afterTime);
		double minT = minHit.t();   // minHit.t(), cached to avoid repeated calls
		
		for (Solid s : solids) {
			Hit hit = s.firstHit(ray, afterTime);
			double t = hit.t();
			if (t < minT) {
				minT = t;
				minHit = hit;
			}
		}
		
		return minHit;
	}
	
	
	@Override
	public boolean hitBetween(Ray ray, double afterTime, double beforeTime) {
		for (Solid s : solids) {
			if (s.firstHit(ray, afterTime).t() < beforeTime) {
				return true;
			}
		}
		return false;
	}
	
}
