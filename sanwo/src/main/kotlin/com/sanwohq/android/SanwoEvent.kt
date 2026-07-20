package com.sanwohq.android

/**
 * Events emitted by the payment checkout flow.
 */
enum class SanwoEvent(val value: String) {
    /** Payment completed successfully. */
    SUCCESS("success"),

    /** User explicitly cancelled the payment. */
    CANCELLED("cancelled"),

    /** Payment modal was closed (may or may not indicate cancellation). */
    CLOSED("closed"),

    /** An error occurred during payment. */
    ERROR("error"),

    /** Payment UI has loaded and is ready. */
    LOADED("loaded");

    companion object {
        /**
         * Resolves a [SanwoEvent] from its string value, or `null` if unrecognised.
         */
        fun fromValue(value: String): SanwoEvent? =
            entries.firstOrNull { it.value == value }
    }
}

/**
 * Payload delivered with a [SanwoEvent].
 *
 * @property event The event type.
 * @property data Arbitrary key-value data associated with the event.
 */
data class SanwoEventPayload(
    val event: SanwoEvent,
    val data: Map<String, Any?> = emptyMap(),
)

/**
 * A simple, thread-safe event emitter for [SanwoEvent].
 */
internal class SanwoEventEmitter {

    private val listeners = mutableMapOf<SanwoEvent, MutableList<(SanwoEventPayload) -> Unit>>()

    /**
     * Register a listener for [event]. Returns a removal handle.
     */
    @Synchronized
    fun on(event: SanwoEvent, listener: (SanwoEventPayload) -> Unit): () -> Unit {
        val list = listeners.getOrPut(event) { mutableListOf() }
        list.add(listener)
        return {
            synchronized(this) {
                list.remove(listener)
            }
        }
    }

    /**
     * Emit an event, invoking all registered listeners.
     */
    @Synchronized
    fun emit(event: SanwoEvent, data: Map<String, Any?> = emptyMap()) {
        val payload = SanwoEventPayload(event = event, data = data)
        listeners[event]?.toList()?.forEach { it(payload) }
    }

    /**
     * Remove all listeners.
     */
    @Synchronized
    fun clear() {
        listeners.clear()
    }
}
