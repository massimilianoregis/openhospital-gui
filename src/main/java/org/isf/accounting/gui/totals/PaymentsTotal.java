/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2021 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.accounting.gui.totals;

import java.math.BigDecimal;
import java.util.Collection;

import org.isf.accounting.model.BillPayments;

public class PaymentsTotal {

	private final Collection<Integer> notDeletedBills;
	private final Collection<BillPayments> paymentsPeriod;

	public PaymentsTotal(Collection<Integer> notDeletedBills, Collection<BillPayments> paymentsPeriod) {
		this.notDeletedBills = notDeletedBills;
		this.paymentsPeriod = paymentsPeriod;
	}

	public BigDecimal getValue() {
		return paymentsPeriod.stream()
				.filter(payment -> notDeletedBills.contains(payment.getBill().getId()))
				.map(payment -> new BigDecimal(Double.toString(payment.getAmount())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

}
