package com.backend.designpatterns.structural.composite;

import java.util.ArrayList;
import java.util.List;

/**
 * Step 4: COMPOSITE NODE (OR)
 */
public class Step04_AnyOfPolicy implements Step02_AccessPolicy {

    private final List<Step02_AccessPolicy> policies = new ArrayList<>();

    public Step04_AnyOfPolicy add(Step02_AccessPolicy policy) {
        this.policies.add(policy);
        return this;
    }

    @Override
    public boolean isSatisfiedBy(Step01_UserContext context) {
        System.out.println(" [Composite] Evaluating AnyOfPolicy (OR)...");
        for (Step02_AccessPolicy policy : policies) {
            if (policy.isSatisfiedBy(context)) {
                System.out.println(" [Composite] -> PASS");
                return true;
            }
        }
        System.out.println(" [Composite] -> FAIL");
        return false;
    }
}
