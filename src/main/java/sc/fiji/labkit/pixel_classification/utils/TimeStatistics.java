package sc.fiji.labkit.pixel_classification.utils;

import net.imglib2.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class can be used to measure the average runtime of a certain code
 * fragment. To do this surround the code of interest with a timer  like this:
 * <pre>
 *     {@code
 *     TimeStatistics.Timer timer = TimeStatistics.a.startTimer();
 *
 *     codeOfInterest();
 *
 *     timer.stop();
 *     }
 * </pre>
 *
 * The measurement results will be printed on the console when the program
 * finishes.
 */
public class TimeStatistics
{

	private static final List<AverageTime> times = new ArrayList<>();

	public static AverageTime a = newAverageTime("measurement a");
	public static AverageTime b = newAverageTime("measurement b");
	public static AverageTime c = newAverageTime("measurement c");
	public static AverageTime d = newAverageTime("measurement d");

	static {
		Runtime.getRuntime().addShutdownHook( new Thread(() -> {
			System.err.println("Average Times");
			for( AverageTime time : times)
				System.err.println(time);
		}) );
	}

	synchronized
	private static AverageTime newAverageTime( String title )
	{
		AverageTime n = new AverageTime( title );
		times.add( n );
		return n;
	}

	public static class AverageTime {

		private final String title;

		private final Average average = new Average();

		public AverageTime(String title) {
			this.title = title;
		}

		public Timer startTimer() {
			long start = System.nanoTime();
			return () -> average.addValue( System.nanoTime() - start );
		}

		public double getSeconds() {
			return average.getAverage() * 1e-9;
		}

		@Override
		public String toString()
		{
			return title + ": " + StopWatch.secondsToString( getSeconds() );
		}
	}

	public interface Timer
	{

		void stop();
	}

	private static class Average {

		AtomicLong nanoSeconds = new AtomicLong(0);
		AtomicLong counter = new AtomicLong(0);

		public void addValue(long value) {
			nanoSeconds.addAndGet( value );
			counter.incrementAndGet();
		}

		public double getAverage() {
			return (double) nanoSeconds.get() / (double) counter.get();
		}
	}
}
