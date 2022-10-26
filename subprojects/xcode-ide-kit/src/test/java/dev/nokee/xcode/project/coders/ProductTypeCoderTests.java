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

import dev.nokee.xcode.objects.targets.ProductType;
import dev.nokee.xcode.objects.targets.ProductTypes;
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

import java.util.Arrays;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductTypeCoderTests {
	@InjectMocks ProductTypeCoder subject;

	@Nested
	class WhenDecoding {
		@Mock Decoder context;

		@ParameterizedTest
		@ArgumentsSource(KnownProductTypeProvider.class)
		void canDecodeKnownProductType(ProductType knownProductType) {
			when(context.decodeString()).thenReturn(knownProductType.getIdentifier());
			assertThat(subject.decode(context), equalTo(knownProductType));
		}

		@Test
		void canDecodeAdhocProductType() {
			when(context.decodeString()).thenReturn("com.example.my-type");
			assertThat(subject.decode(context), equalTo(ProductType.of("com.example.my-type", null)));
		}
	}

	@Nested
	class WhenEncoding {
		@Mock Encoder encoder;

		@ParameterizedTest
		@ArgumentsSource(KnownProductTypeProvider.class)
		void canEncodeKnownProductType(ProductType knownProductType) {
			subject.encode(knownProductType, encoder);
			verify(encoder).encodeString(knownProductType.getIdentifier());
		}

		@Test
		void canEncodeAdhocProductType() {
			subject.encode(ProductType.of("com.example.my-type", null), encoder);
			verify(encoder).encodeString("com.example.my-type");
		}
	}

	static final class KnownProductTypeProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Arrays.stream(ProductTypes.values()).map(Arguments::of);
		}
	}
}
