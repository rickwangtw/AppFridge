package com.mysticwind.disabledappmanager.ui.common;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/** SwipeDetector from
 * http://stackoverflow.com/questions/17520750/list-view-item-swipe-left-and-swipe-right
 *
 */
public class SwipeDetector implements View.OnTouchListener {

    public enum Action {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        TOP_TO_BOTTOM,
        BOTTOM_TO_TOP,
        NONE
    }

    private static final String TAG = "SwipeDetector";
    private static final int MIN_DISTANCE = 100;

    private float downX, downY, upX, upY;
    private Action isSwipeDetected = Action.NONE;

    public boolean swipeDetected() {
        return isSwipeDetected != Action.NONE;
    }

    public Action getAction() {
        return isSwipeDetected;
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                isSwipeDetected = Action.NONE;
                return false; // allow other events like Click to be processed
            }
            case MotionEvent.ACTION_MOVE: {
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                // horizontal swipe detection
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    // left or right
                    if (deltaX < 0) {
                        Log.d(TAG, "Swipe Left to Right");
                        isSwipeDetected = Action.LEFT_TO_RIGHT;
                        return true;
                    }
                    if (deltaX > 0) {
                        Log.d(TAG, "Swipe Right to Left");
                        isSwipeDetected = Action.RIGHT_TO_LEFT;
                        return true;
                    }
                } else

                    // vertical swipe detection
                    if (Math.abs(deltaY) > MIN_DISTANCE) {
                        // top or down
                        if (deltaY < 0) {
                            Log.d(TAG, "Swipe Top to Bottom");
                            isSwipeDetected = Action.TOP_TO_BOTTOM;
                            return false;
                        }
                        if (deltaY > 0) {
                            Log.d(TAG, "Swipe Bottom to Top");
                            isSwipeDetected = Action.BOTTOM_TO_TOP;
                            return false;
                        }
                    }
                return true;
            }
        }
        return false;
    }
}
