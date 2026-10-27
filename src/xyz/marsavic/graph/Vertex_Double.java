package xyz.marsavic.graph;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import xyz.marsavic.gfxlab.UtilsGL;
import xyz.marsavic.reactions.elements.Element;
import xyz.marsavic.reactions.elements.ElementDouble;

import java.util.Collection;
import java.util.List;


public class Vertex_Double extends HBox implements Vertex {
	
	public final ElementDouble element;
	
	public final VertexOutputJack vertexOutputJack;
	public final List<VertexOutputJack> outputJacks;
	
	DoubleProperty pMantissa;
	ReadOnlyObjectProperty<Integer> pExponent;
	
	
	public Vertex_Double(ElementDouble element) {
		this.element = element;
		
		getStyleClass().add("vertex");
		getStyleClass().add("vertex-double");
		
		vertexOutputJack = new VertexOutputJack(element.out);
		Label label = new Label();
		
		double value = element.out.get();
		int exponent;
		double mantissa;
		if (value != 0.0) {
			exponent = (int) Math.ceil(Math.log10(Math.abs(value)));
			mantissa = value / Math.pow(10, exponent);
		} else {
			exponent = 0;
			mantissa = 0;
		}
//		System.out.printf("%f = %f * 10^%d\n", value, mantissa, exponent);
		
		Slider slider = new Slider(-1, 1, mantissa);
		pMantissa = slider.valueProperty();
		label.textProperty().bind(pMantissa.asString("%.2f"));
				
		Spinner<Integer> spinner = new Spinner<>(Integer.MIN_VALUE, Integer.MAX_VALUE, exponent);
		pExponent = spinner.valueProperty();
		
		slider.valueProperty().addListener((observable, oldValue, newValue) -> update());
		spinner.valueProperty().addListener((observable, oldValue, newValue) -> update());
		
		
		setAlignment(Pos.CENTER_LEFT);
		getChildren().addAll(
				slider,
				label,
				spinner,
				vertexOutputJack
		);		
		
		
		outputJacks = List.of(vertexOutputJack);
	}
	
	private double value() {
		return pMantissa.doubleValue() * Math.pow(10, pExponent.getValue());
	}
	
	private void update() {
		double v = value();
		UtilsGL.parallelReactions.submit(() -> element.setResult(v));
	}
	
	@Override public Element element() { return element; }	
	@Override public Collection<VertexInputJack>  inputJacks () { return List.of(); }
	@Override public Collection<VertexOutputJack> outputJacks() { return outputJacks; }
	@Override public Region region() { return this; }
	
}
