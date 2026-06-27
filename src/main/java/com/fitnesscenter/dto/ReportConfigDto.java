package com.fitnesscenter.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate; // Using java.time for modern date handling
import java.util.List;
import java.util.Objects;

@Setter
@Getter
public class ReportConfigDto {


    private String entityName;
    private List<SelectFieldDto> selectFields;
    private List<FilterConditionDto> filterConditions;
    private List<String> groupByFields;
    private List<HavingConditionDto> havingConditions;
    private List<SortOrderDto> sortOrders;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String outputFormat;
    private String reportName;
    private String companyName;
    private LocalDate reportDate;

    public static class SelectFieldDto {
        private String fieldPath;
        private String aggregateFunction;
        private String alias;

        public SelectFieldDto() {}

        public SelectFieldDto(String fieldPath, String aggregateFunction, String alias) {
            this.fieldPath = fieldPath;
            this.aggregateFunction = aggregateFunction;
            this.alias = alias;
        }

        public String getFieldPath() {
            return fieldPath;
        }

        public void setFieldPath(String fieldPath) {
            this.fieldPath = fieldPath;
        }

        public String getAggregateFunction() {
            return aggregateFunction;
        }

        public void setAggregateFunction(String aggregateFunction) {
            this.aggregateFunction = aggregateFunction;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SelectFieldDto that = (SelectFieldDto) o;
            return Objects.equals(fieldPath, that.fieldPath) &&
                    Objects.equals(aggregateFunction, that.aggregateFunction) &&
                    Objects.equals(alias, that.alias);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldPath, aggregateFunction, alias);
        }
    }

    public static class FilterConditionDto {
        private String fieldPath;
        private String operator;
        private Object value;
        private Object value2;

        public FilterConditionDto() {}

        public FilterConditionDto(String fieldPath, String operator, Object value) {
            this.fieldPath = fieldPath;
            this.operator = operator;
            this.value = value;
        }

        public FilterConditionDto(String fieldPath, String operator, Object value, Object value2) {
            this.fieldPath = fieldPath;
            this.operator = operator;
            this.value = value;
            this.value2 = value2;
        }


        public String getFieldPath() {
            return fieldPath;
        }

        public void setFieldPath(String fieldPath) {
            this.fieldPath = fieldPath;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Object getValue2() {
            return value2;
        }

        public void setValue2(Object value2) {
            this.value2 = value2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FilterConditionDto that = (FilterConditionDto) o;
            return Objects.equals(fieldPath, that.fieldPath) &&
                    Objects.equals(operator, that.operator) &&
                    Objects.equals(value, that.value) &&
                    Objects.equals(value2, that.value2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldPath, operator, value, value2);
        }
    }

    public static class HavingConditionDto {
        private String aggregateAlias;
        private String operator;
        private Object value;

        public HavingConditionDto() {}

        public HavingConditionDto(String aggregateAlias, String operator, Object value) {
            this.aggregateAlias = aggregateAlias;
            this.operator = operator;
            this.value = value;
        }

        public String getAggregateAlias() {
            return aggregateAlias;
        }

        public void setAggregateAlias(String aggregateAlias) {
            this.aggregateAlias = aggregateAlias;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HavingConditionDto that = (HavingConditionDto) o;
            return Objects.equals(aggregateAlias, that.aggregateAlias) &&
                    Objects.equals(operator, that.operator) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(aggregateAlias, operator, value);
        }
    }
    public static class SortOrderDto {
        private String fieldOrAlias;
        private String direction;

        public SortOrderDto() {}

        public SortOrderDto(String fieldOrAlias, String direction) {
            this.fieldOrAlias = fieldOrAlias;
            this.direction = direction;
        }

        public String getFieldOrAlias() {
            return fieldOrAlias;
        }

        public void setFieldOrAlias(String fieldOrAlias) {
            this.fieldOrAlias = fieldOrAlias;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SortOrderDto that = (SortOrderDto) o;
            return Objects.equals(fieldOrAlias, that.fieldOrAlias) &&
                    Objects.equals(direction, that.direction);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldOrAlias, direction);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportConfigDto that = (ReportConfigDto) o;
        return Objects.equals(entityName, that.entityName) &&
                Objects.equals(selectFields, that.selectFields) &&
                Objects.equals(filterConditions, that.filterConditions) &&
                Objects.equals(groupByFields, that.groupByFields) &&
                Objects.equals(havingConditions, that.havingConditions) &&
                Objects.equals(sortOrders, that.sortOrders) &&
                Objects.equals(dateFrom, that.dateFrom) &&
                Objects.equals(dateTo, that.dateTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityName, selectFields, filterConditions, groupByFields, havingConditions, sortOrders, dateFrom, dateTo);
    }
}