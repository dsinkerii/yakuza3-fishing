package com.example.mixins;

import com.example.FishingBobberEntityAccessor;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingBobberEntity.class)
public interface FishingBobberEntityMixin extends FishingBobberEntityAccessor {
    @Accessor("caughtFish")
    boolean getCaughtFish();
}