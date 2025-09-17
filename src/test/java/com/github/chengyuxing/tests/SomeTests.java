package com.github.chengyuxing.tests;


import javax.persistence.Entity;
import javax.persistence.Id;

public class SomeTests {
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
