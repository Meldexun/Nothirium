package meldexun.nothirium.util;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongComparators;
import it.unimi.dsi.fastutil.longs.LongIterator;

public class FPSAnalyzer {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final DecimalFormat FORMAT = new DecimalFormat("####0.0");
	private static long[] FRAME_TIMES = new long[1000];
	private static int maxFrameCount = 10;
	private static int counter;
	private static long t;

	public static void start() {
		t = System.nanoTime();
	}

	public static void end() {
		FRAME_TIMES[counter] += System.nanoTime() - t;
	}

	public static void print() {
		if (counter % FRAME_TIMES.length == 0) {
			double avg = Arrays.stream(FRAME_TIMES).average().getAsDouble();
			LongArrayList maxes = new LongArrayList(maxFrameCount);
			Arrays.stream(FRAME_TIMES).forEach(t -> {
				if (maxes.size() < maxFrameCount) {
					maxes.add(t);
					maxes.sort(LongComparators.OPPOSITE_COMPARATOR);
				} else if (maxes.getLong(maxes.size() - 1) < t) {
					maxes.set(maxes.size() - 1, t);
					maxes.sort(LongComparators.OPPOSITE_COMPARATOR);
				}
			});
			double maxesAvg = 0;
			for (LongIterator iter = maxes.iterator(); iter.hasNext(); ) {
				maxesAvg += iter.nextLong();
			}
			maxesAvg /= maxes.size();

			StringBuilder sb = new StringBuilder();

			sb.append("avg=");
			add(sb, avg);

			sb.append("    maxesAvg=");
			add(sb, maxesAvg);

			sb.append("    maxes=[");
			sb.append(' ');
			for (long l : maxes) {
				add(sb, l);
				sb.append(' ');
			}
			sb.append(']');

			LOGGER.info(sb);
		}
		counter = (counter + 1) % FRAME_TIMES.length;
		FRAME_TIMES[counter] = 0;
	}

	private static void add(StringBuilder sb, double t) {
		String s = FORMAT.format(t / 1_000.0D);
		for (int i = s.length(); i < 7; i++) {
			sb.append(' ');
		}
		sb.append(s);
	}

}
