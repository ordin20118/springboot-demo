package com.example.demo.repository

import com.example.demo.domain.User
import com.example.demo.domain.UserState
import com.example.demo.domain.QUser
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryCustom(
    private val jpaQueryFactory: JPAQueryFactory
) {
    
    fun findUsersByState(state: UserState): List<User> {
        val qUser = QUser.user
        return jpaQueryFactory.selectFrom(qUser)
            .where(qUser.state.eq(state))
            .fetch()
    }
    
    fun findUsersByEmailContaining(email: String): List<User> {
        val qUser = QUser.user
        return jpaQueryFactory.selectFrom(qUser)
            .where(qUser.email.containsIgnoreCase(email))
            .fetch()
    }
    
    fun findUsersByNicknameContaining(nickname: String): List<User> {
        val qUser = QUser.user
        return jpaQueryFactory.selectFrom(qUser)
            .where(qUser.nickname.containsIgnoreCase(nickname))
            .fetch()
    }
    
    fun findUsersByComplexCondition(nickname: String?, state: UserState?): List<User> {
        val qUser = QUser.user
        return jpaQueryFactory.selectFrom(qUser)
            .where(
                nickname?.let { qUser.nickname.containsIgnoreCase(it) },
                state?.let { qUser.state.eq(it) }
            )
            .fetch()
    }
    
    fun findActiveUsers(): List<User> {
        val qUser = QUser.user
        return jpaQueryFactory.selectFrom(qUser)
            .where(qUser.state.eq(UserState.ACTIVE))
            .fetch()
    }
    
    fun findUsersByAgeRange(minAge: Int, maxAge: Int): List<User> {
        val qUser = QUser.user
        return jpaQueryFactory.selectFrom(qUser)
            .where(qUser.age.between(minAge, maxAge))
            .fetch()
    }
    
    fun findUsersByGender(gender: com.example.demo.domain.Gender): List<User> {
        val qUser = QUser.user
        return jpaQueryFactory.selectFrom(qUser)
            .where(qUser.gender.eq(gender))
            .fetch()
    }
    
    fun findUsersWithSocialAccounts(): List<User> {
        val qUser = QUser.user
        return jpaQueryFactory.selectFrom(qUser)
            .where(qUser.socialAccounts.isNotEmpty)
            .fetch()
    }
} 