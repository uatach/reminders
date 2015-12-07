package com.lostrealm.lembretes;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.view.KeyEvent;
import android.view.Menu;

public class MainActivityUnitTest extends ActivityUnitTestCase<MainActivity> {

    Intent intent;

    public MainActivityUnitTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        intent = new Intent(getInstrumentation().getTargetContext(), MainActivity.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

//    public void testOpenSettings() {
//        startActivity(intent, null, null);
//        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(SettingsActivity.class.getName(), null, false);
////        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
//        getInstrumentation().invokeMenuActionSync(getActivity(), R.id.action_settings, 0);
//
//        Activity activity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 1000);
//        assertEquals(true, getInstrumentation().checkMonitorHit(activityMonitor, 1));
//        activity.finish();
//    }
//
//    public void testOpenAbout() {
//        startActivity(intent, null, null);
//    }
}
