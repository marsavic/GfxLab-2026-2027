package xyz.marsavic.gfxlab.entry_points;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Animation;
import xyz.marsavic.gfxlab.arrays.Array2;
import xyz.marsavic.gfxlab.arrays.Matrix;
import xyz.marsavic.javafx.UtilsFX;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.resources.Rr;
import xyz.marsavic.statistics.StatisticsCollector;
import xyz.marsavic.testing.Benchmarking;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Benchmark {
	
/*	 
	When benchmarking:
	Program:	
		- AggregatorFixed.constructor(N), where N is the number of iterations.
		- parallelism = 3/4 cores
	Java VM:
		- -XX:ActiveProcessorCount=14        (n-2)
		- 		
	OS and software:
		- Close browsers and other programs
		- taskset -c 0-13 java ...           (n-2)		
	Hardware:
		- Set cpu power and thermal profiles to ensure steady performance without throttling
			- Powersave (quiet) mode
		- Battery only (charging produces heat, turns on and off)
	...
	*/
	
	static Benchmarking.Result benchmark(ElementF<Animation> animation, String description, boolean saveToFile) throws IOException {
		Image[] image = new Image[1];
		
		var result = Benchmarking.run(1, () -> {
			Rr<Matrix<Integer>> rMI = animation.result().at(0);
			Vector size = rMI.f(Array2::size);
			
			if (saveToFile) {
				image[0] = rMI.f(UtilsFX::writeArray2ToImage);
			}
			
			rMI.release();
			return size;
		});
		
		if (saveToFile) {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"));
			String directoryPath = "renders/benchmarks";
			Files.createDirectories(Paths.get(directoryPath));
			String fileName = String.format("%s/%s %7.2f %s.png", directoryPath, timestamp, result.stats().min(), description);
			ImageIO.write(SwingFXUtils.fromFXImage(image[0], null), "png", new File(fileName));		
		}
		
		return result;
	}


	/** Benchmarks the animation given by the method setupBenchmark of the calling class. */
	public static void run() {
		run(Setups.STACK_WALKER.getCallerClass());
	}
	
	
	static void run(Class<?> c) {
		var animation = Setups.invoke(c, "setupBenchmark");
		var desc = c.getSimpleName();
		
		try {
			benchmark(animation, desc, true);
			
			StatisticsCollector sc = new StatisticsCollector();
			for (int i = 0; i < 16; i++) {
				var result = benchmark(animation, desc, false);
				System.out.printf("%3d %7.2f\n", i, result.stats().sum());
				sc.add(result.stats().sum());
			}
			System.out.println();
			System.out.println(sc.get());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	
	/** Arguments: the fully qualified name of a class with the method setupBenchmark. */
	public static void main(String[] args) throws ClassNotFoundException {
		run(Class.forName(new ParameterReader(args).next("class")));
	}
	
}
