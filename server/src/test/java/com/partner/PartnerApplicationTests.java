package com.partner;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
            "spring.autoconfigure.exclude="
                    + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        })
class PartnerApplicationTests {

    @Test
    void contextLoads() {
    }
}
