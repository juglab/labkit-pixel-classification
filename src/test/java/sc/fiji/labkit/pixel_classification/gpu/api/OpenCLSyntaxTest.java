/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.gpu.api;

import sc.fiji.labkit.pixel_classification.gpu.api.OpenCLSyntax;
import org.junit.Test;

import java.util.Arrays;
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
