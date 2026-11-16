#!/usr/bin/env python3
"""Mechanical 1.12 -> 1.21 renames for decompiled Lycanites Mobs 2.0.8.9 client classes (models, renderers).
usage: lyc_port.py <file.java>...   (edits in place; the rest is done by hand)"""
import re, sys

IMPORTS = {
   'javax.vecmath.Vector2f': 'org.joml.Vector2f',
   'javax.vecmath.Vector3f': 'org.joml.Vector3f',
   'javax.vecmath.Vector4f': 'org.joml.Vector4f',
   'net.minecraft.entity.EntityLiving': 'net.minecraft.world.entity.Mob',
   'net.minecraft.entity.EntityLivingBase': 'net.minecraft.world.entity.LivingEntity',
   'net.minecraft.entity.Entity': 'net.minecraft.world.entity.Entity',
   'net.minecraft.util.math.MathHelper': 'net.minecraft.util.Mth',
   'net.minecraft.util.ResourceLocation': 'net.minecraft.resources.ResourceLocation',
   'net.minecraft.client.model.ModelBase': 'net.ilexiconn.llibrary.client.model.legacy.ModelBase',
   'net.minecraft.client.model.ModelRenderer': 'net.ilexiconn.llibrary.client.model.legacy.ModelRenderer',
   'net.minecraft.client.renderer.GlStateManager': 'net.ilexiconn.llibrary.client.model.legacy.LegacyRender',
   'org.lwjgl.opengl.GL11': 'net.ilexiconn.llibrary.client.model.legacy.LegacyRender',
}
DROP = ['net.minecraftforge.fml.relauncher.Side', 'net.minecraftforge.fml.relauncher.SideOnly',
        'net.minecraft.client.renderer.GlStateManager.DestFactor', 'net.minecraft.client.renderer.GlStateManager.SourceFactor']

def port(s):
   for d in DROP:
      s = s.replace('import %s;\n' % d, '')
   for a, b in IMPORTS.items():
      s = s.replace('import %s;\n' % a, 'import %s;\n' % b)
   s = re.sub(r'@SideOnly\(Side\.CLIENT\)\n', '', s)
   s = re.sub(r'\bEntityLivingBase\b', 'LivingEntity', s)
   s = re.sub(r'\bEntityLiving\b', 'Mob', s)
   s = re.sub(r'\bMathHelper\b', 'Mth', s)
   s = s.replace('GL11.glRotatef(', 'LegacyRender.rotate(').replace('GL11.glTranslatef(', 'LegacyRender.translate(')
   s = s.replace('GL11.glScalef(', 'LegacyRender.scale(').replace('GL11.glScaled(', 'LegacyRender.scale(')
   s = s.replace('GL11.glTranslated(', 'LegacyRender.translate(')
   s = s.replace('GL11.glPushMatrix()', 'LegacyRender.pushMatrix()').replace('GL11.glPopMatrix()', 'LegacyRender.popMatrix()')
   s = re.sub(r'\bGlStateManager\.(pushMatrix|popMatrix|translate|rotate|scale|color)\(', r'LegacyRender.\1(', s)
   for m in ['enableLighting', 'disableLighting', 'enableBlend', 'disableBlend', 'enableCull', 'disableCull']:
      s = s.replace('GlStateManager.%s()' % m, 'LycGL.%s()' % m)
   s = s.replace('GlStateManager.enableAlpha();', '').replace('GlStateManager.disableAlpha();', '')
   s = re.sub(r'OpenGlHelper\.setLightmapTextureCoords\(OpenGlHelper\.lightmapTexUnit, \(float\)j, \(float\)k\)', 'LycGL.setLightmap(i)', s)
   s = s.replace('.getBrightnessForRender()', 'LycGL.entityLight()') if False else s.replace('entity.getBrightnessForRender()', 'LycGL.entityLight()')
   if 'LycGL.' in s and 'import com.lycanitesmobs.client.renderer.LycGL;' not in s:
      s = s.replace('\nimport ', '\nimport com.lycanitesmobs.client.renderer.LycGL;\nimport ', 1)
   s = s.replace('import net.minecraft.client.renderer.OpenGlHelper;\n', '')
   s = re.sub(r'\.onGround\b(?!\()', '.onGround()', s)
   s = s.replace('.isRiding()', '.isPassenger()').replace('.getRNG()', '.getRandom()')
   s = s.replace('.isBeingRidden()', '.isVehicle()')
   s = re.sub(r'\.getX\(\)', '.x', s) if 'org.joml.Vector2f' in s else s
   s = re.sub(r'\.getY\(\)', '.y', s) if 'org.joml.Vector2f' in s else s
   s = s.replace('(float) Math.PI', '(float)Math.PI')
   return s

for f in sys.argv[1:]:
   s = open(f).read()
   t = port(s)
   if t != s:
      open(f, 'w').write(t)
