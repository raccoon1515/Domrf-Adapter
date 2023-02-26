package ru.raccoon.domrf

import ru.raccoon.domrf.excel.ExcelDocument
import ru.raccoon.domrf.parser.DocumentParser
import ru.raccoon.domrf.processor.PaymentsProcessor
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    val (reportFile, resultFile) = args
        .map { File(it) }

    if (!reportFile.isFile) {
        throw IllegalArgumentException("Report file path ${reportFile.absolutePath} is not a file!")
    }

    val parser = DocumentParser(reportFile)
    val payments = runCatching {
        parser.parse()
    }.getOrElse {
        System.err.println("Failed to parse report file")
        it.printStackTrace()
        exitProcess(1)
    }

    val paymentsProcessor = PaymentsProcessor(payments)
    val processedPayments = paymentsProcessor.groupByMonth()

    runCatching {
        ExcelDocument(processedPayments)
            .save(resultFile)
    }.onFailure {
        System.err.println("Failed to write excel file")
        it.printStackTrace()
        exitProcess(1)
    }

}
