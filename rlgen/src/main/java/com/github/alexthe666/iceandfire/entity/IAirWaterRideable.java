package com.github.alexthe666.iceandfire.entity;

/** The rideable mobs MessageDragonControl handled in 1.7.1 (hippogryph, hippocampus, amphithere): their rider's key state. */
public interface IAirWaterRideable {
   byte getControlState();

   void setControlState(byte state);
}
