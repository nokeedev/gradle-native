package dev.nokee.platform.ios.fixtures

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile
import dev.gradleplugins.fixtures.sources.SourceFileElement
import dev.nokee.platform.ios.fixtures.elements.NokeeAppUnitXCTestInfoPlist

class SwiftIosUnitXCTest extends SourceElement {
	final SourceElement main
	final SourceElement unitTest = ofElements(new SwiftUnitTest(), new NokeeAppUnitXCTestInfoPlist())

	SwiftIosUnitXCTest(SourceElement main) {
		this.main = main
	}

	@Override
	List<SourceFile> getFiles() {
		return main.files + unitTest.files
	}

	@Override
	void writeToProject(File projectDir) {
		main.writeToProject(projectDir)
		unitTest.writeToProject(projectDir)
	}

	SourceElement withUiTest() {
		return new SwiftIosUiXCTest(this);
	}

	private static class SwiftUnitTest extends SourceFileElement {
		@Override
		SourceFile getSourceFile() {
			return sourceFile('swift', 'swift_ios_applicationTests.swift', '''
import XCTest
@testable import swift_ios_application

class swift_ios_applicationTests: XCTestCase {

    override func setUp() {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testExample() {
        // This is an example of a functional test case.
        // Use XCTAssert and related functions to verify your tests produce the correct results.
    }

    func testPerformanceExample() {
        // This is an example of a performance test case.
        self.measure {
            // Put the code you want to measure the time of here.
        }
    }

}
''')
		}

		@Override
		String getSourceSetName() {
			return 'unitTest'
		}
	}
}
