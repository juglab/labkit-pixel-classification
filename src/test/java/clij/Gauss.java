
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLKernel;
import net.haesleinhuepf.clij.clearcl.util.CLKernelExecutor;
import net.haesleinhuepf.clij2.CLIJ2;

import java.util.HashMap;

public class Gauss {

	private static CLKernelExecutor executor;

	public static void convolve(CLIJ2 clij, ClearCLBuffer inputBuffer, ClearCLBuffer kernelBuffer,
		ClearCLBuffer outputBuffer)
	{
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("input", inputBuffer);
		parameters.put("kernelValues", kernelBuffer);
		parameters.put("output", outputBuffer);
		HashMap<String, Object> defines = new HashMap<>();
		long[] localSizes = { outputBuffer.getWidth(), 1, 1 };
		defines.put("KERNEL_LENGTH", kernelBuffer.getWidth());
		defines.put("BLOCK_SIZE", localSizes[0]);
		defines.put("INPUT_X_SKIP", 1L);
		defines.put("INPUT_Y_SKIP", inputBuffer.getWidth());
		defines.put("INPUT_Z_SKIP", inputBuffer.getWidth() * inputBuffer.getHeight());
		defines.put("INPUT_OFFSET", 0L);
		defines.put("OUTPUT_X_SKIP", 1L);
		defines.put("OUTPUT_Y_SKIP", outputBuffer.getWidth());
		defines.put("OUTPUT_Z_SKIP", outputBuffer.getWidth() * outputBuffer.getHeight());
		defines.put("OUTPUT_OFFSET", 0L);

		if (executor == null)
			executor = new CLKernelExecutor(clij.getCLIJ().getClearCLContext());
		executor.setProgramFilename("gauss.cl");
		executor.setKernelName("convolve1d");
		executor.setAnchorClass(Gauss.class);
		executor.setParameterMap(parameters);
		executor.setConstantsMap(defines);
		executor.setGlobalSizes(outputBuffer.getDimensions());
		executor.setImageSizeIndependentCompilation(true);
		executor.setLocalSizes(localSizes);

		ClearCLKernel kernel = executor.enqueue(false, null);
		kernel.close();
	}
}
