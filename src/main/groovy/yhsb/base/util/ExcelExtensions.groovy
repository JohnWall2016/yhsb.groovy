package yhsb.base.util

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Excels {
    enum Type {
        Xls, Xlsx, Auto
    }

    static Workbook load(String fileName, Type type = Type.Auto) {
        if (type == Type.Auto) {
            def fn = fileName.toLowerCase()
            if (fn.endsWith('.xls'))
                type = Type.Xls
            else if (fn.endsWith('.xlsx'))
                type = Type.Xlsx
            else
                throw new Exception("unknown excel type: $fn")
        }

        def loadFile = {
            new ByteArrayInputStream(
                    Files.readAllBytes(Paths.get(fileName))
            )
        }

        if (type == Type.Xls)
            new HSSFWorkbook(loadFile())
        else
            new XSSFWorkbook(loadFile())
    }

    static Workbook load(Path path) {
        load(path.toString(), Type.Auto)
    }
}

class ExcelExtensions {
    static void save(Workbook workbook, String fileName) {
        workbook.save(Paths.get(fileName))
    }

    static void save(Workbook workbook, Path file) {
        Files.newOutputStream(file).withCloseable {
            workbook.write(it)
        }
    }

    static Row createRow(
            Sheet sheet,
            int targetRowIndex,
            int sourceRowIndex,
            boolean clearValue
    ) {
        if (targetRowIndex == sourceRowIndex) {
            throw new IllegalArgumentException(
                    "sourceIndex and targetIndex cannot be same: $sourceRowIndex == $targetRowIndex"
            )
        }

        def srcRow = sheet.getRow(sourceRowIndex)
        def newRow = sheet.createRow(targetRowIndex)
        newRow.setHeight(srcRow.height)

        for (index in srcRow.firstCellNum..<srcRow.lastCellNum) {
            def srcCell = srcRow.getCell(index)
            if (srcCell) {
                newRow.createCell(index).with {
                    cellStyle = srcCell.cellStyle
                    cellComment = srcCell.cellComment
                    hyperlink = srcCell.hyperlink
                    switch (srcCell.cellType) {
                        case CellType.NUMERIC:
                            cellValue = clearValue ? 0 : srcCell.numericCellValue
                            break
                        case CellType.STRING:
                            cellValue = clearValue ? '' : srcCell.stringCellValue
                            break
                        case CellType.FORMULA:
                            cellFormula = clearValue ? null : srcCell.cellFormula
                            break
                        case CellType.BLANK:
                            setBlank()
                            break
                        case CellType.BOOLEAN:
                            cellValue = clearValue ? false : srcCell.booleanCellValue
                            break
                        case CellType.ERROR:
                            cellErrorValue = srcCell.errorCellValue
                            break
                        default:
                            break
                    }
                    it
                }
            }
        }

        def merged = new CellRangeAddressList()
        for (index in 0..<sheet.numMergedRegions) {
            def address = sheet.getMergedRegion(index)
            if (sourceRowIndex == address.firstRow && sourceRowIndex == address.lastRow) {
                merged.addCellRangeAddress(
                        targetRowIndex,
                        address.firstColumn,
                        targetRowIndex,
                        address.lastColumn
                )
            }
        }
        for (region in merged.cellRangeAddresses) {
            sheet.addMergedRegion(region)
        }

        newRow
    }

    static Row getOrCopyRow(Sheet sheet, int targetRowIndex, int sourceRowIndex, boolean clearValue = false) {
        if (targetRowIndex == sourceRowIndex) {
            sheet.getRow(sourceRowIndex)
        } else {
            if (sheet.lastRowNum >= targetRowIndex) {
                sheet.shiftRows(targetRowIndex, sheet.lastRowNum, 1, true, false)
            }
            sheet.createRow(targetRowIndex, sourceRowIndex, clearValue)
        }
    }

    static void copyRows(Sheet sheet, int startRowIndex, int count, int sourceRowIndex, boolean clearValue = false) {
        sheet.shiftRows(startRowIndex, sheet.lastRowNum, count, true, false)
        for (i in 0..<count) {
            sheet.createRow(startRowIndex + i, sourceRowIndex, clearValue)
        }
    }

    static Iterator<Row> rowIterator(Sheet sheet, int start, int end = -1) {
        new Iterator<Row>() {
            private int index = Math.max(0, start)
            private int last = end == -1 ? sheet.lastRowNum : Math.min(end, sheet.lastRowNum)

            @Override
            boolean hasNext() {
                index <= last
            }

            @Override
            Row next() {
                sheet.getRow(index++)
            }
        }
    }

    static Cell getCell(Sheet sheet, String cellName) {
        def ref = CellRef.from(cellName)
        if (ref) {
            sheet.getRow(ref.rowIndex - 1).getCell(ref.columnIndex - 1)
        } else {
            null
        }
    }

