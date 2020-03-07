package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;

import java.util.HashMap;

public class Gauss {

	public static void convolve(CLIJ2 clij, ClearCLBuffer inputBuffer, ClearCLBuffer kernelBuffer, ClearCLBuffer outputBuffer) {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("input", inputBuffer);
		parameters.put("kernelValues", kernelBuffer);
		parameters.put("output", outputBuffer);
		parameters.put("skip", 1);
		clij.execute(Gauss.class, "gauss.cl", "gauss", outputBuffer.getDimensions(), outputBuffer.getDimensions(), parameters);
	}
}
