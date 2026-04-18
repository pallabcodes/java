package com.backend.designpatterns.creational.abstractfactory;

/**
 * THE ABSTRACT FACTORY
 * 
 * Defines factory methods for EACH product in the family.
 * Ensures that Storage and Logger always originate from the same ecosystem
 * (family).
 * 
 * The InfrastructureFactory interface defines two factory methods, and their
 * return types are the Product Interfaces (Storage and AuditLogger).
 */

public interface InfrastructureFactory {

    /**
     * Returns an object that MUST implement the Storage interface.
     * 
     * THE "MASKING" EFFECT:
     * Even if the returned object is an 'S3Storage' with extra methods like
     * 'getBucketName()',
     * the caller ONLY sees the 'Storage' interface. This "strips away" or excludes
     * implementation-
     * specific fields from the client's view.
     * 
     * WHY? (Safety Feature):
     * It prevents the client from becoming "addicted" to AWS-specific features,
     * ensuring the family can be swapped (e.g., to On-Prem) without breaking client
     * code.
     */
    Storage createStorage(); // Must return SOMETHING that implements Storage

    /**
     * Returns an ecosystem-compatible AuditLogger.
     */
    AuditLogger createLogger(); // Must return SOMETHING that implements AuditLogger
}

// Why do it this way? It ensures that if you are in an "AWS" mode, you don't
// accidentally get a "Local" logger. The factory forces them to come from the
// same family.