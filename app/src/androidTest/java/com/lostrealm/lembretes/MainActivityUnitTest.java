package com.lostrealm.lembretes;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

public class MainActivityUnitTest extends ActivityUnitTestCase<MainActivity> {
    public MainActivityUnitTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Intent intent = new Intent(getInstrumentation().getTargetContext(), MainActivity.class);
        startActivity(intent, null, null);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
