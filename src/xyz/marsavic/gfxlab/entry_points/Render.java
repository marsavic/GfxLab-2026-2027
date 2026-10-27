package xyz.marsavic.gfxlab.entry_points;

import javafx.embed.swing.SwingFXUtils;
import xyz.marsavic.gfxlab.Animation;
import xyz.marsavic.javafx.UtilsFX;
import xyz.marsavic.reactions.elements.ElementF;
import xyz.marsavic.statistics.StatisticsWeightDecayedWithAmount;
import xyz.marsavic.time.Timer;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Render {
		
	static void render(ElementF<Animation> animation, String name, int pFrame, int qFrame) throws IOException {
		String directoryPath = "renders/animation/" + name;
		Files.createDirectories(Paths.get(directoryPath));

		Timer timer = new Timer();
		
		StatisticsWeightDecayedWithAmount statistics = new StatisticsWeightDecayedWithAmount(1.0);
		
		for (int iFrame = pFrame; iFrame < qFrame; iFrame++) {
			String fileName = String.format("%s/%08d.png", directoryPath, iFrame);
			
			double time = -timer.getTime();
			var image = UtilsFX.generateImage(animation, iFrame);
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", new File(fileName));
			time += timer.getTime();
			statistics.add(time);
			
			int timeRemaining = (int) (statistics.value() * (qFrame - iFrame - 1));
			
			int timeRemainingS = (timeRemaining) % 60;
			int timeRemainingM = ((timeRemaining) / 60) % 60;
			int timeRemainingH = (((timeRemaining) / 60) / 60);
			
			System.out.format("%08d  %7.2f  %02d:%02d:%02d\n", iFrame, time, timeRemainingH, timeRemainingM, timeRemainingS);
		}
	}
	
	
	/** Renders the animation given by the method setupRender of the calling class. */
	public static void run() {
		run(Setups.STACK_WALKER.getCallerClass(), new ParameterReader(new String[0]));
	}
	
	
	static void run(Class<?> c, ParameterReader parameterReader) {
		int pFrame = Integer.parseInt(parameterReader.next("pFrame"));
		int qFrame = Integer.parseInt(parameterReader.next("qFrame"));
		
		System.out.println(Profiling.infoTextSystem());
		System.out.println();
		try {
			render(Setups.invoke(c, "setupRender"), c.getSimpleName(), pFrame, qFrame);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	
	/** Arguments: the fully qualified name of a class with the method setupRender, pFrame, qFrame. */
	public static void main(String[] args) throws ClassNotFoundException {
		ParameterReader parameterReader = new ParameterReader(args);
		run(Class.forName(parameterReader.next("class")), parameterReader);
	}
	
}
