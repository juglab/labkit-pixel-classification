
package sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.lipschitz;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractGroupFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.SingleFeatures;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
@Deprecated
@Plugin(type = FeatureOp.class, label = "Lipschitz (for each sigma)")
public class LipschitzFeature extends AbstractGroupFeatureOp {

	@Parameter
	private long border;

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 2;
	}

	protected List<FeatureSetting> initFeatures() {
		return Arrays.stream(new double[] { 5, 10, 15, 20, 25 })
			.mapToObj(slope -> SingleFeatures.lipschitz(slope, border))
			.collect(Collectors.toList());
	}
}
