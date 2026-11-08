package ru.yanes.Panels;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * A document filter that limits the input text by character and line count.
 * <p>
 * Used for text areas where limits must be adhered to, such as a
 * comment field with a fixed number of lines and characters. If inserting or replacing
 * text would cause either limit to be exceeded, the operation is rejected.
 * <p>
 * Only explicit line breaks ({@code '\n'}) are counted as lines; lines produced by automatic
 * word wrapping in a {@link javax.swing.JTextArea} are not taken into account.
 * <p>
 * The class extends {@link DocumentFilter} and overrides its methods
 * {@link #insertString(FilterBypass, int, String, AttributeSet)} and
 * {@link #replace(FilterBypass, int, int, String, AttributeSet)}.
 */
public class TextLimitDocumentFilter extends DocumentFilter {
	/** Maximum number of rows allowed in a document. */
	private final int rowLimit;
	/** Maximum number of characters allowed in a document. */
	private final int charLimit;

	/**
	 * Creates a filter with the specified limits.
	 *
	 * @param rows maximum number of rows;
	 * @param chars maximum number of characters.
	 */
	TextLimitDocumentFilter(int rows, int chars) {
		this.rowLimit = rows;
		this.charLimit = chars;
	}

	/**
	 * Overridden string insertion method. Checks that after inserting
	 * the total number of characters and lines does not exceed the set limits.
	 * If the limits are met, calls the parent class implementation.
	 * Otherwise, the operation is ignored (the text is not changed).
	 *
	 * @param fb {@link FilterBypass} object allowing the parent method to be called;
	 * @param offset insertion position;
	 * @param string string to insert;
	 * @param attr set of attributes of the inserted text.
	 * @throws BadLocationException if the insertion position is invalid.
	 */
	@Override
	public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
		String current = fb.getDocument().getText(0, fb.getDocument().getLength());
		String newText = current.substring(0, offset) + string + current.substring(offset);
		if (newText.length() <= charLimit && lineCount(newText) <= rowLimit) {
			super.insertString(fb, offset, string, attr);
		}
	}

	/**
	 * Overridden text replacement method. Checks that after the replacement
	 * the total number of characters and lines does not exceed the set limits.
	 * If the limits are met, calls the parent class implementation.
	 * Otherwise, the operation is ignored (the text is not changed).
	 *
	 * @param fb {@link FilterBypass} object allowing the parent method to be called;
	 * @param offset start position of the replaced range;
	 * @param length length of the replaced range;
	 * @param text new text to insert instead of the range;
	 * @param attrs set of attributes of the new text.
	 * @throws BadLocationException if the position or the range is invalid.
	 */
	@Override
	public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
		String current = fb.getDocument().getText(0, fb.getDocument().getLength());
		String newText = current.substring(0, offset) + text + current.substring(offset + length);

		if (newText.length() <= charLimit && lineCount(newText) <= rowLimit) {
			super.replace(fb, offset, length, text, attrs);
		}
	}

	/**
	 * Counts the number of lines in the entered text.
	 * An empty line is counted as 0 lines; otherwise, the number of lines is equal to
	 * the number of line feed characters {@code '\n'} plus one.
	 *
	 * @param text the text to count lines for.
	 * @return the number of lines in the text.
	 */
	private int lineCount(String text) {
		int count = text.isEmpty() ? 0 : 1;
		for (char c : text.toCharArray()) {
			if (c == '\n') count++;
		}
		return count;
	}
}