package org.labcabrera.parking.catalog.infrastructure.jpa;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RsqlSpecificationBuilder {

    private static final Pattern CLAUSE = Pattern.compile("(\\w+)(==|!=|=gt=|=lt=)(.+)");

    private RsqlSpecificationBuilder() {
    }

    public static Specification<ParkingFacilityJpaEntity> build(String rsql) {
        if (rsql == null || rsql.isBlank()) {
            return null;
        }
        String[] clauses = rsql.split(";");
        return (Root<ParkingFacilityJpaEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (String c : clauses) {
                Matcher m = CLAUSE.matcher(c.trim());
                if (!m.matches()) {
                    continue;
                }
                String field = m.group(1);
                String op = m.group(2);
                String rawValue = m.group(3);
                String value = stripQuotes(rawValue);
                try {
                    switch (op) {
                    case "==":
                        if (value.contains("*")) {
                            String like = value.replace("*", "%").toLowerCase();
                            predicates.add(cb.like(cb.lower(root.get(field)), like));
                        }
                        else {
                            predicates.add(cb.equal(root.get(field), convertValue(root, field, value)));
                        }
                        break;
                    case "!=":
                        predicates.add(cb.notEqual(root.get(field), convertValue(root, field, value)));
                        break;
                    case "=gt=":
                        predicates.add(cb.greaterThan(root.get(field).as(Double.class), toNumber(value)));
                        break;
                    case "=lt=":
                        predicates.add(cb.lessThan(root.get(field).as(Double.class), toNumber(value)));
                        break;
                    }
                }
                catch (Exception e) {
                    // skip invalid clause
                }
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String stripQuotes(String s) {
        s = s.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static Double toNumber(String s) {
        try {
            return Double.valueOf(s);
        }
        catch (NumberFormatException e) {
            return Double.valueOf(0);
        }
    }

    private static Object convertValue(Root<ParkingFacilityJpaEntity> root, String field, String value) {
        // try to infer numeric types from field name
        if (field.equals("totalSpots") || field.equals("freeCancelHours") || field.equals("penaltyCancelMinutes")) {
            try {
                return Integer.valueOf(value);
            }
            catch (NumberFormatException e) {
            }
        }
        if (field.equals("latitude") || field.equals("longitude")) {
            try {
                return Double.valueOf(value);
            }
            catch (NumberFormatException e) {
            }
        }
        if (field.equals("dailyRate")) {
            try {
                return new java.math.BigDecimal(value);
            }
            catch (NumberFormatException e) {
            }
        }
        // default to String
        return value;
    }
}
