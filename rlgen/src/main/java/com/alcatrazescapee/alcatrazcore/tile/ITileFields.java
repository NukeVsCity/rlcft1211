package com.alcatrazescapee.alcatrazcore.tile;

/** alcatrazcore 1.12 ITileFields: int fields a container sends to the client (ContainerTileInventory.detectAndSendFieldChanges). */
public interface ITileFields {
   int getFieldCount();

   int getField(int id);

   void setField(int id, int value);
}
