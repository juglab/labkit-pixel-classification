
package net.imglib2.trainable_segmentation.pixel_feature.filter.hessian;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.composite.RealComposite;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by arzt on 13.09.17.
 */
public class EigenValuesSymmetric3DTest {

	@Test
	public void testEigenValues() {
		RealComposite<DoubleType> in = new RealComposite<>(ArrayImgs.doubles(new double[] { 1, 2, 3, 4,
			5, 6 }, 6).randomAccess(), 6);
		float[] eigenValues = new float[3];
		RealComposite<FloatType> out = new RealComposite<>(ArrayImgs.floats(eigenValues, 3)
			.randomAccess(), 3);
		EigenValuesSymmetric3D<DoubleType, FloatType> eigenvaluesComputer =
			new EigenValuesSymmetric3D<>();
		eigenvaluesComputer.compute(in, out);
		assertArrayEquals(new float[] { 11.345f, 0.171f, -0.516f }, eigenValues, 0.001f);
	}
}
