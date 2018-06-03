package com.example.sucharitharumesh.g7_a3.svmwrapperlibrary;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sdj on 4/3/17.
 *
 * Enum for different activity types in DB
 */

public enum ActivityType {
    WALKING(0),
    RUNNING(1),
    JUMPING(2);

    public final int fId;

    private ActivityType(int id) {
        this.fId = id;
    }

    private static final Map<Integer, ActivityType > lookup
            = new HashMap<Integer, ActivityType >();

    static {
        for (ActivityType s : EnumSet.allOf(ActivityType.class))
            lookup.put(s.fId, s);
    }

    public static ActivityType getValue(int intValue) {
        return lookup.get(intValue);
    }
}
