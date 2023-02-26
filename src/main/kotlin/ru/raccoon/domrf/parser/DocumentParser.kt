package ru.raccoon.domrf.parser

import org.apache.pdfbox.io.RandomAccessFile
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.text.PDFTextStripper
import ru.raccoon.domrf.dto.PaymentRecord
import ru.raccoon.domrf.dto.Payments
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DocumentParser(private val file: File) {

    private val pdfParser: PDFParser by lazy {
        PDFParser(wrapFile(file))
    }

    fun parse(): Payments {
        pdfParser.parse()

        val documentLines = pdfParser.pdDocument.use { document ->
            PDFTextStripper()
                .apply { sortByPosition = true }
                .getText(document)
                .lineSequence()
                .drop(MADE_PAYMENTS_HEADER_LINE_COUNT)
                .map { line -> line.trim() }
                .toList()
        }

        if (documentLines.isEmpty()) {
            throw IllegalArgumentException("Document is empty!")
        }

        val madePayments = getTable(documentLines)
        val plannedPayments = getTable(
            source = documentLines,
            startIndex = madePayments.size + PLANNED_PAYMENTS_HEADER_LINE_COUNT
        )

        return Payments(madePayments = madePayments, plannedPayments = plannedPayments)
    }

    private fun getTable(source: Collection<String>, startIndex: Int = 0): List<PaymentRecord> =
        source.asSequence()
            .drop(startIndex)
            .filter { line -> line.length > TABLE_ROW_LENGTH_THRESHOLD }
            .takeWhile { line -> !line.startsWith(TABLE_END_KEY_WORD) }
            .map(Companion::parseLine)
            .toList()

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())

        private const val MADE_PAYMENTS_HEADER_LINE_COUNT = 14
        private const val PLANNED_PAYMENTS_HEADER_LINE_COUNT = 9
        private const val TABLE_END_KEY_WORD = "ИТОГО"
        private const val TABLE_ROW_LENGTH_THRESHOLD = 5

        private const val DATE_GROUP_NAME = "date"
        private const val PAYMENT_SUM_GROUP_NAME = "paymentSum"
        private const val PERCENTS_PAYMENT_GROUP_NAME = "percentsPayment"
        private const val MAIN_DEPT_GROUP_NAME = "mainDept"
        private const val OTHER_PAYMENTS_GROUP_NAME = "otherPayments"
        private const val REMAINING_DEPT_GROUP_NAME = "remainingDebt"

        private val tableRowRegex = Regex(
            "^(?<$DATE_GROUP_NAME>(?>\\d{2}\\.){2}\\d{4})\\s(?<$PAYMENT_SUM_GROUP_NAME>.+\\.\\d{2})\\s(?<$PERCENTS_PAYMENT_GROUP_NAME>.+\\.\\d{2})\\s(?<$MAIN_DEPT_GROUP_NAME>.+\\.\\d{2})\\s(?<$OTHER_PAYMENTS_GROUP_NAME>.+\\.\\d{2})\\s(?<$REMAINING_DEPT_GROUP_NAME>.+\\.\\d{2})\$"
        )

        private fun MatchGroupCollection.intValueOf(groupName: String): Int {
            val groupValue = this[groupName]?.value
                ?: throw IllegalStateException("Group with name $groupName does not exist in the match group collection")

            return (groupValue.replace(" ", "").toDouble() * 100.00)
                .toInt()
        }

        private fun wrapFile(file: File): RandomAccessFile = RandomAccessFile(file, "r")

        private fun parseLine(line: CharSequence): PaymentRecord {
            val groups = tableRowRegex.find(line)?.groups
                ?: throw IllegalArgumentException("Failed to parse document line: $line. Match result does not found")

            val date = LocalDate.parse(groups[DATE_GROUP_NAME]!!.value, dateTimeFormatter)
            val paymentSum = groups.intValueOf(PAYMENT_SUM_GROUP_NAME)
            val percentsPayment = groups.intValueOf(PERCENTS_PAYMENT_GROUP_NAME)
            val mainDept = groups.intValueOf(MAIN_DEPT_GROUP_NAME)
            val otherPayments = groups.intValueOf(OTHER_PAYMENTS_GROUP_NAME)
            val remainingDept = groups.intValueOf(REMAINING_DEPT_GROUP_NAME)

            return PaymentRecord(
                date = date,
                paymentSum = paymentSum,
                percentsPayment = percentsPayment,
                mainDept = mainDept,
                otherPayments = otherPayments,
                remainingDebt = remainingDept
            )
        }
    }
}
