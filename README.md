# @router360/react-native-vungle

## Getting started

`$ npm install @router360/react-native-vungle --save`

### Mostly automatic installation

`$ react-native link @router360/react-native-vungle`

## Usage
```javascript
import RnVungle from '@router360/react-native-vungle';

// TODO: What to do with the module?
RnVungle;

// Initialize
RnVungle.init(appid);

// Load Ads
RnVungle.loadAds(placementId);

// Show Ads
RnVungle.showAds(placementId,userId,appid);

// Boolean. Returns a promise
RnVungle.isInitialized();
```
