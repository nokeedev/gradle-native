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
package dev.nokee.platform.base.internal.dependencies;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

public interface DependencyBucketIdentityDefaultDisplayNameTester {
	DependencyBucketIdentity createSubject(String name);

	@Test
	default void useSingleWordBucketNameAsIsInDisplayName() {
		assertThat(createSubject("guke").getDisplayName(), matchesPattern("^guke( .+)?"));
	}

	@Test
	default void splitMultiWordBucketNameOnCamelCaseToLowerCaseInDisplayName() {
		assertThat(createSubject("noxiKubu").getDisplayName(), matchesPattern("^noxi kubu( .+)?"));
		assertThat(createSubject("javiKotoLasu").getDisplayName(), matchesPattern("^javi koto lasu( .+)?"));
	}

	@Test
	default void capitalizeApiWordInDisplayName() {
		assertThat(createSubject("api").getDisplayName(), matchesPattern("^API( .+)?"));
		assertThat(createSubject("jenoApi").getDisplayName(), matchesPattern("^jeno API( .+)?"));
		assertThat(createSubject("wineApiFopu").getDisplayName(), matchesPattern("^wine API fopu( .+)?"));
	}

	@Test
	default void capitalizeJvmWordInDisplayName() {
		assertThat(createSubject("jvm").getDisplayName(), matchesPattern("^JVM( .+)?"));
		assertThat(createSubject("qupoJvm").getDisplayName(), matchesPattern("^qupo JVM( .+)?"));
		assertThat(createSubject("loxaJvmNali").getDisplayName(), matchesPattern("^loxa JVM nali( .+)?"));
	}

	@Test
	default void doesNotCapitalizeApiWhenNotWordInDisplayName() {
		assertThat(createSubject("apih").getDisplayName(), matchesPattern("^apih( .+)?"));
		assertThat(createSubject("japi").getDisplayName(), matchesPattern("^japi( .+)?"));
		assertThat(createSubject("kapiy").getDisplayName(), matchesPattern("^kapiy( .+)?"));
		assertThat(createSubject("jaweApiy").getDisplayName(), matchesPattern("^jawe apiy( .+)?"));
		assertThat(createSubject("nujaOapi").getDisplayName(), matchesPattern("^nuja oapi( .+)?"));
	}

	@Test
	default void doesNotCapitalizeJvmWhenNotWordInDisplayName() {
		assertThat(createSubject("jvmt").getDisplayName(), matchesPattern("^jvmt( .+)?"));
		assertThat(createSubject("ejvm").getDisplayName(), matchesPattern("^ejvm( .+)?"));
		assertThat(createSubject("wjvmo").getDisplayName(), matchesPattern("^wjvmo( .+)?"));
		assertThat(createSubject("varoJvmf").getDisplayName(), matchesPattern("^varo jvmf( .+)?"));
		assertThat(createSubject("manuNjvm").getDisplayName(), matchesPattern("^manu njvm( .+)?"));
	}
}
