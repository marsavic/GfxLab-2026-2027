package xyz.marsavic.gfxlab.entry_points;

import xyz.marsavic.gfxlab.UtilsGL;
import xyz.marsavic.time.Profiler;
import xyz.marsavic.time.Timer;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;


public class Profiling {

	private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
	
	private static final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
	private static final int nCpus = operatingSystemMXBean.getAvailableProcessors();
	
	private static final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	private static ObjectName name;
	static {
		try {
			name = ObjectName.getInstance("java.lang:type=OperatingSystem");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static double lastValue = Double.NaN;
	private static double nextTime = 0.0;
	private static final double cpuQueryInterval = 1.0;
	
	
	
	private static double getProcessCpuLoad() {
		double time = Profiler.timerDefault.getTime();
		if (time < nextTime) {
			return lastValue;
		}
		nextTime = time + cpuQueryInterval;
		try {
			Attribute attribute = (Attribute) mbs.getAttributes(name, new String[] {"ProcessCpuLoad"}).getFirst();
			return lastValue = (Double) attribute.getValue();
		} catch (InstanceNotFoundException | ReflectionException e) {
			e.printStackTrace();
			return Double.NaN;
		}
	}
	
	static Timer timer = new Timer();
	
	public static String infoTextSystem() {
		StringBuilder sb = new StringBuilder();

		MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
	
		double conversion = 1024*1024;
		String unit = "MB";
		sb.append(String.format("Heap used       : %8.2f %s\n", heapMemoryUsage.getUsed     () / conversion, unit));
		sb.append(String.format("Heap max        : %8.2f %s\n", heapMemoryUsage.getMax      () / conversion, unit));
		sb.append(String.format("Memory committed: %8.2f %s\n", heapMemoryUsage.getCommitted() / conversion, unit));
		
		sb.append(String.format("Parallelism     : %d/%d\n", UtilsGL.parallelism, nCpus));
		sb.append(String.format("CPU load average: %.2f\n", getProcessCpuLoad()));
		
		sb.append(String.format("Time elapsed    : %8.2f\n", timer.getTime()));

		sb.append("\n");
		
		return sb.toString();
	}
	
	
	public static String infoTextProfilers() {
		StringBuilder sb = new StringBuilder();
		
/*
		// Profilers change, can't sort them while running, so...
		UtilsGL.profilers().stream()
				.map(p -> new T2<>(p, p.durationPerSecond()))
				.sorted(Comparator.comparingDouble(t -> -t.p1()))
				.forEach(t -> {
					sb.append(t.p0().toStringAggregateName());
					sb.append("\n");
				});
*/

		UtilsGL.profilers()
				.forEach(p -> {
					sb.append(p.toStringAggregateName());
					sb.append("\n");
				});
		
		sb.append("\n");
		
		return sb.toString();
	}
	
}
