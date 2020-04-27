
package net.imglib2.trainable_segmention.utils;

import net.imglib2.trainable_segmention.gpu.api.GpuPool;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;
import org.junit.runners.parameterized.TestWithParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runner for JUnit tests, that can run on either CPU or GPU. The test must have
 * a constructor with a boolean parameter indicating if the test runs on CPU or
 * GPU.
 */
public class CpuGpuRunner extends Suite {

	private final List<Runner> runners;

	/**
	 * Only called reflectively. Do not use programmatically.
	 */
	public CpuGpuRunner(Class<?> klass) throws Throwable {
		super(klass, Collections.<Runner> emptyList());
		runners = Collections.unmodifiableList(createRunnersForParameters());
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	private List<Runner> createRunnersForParameters() throws Exception {
		List<Runner> runners = new ArrayList<>();
		runners.add(createRunner("CPU", false));
		if (isGpuAvailable())
			runners.add(createRunner("GPU", true));
		return runners;
	}

	private Runner createRunner(String name, boolean useGpu) throws InitializationError {
		TestWithParameters test = new TestWithParameters("[" + name + "]", getTestClass(), Collections
			.singletonList(useGpu));
		return new BlockJUnit4ClassRunnerWithParameters(test);
	}

	private boolean isGpuAvailable() {
		return GpuPool.isGpuAvailable();
	}
}
