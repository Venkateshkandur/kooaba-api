//
//  SampleViewController.h
//  kooabaQuerySample
//
//  Created by Joachim Fornallaz on 18.09.09.
//  Copyright kooaba AG 2009. All rights reserved.
//
//  Permission is given to use this source code file, free of charge, in any
//  project, commercial or otherwise, entirely at your risk, with the condition
//  that any redistribution (in part or whole) of source code must retain
//  this copyright and permission notice. 
//

#import <UIKit/UIKit.h>

@interface SampleViewController : UIViewController {
	IBOutlet UIImagePickerController *imagePickerController;
	IBOutlet UITextView *textView;
}

- (IBAction)takePicture;

@end
