/*
 * Lembretes. This software is intended for students from UNICAMP as a simple reminder of the daily meal.
 * Copyright (C) 2013-2015  Edson Duarte (edsonduarte1990@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lostrealm.lembretes;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        try {
            TextView view = (TextView) findViewById(R.id.appVersionTextView);
            view.setText(getString(R.string.app_version, getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openProjectPage(View view) {
        this.startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.project_url))), getString(R.string.about_activity_button_action)));
    }
}