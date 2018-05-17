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

#import "CookieJar.h"

@interface CookieJar()

    + (NSString*) getDomainKey: (NSString*) domain;
    + (NSString*) getPathKey: (NSString*) path;
    + (BOOL) keyMatch:(NSString*) key pattern:(NSString*) pattern;

@end

@implementation CookieJar {

    /**
    * Store of the cookies. Contains a directory of domains, paths and cookie names. eg:
    *
    * "www.google.com": {
    *   "/": {
    *     "token": { ... }
    *   }
    * }
    *
    */
    NSMutableDictionary* _domains;

}

#pragma mark Static Helpers

+ (NSString*) getDomainKey: (NSString*) domain {
    domain = [domain stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    domain = [domain lowercaseString];

    NSString* key = @"";
    while (domain.length > 0) {
        NSRange range = [domain rangeOfString:@"." options:NSBackwardsSearch];
        if (range.length == 0) {
            break;
        }
        key = [key stringByAppendingFormat:@"%@\t", [domain substringFromIndex:range.location+1]];
        domain = [domain substringToIndex:range.location];
    }
    key = [key stringByAppendingString:domain];
    return key;
}

+ (NSString*) getPathKey: (NSString*) path {
    if ([path characterAtIndex:path.length -1] != '/') {
        path = [path stringByAppendingString:@"/"];
    }
    return [path stringByReplacingOccurrencesOfString:@"/" withString:@"\t"];
}


+ (BOOL) keyMatch:(NSString*) key pattern:(NSString*) pattern {
    if ([key isEqualToString:pattern]) {
        return true;
    }
    NSRange r = [key rangeOfString:pattern];
    if (r.length == 0 || r.location != 0) {
        return false;
    }
    return [key characterAtIndex:r.length - 1] == '\t';
}

#pragma mark Initialization

- (instancetype) init {
    self = [super init];
    if (self) {
        _domains = [NSMutableDictionary dictionary];
    }
    return self;
}

#pragma mark Cookie storage

- (void) putCookie:(NSHTTPCookie*) cookie {
    // only domain names are case insensitive
    NSString* domainKey = [CookieJar getDomainKey:cookie.domain];
    NSString* pathKey = [CookieJar getPathKey:cookie.path];
    NSString* name = [cookie name];

    NSMutableDictionary* paths = _domains[domainKey];
    if (!paths) {
        paths = _domains[domainKey] = [NSMutableDictionary dictionary];
    }
    NSMutableDictionary* cookies = paths[pathKey];
    if (!cookies) {
        cookies = paths[pathKey] = [NSMutableDictionary dictionary];
    }
    cookies[name] = cookie;
}

- (void) putCookies:(NSArray*) cookies {
    for (NSHTTPCookie* cookie in cookies) {
        [self putCookie:cookie];
    }
}

- (NSArray*) cookies {
    NSMutableArray* resultCookies = [NSMutableArray array];
    for (NSDictionary* domains in [_domains allValues]) {
        for (NSDictionary* paths in [domains allValues]) {
            [resultCookies addObjectsFromArray:[paths allValues]];
        }
    }
    return resultCookies;
}

- (NSDictionary*) cookiesForURL:(NSURL*) url {
    if (!url.host) {
        return nil;
    }
    NSString* host = [CookieJar getDomainKey:url.host];
    NSString* path = [CookieJar getPathKey:url.path];
    BOOL secure = [url.scheme isEqualToString:@"https"];
    NSDate* now = [NSDate date];

    NSMutableDictionary* resultCookies = [NSMutableDictionary dictionary];
    for (NSString* domain in [[_domains allKeys] sortedArrayUsingSelector:@selector(compare:)]) {
        if ([CookieJar keyMatch:host pattern:domain]) {
            NSDictionary* pathIdx = _domains[domain];
            for (NSString* cookiePath in [[pathIdx allKeys] sortedArrayUsingSelector:@selector(compare:)]) {
                // "The request-uri's path path-matches the cookie's path."
                if (![CookieJar keyMatch:path pattern:cookiePath]) {
                    continue;
                }
                NSMutableDictionary* cookieIdx = pathIdx[cookiePath];
                for (NSHTTPCookie* c in [cookieIdx allValues]) {
                    // "If the cookie's secure-only-flag is true, then the request-uri's
                    // scheme must denote a "secure" protocol"
                    if ([c isSecure] && !secure) {
                        continue;
                    }

                    // deferred from S5.3
                    // non-RFC: allow retention of expired cookies by choice
                    if ([c.expiresDate timeIntervalSinceDate:now] < 0) {
                        [cookieIdx removeObjectForKey:c.name];
                        continue;
                    }
                    resultCookies[c.name] = c;
                }
            }
        }
    }
    return resultCookies;
}

- (void) clear {
    [_domains removeAllObjects];
}

#pragma mark Request / Response Handling

- (void) handleCookiesInRequest:(NSMutableURLRequest*) request {
    NSURL* url = request.URL;
    NSArray* cookies = [[self cookiesForURL:url] allValues];
    NSDictionary* headers = [NSHTTPCookie requestHeaderFieldsWithCookies:cookies];

    NSUInteger count = [headers count];
    __unsafe_unretained id keys[count], values[count];
    [headers getObjects:values andKeys:keys];

    for (NSUInteger i = 0; i < count; i++) {
        [request setValue:values[i] forHTTPHeaderField:keys[i]];
    }
}


- (void) handleCookiesInResponse:(NSHTTPURLResponse*) response {
    NSURL* url = response.URL;
    NSArray* cookies = [NSHTTPCookie cookiesWithResponseHeaderFields:response.allHeaderFields forURL:url];
    [self putCookies:cookies];
}


@end
