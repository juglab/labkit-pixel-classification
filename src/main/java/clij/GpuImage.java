
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;

import java.nio.Buffer;

public class GpuImage implements AutoCloseable {

	private final ClearCLBuffer clearClBuffer;

	public GpuImage(ClearCLBuffer clearClBuffer) {
		this.clearClBuffer = clearClBuffer;
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
		clearClBuffer.close();
	}

	public NativeTypeEnum getNativeType() {
		return clearClBuffer.getNativeType();
	}

	public void writeTo(Buffer buffer, boolean blocking) {
		clearClBuffer.writeTo(buffer, blocking);
	}
}
