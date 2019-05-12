/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jcapp.mapwallpaper;

import android.content.SharedPreferences;
import android.util.Log;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.Purchase;
import com.jcapp.mapwallpaper.billing.BillingManager;
import com.jcapp.mapwallpaper.view.PremiumDelegate;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Handles control logic of the BaseGamePlayActivity
 */
public class MainViewController {
    private static final String TAG = "MainViewController";

    private final UpdateListener mUpdateListener;
    private       MapsActivity   mActivity;

    // Tracks if we currently own subscriptions SKUs
    private boolean mGoldMonthly;
    private boolean mGoldYearly;

    // Tracks if we currently own a premium car
    private boolean mIsPremium;

    // Current amount of gas in tank, in units
    private int mTank;

    public MainViewController(MapsActivity activity) {
        mUpdateListener = new UpdateListener();
        mActivity = activity;
        loadData();
    }

    public boolean isPremiumPurchased() {
        return mIsPremium;
    }

    public UpdateListener getUpdateListener() {
        return mUpdateListener;
    }

    /**
     * Handler to billing updates
     */
    private class UpdateListener implements BillingManager.BillingUpdatesListener {
        @Override
        public void onBillingClientSetupFinished() {
            mActivity.onBillingManagerSetupFinished();
        }

        @Override
        public void onConsumeFinished(String token, @BillingResponse int result) {

        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchaseList) {
            mGoldMonthly = false;
            mGoldYearly = false;

            for (Purchase purchase : purchaseList) {
                switch (purchase.getSku()) {
                    case PremiumDelegate.SKU_ID:
                        Log.d(TAG, "You are Premium! Congratulations!!!");
                        mIsPremium = true;
                        break;
                }
            }

            mActivity.showRefreshedUi();
        }
    }


    /**
     * Save current tank level to disc
     * <p>
     * Note: In a real application, we recommend you save data in a secure way to
     * prevent tampering.
     * For simplicity in this sample, we simply store the data using a
     * SharedPreferences.
     */
    private void saveData() {
        SharedPreferences.Editor spe = mActivity.getPreferences(MODE_PRIVATE).edit();
        spe.putInt("tank", mTank);
        spe.apply();
        Log.d(TAG, "Saved data: tank = " + String.valueOf(mTank));
    }

    private void loadData() {
        SharedPreferences sp = mActivity.getPreferences(MODE_PRIVATE);
        mTank = sp.getInt("tank", 2);
        Log.d(TAG, "Loaded data: tank = " + String.valueOf(mTank));
    }
}