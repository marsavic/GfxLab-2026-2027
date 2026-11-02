package xyz.marsavic.graph;

import javafx.geometry.Pos;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.javafx.UtilsFX;
import xyz.marsavic.reactions.elements.Element;
import xyz.marsavic.reactions.elements.ElementValue;

import java.util.Collection;
import java.util.List;


public class Vertex_Vec3 extends HBox implements Vertex {

	public final ElementValue<Vec3> element;

	public final VertexOutputJack vertexOutputJack;
	public final List<VertexOutputJack> outputJacks;

	private final Spinner<Double> spnX, spnY, spnZ;


	public Vertex_Vec3(ElementValue<Vec3> element) {
		this.element = element;

		getStyleClass().add("vertex");
		getStyleClass().add("vertex-vec3");

		vertexOutputJack = new VertexOutputJack(element.out);

		Vec3 value = element.out.get();
		spnX = spinner(value.x());
		spnY = spinner(value.y());
		spnZ = spinner(value.z());


		setAlignment(Pos.CENTER_LEFT);
		getChildren().addAll(
				spnX,
				spnY,
				spnZ,
				vertexOutputJack
		);


		outputJacks = List.of(vertexOutputJack);
	}


	/** An editable spinner for one coordinate, with the step 1. */
	private Spinner<Double> spinner(double value) {
		Spinner<Double> spinner = new Spinner<>(-Double.MAX_VALUE, Double.MAX_VALUE, value, 1);
		UtilsFX.makeEditable(spinner, "-?\\d*\\.?\\d*([eE]-?\\d*)?", Double::valueOf, 0.0, _ -> update());
		spinner.setPrefWidth(100);
		return spinner;
	}

	private void update() {
		element.setResult(Vec3.xyz(spnX.getValue(), spnY.getValue(), spnZ.getValue()));
	}


	@Override public Element element() { return element; }
	@Override public Collection<VertexInputJack>  inputJacks () { return List.of(); }
	@Override public Collection<VertexOutputJack> outputJacks() { return outputJacks; }
	@Override public Region region() { return this; }

}
