package mcen.gui

import com.mojang.blaze3d.pipeline.MainTarget
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandler
import org.lwjgl.system.Platform

abstract class RenderScreen() : Screen(Component.literal("Render screen")) {
    private val target = MainTarget(1024, 1024)

    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
//        Minecraft.getInstance().mainRenderTarget.unbindWrite()
//        Minecraft.getInstance().mainRenderTarget.unbindRead()
//        target.clear(Minecraft.ON_OSX)
//        target.bindWrite(true)
//        renderToBuffer(pPoseStack)
//        target.unbindWrite()
////
//////        target.blitToScreen(1024, 1024)
//////        target.blitToScreen(512, 512)
//////        target.blitToScreen()
//        Minecraft.getInstance().mainRenderTarget.bindWrite(true)
////        target.bindRead()
//        renderBackground(pPoseStack)
////        target.unbindRead()
        Renderer.render { render() }
    }

    protected val targetId: Int get() = target.colorTextureId

    /**
     * Renders an item on the gui at the given position on top of everything.
     */
    protected fun renderItem(
        x: Float,
        y: Float,
        scale: Float,
        rotation: Quaternion,
        item: ItemStack,
    ) {
        RenderSystem.enableBlend()
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        val poseStack = RenderSystem.getModelViewStack()
        poseStack.pushPose()
//        poseStack.translate(0.0, 0.0, 100.0f.toDouble())
        poseStack.translate(x.toDouble(), (y.toDouble()), -10.0)
        poseStack.scale(1.0f, -1.0f, 1.0f)
        poseStack.scale(scale, scale, scale)
        RenderSystem.applyModelViewMatrix()
        val blockPoseStack = PoseStack()
        blockPoseStack.pushPose()
        blockPoseStack.mulPose(rotation)
        blockPoseStack.scale(24f, 16f, 2f)
        val bufferSource = Minecraft.getInstance().renderBuffers().bufferSource()
        Minecraft.getInstance().itemRenderer.renderStatic(
            item,
            ItemTransforms.TransformType.GUI,
            15728880,
            OverlayTexture.NO_OVERLAY,
            blockPoseStack,
            bufferSource,
            0
        )
        bufferSource.endBatch()
        poseStack.popPose()
        RenderSystem.applyModelViewMatrix()
    }

    override fun onClose() {
        target.destroyBuffers()
        super.onClose()
    }

    /**
     * Renders our frame
     */
    protected abstract fun Renderer.render()

    companion object {
        /**
         * Stores our imgui renderer, an initializes the backend upon loading completion on the clietn
         */
        @JvmStatic
         val Renderer = RenderContext(object : RendererBackend {
            override val windowHandle: Long
                get() = Minecraft.getInstance().window.window
            override val glslVersion: String
                get() = if (Platform.get() != Platform.MACOSX) "#version 120" else "#version 410"
        })

    }
}