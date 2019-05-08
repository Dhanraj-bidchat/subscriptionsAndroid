
import { NativeModules } from 'react-native';

const { RNSubscriptionsAndroid } = NativeModules;

class InAppBilling {

    static initBilling(products, callback) {
        return RNSubscriptionsAndroid.initBillingClient(products,callback);
      }

      static getProducts(callback) {
        return RNSubscriptionsAndroid.loadSubscriptionProducts(callback);
      }

      static subscribeTo(oldProductId = null,productId,prorationMode = 1, callback) {
        return RNSubscriptionsAndroid.subscribeTo(oldProductId,productId,prorationMode, callback);
      }

}

module.exports = InAppBilling;


// export default RNSubscriptionsAndroid;
