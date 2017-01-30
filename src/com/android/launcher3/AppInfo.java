/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.os.UserHandle;
import android.util.Log;

import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.PackageManagerHelper;

import java.util.ArrayList;

/**
 * Represents an app in AllAppsView.
 */
public class AppInfo extends ItemInfoWithIcon {

    /**
     * The intent used to start the application.
     */
    public Intent intent;

    public ComponentName componentName;

    /**
     * {@see ShortcutInfo#isDisabled}
     */
    int isDisabled = ShortcutInfo.DEFAULT;

    public AppInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }

    @Override
    public Intent getIntent() {
        return intent;
    }

    /**
     * Must not hold the Context.
     */
    public AppInfo(Context context, LauncherActivityInfo info, UserHandle user) {
        this(context, info, user, UserManagerCompat.getInstance(context).isQuietModeEnabled(user));
    }

    public AppInfo(Context context, LauncherActivityInfo info, UserHandle user,
            boolean quietModeEnabled) {
        this.componentName = info.getComponentName();
        this.container = ItemInfo.NO_ID;
        this.user = user;
        if (PackageManagerHelper.isAppSuspended(info.getApplicationInfo())) {
            isDisabled |= ShortcutInfo.FLAG_DISABLED_SUSPENDED;
        }
        if (quietModeEnabled) {
            isDisabled |= ShortcutInfo.FLAG_DISABLED_QUIET_USER;
        }

        intent = makeLaunchIntent(context, info, user);
    }

    public AppInfo(AppInfo info) {
        super(info);
        componentName = info.componentName;
        title = Utilities.trim(info.title);
        intent = new Intent(info.intent);
        isDisabled = info.isDisabled;
        iconBitmap = info.iconBitmap;
    }

    @Override
    protected String dumpProperties() {
        return super.dumpProperties() + " componentName=" + componentName;
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }

    public ComponentKey toComponentKey() {
        return new ComponentKey(componentName, user);
    }

    public static Intent makeLaunchIntent(Context context, LauncherActivityInfo info,
            UserHandle user) {
        long serialNumber = UserManagerCompat.getInstance(context).getSerialNumberForUser(user);
        return new Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(info.getComponentName())
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            .putExtra(EXTRA_PROFILE, serialNumber);
    }

    @Override
    public boolean isDisabled() {
        return isDisabled != 0;
    }
}
