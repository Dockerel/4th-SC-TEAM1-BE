package com.gdg.Todak.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.common.interceptor.QueryCountInterceptor;
import com.gdg.Todak.member.Interceptor.AdminLoginCheckInterceptor;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.service.AuthService;
import com.gdg.Todak.member.service.MemberService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = {
        AuthController.class,
        MemberController.class
})
@Import(TestWebConfig.class)
public abstract class ControllerTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MemberService memberService;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    AdminLoginCheckInterceptor adminLoginCheckInterceptor;

    @MockitoBean
    QueryCountInterceptor queryCountInterceptor;

    @MockitoBean
    MemberRepository memberRepository;

    @BeforeEach
    void setup() throws Exception {
        when(queryCountInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }
}

