package dev.nokee.platform.ios.fixtures

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile
import dev.gradleplugins.fixtures.sources.SourceFileElement
import dev.nokee.platform.ios.fixtures.elements.NokeeAppAssets
import dev.nokee.platform.ios.fixtures.elements.NokeeAppBaseLanguage
import dev.nokee.platform.ios.fixtures.elements.NokeeAppInfoPlist

import java.nio.file.Path

class SwiftIosApp extends SourceElement {
	final SourceElement main = ofElements(
		new SwiftAppDelegate(), new SwiftSceneDelegate(), new SwiftViewController(),
		new NokeeAppBaseLanguage(true), new NokeeAppInfoPlist(true), new NokeeAppAssets()
	)

	@Override
	List<SourceFile> getFiles() {
		return main.files
	}

	@Override
	void writeToProject(Path projectDir) {
		main.writeToProject(projectDir)
	}

	SwiftIosUnitXCTest withUnitTest() {
		return new SwiftIosUnitXCTest(this)
	}

	private static class SwiftAppDelegate extends SourceFileElement {
		@Override
		SourceFile getSourceFile() {
			return sourceFile('swift', 'AppDelegate.swift', '''
import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {



    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        return true
    }

    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }


}
''')
		}


	}

	private static class SwiftSceneDelegate extends SourceFileElement {
		@Override
		SourceFile getSourceFile() {
			return sourceFile('swift', 'SceneDelegate.swift', '''
import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?


    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        // Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
        // If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
        // This delegate does not imply the connecting scene or session are new (see `application:configurationForConnectingSceneSession` instead).
        guard let _ = (scene as? UIWindowScene) else { return }
    }

    func sceneDidDisconnect(_ scene: UIScene) {
        // Called as the scene is being released by the system.
        // This occurs shortly after the scene enters the background, or when its session is discarded.
        // Release any resources associated with this scene that can be re-created the next time the scene connects.
        // The scene may re-connect later, as its session was not neccessarily discarded (see `application:didDiscardSceneSessions` instead).
    }

    func sceneDidBecomeActive(_ scene: UIScene) {
        // Called when the scene has moved from an inactive state to an active state.
        // Use this method to restart any tasks that were paused (or not yet started) when the scene was inactive.
    }

    func sceneWillResignActive(_ scene: UIScene) {
        // Called when the scene will move from an active state to an inactive state.
        // This may occur due to temporary interruptions (ex. an incoming phone call).
    }

    func sceneWillEnterForeground(_ scene: UIScene) {
        // Called as the scene transitions from the background to the foreground.
        // Use this method to undo the changes made on entering the background.
    }

    func sceneDidEnterBackground(_ scene: UIScene) {
        // Called as the scene transitions from the foreground to the background.
        // Use this method to save data, release shared resources, and store enough scene-specific state information
        // to restore the scene back to its current state.
    }


}
''')
		}
	}

	private static class SwiftViewController extends SourceFileElement {
		@Override
		SourceFile getSourceFile() {
			return sourceFile('swift', 'ViewController.swift', '''
import UIKit

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
    }


}
''')
		}
	}
}
