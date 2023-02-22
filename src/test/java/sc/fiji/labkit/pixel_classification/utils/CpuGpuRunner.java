/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.utils;

import sc.fiji.labkit.pixel_classification.gpu.api.GpuPool;
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
