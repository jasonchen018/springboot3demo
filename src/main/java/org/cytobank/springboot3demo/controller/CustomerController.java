package org.cytobank.springboot3demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.cytobank.springboot3demo.constants.CaffeineCacheConstants;
import org.cytobank.springboot3demo.indentify.CustomKeyGenerator;
import org.cytobank.springboot3demo.model.Customer;
import org.cytobank.springboot3demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * @author jasonchen
 */
@RestController
@RequestMapping("/api/v1/customers")
@Slf4j
@CacheConfig(cacheNames = {CaffeineCacheConstants.CACHE_NAME_CUSTOMERS})
public class CustomerController {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomKeyGenerator customKeyGenerator;

    @GetMapping
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    record NewCustomerRequest(String name, String email, Integer age) {
    }

    @GetMapping("/{customerId}")
//    @Cacheable(keyGenerator = "customKeyGenerator")
    @Cacheable(value = CaffeineCacheConstants.CACHE_NAME_CUSTOMERS, key = "#id", unless = "#result == null")
    public ResponseEntity<Customer> find(@PathVariable("customerId") Integer id) {
        log.info("Customer data fetched from database:: " + id);
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.map(value -> new ResponseEntity<>(value, HttpStatus.ACCEPTED)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @PostMapping
    @CachePut(value = CaffeineCacheConstants.CACHE_NAME_CUSTOMERS, key = "#result.body.id", unless = "#result == null")
    public ResponseEntity<Customer> save(@RequestBody NewCustomerRequest request) {
        Customer customer = new Customer();
        customer.setAge(request.age);
        customer.setName(request.name);
        customer.setEmail(request.email);

        return new ResponseEntity<>(customerRepository.save(customer), HttpStatus.CREATED);
    }

    @DeleteMapping("{customerId}")
    @CacheEvict(value = CaffeineCacheConstants.CACHE_NAME_CUSTOMERS, key = "#id", allEntries = true)
    public ResponseEntity<HttpStatus> deleteCustomer(@PathVariable("customerId") Integer id) {
        Optional<Customer> customer = customerRepository.findById(id);
        customer.ifPresent(value -> customerRepository.delete(value));

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/delete/all")
    @Caching(evict = {@CacheEvict(value = CaffeineCacheConstants.CACHE_NAME_CUSTOMERS, allEntries = true)})
    public ResponseEntity<HttpStatus> deleteAll() {
        log.info("Clean all cache");
        customerRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.LOOP_DETECTED);
    }

    @PutMapping("{customerId}")
    @CachePut(value = CaffeineCacheConstants.CACHE_NAME_CUSTOMERS, key = "#id")
    public ResponseEntity<Customer> updateCustomer(@PathVariable("customerId") Integer id, @RequestBody NewCustomerRequest request) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            customer.get().setAge(request.age);
            customer.get().setName(request.name);
            customer.get().setEmail(request.email);
            final Customer updateCustomer = customerRepository.save(customer.get());
            return ResponseEntity.ok(updateCustomer);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}