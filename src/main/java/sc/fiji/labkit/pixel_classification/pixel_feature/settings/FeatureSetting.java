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

package sc.fiji.labkit.pixel_classification.pixel_feature.settings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.imagej.ops.OpInfo;
import net.imagej.ops.OpService;
import net.imglib2.util.Cast;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import org.scijava.Context;
import org.scijava.command.CommandInfo;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleItem;
import org.scijava.service.SciJavaService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Stores settings of a feature like class and parameter values. Responsible for
 * serialization, and creating an instance of a feature.
 *
 * @author Matthias Arzt
 */
public class FeatureSetting {

	private final CommandInfo commandInfo;

	private final Map<String, Object> parameterValues = new HashMap<>();

	private FeatureSetting(CommandInfo commandInfo, Function<String, ?> parameterSupplier) {
		this.commandInfo = commandInfo;
		for (String parameter : initParameters())
			parameterValues.put(parameter, parameterSupplier.apply(parameter));
	}

	public FeatureSetting(Class<? extends FeatureOp> featureClass, Object... args) {
		this(new CommandInfo(featureClass), ignore -> null);
		for (Map.Entry<String, ?> entry : argsToMap(args).entrySet())
			setParameter(entry.getKey(), entry.getValue());
	}

	public FeatureSetting(FeatureSetting featureSetting) {
		this.commandInfo = featureSetting.commandInfo;
		this.parameterValues.putAll(featureSetting.parameterValues);
	}

	private static Map<String, Object> argsToMap(Object[] args) {
		Map<String, Object> map = new HashMap<>();
		if (args.length % 2 != 0) throw new IllegalArgumentException();
		for (int i = 0; i < args.length; i += 2) {
			Object key = args[i];
			Object value = args[i + 1];
			if (!(key instanceof String)) throw new IllegalArgumentException();
			map.put((String) key, value);
		}
		return map;
	}

	public static FeatureSetting fromClass(Class<? extends FeatureOp> featureClass) {
		try {
			return fromModule(new OpInfo(featureClass).cInfo().createModule());
		}
		catch (ModuleException e) {
			throw new RuntimeException(e);
		}
	}

	public static FeatureSetting fromOp(FeatureOp op) {
		CommandInfo commandInfo = new OpInfo(op.getClass()).cInfo();
		Module module = commandInfo.createModule(op);
		return new FeatureSetting(commandInfo, module::getInput);
	}

	public static FeatureSetting fromModule(Module module) {
		Object object = module.getDelegateObject();
		if (object instanceof FeatureOp)
			return fromOp((FeatureOp) object);
		throw new IllegalArgumentException();
	}

	public static FeatureSetting copy(FeatureSetting fs) {
		return new FeatureSetting(fs.commandInfo, fs.parameterValues::get);
	}

	public FeatureOp newInstance(Context context, GlobalSettings globalSettings) {
		@SuppressWarnings("unchecked")
		FeatureOp delegateObject = (FeatureOp) asModule(globalSettings).getDelegateObject();
		context.inject(delegateObject);
		delegateObject.setEnvironment(context.service(OpService.class));
		delegateObject.initialize();
		return delegateObject;
	}

	public Set<String> parameters() {
		return parameterValues.keySet();
	}

	private List<String> initParameters() {
		List<String> parameters = new ArrayList<>();
		commandInfo.inputs().forEach(mi -> {
			if (isParameterValid(mi)) parameters.add(mi.getName());
		});
		return parameters;
	}

	public <T> void setParameter(String name, T value) {
		if (!parameterValues.containsKey(name))
			throw new IllegalArgumentException("Invalid parameter key: " + name + " for feature: " +
				commandInfo.getTitle());
		parameterValues.put(name, value);
	}

	public Object getParameter(String name) {
		return parameterValues.get(name);
	}

	public Type getParameterType(String name) {
		return commandInfo.getInput(name).getGenericType();
	}

	public Module asModule(GlobalSettings globalSettings) {
		try {
			Module module = commandInfo.createModule();
			for (String parameter : parameters()) {
				Object value = parameterValues.get(parameter);
				if (value != null)
					module.setInput(parameter, value);
			}
			module.setInput("globalSettings", globalSettings);
			module.resolveInput("globalSettings");
			module.resolveInput("in");
			module.resolveInput("out");
			return module;
		}
		catch (ModuleException e) {
			throw new RuntimeException(e);
		}
	}

	public String getName() {
		String label = commandInfo.getLabel();
		return label.isEmpty() ? commandInfo.getPluginClass().getSimpleName() : label;
	}

	public static FeatureSetting fromJson(JsonElement element) {
		JsonObject o = element.getAsJsonObject();
		String className = o.get("class").getAsString();
		className = updateDeprecatedPackageNames(className);
		FeatureSetting fs = FeatureSetting.fromClass(classForName(className));
		for (String p : fs.parameters())
			fs.setParameter(p, new Gson().fromJson(o.get(p), fs.getParameterType(p)));
		return fs;
	}

	private static String updateDeprecatedPackageNames( String className )
	{
		if( className.startsWith( "net.imglib2.trainable_segmention.pixel_feature.filter" ) )
			return className.replace( "net.imglib2.trainable_segmention.pixel_feature.filter", "sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated" );
		if( className.startsWith( "net.imglib2.trainable_segmentation.pixel_feature.filter" ) )
			return className.replace( "net.imglib2.trainable_segmentation.pixel_feature.filter", "sc.fiji.labkit.pixel_classification.pixel_feature.filter" );
		return className;
	}

	public JsonElement toJsonTree() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("class", new JsonPrimitive(commandInfo.getDelegateClassName()));
		for (String parameter : parameters())
			jsonObject.add(parameter, new Gson().toJsonTree(getParameter(parameter), getParameterType(
				parameter)));
		return jsonObject;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FeatureSetting))
			return false;
		FeatureSetting fs = (FeatureSetting) obj;
		return pluginClass().equals(fs.pluginClass()) &&
			this.parameterValues.equals(fs.parameterValues);
	}

	public Class<? extends FeatureOp> pluginClass() {
		return Cast.unchecked(this.commandInfo.getPluginClass());
	}

	@Override
	public int hashCode() {
		return Objects.hash(commandInfo.getPluginClass(), parameterValues);
	}

	// -- Helper methods --

	private static final List<String> EXCLUDE = Arrays.asList("in", "out", "globalSettings",
		"context");

	private boolean isParameterValid(ModuleItem<?> mi) {
		return !(EXCLUDE.contains(mi.getName())) &&
			!SciJavaService.class.isAssignableFrom(mi.getType());
	}

	private static Class<? extends FeatureOp> classForName(String className) {
		try {
			@SuppressWarnings("unchecked")
			Class<? extends FeatureOp> tClass = (Class<? extends FeatureOp>) Class.forName(className);
			return tClass;
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
