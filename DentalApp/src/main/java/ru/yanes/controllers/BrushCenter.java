package ru.yanes.controllers;

/**
 * The center of a brush circle (swatch) on the brush panel.
 * <p>
 * Coordinates are in pixels and relative to the upper-left corner of the brush panel.
 * Instances are created by {@link DentalGeometry#getBrushCircleCenter(int, int, ru.yanes.models.Brush)}
 * and used both for drawing the brushes and for hit-testing clicks.
 *
 * @param x x-coordinate of the circle center;
 * @param y y-coordinate of the circle center.
 */
public record BrushCenter(int x, int y) {
}
