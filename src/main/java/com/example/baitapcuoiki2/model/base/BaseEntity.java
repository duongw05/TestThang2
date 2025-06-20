package com.example.baitapcuoiki2.model.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "createdDate", updatable = false)
    private Date createdDate;

    @LastModifiedDate
    @Column(name = "modifiedDate")
    private Date modifiedDate;

    @CreatedBy
    @Column(name = "createdBy", updatable = false, length = 255)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "modifiedBy", length = 255)
    private String modifiedBy;
}
