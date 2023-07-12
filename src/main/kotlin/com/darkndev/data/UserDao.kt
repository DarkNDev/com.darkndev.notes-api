package com.darkndev.data

import com.darkndev.data.NoteDatabaseFactory.userQuery
import com.darkndev.models.User
import com.darkndev.models.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class UserDao {

    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        password = row[Users.password],
        salt = row[Users.salt],
        updated = row[Users.updated]
    )

    suspend fun allUsers(): List<User> = userQuery {
        Users.selectAll().map(::resultRowToUser)
    }

    suspend fun getUserByUsername(username: String): User? = userQuery {
        Users.select { Users.username eq username }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    suspend fun insertUser(username: String, password: String, salt: String): User? = userQuery {
        val insertStatement = Users.insert {
            it[Users.username] = username
            it[Users.password] = password
            it[Users.salt] = salt
            it[Users.updated] = System.currentTimeMillis()
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToUser)
    }
}