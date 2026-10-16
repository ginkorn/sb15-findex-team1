package com.sprint.mission.sb15findexteam1.common.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 쿼리를 만드는 JPAQueryFactory를 스프링 빈으로 등록한다.
 *
 * <p>Repository에서 생성자로 주입받아 쓰면 된다.
 * <pre>
 * private final JPAQueryFactory queryFactory;
 * </pre>
 * (기존처럼 {@code new JPAQueryFactory(entityManager)}로 직접 만들어도 동작한다)
 */
@Configuration
public class QuerydslConfig {

  @Bean
  public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
    return new JPAQueryFactory(entityManager);
  }
}
