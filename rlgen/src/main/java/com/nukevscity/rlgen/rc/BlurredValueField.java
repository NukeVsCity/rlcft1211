package com.nukevscity.rlgen.rc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** IvToolkit BlurredValueField (inverse-distance blurred random values, split into chunks of 10 values). */
public final class BlurredValueField implements BlurPivot {

   private record Value(double value, int[] pos) implements BlurPivot {
      @Override
      public double weight() {
         return 1.0;
      }
   }

   private final List<Value> values = new ArrayList<>();
   private int[] chunkCount;
   private BlurredValueField[] chunks;
   private int[] offset;
   private int[] size;
   private int[] center;
   private int[] neighborP1;
   private int[] neighborP2;
   private double weight;
   private double average;

   public BlurredValueField(int... size) {
      this.setBounds(new int[size.length], size);
   }

   private BlurredValueField(int[] offset, int[] size) {
      this.setBounds(offset, size);
   }

   static double getValue(List<? extends BlurPivot> values, int[] position) {
      int idx = 0;
      double total = 0.0;
      double[] invDist = new double[values.size()];
      for (BlurPivot value : values) {
         double dist = 0.0;
         for (int j = 0; j < position.length; j++) {
            double d = position[j] - value.pos()[j];
            dist += d * d;
         }
         dist *= dist;
         dist *= dist;
         if (dist <= 1.0E-4) {
            return value.value();
         }
         invDist[idx] = 1.0 / dist * value.weight();
         total += invDist[idx++];
      }
      double retVal = 0.0;
      total = 1.0 / total;
      idx = 0;
      for (BlurPivot value : values) {
         retVal += value.value() * invDist[idx++] * total;
      }
      return retVal;
   }

   private boolean almostContains(int[] pos) {
      for (int d = 0; d < pos.length; d++) {
         if (pos[d] < this.neighborP1[d] || pos[d] >= this.neighborP2[d]) {
            return false;
         }
      }
      return true;
   }

   private void setBounds(int[] chunkOffset, int[] chunkSize) {
      this.offset = chunkOffset;
      this.size = chunkSize;
      this.center = new int[this.offset.length];
      for (int d = 0; d < this.offset.length; d++) {
         this.center[d] = this.offset[d] + this.size[d] / 2;
      }
      this.neighborP1 = new int[this.offset.length];
      this.neighborP2 = new int[this.offset.length];
      for (int d = 0; d < this.offset.length; d++) {
         this.neighborP1[d] = this.offset[d] - this.size[d];
         this.neighborP2[d] = this.offset[d] + this.size[d] * 2;
      }
   }

   public int[] getSize() {
      return this.size.clone();
   }

   private boolean addValue(Value value) {
      if (this.chunks != null) {
         if (!this.getChunk(value.pos).addValue(value)) {
            return false;
         }
      } else {
         for (Value v : this.values) {
            if (Arrays.equals(v.pos, value.pos)) {
               return false;
            }
         }
         this.values.add(value);
         if (this.values.size() > 10) {
            this.split();
         }
      }
      this.average = this.calculateAverage();
      this.weight += value.weight();
      return true;
   }

   private double calculateAverage() {
      if (this.chunks != null) {
         double s = 0.0;
         for (BlurredValueField c : this.chunks) {
            s += c.average;
         }
         return s / this.chunks.length;
      }
      double s = 0.0;
      for (Value v : this.values) {
         s += v.value;
      }
      return s / this.values.size();
   }

   private BlurredValueField getChunk(int[] pos) {
      int[] chunkPos = new int[pos.length];
      for (int d = 0; d < pos.length; d++) {
         chunkPos[d] = (pos[d] - this.offset[d]) * this.chunkCount[d] / this.size[d];
      }
      for (int d = 0; d < chunkPos.length; d++) {
         if (chunkPos[d] < 0 || chunkPos[d] >= this.chunkCount[d]) {
            return null;
         }
      }
      int index = 0;
      for (int d = 0; d < chunkPos.length; d++) {
         index *= this.chunkCount[d];
         index += chunkPos[d];
      }
      return this.chunks[index];
   }

   private int[] chunkFromIndex(int index) {
      int[] chunkPos = new int[this.size.length];
      for (int i = chunkPos.length - 1; i >= 0; i--) {
         chunkPos[i] = index % this.chunkCount[i];
         index /= this.chunkCount[i];
      }
      return chunkPos;
   }

   private void split() {
      int max = 1;
      this.chunkCount = new int[this.size.length];
      for (int d = 0; d < this.size.length; d++) {
         this.chunkCount[d] = Math.min(this.size[d], 2);
         max *= this.chunkCount[d];
      }
      if (max > 1) {
         this.chunks = new BlurredValueField[max];
         for (int c = 0; c < this.chunks.length; c++) {
            int[] chunkPos = this.chunkFromIndex(c);
            int[] chunkOffset = new int[this.size.length];
            int[] chunkSize = new int[this.size.length];
            for (int d = 0; d < this.size.length; d++) {
               chunkOffset[d] = this.chunkOffset(d, chunkPos[d]);
               chunkSize[d] = this.chunkOffset(d, chunkPos[d] + 1) - chunkOffset[d];
            }
            this.chunks[c] = new BlurredValueField(chunkOffset, chunkSize);
         }
         for (Value value : this.values) {
            this.getChunk(value.pos).addValue(value);
         }
         this.values.clear();
      }
   }

   private int chunkOffset(int d, int chunkPos) {
      int roundUpCorrection = this.chunkCount[d] - 1;
      return this.offset[d] + (this.size[d] * chunkPos + roundUpCorrection) / this.chunkCount[d];
   }

   public boolean addValue(double value, Random random) {
      int[] pos = new int[this.size.length];
      for (int i = 0; i < pos.length; i++) {
         pos[i] = random.nextInt(this.size[i]) + this.offset[i];
      }
      return this.addValue(new Value(value, pos));
   }

   public double getValue(int... position) {
      List<BlurPivot> relevant = new ArrayList<>();
      this.addRelevantValues(position, relevant);
      return getValue(relevant, position);
   }

   private void addRelevantValues(int[] position, List<BlurPivot> rv) {
      if (this.chunks != null) {
         for (BlurredValueField chunk : this.chunks) {
            if (chunk.almostContains(position)) {
               chunk.addRelevantValues(position, rv);
            } else {
               rv.add(chunk);
            }
         }
      } else {
         rv.addAll(this.values);
      }
   }

   @Override
   public double value() {
      return this.average;
   }

   @Override
   public double weight() {
      return this.weight;
   }

   @Override
   public int[] pos() {
      return this.center;
   }
}
