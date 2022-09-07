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
package dev.nokee.model.internal.plugins;

import lombok.val;
import org.gradle.api.plugins.PluginAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PluginTargetSupportTest {
	@Mock Consumer<PluginAware> firstTargetAction;
	@Mock Consumer<PluginAware> secondTargetAction;
	PluginTargetSupport subject;

	@BeforeEach
	void createSubject() {
		subject = PluginTargetSupport.builder().withPluginId("com.example.my-plugin")
			.forTarget(Target1.class, firstTargetAction).forTarget(Target2.class, secondTargetAction).build();
	}

	@Test
	void callsFirstTargetActionOnly() {
		val target = Mockito.mock(Target1.class);
		subject.apply(target);
		verify(firstTargetAction).accept(target);
		verifyNoInteractions(secondTargetAction);
	}

	@Test
	void callsSecondTargetActionOnly() {
		val target = Mockito.mock(Target2.class);
		subject.apply(target);
		verifyNoInteractions(firstTargetAction);
		verify(secondTargetAction).accept(target);
	}

	@Test
	void throwsExceptionWhenTargetNotSupported() {
		val target = Mockito.mock(WrongTarget.class, "wrong-target");
		val ex = assertThrows(RuntimeException.class, () -> subject.apply(target));
		assertThat(ex.getMessage(), equalTo("Could not apply plugin 'com.example.my-plugin' to wrong-target. Please refer to the plugin reference documentation."));
		verifyNoInteractions(firstTargetAction, secondTargetAction);
	}

	private interface Target1 extends PluginAware {}
	private interface Target2 extends PluginAware {}
	private interface WrongTarget extends PluginAware {}
}
