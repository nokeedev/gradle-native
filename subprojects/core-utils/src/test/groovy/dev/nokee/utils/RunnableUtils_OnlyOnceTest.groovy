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
package dev.nokee.utils

import com.google.common.testing.EqualsTester
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.RunnableUtils.onlyOnce

@Subject(RunnableUtils)
class RunnableUtils_OnlyOnceTest extends Specification {

	def "can equals"() {
		given:
		def runnableGroup2 = newRunnable()
		def runnableGroup3 = newRunnable()
		def notRanRunnable = onlyOnce(runnableGroup3)
		def alreadyRanRunnable = run(onlyOnce(runnableGroup3))

		expect:
		new EqualsTester()
			.addEqualityGroup(onlyOnce(newRunnable()))
			.addEqualityGroup(onlyOnce(runnableGroup2), onlyOnce(runnableGroup2))
			.addEqualityGroup(notRanRunnable, alreadyRanRunnable)
			.testEquals()
	}

	def "can run only once"() {
		given:
		def runnable = Mock(Runnable)
		def subject = onlyOnce(runnable)

		when:
		subject.run()
		then:
		1 * runnable.run()

		when:
		subject.run()
		then:
		0 * runnable.run()

		when:
		subject.run()
		then:
		0 * runnable.run()
	}

	def "has sensible toString"() {
		given:
		def runnable = newRunnable()

		expect:
		onlyOnce(runnable).toString() == "RunnableUtils.onlyOnce(${runnable})"
	}

	static Runnable newRunnable() {
		return new Runnable() {
			@Override
			void run() {}
		}
	}

	static Runnable run(Runnable runnable) {
		runnable.run()
		return runnable
	}
}