    static Cell getCell(Sheet sheet, int row, int col) {
        sheet.getRow(row).getCell(col)
    }

    static Cell getCell(Sheet sheet, int row, String colName) {
        sheet.getRow(row).getCell(colName)
    }

    static Cell getAt(Sheet sheet, String cellName) {
        sheet.getCell(cellName)
    }

    static void deleteRow(Sheet sheet, int rowIndex) {
        /*int lastRowNum = sheet.lastRowNum
        if (rowIndex >= 0 && rowIndex < lastRowNum) {
            sheet.shiftRows(rowIndex + 1, lastRowNum, -1)
        }
        if (rowIndex == lastRowNum) {
            Row removingRow = sheet.getRow(rowIndex)
            if (removingRow != null) {
                sheet.removeRow(removingRow)
            }
        }*/
        deleteRows(sheet, rowIndex, 1)
    }

    static void deleteRows(Sheet sheet, int rowIndex, int count) {
        int lastRowNum = sheet.lastRowNum
        if (rowIndex >= 0 && rowIndex <= lastRowNum && count > 0) {
            int endRowIndex = Math.min(rowIndex + count, lastRowNum)
            (rowIndex..((endRowIndex == lastRowNum) ? endRowIndex : endRowIndex - 1)).each {
                def row = sheet.getRow(it)
                if (row != null) {
                    sheet.removeRow(row)
                }
            }
            if (endRowIndex != lastRowNum) {
                sheet.shiftRows(endRowIndex, lastRowNum, rowIndex - endRowIndex)
            }
        }
    }

    static Cell getCell(Row row, String columnName) {
        row.getCell(CellRef.columnNameToNumber(columnName) - 1)
    }

    static Cell getAt(Row row, String columnName) {
        row.getCell(columnName)
    }

    static Cell createCell(Row row, String columnName) {
        row.createCell(CellRef.columnNameToNumber(columnName) - 1)
    }

    static Cell getOrCreateCell(Row row, int col) {
        def cell = row.getCell(col)
        cell ?: row.createCell(col)
    }

    static Cell getOrCreateCell(Row row, String columnName) {
        row.getOrCreateCell(CellRef.columnNameToNumber(columnName) - 1)
    }

    static void copyTo(Row src, Row dest, String... fields) {
        for (field in fields) {
            dest.getOrCreateCell(field).cellValue = src.getCell(field).value
        }
    }

    static String getValue(Cell cell) {
        if (!cell) return ''
        cell.getString(cell.cellType)
    }

    static String getString(Cell cell, CellType type) {
        switch (type) {
            case CellType.STRING:
                return cell.stringCellValue
            case CellType.BOOLEAN:
                return cell.booleanCellValue.toString()
            case CellType.NUMERIC:
                def v = cell.numericCellValue
                if (v.validInt) {
                    return v.toInteger().toString()
                } else {
                    return v.toString()
                }
            case CellType.FORMULA:
                return getString(cell, cell.cachedFormulaResultType)
            case CellType.BLANK:
                return ''
            case CellType.ERROR:
                return ''
            default:
                throw new Exception("unsupported type: $type")
        }
    }
}

class CellRef {
    final int rowIndex
    final int columnIndex
    final boolean rowAnchored
    final boolean columnAnchored
    final String columnName

    CellRef(int rowIndex, int columnIndex, boolean rowAnchored = false, boolean columnAnchored = false) {
        this.rowIndex = rowIndex
        this.columnIndex = columnIndex
        this.rowAnchored = rowAnchored
        this.columnAnchored = columnAnchored
        this.columnName = columnNumberToName(columnIndex)
    }

    String toAddress() {
        def sb = new StringBuilder()
        if (columnAnchored) sb.append('$')
        sb.append(columnName)
        if (rowAnchored) sb.append('$')
        sb.append(rowIndex)
        sb.toString()
    }

    @Override
    String toString() {
        toAddress()
    }

    static int columnNameToNumber(String name) {
        def num = 0
        for (ch in name.toUpperCase().chars()) {
            num *= 26
            num += ch - 64
        }
        num
    }

    static String columnNumberToName(int number) {
        def dividend = number
        def name = new StringBuilder()
        while (dividend > 0) {
            def modulo = (dividend - 1) % 26
            name.append((65 + modulo) as char)
            dividend = (dividend - 1).intdiv(26)
        }
        name.reverse().toString()
    }

    static final cellRegex = /^(\$?)([A-Z]+)(\$?)(\d+)$/

    static CellRef from(String address) {
        def m = address =~ cellRegex
        if (m.find()) {
            new CellRef(
                    m.group(4).toInteger(),
                    columnNameToNumber(m.group(2)),
                    !m.group(3),
                    !m.group(1)
            )
        } else {
            null
        }
    }
}
