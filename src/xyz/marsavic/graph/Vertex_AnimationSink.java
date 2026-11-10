package xyz.marsavic.graph;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.marsavic.functions.A1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Animation;
import xyz.marsavic.gfxlab.ElementAnimationSink;
import xyz.marsavic.gfxlab.UtilsGL;
import xyz.marsavic.gfxlab.resources.Resources;
import xyz.marsavic.javafx.UtilsFX;
import xyz.marsavic.reactions.Dispatcher;
import xyz.marsavic.reactions.Event;
import xyz.marsavic.reactions.Reactions;
import xyz.marsavic.reactions.values.EventInvalidated;
import xyz.marsavic.time.Profiler;
import xyz.marsavic.time.TimeProviderSystem;
import xyz.marsavic.utils.Loop;
import xyz.marsavic.utils.Utils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;


@SuppressWarnings("FieldCanBeLocal")
public class Vertex_AnimationSink extends VBox implements Vertex {
	
	public final ElementAnimationSink element;

	public final VertexHeader vertexHeader;
	public final VertexInputJack vertexInputJack;
	public final List<VertexInputJack> inputJacks;

	private final Canvas canvas;
	private final ChoiceBox<Mode> chbMode;
	private final HBox modeControls;  // The controls of the chosen mode.
	private final Spinner<Integer> spnIFrame;
	private final SpinnerValueFactory.IntegerSpinnerValueFactory iFrameValueFactory;
	private final Slider sldIFrame;
	private final Spinner<Integer> spnStep;
	private final Spinner<Integer> spnFps;
	private final Label lblIFrame;
	private final Label lblTime;
	private final Button btnCopyToClipboard;
	private final Button btnSaveImage;


	private final Loop loop;
	final A1<EventInvalidated> onInvalidated = this::invalidated;

	
	public Vertex_AnimationSink(ElementAnimationSink element) {
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
		
		animationDisplayed = element.in0.get();
		size2old = animationDisplayed.size().p12();
		canvas = new Canvas(size2old.x(), size2old.y());
		chbMode = new ChoiceBox<>();
		modeControls = new HBox();
		spnIFrame = new Spinner<>();
		sldIFrame = new Slider();
		spnStep = new Spinner<>(Integer.MIN_VALUE, Integer.MAX_VALUE, step);
		spnFps = new Spinner<>(Integer.MIN_VALUE, Integer.MAX_VALUE, fps);
		lblIFrame = new Label();
		lblTime = new Label();
		btnCopyToClipboard = new Button(null, new FontIcon(Resources.Ikons.COPY));
		btnSaveImage = new Button(null, new FontIcon(Resources.Ikons.SAVE));

		lblIFrame.getStyleClass().add("frame-index");
		lblTime.getStyleClass().add("frame-time");

		chbMode.setTooltip(new Tooltip("""
				How the frame index of the next frame is chosen.
				- Manual: the frame index is set by the spinner or the slider.
				- Fixed step: the step is added to the frame index.
				- Wall time: the frame index advances with the wall time, at the frame rate."""));
		spnIFrame.setTooltip(new Tooltip("Frame index. If the frames repeat, it is taken modulo the number of frames."));
		sldIFrame.setTooltip(new Tooltip("Frame index, from 0 to the number of frames − 1."));
		spnStep.setTooltip(new Tooltip("Step, added to the frame index for each next frame."));
		spnFps.setTooltip(new Tooltip("Frame rate, in frames per second."));
		lblIFrame.setTooltip(new Tooltip("Frame index of the displayed frame."));
		lblTime.setTooltip(new Tooltip("Time of the displayed frame, in seconds: frame index / frame rate."));
		btnCopyToClipboard.setTooltip(new Tooltip("Copy the image to the clipboard."));
		btnSaveImage.setTooltip(new Tooltip("Save the image to a file."));

		chbMode.getItems().setAll(Mode.values());
		chbMode.setValue(mode);
		chbMode.valueProperty().addListener((_, _, mode) -> showMode(mode));

		iFrameValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
		spnIFrame.setValueFactory(iFrameValueFactory);
		UtilsFX.makeEditable(spnIFrame, "-?\\d*", Integer::valueOf, 0, value -> {
			// The value is replaced by animation.iFrame, which takes it modulo the number of frames if the frames repeat.
			int iFrame = animationDisplayed.iFrame(value);
			iFrameManual = iFrame;
			iFrameValueFactory.setValue(iFrame);
		});
		sldIFrame.valueProperty().addListener((_, _, value) -> {
			if (!settingSlider) {
				iFrameValueFactory.setValue((int) Math.round(value.doubleValue()));
			}
		});
		UtilsFX.makeEditable(spnStep, "-?\\d*", Integer::valueOf, 0, value -> step = value);
		UtilsFX.makeEditable(spnFps, "-?\\d*", Integer::valueOf, 0, value -> {
			fps = value;
			showFrameInfo();
		});

		btnCopyToClipboard.setOnAction(event -> UtilsFX.copyImageToClipboard(image()));
		btnSaveImage.setOnAction(event -> UtilsFX.saveImageToFileWithDialog(image()));

		HBox controlPanel = new HBox(
				chbMode,
				modeControls,
				btnCopyToClipboard,
				btnSaveImage
//				lblInfo
		);
		modeControls.setAlignment(Pos.CENTER_LEFT);
		modeControls.setSpacing(8.0);
		HBox.setHgrow(modeControls, Priority.ALWAYS);
		HBox.setHgrow(sldIFrame, Priority.ALWAYS);
		showMode(mode);

		controlPanel.setAlignment(Pos.CENTER_LEFT);
		controlPanel.setPadding(new Insets(8.0));
		controlPanel.setSpacing(8.0);
//		controlPanel.setPrefHeight(24);
		
		// ------
		
		
		getChildren().addAll(
				hBox,
				controlPanel,
				canvas
		);
		
		inputJacks = List.of(vertexInputJack);
		
		element.in0.output().onInvalidated().add(onInvalidated); // TODO this should listen to input changed instead (because input can invalidated also if it is reconnected)

		loop = new Loop(this::loop);

		animationTimer.start();
	}

	
	
