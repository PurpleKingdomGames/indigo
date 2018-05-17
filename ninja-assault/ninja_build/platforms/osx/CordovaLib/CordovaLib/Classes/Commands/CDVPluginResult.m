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

#import "CDVPluginResult.h"
#import "CDVJSON.h"

@interface CDVPluginResult ()

- (CDVPluginResult*) initWithStatus:(CDVCommandStatus) statusOrdinal message:(id) theMessage;

@end

@implementation CDVPluginResult
@synthesize status, message, keepCallback, associatedObject;

static NSArray* org_apache_cordova_CommandStatusMsgs;

id messageFromArrayBuffer(NSData* data) {
    return @{
            @"CDVType" : @"ArrayBuffer",
            @"data" : [data base64EncodedStringWithOptions:0]
    };
}

id massageMessage(id message) {
    if ([message isKindOfClass:[NSData class]]) {
        return messageFromArrayBuffer(message);
    }
    return message;
}

id messageFromMultipart(NSArray* theMessages) {
    NSMutableArray* messages = [NSMutableArray arrayWithArray:theMessages];

    for (NSUInteger i = 0; i < messages.count; ++i) {
        messages[i] = massageMessage(messages[i]);
    }

    return @{
            @"CDVType" : @"MultiPart",
            @"messages" : messages
    };
}

+ (void) initialize {
    org_apache_cordova_CommandStatusMsgs = @[
            @"No result",
            @"OK",
            @"Class not found",
            @"Illegal access",
            @"Instantiation error",
            @"Malformed url",
            @"IO error",
            @"Invalid action",
            @"JSON error",
            @"Error"
    ];
}

- (CDVPluginResult*) init {
    return [self initWithStatus:CDVCommandStatus_NO_RESULT message:nil];
}

- (CDVPluginResult*) initWithStatus:(CDVCommandStatus) statusOrdinal message:(id) theMessage {
    self = [super init];
    if (self) {
        status = @(statusOrdinal);
        message = theMessage;
        keepCallback = @NO;
    }
    return self;
}

+ (CDVPluginResult*) resultWithStatus:(CDVCommandStatus) statusOrdinal {
    return [[self alloc] initWithStatus:statusOrdinal message:nil];
}

+ (CDVPluginResult*) resultWithStatus:(CDVCommandStatus) statusOrdinal messageAsString:(NSString*) theMessage {
    return [[self alloc] initWithStatus:statusOrdinal message:theMessage];
}

+ (CDVPluginResult*) resultWithStatus:(CDVCommandStatus) statusOrdinal messageAsArray:(NSArray*) theMessage {
    return [[self alloc] initWithStatus:statusOrdinal message:theMessage];
}

+ (CDVPluginResult*) resultWithStatus:(CDVCommandStatus) statusOrdinal messageAsInt:(int) theMessage {
    return [[self alloc] initWithStatus:statusOrdinal message:@(theMessage)];
}

+ (CDVPluginResult*) resultWithStatus:(CDVCommandStatus) statusOrdinal messageAsDouble:(double) theMessage {
    return [[self alloc] initWithStatus:statusOrdinal message:@(theMessage)];
}

+ (CDVPluginResult*) resultWithStatus:(CDVCommandStatus) statusOrdinal messageAsBool:(BOOL) theMessage __unused {
    return [[self alloc] initWithStatus:statusOrdinal message:@(theMessage)];
}

+ (CDVPluginResult*) resultWithStatus:(CDVCommandStatus) statusOrdinal messageAsDictionary:(NSDictionary*) theMessage {
    return [[self alloc] initWithStatus:statusOrdinal message:theMessage];
}

+ (CDVPluginResult*) resultWithStatus:(CDVCommandStatus) statusOrdinal messageAsArrayBuffer:(NSData*) theMessage {
    return [[self alloc] initWithStatus:statusOrdinal message:messageFromArrayBuffer(theMessage)];
}

+ (CDVPluginResult*) resultWithStatus:(CDVCommandStatus) statusOrdinal messageAsMultipart:(NSArray*) theMessages {
    return [[self alloc] initWithStatus:statusOrdinal message:messageFromMultipart(theMessages)];
}

+ (CDVPluginResult*) resultWithStatus:(CDVCommandStatus) statusOrdinal messageToErrorObject:(int) errorCode {
    NSDictionary* errDict = @{@"code" : @(errorCode)};

    return [[self alloc] initWithStatus:statusOrdinal message:errDict];
}

- (void) setKeepCallbackAsBool:(BOOL) bKeepCallback {
    [self setKeepCallback:@(bKeepCallback)];
}

- (NSString*) argumentsAsJSON {
    id arguments = (self.message == nil ? [NSNull null] : self.message);
    NSArray* argumentsWrappedInArray = @[arguments];

    NSString* argumentsJSON = [argumentsWrappedInArray JSONString];

    argumentsJSON = [argumentsJSON substringWithRange:NSMakeRange(1, [argumentsJSON length] - 2)];

    return argumentsJSON;
}

static BOOL gIsVerbose = NO;

+ (void) setVerbose:(BOOL) verbose {
    gIsVerbose = verbose;
}

+ (BOOL) isVerbose {
    return gIsVerbose;
}

@end
