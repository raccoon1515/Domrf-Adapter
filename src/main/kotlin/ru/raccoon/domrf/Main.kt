package ru.raccoon.domrf

import ru.raccoon.domrf.parser.DocumentParser
import ru.raccoon.domrf.processor.PaymentsProcessor

fun main(args: Array<String>) {

    val parser = DocumentParser(args[0])
    val payments = parser.parse()

    val paymentsProcessor = PaymentsProcessor(payments)
    val processedPayments = paymentsProcessor.groupByMonth()

    println()
}


