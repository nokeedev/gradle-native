import SwiftGreeter

func main() -> Int {
	let greeter = Greeter()
	if (greeter.sayHello(name: "Alice") == "Bonjour, Alice!") {
		return 0
	}
	return -1
}

_ = main()
