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

#import "NSScreen+Utils.h"

@implementation NSScreen (Utils)

+ (NSArray*) sortedScreens {
    NSArray* sortedScreens = [[NSScreen screens] sortedArrayUsingComparator:^NSComparisonResult(NSScreen* screen1, NSScreen* screen2) {
        if (screen1.frame.origin.x > screen2.frame.origin.x) {
            return (NSComparisonResult) NSOrderedDescending;
        }

        if (screen1.frame.origin.x < screen2.frame.origin.x) {
            return (NSComparisonResult) NSOrderedAscending;
        }

        return (NSComparisonResult) NSOrderedSame;
    }];
    return sortedScreens;
}

+ (NSArray*) sortedScreenRects {
    NSMutableArray* screenWidths = [[NSMutableArray alloc] init];
    NSRect mainScreenRect;

    mainScreenRect = [[NSScreen mainScreen] frame];
    [screenWidths addObject:[NSValue valueWithRect:mainScreenRect]];

    NSArray* sortedScreens = [self sortedScreens];

    for (NSScreen* screen in sortedScreens) {
        if ([screen isNotEqualTo:[NSScreen mainScreen]] &&
                screen.frame.origin.y + screen.frame.size.height == mainScreenRect.origin.y + mainScreenRect.size.height &&
                screen.frame.origin.x > mainScreenRect.origin.x) {
            [screenWidths addObject:[NSValue valueWithRect:screen.frame]];
        }
    }

    return screenWidths;
}

+ (NSRect) fullScreenRect {
    NSArray* screenRects = [self sortedScreenRects];
    NSRect rect = [[NSScreen mainScreen] frame];
    CGFloat finalWidth = 0.0f;

    for (NSValue* rectValue in screenRects) {
        finalWidth += [rectValue rectValue].size.width;
    }

    rect.size.width = finalWidth;

    return rect;
}

@end
