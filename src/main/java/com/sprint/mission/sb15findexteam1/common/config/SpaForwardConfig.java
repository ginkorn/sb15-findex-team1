package com.sprint.mission.sb15findexteam1.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 프론트엔드(React) 화면 주소를 index.html로 연결하는 설정.
 *
 * <p>화면 파일은 src/main/resources/static에 있고, Spring이 그대로 제공한다.
 * React는 /dashboard 같은 주소를 브라우저 안에서만 바꾸기 때문에,
 * 그 주소에서 새로고침하면 Spring은 "/dashboard"라는 파일을 찾다가 404(Whitelabel Error Page)를 낸다.
 * 그래서 화면 주소로 들어온 요청은 index.html로 넘겨 React가 처리하게 한다.
 *
 * <p>화면 주소 목록은 frontend/src/main.tsx의 라우터 설정과 같아야 한다.
 * /api/** 요청은 여기에 해당하지 않으므로 그대로 컨트롤러가 처리한다.
 */
@Configuration
public class SpaForwardConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("forward:/index.html");
    registry.addViewController("/dashboard").setViewName("forward:/index.html");
    registry.addViewController("/index-management").setViewName("forward:/index.html");
    registry.addViewController("/data-management").setViewName("forward:/index.html");
    registry.addViewController("/integrations").setViewName("forward:/index.html");
  }
}
