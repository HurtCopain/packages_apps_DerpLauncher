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
package com.android.quickstep.util;

import static com.android.quickstep.TouchInteractionService.MAIN_THREAD_EXECUTOR;

import android.graphics.Rect;
import android.util.ArraySet;

import com.android.launcher3.Utilities;
import com.android.launcher3.util.Preconditions;
import com.android.quickstep.util.SwipeAnimationTargetSet.SwipeAnimationListener;
import com.android.systemui.shared.system.RecentsAnimationControllerCompat;
import com.android.systemui.shared.system.RecentsAnimationListener;
import com.android.systemui.shared.system.RemoteAnimationTargetCompat;

import java.util.Set;

import androidx.annotation.UiThread;

/**
 * Wrapper around {@link RecentsAnimationListener} which delegates callbacks to multiple listeners
 * on the main thread
 */
public class RecentsAnimationListenerSet implements RecentsAnimationListener {

    private final Set<SwipeAnimationListener> mListeners = new ArraySet<>();

    @UiThread
    public void addListener(SwipeAnimationListener listener) {
        Preconditions.assertUIThread();
        mListeners.add(listener);
    }

    @UiThread
    public void removeListener(SwipeAnimationListener listener) {
        Preconditions.assertUIThread();
        mListeners.remove(listener);
    }

    @Override
    public final void onAnimationStart(RecentsAnimationControllerCompat controller,
            RemoteAnimationTargetCompat[] targets, Rect homeContentInsets,
            Rect minimizedHomeBounds) {
        SwipeAnimationTargetSet targetSet = new SwipeAnimationTargetSet(controller, targets,
                homeContentInsets, minimizedHomeBounds);
        Utilities.postAsyncCallback(MAIN_THREAD_EXECUTOR.getHandler(), () -> {
            for (SwipeAnimationListener listener : getListeners()) {
                listener.onRecentsAnimationStart(targetSet);
            }
        });
    }

    @Override
    public final void onAnimationCanceled() {
        Utilities.postAsyncCallback(MAIN_THREAD_EXECUTOR.getHandler(), () -> {
            for (SwipeAnimationListener listener : getListeners()) {
                listener.onRecentsAnimationCanceled();
            }
        });
    }

    private SwipeAnimationListener[] getListeners() {
        return mListeners.toArray(new SwipeAnimationListener[mListeners.size()]);
    }
}