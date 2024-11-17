package com.github.chengyuxing.tests;

import com.github.chengyuxing.sql.EntityManager;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.Id;

public class SomeTests {
    @Test
    public void test1() {
        EntityManager entityManager = new EntityManager(':');
        System.out.println(entityManager.getEntityMeta(Me.class));
    }

    @Entity
    public class Me {

        @Id
        private Long id;

        public void setId(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}
