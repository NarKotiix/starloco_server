package org.starloco.locos.area.map.entity;

import org.junit.Test;
import org.starloco.locos.command.administration.Group;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HouseOwnershipPolicyTest {

    @Test
    public void shouldAllowAdminGroupToBypassSingleHouseLimit() {
        Group admin = new Group(-1001, "Admin", false, "all");
        Group uppercaseAdmin = new Group(-1002, "ADMIN", false, "all");

        assertTrue(House.hasUnlimitedHouseOwnership(admin));
        assertTrue(House.hasUnlimitedHouseOwnership(uppercaseAdmin));
    }

    @Test
    public void shouldKeepSingleHouseLimitForNonAdminGroups() {
        Group player = new Group(-1003, "Player", true, "all");
        Group moderator = new Group(-1004, "Moderator", false, "all");

        assertFalse(House.hasUnlimitedHouseOwnership(null));
        assertFalse(House.hasUnlimitedHouseOwnership(player));
        assertFalse(House.hasUnlimitedHouseOwnership(moderator));
    }
}

