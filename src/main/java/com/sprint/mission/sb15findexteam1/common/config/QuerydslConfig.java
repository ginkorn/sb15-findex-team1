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
 *
 * <p>주의: {@code @DataJpaTest}는 JPA 관련 빈만 띄우기 때문에 이 설정이 로드되지 않는다.
 * Repository 구현체(XxxQueryRepositoryImpl)는 팀 공통 방식대로
 * {@code new JPAQueryFactory(entityManager)}로 직접 만들어 쓰는 것을 권장한다.
 * 이 빈은 서비스 등 그 밖의 곳에서 필요할 때 쓴다.
 */
@Configuration
public class QuerydslConfig {

  @Bean
  public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
    return new JPAQueryFactory(entityManager);
  }
}
