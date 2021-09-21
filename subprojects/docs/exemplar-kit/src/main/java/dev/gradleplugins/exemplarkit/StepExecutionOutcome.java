/*
 * Copyright 2020 the original author or authors.
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
package dev.gradleplugins.exemplarkit;

/**
 * The execution outcome of a step from an exemplar.
 */
public enum StepExecutionOutcome {
	/**
	 * The step executed either successfully or not.
	 */
	EXECUTED,

	/**
	 * The step failed for a reason other then the executable returning a non-zero exit value.
	 */
	FAILED,

	/**
	 * The step was not executed for some reason.
	 */
	SKIPPED
}
