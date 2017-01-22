/*
 * Lembretes. This software is intended for students from UNICAMP as a simple reminder of the daily meal.
 * Copyright (C) 2013-2017  Edson Duarte (edsonduarte1990@gmail.com)
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

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;

import hugo.weaving.DebugLog;

final class Meal implements Serializable {

    private String titleShort;
    private String titleFull;
    private String type;
    private String descriptionShort;
    private String descriptionFull;

    @DebugLog
    Meal(Element title, Element meal, Element body) {
        this.titleShort = title.text()
                .replaceFirst(".*\\(", "")
                .replaceFirst("\\).*", "")
                .concat(" - " + meal.text())
                .toUpperCase();

        this.titleFull = title.text()
                .replaceAll(" - ", "\n")
                .concat("\n" + meal.text())
                .toUpperCase();

        this.type = meal.text();

        Elements lines = body.getElementsByTag("td");

        this.descriptionShort = lines.get(1).text()
                .replaceFirst(".*: ", "")
                .toUpperCase();

        this.descriptionFull = this.titleFull;
        for (Element e : lines) {
            this.descriptionFull += "\n" + e.text().toUpperCase();
        }
    }

    String getTitleShort() {
        return titleShort;
    }

    String getTitleFull() {
        return titleFull;
    }

    String getDescriptionShort() {
        return descriptionShort;
    }

    String getDescriptionFull() {
        return descriptionFull;
    }

}
