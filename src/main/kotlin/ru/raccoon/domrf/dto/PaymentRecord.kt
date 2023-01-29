package ru.raccoon.domrf.dto

import java.time.LocalDate

data class PaymentRecord(
    val date: LocalDate,
    val paymentSum: Int,
    val percentsPayment: Int,
    val mainDept: Int,
    val otherPayments: Int,
    val remainingDebt: Int
)
