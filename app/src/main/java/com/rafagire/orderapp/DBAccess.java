package com.rafagire.orderapp;

import java.util.List;

public interface DBAccess{
    void createTable();
    void deleteTable();
    void add(Product product) throws DBException;
    void update(Product product) throws DBException;
    void remove(int id) throws DBException;
    Product findById(int id) throws DBException;
    List<Product> findByType(String type) throws DBException;
    List<Product> findByName(String name) throws DBException;
    List<Product> findAll() throws DBException;
}
