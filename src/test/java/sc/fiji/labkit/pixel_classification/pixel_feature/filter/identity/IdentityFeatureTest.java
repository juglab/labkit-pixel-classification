
package sc.fiji.labkit.pixel_classification.pixel_feature.filter.identity;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.SingleFeatures;
import sc.fiji.labkit.pixel_classification.utils.CpuGpuRunner;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests {@link IdentityFeature}.
 */
@RunWith(CpuGpuRunner.class)
public class IdentityFeatureTest {

	public IdentityFeatureTest(boolean useGpu) {
		this.calculator.setUseGpu(useGpu);
	}

	private final FeatureCalculator calculator = FeatureCalculator.default2d()
		.addFeature(SingleFeatures.identity())
		.build();

	@Test
	public void test() {
		Img<FloatType> image = ArrayImgs.floats(new float[] { 1, 2, 3, 4 }, 2, 2);
		RandomAccessibleInterval<FloatType> result = calculator.apply(image);
		ImgLib2Assert.assertImageEquals(image, Views.hyperSlice(result, 2, 0));
	}
}
