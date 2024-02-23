#include <string>

#ifdef __has_attribute
#if __has_attribute(objc_root_class)
__attribute__((objc_root_class))
#endif
#endif
@interface Greeter { id isa; }
+ (id)new;
- (std::string)sayHello:(std::string)name;
@end
