/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
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
 * imitations under the License.
 */

package edu.csee.umbc.mahbub1.medialogger;

import android.app.Application;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.filters.NearbyFilter;

import java.util.Set;

import edu.csee.umbc.mahbub1.medialogger.common.Constants;

/**
 * The application instance for the mobile app. We need to initialize the {@link WearManager} in the
 * {@link #onCreate()} method of the application instance to make sure it is available before any
 * other component of the application has been instantiated.
 */
public class MobileApplication extends Application {

    private static String sPackageName;
    private static int sCurrentPage = Constants.TARGET_INTRO;

    @Override
    public void onCreate() {
        super.onCreate();
        WearManager.initialize(getApplicationContext());
        sPackageName = getApplicationContext().getPackageName();
    }

    /**
     * A helper method to send a message to the nearby nodes with the information about the pages
     * that are being opened as we navigate through this app. This information is used on the
     * "Data Exchange" demo page.
     */
    public static void sendNavMessage(int page) {
        WearManager wearManager = WearManager.getInstance();
        Set<Node> nodes = wearManager.getConnectedNodes();
        if (nodes == null) {
            return;
        }
        Set<Node> nearbyNodes = new NearbyFilter().filterNodes(nodes);
        DataMap dataMap = new DataMap();
        dataMap.putInt(Constants.KEY_PAGE, page);
        for(Node node : nearbyNodes) {
            wearManager.sendMessage(node.getId(), Constants.NAVIGATION_PATH_MOBILE, dataMap, null);
        }
    }

    public static void setPage(int page) {
        sCurrentPage = page;
        sendNavMessage(page);
    }

    public static int getPage() {
        return sCurrentPage;
    }

    /**
     * Returns the package name of this app (same on wear or mobile sides)
     */
    public static String getPackage() {
        return sPackageName;
    }
}
