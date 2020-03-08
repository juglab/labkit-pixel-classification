package clij;

import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLKernel;
import net.haesleinhuepf.clij.clearcl.util.CLKernelExecutor;
import net.haesleinhuepf.clij.clearcl.util.ElapsedTime;
import net.haesleinhuepf.clij2.CLIJ2;

import java.util.HashMap;

public class Gauss {

	private static CLKernelExecutor executor;

	public static void convolve(CLIJ2 clij, ClearCLBuffer inputBuffer, ClearCLBuffer kernelBuffer, ClearCLBuffer outputBuffer) {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("input", inputBuffer);
		parameters.put("kernelValues", kernelBuffer);
		parameters.put("output", outputBuffer);
		parameters.put("skip", 1);
		HashMap<String, Object> defines = new HashMap<>();
		long[] localSizes = {outputBuffer.getWidth(), 1, 1};
		defines.put("KERNEL_LENGTH", kernelBuffer.getWidth());
		defines.put("BLOCK_SIZE", localSizes[0]);

		if(executor == null)
			executor = new CLKernelExecutor(clij.getCLIJ().getClearCLContext());
		executor.setProgramFilename("gauss.cl");
		executor.setKernelName("gauss");
		executor.setAnchorClass(Gauss.class);
		executor.setParameterMap(parameters);
		executor.setConstantsMap(defines);
		executor.setGlobalSizes(outputBuffer.getDimensions());
		executor.setImageSizeIndependentCompilation(false);
		executor.setLocalSizes(localSizes);

		ClearCLKernel kernel = executor.enqueue(false, null);
		kernel.close();
	}
}
