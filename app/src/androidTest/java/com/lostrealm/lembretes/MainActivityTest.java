package com.lostrealm.lembretes;

import android.test.SingleLaunchActivityTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.TextView;

public class MainActivityTest extends SingleLaunchActivityTestCase<MainActivity> {

    private MainActivity mainActivity;
    private TextView titleTextView;

    public MainActivityTest() {
        super("com.lostrealm.lembretes", MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mainActivity = getActivity();
        titleTextView = (TextView) mainActivity.findViewById(R.id.titleView);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @SmallTest
    public void testPreconditions() {
        assertNotNull("mainActivity is null", mainActivity);
        assertNotNull("titleTextView is null", titleTextView);
    }

    @SmallTest
    public void testInitialValues() {
        final String expectedTitleText = mainActivity.getString(R.string.main_activity_label);
        final String actualTitleText = titleTextView.getText().toString();
        assertEquals(expectedTitleText, actualTitleText);
    }
}
