package com.fitnesscenter.service;

import com.fitnesscenter.dto.ReportConfigDto;
import com.fitnesscenter.entity.Client;
import com.fitnesscenter.entity.RegistrationOfVisit;
import com.fitnesscenter.entity.Sale;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.apache.poi.xwpf.usermodel.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ReportService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Map<String, Object>> generateReportData(ReportConfigDto config) {
        String entityName = config.getEntityName();
        if (entityName == null || entityName.trim().isEmpty()) {
            throw new IllegalArgumentException("Entity name must be specified in ReportConfigDto.");
        }

        if ("Sale".equalsIgnoreCase(entityName)) {
            return generateDynamicReportForEntity(config, Sale.class);
        } else if ("Client".equalsIgnoreCase(entityName)) {
            return generateDynamicReportForEntity(config, Client.class);
        } else if("RegistrationOfVisit".equalsIgnoreCase(entityName)) {
            return generateDynamicReportForEntity(config, RegistrationOfVisit.class);
        }

        throw new IllegalArgumentException("Unsupported entity type for reporting: " + entityName);
    }

    private <T> List<Map<String, Object>> generateDynamicReportForEntity(ReportConfigDto config, Class<T> entityClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<T> root = cq.from(entityClass);

        Map<String, Join<?, ?>> activeJoins = new HashMap<>();
        List<Selection<?>> selections = new ArrayList<>();
        List<Expression<?>> groupByExpressions = new ArrayList<>();
        List<Predicate> predicates = new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        List<Predicate> havingPredicates = new ArrayList<>();

        if (config.getSelectFields() == null || config.getSelectFields().isEmpty()) {
            throw new IllegalArgumentException("At least one select field must be specified.");
        }
        for (ReportConfigDto.SelectFieldDto selectField : config.getSelectFields()) {
            Path<?> fieldPath = getPath(root, selectField.getFieldPath(), activeJoins);
            Selection<?> selection;
            String alias = selectField.getAlias() != null && !selectField.getAlias().isEmpty()
                    ? selectField.getAlias()
                    : selectField.getFieldPath().replace(".", "_");

            if (selectField.getAggregateFunction() != null && !selectField.getAggregateFunction().isEmpty()) {
                // Handle aggregate functions
                selection = switch (selectField.getAggregateFunction().toUpperCase()) {
                    case "COUNT" -> cb.count(fieldPath).alias(alias);
                    case "SUM" -> cb.sum(fieldPath.as(Double.class)).alias(alias);
                    case "AVG" -> cb.avg(fieldPath.as(Double.class)).alias(alias);
                    // Add MIN, MAX etc.
                    default ->
                            throw new IllegalArgumentException("Unsupported aggregate function: " + selectField.getAggregateFunction());
                };
            } else {
                selection = fieldPath.alias(alias);
                groupByExpressions.add(fieldPath);
            }
            selections.add(selection);
        }
        cq.multiselect(selections);

        if (!groupByExpressions.isEmpty()) {
            cq.groupBy(groupByExpressions);
        }

        if (config.getFilterConditions() != null) {
            for (ReportConfigDto.FilterConditionDto filter : config.getFilterConditions()) {
                Path<?> filterPath = getPath(root, filter.getFieldPath(), activeJoins);
                predicates.add(createPredicate(cb, filterPath, filter.getOperator(), filter.getValue(), filter.getValue2()));
            }
        }

        if ("Sale".equalsIgnoreCase(config.getEntityName())) {
            if (config.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(getPath(root, "startDate", activeJoins).as(LocalDate.class), config.getDateFrom()));
            }
            if (config.getDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(getPath(root, "startDate", activeJoins).as(LocalDate.class), config.getDateTo()));
            }
        }


        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        if (config.getGroupByFields() != null && !config.getGroupByFields().isEmpty()) {
            for (String groupByField : config.getGroupByFields()) {
                groupByExpressions.add(getPath(root, groupByField, activeJoins));
            }
            cq.groupBy(groupByExpressions);
        }

        if (config.getHavingConditions() != null && !config.getHavingConditions().isEmpty()) {
            if (config.getGroupByFields() == null || config.getGroupByFields().isEmpty()) {
                throw new IllegalArgumentException("HAVING clause requires a GROUP BY clause.");
            }
            for (ReportConfigDto.HavingConditionDto having : config.getHavingConditions()) {
                Expression<?> aggExpression = findAliasedExpression(selections, having.getAggregateAlias());
                if (aggExpression == null) {
                    throw new IllegalArgumentException("HAVING clause error: Could not find aliased aggregate expression for '" + having.getAggregateAlias() + "'");
                }

                Object value = having.getValue();
                if (aggExpression.getJavaType() != null) {
                    Class<?> javaType = aggExpression.getJavaType();
                    if (Number.class.isAssignableFrom(javaType)) {
                        if (javaType == Long.class) {
                            value = value instanceof Long ? value : Long.parseLong(value.toString());
                        } else if (javaType == Integer.class) {
                            value = value instanceof Integer ? value : Integer.parseInt(value.toString());
                        } else if (javaType == Double.class) {
                            value = value instanceof Double ? value : Double.parseDouble(value.toString());
                        }
                    }
                }
                havingPredicates.add(createPredicate(cb, aggExpression, having.getOperator(), value, null));
            }
            cq.having(cb.and(havingPredicates.toArray(new Predicate[0])));
        }

        if (config.getSortOrders() != null && !config.getSortOrders().isEmpty()) {
            for (ReportConfigDto.SortOrderDto sortOrder : config.getSortOrders()) {
                Expression<?> sortExpression = findAliasedExpression(selections, sortOrder.getFieldOrAlias());
                Path<?> sortPath = null;
                if (sortExpression == null) {
                    try {
                        sortPath = getPath(root, sortOrder.getFieldOrAlias(), activeJoins);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("ORDER BY error: Invalid field or alias name - " + sortOrder.getFieldOrAlias(), e);
                    }
                }

                Order order;
                Expression<?> expressionToOrder = sortExpression != null ? sortExpression : sortPath;

                if ("desc".equalsIgnoreCase(sortOrder.getDirection())) {
                    order = cb.desc(expressionToOrder);
                } else {
                    order = cb.asc(expressionToOrder);
                }
                orders.add(order);
            }
            cq.orderBy(orders);
        }

        List<Tuple> resultTuples = entityManager.createQuery(cq).getResultList();
        return convertTuplesToMapList(resultTuples, selections);
    }

    private Path<?> getPath(Path<?> initialPath, String fieldPath, Map<String, Join<?, ?>> createdJoins) {
        String[] parts = fieldPath.split("\\.");

        if (fieldPath.startsWith("sale.")) {
            if (!(initialPath instanceof Root<?> root) || !root.getJavaType().getSimpleName().equals("RegistrationOfVisit")) {
                throw new IllegalArgumentException("Field path 'sale.*' is only allowed for RegistrationOfVisit entity");
            }

            Path<Long> saleIdPath = initialPath.get("saleId");

            Join<RegistrationOfVisit, Sale> saleJoin = createdJoins.containsKey("sale")
                    ? (Join<RegistrationOfVisit, Sale>) createdJoins.get("sale")
                    : ((Root<RegistrationOfVisit>) initialPath).join("saleId", JoinType.LEFT);

            createdJoins.putIfAbsent("sale", saleJoin);
            Path<?> currentPath = saleJoin;

            for (int i = 1; i < parts.length; i++) {
                currentPath = currentPath.get(parts[i]);
            }
            return currentPath;
        }

        Path<?> currentPath = initialPath;
        String joinKeyPrefix = initialPath instanceof Root ? ((Root<?>) initialPath).getJavaType().getSimpleName() : "";

        for (int i = 0; i < parts.length - 1; i++) {
            String currentJoinAttribute = parts[i];
            StringBuilder joinMapKey = new StringBuilder(joinKeyPrefix + "." + currentJoinAttribute);

            Join<?, ?> join = createdJoins.get(joinMapKey.toString());
            if (join == null) {
                if (currentPath instanceof From) {
                    join = ((From<?, ?>) currentPath).join(currentJoinAttribute, JoinType.LEFT);
                    createdJoins.put(joinMapKey.toString(), join);
                } else {
                    throw new IllegalArgumentException("Cannot create join from path: " + currentPath);
                }
            }
            currentPath = join;
            joinKeyPrefix = joinMapKey.toString();
        }
        return currentPath.get(parts[parts.length - 1]);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate createPredicate(CriteriaBuilder cb, Expression<?> path, String operator, Object value, Object value2) {
        if (value == null && !"IS_NULL".equalsIgnoreCase(operator) && !"IS_NOT_NULL".equalsIgnoreCase(operator)) {
            throw new IllegalArgumentException("Value cannot be null for operator: " + operator);
        }

        return switch (operator.toUpperCase()) {
            case "EQUALS" -> cb.equal(path, value);
            case "NOT_EQUALS" -> cb.notEqual(path, value);
            case "LIKE" -> {
                assert value != null;
                yield cb.like(path.as(String.class), "%" + value + "%");
            }
            case "STARTS_WITH" -> {
                assert value != null;
                yield cb.like(path.as(String.class), value + "%");
            }
            case "ENDS_WITH" -> {
                assert value != null;
                yield cb.like(path.as(String.class), "%" + value);
            }
            case "GREATER_THAN" -> {
                if (value instanceof Comparable)
                    yield cb.greaterThan((Expression<? extends Comparable>) path, (Comparable) value);
                throw new IllegalArgumentException("Value must be Comparable for GREATER_THAN");
            }
            case "GREATER_THAN_OR_EQUALS" -> {
                if (value instanceof Comparable)
                    yield cb.greaterThanOrEqualTo((Expression<? extends Comparable>) path, (Comparable) value);
                throw new IllegalArgumentException("Value must be Comparable for GREATER_THAN_OR_EQUALS");
            }
            case "LESS_THAN" -> {
                if (value instanceof Comparable)
                    yield cb.lessThan((Expression<? extends Comparable>) path, (Comparable) value);
                throw new IllegalArgumentException("Value must be Comparable for LESS_THAN");
            }
            case "LESS_THAN_OR_EQUALS" -> {
                if (value instanceof Comparable)
                    yield cb.lessThanOrEqualTo((Expression<? extends Comparable>) path, (Comparable) value);
                throw new IllegalArgumentException("Value must be Comparable for LESS_THAN_OR_EQUALS");
            }
            case "BETWEEN" -> {
                if (value instanceof Comparable && value2 instanceof Comparable)
                    yield cb.between((Expression<? extends Comparable>) path, (Comparable) value, (Comparable) value2);
                throw new IllegalArgumentException("Values for BETWEEN must be Comparable");
            }
            case "IN" -> {
                if (value instanceof List) yield path.in((List<?>) value);
                throw new IllegalArgumentException("Value for IN operator must be a List");
            }
            case "IS_NULL" -> cb.isNull(path);
            case "IS_NOT_NULL" -> cb.isNotNull(path);
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }

    private Expression<?> findAliasedExpression(List<Selection<?>> selections, String alias) {
        for (Selection<?> sel : selections) {
            if (alias.equals(sel.getAlias()) && sel instanceof Expression) {
                return (Expression<?>) sel;
            }
        }
        return null;
    }

    private List<Map<String, Object>> convertTuplesToMapList(List<Tuple> tuples, List<Selection<?>> selections) {
        return tuples.stream()
                .map(tuple -> {
                    Map<String, Object> row = new HashMap<>();
                    for (Selection<?> selection : selections) {
                        String alias = selection.getAlias();
                        if (alias != null) {
                            row.put(alias, tuple.get(alias));
                        } else if (selection instanceof Path<?> path) {
                            String pathStr = path.toString();
                            String fieldName = pathStr.contains(".") ? pathStr.substring(pathStr.lastIndexOf('.') + 1) : pathStr;
                            fieldName = fieldName.startsWith(path.getModel().getBindableJavaType().getSimpleName()+".") ?
                                    fieldName.substring(path.getModel().getBindableJavaType().getSimpleName().length()+1) : fieldName;
                            row.put(fieldName, tuple.get(path));
                        } else {
                            row.put("unknown_" + selections.indexOf(selection), tuple.get(selection));
                        }
                    }
                    return row;
                })
                .collect(Collectors.toList());
    }

    public byte[] exportReportToExcel(
            List<Map<String, Object>> reportData,
            List<String> headers,
            String reportTitle,
            String companyName,
            LocalDate reportDate) throws IOException {

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Report");
            int numberOfColumns = headers.size();

            Font defaultFont = workbook.createFont();
            defaultFont.setFontName("Calibri");
            defaultFont.setFontHeightInPoints((short) 11);

            Font boldFont = workbook.createFont();
            boldFont.setFontName("Calibri");
            boldFont.setFontHeightInPoints((short) 11);
            boldFont.setBold(true);

            Font titleFont = workbook.createFont();
            titleFont.setFontName("Calibri");
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setBold(true);

            CellStyle baseBorderStyle = workbook.createCellStyle();
            baseBorderStyle.setBorderBottom(BorderStyle.THIN);
            baseBorderStyle.setBorderTop(BorderStyle.THIN);
            baseBorderStyle.setBorderLeft(BorderStyle.THIN);
            baseBorderStyle.setBorderRight(BorderStyle.THIN);
            baseBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle companyStyle = workbook.createCellStyle();
            companyStyle.setFont(boldFont);
            companyStyle.setAlignment(HorizontalAlignment.LEFT);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setFont(defaultFont);
            dateStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.cloneStyleFrom(baseBorderStyle);
            headerStyle.setFont(boldFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.cloneStyleFrom(baseBorderStyle);
            cellStyle.setFont(defaultFont);
            cellStyle.setAlignment(HorizontalAlignment.LEFT);

            CellStyle evenRowStyle = workbook.createCellStyle();
            evenRowStyle.cloneStyleFrom(cellStyle);
            evenRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            evenRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int currentRow = 0;

            Row companyRow = sheet.createRow(currentRow++);
            Cell companyCell = companyRow.createCell(0);
            companyCell.setCellValue(companyName != null ? companyName : "Company Name");
            companyCell.setCellStyle(companyStyle);

            Row titleRow = sheet.createRow(currentRow++);
            titleRow.setHeightInPoints(20);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(reportTitle != null ? reportTitle : "Отчет");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 0, numberOfColumns - 1));

            Row dateRow = sheet.createRow(currentRow++);
            Cell dateCell = dateRow.createCell(0);
            String formattedDate = (reportDate != null ? reportDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) :
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            dateCell.setCellValue("Дата создания отчёта: " + formattedDate);
            dateCell.setCellStyle(dateStyle);
            sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 0, numberOfColumns - 1));

            sheet.createRow(currentRow++);

            int headerRowIndex = currentRow;
            Row headerRow = sheet.createRow(currentRow++);
            headerRow.setHeightInPoints(15);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            for (Map<String, Object> dataRow : reportData) {
                Row row = sheet.createRow(currentRow);
                CellStyle currentDataRowStyle = (currentRow % 2 != 0) ? cellStyle : evenRowStyle;

                for (int i = 0; i < headers.size(); i++) {
                    String headerKey = headers.get(i);
                    Object value = dataRow.get(headerKey);
                    Cell cell = row.createCell(i);

                    cell.setCellStyle(currentDataRowStyle);

                    if (value == null) {
                        cell.setCellValue("");
                    } else if (value instanceof String) {
                        cell.setCellValue((String) value);
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof LocalDate || value instanceof LocalDateTime || value instanceof java.util.Date) {
                        CellStyle dateCellStyle = workbook.createCellStyle();
                        dateCellStyle.cloneStyleFrom(currentDataRowStyle);
                        dateCellStyle.setDataFormat(workbook.createDataFormat().getFormat("dd.MM.yyyy"));
                        cell.setCellStyle(dateCellStyle);
                        if (value instanceof LocalDate) {
                            cell.setCellValue(Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                        } else if (value instanceof LocalDateTime) {
                            cell.setCellValue(Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant()));
                        } else {
                            cell.setCellValue((java.util.Date) value);
                        }
                    } else if (value instanceof Boolean) {
                        cell.setCellValue((Boolean) value ? "Да" : "Нет");
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
                currentRow++;
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024);
            }

            sheet.setDisplayGridlines(true);
            sheet.setFitToPage(true);
            PrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setLandscape(true);
            printSetup.setFitWidth((short) 1);
            printSetup.setFitHeight((short) 0);
            sheet.setHorizontallyCenter(true);
            sheet.setAutoFilter(new CellRangeAddress(headerRowIndex, headerRowIndex, 0, numberOfColumns - 1));
            sheet.setRepeatingRows(new CellRangeAddress(headerRowIndex, headerRowIndex, 0, numberOfColumns - 1));
            workbook.setPrintArea(0, 0, numberOfColumns - 1, 0, currentRow - 1);

            Header header = sheet.getHeader();
            header.setCenter(reportTitle != null ? reportTitle : "Отчет");
            Footer footer = sheet.getFooter();
            footer.setRight("Page &P of &N");

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportReportToWord(List<Map<String, Object>> reportData, List<String> headers, String reportTitle, String companyName, LocalDate reportDate) throws IOException {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setIndentationLeft(720);
            paragraph.setIndentationRight(720);

            addHeader(document, companyName);
            addContent(document, reportTitle, reportDate);
            addTable(document, reportData, headers);

            document.write(out);
            return out.toByteArray();
        }
    }

    private void addHeader(XWPFDocument document, String companyName) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.RIGHT);

        XWPFRun run = paragraph.createRun();
        run.setText("Директору \"" + companyName + "\"");
        run.setBold(true);
        run.setFontSize(14);

        paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
        run = paragraph.createRun();
        run.setText("_________________________________________");
        run.setUnderline(UnderlinePatterns.SINGLE);

        paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
        run = paragraph.createRun();
        run.setText("Фамилия Имя Отчество");
        run.setFontSize(10);
    }

    private void addContent(XWPFDocument document, String reportTitle, LocalDate reportDate) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = paragraph.createRun();
        run.setText(reportTitle != null ? reportTitle : "Отчет");
        run.setBold(true);
        run.setFontSize(16);
        run.addBreak();

        run = paragraph.createRun();
        run.setText("Отчет за ______ месяц");
        run.setFontSize(12);
        run.addBreak();

        run = paragraph.createRun();
        LocalDate effectiveDate = reportDate != null ? reportDate : LocalDate.now();
        String formattedDate = effectiveDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        run.setText("Дата создания отчета: " + formattedDate);
        run.setFontSize(10);
    }

    private void addTable(XWPFDocument document, List<Map<String, Object>> reportData, List<String> headers) {
        if (reportData == null || reportData.isEmpty() || headers == null || headers.isEmpty()) {
            document.createParagraph().createRun().setText("Нет данных для отчета.");
            return;
        }

        XWPFTable table = document.createTable(reportData.size() + 1, headers.size());
        CTTblPr tblPr = table.getCTTbl().addNewTblPr();
        tblPr.addNewJc().setVal(STJcTable.CENTER);
        table.getCTTbl().addNewTblPr().addNewTblW().setW(BigInteger.valueOf(5000));

        for (int i = 0; i < table.getNumberOfRows(); i++) {
            XWPFTableRow row = table.getRow(i);
            for (int j = 0; j < row.getTableCells().size(); j++) {
                XWPFTableCell cell = row.getCell(j);
                CTTc tc = cell.getCTTc();
                CTTcPr tcPr = tc.isSetTcPr() ? tc.getTcPr() : tc.addNewTcPr();
                CTTcBorders borders = tcPr.isSetTcBorders() ? tcPr.getTcBorders() : tcPr.addNewTcBorders();

                CTBorder top = borders.addNewTop();
                top.setVal(STBorder.THICK);

                CTBorder bottom = borders.addNewBottom();
                bottom.setVal(STBorder.THICK);

                CTBorder left = borders.addNewLeft();
                left.setVal(STBorder.THICK);

                CTBorder right = borders.addNewRight();
                right.setVal(STBorder.THICK);
            }
        }

        XWPFTableRow headerRow = table.getRow(0);
        setHeaderRow(headerRow, headers);

        for (int i = 0; i < reportData.size(); i++) {
            XWPFTableRow dataRow = table.getRow(i + 1);
            Map<String, Object> rowMap = reportData.get(i);
            setDataRow(dataRow, rowMap, headers);
        }
    }

    private void setHeaderRow(XWPFTableRow row, List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            XWPFTableCell cell = row.getCell(i);
            XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
            p.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = p.createRun();
            run.setBold(true);
            run.setText(headers.get(i));
        }
    }

    private void setDataRow(XWPFTableRow row, Map<String, Object> rowData, List<String> headers) {
        for (int i = 0; i < row.getTableCells().size(); i++) {
            XWPFTableCell cell = row.getCell(i);
            XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
            p.setAlignment(ParagraphAlignment.LEFT);
            String header = headers.get(i);
            Object value = rowData.get(header);
            XWPFRun run = p.createRun();
            run.setText(value != null ? value.toString() : "");
        }
    }
}