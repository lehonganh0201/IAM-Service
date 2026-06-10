package com.example.iamservice.aop.aspect;

import com.example.iamservice.aop.annotation.RateLimit;
import org.aspectj.lang.annotation.Pointcut;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 10:10
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public class CommonJoinPointConfig {
    @Pointcut("execution(* com.example.iamservice.controller..*(..))")
    public void controllerPointcut() {
        // Pointcut for all methods in controller package
    }

    @Pointcut("execution(* com.example.iamservice.repository..*(..))")
    public void repositoryPointcut() {
        // Pointcut for all methods in repository package
    }

    @Pointcut("@annotation(rateLimit)")
    public void rateLimitPointcut(RateLimit rateLimit) {

    }

    @Pointcut("execution(* com.example.iamservice.controller.AuthController.login(..)) || " +
            "execution(* com.example.iamservice.controller.AuthController.logout(..)) || " +
            "execution(* com.example.iamservice.controller.UserController.updateUserPassword(..)) || " +
            "execution(* com.example.iamservice.controller.UserController.updateUser(..)) || " +
            "execution(* com.example.iamservice.controller.UserController.getMe(..))")
    public void userActivityLogController() {}
}

