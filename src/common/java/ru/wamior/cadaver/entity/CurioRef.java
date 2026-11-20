package ru.wamior.cadaver.entity;

/** Where a stack sat in the accessory inventory: slot type id (e.g. "ring" or "hand/glove"), index, cosmetic or not. */
public record CurioRef(String slotId, int index, boolean cosmetic) {
}
