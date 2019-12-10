package com.afwsamples.testdpc.policy;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.common.Util;
import com.afwsamples.testdpc.common.preference.DpcPreferenceBase;
import com.afwsamples.testdpc.common.preference.DpcSwitchPreference;

public class UserRestrictionsParentDisplayFragment extends BaseSearchablePolicyPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "UserRestrictionsParent";

    private DevicePolicyManager mParentDevicePolicyManager;
    private UserManager mUserManager;
    private ComponentName mAdminComponentName;

    @RequiresApi(api = Util.R_VERSION_CODE)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DevicePolicyManager mDevicePolicyManager = (DevicePolicyManager)
                getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mUserManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
        mParentDevicePolicyManager = mDevicePolicyManager
                .getParentProfileInstance(mAdminComponentName);

        getActivity().getActionBar().setTitle(R.string.user_restrictions_management_title);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(
                getPreferenceManager().getContext());
        setPreferenceScreen(preferenceScreen);

        final Context preferenceContext = getPreferenceManager().getContext();
        for (UserRestriction restriction : UserRestriction.PROFILE_OWNER_ORG_DEVICE_RESTRICTIONS) {
            DpcSwitchPreference preference = new DpcSwitchPreference(preferenceContext);
            preference.setTitle(restriction.titleResId);
            preference.setKey(restriction.key);
            preference.setOnPreferenceChangeListener(this);
            preferenceScreen.addPreference(preference);
        }

        updateAllUserRestrictions();
        constrainPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAllUserRestrictions();
    }

    @Override
    public boolean isAvailable(Context context) {
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String restriction = preference.getKey();
        try {
            if (newValue.equals(true)) {
                mParentDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
            } else {
                mParentDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction);
            }
            updateUserRestriction(restriction);
            return true;
        } catch (SecurityException e) {
            Toast.makeText(getActivity(), R.string.user_restriction_error_msg,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error occurred while updating user restriction: " + restriction, e);
            return false;
        }
    }

    private void updateAllUserRestrictions() {
        for (UserRestriction restriction : UserRestriction.PROFILE_OWNER_ORG_DEVICE_RESTRICTIONS) {
            updateUserRestriction(restriction.key);
        }
    }

    private void updateUserRestriction(String userRestriction) {
        DpcSwitchPreference preference = (DpcSwitchPreference) findPreference(userRestriction);
        boolean disallowed = mUserManager.hasUserRestriction(userRestriction);
        preference.setChecked(disallowed);
    }

    private void constrainPreferences() {
        for (String restriction : UserRestriction.PROFILE_OWNER_ORG_OWNED_RESTRICTIONS) {
            DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
            pref.setMinSdkVersion(Util.R_VERSION_CODE);
        }
    }
}
