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

#import "CDVWindowSizeCommand.h"
#import "NSScreen+Utils.h"

@implementation CDVWindowSizeCommand {
}

static NSRect savedFrameRect;

/**
 * Makes the window fullscreen by resizing it to the size of all attached displays. This is different from just entering
 * normal OSX fullscreen mode which only overs the main display.
 */
+ (void) makeFullScreen:(NSWindow*) window {
    NSRect fullScreenRect = [NSScreen fullScreenRect];
    NSLog(@"Full screen resolution: %.1f x %.1f", fullScreenRect.size.width, fullScreenRect.size.height);
    [window setStyleMask:window.styleMask & ~NSTitledWindowMask];
    [window setHidesOnDeactivate:YES];
    [window setLevel:NSMainMenuWindowLevel + 1];
    savedFrameRect = window.frame;
    [window setFrame:fullScreenRect display:YES];
}

/**
 * Takes the window off fullscreen mode.
 */
+ (void) removeFullScreen:(NSWindow*) window {
    [window setStyleMask:window.styleMask | NSTitledWindowMask];
    [window setHidesOnDeactivate:NO];
    [window setLevel:NSNormalWindowLevel];
    [window setFrame:savedFrameRect display:YES];
}

/**
 * Toggles fullscreen mode of the window.
 */
+ (void) toggleFullScreen:(NSWindow*) window {
    if ((window.styleMask & NSTitledWindowMask) == NSTitledWindowMask) {
        [CDVWindowSizeCommand makeFullScreen:window];
    } else {
        [CDVWindowSizeCommand removeFullScreen:window];
    }
}

+ (void) setSizeOfWindow:(NSWindow*) window size:(NSSize) size {
    NSLog(@"Set window size to %.1f x %.1f", size.width, size.height);
    NSRect frameRect = window.frame;
    frameRect.size = size;
    [window setFrame:frameRect display:YES];
}

@end
