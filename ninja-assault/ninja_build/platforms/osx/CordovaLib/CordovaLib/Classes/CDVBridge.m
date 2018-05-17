/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

#import <WebKit/WebKit.h>
#import <JavaScriptCore/JavaScriptCore.h>
#include <objc/message.h>

#import "CDVBridge.h"
#import "CDVViewController.h"

@implementation CDVBridge

- (void) registerJavaScriptHelpers {
    NSString* cordovaBridgeUtil = @"var CordovaBridgeUtil = {};";
    NSString* dictionaryKeys = [NSString stringWithFormat:@"CordovaBridgeUtil.getDictionaryKeys = function(obj) { return Object.keys(obj);};"];

    id win = [self.webView windowScriptObject];
    [win evaluateWebScript:cordovaBridgeUtil];
    [win evaluateWebScript:dictionaryKeys];
}

- (id) initWithWebView:(WebView*) webView andViewController:(CDVViewController*) viewController {
    if ((self = [super init]) != nil) {
        self.webView = webView;
        self.viewController = viewController;
        [self registerJavaScriptHelpers];
    }

    return self;
}

- (void) exec:(NSString*) callbackId withService:(NSString*) service andAction:(NSString*) action andArguments:(WebScriptObject*) webScriptObject {
    // We are going with the iOS method of passing in a callbackId.
    // Note that we can use the JavaScriptCore C API to pass in the JavaScript function references
    // and context and call them directly, but this is done this way for possible plugin sharing
    // between iOS and OS X. Also we are going async as well.

    // we're just going to assume the webScriptObject passed in is an NSArray
    NSArray* arguments = [[webScriptObject JSValue] toArray];

    CDVInvokedUrlCommand* command = [[CDVInvokedUrlCommand alloc] initWithArguments:arguments callbackId:callbackId className:service methodName:action];

    if ((command.cmdClassName == nil) || (command.methodName == nil)) {
        NSLog(@"ERROR: Classname and/or methodName not found for command.");
        return;
    }

    // Fetch an instance of this class
    CDVPlugin* obj = [_viewController.commandDelegate getCommandInstance:command.cmdClassName];

    if (!([obj isKindOfClass:[CDVPlugin class]])) {
        NSLog(@"ERROR: Plugin '%@' not found, or is not a CDVPlugin. Check your plugin mapping in config.xml.", command.cmdClassName);
        return;
    }

    // Find the proper selector to call.
    NSString* methodName = [NSString stringWithFormat:@"%@:", command.methodName];
    SEL normalSelector = NSSelectorFromString(methodName);
    if ([obj respondsToSelector:normalSelector]) {
        // [obj performSelector:normalSelector withObject:command];
        objc_msgSend(obj, normalSelector, command);
    } else {
        // There's no method to call, so throw an error.
        NSLog(@"ERROR: Method '%@' not defined in Plugin '%@'", methodName, command.cmdClassName);
    }
}

#pragma mark WebScripting Protocol

/* checks whether a selector is acceptable to be called from JavaScript */
+ (BOOL) isSelectorExcludedFromWebScript:(SEL) sel {
    return sel != @selector(exec:withService:andAction:andArguments:);
}

/* helper function so we don't have to have underscores and stuff in js to refer to the right method */
+ (NSString*) webScriptNameForSelector:(SEL) aSelector {
    if (aSelector == @selector(exec:withService:andAction:andArguments:)) {
        return @"exec";
    } else {
        return nil;
    }
}

// right now exclude all properties (eg keys)
+ (BOOL) isKeyExcludedFromWebScript:(const char*) name {
    return YES;
}

@end
