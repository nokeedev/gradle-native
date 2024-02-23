#ifdef __has_attribute
#if __has_attribute(objc_root_class)
__attribute__((objc_root_class))
#endif
#endif
@interface Greeter { id isa; }
+ (id)new;
- (char *)sayHello:(const char *)name;
@end
