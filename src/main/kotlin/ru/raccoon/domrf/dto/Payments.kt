package ru.raccoon.domrf.dto

data class Payments(
    val madePayments: List<PaymentRecord>,
    val plannedPayments: List<PaymentRecord>
) {
    override fun toString(): String {
        return "ru.raccoon.domrf.dto.Payments(madePayments=${madePayments.joinToString(separator = System.lineSeparator())}" +
                "${System.lineSeparator()}*****************************${System.lineSeparator()}" +
         "plannedPayments=${plannedPayments.joinToString(separator = System.lineSeparator())})"
    }
}
