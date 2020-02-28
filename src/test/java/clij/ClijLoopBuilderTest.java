
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.util.HashMap;

public class ClijLoopBuilderTest {

	private final CLIJ2 clij = CLIJ2.getInstance();

	@Test
	public void test() {
		ClearCLBuffer a = clij.push(ArrayImgs.floats(new float[] { 1, 2, 3, 4 }, 2, 2));
		ClearCLBuffer b = clij.push(ArrayImgs.floats(new float[] { 5, 6, 7, 8 }, 2, 2));
		ClearCLBuffer c = clij.create(a);
		binaryOp(a, b, c, "c = a + b");
		RandomAccessibleInterval<? extends RealType<?>> result = clij.pullRAI(c);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 6, 8, 10, 12 }, 2,
			2);
		ImgLib2Assert.assertImageEqualsRealType(result, expected, 0.0);
	}

	private void binaryOp(ClearCLBuffer a, ClearCLBuffer b, ClearCLBuffer c, String s) {
		HashMap<String, Object> paramters = new HashMap<>();
		HashMap<String, Object> defines = new HashMap<>();
		long[] dims = a.getDimensions();
		paramters.put("a", a);
		paramters.put("b", b);
		paramters.put("c", c);
		defines.put("OPERATION(a,b,c)", s);
		clij.execute(ClijLoopBuilderTest.class, "binary_operation.cl", "binary_operation",
			dims, dims, paramters, defines);
	}
}
