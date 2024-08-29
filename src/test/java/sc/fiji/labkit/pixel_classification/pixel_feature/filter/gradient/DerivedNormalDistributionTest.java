/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.gradient;

import org.junit.Ignore;
import org.junit.Test;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;

import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthias Arzt
 */
@Deprecated
public class DerivedNormalDistributionTest {

	@Test
	public void testDerivedNormalDistribution() {
		DoubleUnaryOperator f = DerivedNormalDistribution.derivedNormalDistribution(5);
		assertEquals(0.9718373972, f.applyAsDouble(2.0), 0.00001);
		assertEquals(-1.4518243471, f.applyAsDouble(1.0), 0.00001);
		assertEquals(0.0, f.applyAsDouble(0.0), 0.00001);
	}

	@Ignore("DerivedGaussKernel returns bad values for small sigma")
	@Test
	public void testDerivedGaussKernel() {
		Kernel1D kernel = DerivedNormalDistribution.derivedGaussKernel(0.3, 0);
		double sum = DoubleStream.of(kernel.fullKernel()).sum();
		assertEquals(1.0, sum, 0.0000001);
	}

}
