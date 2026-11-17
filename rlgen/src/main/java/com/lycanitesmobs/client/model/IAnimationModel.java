package com.lycanitesmobs.client.model;

public interface IAnimationModel {
   void angle(float var1, float var2, float var3, float var4);

   void rotate(float var1, float var2, float var3);

   void translate(float var1, float var2, float var3);

   void scale(float var1, float var2, float var3);

   double rotateToPoint(double var1, double var3);

   double rotateToPoint(double var1, double var3, double var5, double var7);

   double[] rotateToPoint(double var1, double var3, double var5, double var7, double var9, double var11);
}
