package tools.skyblock.skyhouse.mcmod.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import tools.skyblock.skyhouse.mcmod.SkyhouseMod;
import tools.skyblock.skyhouse.mcmod.models.Auction;
import tools.skyblock.skyhouse.mcmod.models.SearchFilter;
import tools.skyblock.skyhouse.mcmod.util.Resources;
import tools.skyblock.skyhouse.mcmod.util.Utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FlipListGui extends CustomGui {


    private SearchFilter filter;
    private List<Auction> auctions;
    private int page = 0;
    private int guiLeft, guiTop;
    private float guiScale;
    private int lastPageAuctions;
    private int totalPages;
    private int shownAucs;


    public FlipListGui(JsonArray json, SearchFilter searchFilter) {
        filter = searchFilter;
        auctions = new ArrayList<>();
        int i = 0;
        for (JsonElement el : json) {
            JsonObject item = el.getAsJsonObject();
            Auction auction = SkyhouseMod.serializeGson.fromJson(item, Auction.class);
            if (!SkyhouseMod.INSTANCE.getOverlayManager().auctionBlacklist.contains(auction.getUuid()))
                auctions.add(auction);
        }
        processAuctions();
        initGui();
    }

    @Override
    public void initGui() {
        tick();
        guiScale = Utils.getScaleFactor();
    }

    public void processAuctions() {
        for (Auction auc : auctions) {
            auc.process();
        }
    }

    @Override
    public void tick() {
        super.tick();
        guiLeft = Utils.getGuiLeft();
        guiTop = Utils.getGuiTop();
        if (auctions.size() == 0) {
            totalPages = 0;
            page = 0;
            shownAucs = 0;
        } else {
            totalPages = (int) Math.ceil((double) auctions.size() / 4);
            lastPageAuctions = auctions.size() % 4 == 0 ?  4 : auctions.size() % 4;
            if (page > totalPages - 1) page--;
            shownAucs = page == totalPages - 1 ? lastPageAuctions : 4;
        }

    }


    @Override
    public void drawScreen(int mouseX, int mouseY) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.AH_OVERLAY_BACKGROUND);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableDepth();
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0);
        GlStateManager.scale(guiScale, guiScale, guiScale);
        drawTexturedModalRect(0, 0, 0, 0, 256, 256);
        Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.GUI_COMPONENTS);
        drawTexturedModalRect(0, 256, 0, 45, 256, 32);

        Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.GUI_ICONS);
        drawTexturedModalRect(10, 256+8, 176, 0, 16, 16);
        drawTexturedModalRect(230, 256+8, 144, 0, 16, 16);

        drawCenteredString(Minecraft.getMinecraft().fontRendererObj, "Found " + auctions.size() + " flips! Page " + (page+1) + " of " + totalPages, 120, 20, 0xffffff);

        if (page != 0) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.GUI_ICONS);
            drawTexturedModalRect(12, 10, 0, 0, 16, 16);
        }
        if (page != totalPages-1) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.GUI_ICONS);
            drawTexturedModalRect(256-12-16, 10, 16, 0, 16, 16);
        }
        int i = 0;
        for (Auction auction : auctions.subList(page*4, page*4+shownAucs)) {
            ItemStack toRender = auction.getStack();

            if (auction.isRecomb()) {

                Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.RECOMB_ICON);
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.color(1f, 1f, 1f, 0.75f);
                GlStateManager.scale(0.0625, 0.0625, 0.0625);
                drawTexturedModalRect((12+135+16)*16, (- 19 + (i+1) * 55)*16, 0, 0, 256, 256);
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }

            GlStateManager.pushMatrix();
            GlStateManager.scale(2, 2, 2);
            GlStateManager.enableDepth();

            Utils.renderItem(toRender, (18) / 2, (-15 + 55 * ++i) / 2);
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
            Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.GUI_COMPONENTS);
            drawTexturedModalRect(12, - 20 + i * 55, 0, 0, 216, 45);
            Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.GUI_ICONS);
            drawTexturedModalRect(12+199, - 19 + i * 55, 80, 0, 16, 16);
            drawTexturedModalRect(12+135, - 19 + i * 55, 160, 0, 16, 16);
            String nameToDraw = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(auction.getStack().getDisplayName(), 150).trim();
            if (!nameToDraw.equals(auction.getStack().getDisplayName())) nameToDraw += (EnumChatFormatting.GRAY + "...");
            drawString(Minecraft.getMinecraft().fontRendererObj, nameToDraw, (57), (5 + 55 * i), 0xffffff);
            drawString(Minecraft.getMinecraft().fontRendererObj, NumberFormat.getNumberInstance(Locale.UK).format(auction.getProfit()) + " profit",
                    60, -15 + 55 * i, 0x00ff00);
        }
        i = 0;
        GlStateManager.popMatrix();
        for (Auction auction : auctions.subList(page*4, page*4+shownAucs)) {
            ItemStack toRender = auction.getStack();
            if (hover(mouseX-guiLeft, mouseY-guiTop, 18, -15+55*++i, 32, 32, guiScale))
                toRender.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
            if (hover(mouseX-guiLeft, mouseY-guiTop, 12+135, - 19 + i * 55, 15, 15, guiScale))
                drawHoveringText(Arrays.asList(
                        EnumChatFormatting.GREEN + "Price: " + NumberFormat.getNumberInstance(Locale.UK).format(auction.getPrice()),
                        EnumChatFormatting.GREEN + "Resell: " + NumberFormat.getNumberInstance(Locale.UK).format(auction.getPrice() + auction.getProfit()),
                        EnumChatFormatting.GREEN + "Profit: " + NumberFormat.getNumberInstance(Locale.UK).format(auction.getProfit()) + "" + EnumChatFormatting.RESET
                ), mouseX, mouseY, Minecraft.getMinecraft().fontRendererObj);
        }
        super.drawScreen(mouseX, mouseY);
        if (hover(mouseX-guiLeft, mouseY-guiTop, 10, 256+8, 16, 16, guiScale)) {
            drawHoveringText(Arrays.asList(EnumChatFormatting.RED + "Back to menu"), mouseX, mouseY);
        } else if (hover(mouseX-guiLeft, mouseY-guiTop, 230, 256+8, 16, 16, guiScale)) {
            drawHoveringText(Arrays.asList(EnumChatFormatting.GREEN + "Refresh auctions"), mouseX, mouseY);
        }
        GlStateManager.enableDepth();

    }

    @Override
    public void click(int mouseX, int mouseY) {
        int start = 4 * page;
        for (int i = 1; i < shownAucs+1; i++) {
            if (hover(mouseX-guiLeft, mouseY-guiTop, 12+199, - 19 + i * 55, 16, 16, guiScale)) {
                SkyhouseMod.INSTANCE.getOverlayManager().auctionBlacklist.add(auctions.get(start + i - 1).getUuid());
                auctions.remove(start + i - 1);
            }
            else if (hover(mouseX-guiLeft, mouseY-guiTop, 12, - 20 + i * 55, 216, 45, guiScale))
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/viewauction " + auctions.get(start + i - 1).getUuid());
        }
        if (page != 0 && hover(mouseX-guiLeft, mouseY-guiTop, 12, 10, 16, 16, guiScale)) page--;
        else if (page != totalPages-1 && hover(mouseX-guiLeft, mouseY-guiTop, 256-12-16, 10, 16, 16, guiScale)) page++;
        else if (hover(mouseX-guiLeft, mouseY-guiTop, 10, 256+8, 16, 16, guiScale)) {
            SkyhouseMod.INSTANCE.getOverlayManager().close();
        } else if (hover(mouseX-guiLeft, mouseY-guiTop, 230, 256+8, 16, 16, guiScale)) {
            SkyhouseMod.INSTANCE.getOverlayManager().search(filter);
        }
    }

    @Override
    public void keyEvent() {

    }

}
