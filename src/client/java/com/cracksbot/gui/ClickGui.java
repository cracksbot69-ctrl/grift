package com.cracksbot.gui;

import com.cracksbot.CracksBotClient;
import com.cracksbot.modules.HackModule;
import com.cracksbot.modules.ModuleCategory;
import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ClickGui extends Screen {

    private static final int PANEL_WIDTH = 110;
    private static final int PANEL_HEADER = 16;
    private static final int MODULE_HEIGHT = 13;
    private static final int SETTING_HEIGHT = 12;

    // Schwarz/Weiß Theme
    private static final int BG_PANEL = 0xF0111111;
    private static final int BG_HEADER = 0xFF1A1A1A;
    private static final int BG_MODULE = 0xE0141414;
    private static final int BG_MODULE_HOVER = 0xE0222222;
    private static final int BG_MODULE_ON = 0xE01A1A1A;
    private static final int BG_SETTING = 0x800D0D0D;
    private static final int BORDER = 0xFF333333;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF999999;
    private static final int DIM = 0xFF555555;
    private static final int ACCENT = 0xFFFFFFFF;
    private static final int SLIDER_BG = 0xFF2A2A2A;
    private static final int TOGGLE_OFF = 0xFF333333;
    private static final int TOGGLE_ON = 0xFFFFFFFF;

    // Panel positions (persistent)
    private static final Map<ModuleCategory, int[]> panelPos = new LinkedHashMap<>();
    private static final Set<ModuleCategory> collapsed = new HashSet<>();
    private static final Set<HackModule> expanded = new HashSet<>();

    private ModuleCategory dragging = null;
    private int dragOffX, dragOffY;
    private NumberSetting draggingSlider = null;
    private int sliderX, sliderW;
    private String search = "";
    private boolean searchActive = false;

    public ClickGui() {
        super(Component.literal("Grift"));
    }

    @Override
    protected void init() {
        if (panelPos.isEmpty()) {
            int x = 8;
            for (ModuleCategory cat : ModuleCategory.values()) {
                panelPos.put(cat, new int[]{x, 24});
                x += PANEL_WIDTH + 6;
            }
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        // Dim overlay
        gfx.fill(0, 0, width, height, 0x99000000);

        // Watermark
        gfx.drawString(font, "Grift", 5, 3, WHITE, true);
        gfx.drawString(font, " v" + CracksBotClient.VERSION, 5 + font.width("Grift"), 3, DIM, true);

        // Search
        int sx = width / 2 - 70;
        gfx.fill(sx - 1, 2, sx + 141, 16, searchActive ? WHITE : BORDER);
        gfx.fill(sx, 3, sx + 140, 15, 0xFF111111);
        String disp = search.isEmpty() && !searchActive ? "Search..." : search + (searchActive ? "_" : "");
        gfx.drawString(font, disp, sx + 3, 5, search.isEmpty() ? DIM : WHITE, false);

        // Panels
        for (ModuleCategory cat : ModuleCategory.values()) {
            renderPanel(gfx, cat, mouseX, mouseY);
        }
    }

    private void renderPanel(GuiGraphics gfx, ModuleCategory cat, int mx, int my) {
        int[] pos = panelPos.get(cat);
        int px = pos[0], py = pos[1];
        List<HackModule> mods = getModules(cat);
        if (mods.isEmpty()) return;

        int h = PANEL_HEADER;
        if (!collapsed.contains(cat)) {
            for (HackModule m : mods) {
                h += MODULE_HEIGHT;
                if (expanded.contains(m)) h += m.getSettings().size() * SETTING_HEIGHT + 4;
            }
        }

        // Shadow + bg
        gfx.fill(px + 1, py + 1, px + PANEL_WIDTH + 1, py + h + 1, 0x50000000);
        gfx.fill(px, py, px + PANEL_WIDTH, py + h, BG_PANEL);

        // Header
        gfx.fill(px, py, px + PANEL_WIDTH, py + PANEL_HEADER, BG_HEADER);
        gfx.fill(px, py, px + PANEL_WIDTH, py + 1, WHITE);
        gfx.drawString(font, cat.displayName, px + 4, py + 4, WHITE, true);
        gfx.drawString(font, collapsed.contains(cat) ? "+" : "-", px + PANEL_WIDTH - 10, py + 4, GRAY, false);

        // Border lines
        gfx.fill(px, py, px + 1, py + h, BORDER);
        gfx.fill(px + PANEL_WIDTH - 1, py, px + PANEL_WIDTH, py + h, BORDER);
        gfx.fill(px, py + h - 1, px + PANEL_WIDTH, py + h, BORDER);

        if (collapsed.contains(cat)) return;

        int ry = py + PANEL_HEADER;
        for (HackModule m : mods) {
            boolean hover = mx >= px && mx < px + PANEL_WIDTH && my >= ry && my < ry + MODULE_HEIGHT;
            gfx.fill(px + 1, ry, px + PANEL_WIDTH - 1, ry + MODULE_HEIGHT, m.isEnabled() ? BG_MODULE_ON : (hover ? BG_MODULE_HOVER : BG_MODULE));

            if (m.isEnabled()) {
                gfx.fill(px + 1, ry, px + 3, ry + MODULE_HEIGHT, WHITE);
            }

            gfx.drawString(font, m.getName(), px + 5, ry + 3, m.isEnabled() ? WHITE : GRAY, false);

            if (!m.getSettings().isEmpty()) {
                gfx.drawString(font, expanded.contains(m) ? "v" : ">", px + PANEL_WIDTH - 11, ry + 3, DIM, false);
            }

            ry += MODULE_HEIGHT;

            if (expanded.contains(m)) {
                ry += 2;
                for (Setting<?> s : m.getSettings()) {
                    gfx.fill(px + 3, ry, px + PANEL_WIDTH - 3, ry + SETTING_HEIGHT, BG_SETTING);
                    if (s instanceof BoolSetting bs) renderBool(gfx, bs, px + 3, ry, PANEL_WIDTH - 6);
                    else if (s instanceof NumberSetting ns) renderNumber(gfx, ns, px + 3, ry, PANEL_WIDTH - 6);
                    else if (s instanceof ModeSetting ms) renderMode(gfx, ms, px + 3, ry, PANEL_WIDTH - 6);
                    ry += SETTING_HEIGHT;
                }
                ry += 2;
            }
        }
    }

    private void renderBool(GuiGraphics gfx, BoolSetting s, int x, int y, int w) {
        gfx.drawString(font, s.getName(), x + 2, y + 2, GRAY, false);
        int tx = x + w - 16;
        gfx.fill(tx, y + 2, tx + 14, y + 9, s.getValue() ? TOGGLE_ON : TOGGLE_OFF);
        if (s.getValue()) gfx.fill(tx + 8, y + 2, tx + 14, y + 9, WHITE);
        else gfx.fill(tx, y + 2, tx + 6, y + 9, DIM);
    }

    private void renderNumber(GuiGraphics gfx, NumberSetting s, int x, int y, int w) {
        gfx.drawString(font, s.getName() + " " + String.format("%.1f", s.getValue()), x + 2, y + 2, GRAY, false);
        int sx = x + 2, sw = w - 4;
        gfx.fill(sx, y + SETTING_HEIGHT - 2, sx + sw, y + SETTING_HEIGHT, SLIDER_BG);
        int filled = (int) (sw * s.getRatio());
        gfx.fill(sx, y + SETTING_HEIGHT - 2, sx + filled, y + SETTING_HEIGHT, WHITE);
    }

    private void renderMode(GuiGraphics gfx, ModeSetting s, int x, int y, int w) {
        gfx.drawString(font, s.getName(), x + 2, y + 2, GRAY, false);
        int mw = font.width(s.getValue());
        gfx.drawString(font, "< " + s.getValue() + " >", x + w - mw - 16, y + 2, WHITE, false);
    }

    // --- Input ---

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        int mx = (int) event.x(), my = (int) event.y();
        int button = event.button();

        // Search
        int sx = width / 2 - 70;
        if (mx >= sx && mx <= sx + 140 && my >= 3 && my <= 15) {
            searchActive = true;
            return true;
        }
        searchActive = false;

        ModuleCategory[] cats = ModuleCategory.values();
        for (int i = cats.length - 1; i >= 0; i--) {
            ModuleCategory cat = cats[i];
            int[] pos = panelPos.get(cat);
            if (pos == null) continue;
            int px = pos[0], py = pos[1];
            List<HackModule> mods = getModules(cat);
            if (mods.isEmpty()) continue;

            if (mx >= px && mx < px + PANEL_WIDTH && my >= py && my < py + PANEL_HEADER) {
                if (button == 1) {
                    if (collapsed.contains(cat)) collapsed.remove(cat); else collapsed.add(cat);
                } else {
                    dragging = cat;
                    dragOffX = mx - px;
                    dragOffY = my - py;
                }
                return true;
            }

            if (collapsed.contains(cat)) continue;

            int ry = py + PANEL_HEADER;
            for (HackModule m : mods) {
                if (mx >= px && mx < px + PANEL_WIDTH && my >= ry && my < ry + MODULE_HEIGHT) {
                    if (button == 0) m.toggle();
                    else if (button == 1 && !m.getSettings().isEmpty()) {
                        if (expanded.contains(m)) expanded.remove(m); else expanded.add(m);
                    }
                    return true;
                }
                ry += MODULE_HEIGHT;

                if (expanded.contains(m)) {
                    ry += 2;
                    for (Setting<?> s : m.getSettings()) {
                        if (mx >= px + 3 && mx < px + PANEL_WIDTH - 3 && my >= ry && my < ry + SETTING_HEIGHT) {
                            clickSetting(s, mx, px + 3, PANEL_WIDTH - 6, button);
                            return true;
                        }
                        ry += SETTING_HEIGHT;
                    }
                    ry += 2;
                }
            }
        }
        return super.mouseClicked(event, bl);
    }

    private void clickSetting(Setting<?> s, int mx, int sx, int sw, int button) {
        if (s instanceof BoolSetting bs) bs.toggle();
        else if (s instanceof NumberSetting ns) {
            draggingSlider = ns;
            sliderX = sx + 2;
            sliderW = sw - 4;
            double r = (double)(mx - sliderX) / sliderW;
            ns.setFromRatio(Math.max(0, Math.min(1, r)));
        }
        else if (s instanceof ModeSetting ms) {
            if (button == 0) ms.cycle(); else ms.cycleBack();
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        int mx = (int) event.x(), my = (int) event.y();
        if (dragging != null) {
            int[] pos = panelPos.get(dragging);
            pos[0] = mx - dragOffX;
            pos[1] = my - dragOffY;
            return true;
        }
        if (draggingSlider != null) {
            double r = (double)(mx - sliderX) / sliderW;
            draggingSlider.setFromRatio(Math.max(0, Math.min(1, r)));
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        draggingSlider = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searchActive) {
            if (event.key() == 259 && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                return true;
            }
            if (event.key() == 256) {
                searchActive = false;
                search = "";
                return true;
            }
            return true;
        }
        if (event.key() == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (searchActive && event.isAllowedChatCharacter()) {
            search += event.codepointAsString();
            return true;
        }
        return super.charTyped(event);
    }

    private List<HackModule> getModules(ModuleCategory cat) {
        List<HackModule> result = new ArrayList<>();
        for (HackModule m : CracksBotClient.INSTANCE.getModules()) {
            if (m.getModuleCategory() == cat) {
                if (search.isEmpty() || m.getName().toLowerCase().contains(search.toLowerCase())) {
                    result.add(m);
                }
            }
        }
        return result;
    }
}
