#ifdef __has_attribute
#if __has_attribute(objc_root_class)
__attribute__((objc_root_class))
#endif
#endif
@interface GreetAlice { id isa; }
+ (id)new;
- (void)sayHelloToAlice;
@end
