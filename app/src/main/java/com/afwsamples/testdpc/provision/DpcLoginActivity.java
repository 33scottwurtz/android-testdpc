/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.afwsamples.testdpc.provision;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import com.afwsamples.testdpc.AddAccountActivity;
import com.afwsamples.testdpc.R;
import com.android.setupwizardlib.GlifLayout;

/**
 * Activity that gets launched by the
 * {@link android.app.admin.DevicePolicyManager#ACTION_GET_PROVISIONING_MODE} intent.
 */
public class DpcLoginActivity extends Activity {

    // TODO: clean up these hard coded constants once the new SDK is available.
    private static final String EXTRA_PROVISIONING_MODE =
        "android.app.extra.PROVISIONING_MODE";
    public static final int PROVISIONING_MODE_DO = 1;
    public static final int PROVISIONING_MODE_PO = 2;
    public static final int PROVISIONING_MODE_MANAGED_PROFILE_ON_FULLY_MANAGED_DEVICE = 3;

    private static final String LOG_TAG = "DpcLoginActivity";
    private static final int ADD_ACCOUNT_REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_dpc_login);
        GlifLayout layout = findViewById(R.id.dpc_login);
        layout.findViewById(R.id.suw_navbar_next).setOnClickListener(this::onNavigateNext);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADD_ACCOUNT_REQUEST_CODE:
                finishWithIntent(createResultIntentFromData(data));
                break;
            default:
                Log.d(LOG_TAG, "Unknown result code: " + resultCode);
                break;
        }
    }

    private Intent createResultIntentFromData(Intent data) {
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_PROVISIONING_MODE, PROVISIONING_MODE_PO);
        if (data != null && data.hasExtra(EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE)) {
            final Account accountToMigrate = data.getParcelableExtra(
                EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE);
            resultIntent.putExtra(EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE, accountToMigrate);
        }
        return resultIntent;
    }

    private void onNavigateNext(View nextButton) {
        final Intent intent = new Intent();
        RadioGroup dpcLoginOptions = findViewById(R.id.dpc_login_options);
        switch (dpcLoginOptions.getCheckedRadioButtonId()) {
            case R.id.dpc_login_do:
                intent.putExtra(EXTRA_PROVISIONING_MODE, PROVISIONING_MODE_DO);
                finishWithIntent(intent);
                return;
            case R.id.dpc_login_po:
                startActivityForResult(
                    new Intent(getApplicationContext(), AddAccountActivity.class),
                    ADD_ACCOUNT_REQUEST_CODE);
                return;
            case R.id.dpc_login_comp:
                intent.putExtra(EXTRA_PROVISIONING_MODE,
                    PROVISIONING_MODE_MANAGED_PROFILE_ON_FULLY_MANAGED_DEVICE);
                finishWithIntent(intent);
                return;
            default:
                finish();
        }
    }

    private void finishWithIntent(Intent intent) {
        setResult(RESULT_OK, intent);
        finish();
    }
}
