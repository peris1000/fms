package com.zimono.trg.a.config;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class DatabaseLivenessCheckTest {

    @Inject
    @Liveness
    DatabaseLivenessCheck databaseLivenessCheck;

    @InjectMock
    DataSource dataSource;

    @Test
    public void testCall_Success() throws SQLException {
        // Given
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        doNothing().when(connection).close();

        // When
        HealthCheckResponse response = databaseLivenessCheck.call();

        // Then
        assertNotNull(response);
        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("Database connection", response.getName());
        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).close();
    }

    @Test
    public void testCall_Failure() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

        // When
        HealthCheckResponse response = databaseLivenessCheck.call();

        // Then
        assertNotNull(response);
        assertEquals(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertEquals("Database connection", response.getName());
        verify(dataSource, times(1)).getConnection();
    }

    @Test
    public void testCall_ConnectionCloseFailure() throws SQLException {
        // Given
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        doThrow(new SQLException("Close failed")).when(connection).close();

        // When
        HealthCheckResponse response = databaseLivenessCheck.call();

        // Then
        assertNotNull(response);
        assertEquals(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertEquals("Database connection", response.getName());
        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).close();
    }
}
