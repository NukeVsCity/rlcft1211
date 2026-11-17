#!/usr/bin/env python3
"""Mechanical 1.12 -> 1.21 renames for the decompiled Lycanites Mobs 2.0.8.9 GUI classes (on top of lyc_port.py).
usage: lyc_port_gui.py <file.java>...   (edits in place; the rest is done by hand)"""
import re, sys, os
sys.path.insert(0, os.path.dirname(__file__))
from lyc_port import port

IMPORTS = {
   'net.minecraft.entity.player.EntityPlayer': 'net.minecraft.world.entity.player.Player',
   'com.lycanitesmobs.client.localisation.LanguageManager': 'com.lycanitesmobs.core.LanguageManager',
   'com.lycanitesmobs.client.AssetManager': 'com.lycanitesmobs.core.AssetManager',
   'net.minecraftforge.fml.client.GuiScrollingList': 'com.lycanitesmobs.client.gui.GuiScrollingList',
   'net.minecraft.client.gui.GuiButton': 'com.lycanitesmobs.client.gui.buttons.ButtonBase',
   'net.minecraft.client.renderer.Tessellator': 'net.minecraft.client.gui.GuiGraphics',
   'net.minecraft.client.gui.FontRenderer': 'net.minecraft.client.gui.Font',
}
DROP = ['java.io.IOException']

def gui(s):
   s = port(s)
   for d in DROP:
      s = s.replace('import %s;\n' % d, '')
   for a, b in IMPORTS.items():
      s = s.replace('import %s;\n' % a, 'import %s;\n' % b)
   s = re.sub(r'\bEntityPlayer\b', 'Player', s)
   s = s.replace('Minecraft.getMinecraft()', 'Minecraft.getInstance()')
   s = re.sub(r'getFontRenderer\(\)\s*\.drawString\(', 'drawString(', s)
   s = s.replace('getFontRenderer().getStringWidth(', 'getFontRenderer().width(')
   s = s.replace('.getWordWrappedHeight(', '.wordWrapHeight(')
   s = s.replace('new GuiButton(', 'new ButtonBase(').replace('GuiButton ', 'ButtonBase ')
   s = s.replace('this.buttonList.add(', 'this.addButton(')
   s = s.replace('protected void actionPerformed(ButtonBase guiButton) throws IOException', 'public void actionPerformed(ButtonBase guiButton)')
   s = s.replace('Tessellator tessellator)', 'GuiGraphics graphics)')
   s = s.replace('public String getTitle()', 'public String getTitleText()')
   s = re.sub(r'player\.openGui\(LycanitesMobs\.instance, GuiHandler\.GuiType\.BEASTIARY\.id, player\.getEntityWorld\(\), GuiHandler\.Beastiary\.\w+\.id, 0, 0\);',
              'Minecraft.getInstance().setScreen(new %s(player));', s)
   m = re.search(r'public class (\w+)', s)
   if m:
      s = s.replace('new %s(player));', 'new %s(player));' % m.group(1))
   s = s.replace('this.mc.player', 'this.mc.player')
   return s

for f in sys.argv[1:]:
   s = open(f).read()
   t = gui(s)
   if t != s:
      open(f, 'w').write(t)
