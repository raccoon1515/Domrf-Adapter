package ru.raccoon.domrf.excel

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.raccoon.domrf.dto.Payments
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.properties.Delegates

class ExcelDocument(
    private val payments: Payments
) {

    private val workbook = XSSFWorkbook()
    private val sheet = workbook.createSheet("График")

    fun save(file: File) {
        build()

        try {
            FileOutputStream(file).use {  workbook.write(it) }
        } catch (ex: IOException) {
            System.err.println("Failed to write into ${file.absolutePath}")
            throw ex
        }
    }

    private fun buildHeader() {
        row {
            number = 0
            style = {
                fillForegroundColor = IndexedColors.LIGHT_YELLOW.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
            }
            font = {
                fontName = "Arial"
                bold = true
                fontHeightInPoints = 14
            }
            cell(0) { setCellValue("Test") }
            cell(1) { setCellValue("Test2") }
            cell(2) { setCellValue("Test3") }
            cell(3) { setCellValue("Test4") }
        }
    }

    private fun build() {
        buildHeader()

    }

    private inner class RowData(
        builder: RowData.() -> Unit
    ) {
        var number by Delegates.notNull<Int>()
        var style: (XSSFCellStyle.() -> Unit)? = null
        var font: (XSSFFont.() -> Unit)? = null

        private val row: XSSFRow
        private val rowStyle: XSSFCellStyle
        private val cells = mutableMapOf<Int, Cell.() -> Unit>()

        init {
            this.apply(builder)
            row = sheet.createRow(number)
            rowStyle = workbook.createCellStyle()

            if (style != null) {
                rowStyle.apply(style!!)
            }
            if (font != null) {
                rowStyle.setFont(workbook.createFont().apply(font!!))
            }
            row.rowStyle = rowStyle

            cells.forEach { (column, builder) ->
                val cell = row.createCell(column).apply(builder)
                cell.cellStyle = rowStyle
            }
        }

        fun cell(column: Int, builder: Cell.() -> Unit) {
            cells[column] = builder
        }

    }

    private fun row(builder: RowData.() -> Unit) {
        runCatching {
            RowData(builder)
        }.onFailure {
            System.err.println("Failed to build a row")
            throw it
        }
    }

}
