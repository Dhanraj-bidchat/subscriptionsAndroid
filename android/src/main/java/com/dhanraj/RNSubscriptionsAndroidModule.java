
package com.dhanraj;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.uimanager.annotations.ReactProp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RNSubscriptionsAndroidModule extends ReactContextBaseJavaModule
        implements PurchasesUpdatedListener, SkuDetailsResponseListener,BillingClientStateListener {

  private static final String TAG = RNSubscriptionsAndroidModule.class.getSimpleName();
  private final ReactApplicationContext reactContext;
  private BillingClient billingClient;
  private List<String> subscriptionProducts = new ArrayList<>();
  private List<SkuDetails> skuDetails = new ArrayList<>();
  private Callback purchaseCB;

  public RNSubscriptionsAndroidModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }


  @ReactMethod
  public void initBillingClient(String products, final Callback cb) {

    String ProductData  = products.replace("[", "").replace("]", "")
            .replace("\"", "");
    subscriptionProducts = new ArrayList<>(Arrays.asList(ProductData.split(",")));
    billingClient = BillingClient.newBuilder(this.reactContext).setListener(this).build();
    billingClient.startConnection(new BillingClientStateListener() {
      @Override
      public void onBillingSetupFinished(int responseCode) {
        Log.e(TAG, "onBillingSetupFinished: "+responseCode );
        if(responseCode == BillingClient.BillingResponse.OK) {
          // The BillingClient is ready. You can query purchases here.
          cb.invoke(null, "OK");
          Log.e(TAG, "onBillingSetupFinished: "+"BillingClient is ready. You can query purchases here" );
        } else {
          Log.e(TAG, "onBillingSetupFinished: 11" );
          billingClient.endConnection();
          // cb.invoke("BillingClient is not ready", null);

          try {
            Log.e(TAG, "onBillingSetupFinished: 111" );
            cb.invoke(getErrorJson(responseCode), null);
          }catch (Exception e) {
            Log.e(TAG, "onBillingSetupFinished: 112" );
            e.printStackTrace();
          }
        }
      }

      @Override
      public void onBillingServiceDisconnected() {
        Log.e(TAG, "onBillingServiceDisconnected: 1" );
//        purchaseCB.invoke(getErrorJson(3), null);
        billingClient.endConnection();
      }
    });
    Log.e(TAG, "initBillingClient CALING: "+ billingClient.isReady() );
  }

  @Override
  public String getName() {
    return "RNSubscriptionsAndroid";
  }

  @ReactMethod
  public void showLongToast() {
    Toast.makeText(this.reactContext, "This is long toast", Toast.LENGTH_SHORT).show();
  }


  private void loadProducts(final Callback pCallback) {
    Log.e(TAG, "loadSubscriptionProducts CALING: "+ billingClient.isReady()+ " LIST "+ " "+subscriptionProducts );
    if(billingClient.isReady()) {
      SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
      params.setSkusList(subscriptionProducts);
      params.setType(BillingClient.SkuType.SUBS);
      billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
        @Override
        public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
          Log.e(TAG, "onSkuDetailsResponse: "+responseCode+ "skuDetailsList: "+ skuDetailsList );

          // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
          if((skuDetailsList != null) && (skuDetailsList.size() > 0)) {
            skuDetails = new ArrayList<>();
            skuDetails.addAll(skuDetailsList);

          } else {
            //todo: if no products handle
          }
          Log.e(TAG, "loadProducts: productsCallback"+pCallback+ "skuDetails: "+ skuDetails );
          //TODO: FOR FETCH PRODUCTS METHOD
          if(pCallback != null) {
            Log.e(TAG, "ProductsLoaded: productsCallback "+skuDetails.size() );
//            List<String> products = new ArrayList<>();
//            for (int i = 0; i < skuDetails.size(); i++) {
//              SkuDetails item = skuDetails.get(i);
//              products.add(item.toString());
//            }
            pCallback.invoke(skuDetails.toString());
          }


        }
      });
    } else {
      if(pCallback != null) {
        Log.e(TAG, "ProductsLoaded: billing client not ready " );
        pCallback.invoke("[]");
      }
    }
  }

  @ReactMethod
  private void loadSubscriptionProducts(final Callback cb) {
    loadProducts(cb);
  }

  @ReactMethod
  public void subscribeTo(String oldProduct,String productId, int prorationMode, Callback cb) {

    purchaseCB = cb;
    boolean isProductExist = false;
    SkuDetails product = null;
    Log.e(TAG, "subscribeTo: oldProduct: "+oldProduct+" productId: "+productId+" prorationMode: "+prorationMode);
    for (int i = 0; i < skuDetails.size(); i++) {
      SkuDetails details = skuDetails.get(i);
      Log.e(TAG, "subscribeTo: details"+ details.getSku());
      if(details.getSku().equals(productId)) {
//        Toast.makeText(reactContext, "Product exists", Toast.LENGTH_SHORT).show();
        product = skuDetails.get(i);
        isProductExist = true;
        break;
      }
      Log.e(TAG, "subscribeTo: details doing");
    }

    Log.e(TAG, "subscribeTo: details done");
    if(isProductExist) {
//      purchaseCB.invoke(null, "Product Exists");
      Log.e(TAG, "subscribeTo PRODUCT EXISTS " );
      purchaseDigitalProduct(oldProduct,product, prorationMode);
    } else {
      Log.e(TAG, "subscribeTo PRODUCT NOT EXISTS " );
      purchaseCB.invoke("Product not Exist", null);
    }

  }

  @ReactMethod
  public void subscribeToPlan(final String oldProduct, final String productId, final int prorationMode, final Callback cb) {

    purchaseCB = cb;
    if(billingClient.isReady()) {
      SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
      params.setSkusList(subscriptionProducts);
      params.setType(BillingClient.SkuType.SUBS);
      billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
        @Override
        public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
          Log.e(TAG, "onSkuDetailsResponse: "+responseCode+ "skuDetailsList: "+ skuDetailsList );
          // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
          if((skuDetailsList != null) && (skuDetailsList.size() > 0)) {
            skuDetails = new ArrayList<>();
            skuDetails.addAll(skuDetailsList);

            Log.e(TAG, "loadProducts: productsCallback"+ "skuDetails: "+ skuDetails );
            purchaseNow(cb, oldProduct, productId, prorationMode, responseCode);
          } else {
            billingClient.endConnection();
            //todo: if no products handle
            Log.e(TAG, "subscribeTo No Products available " );
            purchaseCB.invoke(getErrorJson(responseCode), null);
          }
        }
      });
    } else {
      Toast.makeText(reactContext, "Billing client not ready yet", Toast.LENGTH_SHORT);
    }

  }


  public void purchaseNow(Callback cb, String oldProduct, String productId, int prorationMode, int responseCode) {

    purchaseCB = cb;
    boolean isProductExist = false;
    SkuDetails product = null;
    Log.e(TAG, "subscribeTo: oldProduct: "+oldProduct+" productId: "+productId+" prorationMode: "+prorationMode);
    for (int i = 0; i < skuDetails.size(); i++) {
      SkuDetails details = skuDetails.get(i);
      Log.e(TAG, "subscribeTo: details"+ details.getSku());
      if(details.getSku().equals(productId)) {
//        Toast.makeText(reactContext, "Product exists", Toast.LENGTH_SHORT).show();
        product = skuDetails.get(i);
        isProductExist = true;
        break;
      }
      Log.e(TAG, "subscribeTo: details doing");
    }

    Log.e(TAG, "subscribeTo: details done");
    if(isProductExist) {
//      purchaseCB.invoke(null, "Product Exists");
      Log.e(TAG, "subscribeTo PRODUCT EXISTS " );
      purchaseDigitalProduct(oldProduct,product, prorationMode);
    } else {
      billingClient.endConnection();
      Log.e(TAG, "subscribeTo PRODUCT NOT EXISTS " );
//      purchaseCB.invoke("Product not Exist", null);
      purchaseCB.invoke(getErrorJson(responseCode), null);
    }

  }

  /**
   *
   * @param productToBuy- product to purchase
   * @param oldProduct - old subscription id used while upgrading or downgrading
   * @param prorationMode - for upgrading and downgrading with price adjustment or not ; default = 1
   * note: pass prorationMode = 2 for upgrading and prorationMode = 4 for downgrading
   */
  public void purchaseDigitalProduct(String oldProduct,SkuDetails productToBuy,  int prorationMode) {
    Log.e(TAG, "purchaseDigitalProduct: oldProduct: "+oldProduct+ " productToBuy: "+ productToBuy.getSku()+ "prorationMode: "+prorationMode );
    BillingFlowParams.Builder flowParams = BillingFlowParams.newBuilder();
    flowParams.setSkuDetails(productToBuy);
    if(oldProduct != null) { // && !oldProduct.equals(productToBuy.getSku())
      Log.e(TAG, "purchaseDigitalProduct: "+ "applying proration" );
      flowParams.setOldSku(oldProduct);
      flowParams.setReplaceSkusProrationMode((prorationMode == 0) ? BillingFlowParams.ProrationMode.IMMEDIATE_WITH_TIME_PRORATION :prorationMode);
    }

    int responseCode2 = billingClient.launchBillingFlow(getReactApplicationContext().getCurrentActivity(), flowParams.build());
    Log.e(TAG, "purchaseDigitalProduct:(0 = OK | 1 = USER CANCELED | 2-8 =ANY OTHER) "+responseCode2 );

  }

  @Override
  public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
    Log.e(TAG, "onPurchasesUpdated: "+ responseCode+ " purchases: "+ purchases );

    if (responseCode == BillingClient.BillingResponse.OK
            && purchases != null) {
      for (Purchase purchase : purchases) {
        handlePurchase(purchase);
//        purchaseCB.invoke(null, purchase.toString());

      }
    } else if(responseCode == BillingClient.BillingResponse.OK) {
      //history
      billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS, new PurchaseHistoryResponseListener() {
        @Override
        public void onPurchaseHistoryResponse(int responseCode, List<Purchase> purchasesList) {
          Log.e(TAG, "onPurchaseHistoryResponse: "+ responseCode+ " purchasesList: "+ purchasesList );
          if(purchasesList != null) {
            Purchase purchase = purchasesList.get(0);
            Log.e(TAG, "onPurchaseHistoryResponse: "+ " purchasesListUpdated: "+ purchase );
            billingClient.endConnection();
            purchaseCB.invoke(null, purchase.getPurchaseToken());
          } else {
            billingClient.endConnection();
            purchaseCB.invoke(getErrorJson(responseCode), null);
          }
        }
      });
    } else {
      billingClient.endConnection();
      Log.e(TAG, "onPurchasesUpdated errorthrown: re" );
      //Error cases
      try {
        purchaseCB.invoke(getErrorJson(responseCode), null);
      } catch (Exception e) {
        Log.e(TAG, "onPurchasesUpdated errorthrown: "+ e.getMessage() );
        e.printStackTrace();
      }
    }
  }

  public String getBillingResponse(int responseCode) {
    String errorMsg = "";
    switch (responseCode) {
      case BillingClient.BillingResponse.USER_CANCELED://1
        errorMsg = "User pressed back or canceled a dialog";
        break;
      case BillingClient.BillingResponse.SERVICE_UNAVAILABLE://2
        errorMsg = "Network connection is down";
        break;
      case BillingClient.BillingResponse.BILLING_UNAVAILABLE://3
        errorMsg = "Billing API version is not supported for the type requested ";
        break;
      case BillingClient.BillingResponse.ITEM_UNAVAILABLE://4
        errorMsg = "Requested product is not available for purchase";
        break;
      case BillingClient.BillingResponse.DEVELOPER_ERROR: //5
        errorMsg = "Invalid arguments provided to the API";
        break;
      case BillingClient.BillingResponse.ERROR: //6
        errorMsg = "Fatal error during the API action";
        break;
      case BillingClient.BillingResponse.ITEM_ALREADY_OWNED: //7
        errorMsg = "Failure to purchase since item is already owned";
        break;
      case BillingClient.BillingResponse.SERVICE_DISCONNECTED: //8
        errorMsg = "Failure to consume since item is not owned ";
        break;
      case BillingClient.BillingResponse.SERVICE_TIMEOUT: //-1
        errorMsg = "The request has reached the maximum timeout before Google Play responds";
        break;
      case BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED: //-2
        errorMsg = "Requested feature is not supported by Play Store on the current device.";
        break;
      default://BillingClient.BillingResponse.OK
        errorMsg = "Purchase Successful";
        break;
    }

    return errorMsg;
  }

  public String getErrorJson(int responseCode) {
    //Error callback here
    JSONObject errorPurchase = new JSONObject();
    try {
      errorPurchase.put("errorCode", responseCode);
      errorPurchase.put("errorMessage", getBillingResponse(responseCode));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    Log.e(TAG, "getErrorJson: "+errorPurchase);
    return errorPurchase.toString();
  }

  private void handlePurchase(Purchase purchase) {
    Log.e(TAG, "handlePurchase success: "+ purchase );
    billingClient.endConnection();
    purchaseCB.invoke(null, purchase.getPurchaseToken());
  }


  @Override
  public void onBillingSetupFinished(int responseCode) {
    Log.e(TAG, "onBillingSetupFinished new: "+responseCode );
  }

  @Override
  public void onBillingServiceDisconnected() {
    Log.e(TAG, "onBillingServiceDisconnected: 2" );
//    purchaseCB.invoke(getErrorJson(3), null);
  }

  @Override
  public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
    Log.e(TAG, "onSkuDetailsResponse: "+ responseCode+ "skuDetailsList: "+ skuDetailsList );
  }

}