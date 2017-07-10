package net.imglib2.algorithm.features.ops;

import net.imagej.ops.special.computer.AbstractUnaryComputerOp;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

/**
 * @author Matthias Arzt
 */
public abstract class FeatureOp extends AbstractUnaryComputerOp<RandomAccessible<FloatType>, List<RandomAccessibleInterval<FloatType>>> {

}
