package com.lostrealm.lembretes;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.VerificationModes;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.After;
import org.junit.Before;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mainActivity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mainActivity = getActivity();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @SmallTest
    public void testPreconditions() {
        assertNotNull("mainActivity is null", mainActivity);
    }

    @SmallTest
    public void testOpenAboutActivity() {
        Intents.init();
        Espresso.onView(ViewMatchers.withId(R.id.action_about)).perform(ViewActions.click());
        Intents.intended(IntentMatchers.hasComponent(AboutActivity.class.getName()));
        Intents.release();
    }

    @SmallTest
    public void testOpenSettingsActivity() {
        Intents.init();
        Espresso.onView(ViewMatchers.withId(R.id.action_settings)).perform(ViewActions.click());
        Intents.intended(IntentMatchers.hasComponent(SettingsActivity.class.getName()));
        Intents.release();
    }

    @SmallTest
    public void testRefresh() {
        Intents.init();
        Espresso.onView(ViewMatchers.withId(R.id.action_refresh)).perform(ViewActions.click());
//        Intents.intended(IntentMatchers.hasComponent(MainIntentService.class.getName()));
        Intents.release();
    }
}
