package com.space.repository;

import com.space.model.Ship;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipCrudRepository extends PagingAndSortingRepository<Ship, Long> {

}
