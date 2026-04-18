package com.backend.designpatterns.structural.composite;

/**
 * Step 3: LEAF NODE (Time)
 */
public class Step03_TimeOfDayPolicy implements Step02_AccessPolicy {

    private final int startHour;
    private final int endHour;

    public Step03_TimeOfDayPolicy(int startHour, int endHour) {
        this.startHour = startHour;
        this.endHour = endHour;
    }

    @Override
    public boolean isSatisfiedBy(Step01_UserContext context) {
        boolean match = context.currentHour() >= startHour && context.currentHour() <= endHour;
        System.out.println("  [Leaf] TimeOfDayPolicy (" + startHour + "-" + endHour + ") -> " + match);
        return match;
    }
}
