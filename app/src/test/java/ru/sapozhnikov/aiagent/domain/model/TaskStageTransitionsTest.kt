package ru.sapozhnikov.aiagent.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskStageTransitionsTest {

    @Test
    fun `isValidTransition allows forward transitions`() {
        assertTrue(TaskStageTransitions.isValidTransition(TaskStage.DATA_COLLECTION, TaskStage.PLANNING))
        assertTrue(TaskStageTransitions.isValidTransition(TaskStage.PLANNING, TaskStage.EXECUTION))
        assertTrue(TaskStageTransitions.isValidTransition(TaskStage.EXECUTION, TaskStage.VALIDATION))
        assertTrue(TaskStageTransitions.isValidTransition(TaskStage.VALIDATION, TaskStage.DONE))
    }

    @Test
    fun `isValidTransition allows backward transitions one step`() {
        assertTrue(TaskStageTransitions.isValidTransition(TaskStage.PLANNING, TaskStage.DATA_COLLECTION))
        assertTrue(TaskStageTransitions.isValidTransition(TaskStage.EXECUTION, TaskStage.PLANNING))
        assertTrue(TaskStageTransitions.isValidTransition(TaskStage.VALIDATION, TaskStage.EXECUTION))
        assertTrue(TaskStageTransitions.isValidTransition(TaskStage.DONE, TaskStage.VALIDATION))
    }

    @Test
    fun `isBackwardTransition detects direction`() {
        assertTrue(TaskStageTransitions.isBackwardTransition(TaskStage.PLANNING, TaskStage.DATA_COLLECTION))
        assertTrue(TaskStageTransitions.isBackwardTransition(TaskStage.DONE, TaskStage.VALIDATION))
        assertFalse(TaskStageTransitions.isBackwardTransition(TaskStage.DATA_COLLECTION, TaskStage.PLANNING))
    }

    @Test
    fun `isValidTransition rejects skipped stages`() {
        assertFalse(TaskStageTransitions.isValidTransition(TaskStage.DATA_COLLECTION, TaskStage.EXECUTION))
        assertFalse(TaskStageTransitions.isValidTransition(TaskStage.DATA_COLLECTION, TaskStage.DONE))
        assertFalse(TaskStageTransitions.isValidTransition(TaskStage.PLANNING, TaskStage.VALIDATION))
        assertFalse(TaskStageTransitions.isValidTransition(TaskStage.EXECUTION, TaskStage.DONE))
        assertFalse(TaskStageTransitions.isValidTransition(TaskStage.VALIDATION, TaskStage.PLANNING))
        assertFalse(TaskStageTransitions.isValidTransition(TaskStage.DONE, TaskStage.EXECUTION))
    }

    @Test
    fun `parseTransitionMarker reads stage from marker`() {
        assertEquals(
            TaskStage.PLANNING,
            TaskStageTransitions.parseTransitionMarker("Готово.\n[[TRANSITION:PLANNING]]"),
        )
        assertEquals(
            TaskStage.DATA_COLLECTION,
            TaskStageTransitions.parseTransitionMarker("[[transition:data_collection]]"),
        )
    }

    @Test
    fun `parseTransitionMarker returns null for invalid marker`() {
        assertNull(TaskStageTransitions.parseTransitionMarker("[[TRANSITION:UNKNOWN]]"))
        assertNull(TaskStageTransitions.parseTransitionMarker("no marker here"))
    }

    @Test
    fun `stripTransitionMarkers removes marker from text`() {
        assertEquals(
            "Итог сбора данных.",
            TaskStageTransitions.stripTransitionMarkers("Итог сбора данных.\n[[TRANSITION:PLANNING]]"),
        )
    }
}
