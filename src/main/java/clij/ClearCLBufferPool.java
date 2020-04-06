
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;

/**
 * The {@link ClearCLBufferPool} allows to easily reuse {@link ClearCLBuffer}s.
 * <p>
 * The method {@link #create} might be used as usual to get a
 * {@link ClearCLBuffer}, but the buffer might has been used before. After use a
 * buffer should be returned by calling {@link #release(ClearCLBuffer)}.
 */
class ClearCLBufferPool implements AutoCloseable {

	private final BiFunction<long[], NativeTypeEnum, ClearCLBuffer> bufferConstructor;

	private final Map<Specification, Queue<ClearCLBuffer>> unused = new ConcurrentHashMap<>();

	ClearCLBufferPool(BiFunction<long[], NativeTypeEnum, ClearCLBuffer> bufferConstructor) {
		this.bufferConstructor = bufferConstructor;
	}

	public ClearCLBuffer create(long[] dimensions, NativeTypeEnum type) {
		Specification key = new Specification(dimensions, type);
		Queue<ClearCLBuffer> list = unused.get(key);
		if (list != null) {
			ClearCLBuffer buffer = list.poll();
			if (buffer != null)
				return buffer;
		}
		return bufferConstructor.apply(dimensions, type);
	}

	public void release(ClearCLBuffer buffer) {
		Specification key = new Specification(buffer.getDimensions().clone(), buffer.getNativeType());
		Queue<ClearCLBuffer> list = unused.computeIfAbsent(key,
			ignore -> new ConcurrentLinkedQueue<>());
		list.add(buffer);
	}

	@Override
	public void close() {
		for (Queue<ClearCLBuffer> list : unused.values())
			closeAll(list);
	}

	private void closeAll(Queue<ClearCLBuffer> list) {
		while (true) {
			ClearCLBuffer buffer = list.poll();
			if (buffer == null)
				break;
			buffer.close();
		}
	}

	private static class Specification {

		private final long[] dimensions;

		private final NativeTypeEnum type;

		private Specification(long[] dimensions, NativeTypeEnum type) {
			this.type = type;
			this.dimensions = dimensions;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Specification &&
				Arrays.equals(dimensions, ((Specification) obj).dimensions) &&
				type.equals(((Specification) obj).type);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(dimensions) + 31 * type.hashCode();
		}
	}
}