	private final Profiler profilerPulses = UtilsGL.profiler(this, "pulses");
	
	AnimationTimer animationTimer = new AnimationTimer() {
		@Override public void handle(long now) {
			profilerPulses.measure(() -> demandUpdate());
		}
	};


	
	private final Profiler profilerInvalidated = UtilsGL.profiler(this, "invalidated");

	private void invalidated(EventInvalidated e) {
		profilerInvalidated.measure(() -> {
			invalidated.set(true);
			demandUpdate();
		});
	}


	private final AtomicBoolean shouldUpdate = new AtomicBoolean(true);
	private final AtomicBoolean invalidated = new AtomicBoolean(true); // The input was invalidated since the last frame was requested.

	private void demandUpdate() {
		synchronized (shouldUpdate) {
			shouldUpdate.set(true);
			shouldUpdate.notifyAll();
		}
	}



	/** How the frame index of the next frame is chosen. */
	private enum Mode {
		MANUAL    ("Manual"),      // The frame index is given by spnIFrame (or sldIFrame).
		FIXED_STEP("Fixed step"),  // The frame index is the last one plus the step.
		WALL_TIME ("Wall time");   // The frame index advances with the wall time, at the frame rate.

		private final String label;
		Mode(String label) { this.label = label; }
		@Override public String toString() { return label; }
	}

	private void showMode(Mode mode) {
		this.mode = mode;
		modeControls.getChildren().setAll(switch (mode) {
			case MANUAL     -> List.of(spnIFrame, sldIFrame);
			case FIXED_STEP -> List.of(spnStep, lblIFrame);
			case WALL_TIME  -> List.of(spnFps, lblIFrame, lblTime);
		});
	}

	/** Shows the frame index of the displayed frame, and its time at the frame rate. */
	private void showFrameInfo() {
		lblIFrame.setText(Integer.toString(iFrameDisplayed));
		lblTime.setText(fps == 0 ? "–" : String.format("%.2f", iFrameDisplayed / (double) Math.abs(fps)));
	}

