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
package org.isf.opetype.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.opetype.gui.OperationTypeEdit.OperationTypeListener;
import org.isf.opetype.manager.OperationTypeBrowserManager;
import org.isf.opetype.model.OperationType;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * Browsing of table OperationType
 *
 * @author Furlanetto, Zoia, Finotto
 */
public class OperationTypeBrowser extends ModalJFrame implements OperationTypeListener {

	private static final long serialVersionUID = 1L;
	private List<OperationType> pOperationType;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase()
	};
	private int[] pColumnWidth = {80, 200};
	private JPanel jContainPanel;
	private JPanel jButtonPanel;
	private JButton jNewButton;
	private JButton jEditButton;
	private JButton jCloseButton;
	private JButton jDeleteButton;
	private JTable jTable;
	private OperationTypeBrowserModel model;
	private int selectedrow;

	private OperationTypeBrowserManager operationTypeBrowserManager = Context.getApplicationContext().getBean(OperationTypeBrowserManager.class);

	private OperationType operationType;
	private final JFrame myFrame;

	/**
	 * This method initializes 
	 */
	public OperationTypeBrowser() {
		super();
		myFrame = this;
		initialize();
		setVisible(true);
	}
	
	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.opetype.operationtypebrowser.title"));
		this.setContentPane(getJContainPanel());
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
			jContainPanel.add(new JScrollPane(getJTable()),	java.awt.BorderLayout.CENTER);
			validate();
		}
		return jContainPanel;
	}
	
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJNewButton(), null);
			jButtonPanel.add(getJEditButton(), null);
			jButtonPanel.add(getJDeleteButton(), null);
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}

	private JButton getJNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jNewButton.addActionListener(actionEvent -> {
				operationType = new OperationType("", "");
				OperationTypeEdit newrecord = new OperationTypeEdit(myFrame, operationType, true);
				newrecord.addOperationTypeListener(OperationTypeBrowser.this);
				newrecord.setVisible(true);
			});
		}
		return jNewButton;
	}
	
	/**
	 * This method initializes jEditButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJEditButton() {
		if (jEditButton == null) {
			jEditButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jEditButton.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
				} else {
					selectedrow = jTable.getSelectedRow();
					operationType = (OperationType) (model.getValueAt(selectedrow, -1));
					OperationTypeEdit newrecord = new OperationTypeEdit(myFrame, operationType, false);
					newrecord.addOperationTypeListener(OperationTypeBrowser.this);
					newrecord.setVisible(true);
				}
			});
		}
		return jEditButton;
	}
	
	/**
	 * This method initializes jCloseButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
		return jCloseButton;
	}
	
	/**
	 * This method initializes jDeleteButton
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jDeleteButton.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
				} else {
					OperationType opType = (OperationType) (model.getValueAt(jTable.getSelectedRow(), -1));
					int answer = MessageDialog.yesNo(null, "angal.opetype.deleteoperationtype.fmt.msg", opType.getDescription());
					if (answer == JOptionPane.YES_OPTION) {

						boolean deleted;
						try {
							deleted = operationTypeBrowserManager.deleteOperationType(opType);
						} catch (OHServiceException e) {
							deleted = false;
							OHServiceExceptionUtil.showMessages(e);
						}

						if (deleted) {
							pOperationType.remove(jTable.getSelectedRow());
							model.fireTableDataChanged();
							jTable.updateUI();
						}
					}
				}
			});
		}
		return jDeleteButton;
	}

	private JTable getJTable() {
		if (jTable == null) {
			model = new OperationTypeBrowserModel();
			jTable = new JTable(model);
			jTable.getColumnModel().getColumn(0).setMinWidth(pColumnWidth[0]);
			jTable.getColumnModel().getColumn(1).setMinWidth(pColumnWidth[1]);
		}
		return jTable;
	}

	class OperationTypeBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public OperationTypeBrowserModel() {
			try {
				pOperationType = operationTypeBrowserManager.getOperationType();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
				pOperationType = null;
			}
		}

		@Override
		public int getRowCount() {
			if (pOperationType == null) {
				return 0;
			}
			return pOperationType.size();
		}

		@Override
		public String getColumnName(int c) {
			return pColumns[c];
		}

		@Override
		public int getColumnCount() {
			return pColumns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return pOperationType.get(r).getCode();
			} else if (c == -1) {
				return pOperationType.get(r);
			} else if (c == 1) {
				return pOperationType.get(r).getDescription();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	@Override
	public void operationTypeUpdated(AWTEvent e) {
		pOperationType.set(selectedrow, operationType);
		((OperationTypeBrowserModel) jTable.getModel()).fireTableDataChanged();
		jTable.updateUI();
		if ((jTable.getRowCount() > 0) && selectedrow > -1) {
			jTable.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	@Override
	public void operationTypeInserted(AWTEvent e) {
		pOperationType.add(0, operationType);
		((OperationTypeBrowserModel) jTable.getModel()).fireTableDataChanged();
		if (jTable.getRowCount() > 0) {
			jTable.setRowSelectionInterval(0, 0);
		}
	}

}
