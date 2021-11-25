
package net.imglib2.trainable_segmentation.gpu.api;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;

import java.util.function.Consumer;

public class GpuImage implements AutoCloseable {

	private ClearCLBuffer clearClBuffer;

	private final Consumer<ClearCLBuffer> onClose;

	public GpuImage(ClearCLBuffer clearClBuffer, Consumer<ClearCLBuffer> onClose) {
		this.clearClBuffer = clearClBuffer;
		this.onClose = onClose;
	}

	public long[] getDimensions() {
		return clearClBuffer.getDimensions();
	}

	public long getWidth() {
		return clearClBuffer.getWidth();
	}

	public long getHeight() {
		return clearClBuffer.getHeight();
	}

	public long getDepth() {
		return clearClBuffer.getDepth();
	}

	public ClearCLBuffer clearCLBuffer() {
		return clearClBuffer;
	}

	@Override
	public void close() {
		if (clearClBuffer == null)
			return;
		ClearCLBuffer buffer = clearClBuffer;
		clearClBuffer = null;
		onClose.accept(buffer);
	}

	public NativeTypeEnum getNativeType() {
		return clearClBuffer.getNativeType();
	}

	public long getNumberOfChannels() {
		return clearClBuffer.getNumberOfChannels();
	}
}
