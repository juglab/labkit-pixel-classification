package net.imglib2.algorithm.features;

import com.google.gson.*;
import net.imagej.ops.CustomOpEnvironment;
import net.imagej.ops.Op;
import net.imagej.ops.OpInfo;
import net.imagej.ops.OpService;
import net.imagej.ops.special.computer.AbstractUnaryComputerOp;
import net.imagej.ops.special.computer.Computers;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;
import org.scijava.command.CommandInfo;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.service.SciJavaService;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Matthias Arzt
 */

public class OpsGsonTest {

	@Test
	public void testSerialization() {
		Gson gson = initGson(RevampUtils.ops());
		OpService ops = RevampUtils.ops();
		CustomOpEnvironment env = new CustomOpEnvironment(ops, Arrays.asList(new OpInfo(TestOp.class)));

		TestOp testOp = (TestOp) (Object) Computers.unary(env, TestOp.class, List.class, RandomAccessible.class, 2.0, 4.0);

		String actual = gson.toJson(testOp);
		String expected = "{\"class\":\"" + TestOp.class.getName() + "\",\"sigma\":2.0,\"sigma2\":4.0}";
		assertEquals(expected, actual);
	}

	@Test
	public void testDeserialization() {
		String serialized = "{\"class\":\"" + TestOp.class.getName() + "\",\"sigma\":2.0,\"sigma2\":4.0}";

		OpService ops = RevampUtils.ops();
		Gson gson = initGson(ops);

		TestOp loaded = (TestOp) gson.fromJson(serialized, Op.class);

		assertSame(ops, loaded.ops());
		assertEquals(2.0, loaded.sigma, 1e-9);
		assertEquals(4.0, loaded.sigma2, 1e-9);

	}

	// -- Helper methods --

	private Gson initGson(OpService ops) {
		return new GsonBuilder()
				.registerTypeHierarchyAdapter(Op.class, new OpSerializer())
				.registerTypeAdapter(Op.class, new OpDeserializer(ops))
				.create();
	}

	// -- Helper classes --

	public static class TestOp extends AbstractUnaryComputerOp<RandomAccessible<FloatType>, List<RandomAccessibleInterval<FloatType>>> {

		@Parameter
		public double sigma;

		@Parameter
		public double sigma2;

		@Override
		public void compute(RandomAccessible<FloatType> input, List<RandomAccessibleInterval<FloatType>> output) {
			// not needed for testing
		}
	}

	static class OpSerializer implements JsonSerializer<Op> {

		@Override
		public JsonElement serialize(Op op, Type type, JsonSerializationContext jsonSerializationContext) {
			CommandInfo commandInfo = new OpInfo(op.getClass()).cInfo();
			Module module = commandInfo.createModule(op);
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("class", new JsonPrimitive(op.getClass().getName()));
			for(ModuleItem<?> input : commandInfo.inputs()) {
				if(! SciJavaService.class.isAssignableFrom(input.getType()) &&
						! Arrays.asList("in", "out", "ops").contains(input.getName()) )
					jsonObject.add(input.getName(), jsonSerializationContext.serialize(input.getValue(module)));
			}
			return jsonObject;
		}
	}

	static class OpDeserializer implements JsonDeserializer<Op> {

		private final OpService opService;

		OpDeserializer(OpService opService) {
			this.opService = opService;
		}

		@Override
		public Op deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject o = jsonElement.getAsJsonObject();
			String className = o.get("class").getAsString();
			o.remove("class");
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Op> type1 = (Class<? extends Op>) Class.forName(className);
				OpInfo opInfo = new OpInfo(type1);
				Op op = jsonDeserializationContext.deserialize(o, type1);
				op.setEnvironment(opService);
				opService.getContext().inject(op);
				return op;
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
