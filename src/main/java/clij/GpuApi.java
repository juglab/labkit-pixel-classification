
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import java.util.HashMap;

public class GpuApi implements AutoCloseable {

	private final CLIJ2 clij;

	public GpuApi(CLIJ2 clij) {
		this.clij = clij;
	}

	public ClearCLBuffer create(long[] dimensions, NativeTypeEnum type) {
		return clij.create(dimensions, type);
	}

	public ClearCLBuffer push(RandomAccessibleInterval<? extends RealType> image) {
		return clij.push(image);
	}

	public RandomAccessibleInterval pullRAI(ClearCLBuffer image) {
		return clij.pullRAI(image);
	}

	@Override
	public void close() {
		clij.close();
	}

	public static GpuApi getInstance() {
		CLIJ2 clij = CLIJ2.getInstance();
		clij.setKeepReferences(false);
		return new GpuApi(clij);
	}

	public void execute(Class<?> anchorClass, String kernelFile, String kernelName,
		long[] globalSizes, long[] localSizes, HashMap<String, Object> parameters,
		HashMap<String, Object> defines)
	{
		clij.executeSubsequently(anchorClass, kernelFile, kernelName, null, globalSizes, localSizes,
			parameters, defines, null).close();
	}

	public void execute(Class<?> anchorClass, String kernelFile, String kernelName,
		long[] globalSizes, HashMap<String, Object> parameters, HashMap<String, Object> constants)
	{
		execute(anchorClass, kernelFile, kernelName, globalSizes, null, parameters, constants);
	}

	public void minimum2DBox(ClearCLBuffer inputBuffer, ClearCLBuffer tmp, long l, long l1) {
		clij.minimum2DBox(inputBuffer, tmp, l, l1);
	}

	public void minimum3DBox(ClearCLBuffer inputBuffer, ClearCLBuffer tmp, long l, long l1, long l2) {
		clij.minimum3DBox(inputBuffer, tmp, l, l1, l2);
	}

	public void maximum2DBox(ClearCLBuffer inputBuffer, ClearCLBuffer tmp, long l, long l1) {
		clij.maximum2DBox(inputBuffer, tmp, l, l1);
	}

	public void maximum3DBox(ClearCLBuffer inputBuffer, ClearCLBuffer tmp, long l, long l1, long l2) {
		clij.maximum3DBox(inputBuffer, tmp, l, l1, l2);
	}

	public void mean2DBox(ClearCLBuffer inputBuffer, ClearCLBuffer tmp, long l, long l1) {
		clij.mean2DBox(inputBuffer, tmp, l, l1);
	}

	public void mean3DBox(ClearCLBuffer inputBuffer, ClearCLBuffer tmp, long l, long l1, long l2) {
		clij.mean3DBox(inputBuffer, tmp, l, l1, l2);
	}

	@Deprecated
	public void gaussianBlur3D(ClearCLBuffer inputBuffer, ClearCLBuffer output, int i, int i1,
		int i2)
	{
		clij.gaussianBlur3D(inputBuffer, output, i, i1, i2);
	}
}
