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
package org.isf.vaccine.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.EventListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;
import org.isf.vactype.manager.VaccineTypeBrowserManager;
import org.isf.vactype.model.VaccineType;

/**
 * This class allow vaccines edits and inserts
 *
 * @author Eva
 *
 * modification history
 *  20/10/2011 - Cla - insert vaccinetype managment
 */
public class VaccineEdit extends JDialog {

	private static final long serialVersionUID = 1L;
	private EventListenerList vaccineListeners = new EventListenerList();

	public interface VaccineListener extends EventListener {

		void vaccineUpdated(AWTEvent e);

		void vaccineInserted(AWTEvent e);
	}

	public void addVaccineListener(VaccineListener l) {
		vaccineListeners.add(VaccineListener.class, l);
	}

	public void removeVaccineListener(VaccineListener listener) {
		vaccineListeners.remove(VaccineListener.class, listener);
	}

	private void fireVaccineInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = vaccineListeners.getListeners(VaccineListener.class);
		for (EventListener listener : listeners) {
			((VaccineListener) listener).vaccineInserted(event);
		}
	}

	private void fireVaccineUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = vaccineListeners.getListeners(VaccineListener.class);
		for (EventListener listener : listeners) {
			((VaccineListener) listener).vaccineUpdated(event);
		}
	}

	private VaccineBrowserManager vaccineBrowserManager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);
	private VaccineTypeBrowserManager vaccineTypeBrowserManager = Context.getApplicationContext().getBean(VaccineTypeBrowserManager.class);

	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JTextField descriptionTextField;
	private JTextField codeTextField;
	private JComboBox<VaccineType> vaccineTypeComboBox;
	private Vaccine vaccine;
	private boolean insert;

	/**
	 * This is the default constructor; we pass the arraylist and the selected row
	 * because we need to update them
	 */
	public VaccineEdit(JFrame owner, Vaccine old, boolean inserting) {
		super(owner, true);
		insert = inserting;
		vaccine = old;        //operation will be used for every operation
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.vaccine.newvaccine.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.vaccine.editvaccine.title"));
		}
		pack();
		setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getDataPanel(), java.awt.BorderLayout.NORTH);
			jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes dataPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getDataPanel() {
		if (dataPanel == null) {
			// vaccine type
			JLabel vaccineTypeDescLabel = new JLabel(MessageBundle.getMessage("angal.vaccine.vaccinetype") + ':');
			// vaccine code
			JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ':');
			// vaccine description
			JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ':');

			dataPanel = new JPanel();
			dataPanel.setLayout(new SpringLayout());
			dataPanel.add(vaccineTypeDescLabel);
			dataPanel.add(getVaccineTypeComboBox());
			dataPanel.add(codeLabel);
			dataPanel.add(getCodeTextField());
			dataPanel.add(descLabel);
			dataPanel.add(getDescriptionTextField());
			SpringUtilities.makeCompactGrid(dataPanel, 3, 2, 5, 5, 5, 5);
		}
		return dataPanel;
	}

	/**
	 * This method initializes buttonPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getOkButton(), null);
			buttonPanel.add(getCancelButton(), null);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes cancelButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
		}
		return cancelButton;
	}

	/**
	 * This method initializes okButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(actionEvent -> {

				vaccine.setDescription(descriptionTextField.getText());
				vaccine.setCode(codeTextField.getText());
				vaccine.setVaccineType(new VaccineType(((VaccineType) vaccineTypeComboBox.getSelectedItem()).getCode(),
						((VaccineType) vaccineTypeComboBox.getSelectedItem()).getDescription()));

				boolean result = false;
				Vaccine savedVaccine;
				if (insert) {
					try {
						savedVaccine = vaccineBrowserManager.newVaccine(vaccine);
						if (savedVaccine != null) {
							vaccine.setLock(savedVaccine.getLock());
							result = true;
						}

						if (result) {
							fireVaccineInserted();
							dispose();
						} else {
							MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
						}
					} catch (OHServiceException e1) {
						OHServiceExceptionUtil.showMessages(e1);
					}
				} else {
					try {
						savedVaccine = vaccineBrowserManager.updateVaccine(vaccine);
						if (savedVaccine != null) {
							vaccine.setLock(savedVaccine.getLock());
							result = true;
						}

						if (result) {
							fireVaccineUpdated();
							dispose();
						} else {
							MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
						}
					} catch (OHServiceException e1) {
						OHServiceExceptionUtil.showMessages(e1);
					}
				}
			});
		}
		return okButton;
	}

	/**
	 * This method initializes descriptionTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getDescriptionTextField() {
		if (descriptionTextField == null) {
			descriptionTextField = new VoLimitedTextField(50, 25);
			if (!insert) {
				descriptionTextField.setText(vaccine.getDescription());
			}
		}
		return descriptionTextField;
	}

	/**
	 * This method initializes codeTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getCodeTextField() {
		if (codeTextField == null) {
			codeTextField = new VoLimitedTextField(10);
			if (!insert) {
				codeTextField.setText(vaccine.getCode());
				codeTextField.setEnabled(false);
			}
		}
		return codeTextField;
	}

	/**
	 * This method initializes vaccineTypeComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox<VaccineType> getVaccineTypeComboBox() {
		if (vaccineTypeComboBox == null) {
			vaccineTypeComboBox = new JComboBox<>();
			try {
				List<VaccineType> types = vaccineTypeBrowserManager.getVaccineType();
				if (insert) {
					if (types != null) {
						for (VaccineType elem : types) {
							vaccineTypeComboBox.addItem(elem);
						}
					}
				} else {
					VaccineType selectedVaccineType = null;
					if (types != null) {
						for (VaccineType elem : types) {
							vaccineTypeComboBox.addItem(elem);
							if (vaccine.getVaccineType().equals(elem)) {
								selectedVaccineType = elem;
							}
						}
						if (selectedVaccineType != null) {
							vaccineTypeComboBox.setSelectedItem(vaccine.getVaccineType());
						}
					}
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		return vaccineTypeComboBox;
	}

}
