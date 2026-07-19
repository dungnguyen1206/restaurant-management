package com.rroms.restaurantmanagement.specfication;

import com.rroms.restaurantmanagement.criteria.MenuItemCriteria;
import com.rroms.restaurantmanagement.entity.MenuItem;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MenuItemSpecification {

    public static Specification<MenuItem> build(MenuItemCriteria criteria) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // search itemName + description

            if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {

                String keyword = "%" + criteria.getKeyword().toLowerCase() + "%";

                predicates.add(cb.or(cb.like(cb.lower(root.get("itemName")), keyword),
                               cb.like(cb.lower(root.get("description")), keyword))
                );
            }
            // category

            if (criteria.getCategoryId() != null) {
                predicates.add(cb.equal(

                        root.get("category").get("categoryId"),

                        criteria.getCategoryId()
                ));
            }

            // sold out

            if (criteria.getSoldOut() != null) {
                predicates.add(cb.equal(root.get("isSoldOut"), criteria.getSoldOut()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

    }
}
