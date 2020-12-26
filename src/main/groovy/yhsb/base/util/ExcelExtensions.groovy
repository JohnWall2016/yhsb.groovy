package yhsb.base.util

import org.apache.poi.hssf.usermodel.HSSFWorkbook
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
    static void save(Workbook self, String fileName) {
        self.save(Paths.get(fileName))
    }

    static void save(Workbook self, Path file) {
        Files.newOutputStream(file).withCloseable {
            self.write(it)
        }
    }

    static Row createRow(
            Sheet self,
            int targetRowIndex,
            int sourceRowIndex,
            boolean clearValue
    ) {
        if (targetRowIndex == sourceRowIndex) {
            throw new IllegalArgumentException(
                    "sourceIndex and targetIndex cannot be same: $sourceRowIndex == $targetRowIndex"
            )
        }

        def srcRow = self.getRow(sourceRowIndex)
        def newRow = self.createRow(targetRowIndex)
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
        for (index in 0..<self.numMergedRegions) {
            def address = self.getMergedRegion(index)
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
            self.addMergedRegion(region)
        }

        newRow
    }

    static Row getOrCopyRow(Sheet self, int targetRowIndex, int sourceRowIndex, boolean clearValue = false) {
        if (targetRowIndex == sourceRowIndex) {
            self.getRow(sourceRowIndex)
        } else {
            if (self.lastRowNum >= targetRowIndex) {
                self.shiftRows(targetRowIndex, self.lastRowNum, 1, true, false)
            }
            self.createRow(targetRowIndex, sourceRowIndex, clearValue)
        }
    }

    static void copyRows(Sheet self, int startRowIndex, int count, int sourceRowIndex, boolean clearValue = false) {
        self.shiftRows(startRowIndex, self.lastRowNum, count, true, false)
        for (i in 0..<count) {
            self.createRow(startRowIndex + i, sourceRowIndex, clearValue)
        }
    }

    static Iterator<Row> rowIterator(Sheet self, int start, int end = -1) {
        new Iterator<Row>() {
            private int index = Math.max(0, start)
            private int last = end == -1 ? self.lastRowNum : Math.min(end, self.lastRowNum)

            @Override
            boolean hasNext() {
                index <= last
            }

            @Override
            Row next() {
                self.getRow(index++)
            }
        }
    }
}
