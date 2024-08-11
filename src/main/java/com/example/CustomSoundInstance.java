package com.example;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class CustomSoundInstance implements SoundInstance {
    private float customVolume;
    private float x;
    private float y;
    private float z;
    private float volume;
    private boolean repeat;
    private int repeatDelay;
    private float pitch;
    private SoundEvent sound;
    private SoundCategory category;

    @Override
    public Identifier getId() {
        return sound.getId();
    }

    @Nullable
    @Override
    public WeightedSoundSet getSoundSet(SoundManager soundManager) {
        return null;
    }

    @Override
    public Sound getSound() {
        return null;
    }

    @Override
    public SoundCategory getCategory() {
        return category;
    }

    @Override
    public boolean isRepeatable() {
        return repeat;
    }

    @Override
    public boolean isRelative() {
        return false;
    }

    @Override
    public int getRepeatDelay() {
        return repeatDelay;
    }

    @Override
    public float getVolume() {
        return customVolume;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public AttenuationType getAttenuationType() {
        return null;
    }

    public void setVolume(float volume) {
        this.customVolume = volume;
    }
}
