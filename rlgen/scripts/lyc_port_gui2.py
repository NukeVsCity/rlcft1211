#!/usr/bin/env python3
"""Second pass of 1.12 -> 1.21 renames for the Lycanites GUI classes (after lyc_port_gui.py): texture binding through
BaseScreen/BaseContainerScreen.bindTexture, 1.12 GuiContainer field names, FontRenderer calls.
usage: lyc_port_gui2.py <file.java>..."""
import re, sys

def fix(s):
   s = re.sub(r'(?:this\.)?(?:mc|Minecraft\.getInstance\(\))\.getTextureManager\(\)\.bindTexture\(', 'this.bindTexture(', s)
   s = s.replace('GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);', '')
   s = s.replace('LegacyRender.color(1.0F, 1.0F, 1.0F, 1.0F);', '')
   s = re.sub(r'\bthis\.xSize\b', 'this.imageWidth', s)
   s = re.sub(r'\bthis\.ySize\b', 'this.imageHeight', s)
   s = re.sub(r'\bthis\.guiLeft\b', 'this.leftPos', s)
   s = re.sub(r'\bthis\.guiTop\b', 'this.topPos', s)
   s = re.sub(r'(?:this\.)?fontRenderer\.drawString\(', 'this.drawString(', s)
   s = re.sub(r'(?:this\.)?fontRenderer\.getStringWidth\(', 'this.font.width(', s)
   s = re.sub(r'(?:this\.)?fontRenderer\.getWordWrappedHeight\(', 'this.font.wordWrapHeight(', s)
   s = s.replace('public void initGui() {\n      super.initGui();', '@Override\n   protected void init() {\n      super.init();')
   s = s.replace('this.mc.player.closeScreen()', 'this.onClose()')
   s = s.replace('.isEntityAlive()', '.isAlive()')
   s = re.sub(r'\.enabled = ', '.active = ', s)
   s = re.sub(r'this\.buttonList\s*\.add\(', 'this.addButton(', s)
   s = s.replace('import org.lwjgl.opengl.GL11;\n', '')
   return s

for f in sys.argv[1:]:
   s = open(f).read()
   t = fix(s)
   if t != s:
      open(f, 'w').write(t)
