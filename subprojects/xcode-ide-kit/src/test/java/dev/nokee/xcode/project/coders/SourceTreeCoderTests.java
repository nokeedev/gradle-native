/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.xcode.project.coders;

import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.project.Decoder;
import dev.nokee.xcode.project.Encoder;
import dev.nokee.xcode.project.ValueCoder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static dev.nokee.xcode.objects.files.PBXSourceTree.ABSOLUTE;
import static dev.nokee.xcode.objects.files.PBXSourceTree.BUILT_PRODUCTS_DIR;
import static dev.nokee.xcode.objects.files.PBXSourceTree.DEVELOPER_DIR;
import static dev.nokee.xcode.objects.files.PBXSourceTree.GROUP;
import static dev.nokee.xcode.objects.files.PBXSourceTree.SDKROOT;
import static dev.nokee.xcode.objects.files.PBXSourceTree.SOURCE_ROOT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SourceTreeCoderTests {
	@InjectMocks SourceTreeCoder subject;

	@Nested
	class WhenDecoding {
		@Mock Decoder decoder;

		@ParameterizedTest
		@ArgumentsSource(KnownSourceTreeProvider.class)
		void canDecodeKnownSourceTree(PBXSourceTree knownSourceTree) {
			when(decoder.decodeString()).thenReturn(knownSourceTree.toString());
			assertThat(subject.decode(decoder), equalTo(knownSourceTree));
		}

		@Test
		void canDecodeAdhocSourceTree() {
			when(decoder.decodeString()).thenReturn("MY_CUSTOM_PATH");
			assertThat(subject.decode(decoder), equalTo(PBXSourceTree.of("MY_CUSTOM_PATH")));
		}
	}

	@Nested
	class WhenEncoding {
		@Mock Encoder encoder;

		@ParameterizedTest
		@ArgumentsSource(KnownSourceTreeProvider.class)
		void canEncodeKnownSourceTree(PBXSourceTree knownSourceTree) {
			subject.encode(knownSourceTree, encoder);
			verify(encoder).encodeString(knownSourceTree.toString());
		}

		@Test
		void canEncodeAdhocSourceTree() {
			subject.encode(PBXSourceTree.of("MY_CUSTOM_PATH"), encoder);
			verify(encoder).encodeString("MY_CUSTOM_PATH");
		}
	}

	static final class KnownSourceTreeProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(ABSOLUTE, GROUP, SOURCE_ROOT, SDKROOT, DEVELOPER_DIR, BUILT_PRODUCTS_DIR).map(Arguments::of);
		}
	}
}
