package xyz.marsavic.graph;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.javafx.ColorPopup;
import xyz.marsavic.javafx.UtilsFX;
import xyz.marsavic.reactions.elements.Element;
import xyz.marsavic.reactions.elements.ElementValue;

import java.util.Collection;
import java.util.List;


/** Shows the color; a click on it opens or closes a ColorPopup, which changes the color while the user picks. */
public class Vertex_Color extends HBox implements Vertex {

	public final ElementValue<Color> element;

	public final VertexOutputJack vertexOutputJack;
	public final List<VertexOutputJack> outputJacks;

	private final Rectangle swatch = new Rectangle(20, 20);
	private ColorPopup popup;


	public Vertex_Color(ElementValue<Color> element) {
		this.element = element;

		getStyleClass().add("vertex");
		getStyleClass().add("vertex-color");

		vertexOutputJack = new VertexOutputJack(element.out);

		swatch.setFill(UtilsFX.toFX(element.out.get()));
		swatch.setOnMouseClicked(e -> {
			if (!e.isStillSincePress()) return;   // The end of dragging the vertex.
			if (popup != null && popup.isShowing()) {
				popup.hide();
			} else {
				popup = new ColorPopup(element.out.get(), this::update);
				popup.show(swatch);
			}
		});


		setAlignment(Pos.CENTER_LEFT);
		getChildren().addAll(
				swatch,
				vertexOutputJack
		);


		outputJacks = List.of(vertexOutputJack);
	}


	private void update(Color color) {
		swatch.setFill(UtilsFX.toFX(color));
		element.setResult(color);
	}


	@Override public Element element() { return element; }
	@Override public Collection<VertexInputJack>  inputJacks () { return List.of(); }
	@Override public Collection<VertexOutputJack> outputJacks() { return outputJacks; }
	@Override public Region region() { return this; }

}
