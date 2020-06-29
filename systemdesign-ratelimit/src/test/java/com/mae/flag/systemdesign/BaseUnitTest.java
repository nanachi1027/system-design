package com.mae.flag.systemdesign;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * We let the BaseUnitTest under the same package with RateLimitApplication -- entry of SpringApplication
 * to get rid of exceptions during unit tests running perrod.
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class BaseUnitTest {
}
