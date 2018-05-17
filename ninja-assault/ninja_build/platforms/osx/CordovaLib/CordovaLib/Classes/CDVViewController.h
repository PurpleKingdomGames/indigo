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

#import <Cocoa/Cocoa.h>
#import <WebKit/WebKit.h>
#import <Foundation/NSJSONSerialization.h>
#import "CDVAvailability.h"
#import "CDVInvokedUrlCommand.h"
#import "CDVCommandDelegate.h"
#import "CDVCommandQueue.h"
#import "CDVPlugin.h"
#import "CDVWebViewDelegate.h"

@interface CDVViewController : NSWindowController {

@protected
    id <CDVCommandDelegate> _commandDelegate;
    CDVCommandQueue* _commandQueue;
    NSString* _userAgent;
}

@property (nonatomic, strong) IBOutlet NSView* contentView;
@property (nonatomic, strong) IBOutlet WebView* webView;
@property (nonatomic, strong) IBOutlet CDVWebViewDelegate* webViewDelegate;

@property (nonatomic, readonly, strong) NSMutableDictionary* pluginObjects;
@property (nonatomic, readonly, strong) NSDictionary* pluginsMap;
@property (nonatomic, readonly, strong) NSMutableDictionary* settings;
@property (nonatomic, readonly, strong) NSXMLParser* configParser;
@property (nonatomic, readonly, assign) BOOL loadFromString;

@property (nonatomic, readwrite, copy) NSString* wwwFolderName;
@property (nonatomic, readwrite, copy) NSString* startPage;
@property (nonatomic, readonly, strong) CDVCommandQueue* commandQueue;
@property (nonatomic, readonly, strong) id <CDVCommandDelegate> commandDelegate;

- (id) getCommandInstance:(NSString*) pluginName;

/**
 * Action that toggles the fullscreen.
 * This is an action target of the MainMenu -> Fullscreen menu item.
 */
- (IBAction) onFullscreen:(id) sender;

/**
 * Action that invokes the application's preferences.
 * This is an action target of the MainMenu -> Preferences menu item.
 *
 * The default implementation doesn't do much yet and implementations would need to override
 * this method on the {@link MainViewController} or adjust the menu items `target` property.
 */
- (IBAction) onPreferences:(id) sender;

- (void) registerPlugin:(CDVPlugin*) plugin withClassName:(NSString*) className;

- (void) registerPlugin:(CDVPlugin*) plugin withPluginName:(NSString*) pluginName;

@end

// add private web preferences
@interface WebPreferences (WebPrivate)

- (BOOL) webGLEnabled;

- (void) setWebGLEnabled:(BOOL) enabled;

- (BOOL) localStorageEnabled;

- (void) setLocalStorageEnabled:(BOOL) localStorageEnabled;

- (NSString*) _localStorageDatabasePath;

- (void) _setLocalStorageDatabasePath:(NSString*) path;
@end
