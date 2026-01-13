package com.edgerush.lootman.domain.simulation.model

/**
 * Enumeration of simulation request statuses.
 */
enum class SimulationStatus {
    /** Request created but not yet started */
    PENDING,

    /** Simulation is currently running */
    RUNNING,

    /** Simulation completed successfully */
    COMPLETED,

    /** Simulation failed with an error */
    FAILED
}
