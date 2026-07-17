package com.rroms.restaurantmanagement.repository.projection;

import com.rroms.restaurantmanagement.entity.constant.TableArea;
import com.rroms.restaurantmanagement.entity.constant.TableStatus;

public interface TableView {
    Long getTableId();

    String getTableNumber();

    TableStatus getStatus();

    TableArea getArea();

    Integer getCapacity();

    Long getAssignedWaiterId();

    String getAssignedWaiterName();
}
