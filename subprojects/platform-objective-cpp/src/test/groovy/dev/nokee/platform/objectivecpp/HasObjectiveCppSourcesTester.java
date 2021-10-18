/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.objectivecpp;

import dev.nokee.platform.base.testers.ConfigureMethodTester;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public interface HasObjectiveCppSourcesTester {
	HasObjectiveCppSources subject();

	@Test
	default void hasObjectiveCppSources() {
		assertNotNull(subject().getObjectiveCppSources());
	}

	@Test
	default void canConfigureObjectiveCppSources() {
		ConfigureMethodTester.of(subject(), HasObjectiveCppSources::getObjectiveCppSources)
			.testAction(HasObjectiveCppSources::objectiveCppSources)
			.testClosure(HasObjectiveCppSources::objectiveCppSources);
	}
}
