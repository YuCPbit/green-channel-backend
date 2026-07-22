package edu.greenchannel.subsidy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "gc_college")
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "college_name", nullable = false)
    private String collegeName;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
