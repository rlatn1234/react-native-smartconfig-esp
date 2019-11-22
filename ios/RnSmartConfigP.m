#import "RnSmartConfigP.h"


@interface RnSmartConfigP : NSObject<RCTBridgeModule>

@property (nonatomic, retain) NSMutableDictionary *options;
@property (nonatomic, strong) NSCondition *_condition;

@property (atomic, strong) ESPTouchTask *_esptouchTask;
@end

@implementation RnSmartConfigP

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(stop) {
    [self cancel];
}

RCT_EXPORT_METHOD(start:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [self cancel];
    self.options = options;
    
    NSString *apSsid = [self.options valueForKey:@"ssid"];
    NSString *apPwd = [self.options valueForKey:@"password"];
    NSString *apBssid = [self.options valueForKey:@"bssid"];
    int taskCount = [[self.options valueForKey:@"count"] intValue];
    NSString *cast = [self.options valueForKey:@"cast"];
    BOOL broadcast = [cast  isEqual: @"broadcast"] ? YES : NO;
    
    dispatch_queue_t  queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    dispatch_async(queue, ^{
        
        NSArray *esptouchResultArray = [self executeForResultsWithSsid:apSsid bssid:apBssid password:apPwd taskCount:taskCount broadcast:broadcast];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            ESPTouchResult *firstResult = [esptouchResultArray objectAtIndex:0];

            if (!firstResult.isCancelled)
            {
                NSMutableArray *ret = [[NSMutableArray alloc] init];

                if ([firstResult isSuc])
                {
                    for (int i = 0; i < [esptouchResultArray count]; ++i)
                    {
                        ESPTouchResult *resultInArray = [esptouchResultArray objectAtIndex:i];

                        unsigned char *ipBytes = (unsigned char *)[[resultInArray ipAddrData] bytes];
                    
                        NSString *ipv4String = [NSString stringWithFormat:@"%d.%d.%d.%d", ipBytes[0], ipBytes[1], ipBytes[2], ipBytes[3]];
                    
                        NSDictionary *respData = @{@"bssid": [resultInArray bssid], @"ipv4": ipv4String};
                    
                        [ret addObject: respData];

                        if (![resultInArray isSuc]) break;
                    }
                    resolve(ret);
                }
                else
                {
                    reject(RCTErrorUnspecified, nil, RCTErrorWithMessage(@"Timoutout or not Found"));
                }
            }
            else
            {
                reject(RCTErrorUnspecified, nil, RCTErrorWithMessage(@"Timoutout or not Found"));
            }
        });
    });
}

#pragma mark - the example of how to cancel the executing task

- (void) cancel
{
    [self._condition lock];
    if (self._esptouchTask != nil)
    {
        [self._esptouchTask interrupt];
    }
    [self._condition unlock];
}


#pragma mark - the example of how to use executeForResults
- (NSArray *) executeForResultsWithSsid:(NSString *)apSsid bssid:(NSString *)apBssid password:(NSString *)apPwd taskCount:(int)taskCount broadcast:(BOOL)broadcast
{
    [self._condition lock];
    self._esptouchTask = [[ESPTouchTask alloc]initWithApSsid:apSsid andApBssid:apBssid andApPwd:apPwd];

    //[self._esptouchTask setEsptouchDelegate:self._esptouchDelegate];
    [self._esptouchTask setPackageBroadcast:broadcast];
    [self._condition unlock];
    NSArray * esptouchResults = [self._esptouchTask executeForResults:taskCount];
    NSLog(@"ESPViewController executeForResult() result is: %@",esptouchResults);
    return esptouchResults;
}

@end