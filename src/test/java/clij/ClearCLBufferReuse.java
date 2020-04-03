
package clij;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;

import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

class ClearCLBufferReuse implements AutoCloseable {

	private final GpuApi gpu;

	private final Map<Shape, Deque<GpuImage>> unused = new ConcurrentHashMap<>();

	ClearCLBufferReuse(GpuApi gpu) {
		this.gpu = gpu;
	}

	public GpuImage create(long... dimensions) {
		Shape key = new Shape(dimensions);
		Deque<GpuImage> list = unused.get(key);
		if (list != null) {
			GpuImage buffer = list.pollLast();
			if (buffer != null)
				return buffer;
		}
		return gpu.create(dimensions, NativeTypeEnum.Float);
	}

	public void giveBack(GpuImage buffer) {
		Shape key = new Shape(buffer.getDimensions().clone());
		Deque<GpuImage> list = unused.computeIfAbsent(key, ignore -> new ConcurrentLinkedDeque<>());
		list.addLast(buffer);
	}

	@Override
	public void close() {
		for (Deque<GpuImage> list : unused.values()) {
			closeAll(list);
		}
	}

	private void closeAll(Deque<GpuImage> list) {
		while (true) {
			GpuImage buffer = list.pollLast();
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
