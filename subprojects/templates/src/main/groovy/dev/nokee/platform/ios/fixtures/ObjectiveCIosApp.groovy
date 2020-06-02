package dev.nokee.platform.ios.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.gradleplugins.test.fixtures.sources.objectivec.ObjectiveCSourceElement
import dev.gradleplugins.test.fixtures.sources.objectivec.ObjectiveCSourceFileElement
import dev.nokee.platform.ios.fixtures.elements.NokeeAppAssets
import dev.nokee.platform.ios.fixtures.elements.NokeeAppBaseLanguage
import dev.nokee.platform.ios.fixtures.elements.NokeeAppInfoPlist

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile

class ObjectiveCIosApp extends SourceElement {
	final SourceElement main = ofElements(
		new ObjectiveCAppDelegate(), new ObjectiveCSceneDelegate(), new ObjectiveCViewController(), new ObjectiveCMain(),
		new NokeeAppBaseLanguage(), new NokeeAppInfoPlist(), new NokeeAppAssets()
	)

	@Override
	List<SourceFile> getFiles() {
		return main.files
	}

	@Override
	void writeToProject(TestFile projectDir) {
		main.writeToProject(projectDir)
	}

	ObjectiveCIosUnitXCTest withUnitTest() {
		return new ObjectiveCIosUnitXCTest(this)
	}
}

class ObjectiveCAppDelegate extends ObjectiveCSourceFileElement {
	@Override
	SourceFileElement getHeader() {
		return ofFile(sourceFile('headers', 'AppDelegate.h', '''
#import <UIKit/UIKit.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate>


@end
'''))
	}

	@Override
	SourceFileElement getSource() {
		return ofFile(sourceFile('objc', 'AppDelegate.m', '''
#import "AppDelegate.h"

@interface AppDelegate ()

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Override point for customization after application launch.
    return YES;
}


#pragma mark - UISceneSession lifecycle


- (UISceneConfiguration *)application:(UIApplication *)application configurationForConnectingSceneSession:(UISceneSession *)connectingSceneSession options:(UISceneConnectionOptions *)options {
    // Called when a new scene session is being created.
    // Use this method to select a configuration to create the new scene with.
    return [[UISceneConfiguration alloc] initWithName:@"Default Configuration" sessionRole:connectingSceneSession.role];
}


- (void)application:(UIApplication *)application didDiscardSceneSessions:(NSSet<UISceneSession *> *)sceneSessions {
    // Called when the user discards a scene session.
    // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
    // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
}


@end
'''))
	}
}

class ObjectiveCSceneDelegate extends ObjectiveCSourceFileElement {
	@Override
	SourceFileElement getHeader() {
		return ofFile(sourceFile('headers', 'SceneDelegate.h', '''
#import <UIKit/UIKit.h>

@interface SceneDelegate : UIResponder <UIWindowSceneDelegate>

@property (strong, nonatomic) UIWindow * window;

@end
'''))
	}

	@Override
	SourceFileElement getSource() {
		return ofFile(sourceFile('objc', 'SceneDelegate.m', '''
#import "SceneDelegate.h"

@interface SceneDelegate ()

@end

@implementation SceneDelegate


- (void)scene:(UIScene *)scene willConnectToSession:(UISceneSession *)session options:(UISceneConnectionOptions *)connectionOptions {
    // Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
    // If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
    // This delegate does not imply the connecting scene or session are new (see `application:configurationForConnectingSceneSession` instead).
}


- (void)sceneDidDisconnect:(UIScene *)scene {
    // Called as the scene is being released by the system.
    // This occurs shortly after the scene enters the background, or when its session is discarded.
    // Release any resources associated with this scene that can be re-created the next time the scene connects.
    // The scene may re-connect later, as its session was not neccessarily discarded (see `application:didDiscardSceneSessions` instead).
}


- (void)sceneDidBecomeActive:(UIScene *)scene {
    // Called when the scene has moved from an inactive state to an active state.
    // Use this method to restart any tasks that were paused (or not yet started) when the scene was inactive.
}


- (void)sceneWillResignActive:(UIScene *)scene {
    // Called when the scene will move from an active state to an inactive state.
    // This may occur due to temporary interruptions (ex. an incoming phone call).
}


- (void)sceneWillEnterForeground:(UIScene *)scene {
    // Called as the scene transitions from the background to the foreground.
    // Use this method to undo the changes made on entering the background.
}


- (void)sceneDidEnterBackground:(UIScene *)scene {
    // Called as the scene transitions from the foreground to the background.
    // Use this method to save data, release shared resources, and store enough scene-specific state information
    // to restore the scene back to its current state.
}


@end
'''))
	}
}

class ObjectiveCViewController extends ObjectiveCSourceFileElement {

	@Override
	SourceFileElement getHeader() {
		return ofFile(sourceFile('headers', 'ViewController.h', '''
#import <UIKit/UIKit.h>

@interface ViewController : UIViewController


@end
'''))
	}

	@Override
	SourceFileElement getSource() {
		return ofFile(sourceFile('objc', 'ViewController.m', '''
#import "ViewController.h"

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}


@end
'''))
	}
}

class ObjectiveCMain extends ObjectiveCSourceElement {
	@Override
	SourceElement getHeaders() {
		return empty()
	}

	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('objc', 'main.m', '''
#import <UIKit/UIKit.h>
#import "AppDelegate.h"

int main(int argc, char * argv[]) {
    NSString * appDelegateClassName;
    @autoreleasepool {
        // Setup code that might create autoreleased objects goes here.
        appDelegateClassName = NSStringFromClass([AppDelegate class]);
    }
    return UIApplicationMain(argc, argv, nil, appDelegateClassName);
}
'''))
	}
}

