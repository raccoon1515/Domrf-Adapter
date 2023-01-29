package ru.raccoon.domrf.processor

import ru.raccoon.domrf.dto.PaymentRecord
import ru.raccoon.domrf.dto.Payments
import java.time.LocalDate

class PaymentsProcessor(private val payments: Payments) {

    fun groupByMonth(): Payments {
        val groupedPayments = LinkedHashMap<LocalDate, PaymentRecord>()

        for (madePayment in payments.madePayments) {
            groupedPayments.merge(madePayment.date.withDayOfMonth(1), madePayment) { old, new ->
                new.copy(
                    paymentSum = new.paymentSum + old.paymentSum,
                    percentsPayment = new.percentsPayment + old.percentsPayment,
                    mainDept = new.mainDept + old.mainDept,
                    otherPayments = new.otherPayments + old.otherPayments
                )
            }
        }

        return payments.copy(madePayments =  groupedPayments.values.toList())
    }
}
