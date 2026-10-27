package xyz.marsavic.graph;

import xyz.marsavic.functions.F1;
import xyz.marsavic.functions.F2;
import xyz.marsavic.geometry.Box;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.reactions.elements.Element;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public interface Layout extends F2<
		Map<Element, Box>,
		
		Map<Element, Box>,
		Map<Element, List<Edge>>
	> {
	
	
	static Layout combine(Layout... layouts) {
		return (boxes, map_inputEdges) -> {
			Map<Element, Box> result = boxes;
			for (Layout layout : layouts) {
				result = layout.at(result, map_inputEdges);
			}
			return result;
		};
	}
	
	
	Layout CENTER = (boxes, edges) -> {
		Vector d = Box.span(boxes.values()).c().inverse();
		return Layout.mapTransform(boxes, b -> b.translate(d));
	};
	
	
	Layout QUANTIZE = (boxes, edges) -> Layout.mapTransform(boxes, b -> Box.pq(b.p().round(), b.q().round()));
	
	
	static Layout margins(Vector margin) {
		Vector halfMargin = margin.mul(0.5);
		return (boxes, edges) -> Layout.mapTransform(boxes, b -> b.grow(halfMargin));
	}


	
	// TODO Move to Utils
	static <K, V, U> Map<K, U> mapTransform(Map<K, V> source, F1<U, V> f) {
		return source.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				e -> f.at(e.getValue())
		));
	}
	
	
	// TODO Move to Utils
	static <V> Iterable<V> flatten(Iterable<? extends Iterable<V>> iterable) {
		return () -> StreamSupport.stream(iterable.spliterator(), false)
				.flatMap(inner -> StreamSupport.stream(inner.spliterator(), false))
				.iterator();
	}

}
