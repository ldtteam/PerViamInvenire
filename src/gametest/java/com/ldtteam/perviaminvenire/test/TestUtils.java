package com.ldtteam.perviaminvenire.test;

import com.ldtteam.perviaminvenire.test.level.EmptyLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class TestUtils {

    private TestUtils() {
        throw new IllegalStateException("Can not instantiate an instance of: TestUtils. This is a utility class");
    }

    public static boolean doesEntityTypeFloat(final EntityType<?> type) {
        try {
            final Entity entity = type.create(new EmptyLevel());
            if (!(entity instanceof Mob mob))
                return false;

            return mob.getNavigation().canFloat();
        } catch (final Exception e) {
            return false;
        }
    }
}
