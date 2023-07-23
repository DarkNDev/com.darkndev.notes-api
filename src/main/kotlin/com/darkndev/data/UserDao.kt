package com.darkndev.data

import com.darkndev.data.NoteDatabaseFactory.userQuery
import com.darkndev.models.User
import com.darkndev.models.Users
import org.jetbrains.exposed.sql.*

class UserDao {

    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id],
        emailId = row[Users.emailId],
        password = row[Users.password],
        salt = row[Users.salt],
        verified = row[Users.verified]
    )

    suspend fun allUsers(): List<User> = userQuery {
        Users.selectAll().map(::resultRowToUser)
    }

    suspend fun getUserByUsername(emailId: String): User? = userQuery {
        Users.select { Users.emailId eq emailId }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    suspend fun insertUser(emailId: String, password: String, salt: String): User? = userQuery {
        val insertStatement = Users.insert {
            it[Users.emailId] = emailId
            it[Users.password] = password
            it[Users.salt] = salt
            it[verified] = false
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToUser)
    }

    suspend fun verifyUser(emailId: String) = userQuery {
        Users.update({ Users.emailId eq emailId }) {
            it[verified] = true
        } > 0
    }
}