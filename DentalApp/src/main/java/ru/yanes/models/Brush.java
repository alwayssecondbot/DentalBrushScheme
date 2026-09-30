package ru.yanes.models;

import lombok.Getter;

import java.awt.Color;

/**
 * Enumeration of toothbrush types used to fill gaps.
 * <p>
 * Each toothbrush is characterized by its diameter (in millimeters) and color, which is
 * used for display in the diagram. The diameter determines the brush size
 * (from the smallest XXS to the largest XXL).
 * <p>
 * Lombok's {@code @Getter} annotation generates getters for the {@code diameter} and {@code color} fields.
 */
@Getter
public enum Brush {
	/** Smallest brush, 0.4 mm in diameter, pink. */
	XXS(0.4F, new Color(237,60,202)),
	/** Extra-small brush, 0.45 mm in diameter, orange. */
	XS(0.45F, new Color(255,160,0)),
	/** Small brush, 0.5 mm in diameter, red. */
	S(0.5F, new Color(248,0,0)),
	/** Medium brush, 0.6 mm in diameter, blue. */
	M(0.6F, new Color(66,170,255)),
	/** Large brush, 0.7 mm in diameter, yellow. */
	L(0.7F, new Color(251, 255, 0)),
	/** Extra-large brush, 0.8 mm diameter, light green color. */
	XL(0.8F, new Color(178,236,93)),
	/** Largest brush, 1.1 mm diameter, purple color. */
	XXL(1.1F, new Color(128,0,255));
	/** Brush diameter in millimeters. */
	private final float diameter;
	/** Brush color used for rendering. */
	private final Color color;

	/**
	 * Enumeration constructor. Private by default.
	 *
	 * @param diameter brush diameter
	 * @param color brush color
	 */
	Brush(float diameter, Color color) {
		this.diameter = diameter;
		this.color = color;
	}
}
