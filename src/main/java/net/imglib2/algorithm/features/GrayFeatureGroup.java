package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;
import java.util.Objects;

/**
 * @author Matthias Arzt
 */
public class GrayFeatureGroup implements FeatureGroup {

	private final FeatureJoiner joiner;

	GrayFeatureGroup(List<FeatureOp> features) {
		this.joiner = new FeatureJoiner(features);
		if(globalSettings().imageType() != GlobalSettings.ImageType.GRAY_SCALE)
			throw new IllegalArgumentException("GrayFeatureGroup requires ImageType to be gray scale");
	}

	@Override
	public int count() {
		return joiner.count();
	}

	@Override
	public void apply(RandomAccessible<?> in, List<RandomAccessibleInterval<FloatType>> out) {
		if(!(in.randomAccess().get() instanceof RealType))
			throw new IllegalArgumentException();
		joiner.apply(RevampUtils.randomAccessibleToFloat(RevampUtils.uncheckedCast(in)), out);
	}

	@Override
	public List<String> attributeLabels() {
		return joiner.attributeLabels();
	}

	@Override
	public List<FeatureOp> features() {
		return joiner.features();
	}

	@Override
	public Class<FloatType> getType() {
		return FloatType.class;
	}

	@Override
	public GlobalSettings globalSettings() {
		return joiner.globalSettings();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GrayFeatureGroup))
			return false;
		GrayFeatureGroup other = (GrayFeatureGroup) obj;
		return getType().equals(other.getType()) &&
				attributeLabels().equals(other.attributeLabels());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getType(), attributeLabels());
	}
}
