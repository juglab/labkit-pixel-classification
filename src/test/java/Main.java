import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imglib2.trainable_segmention.classification.SegmenterTest;
import org.scijava.Context;

/**
 * Created by arzt on 28.06.17.
 */
public class Main {

	public static void main(String... args) {
		// new SegmenterTest().testClassification();
		OpService service = new Context().service(OpService.class);
		Ops.Math.Add op = service.op(Ops.Math.Add.class, 5, 5);
	}
}
