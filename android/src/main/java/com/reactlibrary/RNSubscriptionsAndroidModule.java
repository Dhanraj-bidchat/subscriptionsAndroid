
package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import android.widget.Toast;

public class RNSubscriptionsAndroidModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNSubscriptionsAndroidModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNSubscriptionsAndroid";
  }

  @ReactMethod
  public void showLongToast() {
    Toast.makeText(reactContext, "This is long toast", Toast.LENGTH_SHORT).show();
  }
}