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

package sc.fiji.labkit.pixel_classification.gpu.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

class OpenCLSyntax {

	private final static Predicate<String> IDENTIFIER_PATTERN =
		Pattern.compile("\\A[_a-zA-Z][_a-zA-Z0-9]*\\z").asPredicate();

	private final static Set<String> RESERVED_WORDS = initSetOfReservedWords();

	public static boolean isIdentifier(String name) {
		return IDENTIFIER_PATTERN.test(name);
	}

	public static boolean isReservedWord(String name) {
		return RESERVED_WORDS.contains(name);
	}

	public static boolean isValidVariableName(String name) {
		return isIdentifier(name) && !isReservedWord(name);
	}

	// -- Helper methods --

	private static Set<String> initSetOfReservedWords() {
		List<String> scalarTypes = Arrays.asList("char", "uchar", "short", "ushort", "int", "uint",
			"long", "ulong",
			"float", "double");
		List<String> vectorTypes = anyCombination(scalarTypes, Arrays.asList("2", "4", "8", "16",
			"32"));
		HashSet<String> result = new HashSet<>();
		result.addAll(scalarTypes);
		result.addAll(vectorTypes);
		result.addAll(Arrays.asList("void", "unsigned", "signed", "local", "global", "constant",
			"private", "__local",
			"__global", "__constant", "__private", "image2d_t", "image3d_t", "image2d_array_t",
			"image1d_t",
			"image1d_buffer_t", "image1d_array_t", "image2d_depth_t", "image2d_array_depth_t",
			"sampler_t",
			"queue_t", "ndrange_t", "clk_event_t", "reserve_id_t", "event_t", "cl_mem_fence_flags"));
		return result;
	}

	static List<String> anyCombination(List<String> listA, List<String> listB) {
		ArrayList<String> result = new ArrayList<>();
		for (String b : listB)
			for (String a : listA)
				result.add(a + b);
		return result;
	}
}
