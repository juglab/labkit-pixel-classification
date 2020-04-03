
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;

import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

class ClearCLBufferReuse implements AutoCloseable {

	private final CLIJ2 clij;

	private final Map<Shape, Deque<ClearCLBuffer>> unused = new ConcurrentHashMap<>();

	ClearCLBufferReuse(CLIJ2 clij) {
		this.clij = clij;
	}

	public ClearCLBuffer create(long... dimensions) {
		Shape key = new Shape(dimensions);
		Deque<ClearCLBuffer> list = unused.get(key);
		if (list != null) {
			ClearCLBuffer buffer = list.pollLast();
			if (buffer != null)
				return buffer;
		}
		return clij.create(dimensions);
	}

	public void giveBack(ClearCLBuffer buffer) {
		Shape key = new Shape(buffer.getDimensions().clone());
		Deque<ClearCLBuffer> list = unused.computeIfAbsent(key,
			ignore -> new ConcurrentLinkedDeque<>());
		list.addLast(buffer);
	}

	@Override
	public void close() {
		for (Deque<ClearCLBuffer> list : unused.values()) {
			closeAll(list);
		}
	}

	private void closeAll(Deque<ClearCLBuffer> list) {
		while (true) {
			ClearCLBuffer buffer = list.pollLast();
			if (buffer == null)
				break;
			buffer.close();
		}
	}

	private static class Shape {

		private final long[] shap;

		private Shape(long... shape) {
			this.shap = shape;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Shape && Arrays.equals(shap, ((Shape) obj).shap);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(shap);
		}
	}
}
