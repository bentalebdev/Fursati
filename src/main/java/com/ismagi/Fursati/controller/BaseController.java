package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Base controller that provides common functionality for all controllers
 * Specifically handles authentication and access control
 */
public abstract class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    /**
     * Gets the authenticated user ID from the session
     * Throws an exception if the user is not authenticated
     */
    protected Object getAuthenticatedUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return session.getAttribute("userId");
    }

    /**
     * Gets the authenticated user type from the session
     * Throws an exception if the user is not authenticated
     */
    protected AuthService.UserType getAuthenticatedUserType(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userType") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return (AuthService.UserType) session.getAttribute("userType");
    }

    /**
     * Verifies that the authenticated user matches the requested user ID
     * Used to ensure users can only access their own data
     * Admin users bypass this check (they can access any data)
     */
    protected void verifyAuthenticatedUser(HttpServletRequest request, Long requestedUserId) {
        // If the user is an admin, allow access to any user's data
        AuthService.UserType userType = getAuthenticatedUserType(request);
        if (userType == AuthService.UserType.ADMIN) {
            return;
        }

        // For non-admin users, ensure they can only access their own data
        Object userId = getAuthenticatedUserId(request);
        if (userId instanceof Integer) {
            // Special case for comparing Integer with Long
            if (requestedUserId.intValue() != (Integer) userId) {
                logger.warn("Unauthorized access attempt: User {} tried to access data for user {}", userId, requestedUserId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to access this resource");
            }
        } else if (userId instanceof Long) {
            // Normal case comparing Long with Long
            if (!requestedUserId.equals(userId)) {
                logger.warn("Unauthorized access attempt: User {} tried to access data for user {}", userId, requestedUserId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to access this resource");
            }
        } else {
            // This shouldn't happen with proper type handling
            logger.error("Unexpected user ID type: {}", userId != null ? userId.getClass().getName() : "null");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected user ID type");
        }
    }

    /**
     * Gets the user ID as Long, converting from Integer if necessary
     */
    protected Long getUserIdAsLong(HttpServletRequest request) {
        Object userId = getAuthenticatedUserId(request);
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        } else {
            logger.error("Unexpected user ID type: {}", userId != null ? userId.getClass().getName() : "null");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected user ID type");
        }
    }
}