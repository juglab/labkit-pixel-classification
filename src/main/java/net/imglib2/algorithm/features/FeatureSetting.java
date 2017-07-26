package net.imglib2.algorithm.features;

import net.imagej.ops.OpEnvironment;
import net.imagej.ops.OpInfo;
import net.imglib2.algorithm.features.ops.FeatureOp;
import org.scijava.command.CommandInfo;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleItem;
import org.scijava.service.SciJavaService;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

/**
 * Stores settings of a feature like class and parameter values. Responsible for
 * serialization, and creating an instance of a feature.
 *
 * @autor Matthias Arzt
 */
public class FeatureSetting {

	private final CommandInfo commandInfo;

	private final Map<String, Object> parameterValues = new HashMap<>();

	private final GlobalSettings globalSetting = GlobalSettings.defaultSettings();

	private FeatureSetting(CommandInfo commandInfo, Function<String, ?> parameterSupplier) {
		this.commandInfo = commandInfo;
		for(String parameter : parameters())
			parameterValues.put(parameter, parameterSupplier.apply(parameter));
	}

	public static FeatureSetting fromClass(Class<? extends FeatureOp> featureClass) {
		try {
			return fromModule(new OpInfo(featureClass).cInfo().createModule());
		} catch (ModuleException e) {
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
		if(object instanceof FeatureOp)
			return fromOp((FeatureOp) object);
		throw new IllegalArgumentException();
	}

	public static FeatureSetting copy(FeatureSetting fs) {
		return new FeatureSetting(fs.commandInfo, fs.parameterValues::get);
	}

	public FeatureOp newInstance(OpEnvironment ops) {
		@SuppressWarnings("unchecked")
		FeatureOp delegateObject = (FeatureOp) asModule().getDelegateObject();
		ops.context().inject(delegateObject);
		delegateObject.setEnvironment(ops);
		delegateObject.setGlobalSettings(globalSetting);
		delegateObject.initialize();
		return delegateObject;
	}

	public List<String> parameters() {
		List<String> parameters = new ArrayList<>();
		commandInfo.inputs().forEach(mi -> {
			if(isParameterValid(mi)) parameters.add(mi.getName());
		});
		return parameters;
	}

	public <T> void setParameter(String name, T value) {
		parameterValues.put(name, value);
	}

	public Object getParameter(String name) {
		return parameterValues.get(name);
	}

	public Type getParameterType(String name) {
		return commandInfo.getInput(name).getGenericType();
	}

	public Module asModule() {
		try {
			Module module = commandInfo.createModule();
			for(String parameter : parameters()) {
				Object value = parameterValues.get(parameter);
				if(value != null)
					module.setInput(parameter, value);
			}
			module.resolveInput("in");
			module.resolveInput("out");
			return module;
		} catch (ModuleException e) {
			throw new RuntimeException(e);
		}
	}

	public String getName() {
		return commandInfo.getPluginClass().getSimpleName();
	}

	// -- Helper methods --

	private static final List<String> EXCLUDE = Arrays.asList("in", "out");

	private boolean isParameterValid(ModuleItem<?> mi) {
		return !(EXCLUDE.contains(mi.getName())) &&
				!SciJavaService.class.isAssignableFrom(mi.getType());
	}
}
