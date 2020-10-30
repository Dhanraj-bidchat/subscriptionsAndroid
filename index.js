
import { NativeModules } from 'react-native';

const { RNSubscriptionsAndroid } = NativeModules;

class InAppBilling {

    static initBilling(products, callback) {
        return RNSubscriptionsAndroid.initBillingClient(products,callback);
      }

      static getProducts(callback) {
        return RNSubscriptionsAndroid.loadSubscriptionProducts(callback);
      }

      static subscribeTo(oldProductId = null,productId,prorationMode = 1, callback,purchasetoken) {
        return RNSubscriptionsAndroid.subscribeTo(oldProductId,purchasetoken,productId,prorationMode, callback);
      }

      static subscribeToPlan(oldProductId = null, purchasetoken = null,productId,prorationMode = 1, callback) {
        return RNSubscriptionsAndroid.subscribeToPlan(oldProductId,purchasetoken,productId,prorationMode, callback);
      }

      static checkBilling(callback) {
        return RNSubscriptionsAndroid.checkBilling(callback);
      }

}

module.exports = InAppBilling;


// export default RNSubscriptionsAndroid;
