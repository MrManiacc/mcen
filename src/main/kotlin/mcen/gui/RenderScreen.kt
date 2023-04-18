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

abstract class RenderScreen(private val renderer: RenderContext) : Screen(Component.literal("Render screen")) {
    private val target = MainTarget(1024, 1024)

    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
//        Minecraft.getInstance().mainRenderTarget.unbindWrite()
//        Minecraft.getInstance().mainRenderTarget.unbindRead()
        target.clear(Minecraft.ON_OSX)
        target.bindWrite(true)
        renderToBuffer(pPoseStack)
        target.unbindWrite()
//
////        target.blitToScreen(1024, 1024)
////        target.blitToScreen(512, 512)
////        target.blitToScreen()
        Minecraft.getInstance().mainRenderTarget.bindWrite(true)
////        target.bindRead()
//        renderBackground(pPoseStack)
////        target.unbindRead()
        renderer.render { render() }
    }

    private val bufferedTypes = HashMap<Item, Int>()
    protected fun renderInventory(itemHandler: IItemHandler, poseStack: PoseStack) {
        bufferedTypes.clear()
        val slots = itemHandler.slots
        for (i in 0 until slots) {
            val stack = itemHandler.getStackInSlot(i)
            if (stack.isEmpty) continue
            val item = stack.item
            var value = bufferedTypes.getOrPut(item) { 0 }
            value += stack.count
            bufferedTypes[item] = value
        }
        val count = bufferedTypes.size
        val rows = (count % 3) * 10f
        val cols = (count / 3) * 10f
        val size = (cols * 10).toInt()
        val quaternion = Vector3f.YP.rotationDegrees(0f)
        var i = 0
        bufferedTypes.forEach { (t, u) ->
            val x = (i % 3) * 50f
            val y = 10 + ((i / 3) * 35f)
            renderItem(x + 20, y + 5, 2f, quaternion, ItemStack(t))
//            poseStack.pushPose()
//            poseStack.scale(0.8f, 0.5f, 1f)
            font.drawShadow(poseStack, "$u", x,y - 5f, 0xffffff.toInt())
//            poseStack.popPose()
            i++
        }
    }

    protected val targetId: Int get() = target.colorTextureId

    protected abstract fun renderToBuffer(stack: PoseStack)

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
}