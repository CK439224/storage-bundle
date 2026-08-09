package com.storagebundle.core.common.result

/**
 * The result of an operation that can fail in a way the caller must handle.
 *
 * The coding-standards brief asks for functions to signal errors with an unambiguous return
 * value. A sealed hierarchy serves that intent better than a magic `0`/`1` (PLAN.md §7): it
 * carries *why* the operation failed, and the compiler enforces that callers handle both
 * branches rather than silently ignoring a status code.
 *
 * @param T the value produced on success.
 */
sealed interface Outcome<out T> {

    /** The operation succeeded and produced [value]. */
    data class Success<out T>(val value: T) : Outcome<T>

    /**
     * The operation failed.
     *
     * @property reason machine-readable classification, used to choose UI copy.
     * @property cause the originating exception, if any. Never surfaced to the user directly —
     *   error text shown in the UI is derived from [reason] (PLAN.md §6, Error Handling).
     */
    data class Failure(
        val reason: FailureReason,
        val cause: Throwable? = null,
    ) : Outcome<Nothing>
}

/** Why an [Outcome.Failure] occurred. Drives user-facing copy without leaking internals. */
enum class FailureReason {
    /** A required runtime permission has not been granted. */
    PermissionDenied,

    /** The underlying item no longer exists — for example a deleted media entry. */
    NotFound,

    /** The user declined a system confirmation dialog, such as a delete request. */
    UserCancelled,

    /** Reading or writing device storage failed. */
    StorageUnavailable,

    /** The operation was cancelled by the system, typically process death mid-scan. */
    Interrupted,

    /** Anything not otherwise classified. */
    Unknown,
}

/** Returns the success value, or `null` when this is a [Outcome.Failure]. */
fun <T> Outcome<T>.getOrNull(): T? = when (this) {
    is Outcome.Success -> value
    is Outcome.Failure -> null
}

/** Returns the success value, or [fallback] when this is a [Outcome.Failure]. */
fun <T> Outcome<T>.getOrDefault(fallback: T): T = getOrNull() ?: fallback

/** Maps a successful value through [transform], leaving a failure untouched. */
inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}

/** Runs [block] only when this is a [Outcome.Success]. */
inline fun <T> Outcome<T>.onSuccess(block: (T) -> Unit): Outcome<T> {
    if (this is Outcome.Success) block(value)
    return this
}

/** Runs [block] only when this is a [Outcome.Failure]. */
inline fun <T> Outcome<T>.onFailure(block: (Outcome.Failure) -> Unit): Outcome<T> {
    if (this is Outcome.Failure) block(this)
    return this
}
