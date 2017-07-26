package net.imglib2.algorithm.features.gui;

import org.scijava.module.*;

import java.util.*;

/**
 * @author Matthias Arzt
 */
class TrivialModule implements Module {

	private ModuleInfo info;

	private final Map<String, Object> values;

	private final Set<String> resolved = new TreeSet<>();

	TrivialModule(Map<String, Object> map) {
		MutableModuleInfo i = new DefaultMutableModuleInfo();
		i.setModuleClass(TrivialModule.class);
		for(Map.Entry<String, Object> entry : map.entrySet())
			i.addInput(new DefaultMutableModuleItem<>(i, entry.getKey(), entry.getValue().getClass()));
		values = map;
		info = i;
	}

	@Override
	public void preview() {
		// do nothing
	}

	@Override
	public void cancel() {
		// do nothing
	}

	@Override
	public void initialize() throws MethodCallException {
		// do nothing
	}

	@Override
	public ModuleInfo getInfo() {
		return info;
	}

	@Override
	public Object getDelegateObject() {
		return values;
	}

	@Override
	public Object getInput(String name) {
		return values.get(name);
	}

	@Override
	public Object getOutput(String name) {
		throw new NoSuchElementException();
	}

	@Override
	public Map<String, Object> getInputs() {
		return values;
	}

	@Override
	public Map<String, Object> getOutputs() {
		return null;
	}

	@Override
	public void setInput(String name, Object value) {
		values.put(name, value);
	}

	@Override
	public void setOutput(String name, Object value) {
		throw new IllegalArgumentException();
	}

	@Override
	public void setInputs(Map<String, Object> inputs) {
		values.putAll(inputs);
	}

	@Override
	public void setOutputs(Map<String, Object> outputs) {

	}

	@Override
	public boolean isInputResolved(String name) {
		return resolved.contains(name);
	}

	@Override
	public boolean isOutputResolved(String name) {
		return false;
	}

	@Override
	public void resolveInput(String name) {
		resolved.add(name);
	}

	@Override
	public void resolveOutput(String name) {
		throw new IllegalArgumentException();
	}

	@Override
	public void unresolveInput(String name) {
		resolved.remove(name);
	}

	@Override
	public void unresolveOutput(String name) {
		throw new IllegalArgumentException();
	}

	@Override
	public void run() {
		throw new UnsupportedOperationException();
	}
}
