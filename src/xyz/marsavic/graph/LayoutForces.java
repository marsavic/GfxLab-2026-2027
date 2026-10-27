package xyz.marsavic.graph;

import xyz.marsavic.geometry.Box;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.reactions.elements.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.marsavic.geometry.Vector.*;


public class LayoutForces implements Layout {
	
	public static LayoutForces INSTANCE = new LayoutForces() {};
	private LayoutForces() {}
	
	
	@Override
	public Map<Element, Box> at(Map<Element, Box> boxes, Map<Element, List<Edge>> map_inputEdges) {
		Element[] elements = boxes.keySet().toArray(new Element[0]);
		Iterable<Edge> edges = Layout.flatten(map_inputEdges.values());
		Map<Element, Box> boxesMutable = new HashMap<>(boxes);
		
		int iMax = 1000;
		
		for (int i = 0; i < iMax; i++) {
			double m = 1 + 32 * (1 - (double) i / iMax);
			double dMax = iteration(elements, boxesMutable, edges, m);
//			if (dMax <= 0.01) break;
		}
		
		return boxesMutable;
	}
	
	
	/** Returns the longest translation length squared. */
	private static double iteration(Element[] elements, Map<Element, Box> boxes, Iterable<Edge> edges, double magnitude) {
		Map<Element, Vector> forces = new HashMap<>();
		
		for (Edge edge : edges) {
			Element ei = edge.input ().element();
			Element eo = edge.output().element();
			
			Box bi = boxes.get(ei);
			Box bo = boxes.get(eo);
			
			Vector ci = bi.lerp(xy(0, 0.5));
			Vector co = bo.lerp(xy(1, 0.5));
			
			Vector d = ci.sub(co);
			double l = d.length();
			d = d.div(l + 16);
			
			forces.merge(ei, d.inverse(), Vector::add);
			forces.merge(eo, d          , Vector::add);
		}
		
		for (Element e0 : elements) {
			Box b0 = boxes.get(e0);
			for (Element e1 : elements) {
				if (e0 == e1) continue;
				Box b1 = boxes.get(e1);
				Vector d = b0.minOffsetToAvoidOverlap(b1);
				double l = d.length();
				if (!d.allZero()) {
					forces.merge(e0, d.mul(8 / (l + 4)), Vector::add);
				}
			}
		}
		
		
		double maxL = Double.NEGATIVE_INFINITY;
		for (Element e : elements) {
			Vector f = forces.get(e);
			double l = f.length();
			Vector d = f.mul(magnitude / (l + 10));
			boxes.computeIfPresent(e, (_, b) -> b.translate(d));
			
			maxL = Math.max(d.lengthSquared(), l);
		}
		
		return maxL;
	}
	
}
