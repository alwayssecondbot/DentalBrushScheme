package ru.yanes.models;

import lombok.Getter;
import java.util.Arrays;

/**
 * A class representing a model of the oral cavity: contains all the teeth and the spaces between them.
 * <p>
 * The teeth are stored in the {@link #teeth} array in the order of drawing on the dental schema:
 * indices 0–15 are the upper jaw (FDI 28–21, then 11–18), indices 16–31 are the lower jaw (FDI 48–41, then 31–38).
 * The spaces are stored in the {@link #spaces} array; each space connects two adjacent teeth.
 * <p>
 * The numbering of the spaces has a peculiarity: there is no space between the last tooth of the upper jaw (18) and the first
 * tooth of the lower jaw (48), so the space indices are offset for the lower jaw. The {@code spaces} array has a length of {@code teeth.length - 2} (30),
 * since there are 32 teeth in total, and there is one less space for each jaw (16 teeth → 15 spaces).
 * <p>
 * Lombok's {@code @Getter} annotation automatically generates getters for the {@link #teeth} and {@link #spaces} fields.
 */
@Getter
public class Mouth {
	/** An array of all teeth. The order corresponds to the order of drawing on the diagram (see the class description). */
	private final Tooth[] teeth;
	/**
	 * An array of all spaces between teeth. The length is 2 less than the number of teeth:
	 * indices 0–14 are the upper jaw, indices 15–29 are the lower jaw.
	 */
	private final Space[] spaces;

	/**
	 * Creates a mouth model, initializing 32 teeth with fixed FDI positions
	 * and 30 spaces between them. Wisdom teeth (18, 28, 38, 48) are created inaccessible.
	 * <p>
	 * Tooth positions are specified in traversal order: upper jaw (28–18), then lower jaw (48–38).
	 * Spaces are created for each pair of adjacent teeth, except for the pair "18–48" (the transition between the jaws).
	 */
	public Mouth() {
		int[] TEETH_POSITION = new int[] {28, 27, 26, 25, 24, 23, 22, 21, 11, 12, 13, 14, 15, 16, 17, 18, 48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38};

		int teethNumber = TEETH_POSITION.length;
		this.teeth = new Tooth[teethNumber];

		for(int i = 0; i < teethNumber; ++i) {
			this.teeth[i] = new Tooth(TEETH_POSITION[i]);
		}

		// Number of spaces for each jaw lesser by 1 (for 2 jaws by 2)
		int spaceNumber = teeth.length - 2;
		this.spaces = new Space[spaceNumber];


		for(int i = 0; i < spaceNumber; ++i) {
			//As we don't have spaces between 38 and 28 teeth we skip this pare
			if (i >= 15) {
				this.spaces[i] = new Space(this.teeth[i + 1], this.teeth[i + 2], i);
			} else {
				this.spaces[i] = new Space(this.teeth[i], this.teeth[i + 1], i);
			}
		}
	}

	/**
	 * Returns a tooth by its index in the {@link #teeth} array.
	 *
	 * @param pos the tooth index (from 0 to {@code teeth.length - 1}), not the FDI number.
	 * @return the tooth with the specified index.
	 * @throws IllegalArgumentException if {@code pos} is out of range.
	 */
	public Tooth getTooth(int pos) {
		if (pos >= 0 && pos < this.teeth.length) {
			return this.teeth[pos];
		} else {
			throw new IllegalArgumentException("Invalid position: " + pos);
		}
	}

	/**
	 * Returns a space by its index in the {@link #spaces} array.
	 *
	 * @param pos the space index (from 0 to {@code spaces.length - 1}).
	 * @return the space with the specified index.
	 * @throws IllegalArgumentException if {@code pos} is out of range.
	 */
	public Space getSpace(int pos) {
		if (pos >= 0 && pos < this.spaces.length) {
			return this.spaces[pos];
		} else {
			throw new IllegalArgumentException("Invalid position: " + pos);
		}
	}

	/**
	 * Resets the entire model: makes all teeth accessible,
	 * and cleans the installed brushes (inner and outer) in all spaces.
	 */
	public void resetAll() {
		Arrays.stream(this.teeth).forEach((tooth) -> tooth.setAvailable(true));
		Arrays.stream(this.spaces).forEach((space) -> {
			space.emptyInnerBrush();
			space.emptyOuterBrush();
		});
	}
}
