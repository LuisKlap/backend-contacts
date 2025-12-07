package com.uex.contacts.repository;

import com.uex.contacts.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

  Page<Contact> findByOwnerId(Long ownerId, Pageable pageable);

  Optional<Contact> findByOwnerIdAndCpf(Long ownerId, String cpf);

  boolean existsByOwnerIdAndCpf(Long ownerId, String cpf);

  Page<Contact> findByOwnerIdAndNameContainingIgnoreCase(Long ownerId, String name, Pageable pageable);

  Page<Contact> findByOwnerIdAndCpfContaining(Long ownerId, String cpfFragment, Pageable pageable);

  void deleteByOwnerId(Long ownerId);

  @Query(value = "SELECT * FROM contact c WHERE c.owner_id = :ownerId AND regexp_replace(c.cpf, '\\D', '', 'g') LIKE concat('%', :digits, '%')", countQuery = "SELECT count(*) FROM contact c WHERE c.owner_id = :ownerId AND regexp_replace(c.cpf, '\\D', '', 'g') LIKE concat('%', :digits, '%')", nativeQuery = true)
  Page<Contact> findByOwnerIdAndCpfDigitsContaining(@Param("ownerId") Long ownerId, @Param("digits") String digits,
      Pageable pageable);
}
