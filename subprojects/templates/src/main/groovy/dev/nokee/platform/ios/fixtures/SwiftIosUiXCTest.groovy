package dev.nokee.platform.ios.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.nokee.platform.ios.fixtures.elements.NokeeAppUiXCTestInfoPlist

class SwiftIosUiXCTest extends SourceElement {
	final SourceElement main
	final SourceElement uiTest = ofElements(new SwiftUiTest(), new NokeeAppUiXCTestInfoPlist())

	SwiftIosUiXCTest(SourceElement main) {
		this.main = main
	}

	@Override
	List<SourceFile> getFiles() {
		return main.files + uiTest.files
	}

	@Override
	void writeToProject(TestFile projectDir) {
		main.writeToProject(projectDir)
		uiTest.writeToProject(projectDir)
	}
}

class SwiftUiTest extends SourceFileElement {
	@Override
	SourceFile getSourceFile() {
		return sourceFile('swift', 'swift_ios_applicationUITests.m', '''
import XCTest

class swift_ios_applicationUITests: XCTestCase {

    override func setUp() {
        // Put setup code here. This method is called before the invocation of each test method in the class.

        // In UI tests it is usually best to stop immediately when a failure occurs.
        continueAfterFailure = false

        // In UI tests it’s important to set the initial state - such as interface orientation - required for your tests before they run. The setUp method is a good place to do this.
    }

    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testExample() {
        // UI tests must launch the application that they test.
        let app = XCUIApplication()
        app.launch()

        // Use recording to get started writing UI tests.
        // Use XCTAssert and related functions to verify your tests produce the correct results.
    }

    func testLaunchPerformance() {
        if #available(macOS 10.15, iOS 13.0, tvOS 13.0, *) {
            // This measures how long it takes to launch your application.
            measure(metrics: [XCTOSSignpostMetric.applicationLaunch]) {
                XCUIApplication().launch()
            }
        }
    }
}
''')
	}

	@Override
	String getSourceSetName() {
		return 'uiTest'
	}
}
