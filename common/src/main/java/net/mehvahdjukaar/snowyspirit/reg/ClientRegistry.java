package net.mehvahdjukaar.snowyspirit.reg;

import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.client.*;
import net.mehvahdjukaar.snowyspirit.client.block_model.GlowLightsModelLoader;
import net.mehvahdjukaar.snowyspirit.common.block.GlowLightsBlockTile;
import net.mehvahdjukaar.snowyspirit.common.entity.SledEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class ClientRegistry {

    public static ModelLayerLocation SLED_MODEL = loc("sled");
    public static ModelLayerLocation QUILT_MODEL = loc("quilt");

    private static ModelLayerLocation loc(String name) {
        return new ModelLayerLocation(SnowySpirit.res(name), name);
    }
    public static void init() {
        ClientPlatformHelper.addModelLayerRegistration(ClientRegistry::registerModelLayers);
        ClientPlatformHelper.addEntityRenderersRegistration(ClientRegistry::registerEntityRenderers);
        ClientPlatformHelper.addSpecialModelRegistration(ClientRegistry::registerSpecialModels);
    }


    public static void setup(){
        ClientPlatformHelper.registerRenderType(ModRegistry.GINGER_CROP.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.GINGER_WILD.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SNOW_GLOBE.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.WREATH.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.GINGER_POT.get(), RenderType.cutout());
        for (var v : ModRegistry.GUMDROPS_BUTTONS.values()) {
            ClientPlatformHelper.registerRenderType(v.get(), RenderType.translucent());
        }
        for (var v : ModRegistry.GLOW_LIGHTS_BLOCKS.values()) {
            ClientPlatformHelper.registerRenderType(v.get(), r -> r == RenderType.translucent() || r == RenderType.cutout());
        }

        ClientPlatformHelper.registerItemProperty(ModRegistry.GINGERBREAD_COOKIE.get(), new ResourceLocation("shape"),
                (stack, world, entity, s) -> entity == null ? 0 : System.identityHashCode(stack) % 4);
    }

    private static void registerEntityRenderers(ClientPlatformHelper.EntityRendererEvent event) {
        event.register(ModRegistry.SLED.get(), SledEntityRenderer::new);
        event.register(ModRegistry.CONTAINER_ENTITY.get(), ContainerHolderEntityRenderer::new);
    }

    private static void registerModelLayers(ClientPlatformHelper.ModelLayerEvent event) {
        event.register(SLED_MODEL, SledModel::createBodyLayer);
        event.register(SLED_MODEL, QuiltModel::createBodyLayer);
    }


    private static void registerSpecialModels(ClientPlatformHelper.SpecialModelEvent event) {
        event.register(SnowySpirit.res("glow_lights_loader"), new GlowLightsModelLoader());
    }


    @SubscribeEvent
    public static void registerBlockColors(ColorHandlerEvent.Block event) {
        BlockColors colors = event.getBlockColors();
        colors.register(new MimicBlockColor(), ModRegistry.GLOW_LIGHTS_BLOCKS.values().stream()
                .map(RegistryObject::get).toArray(Block[]::new));

    }

    public static void playSledSounds(SledEntity sledEntity) {
        Minecraft.getInstance().getSoundManager().play(new SledSoundInstance(sledEntity, false));
        Minecraft.getInstance().getSoundManager().play(new SledSoundInstance(sledEntity,true));
    }

    public static class MimicBlockColor implements BlockColor {

        @Override
        public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
            return col(state, world, pos, tint);
        }

        public static int col(BlockState state, BlockAndTintGetter level, BlockPos pos, int tint) {
            if (level != null && pos != null) {
                if (level.getBlockEntity(pos) instanceof GlowLightsBlockTile tile) {
                    BlockState mimic = tile.mimic;
                    if (mimic != null && !mimic.hasBlockEntity()) {
                        return Minecraft.getInstance().getBlockColors().getColor(mimic, level, pos, tint);
                    }
                }
            }
            return -1;
        }

        public static class noParticle implements BlockColor {
            @Override
            public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
                if (tint == 0) return -1;
                return col(state, world, pos, tint);
            }
        }
    }


}