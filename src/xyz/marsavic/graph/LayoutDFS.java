package xyz.marsavic.graph;

import xyz.marsavic.geometry.Box;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.reactions.elements.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public final class LayoutDFS implements Layout {

	public static LayoutDFS INSTANCE = new LayoutDFS();
	private LayoutDFS() {}
	
	
	private static class Data {
		Box box;
		List<Edge> edges;
		Vector sizeSubtree, sizeChildrenSubtrees;
	}
	
	
	@Override
	public Map<Element, Box> at(Map<Element, Box> boxes, Map<Element, List<Edge>> map_inputEdges) {
		Map<Element, Data> map_data = new HashMap<>();
		
		for (Map.Entry<Element, Box> entry : boxes.entrySet()) {
			Element e = entry.getKey();
			Data data = new Data();
			data.edges = map_inputEdges.get(e);
			data.box = entry.getValue();
			map_data.put(e, data);
		}
		
		for (Element e : map_data.keySet()) {
			if (e.outputs().isEmpty()) {
				dfs0(map_data, e);
				dfs1(map_data, e);
			}
		}
		
		return map_data.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				e -> e.getValue().box
		));
	}
	
	
	private static void dfs0(Map<Element, Data> map_data, Element e) {
		Data data = map_data.get(e);
		
		double w = 0;
		double h = 0;
		
		for (Edge edge : data.edges) {
			Element eChild = edge.output().element();
			dfs0(map_data, eChild);
			Data dataChild = map_data.get(eChild);
			Vector dSubtree = dataChild.sizeSubtree;
			w = Math.max(w, dSubtree.x());
			h += dSubtree.y();
		}
		
		Data dataMe = map_data.get(e);
		dataMe.sizeChildrenSubtrees = Vector.xy(w, h);
		w += dataMe.box.d().x();
		h = Math.max(h, dataMe.box.d().y());
		dataMe.sizeSubtree = Vector.xy(w, h);
	}
	
	
	private static void dfs1(Map<Element, Data> map_data, Element e) {
		Data data = map_data.get(e);
		
		double x = data.box.x().p();
		double y = data.box.y().c() - data.sizeChildrenSubtrees.y() / 2;
		
		for (Edge edge : data.edges) {
			Element eChild = edge.output().element();
			Data dataChild = map_data.get(eChild);
			
			double ys = y + (dataChild.sizeSubtree.y() - dataChild.box.y().d()) / 2;
			
			dataChild.box = Box.pd(
					Vector.xy(x - dataChild.box.d().x(), ys),
					dataChild.box.d()
			);
			y += dataChild.sizeSubtree.y();
			dfs1(map_data, eChild);
		}
	}
	
}
