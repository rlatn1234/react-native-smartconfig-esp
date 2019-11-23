# react-native-smartconfig-p

## Getting started

`$ npm install react-native-smart-config-p --save`
`or`
`$ yarn add react-native-smart-config-p --save`

### Mostly automatic installation

  ## * React Native 0.60+
   [CLI autolink feature](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md)`links the module while building the app.`
   
   `For iOS using` ```cocoapods**```, run:`
   `$ cd ios/ && pod install`

  ## * React Native <= 0.59

   `$ react-native link react-native-smartconfig-p`

### Manual installation


#### iOS

   1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
   2. Go to `node_modules` ➜ `react-native-smartconfig-p` and add `RnSmartConfigP.xcodeproj`
   3. In XCode, in the project navigator, select your project. Add `libRnSmartConfigP.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
   4. Run your project (`Cmd+R`)<

#### Android

   1. Open up `android/app/src/main/java/[...]/MainApplication.java`
	- Add `import com.phuong.smartconfigp.RnSmartConfigPPackage;` to the imports at the top of the file
	- Add `new RnSmartConfigPPackage()` to the list returned by the `getPackages()` method
   2. Append the following lines to `android/settings.gradle`:
		```
		include ':react-native-smartconfig-p'
		project(':react-native-smartconfig-p').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-smartconfig-p/android')
		```
   3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
		```
		implementation project(':react-native-smartconfig-p')
		```


## Usage
```javascript
import SmartConfigP from 'react-native-smartconfig-p';

 SmartConfigP.start({
	ssid: "WiFi Name",
    password: "Password",
    bssid: "MAC", // Mac address of Mobile
    count: 1,     //Number Esp
    cast: 'broadcast'   // boardcast or multicast
 }).then(data => {
	console.log(data);
	/*[
		{
		'bssid': 'device-bssi1', //device bssid
		'ipv4': '192.168.1.11' //local ip address
		},
		{
		'bssid': 'device-bssi2', //device bssid
		'ipv4': '192.168.1.12' //local ip address
		},
		...
	]*/
 }).catch(err => {
	 // if smart config fail after timeout
 });

 // if want cancle, use:
  SmartConfigP.stop();
```
