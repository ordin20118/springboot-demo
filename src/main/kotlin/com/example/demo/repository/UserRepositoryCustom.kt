package com.example.demo.repository

import com.example.demo.domain.QUser
import com.example.demo.domain.User
import com.example.demo.domain.UserRole
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryCustom(
    private val queryFactory: JPAQueryFactory
) {
    
    fun findUsersByRole(role: UserRole): List<User> {
        val user = QUser.user
        
        return queryFactory
            .selectFrom(user)
            .where(user.role.eq(role))
            .fetch()
    }
    
    fun findUsersByEmailContaining(email: String): List<User> {
        val user = QUser.user
        
        return queryFactory
            .selectFrom(user)
            .where(user.email.containsIgnoreCase(email))
            .fetch()
    }
    
    fun findUsersByComplexCondition(name: String?, role: UserRole?): List<User> {
        val user = QUser.user
        
        val query = queryFactory.selectFrom(user)
        
        name?.let {
            query.where(user.name.containsIgnoreCase(it))
        }
        
        role?.let {
            query.where(user.role.eq(it))
        }
        
        return query.fetch()
    }
} 