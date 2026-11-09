package xyz.marsavic.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Popup;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.marsavic.functions.A1;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.gfxlab.resources.Resources;
import xyz.marsavic.utils.Numeric;

import java.util.List;


/**
 * A popup for picking a color by the hue, saturation, and brightness of its sRGB encoded components, as in common
 * color pickers. onChange receives each color while the user picks it. Cancel gives onChange the initial color again.
 * A click outside the popup closes it and keeps the picked color.
 */
public class ColorPopup extends Popup {

	private static final double SIZE = 256;
	private static final double HUE_BAR_WIDTH = 24;

	// The fills are exact: the gradients interpolate the sRGB encoded components, as HSB does.
	private static final BackgroundFill FILL_WHITENING = new BackgroundFill(LinearGradient.valueOf("to right, #ffffff, #ffffff00"), null, null);
	private static final BackgroundFill FILL_DARKENING = new BackgroundFill(LinearGradient.valueOf("to bottom, #00000000, #000000"), null, null);
	private static final BackgroundFill FILL_HUES = new BackgroundFill(LinearGradient.valueOf("to bottom, #ff0000, #ffff00, #00ff00, #00ffff, #0000ff, #ff00ff, #ff0000"), null, null);

	private final A1<Color> onChange;

	/** The hue (in turns), saturation, and brightness of the sRGB encoded components of the picked color. */
	private double h, s, b;

	private final Pane square = new Pane();
	private final Circle squareMarker = new Circle(5);
	private final Rectangle hueMarker = new Rectangle(HUE_BAR_WIDTH, 4);
	private final Rectangle preview = new Rectangle(24, 24);


	public ColorPopup(Color initial, A1<Color> onChange) {
		this.onChange = onChange;

		Vec3 hsb = initial.clampTo01().f(Color::linearToSRGB).hsb();
		h = hsb.x();
		s = hsb.y();
		b = hsb.z();

		square.setPrefSize(SIZE, SIZE);
		square.getChildren().add(squareMarker);
		square.setOnMousePressed(this::pickSaturationBrightness);
		square.setOnMouseDragged(this::pickSaturationBrightness);

		Pane hueBar = new Pane(hueMarker);
		hueBar.setPrefSize(HUE_BAR_WIDTH, SIZE);
		hueBar.setBackground(new Background(FILL_HUES));
		hueBar.setOnMousePressed(this::pickHue);
		hueBar.setOnMouseDragged(this::pickHue);

		for (Shape marker : List.of(squareMarker, hueMarker)) {
			marker.setFill(null);
			marker.setStroke(javafx.scene.paint.Color.WHITE);
			marker.setStrokeWidth(2);
			marker.setEffect(new DropShadow(2, javafx.scene.paint.Color.BLACK));
		}

		Button ok = new Button(null, new FontIcon(Resources.Ikons.OK));
		ok.setDefaultButton(true);
		ok.setOnAction(_ -> hide());

		Button cancel = new Button(null, new FontIcon(Resources.Ikons.CANCEL));
		cancel.setCancelButton(true);
		cancel.setOnAction(_ -> {
			onChange.at(initial);
			hide();
		});

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		HBox bottom = new HBox(8, preview, spacer, ok, cancel);
		bottom.setAlignment(Pos.CENTER_LEFT);

		VBox root = new VBox(8, new HBox(8, square, hueBar), bottom);
		root.setPadding(new Insets(8));
		root.getStyleClass().add("color-popup");

		getContent().add(root);
		setAutoHide(true);

		redraw();
	}


	/** Shows the popup below the node, with the left edges aligned. */
	public void show(Node node) {
		Point2D p = node.localToScreen(0, node.getLayoutBounds().getHeight());
		show(node, p.getX(), p.getY());
	}


	private void pickSaturationBrightness(MouseEvent e) {
		s = Numeric.clamp(e.getX() / SIZE);
		b = Numeric.clamp(1 - e.getY() / SIZE);
		update();
	}


	private void pickHue(MouseEvent e) {
		h = Numeric.clamp(e.getY() / SIZE);
		update();
	}


	private void update() {
		redraw();
		onChange.at(color(h, s, b));
	}


	private void redraw() {
		square.setBackground(new Background(new BackgroundFill(UtilsFX.toFX(color(h, 1, 1)), null, null), FILL_WHITENING, FILL_DARKENING));
		squareMarker.setCenterX(s * SIZE);
		squareMarker.setCenterY((1 - b) * SIZE);
		hueMarker.setY(h * SIZE - hueMarker.getHeight() / 2);
		preview.setFill(UtilsFX.toFX(color(h, s, b)));
	}


	/** The color with the hue h (in turns), saturation s, and brightness b of its sRGB encoded components. */
	private static Color color(double h, double s, double b) {
		return Color.hsb(h, s, b).f(Color::sRGBtoLinear);
	}

}
