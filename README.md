
# react-native-subscriptions-android

## Getting started

`$ npm install react-native-subscriptions-android --save`

### Mostly automatic installation

`$ react-native link react-native-subscriptions-android`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNSubscriptionsAndroidPackage;` to the imports at the top of the file
  - Add `new RNSubscriptionsAndroidPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-subscriptions-android'
  	project(':react-native-subscriptions-android').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-subscriptions-android/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-subscriptions-android')
  	```


## Usage
```javascript
import RNSubscriptionsAndroid from 'react-native-subscriptions-android';

// TODO: What to do with the module?
RNSubscriptionsAndroid;
```
  