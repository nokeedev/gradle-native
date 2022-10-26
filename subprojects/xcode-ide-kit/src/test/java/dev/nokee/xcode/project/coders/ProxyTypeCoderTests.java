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

import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.project.Decoder;
import dev.nokee.xcode.project.Encoder;
import dev.nokee.xcode.project.ValueCoder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProxyTypeCoderTests {
	@InjectMocks ProxyTypeCoder subject;

	@Nested
	class WhenDecoding {
		@Mock Decoder decoder;

		@ParameterizedTest
		@EnumSource(PBXContainerItemProxy.ProxyType.class)
		void canDecodeKnownSubFolder(PBXContainerItemProxy.ProxyType knownProxyType) {
			when(decoder.decodeInteger()).thenReturn(knownProxyType.getIntValue());
			assertThat(subject.decode(decoder), equalTo(knownProxyType));
		}
	}

	@Nested
	class WhenEncoding {
		@Mock Encoder encoder;

		@ParameterizedTest
		@EnumSource(PBXContainerItemProxy.ProxyType.class)
		void canEncodeKnownSubFolder(PBXContainerItemProxy.ProxyType knownProxyType) {
			subject.encode(knownProxyType, encoder);
			verify(encoder).encodeInteger(knownProxyType.getIntValue());
		}
	}
}
