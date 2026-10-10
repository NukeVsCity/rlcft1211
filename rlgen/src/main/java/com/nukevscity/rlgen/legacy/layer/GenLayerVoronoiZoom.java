package com.nukevscity.rlgen.legacy.layer;

public class GenLayerVoronoiZoom extends GenLayer {
   public GenLayerVoronoiZoom(long i, GenLayer genlayer) {
      super(i);
      super.parent = genlayer;
   }

   @Override
   public int[] getInts(int i, int j, int k, int l) {
      i -= 2;
      j -= 2;
      int i1 = i >> 2;
      int j1 = j >> 2;
      int k1 = (k >> 2) + 2;
      int l1 = (l >> 2) + 2;
      int[] aint = this.parent.getInts(i1, j1, k1, l1);
      int i2 = k1 - 1 << 2;
      int j2 = l1 - 1 << 2;
      int[] aint1 = new int[i2 * j2];

      for (int k2 = 0; k2 < l1 - 1; k2++) {
         int l2 = 0;
         int i3 = aint[l2 + 0 + (k2 + 0) * k1];

         for (int j3 = aint[l2 + 0 + (k2 + 1) * k1]; l2 < k1 - 1; l2++) {
            double d0 = 3.6;
            this.initChunkSeed((long)(l2 + i1 << 2), (long)(k2 + j1 << 2));
            double d1 = ((double)this.nextInt(1024) / 1024.0 - 0.5) * 3.6;
            double d2 = ((double)this.nextInt(1024) / 1024.0 - 0.5) * 3.6;
            this.initChunkSeed((long)(l2 + i1 + 1 << 2), (long)(k2 + j1 << 2));
            double d3 = ((double)this.nextInt(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
            double d4 = ((double)this.nextInt(1024) / 1024.0 - 0.5) * 3.6;
            this.initChunkSeed((long)(l2 + i1 << 2), (long)(k2 + j1 + 1 << 2));
            double d5 = ((double)this.nextInt(1024) / 1024.0 - 0.5) * 3.6;
            double d6 = ((double)this.nextInt(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
            this.initChunkSeed((long)(l2 + i1 + 1 << 2), (long)(k2 + j1 + 1 << 2));
            double d7 = ((double)this.nextInt(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
            double d8 = ((double)this.nextInt(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
            int k3 = aint[l2 + 1 + (k2 + 0) * k1] & 0xFF;
            int l3 = aint[l2 + 1 + (k2 + 1) * k1] & 0xFF;

            for (int i4 = 0; i4 < 4; i4++) {
               int j4 = ((k2 << 2) + i4) * i2 + (l2 << 2);

               for (int k4 = 0; k4 < 4; k4++) {
                  double d9 = ((double)i4 - d2) * ((double)i4 - d2) + ((double)k4 - d1) * ((double)k4 - d1);
                  double d10 = ((double)i4 - d4) * ((double)i4 - d4) + ((double)k4 - d3) * ((double)k4 - d3);
                  double d11 = ((double)i4 - d6) * ((double)i4 - d6) + ((double)k4 - d5) * ((double)k4 - d5);
                  double d12 = ((double)i4 - d8) * ((double)i4 - d8) + ((double)k4 - d7) * ((double)k4 - d7);
                  if (d9 < d10 && d9 < d11 && d9 < d12) {
                     aint1[j4++] = i3;
                  } else if (d10 < d9 && d10 < d11 && d10 < d12) {
                     aint1[j4++] = k3;
                  } else if (d11 < d9 && d11 < d10 && d11 < d12) {
                     aint1[j4++] = j3;
                  } else {
                     aint1[j4++] = l3;
                  }
               }
            }

            i3 = k3;
            j3 = l3;
         }
      }

      int[] aint2 = new int[k * l];

      for (int l4 = 0; l4 < l; l4++) {
         System.arraycopy(aint1, (l4 + (j & 3)) * i2 + (i & 3), aint2, l4 * k, k);
      }

      return aint2;
   }
}
