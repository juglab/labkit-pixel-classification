
package clij;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link OpenCLSyntax}.
 */
public class OpenCLSyntaxTest {

	@Test
	public void testIsIdentifier() {
		assertTrue(OpenCLSyntax.isIdentifier("float"));
		assertTrue(OpenCLSyntax.isIdentifier("a"));
		assertTrue(OpenCLSyntax.isIdentifier("a2"));
		assertTrue(OpenCLSyntax.isIdentifier("A"));
		assertTrue(OpenCLSyntax.isIdentifier("B"));
		assertTrue(OpenCLSyntax.isIdentifier("UPPERCASE"));
		assertTrue(OpenCLSyntax.isIdentifier("image_1"));
		assertTrue(OpenCLSyntax.isIdentifier("_x"));
		assertFalse(OpenCLSyntax.isIdentifier("1"));
		assertFalse(OpenCLSyntax.isIdentifier("space is not allowed"));
		assertFalse(OpenCLSyntax.isIdentifier("4b"));
	}

	@Test
	public void testIsValid() {
		assertTrue(OpenCLSyntax.isValidVariableName("a"));
		assertTrue(OpenCLSyntax.isValidVariableName("a2"));
		assertFalse(OpenCLSyntax.isValidVariableName("7a"));
		assertFalse(OpenCLSyntax.isValidVariableName("int2"));
	}

	@Test
	public void testIsReservedWord() {
		assertTrue(OpenCLSyntax.isReservedWord("float"));
		assertTrue(OpenCLSyntax.isReservedWord("global"));
		assertTrue(OpenCLSyntax.isReservedWord("float2"));
		assertFalse(OpenCLSyntax.isReservedWord("abc"));
	}

	@Test
	public void testAnyCombination() {
		List<String> a = Arrays.asList("x", "y");
		List<String> b = Arrays.asList("1", "2");
		List<String> result = OpenCLSyntax.anyCombination(a, b);
		List<String> expected = Arrays.asList("x1", "y1", "x2", "y2");
		assertEquals(expected, result);
	}

}
