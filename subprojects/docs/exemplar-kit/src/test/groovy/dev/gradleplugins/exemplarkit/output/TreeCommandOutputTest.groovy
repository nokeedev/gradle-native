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
package dev.gradleplugins.exemplarkit.output

import com.google.common.testing.EqualsTester
import org.junit.jupiter.api.Test

import static dev.gradleplugins.exemplarkit.output.TreeCommandOutput.from
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

class TreeCommandOutputTest {
	private static final String PLAIN_OUTPUT = '''./a
			||-- b1.txt
			||-- b2
			||   |-- c1
			||   |   |-- d1.txt
			||   |   `-- d2.txt
			||   `-- c2
			||       |-- d3.txt
			||       `-- d4.txt
			|`-- b3.txt
			|
			|3 directories, 6 files
			|'''.stripMargin()
	private static final String RICH_OUTPUT = '''./a
			|├── b1.txt
			|├── b2
			|│   ├── c1
			|│   │   ├── d1.txt
			|│   │   └── d2.txt
			|│   └── c2
			|│       ├── d3.txt
			|│       └── d4.txt
			|└── b3.txt
			|
			|3 directories, 6 files
			|'''.stripMargin()

	@Test
	void "can parse tree command output with plain text"() {
		assertThat(from(PLAIN_OUTPUT).paths, contains('./a/b1.txt', './a/b2', './a/b2/c1', './a/b2/c1/d1.txt', './a/b2/c1/d2.txt', './a/b2/c2', './a/b2/c2/d3.txt', './a/b2/c2/d4.txt', './a/b3.txt'))
	}

	@Test
	void "can parse tree command output with rich text"() {
		assertThat(from(RICH_OUTPUT).paths, contains('./a/b1.txt', './a/b2', './a/b2/c1', './a/b2/c1/d1.txt', './a/b2/c1/d2.txt', './a/b2/c2', './a/b2/c2/d3.txt', './a/b2/c2/d4.txt', './a/b3.txt'))
	}

	@Test
	void "can compare tree command output"() {
		new EqualsTester()
			.addEqualityGroup(from(PLAIN_OUTPUT), from(RICH_OUTPUT))
			.testEquals()
	}
}
