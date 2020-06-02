package dev.nokee.platform.ios.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.gradleplugins.test.fixtures.sources.objectivec.ObjectiveCSourceElement
import dev.nokee.platform.ios.fixtures.elements.NokeeAppUiXCTestInfoPlist

class ObjectiveCIosUiXCTest extends SourceElement {
	final SourceElement main
	final SourceElement uiTest = ofElements(new ObjectiveCUiTest(), new NokeeAppUiXCTestInfoPlist())

	ObjectiveCIosUiXCTest(SourceElement main) {
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


class ObjectiveCUiTest extends ObjectiveCSourceElement {
	@Override
	SourceElement getHeaders() {
		return empty()
	}

	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('objc', 'objective_c_ios_applicationUITests.m', '''
#import <XCTest/XCTest.h>

@interface objective_c_ios_applicationUITests : XCTestCase

@end

@implementation objective_c_ios_applicationUITests

- (void)setUp {
    // Put setup code here. This method is called before the invocation of each test method in the class.

    // In UI tests it is usually best to stop immediately when a failure occurs.
    self.continueAfterFailure = NO;

    // In UI tests it’s important to set the initial state - such as interface orientation - required for your tests before they run. The setUp method is a good place to do this.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
}

- (void)testExample {
    // UI tests must launch the application that they test.
    XCUIApplication *app = [[XCUIApplication alloc] init];
    [app launch];

    // Use recording to get started writing UI tests.
    // Use XCTAssert and related functions to verify your tests produce the correct results.
}

- (void)testLaunchPerformance {
    if (@available(macOS 10.15, iOS 13.0, tvOS 13.0, *)) {
        // This measures how long it takes to launch your application.
        [self measureWithMetrics:@[XCTOSSignpostMetric.applicationLaunchMetric] block:^{
            [[[XCUIApplication alloc] init] launch];
        }];
    }
}

@end
'''))
	}

	@Override
	String getSourceSetName() {
		return 'uiTest'
	}
}
