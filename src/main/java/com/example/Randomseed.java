package com.example;

import com.example.mixins.FishingBobberEntityMixin;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.client.texture.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.World;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Randomseed implements ModInitializer {

	private static final Identifier MUSIC_ID = Identifier.of("yakuza3-fish:background_music");
	private static final Identifier CAUGHT_ID = Identifier.of("yakuza3-fish:fishcaught");
	private static final Identifier WON_ID = Identifier.of("yakuza3-fish:yougotit");
	private static final Identifier LOST_ID = Identifier.of("yakuza3-fish:yousomehowlost");
	public static final Identifier HOOKED = Identifier.of("yakuza3-fish:textures/hooked.png");
	public static final Identifier COGNRATS = Identifier.of("yakuza3-fish:textures/congrats.png");
	public static final Identifier MISS = Identifier.of("yakuza3-fish:textures/miss.png");
	private static final SoundCategory Y3FISH_CATEGORY = SoundCategory.MUSIC;
	public static SoundEvent MY_SOUND_EVENT = SoundEvent.of(MUSIC_ID);
	public static SoundEvent CAUGHT_EVENT = SoundEvent.of(CAUGHT_ID);
	public static SoundEvent WON_EVENT = SoundEvent.of(WON_ID);
	public static SoundEvent LOST_EVENT = SoundEvent.of(LOST_ID);

	private boolean isSoundPlaying = false;
	private int worldTick = 0;
	private CustomTickableSoundInstance soundInstance;
	boolean isFishing = false;
	int fishingTicks = 0;
	float rawVolume = 0;
	float interpolatedVolume = 0;
	boolean IsCaught = false;
	boolean FirstCaught = false;
	public Sprite HookedSprite;
	int PosMult = 0;
	int FishStartTick = 0;
	boolean IsFishing = false;
	boolean finishedBait = false;
	float congratsVal = 0;
	float RawcongratsVal = 0;
	float lostVal = 0;
	float RawlostVal = 0;

	double easeInOutExpo(double x) {
		return x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? Math.pow(2, 20 * x - 10) / 2 : (2 - Math.pow(2, -20 * x + 10)) / 2;
	}

	@Override
	public void onInitialize() {
		HudRenderCallback.EVENT.register(this::renderGui);
		Registry.register(Registries.SOUND_EVENT, MUSIC_ID, MY_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, CAUGHT_ID, CAUGHT_EVENT);
		Registry.register(Registries.SOUND_EVENT, WON_ID, WON_EVENT);
		Registry.register(Registries.SOUND_EVENT, LOST_ID, LOST_EVENT);
		UseItemCallback.EVENT.register(this::onPlayerHookFish);

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			var world = mc.world;
			worldTick++;

			if (mc.player != null && world != null) {
				if (!isSoundPlaying) {
					soundInstance = new CustomTickableSoundInstance(MY_SOUND_EVENT, worldTick);
					mc.getSoundManager().play(soundInstance);
					isSoundPlaying = true;
				} else if (soundInstance != null) {
					soundInstance.updateVolume(1);
				}
			}
			if(mc.player != null) {
				FishingBobberEntity bobber = mc.player.fishHook;
				if (bobber != null && ((FishingBobberEntityAccessor) bobber).getCaughtFish()) {
					rawVolume = 1;
					if(!FirstCaught && !IsCaught){
						FirstCaught = true;
						FishStartTick = worldTick;
					}else{
						FirstCaught = false;
					}
					IsCaught = true;
				}else{
					rawVolume = 0;
					if(IsCaught){
						finishedBait = true;
						if(!IsFishing){ // WON!!
							CustomTickableSoundInstance wonInstance = new CustomTickableSoundInstance(WON_EVENT, worldTick);
							mc.getSoundManager().play(wonInstance);
							new Thread(() -> {
								RawcongratsVal = 1;
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                RawcongratsVal = 0;
                            }).start();
						}else{ // lost... haha
							CustomTickableSoundInstance lostInstance = new CustomTickableSoundInstance(LOST_EVENT, worldTick);
							mc.getSoundManager().play(lostInstance);
							new Thread(() -> {
								RawlostVal = 1;
								try {
									Thread.sleep(1500);
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}
								RawlostVal = 0;
							}).start();
						}
					}else{
						finishedBait = false;
					}
					IsCaught = false;
					FirstCaught = false;
				}
			}
			interpolatedVolume = interpolatedVolume * (1 - 0.15F) + rawVolume * 0.15F;
			lostVal = lostVal * (1 - 0.15F) + RawlostVal * 0.15F;
			congratsVal = congratsVal * (1 - 0.15F) + RawcongratsVal * 0.15F;

			if(FirstCaught){
				CustomTickableSoundInstance caughtInstance = new CustomTickableSoundInstance(CAUGHT_EVENT, worldTick);
				mc.getSoundManager().play(caughtInstance);
			}
			if(soundInstance != null)
				soundInstance.updateVolume(interpolatedVolume);
		});
	}
	public static void drawTextureWithAlpha(DrawContext drawContext, Identifier textureId, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, float alpha) {
		// Ensure alpha is between 0 and 1
		alpha = Math.max(0, Math.min(1, alpha));

		// Save the current color
		float[] currentColor = RenderSystem.getShaderColor();

		// Set the alpha
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

		// Enable blending
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Draw the texture
		drawContext.drawTexture(textureId, x, y, 0, u, v, width, height, textureWidth, textureHeight);

		// Restore the original color
		RenderSystem.setShaderColor(currentColor[0], currentColor[1], currentColor[2], currentColor[3]);

		// Disable blending (if it wasn't enabled before)
		RenderSystem.disableBlend();
	}

	private void renderGui(DrawContext drawContext, RenderTickCounter tickDelta) {
		MinecraftClient mc = MinecraftClient.getInstance();
		// hooked
		if(mc.player != null && interpolatedVolume > 0.1) {
			int xJava = mc.getWindow().getScaledWidth()/2-627/4;
			int yJava = mc.getWindow().getScaledHeight()/2-89/4+(int)(interpolatedVolume*20);
			drawTextureWithAlpha(drawContext, HOOKED, xJava,yJava, 0, 0, 627/2, 89/2, 627/2, 89/2, interpolatedVolume);
		}
		// congrats
		if(mc.player != null && congratsVal > 0.1) {
			int xJava = mc.getWindow().getScaledWidth()/2-627/4;
			int yJava = mc.getWindow().getScaledHeight()-89-(int)(congratsVal*20);
			drawTextureWithAlpha(drawContext, COGNRATS, xJava,yJava, 0, 0, 627/2, 89/2, 627/2, 89/2, congratsVal);
		}
		// miss
		if(mc.player != null && lostVal > 0.1) {
			int xJava = mc.getWindow().getScaledWidth()/2-627/4;
			int yJava = mc.getWindow().getScaledHeight()-89-(int)(lostVal*20);
			drawTextureWithAlpha(drawContext, MISS, xJava,yJava, 0, 0, 627/2, 89/2, 627/2, 89/2, lostVal);
		}
	}

	private TypedActionResult<ItemStack> onPlayerHookFish(PlayerEntity playerEntity, World world, Hand hand) {
		if (!world.isClient && playerEntity.getStackInHand(hand).getItem() == Items.FISHING_ROD) {
			IsFishing = !IsFishing;
		}
		return TypedActionResult.pass(null);
	}


	private class CustomTickableSoundInstance extends PositionedSoundInstance implements TickableSoundInstance {

		private int startTick;

		public CustomTickableSoundInstance(SoundEvent sound, int startTick) {
            super(sound, SoundCategory.MUSIC, 1, 1, new Random() {
				@Override
				public Random split() {
					return null;
				}

				@Override
				public RandomSplitter nextSplitter() {
					return null;
				}

				@Override
				public void setSeed(long seed) {

				}

				@Override
				public int nextInt() {
					return 0;
				}

				@Override
				public int nextInt(int bound) {
					return 0;
				}

				@Override
				public long nextLong() {
					return 0;
				}

				@Override
				public boolean nextBoolean() {
					return false;
				}

				@Override
				public float nextFloat() {
					return 0;
				}

				@Override
				public double nextDouble() {
					return 0;
				}

				@Override
				public double nextGaussian() {
					return 0;
				}
			}, new BlockPos(0,0,0));
            this.startTick = startTick;
		}

		@Override
		public void tick() {
			// This method will be called every tick
		}

		public void updateVolume(float vol) {
			this.volume = vol;
		}

		@Override
		public boolean isDone() {
			return false; // or implement a condition to stop the sound
		}
	}
}
