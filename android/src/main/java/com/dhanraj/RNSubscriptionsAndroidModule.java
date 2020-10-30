
package com.dhanraj;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RNSubscriptionsAndroidModule extends ReactContextBaseJavaModule
    implements PurchasesUpdatedListener, SkuDetailsResponseListener, BillingClientStateListener {

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

    String ProductData = products.replace("[", "").replace("]", "").replace("\"", "");
    subscriptionProducts = new ArrayList<>(Arrays.asList(ProductData.split(",")));
    billingClient = BillingClient.newBuilder(this.reactContext).setListener(this).enablePendingPurchases().build();
    billingClient.startConnection(new BillingClientStateListener() {

      @Override
      public void onBillingSetupFinished(BillingResult billingResult) {
        Log.e(TAG, "onBillingSetupFinished: " + billingResult.getResponseCode());
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
          // The BillingClient is ready. You can query purchases here.
          cb.invoke(null, "OK");
          Log.e(TAG, "onBillingSetupFinished: " + "BillingClient is ready. You can query purchases here");
        } else {
          Log.e(TAG, "onBillingSetupFinished: 11");
          billingClient.endConnection();
          // cb.invoke("BillingClient is not ready", null);

          try {
            Log.e(TAG, "onBillingSetupFinished: 111");
            cb.invoke(getErrorJson(billingResult), null);
          } catch (Exception e) {
            Log.e(TAG, "onBillingSetupFinished: 112");
            e.printStackTrace();
          }
        }
      }

      @Override
      public void onBillingServiceDisconnected() {
        Log.e(TAG, "onBillingServiceDisconnected: 1");
        // purchaseCB.invoke(getErrorJson(3), null);
        billingClient.endConnection();
      }
    });
    Log.e(TAG, "initBillingClient CALING: " + billingClient.isReady());
  }

  @ReactMethod
  public void checkBilling(final Callback cb) {

    billingClient = BillingClient.newBuilder(this.reactContext).setListener(this).enablePendingPurchases().build();
    billingClient.startConnection(new BillingClientStateListener() {

      @Override
      public void onBillingSetupFinished(BillingResult billingResult) {
        Purchase.PurchasesResult purchasesResult = null;
        Log.e(TAG, "onBillingSetupFinished: " + billingResult.getResponseCode());
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
          // The BillingClient is ready. You can query purchases here.
          purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
          Log.e(TAG, "sailee2 purchasesResult querypurchase: " + " purchasesResult: "
              + purchasesResult.getResponseCode() + "list: " + purchasesResult.getPurchasesList());

          billingClient.endConnection();
          // try {
          //   Purchase purchase = new Purchase();
          // } catch (JSONException e) {
          //   e.printStackTrace();
          // }
          if (purchasesResult.getPurchasesList().size() == 0) {
            cb.invoke(null, null);
          } else {
            cb.invoke(null, purchasesResult.getPurchasesList().get(0).getPurchaseToken());
          }

        } else {
          Log.e(TAG, "onBillingSetupFinished: 11");
          billingClient.endConnection();
          // cb.invoke("BillingClient is not ready", null);

          try {
            Log.e(TAG, "onBillingSetupFinished: 111");
            cb.invoke(getErrorJson(billingResult), null);
          } catch (Exception e) {
            Log.e(TAG, "onBillingSetupFinished: 112");
            e.printStackTrace();
          }
        }
      }

      @Override
      public void onBillingServiceDisconnected() {
        Log.e(TAG, "onBillingServiceDisconnected: 1");
        // purchaseCB.invoke(getErrorJson(3), null);
        billingClient.endConnection();
      }
    });
    Log.e(TAG, "initBillingClient CALING: " + billingClient.isReady());
  }

  @Override
  public void onBillingSetupFinished(BillingResult billingResult) {
    Log.e(TAG, "onBillingSetupFinished new: " + billingResult);
  }

  @Override
  public void onBillingServiceDisconnected() {
    Log.e(TAG, "onBillingServiceDisconnected: 2");
    // purchaseCB.invoke(getErrorJson(3), null);
  }

  @Override
  public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
    Log.e(TAG, "onPurconPurchasesUpdatedhasesUpdated: " + billingResult.getResponseCode() + " purchases: " + purchases);

    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
      Purchase p1 = purchases.get(0);
      if (!p1.isAcknowledged()) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(p1.getPurchaseToken())
            // .setDeveloperPayload(p1.getDeveloperPayload())
            .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
          @Override
          public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
              Log.e(TAG, "acknowledged billing result: ");
            }
          }
        });
      }
      for (Purchase purchase : purchases) {
        handlePurchase(purchase);
        // purchaseCB.invoke(null, purchase.toString());

      }
    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
      // history
      billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS, new PurchaseHistoryResponseListener() {
        @Override
        public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> purchasesList) {
          Log.e(TAG,
              "onPurchaseHistoryResponse: " + billingResult.getResponseCode() + " purchasesList: " + purchasesList);
          if (purchasesList != null) {
            PurchaseHistoryRecord purchase = purchasesList.get(0);
            Log.e(TAG, "onPurchaseHistoryResponse: " + " purchasesListUpdated: " + purchase);
            billingClient.endConnection();
            purchaseCB.invoke(null, purchase.getPurchaseToken());
          } else {
            billingClient.endConnection();
            purchaseCB.invoke(getErrorJson(billingResult), null);
          }
        }
      });
    } else {
      billingClient.endConnection();
      Log.e(TAG, "onPurchasesUpdated errorthrown: re" );
      //Error cases
      try {
        purchaseCB.invoke(getErrorJson(billingResult), null);
      } catch (Exception e) {
        Log.e(TAG, "onPurchasesUpdated errorthrown: "+ e.getMessage() );
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
    Log.e(TAG, "onSkuDetailsResponse: " + billingResult + "skuDetailsList: " + skuDetailsList);
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
    Log.e(TAG, "loadSubscriptionProducts CALING: " + billingClient.isReady() + " LIST " + " " + subscriptionProducts);
    if (billingClient.isReady()) {
      SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
      params.setSkusList(subscriptionProducts);
      params.setType(BillingClient.SkuType.SUBS);
      billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
        @Override
        public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
          Log.e(TAG, "onSkuDetailsResponse: " + billingResult + "skuDetailsList: " + skuDetailsList);

          // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
          if ((skuDetailsList != null) && (skuDetailsList.size() > 0)) {
            skuDetails = new ArrayList<>();
            skuDetails.addAll(skuDetailsList);

          } else {
            // todo: if no products handle
          }
          Log.e(TAG, "loadProducts: productsCallback" + pCallback + "skuDetails: " + skuDetails);
          // TODO: FOR FETCH PRODUCTS METHOD
          if (pCallback != null) {
            Log.e(TAG, "ProductsLoaded: productsCallback " + skuDetails.size());
            // List<String> products = new ArrayList<>();
            // for (int i = 0; i < skuDetails.size(); i++) {
            // SkuDetails item = skuDetails.get(i);
            // products.add(item.toString());
            // }
            pCallback.invoke(skuDetails.toString());
          }
        }
      });
    } else {
      if (pCallback != null) {
        Log.e(TAG, "ProductsLoaded: billing client not ready ");
        pCallback.invoke("[]");
      }
    }
  }

  @ReactMethod
  private void loadSubscriptionProducts(final Callback cb) {
    loadProducts(cb);
  }

  @ReactMethod
  public void subscribeTo(String oldProduct, String transactionReceipt, String productId, int prorationMode,
      Callback cb) {

    purchaseCB = cb;
    boolean isProductExist = false;
    SkuDetails product = null;
    Log.e(TAG,
        "subscribeTo: oldProduct: " + oldProduct + " productId: " + productId + " prorationMode: " + prorationMode);
    for (int i = 0; i < skuDetails.size(); i++) {
      SkuDetails details = skuDetails.get(i);
      Log.e(TAG, "subscribeTo: details" + details.getSku());
      if (details.getSku().equals(productId)) {
        // Toast.makeText(reactContext, "Product exists", Toast.LENGTH_SHORT).show();
        product = skuDetails.get(i);
        isProductExist = true;
        break;
      }
      Log.e(TAG, "subscribeTo: details doing");
    }

    Log.e(TAG, "subscribeTo: details done");
    if (isProductExist) {
      // purchaseCB.invoke(null, "Product Exists");
      Log.e(TAG, "subscribeTo PRODUCT EXISTS ");
      purchaseDigitalProduct(oldProduct, product, prorationMode, transactionReceipt);
    } else {
      Log.e(TAG, "subscribeTo PRODUCT NOT EXISTS ");
      purchaseCB.invoke("Product not Exist", null);
    }

  }

  @ReactMethod
  public void subscribeToPlan(final String oldProduct, final String transactionReceipt, final String productId,
      final int prorationMode, final Callback cb) {

    purchaseCB = cb;
    if (billingClient.isReady()) {
      SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
      params.setSkusList(subscriptionProducts);
      params.setType(BillingClient.SkuType.SUBS);
      billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
        @Override
        public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
          Log.e(TAG, "onSkuDetailsResponse: " + billingResult + "skuDfetailsList: " + skuDetailsList);
          // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
          if ((skuDetailsList != null) && (skuDetailsList.size() > 0)) {
            skuDetails = new ArrayList<>();
            skuDetails.addAll(skuDetailsList);

            Log.e(TAG, "loadProducts: productsCallback" + "skuDetails: " + skuDetails);
            purchaseNow(cb, oldProduct, productId, prorationMode, billingResult, transactionReceipt);
          } else {
            billingClient.endConnection();
            // todo: if no products handle
            Log.e(TAG, "subscribeTo No Products available ");
            purchaseCB.invoke(getErrorJson(billingResult), null);
          }
        }

        // @Override
        // public void onSkuDetailsResponse(int responseCode, List<SkuDetails>
        // skuDetailsList) {
        //
        // }
      });
    } else {
      Toast.makeText(reactContext, "Billing client not ready yet", Toast.LENGTH_SHORT);
    }

  }

  public void purchaseNow(Callback cb, String oldProduct, String productId, int prorationMode,
      BillingResult responseCode, String transactionReceipt) {

    purchaseCB = cb;
    boolean isProductExist = false;
    SkuDetails product = null;
    Log.e(TAG,
        "subscribeTo: oldProduct: " + oldProduct + " productId: " + productId + " prorationMode: " + prorationMode);
    for (int i = 0; i < skuDetails.size(); i++) {
      SkuDetails details = skuDetails.get(i);
      Log.e(TAG, "subscribeTo: details" + details.getSku());
      if (details.getSku().equals(productId)) {
        // Toast.makeText(reactContext, "Product exists", Toast.LENGTH_SHORT).show();
        product = skuDetails.get(i);
        isProductExist = true;
        break;
      }
      Log.e(TAG, "subscribeTo: details doing");
    }

    Log.e(TAG, "subscribeTo: details done");
    if (isProductExist) {
      // purchaseCB.invoke(null, "Product Exists");
      Log.e(TAG, "subscribeTo PRODUCT EXISTS ");
      purchaseDigitalProduct(oldProduct, product, prorationMode, transactionReceipt);
    } else {
      billingClient.endConnection();
      Log.e(TAG, "subscribeTo PRODUCT NOT EXISTS ");
      // purchaseCB.invoke("Product not Exist", null);
      purchaseCB.invoke(getErrorJson(responseCode), null);
    }

  }

  /**
   *
   * @param               productToBuy- product to purchase
   * @param oldProduct    - old subscription id used while upgrading or
   *                      downgrading
   * @param prorationMode - for upgrading and downgrading with price adjustment or
   *                      not ; default = 1 note: pass prorationMode = 2 for
   *                      upgrading and prorationMode = 4 for downgrading
   */
  public void purchaseDigitalProduct(String oldProduct, SkuDetails productToBuy, int prorationMode,
      String transactionReceipt) {
    Log.e(TAG, "purchaseDigitalProduct: oldProduct: " + oldProduct + " productToBuy: " + productToBuy.getSku()
        + "prorationMode: " + prorationMode + "transactionReceipt:" + transactionReceipt);
    BillingFlowParams.Builder flowParams = BillingFlowParams.newBuilder();
    flowParams.setSkuDetails(productToBuy);
    if (oldProduct != null) { // && !oldProduct.equals(productToBuy.getSku())
      Log.e(TAG, "purchaseDigitalProduct: " + "applying proration");
      flowParams.setOldSku(oldProduct, transactionReceipt);
      flowParams.setReplaceSkusProrationMode(
          (prorationMode == 0) ? BillingFlowParams.ProrationMode.IMMEDIATE_WITH_TIME_PRORATION : prorationMode);
    }

    BillingResult responseCode2 = billingClient.launchBillingFlow(getReactApplicationContext().getCurrentActivity(),
        flowParams.build());
    Log.e(TAG,
        "purchaseDigitalProduct:(0 = OK | 1 = USER CANCELED | 2-8 =ANY OTHER) " + responseCode2.getResponseCode());

  }

  public String getBillingResponse(int responseCode) {
    String errorMsg = "";
    switch (responseCode) {
    case BillingClient.BillingResponseCode.USER_CANCELED:// 1
      errorMsg = "User pressed back or canceled a dialog";
      break;
    case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:// 2
      errorMsg = "Network connection is down";
      break;
    case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:// 3
      errorMsg = "Billing API version is not supported for the type requested ";
      break;
    case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:// 4
      errorMsg = "Requested product is not available for purchase";
      break;
    case BillingClient.BillingResponseCode.DEVELOPER_ERROR: // 5
      errorMsg = "Invalid arguments provided to the API";
      break;
    case BillingClient.BillingResponseCode.ERROR: // 6
      errorMsg = "Fatal error during the API action";
      break;
    case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED: // 7
      errorMsg = "Failure to purchase since item is already owned";
      break;
    case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED: // 8
      errorMsg = "Failure to consume since item is not owned ";
      break;
    case BillingClient.BillingResponseCode.SERVICE_TIMEOUT: // -1
      errorMsg = "The request has reached the maximum timeout before Google Play responds";
      break;
    case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED: // -2
      errorMsg = "Requested feature is not supported by Play Store on the current device.";
      break;
    default:// BillingClient.BillingResponse.OK
      errorMsg = "Purchase Successful";
      break;
    }

    return errorMsg;
  }

  public String getErrorJson(BillingResult responseCode) {
    // Error callback here
    JSONObject errorPurchase = new JSONObject();
    try {
      errorPurchase.put("errorCode", responseCode);
      errorPurchase.put("errorMessage", getBillingResponse(responseCode.getResponseCode()));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    Log.e(TAG, "getErrorJson: " + errorPurchase);
    return errorPurchase.toString();
  }

  private void handlePurchase(Purchase purchase) {
    Log.e(TAG, "handlePurchase success: " + purchase);
    billingClient.endConnection();
    purchaseCB.invoke(null, purchase.getPurchaseToken());
  }
}