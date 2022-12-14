package org.cytobank.springboot3demo.repository;

import org.cytobank.springboot3demo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author jasonchen
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
