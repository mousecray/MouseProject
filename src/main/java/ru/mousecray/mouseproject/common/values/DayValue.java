/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.values;

public class DayValue {
    private int value, day;

    public DayValue(int value, int day) {
        this.value = value;
        this.day = day;
    }

    public int getValue()           { return value; }
    public int getDay()             { return day; }

    public void setDay(int day)     { this.day = day; }
    public void setValue(int value) { this.value = value; }
}
