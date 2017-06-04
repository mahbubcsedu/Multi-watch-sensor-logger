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

package edu.csee.umbc.mahbub1.medialogger.pages;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.csee.umbc.mahbub1.medialogger.MobileApplication;
import edu.csee.umbc.mahbub1.medialogger.R;
import edu.csee.umbc.mahbub1.medialogger.common.Constants;

/**
 * The introductory fragment.
 */
public class IntroFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.intro_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobileApplication.setPage(Constants.TARGET_INTRO);
    }
}
