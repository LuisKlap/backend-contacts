package com.uex.contacts.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "owner")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "contacts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_cpf", columnNames = { "owner_id", "cpf" })
})
public class Contact {

  @EqualsAndHashCode.Include
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", length = 150, nullable = false)
  private String name;

  @Column(name = "cpf", length = 20, nullable = false)
  private String cpf;

  @Column(name = "phone", length = 30, nullable = false)
  private String phone;

  @Column(name = "cep", length = 9, nullable = false)
  private String cep;

  @Column(name = "state", length = 2, nullable = false)
  private String state;

  @Column(name = "city", length = 100, nullable = false)
  private String city;

  @Column(name = "street", length = 200, nullable = false)
  private String street;

  @Column(name = "number", length = 20, nullable = false)
  private String number;

  @Column(name = "complement", length = 100)
  private String complement;

  @Column(name = "latitude")
  private BigDecimal latitude;

  @Column(name = "longitude")
  private BigDecimal longitude;

  @Column(name = "neighborhood", length = 100, nullable = false)
  private String neighborhood;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_contact_user"))
  private User owner;
}
