#import "BeaconsPlugin.h"
#if __has_include(<beacons_plugin/beacons_plugin-Swift.h>)
#import <beacons_plugin/beacons_plugin-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "beacons_plugin-Swift.h"
#endif

@implementation BeaconsPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftBeaconsPlugin registerWithRegistrar:registrar];
}
@end
