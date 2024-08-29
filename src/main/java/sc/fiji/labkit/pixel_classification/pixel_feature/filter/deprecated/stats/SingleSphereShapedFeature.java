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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.stats;

import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import preview.net.imglib2.algorithm.neighborhood.HyperEllipsoidShape;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureInput;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Arzt
 */
@Deprecated
@Plugin(type = FeatureOp.class, label = "Min/Max/Mean/Median/Variance")
public class SingleSphereShapedFeature extends AbstractFeatureOp {

	public static final String MIN = "Minimum";

	public static final String MAX = "Maximum";

	public static final String MEAN = "Mean";

	public static final String MEDIAN = "Median";

	public static final String VARIANCE = "Variance";

	@Parameter
	private double radius;

	@Parameter(choices = { MAX, MIN, MEAN, MEDIAN, VARIANCE })
	private String operation;

	private static Class<? extends Op> getOpClass(String operation) {
		if (operation.equals(MIN)) return Ops.Stats.Min.class;
		if (operation.equals(MAX)) return Ops.Stats.Max.class;
		if (operation.equals(MEAN)) return Ops.Stats.Mean.class;
		if (operation.equals(MEDIAN)) return Ops.Stats.Median.class;
		if (operation.equals(VARIANCE)) return Ops.Stats.Variance.class;
		throw new IllegalArgumentException();
	}

	private UnaryComputerOp<Iterable, DoubleType> getComputer() {
		return Computers.unary(ops(), getOpClass(operation), DoubleType.class, Iterable.class);
	}

	@Override
	public int count() {
		return 1;
	}

	@Override
	public void apply(FeatureInput in, List<RandomAccessibleInterval<FloatType>> out) {
		applySingle(in, out.get(0));
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList(operation + "_" + radius);
	}

	private void applySingle(FeatureInput in, RandomAccessibleInterval<FloatType> out) {
		UnaryComputerOp<Iterable, DoubleType> computer = getComputer();
		Shape ellipsoid = new HyperEllipsoidShape(scaledRaduis(globalSettings().pixelSize()));
		RandomAccessible<Neighborhood<FloatType>> neighborhoods = ellipsoid
			.neighborhoodsRandomAccessible(in.original());
		DoubleType tmp = new DoubleType();
		Views.interval(Views.pair(neighborhoods, out), out).forEach(p -> {
			computer.compute(p.getA(), tmp);
			p.getB().set(tmp.getRealFloat());
		});
	}

	private double[] scaledRaduis(List<Double> pixelSizes) {
		return pixelSizes.stream().mapToDouble(pixelSize -> radius / pixelSize).toArray();
	}
}
