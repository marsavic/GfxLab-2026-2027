package xyz.marsavic.graph;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.marsavic.functions.A1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.ElementAnimationSink;
import xyz.marsavic.gfxlab.UtilsGL;
import xyz.marsavic.gfxlab.resources.Resources;
import xyz.marsavic.javafx.UtilsFX;
import xyz.marsavic.reactions.Dispatcher;
import xyz.marsavic.reactions.Event;
import xyz.marsavic.reactions.Reactions;
import xyz.marsavic.reactions.values.EventInvalidated;
import xyz.marsavic.resources.BorrowManagerMap;
import xyz.marsavic.resources.Rr;
import xyz.marsavic.time.Profiler;
import xyz.marsavic.utils.Loop;
import xyz.marsavic.utils.Utils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;


@SuppressWarnings("FieldCanBeLocal")
public class Vertex_AnimationSink_Image extends VBox implements Vertex {
	
	public final ElementAnimationSink element;

	public final VertexHeader vertexHeader;
	public final VertexInputJack vertexInputJack;
	public final List<VertexInputJack> inputJacks;

	private final ImageView imageView;
	private final CheckBox chbEnabled;
	private final Spinner<Integer> spnIFrame;
	private final Button btnCopyToClipboard;
	private final Button btnSaveImage;
	private final Label lblInfo;	

	
	private final Loop loop;
	final A1<EventInvalidated> onInvalidated = this::invalidated;

	
	public Vertex_AnimationSink_Image(ElementAnimationSink element) {
		this.element = element;
	
		getStyleClass().add("vertex");
		getStyleClass().add("vertex-animation-sink");
		
		vertexHeader = new VertexHeader(element);
		vertexInputJack = new VertexInputJack(element.in0);
		
		HBox.setHgrow(vertexHeader, Priority.ALWAYS);
		HBox hBox = new HBox(
				vertexInputJack,
				vertexHeader
		);
		hBox.setAlignment(Pos.CENTER_LEFT);
		

		// ------
		
		imageView = new ImageView();
		chbEnabled = new CheckBox();
		chbEnabled.setSelected(true);
		spnIFrame = new Spinner<>();
		btnCopyToClipboard = new Button(null, new FontIcon(Resources.Ikons.COPY));
		btnSaveImage = new Button(null, new FontIcon(Resources.Ikons.SAVE));
		Pane separator = new Pane();
		lblInfo = new Label();
		
		spnIFrame.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE));
		spnIFrame.getValueFactory().setValue(-1);
		
		btnCopyToClipboard.setOnAction(event -> UtilsFX.copyImageToClipboard(image()));
		btnSaveImage.setOnAction(event -> UtilsFX.saveImageToFileWithDialog(image()));
		
		HBox controlPanel = new HBox(
				chbEnabled,
				spnIFrame,
				btnCopyToClipboard,
				btnSaveImage,
				separator,
				lblInfo
		);
		HBox.setHgrow(separator, Priority.ALWAYS);

		controlPanel.setAlignment(Pos.CENTER_LEFT);
		controlPanel.setPadding(new Insets(8.0));
		controlPanel.setSpacing(8.0);
//		controlPanel.setPrefHeight(24);
		
		// ------
		
		
		getChildren().addAll(
				hBox,
				controlPanel,
				imageView
		);
		
		inputJacks = List.of(vertexInputJack);
		
		element.in0.output().onInvalidated().add(onInvalidated); // TODO this should listen to input changed instead (because input can invalidated also if it is reconnected)
		
		loop = new Loop(this::loop);
//		OnGC.setOnGC(this, () -> loop.demand(Loop.State.STOPPED));
		
		animationTimer.start();
	}

	
	
	private final Profiler profilerTimer = UtilsGL.profiler(this, "timer");
	
	AnimationTimer animationTimer = new AnimationTimer() {
		@Override public void handle(long now) {
			profilerTimer.measure(() -> demandUpdate());
		}
	};
	
	private void invalidated(EventInvalidated e) {
		demandUpdate();
	}
	
	
	private final AtomicBoolean shouldUpdate = new AtomicBoolean(true);
	
	private void demandUpdate() {
//		System.out.println("demandUpdate");
		synchronized (shouldUpdate) {
//			System.out.println("demandUpdate synchronized");
			shouldUpdate.set(true);
			shouldUpdate.notifyAll();
		}
	}
	

	
	private int iFrameNext = 0;
	
	private void loop() {
//		System.out.println("loop 0");
		Utils.waitWhile(shouldUpdate, () -> !shouldUpdate.get(), () -> shouldUpdate.set(false));
//		System.out.println("loop 1");
		Utils.waitWhile(updating, updating::get, this::update);
//		System.out.println("loop 2");
	}

	

	private final Profiler profilerGetFrame = UtilsGL.profiler(this, "get frame");
	private final BorrowManagerMap<WritableImage, Vector> images = new BorrowManagerMap<>(UtilsFX::createWritableImage, null);
	
	private Vector size2old = Vector.ZERO;
	
//	private final FB_O<Profiler> profilerSelector = p ->  p.name().matches(".*add sample.*");
//	private final Comparator<Profiler> profilerComparator = Comparator.comparing(Profiler::lastEventAgo);
	
	final AtomicBoolean updating = new AtomicBoolean(false);
	
	private void update() {
		updating.set(true);
		UtilsFX.submitTask(
				() -> {
//						System.out.println("get frame start");
						var rMI = profilerGetFrame.measure(() -> element.in0.get().at(iFrameNext++));
//						System.out.println("get frame end");
						synchronized (updating) {
							updating.set(false);
							updating.notifyAll();
						}
						return rMI;
				},
				rMI -> {
					rMI.a(mI -> {
						Rr<WritableImage> rImage = images.obtain(mI.size(), true);
						rImage.a(image -> UtilsFX.writeArray2ToImage(image, mI));
						rImage.a(imageView::setImage);
						rImage.release();
					});
					rMI.release();
//					lblInfo.setText(String.format("%.1f", profilerGetFrame.eventsPerSecond()));
					Vector size2new = UtilsFX.imageSize(imageView.getImage());
					if (!Objects.equals(size2new, size2old)) {
						size2old = size2new;
						fireResized();
					}
				}
		);
	}


	private Image image() {
		return imageView.getImage();
	}
	
	
	@Override public ElementAnimationSink element() { return element; }	
	@Override public Collection<VertexInputJack> inputJacks () { return inputJacks ; }
	@Override public Collection<VertexOutputJack> outputJacks() { return List.of(); }
	@Override public Region region() { return this; }
	
	
	
	public static class EventResized implements Event {}
	
	public final Dispatcher<EventResized> dispatcherResized = new Dispatcher<>();
	public Reactions<EventResized> onResized() { return dispatcherResized.reactions(); }
	public void fireResized() { dispatcherResized.fireAsync(new EventResized(), UtilsGL.parallelReactions.executorService()); }
	
}
