/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.utils.jobjects;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * @author <a href="http://www.java2s.com/Code/Java/Swing-JFC/Textfieldonlyacceptsnumbers.htm">...</a>
 */
public class VoFloatTextField extends JTextField {

	private static final long serialVersionUID = 1L;

	/**
	 * @param defval - default value
	 * @param columns - number of columns to show
	 */
	public VoFloatTextField(int defval, int columns) {
		super(String.valueOf(defval), columns);
	}

	public VoFloatTextField(String defval, int columns) {
		super(defval, columns);
	}

	@Override
	protected Document createDefaultModel() {
		return new FloatTextDocument();
	}

	public float getValue() {
		try {
			return Float.parseFloat(getText());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	class FloatTextDocument extends PlainDocument {

		private static final long serialVersionUID = 1L;

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

			if (str == null) {
				return;
			}
			String oldString = getText(0, getLength());
			String newString = oldString.substring(0, offs) + str + oldString.substring(offs);
			try {
				Float.parseFloat(newString + '0');
				super.insertString(offs, str, a);
			} catch (NumberFormatException e) {
				if (!str.matches("^[a-zA-Z0-9]*$")) {
					super.insertString(offs, ".", a);
				}
			}
		}
	}

}
