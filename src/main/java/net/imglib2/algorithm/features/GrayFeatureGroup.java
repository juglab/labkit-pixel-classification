package net.imglib2.algorithm.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.imagej.ops.OpEnvironment;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class GrayFeatureGroup implements FeatureGroup {

	private final FeatureJoiner joiner;

	private final FeatureSettings settings;

	GrayFeatureGroup(OpEnvironment ops, FeatureSettings settings) {
		this.settings = settings;
		List<FeatureOp> featureOps = settings.features().stream()
				.map(x -> x.newInstance(ops, settings.globals())).collect(Collectors.toList());
		this.joiner = new FeatureJoiner(featureOps);
		if(settings.globals().imageType() != GlobalSettings.ImageType.GRAY_SCALE)
			throw new IllegalArgumentException("GrayFeatureGroup requires ImageType to be gray scale");
	}

	@Override
	public OpEnvironment ops() {
		return joiner.ops();
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
	public FeatureSettings settings() {
		return settings;
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
	public int hashCode() {
		return Objects.hash(getType(), attributeLabels());
	}
}
