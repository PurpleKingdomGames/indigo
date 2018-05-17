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

#import "CDVConsole.h"


@implementation CDVConsole


- (void) log:(NSString*) message {
    NSLog(@"%@", message);
}

- (void) trace:(NSString*) message {
    NSLog(@"trace: %@", message);
}

- (void) debug:(NSString*) message {
    NSLog(@"debug: %@", message);
}

- (void) info:(NSString*) message {
    NSLog(@"info: %@", message);
}

- (void) warn:(NSString*) message {
    NSLog(@"warn: %@", message);
}

- (void) error:(NSString*) message {
    NSLog(@"error: %@", message);
}

#pragma mark WebScripting Protocol

/* checks whether a selector is acceptable to be called from JavaScript */
+ (BOOL) isSelectorExcludedFromWebScript:(SEL) sel {
    return sel != @selector(log:) &&
            sel != @selector(trace:) &&
            sel != @selector(debug:) &&
            sel != @selector(info:) &&
            sel != @selector(warn:) &&
            sel != @selector(error:);
}

/* helper function so we don't have to have underscores and stuff in js to refer to the right method */
+ (NSString*) webScriptNameForSelector:(SEL) sel {
    if (sel == @selector(log:)) {
        return @"log";
    } else if (sel == @selector(trace:)) {
        return @"trace";
    } else if (sel == @selector(debug:)) {
        return @"debug";
    } else if (sel == @selector(info:)) {
        return @"info";
    } else if (sel == @selector(warn:)) {
        return @"warn";
    } else if (sel == @selector(error:)) {
        return @"error";
    } else {
        return nil;
    }
}

// right now exclude all properties (eg keys)
+ (BOOL) isKeyExcludedFromWebScript:(const char*) name {
    return YES;
}

@end
