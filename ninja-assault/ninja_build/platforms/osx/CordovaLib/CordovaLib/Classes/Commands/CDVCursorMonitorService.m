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

#import "CDVCursorMonitorService.h"

@interface CDVCursorMonitorService ()
    @property(nonatomic, readwrite) BOOL isRunning;
    @property(nonatomic) CFTimeInterval timeout;
@end

@implementation CDVCursorMonitorService {
    CFAbsoluteTime cursorLastMovedTime;
    NSTimer* cursorHideTimer;
    id localMonitor;
    id globalMonitor;
}

#pragma mark - Public methods

+ (CDVCursorMonitorService*) service {
    static CDVCursorMonitorService* sharedInstance;
    static dispatch_once_t onceToken;

    dispatch_once(&onceToken, ^{
        sharedInstance = [[CDVCursorMonitorService alloc] init];
    });

    return sharedInstance;
}

- (void) startWithTimeout:(CFTimeInterval) timeout {
    if (!self.isRunning) {
        self.timeout = timeout;
        cursorLastMovedTime = CFAbsoluteTimeGetCurrent();

        if (timeout > 0) {
            localMonitor = [NSEvent addLocalMonitorForEventsMatchingMask:NSMouseMovedMask | NSLeftMouseDraggedMask handler:^NSEvent*(NSEvent* event) {
                cursorLastMovedTime = CFAbsoluteTimeGetCurrent();
                CGDisplayShowCursor(kCGDirectMainDisplay);
                return event;
            }];

            globalMonitor = [NSEvent addGlobalMonitorForEventsMatchingMask:NSMouseMovedMask | NSLeftMouseDraggedMask handler:^(NSEvent* event) {
                cursorLastMovedTime = CFAbsoluteTimeGetCurrent();
                CGDisplayShowCursor(kCGDirectMainDisplay);
            }];
            cursorHideTimer = [NSTimer scheduledTimerWithTimeInterval:1
                                                               target:self
                                                             selector:@selector(handleCursorHideTimerFire:)
                                                             userInfo:nil
                                                              repeats:YES];
        } else {
            // hide the cursor immediately when the application becomes active
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleCursorHideTimerFire:)
                                                         name:NSApplicationDidBecomeActiveNotification
                                                       object:nil];
            CGDisplayHideCursor(kCGDirectMainDisplay);
        }
        [self setIsRunning:YES];
    }
}

- (void) stop {
    if (self.isRunning) {
        [NSEvent removeMonitor:localMonitor];
        [NSEvent removeMonitor:globalMonitor];
        [cursorHideTimer invalidate];
        localMonitor = nil;
        globalMonitor = nil;
        cursorHideTimer = nil;
        [[NSNotificationCenter defaultCenter] removeObserver:self name:NSApplicationDidBecomeActiveNotification object:nil];

        CGDisplayShowCursor(kCGDirectMainDisplay);

        [self setIsRunning:NO];
    }
}

#pragma mark - Private methods

- (void) handleCursorHideTimerFire:(NSTimer*) timer {
    if (CFAbsoluteTimeGetCurrent() - cursorLastMovedTime >= self.timeout) {
        CGDisplayHideCursor(kCGDirectMainDisplay);
    }
}

@end
