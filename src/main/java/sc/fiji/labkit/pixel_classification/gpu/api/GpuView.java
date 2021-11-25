
package sc.fiji.labkit.pixel_classification.gpu.api;

import net.imglib2.Dimensions;

/**
 * A {@link GpuView} as a crop of a {@link GpuImage}.
 * <p>
 * A {@link GpuView} can also be a hyperSlice of a {@link GpuImage}, this is the
 * case if
 * {@code gpuView.dimensions().numDimensions() < gpuView.source().getDimensions().length}.
 * <p>
 *
 * @see GpuViews
 */
public class GpuView implements AutoCloseable {

	private final GpuImage source;
	private final Dimensions dimensions;
	private final long offset;

	GpuView(GpuImage source, Dimensions dimensions, long offset) {
		this.source = source;
		this.dimensions = dimensions;
		this.offset = offset;
	}

	/**
	 * Dimensions of the view.
	 */
	public Dimensions dimensions() {
		return dimensions;
	}

	/**
	 * @return underlying {@link GpuImage}. This should only be used by low level
	 *         functions.
	 */
	public GpuImage source() {
		return source;
	}

	/**
	 * @return The index of the pixel, in the underlying {@link GpuImage}, that is
	 *         considered as the origin of the image.
	 */
	public long offset() {
		return offset;
	}

	/**
	 * Closes the underlying {@link GpuImage}.
	 */
	@Override
	public void close() {
		source.close();
	}
}