	private boolean settingSlider = false;  // Whether the code sets sldIFrame, so that its clamped value is not given to spnIFrame.


	// Mirrors of the controls, for the loop thread.
	private volatile Mode mode = Mode.FIXED_STEP;
	private volatile int iFrameManual = 0;
	private volatile int step = 1;
	private volatile int fps = 60;

	private int iFrameLast = -1;
	private double timeLast = TimeProviderSystem.INSTANCE.getTime();  // The wall time of the last update, in seconds.
	private double framesElapsed = 0;  // In wall-time mode, the wall time since frame iFrameLast began, in frames.
	private int iFrameDisplayed;  // The frame index of the displayed frame, as given by Frames.iFrame.
	private Animation animationDisplayed;  // The animation of the displayed frame. Used on the FX thread.

	private void loop() {
		Utils.waitWhile(shouldUpdate, () -> !shouldUpdate.get(), () -> shouldUpdate.set(false));
		Utils.waitWhile(updating, updating::get, this::update);
	}



	private final Profiler profilerGetFrame = UtilsGL.profiler(this, "get frame");

	private Vector size2old;

	final AtomicBoolean updating = new AtomicBoolean(false);

	private void update() {
		Animation animation = element.in0.get();
		Mode mode = this.mode;

		double time = TimeProviderSystem.INSTANCE.getTime();
		framesElapsed = (mode == Mode.WALL_TIME) ? framesElapsed + (time - timeLast) * fps : 0;
		timeLast = time;
		int framesWhole = (int) framesElapsed;  // Rounds toward zero, so with a negative frame rate the frames go back.

		int t = switch (mode) {
			case MANUAL     -> iFrameManual;
			case FIXED_STEP -> iFrameLast + step;
			case WALL_TIME  -> iFrameLast + framesWhole;
		};
		int iFrame = animation.iFrame(t);
		if (!invalidated.getAndSet(false) && iFrame == iFrameDisplayed) {
			return; // The next frame is the displayed one, and nothing changed.
		}
		iFrameLast = iFrame;
		framesElapsed -= framesWhole;
		
		updating.set(true);
		UtilsFX.submitTask(
				() -> profilerGetFrame.measure(() -> animation.at(t)),
				rMI -> {
					rMI.a(mI -> {
						Vector size = mI.size();
						if (!Objects.equals(size, size2old)) {
							size2old = size;
							canvas.setWidth(size.x());
							canvas.setHeight(size.y());
							fireResized();
						}
						UtilsFX.writeArray2ToCanvas(canvas, mI);
					});
					rMI.release();
					animationDisplayed = animation;
					// In manual mode, the spinner gives the frame index, so the new value is computed from the current
					// spinner value, not from t, which can be older. animation.iFrame changes it only if the animation
					// changed.
					int value = (mode == Mode.MANUAL) ? animation.iFrame(iFrameValueFactory.getValue()) : iFrame;
					iFrameValueFactory.setValue(value);
					// The slider covers [0, number of frames) also when the frames do not repeat. A frame index outside
					// that range is shown clamped by the slider.
					settingSlider = true;
					sldIFrame.setMax(animation.size().x() - 1);
					sldIFrame.setValue(value);
					settingSlider = false;
					// The next frame is requested only after this one is drawn, so frames don't pile up when the FX thread is behind.
					synchronized (updating) {
						iFrameDisplayed = iFrame;
						updating.set(false);
						updating.notifyAll();
					}
					showFrameInfo();
				}
		);
	}


	private Image image() {
		return canvas.snapshot(null, null);
	}
	
	
	@Override public ElementAnimationSink element() { return element; }	
	@Override public Collection<VertexInputJack> inputJacks () { return inputJacks ; }
	@Override public Collection<VertexOutputJack> outputJacks() { return List.of(); }
	@Override public Region region() { return this; }
	
	
	
	public static class EventResized implements Event {}
	
	public final Dispatcher<EventResized> dispatcherResized = new Dispatcher<>();
	public Reactions<EventResized> onResized() { return dispatcherResized.reactions(); }
	public void fireResized() { dispatcherResized.fireAsync(new EventResized(), UtilsGL.parallelReactions); }
	
}
