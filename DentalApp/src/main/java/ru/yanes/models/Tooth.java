package ru.yanes.models;

import lombok.Data;

/**
 * A class representing a tooth in a dental schema.
 * <p>
 * Stores the tooth's FDI position number and its current availability.
 * A tooth's availability determines whether it can be modified (e.g., selected or
 * used in toothbrush assignment). By default, most teeth are available,
 * except the four wisdom teeth (positions 18, 28, 38, 48), which
 * are initially unavailable.
 * <p>
 * Lombok's {@code @Data} annotation automatically generates getters and setters
 * for non-final fields, as well as the {@code equals()}, {@code hashCode()}, and {@code toString()} methods.
 */
@Data
public class Tooth {
	/** Tooth position number according to the FDI system (from 11 to 48). This field is immutable. */
	private final int position;
	/** Tooth availability flag. If {@code true}, the tooth is available for interaction. */
	private boolean available;

	/**
	 * Creates a tooth with the specified position. Accessibility is set automatically:
	 * wisdom teeth (positions 18, 28, 38, 48) are created inaccessible, all others are created accessible.
	 *
	 * @param position is the tooth position number according to FDI.
	 */
	Tooth(int position) {
		this.position = position;
		this.available = switch (this.position) {
			case 48, 28, 38, 18 -> false;
			default -> true;
		};
	}
}
