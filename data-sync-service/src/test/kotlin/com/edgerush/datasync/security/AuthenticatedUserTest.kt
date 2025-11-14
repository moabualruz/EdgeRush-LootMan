package com.edgerush.datasync.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AuthenticatedUserTest {
    
    @Test
    fun `isSystemAdmin should return true for SYSTEM_ADMIN role`() {
        val user = AuthenticatedUser(
            id = "user-1",
            username = "admin",
            roles = listOf("SYSTEM_ADMIN")
        )
        
        assertTrue(user.isSystemAdmin())
    }
    
    @Test
    fun `isSystemAdmin should return true for admin mode`() {
        val user = AuthenticatedUser(
            id = "user-1",
            username = "user",
            roles = listOf("PUBLIC_USER"),
            isAdminMode = true
        )
        
        assertTrue(user.isSystemAdmin())
    }
    
    @Test
    fun `isGuildAdmin should return true for GUILD_ADMIN role`() {
        val user = AuthenticatedUser(
            id = "user-1",
            username = "guildadmin",
            roles = listOf("GUILD_ADMIN")
        )
        
        assertTrue(user.isGuildAdmin())
    }
    
    @Test
    fun `isGuildAdmin should return true for SYSTEM_ADMIN`() {
        val user = AuthenticatedUser(
            id = "user-1",
            username = "admin",
            roles = listOf("SYSTEM_ADMIN")
        )
        
        assertTrue(user.isGuildAdmin())
    }
    
    @Test
    fun `hasGuildAccess should return true for system admin`() {
        val user = AuthenticatedUser(
            id = "user-1",
            username = "admin",
            roles = listOf("SYSTEM_ADMIN")
        )
        
        assertTrue(user.hasGuildAccess("any-guild"))
    }
    
    @Test
    fun `hasGuildAccess should return true for guild in list`() {
        val user = AuthenticatedUser(
            id = "user-1",
            username = "user",
            roles = listOf("GUILD_ADMIN"),
            guildIds = listOf("guild-1", "guild-2")
        )
        
        assertTrue(user.hasGuildAccess("guild-1"))
        assertTrue(user.hasGuildAccess("guild-2"))
        assertFalse(user.hasGuildAccess("guild-3"))
    }
    
    @Test
    fun `adminModeUser should have system admin privileges`() {
        val user = AuthenticatedUser.adminModeUser()
        
        assertTrue(user.isAdminMode)
        assertTrue(user.isSystemAdmin())
        assertTrue(user.isGuildAdmin())
        assertTrue(user.hasGuildAccess("any-guild"))
    }
}
