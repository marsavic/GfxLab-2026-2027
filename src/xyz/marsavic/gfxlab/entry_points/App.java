package xyz.marsavic.gfxlab.entry_points;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import xyz.marsavic.functions.A0;
import xyz.marsavic.functions.A1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.ElementAnimationSink;
import xyz.marsavic.gfxlab.resources.Resources;
import xyz.marsavic.graph.*;
import xyz.marsavic.javafx.UtilsFX;
import xyz.marsavic.reactions.elements.Element;
import xyz.marsavic.utils.FileWatcher;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;


public class App extends Application {

	static {
		System.setProperty("prism.forceGPU", "true");
//		System.setProperty("javafx.animation.fullspeed", "true");
//		System.setProperty("javafx.animation.pulse", "60");
//		System.setProperty("prism.vsync", "true");
//		System.setProperty("quantum.multithreaded", "false");
	}
	
	/** The class whose method setupApp gives the animation. */
	private static Class<?> setupClass;
	
	Stage primaryStage;
	Pane root;
	Graph graph;
	Region info;
	TextArea textArea;
	
	static final String cssFilePath = "resources/xyz/marsavic/gfxlab/resources/mars-dark2.css";
	
	
	AnimationTimer animationTimerProfiling = new AnimationTimer() {
		long timeLast = Long.MIN_VALUE;
		boolean firstTime = true;
		
		@Override
		public void handle(long timeNow) {
			long timePassed = timeNow - timeLast;
			if (!firstTime && timePassed < 1_000_000_000 / 15) {
				return;
			}
			firstTime = false;
			timeLast = timeNow;
			
			textArea.setText(Profiling.infoTextSystem() + Profiling.infoTextProfilers());
		}
	};
	
	
	
	void addElements(Graph graph, Element e) {
		Vertex vertex = graph.createVertex(e);
		
		for (VertexInputJack inputJack : vertex.inputJacks()) {
			Element.Input<?> input = inputJack.input;
			Element.Output<?> output = input.output();
			Element eChild = output.element();
			addElements(graph, eChild);
			graph.createConnection(output, input);
		}
	}
	
	
	void initGraph(Graph graph) {
		var sink = new ElementAnimationSink(Setups.invoke(setupClass, "setupApp"));		
		addElements(graph, sink);
	}
	
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		
		primaryStage.setTitle("GFX Lab - " + setupClass.getSimpleName());
		
		graph = new Graph();
		initGraph(graph);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		info = textArea;
		
		root = new Pane(
				graph
		);
		
		graph   .prefWidthProperty ().bind(root.widthProperty ());
		graph   .prefHeightProperty().bind(root.heightProperty());
//		textArea.prefWidthProperty ().bind(root.widthProperty ());
//		textArea.prefHeightProperty().bind(root.heightProperty());
		textArea.setPrefWidth(900);
		textArea.setPrefHeight(250);
		
		info.layoutYProperty().bind(root.heightProperty().subtract(info.heightProperty()));
		
		textArea.setMouseTransparent(true);
		
		// The larger size that fits the screen, or a maximized window if neither fits (e.g. a laptop with display scaling).
		// JavaFX opens the window on the primary screen, unless the window manager places it (e.g. Sway, on the focused
		// output).
		Vector screenSize = UtilsFX.getScreenBox().d();
		Vector sceneSize = Stream.of(Vector.xy(2400, 1200), Vector.xy(1840, 1000)).filter(screenSize::dominates).findFirst().orElse(null);
		Scene scene = (sceneSize == null) ? new Scene(root) : new Scene(root, sceneSize.x(), sceneSize.y());
		primaryStage.setMaximized(sceneSize == null);
		
		
		scene.getStylesheets().setAll(Resources.stylesheetURL);
		primaryStage.getIcons().setAll(Resources.iconsApplication());

		primaryStage.setFullScreenExitHint("");
		
		primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			A0 action = switch (event.getCode()) {
				case ESCAPE -> Platform::exit;
				case F1  -> () -> graph.makeLayout(Graph.layout1);
				case F2  -> () -> graph.makeLayout(Graph.layout2);
				case F3  -> () -> graph.makeLayout(Graph.layout3);
				case F8  -> System::gc;
				case F9  -> this::toggleInfo;
				case F11 -> () -> primaryStage.setFullScreen(!primaryStage.isFullScreen());
				default -> A0.NOOP;
			};
			
			action.at();
		});

		primaryStage.setScene(scene);
		primaryStage.show();
		
		Platform.runLater(() -> {
			graph.makeLayout(Graph.layout1);
			graph.centerOnZero();
		});
		
		animationTimerProfiling.start();
		
		try {
			//noinspection resource
			FileWatcher.watchFile(Paths.get(cssFilePath), this::reloadCSS);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void reloadCSS() {
		Platform.runLater(() -> {
			primaryStage.getScene().getStylesheets().setAll("file:" + cssFilePath);
		});
	}
	
	private void toggleInfo() {
		UtilsFX.toggle(root, info);
	}
	
	
	private void makeLayout() {
		graph.makeLayout(Graph.layout1);
	}
	
	
	/** Shows the animation given by the method setupApp of the calling class. */
	public static void run() {
		run(Setups.STACK_WALKER.getCallerClass());
	}
	
	
	static void run(Class<?> c) {
		setupClass = c;
		launch(App.class);
	}
	
	
	/** Arguments: the fully qualified name of a class with the method setupApp. */
	public static void main(String[] args) throws ClassNotFoundException {
		run(Class.forName(new ParameterReader(args).next("class")));
	}
	
	
	@Override
	public void stop() {
	}	
	
}
