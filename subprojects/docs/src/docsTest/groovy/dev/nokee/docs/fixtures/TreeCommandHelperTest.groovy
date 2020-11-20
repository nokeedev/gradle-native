package dev.nokee.docs.fixtures

import spock.lang.Specification

class TreeCommandHelperTest extends Specification {
	def "can parse typical tree command output from Ubuntu"() {
		def output = '''./a
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

		expect:
		def data = TreeCommandHelper.Output.parse(output)
		data.paths == ['./a/b1.txt', './a/b2', './a/b2/c1', './a/b2/c1/d1.txt', './a/b2/c1/d2.txt', './a/b2/c2', './a/b2/c2/d3.txt', './a/b2/c2/d4.txt', './a/b3.txt']
	}

	def "can parse typical tree command output from macOS"() {
		def output = '''./a
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

		expect:
		println output
		def data = TreeCommandHelper.Output.parse(output)
		data.paths == ['./a/b1.txt', './a/b2', './a/b2/c1', './a/b2/c1/d1.txt', './a/b2/c1/d2.txt', './a/b2/c2', './a/b2/c2/d3.txt', './a/b2/c2/d4.txt', './a/b3.txt']
	}
}
