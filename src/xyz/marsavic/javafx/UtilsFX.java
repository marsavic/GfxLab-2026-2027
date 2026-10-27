package xyz.marsavic.javafx;

import xyz.marsavic.gfxlab.arrays.*;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import xyz.marsavic.functions.A1;
import xyz.marsavic.functions.F0;
import xyz.marsavic.geometry.Box;
import xyz.marsavic.geometry.Interval;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.*;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.utils.ListUtils;
import xyz.marsavic.utils.Utils;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.IntBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class UtilsFX {
	
	private static class DaemonThreadFactory implements ThreadFactory {
		// TODO move to Utils
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		
		DaemonThreadFactory() {
			group = Thread.currentThread().getThreadGroup();
			namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
		}
		
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			t.setDaemon(true);
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}
	}
	
	/** To be used with tasks invoked from the JavaFX thread. */
	public static ExecutorService executor =
			Executors.newFixedThreadPool(
					Runtime.getRuntime().availableProcessors(),
					new DaemonThreadFactory()
			);
	
	
	public static <R> void submitTask(F0<R> f, A1<R> onFxThread) {
		Task<R> task = new Task<>() {
			@Override
			protected R call() {
				return Utils.trace(f);
			}
		};
		task.setOnSucceeded(e -> Utils.trace(() -> onFxThread.at(task.getValue())));
		UtilsFX.executor.submit(task);
	}
	
	
	public static void toggle(Pane parent, Node child) {
		ListUtils.toggle(parent.getChildren(), child);
	}
	
	
	// Converters mars-bits <=> JavaFx ==================================================

	public static Vector imageSize(Image image) {
		return Vector.xy(image.getWidth(), image.getHeight());
	}
	
	
	public static Vector toVector(Point2D p) {
		return Vector.xy(p.getX(), p.getY());
	}
	
/*
	
	public static Vector position(Node node) {
		return layoutP(node).add(translate(node));
	}
	
*/
/*
	public static Vector layoutP(Node node) {
		return Vector.xy(node.getLayoutX(), node.getLayoutY());
	}
*/
	
	public static Vector getTranslate(Node node) {
		return Vector.xy(node.getTranslateX(), node.getTranslateY());
	}
	

	public static void setLayoutP(Node node, Vector p) {
		node.setLayoutX(p.x());
		node.setLayoutY(p.y());
	}
	
	public static void setTranslate(Node node, Vector t) {
		node.setTranslateX(t.x());
		node.setTranslateY(t.y());
	}
	
	
/*
	public static Vector layoutD(Node node) {
		return Vector.xy(node.getLayoutBounds().getWidth(), node.getLayoutBounds().getHeight());
	}
	
	public static Box layoutBox(Region region) {
		return Box.pd(position(region), layoutD(region));
	}
*/
	
	public static Box getScreenBox() {
		return box(Screen.getPrimary().getBounds());
	}
	
	
	public static void setPrefD(Region region, Vector d) {
		region.setPrefSize(d.x(), d.y());
	}
	

	public static Box box(Bounds bounds) {
		return
				Box.xy(
						Interval.pd(bounds.getMinX(), bounds.getWidth ()),
						Interval.pd(bounds.getMinY(), bounds.getHeight())
				);
		
	}
	
	
	public static Box box(Rectangle2D bounds) {
		return
				Box.xy(
						Interval.pd(bounds.getMinX(), bounds.getWidth ()),
						Interval.pd(bounds.getMinY(), bounds.getHeight())
				);
	}

	public static Box box(Node node) {
		return UtilsFX.box(node.getBoundsInParent());
	}
	
	
	public static Point2D toPoint2D(Vector p) {
		return new Point2D(p.x(), p.y());
	}
	
	
	// Image utils ======================================================================
	
	
	public static void copyImageToClipboard(Image image) {
		ClipboardContent content = new ClipboardContent();
		content.putImage(image);
		Clipboard.getSystemClipboard().setContent(content);
	}
	
	
	public static WritableImage createWritableImage(Vector size) {
		return new WritableImage(size.xInt(), size.yInt());
	}
	

	public static void saveImageToFileWithDialog(Image image) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export Image");
		fileChooser.setInitialDirectory(new File("."));
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Image Files", "*.png"),
				new FileChooser.ExtensionFilter("All Files", "*.*")
		);
		File file = fileChooser.showSaveDialog(null);
		
		if (file != null) {
			try {
				ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	
	static final PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
	
	public static void writeArray2ToImage(WritableImage outImage, Array2<Integer> inArray2) {
		int sx = inArray2.size().xInt();
		int sy = inArray2.size().yInt();
		
		if (inArray2 instanceof MatrixInts m) {
			outImage.getPixelWriter().setPixels(0, 0, sx, sy, pixelFormat, m.array(), 0, sx);
		} else {
			UtilsGL.matricesInt.obtain(inArray2.size(), true, m -> {
				m.copyFrom(inArray2);
				outImage.getPixelWriter().setPixels(0, 0, sx, sy, pixelFormat, ((MatrixInts) m).array(), 0, sx);
			});
		}
	}
	
	
	public static Image writeArray2ToImage(Array2<Integer> inArray2) {
		WritableImage image = createWritableImage(inArray2.size());
		writeArray2ToImage(image, inArray2);
		return image;
	}
	
	
	public static void writeArray2ToCanvas(Canvas canvas, Array2<Integer> inArray2) {
		int sx = inArray2.size().xInt();
		int sy = inArray2.size().yInt();
		
		if (inArray2 instanceof MatrixInts m) {
			canvas.getGraphicsContext2D().getPixelWriter().setPixels(0, 0, sx, sy, pixelFormat, m.array(), 0, sx);
		} else {
			UtilsGL.matricesInt.obtain(inArray2.size(), true, m -> {
				m.copyFrom(inArray2);
				canvas.getGraphicsContext2D().getPixelWriter().setPixels(0, 0, sx, sy, pixelFormat, ((MatrixInts) m).array(), 0, sx);
			});
		}
	}
	
	
	
	public static MatrixColor matrixFromImage(Image img) {
		Vector size = UtilsFX.imageSize(img);
		MatrixColor matrixColor = new MatrixColor(size);
		
		int[] buffer = new int[size.areaInt()];
		
		img.getPixelReader().getPixels(
				0, 0,
				size.xInt(), size.yInt(),
				WritablePixelFormat.getIntArgbPreInstance(),
				buffer,
				0, size.xInt()
		);
		
		UtilsGL.parallel.parallel(size.yInt(), y -> {
			for (int x = 0; x < size.xInt(); x++) {
				int k = y * size.xInt() + x;
				Color c = Color.code(buffer[k]);
				matrixColor.set(x, y, c);
			}
		});
		
		return matrixColor;
	}

	
	public static MatrixColor matrixFromImage(InputStream is) {
		return matrixFromImage(new Image(is));
	}
	
	
	public static MatrixColor matrixFromImage(String fileName) {
		try {
			return matrixFromImage(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static Image generateImage(ElementF<Animation> animation, int iFrame) {
		var rMI = animation.result().at(iFrame);
		Image image = rMI.f(UtilsFX::writeArray2ToImage);
		rMI.release();
		return image;
	}
	
}
